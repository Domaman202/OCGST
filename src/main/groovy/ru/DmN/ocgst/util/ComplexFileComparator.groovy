package ru.DmN.ocgst.util

class ComplexFileComparator implements Comparator<String> {
    private static int process(String name) {
        return Integer.parseInt(name.substring(name.indexOf('.') + 1, name.lastIndexOf('.')))
    }

    @Override
    int compare(String o1, String o2) {
        return process(o1) <=> process(o2)
    }
}
