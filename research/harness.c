/*
 * NetherNet SCTP reproduction harness.
 *
 * Sender process: usrsctp configured like libdatachannel + Cloudburst Network (the Geyser side).
 * Receiver process: usrsctp configured to acknowledge like dcSCTP (the Bedrock client): SACK every
 *                   second packet, otherwise after a 200 ms delayed-ack timer, at once on the I-bit.
 * Link: a datagram socketpair; each side holds its outgoing packets for rtt/2. No loss.
 *
 * Traffic: one batch per 50 ms tick (Geyser flushes per tick), cut into <= 10000 byte
 *          messages as NetherNetChannel segments them, optionally a one-off burst at 1 s (join).
 *          Each message carries the time it was produced, so the receiver measures the
 *          latency an application sees, send buffers included.
 *
 * usage: harness rtt_ms min_rto_ms ibit(0 off|1 always|2 cwnd<10mtu) bytes_per_tick burst_bytes seconds
 */
#include <errno.h>
#include <pthread.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/wait.h>
#include <time.h>
#include <unistd.h>
#include <usrsctp.h>

#define MAX_MSG 10000
#define TICK_MS 50
#define MTU_SCTP (1280 - 12 - 48 - 8 - 40) /* what libdatachannel configures */

static int rtt_ms, min_rto, ibit, bytes_per_tick, burst_bytes, seconds;
static int link_fd;
static volatile int running = 1;

static uint64_t now_us(void) {
	struct timespec ts;
	clock_gettime(CLOCK_MONOTONIC, &ts);
	return (uint64_t)ts.tv_sec * 1000000 + ts.tv_nsec / 1000;
}

/* ---------- delay line on the way out ---------- */
struct pkt { struct pkt *next; uint64_t due; size_t len; char data[]; };
static struct pkt *head, *tail;
static pthread_mutex_t q_lock = PTHREAD_MUTEX_INITIALIZER;
static pthread_cond_t q_cond = PTHREAD_COND_INITIALIZER;
static uint64_t pkts_out;

static int conn_output(void *addr, void *buf, size_t len, uint8_t tos, uint8_t set_df) {
	(void)addr; (void)tos; (void)set_df;
	static double loss = -1;
	if (loss < 0) loss = getenv("LOSS") ? atof(getenv("LOSS")) : 0;
	if (loss > 0 && (double)rand() / RAND_MAX < loss) return 0; /* dropped on the link */
	struct pkt *p = malloc(sizeof(*p) + len);
	p->next = NULL;
	p->due = now_us() + (uint64_t)rtt_ms * 500;
	p->len = len;
	memcpy(p->data, buf, len);
	pthread_mutex_lock(&q_lock);
	pkts_out++;
	if (tail) tail->next = p; else head = p;
	tail = p;
	pthread_cond_signal(&q_cond);
	pthread_mutex_unlock(&q_lock);
	return 0;
}

static void *link_writer(void *arg) {
	(void)arg;
	for (;;) {
		pthread_mutex_lock(&q_lock);
		while (!head) pthread_cond_wait(&q_cond, &q_lock);
		struct pkt *p = head;
		uint64_t t = now_us();
		if (p->due > t) {
			pthread_mutex_unlock(&q_lock);
			usleep((useconds_t)(p->due - t));
			continue;
		}
		head = p->next;
		if (!head) tail = NULL;
		pthread_mutex_unlock(&q_lock);
		(void)send(link_fd, p->data, p->len, 0);
		free(p);
	}
	return NULL;
}

static void *link_reader(void *arg) {
	(void)arg;
	char buf[65536];
	for (;;) {
		ssize_t n = recv(link_fd, buf, sizeof(buf), 0);
		if (n <= 0) break;
		usrsctp_conninput(&link_fd, buf, (size_t)n, 0);
	}
	return NULL;
}

