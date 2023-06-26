package ru.DmN.ocgst.api

import ru.DmN.ocgst.impl.OCFODImpl
import ru.DmN.ocgst.util.Packet

interface IOCDrive {
    String getName()

    default IOCDirectory root() {
        return new OCFODImpl(this, "/")
    }

    Packet send(String action, Object data)
}