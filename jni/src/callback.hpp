#pragma once

#include <jni.h>
#include <memory>

struct jvm_callback {
    jobject instance;
};

// libdatachannel reads a peer's user pointer before it runs a callback and does not stop a deletion
// in between, so the user pointer is an id rather than the callback. A dispatch holds what it
// acquired until it returns, and the global reference goes with the last holder.

void* register_callback(JNIEnv* env, jobject listener);

std::shared_ptr<const jvm_callback> acquire_callback(void* id);

void release_callback(void* id);

#ifdef RTC_ENABLE_TEST_DIAGNOSTICS
// Returns how many delayed lookups found their id released under the previous delay
int set_test_dispatch_delay(int millis);
#endif