static void init_stack(void) {
	usrsctp_init(0, conn_output, NULL);
	/* libdatachannel SctpTransport::Init + SetSettings, with Network's minimum RTO */
	usrsctp_sysctl_set_sctp_pr_enable(1);
	usrsctp_sysctl_set_sctp_ecn_enable(0);
	usrsctp_sysctl_set_sctp_recvspace(1024 * 1024);
	usrsctp_sysctl_set_sctp_sendspace(1024 * 1024);
	usrsctp_sysctl_set_sctp_max_chunks_on_queue(10 * 1024);
	usrsctp_sysctl_set_sctp_initial_cwnd(10);
	usrsctp_sysctl_set_sctp_max_burst_default(10);
	usrsctp_sysctl_set_sctp_default_cc_module(getenv("CC") ? atoi(getenv("CC")) : 0);
	usrsctp_sysctl_set_sctp_delayed_sack_time_default(20);
	usrsctp_sysctl_set_sctp_rto_min_default(min_rto);
	usrsctp_sysctl_set_sctp_rto_max_default(10000);
	usrsctp_sysctl_set_sctp_init_rto_max_default(10000);
	usrsctp_sysctl_set_sctp_rto_initial_default(1000);
	usrsctp_sysctl_set_sctp_init_rtx_max_default(5);
	usrsctp_sysctl_set_sctp_assoc_rtx_max_default(5);
	usrsctp_sysctl_set_sctp_path_rtx_max_default(5);
	usrsctp_sysctl_set_sctp_heartbeat_interval_default(10000);
	usrsctp_register_address(&link_fd);
	pthread_t a, b;
	pthread_create(&a, NULL, link_writer, NULL);
	pthread_create(&b, NULL, link_reader, NULL);
}

static void setup_common(struct socket *s) {
	int on = 1;
	usrsctp_setsockopt(s, IPPROTO_SCTP, SCTP_NODELAY, &on, sizeof(on));
	struct sctp_paddrparams spp;
	memset(&spp, 0, sizeof(spp));
	spp.spp_flags = SPP_HB_ENABLE | SPP_PMTUD_DISABLE;
	spp.spp_pathmtu = MTU_SCTP;
	usrsctp_setsockopt(s, IPPROTO_SCTP, SCTP_PEER_ADDR_PARAMS, &spp, sizeof(spp));
}

static int cmp_u32(const void *a, const void *b) {
	uint32_t x = *(const uint32_t *)a, y = *(const uint32_t *)b;
	return x < y ? -1 : x > y;
}

static uint32_t pct(uint32_t *sorted, size_t n, int p) {
	if (!n) return 0;
	size_t i = n * (size_t)p / 100;
	return sorted[i >= n ? n - 1 : i];
}

/* ================= receiver ================= */
#define LAT_MAX 4000000
static uint32_t *lat_ms;
static size_t lat_n;
static uint64_t bytes_rx;
static pthread_mutex_t lat_lock = PTHREAD_MUTEX_INITIALIZER;

static int on_receive(struct socket *sock, union sctp_sockstore addr, void *data, size_t datalen,
                      struct sctp_rcvinfo rcv, int flags, void *ulp_info) {
	(void)sock; (void)addr; (void)rcv; (void)ulp_info;
	if (data == NULL) return 1;
	if (!(flags & MSG_NOTIFICATION) && datalen >= 8) {
		uint64_t t;
		memcpy(&t, data, sizeof(t));
		uint64_t n = now_us();
		pthread_mutex_lock(&lat_lock);
		bytes_rx += datalen;
		if (lat_n < LAT_MAX) lat_ms[lat_n++] = (uint32_t)((n - t) / 1000);
		pthread_mutex_unlock(&lat_lock);
	}
	free(data);
	return 1;
}

static int run_receiver(int result_fd) {
	lat_ms = malloc(sizeof(uint32_t) * LAT_MAX);
	init_stack();
	struct socket *lsock = usrsctp_socket(AF_CONN, SOCK_STREAM, IPPROTO_SCTP, on_receive, NULL, 0, NULL);
	setup_common(lsock);
	/* dcSCTP: delayed ack min(RTO/2, 200 ms), its RTO is >= 400 ms, so 200 ms; ack every 2nd packet */
	struct sctp_sack_info sack;
	memset(&sack, 0, sizeof(sack));
	sack.sack_assoc_id = SCTP_FUTURE_ASSOC;
	sack.sack_delay = 200;
	sack.sack_freq = 2;
	if (usrsctp_setsockopt(lsock, IPPROTO_SCTP, SCTP_DELAYED_SACK, &sack, sizeof(sack))) perror("SCTP_DELAYED_SACK");
	struct sockaddr_conn sc;
	memset(&sc, 0, sizeof(sc));
	sc.sconn_family = AF_CONN;
	sc.sconn_port = htons(5000);
	sc.sconn_addr = &link_fd;
	if (usrsctp_bind(lsock, (struct sockaddr *)&sc, sizeof(sc))) { perror("bind"); return 1; }
	usrsctp_listen(lsock, 1);
	struct socket *asock = usrsctp_accept(lsock, NULL, NULL);
	if (!asock) { perror("accept"); return 1; }
	uint64_t start = now_us();
	sleep((unsigned)seconds);
	double elapsed = (now_us() - start) / 1e6;
	pthread_mutex_lock(&lat_lock);
	qsort(lat_ms, lat_n, sizeof(uint32_t), cmp_u32);
	char line[512];
	int len = snprintf(line, sizeof(line),
	                   "goodput %.1f KiB/s | latency p50 %u p95 %u p99 %u max %u ms | acks sent %lu",
	                   bytes_rx / 1024.0 / elapsed, pct(lat_ms, lat_n, 50), pct(lat_ms, lat_n, 95),
	                   pct(lat_ms, lat_n, 99), lat_n ? lat_ms[lat_n - 1] : 0, (unsigned long)pkts_out);
	pthread_mutex_unlock(&lat_lock);
	(void)write(result_fd, line, (size_t)len);
	close(result_fd);
	_exit(0);
}

