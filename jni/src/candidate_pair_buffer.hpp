#pragma once
#include <rtc/rtc.h>
#include <algorithm>
#include <cstring>
#include <string>
#include <vector>

namespace candidate_pair_buffer {
constexpr int max_size = 65536; // Includes terminating NUL, for each candidate.

// Native selected pairs may change between the sizing and copy calls. Retry a
// bounded number of times and publish both owned strings only after one copy.
template <typename Reader>
int read(int peer, std::string &local, std::string &remote, Reader reader) {
    for (int attempt = 0; attempt < 3; ++attempt) {
        const int size = reader(peer, nullptr, 0, nullptr, 0);
        if (size < 0) return size;
        if (size == 0) return RTC_ERR_FAILURE;
        if (size > max_size) return RTC_ERR_TOO_SMALL;
        std::vector<char> local_buffer(size), remote_buffer(size);
        const int result = reader(peer, local_buffer.data(), size, remote_buffer.data(), size);
        if (result == RTC_ERR_TOO_SMALL) continue;
        if (result < 0) return result;
        if (!std::memchr(local_buffer.data(), '\0', size) || !std::memchr(remote_buffer.data(), '\0', size)) return RTC_ERR_FAILURE;
        local.assign(local_buffer.data());
        remote.assign(remote_buffer.data());
        return result;
    }
    return RTC_ERR_TOO_SMALL;
}
}
