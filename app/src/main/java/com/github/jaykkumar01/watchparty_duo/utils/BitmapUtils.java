package com.github.jaykkumar01.watchparty_duo.utils;

import android.graphics.Bitmap;
import java.io.ByteArrayOutputStream;

public class BitmapUtils {
    public static byte[] getBytes(Bitmap bitmap) {
        if (bitmap == null) return new byte[0];

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream); // Use JPEG for speed and smaller size

        return stream.toByteArray();
    }
}

