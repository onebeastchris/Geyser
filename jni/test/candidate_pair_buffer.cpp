#include "candidate_pair_buffer.hpp"
#include <cassert>
#include <cstring>
#include <iostream>

int main() {
    std::string local, remote;
    int calls = 0;
    auto stable = [&](int, char *l, int ls, char *r, int rs) {
        ++calls;
        const std::string value(512, 'a');
        if (!l) return int(value.size() + 1);
        assert(ls == 513 && rs == 513);
        std::memcpy(l, value.c_str(), 513);
        std::memcpy(r, value.c_str(), 513);
        return 513;
    };
    assert(candidate_pair_buffer::read(1, local, remote, stable) == 513);
    assert(calls == 2 && local.size() == 512 && remote == local);
    calls = 0;
    auto growing = [&](int, char *l, int ls, char *r, int rs) {
        ++calls;
        if (!l) return calls == 1 ? 10 : 20;
        if (calls == 2) return RTC_ERR_TOO_SMALL;
        assert(ls == 20 && rs == 20);
        std::strcpy(l, "local"); std::strcpy(r, "remote");
        return 7;
    };
    assert(candidate_pair_buffer::read(1, local, remote, growing) == 7);
    assert(calls == 4 && local == "local" && remote == "remote");
    calls = 0;
    auto oversize = [&](int, char *l, int, char *, int) {
        ++calls; assert(!l); return candidate_pair_buffer::max_size + 1;
    };
    assert(candidate_pair_buffer::read(1, local, remote, oversize) == RTC_ERR_TOO_SMALL);
    assert(calls == 1 && local == "local" && remote == "remote");
    calls = 0;
    auto unstable = [&](int, char *l, int, char *, int) {
        ++calls; return l ? RTC_ERR_TOO_SMALL : 20;
    };
    assert(candidate_pair_buffer::read(1, local, remote, unstable) == RTC_ERR_TOO_SMALL && calls == 6);
    auto unavailable = [](int, char *, int, char *, int) { return RTC_ERR_NOT_AVAIL; };
    assert(candidate_pair_buffer::read(1, local, remote, unavailable) == RTC_ERR_NOT_AVAIL);
    std::cout << "candidate-pair buffers PASS: dynamic size, bounded growth retries, oversize rejection and unavailable\n";
}
