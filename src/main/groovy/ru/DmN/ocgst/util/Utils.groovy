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
        var size = Math.ceil(array.length / count as int) as int
        var elements = new byte[count][size]
        var k = 0
        for (i in 0..<count) {
            var element = elements[i]
            for (j in 0..<size) {
                if (k == array.length)
                    break
                element[j] = array[k++]
            }
        }
        return elements
    }
}
