package ru.DmN.ocgst.api

interface IOCFile<T extends IOCFile<T>> extends Comparable<T> {
    String getName()

    String getPath()

    boolean rename(String to)

    IOCDirectory getDirectory()

    boolean write(Object data)

    Object read()

    boolean delete()

    boolean exists()

    default boolean isDirectory() {
        return false
    }
}