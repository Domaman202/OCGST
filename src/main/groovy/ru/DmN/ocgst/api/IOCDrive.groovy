package ru.DmN.ocgst.api


import ru.DmN.ocgst.util.Packet

interface IOCDrive {
    String getName()

    IOCFile getRoot()

    IOCFile getFile(String path)

    Packet send(String action, Object data)
}