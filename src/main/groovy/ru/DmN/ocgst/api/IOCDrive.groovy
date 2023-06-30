package ru.DmN.ocgst.api

import ru.DmN.ocgst.util.Packet

import java.util.function.Function

interface IOCDrive {
    String getName()

    IOCFile getRoot()

    IOCFile getFile(String path)

    Packet send(String action, Object data)

    void sendSubscribe(String action, Object data, Function<Packet, Boolean> consumer, int timeout)
}