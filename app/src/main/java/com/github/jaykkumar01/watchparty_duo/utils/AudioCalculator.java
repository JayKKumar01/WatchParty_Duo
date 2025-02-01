package com.github.jaykkumar01.watchparty_duo.utils;


import com.github.jaykkumar01.watchparty_duo.interfaces.AudioData;

public class AudioCalculator {

    private static int[] getAmplitudesFromBytes(byte[] bytes) {
        int[] amps = new int[bytes.length / 2];
        for (int i = 0; i < bytes.length; i += 2) {
            short buff = bytes[i + 1];
            short buff2 = bytes[i];

            buff = (short) ((buff & 0xFF) << 8);
            buff2 = (short) (buff2 & 0xFF);

            short res = (short) (buff | buff2);
            amps[i == 0 ? 0 : i / 2] = res;
        }
        return amps;
    }

    public static float getLoudness(byte[] bytes) {
        int [] amplitudes = getAmplitudesFromBytes(bytes);
        int major = 0;
        int minor = 0;
        for (int i : amplitudes) {
            if (i > major) major = i;
            if (i < minor) minor = i;
        }
        int amplitude = Math.max(major, minor * (-1));
        return (float) amplitude / AudioData.MAX_AMP;
    }
}
