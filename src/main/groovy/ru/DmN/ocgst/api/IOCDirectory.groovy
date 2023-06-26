package ru.DmN.ocgst.api

interface IOCDirectory<T extends IOCDirectory<T>> extends Comparable<T> {
    String getName()

    String getPath()

    boolean rename(String to)

    boolean rename(String from, String to)

    IOCFOD get(String path)

    IOCFOD[] list()

    IOCFOD[] list(String path)

    IOCDirectory mkdir(String path)

    boolean delete()

    boolean delete(String path)

    boolean exists()

    default boolean isDirectory() {
        return true
    }
}