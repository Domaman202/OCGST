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

    static byte[][] split(byte[] array, int count) {
        int size = (array.length / count) as int + (array.length % count > 0 ? 1 : 0)
        byte[][] elements = new byte[count][]
        int k = 0
        for (int i = 0; i < count; i++) {
            int elementLength = Math.min(size, array.length - k)
            byte[] element = new byte[elementLength]
            System.arraycopy(array, k, element, 0, elementLength)
            elements[i] = element
            k += elementLength
        }
        return elements
    }
}
