package ru.DmN.ocgst.util

class Utils {
    static int bti(byte[] bytes) {
        return ((0xFF & bytes[0]) << 24) | ((0xFF & bytes[1]) << 16) | ((0xFF & bytes[2]) << 8) | (0xFF & bytes[3])
    }

    static byte[] itb(int i) {
        return new byte[]{(byte) (i >>> 24), (byte) (i >>> 16), (byte) (i >>> 8), (byte) i};
    }

    static <T> T[][] split(T[] array, int size) {
        var count = Math.ceil(array.length / size) as int
        var elements = new Object[count][size]
        var k = 0
        for (i in 0..<count) {
            var element = elements[i]
            for (j in 0..<size) {
                if (k == array.length)
                    break
                element[j] = array[k++]
            }
        }
        return elements as T[][]
    }

    static <T> List<List<Character>> split(List<T> list, int size) {
        var count = Math.ceil(list.size() / size) as int
        var elements = new ArrayList(count)
        var k = 0
        for (i in 0..<count) {
            var element = elements[i] = new ArrayList<>(size)
            for (j in 0..<size) {
                if (k == list.size())
                    break
                element[j] = list[k++]
            }
        }
        return elements
    }
}