/* ================= sender ================= */
struct msg { struct msg *next; size_t len; char data[]; };
static struct msg *mq_head, *mq_tail;
static uint64_t mq_bytes, mq_bytes_max;
static pthread_mutex_t mq_lock = PTHREAD_MUTEX_INITIALIZER;
static pthread_cond_t mq_cond = PTHREAD_COND_INITIALIZER;
static struct socket *snd_sock;

static void produce(size_t total) {
	while (total > 0) {
		size_t len = total > MAX_MSG ? MAX_MSG : total;
		size_t alloc = len < 16 ? 16 : len;
		struct msg *m = malloc(sizeof(*m) + alloc);
		m->next = NULL;
		m->len = alloc;
		memset(m->data, 0x5a, alloc);
		uint64_t t = now_us();
		memcpy(m->data, &t, sizeof(t));
		pthread_mutex_lock(&mq_lock);
		if (mq_tail) mq_tail->next = m; else mq_head = m;
		mq_tail = m;
		mq_bytes += alloc;
		if (mq_bytes > mq_bytes_max) mq_bytes_max = mq_bytes;
		pthread_cond_signal(&mq_cond);
		pthread_mutex_unlock(&mq_lock);
		total -= len;
	}
}

static uint32_t cwnd_now(void) {
	struct sctp_status st;
	socklen_t l = sizeof(st);
	memset(&st, 0, sizeof(st));
	if (usrsctp_getsockopt(snd_sock, IPPROTO_SCTP, SCTP_STATUS, &st, &l)) return 0;
	return st.sstat_primary.spinfo_cwnd;
}

static void *sender(void *arg) {
	(void)arg;
	while (running) {
		pthread_mutex_lock(&mq_lock);
		while (!mq_head && running) pthread_cond_wait(&mq_cond, &mq_lock);
		struct msg *m = mq_head;
		if (!m) { pthread_mutex_unlock(&mq_lock); break; }
		mq_head = m->next;
		if (!mq_head) mq_tail = NULL;
		pthread_mutex_unlock(&mq_lock);

		struct sctp_sndinfo si;
		memset(&si, 0, sizeof(si));
		si.snd_ppid = htonl(53); /* binary */
		si.snd_flags = SCTP_EOR;
		if (ibit == 1 || (ibit == 2 && cwnd_now() < 10 * MTU_SCTP)) si.snd_flags |= SCTP_SACK_IMMEDIATELY;
		/* blocking: waits while usrsctp's 1 MiB send buffer is full; the app queue grows meanwhile */
		while (running && usrsctp_sendv(snd_sock, m->data, m->len, NULL, 0, &si, sizeof(si),
		                                 SCTP_SENDV_SNDINFO, 0) < 0) {
			if (errno != EAGAIN && errno != EWOULDBLOCK) { perror("sendv"); break; }
			usleep(1000);
		}
		pthread_mutex_lock(&mq_lock);
		mq_bytes -= m->len;
		pthread_mutex_unlock(&mq_lock);
		free(m);
	}
	return NULL;
}

