package ru.DmN.ocgst.util

import groovy.transform.CompileStatic

@CompileStatic
class Utils {
    static int bti(byte[] bytes) {
        return ((0xFF & bytes[0]) << 24) | ((0xFF & bytes[1]) << 16) | ((0xFF & bytes[2]) << 8) | (0xFF & bytes[3])
    }

    static byte[] itb(int i) {
        return new byte[]{(byte) (i >>> 24), (byte) (i >>> 16), (byte) (i >>> 8), (byte) i};
    }
}
