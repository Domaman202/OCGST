package ru.DmN.ocgst.api

interface IOCFOD<T extends IOCFOD<T>> extends IOCFile<T>, IOCDirectory<T> {
    default boolean isDirectory() {
        throw new RuntimeException("Not impl")
    }
}
