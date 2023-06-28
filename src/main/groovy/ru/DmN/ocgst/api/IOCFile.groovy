package ru.DmN.ocgst.api

import ru.DmN.ocgst.util.Packet

interface IOCFile extends Comparable<IOCFile> {
    default void updateCache() {}

    String getName()

    boolean rename(String name)

    String getPathToFile()

    String getAbsolutePath()

    IOCFile subFile(String path)

    boolean mkdir()

    List<IOCFile> list()

    boolean delete()

    boolean write(Object data)

    boolean writeHard(String data)

    Object read()

    String readHard()

    boolean isDirectory()

    boolean exists()

    Packet send(String action, Object data)
}
