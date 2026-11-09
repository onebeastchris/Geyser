#include "callback.hpp"
#include "global_jvm.hpp"
#include <cstdint>
#include <mutex>
#include <new>
#include <shared_mutex>
#include <unordered_map>

#ifdef RTC_ENABLE_TEST_DIAGNOSTICS
#include <atomic>
#include <chrono>
#include <thread>
#endif

namespace {
using callback_map = std::unordered_map<uintptr_t, std::shared_ptr<const jvm_callback>>;
// Never destroyed, libdatachannel threads can still dispatch while the process exits
std::shared_mutex& registry_mutex = *new std::shared_mutex;
callback_map& registry = *new callback_map;
uintptr_t last_id = 0;

#ifdef RTC_ENABLE_TEST_DIAGNOSTICS
std::atomic<int> test_dispatch_delay{0};
std::atomic<int> test_released_lookups{0};
#endif

void delete_callback(const jvm_callback* callback) {
    // During unload there is no JVM left to hand the reference back to
    JNIEnv* env = callback->instance != nullptr ? get_jni_env() : nullptr;
    if (env != nullptr) {
        env->DeleteGlobalRef(callback->instance);
    }
    delete callback;
}
} // namespace

void* register_callback(JNIEnv* env, jobject listener) {
    try {
        std::shared_ptr<jvm_callback> callback(new jvm_callback{nullptr}, delete_callback);
        callback->instance = env->NewGlobalRef(listener);
        if (callback->instance == nullptr) {
            return nullptr;
        }
        std::unique_lock lock(registry_mutex);
        // 0 stays null, and a wrapped 32 bit counter skips ids still in use
        do {
            ++last_id;
        } while (last_id == 0 || registry.count(last_id) != 0);
        registry.emplace(last_id, std::move(callback));
        return reinterpret_cast<void*>(last_id);
    } catch (const std::bad_alloc&) {
        return nullptr;
    }
}

std::shared_ptr<const jvm_callback> acquire_callback(void* id) {
    if (id == nullptr) {
        return nullptr;
    }
#ifdef RTC_ENABLE_TEST_DIAGNOSTICS
    // Lets a probe release the id between libdatachannel reading it and this lookup
    const int delay = test_dispatch_delay.load();
    if (delay != 0) {
        std::this_thread::sleep_for(std::chrono::milliseconds(delay));
    }
#endif
    std::shared_lock lock(registry_mutex);
    const auto it = registry.find(reinterpret_cast<uintptr_t>(id));
    if (it == registry.end()) {
#ifdef RTC_ENABLE_TEST_DIAGNOSTICS
        if (delay != 0) {
            test_released_lookups.fetch_add(1);
        }
#endif
        return nullptr;
    }
    return it->second;
}

void release_callback(void* id) {
    std::shared_ptr<const jvm_callback> callback;
    {
        std::unique_lock lock(registry_mutex);
        const auto it = registry.find(reinterpret_cast<uintptr_t>(id));
        if (it == registry.end()) {
            return;
        }
        callback = std::move(it->second);
        registry.erase(it);
    }
    // Dropped outside the lock, the global reference goes now or with the dispatch still holding it
}

#ifdef RTC_ENABLE_TEST_DIAGNOSTICS
int set_test_dispatch_delay(const int millis) {
    test_dispatch_delay.store(millis);
    return test_released_lookups.exchange(0);
}
#endif
