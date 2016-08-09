package com.hdr.wristband

import android.os.Build
import android.view.View
import java.util.concurrent.atomic.AtomicInteger

val sNextGeneratedId: AtomicInteger = AtomicInteger(1);
fun viewId(): Int {
    if (Build.VERSION.SDK_INT < 17) {
        while (true) {
            val result: Int = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            var newValue: Int = result + 1;
            if (newValue > 0x00FFFFFF)
                newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    } else {
        return View.generateViewId();
    }

}