int main(int argc, char **argv) {
	if (argc < 7) {
		fprintf(stderr, "usage: %s rtt_ms min_rto_ms ibit bytes_per_tick burst_bytes seconds\n", argv[0]);
		return 2;
	}
	rtt_ms = atoi(argv[1]); min_rto = atoi(argv[2]); ibit = atoi(argv[3]);
	bytes_per_tick = atoi(argv[4]); burst_bytes = atoi(argv[5]); seconds = atoi(argv[6]);

	int sp[2], res[2];
	if (socketpair(AF_UNIX, SOCK_DGRAM, 0, sp) || pipe(res)) { perror("socketpair"); return 1; }
	int bufsz = 8 << 20;
	for (int i = 0; i < 2; i++) {
		setsockopt(sp[i], SOL_SOCKET, SO_SNDBUF, &bufsz, sizeof(bufsz));
		setsockopt(sp[i], SOL_SOCKET, SO_RCVBUF, &bufsz, sizeof(bufsz));
	}
	pid_t child = fork();
	if (child == 0) {
		close(sp[0]); close(res[0]);
		link_fd = sp[1];
		return run_receiver(res[1]);
	}
	close(sp[1]); close(res[1]);
	link_fd = sp[0];
	init_stack();

	snd_sock = usrsctp_socket(AF_CONN, SOCK_STREAM, IPPROTO_SCTP, NULL, NULL, 0, NULL);
	setup_common(snd_sock);
	struct sockaddr_conn sc;
	memset(&sc, 0, sizeof(sc));
	sc.sconn_family = AF_CONN;
	sc.sconn_port = htons(5000);
	sc.sconn_addr = &link_fd;
	if (usrsctp_bind(snd_sock, (struct sockaddr *)&sc, sizeof(sc))) { perror("bind"); return 1; }
	usleep(100000); /* let the receiver listen */
	if (usrsctp_connect(snd_sock, (struct sockaddr *)&sc, sizeof(sc))) { perror("connect"); return 1; }

	pthread_t st;
	pthread_create(&st, NULL, sender, NULL);

	uint64_t start = now_us();
	uint64_t next_tick = start;
	int ticks = seconds * 1000 / TICK_MS - 10; /* stop producing a little before the receiver reports */
	static uint32_t cw[100000];
	size_t cwn = 0;
	for (int i = 0; i < ticks; i++) {
		if (i == 20 && burst_bytes > 0) produce((size_t)burst_bytes);
		if (bytes_per_tick > 0) produce((size_t)bytes_per_tick);
		if (cwn < 100000) cw[cwn++] = cwnd_now();
		next_tick += TICK_MS * 1000;
		uint64_t n = now_us();
		if (next_tick > n) usleep((useconds_t)(next_tick - n));
	}
	double elapsed = (now_us() - start) / 1e6;

	struct sctp_timeouts to;
	memset(&to, 0, sizeof(to));
	socklen_t l = sizeof(to);
	usrsctp_getsockopt(snd_sock, IPPROTO_SCTP, SCTP_TIMEOUTS, &to, &l);
	struct sctpstat stat;
	usrsctp_get_stat(&stat);
	qsort(cw, cwn, sizeof(uint32_t), cmp_u32);
	uint64_t left;
	pthread_mutex_lock(&mq_lock);
	left = mq_bytes;
	pthread_mutex_unlock(&mq_lock);

	char rline[512] = {0};
	ssize_t rn = read(res[0], rline, sizeof(rline) - 1);
	if (rn < 0) rn = 0;
	rline[rn] = 0;
	waitpid(child, NULL, 0);

	uint64_t offered = (uint64_t)bytes_per_tick * (uint64_t)ticks + (uint64_t)burst_bytes;
	printf("rtt=%-3d minrto=%-4d ibit=%d tick=%-5d burst=%-7d | T3 %.2f/s, marked %u, rtx chunks %u, fast %u | cwnd p10 %u p50 %u p90 %u | "
	       "offered %.1f KiB/s, app queue max %.0f KiB left %.0f KiB | data pkts %lu | %s\n",
	       rtt_ms, min_rto, ibit, bytes_per_tick, burst_bytes, to.stimo_data / elapsed,
	       stat.sctps_markedretrans, stat.sctps_sendretransdata, stat.sctps_sendfastretrans,
	       pct(cw, cwn, 10), pct(cw, cwn, 50), pct(cw, cwn, 90),
	       offered / 1024.0 / elapsed, mq_bytes_max / 1024.0, left / 1024.0, (unsigned long)pkts_out, rline);
	fflush(stdout);
	_exit(0);
}
