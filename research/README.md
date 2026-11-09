# NetherNet SCTP reproduction (#6854)

Debug material for the NetherNet lag, not part of Geyser.

## What it shows

usrsctp, the SCTP stack inside libdatachannel, does not restart its retransmission (T3) timer when a SACK moves
the cumulative ack forward, as long as that SACK has no gap blocks (`sctp_express_handle_sack` only calls
`sctp_timer_start`, which leaves a running timer alone; the gap path `sctp_handle_sack` does stop it). RFC 4960
6.3.2 R3 asks for a restart, and dcSCTP (`RetransmissionQueue::HandleSack`) and Pion both do it.

So while data stays in flight without a break, the timer runs out RTO after it was armed, however promptly the
client acknowledges. Each expiry doubles the RTO and re-arms the timer with it; the next SACK only puts the RTO back
for the next arming. The result is one timeout per 2 x minimum RTO: 1.25/s at Network's 400 ms. That matches the
reports exactly: Table9351 +6/+7 per 5 s, ShadowxKenji694 ~1.1/s, and FlashlightXi 1.15/s at 400 ms, 0.39/s at
1000 ms and 0.23/s at 2000 ms (predicted 0.5 and 0.25).

When the timer runs out, every chunk older than srtt + 4 x rttvar (without the RTO floor) is retransmitted. The
Bedrock client (dcSCTP, like BDS, which links stock libwebrtc `DcSctpTransport`) holds the SACK for a lone packet up
to 200 ms, so such chunks are common. Each such timeout sets cwnd to one MTU and halves ssthresh, down to 4 MTU.
Throughput stays at a few MTU per RTT, and anything sent faster piles up in libdatachannel's queue.

Data stays in flight without a break once the RTT is longer than Geyser's flush interval (one tick, 50 ms), or
whenever there is a backlog. That's why players at 15-20 ms barely see it and everyone further away does.

## Harness

`harness.c` runs two usrsctp stacks in two processes over a socketpair, each delaying its packets by RTT/2:

- the sender is set up like libdatachannel + Cloudburst Network (RFC 2581 CC, initial cwnd 10 MTU, max burst 10,
  1 MiB buffers, MTU 1172, minimum RTO 400 ms)
- the receiver acknowledges like dcSCTP: every second packet, otherwise after 200 ms, at once on the I-bit
- traffic is one batch per 50 ms tick, cut into 10000 byte messages; `join` adds a 4 MB burst at 1 s
- the receiver measures latency from when a message was produced, send buffers included

```
gcc -O2 -o harness-stock harness.c -I<usrsctp>/usrsctplib -L<usrsctp build>/usrsctplib -lusrsctp -lpthread
./harness-stock <rtt ms> <min rto ms> <ibit 0|1|2> <bytes per tick> <burst bytes> <seconds>
LOSS=0.01 ./harness-stock ...   # random loss on the link
```

Built against usrsctp `fec583d5` (libdatachannel's submodule in opencollab-incubator/libdatachannel `2e2d86ac`),
once as is and once with the fix (`upstream-patches/usrsctp/0001-*.patch`). The tables were measured with a first version of the patch that stopped the timer inside the re-arm loop; the final version (stop before the loop, as `sctp_handle_sack()` does) gave the same numbers within noise in a rerun of the key cases. Upstream sctplab/usrsctp master has the same code.

## Results (25 s runs, no loss, `results-lossless.txt`)

| traffic | RTT | stock: T3/s, goodput, p95 latency | patched: T3/s, goodput, p95 latency |
|---|---|---|---|
| game, 59 KiB/s | 30 ms | 0.00, 57 KiB/s, 15 ms | 0.00, 57 KiB/s, 15 ms |
| game | 70 ms | 1.22, 57, 55 ms | 0.00, 57, 35 ms |
| game | 155 ms | 1.22, 57, 176 ms | 0.00, 57, 78 ms |
| game | 318 ms | 1.76, **32**, **10.2 s** | 0.00, 57, 159 ms |
| heavy, 293 KiB/s | 70 ms | 1.22, 287, 129 ms | 0.00, 287, 56 ms |
| heavy | 155 ms | 1.22, **119**, **13.1 s** (3.2 MB queued) | 0.00, 287, 85 ms |
| heavy | 318 ms | 1.18, **108**, **11.4 s** | 0.00, 287, 401 ms |
| join, 4 MB burst | 30 ms | 0.16, 185, 2.5 s | 0.00, 185, 293 ms |
| join | 70 ms | 1.22, 185, 7.7 s | 0.00, 185, 672 ms |

Minimum RTO 1000 ms on stock usrsctp roughly halves the damage (0.49 T3/s) but still fails at 318 ms and under
heavy traffic. Setting the I-bit without the timer fix stops most spurious retransmissions but not the timeouts.

With 1 % and 3 % random loss (`results-loss.txt`) the patched stack is never worse than stock and often much better
(game traffic at 155 ms / 1 %: p95 3.9 s stock, 0.65 s patched). Heavy traffic with real loss is limited by
congestion control on every variant (Reno's ~1.22 MSS/RTT/sqrt(p)); H-TCP did not help.
