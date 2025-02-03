package com.github.jaykkumar01.watchparty_duo.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class BitmapUtils {
    public static byte[] getBytes(Bitmap bitmap) {
        if (bitmap == null) return new byte[0];

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream); // Use JPEG for speed and smaller size

//        bitmap.recycle();

        return stream.toByteArray();
    }

    public static Bitmap getBitmap(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}

