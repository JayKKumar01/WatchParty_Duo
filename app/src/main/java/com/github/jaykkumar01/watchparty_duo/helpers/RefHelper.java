package com.github.jaykkumar01.watchparty_duo.helpers;

import java.lang.ref.WeakReference;

public class RefHelper {
    public static <T> void reset(WeakReference<T> instanceRef) {
        if (instanceRef != null) {
            instanceRef.clear();
            // Note: Setting instanceRef to null here does not affect the original reference
        }
    }
}
