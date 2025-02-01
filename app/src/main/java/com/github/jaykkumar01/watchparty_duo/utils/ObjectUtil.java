package com.github.jaykkumar01.watchparty_duo.utils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ObjectUtil {
    public static String preserveString(String input) {
        return Arrays.toString(input.getBytes(StandardCharsets.UTF_8));
    }
    public static String restoreString(byte[] bytes){
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String preserveBytes(byte[] byteArray) {
        return Arrays.toString(byteArray);
    }

}
