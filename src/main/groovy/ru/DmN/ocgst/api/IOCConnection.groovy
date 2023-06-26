package ru.DmN.ocgst.api

import ru.DmN.ocgst.util.Packet

import java.util.function.Consumer

interface IOCConnection {
    String getName()

    IOCDrive[] getDrives()

    Packet send(String action, Object data)

    int send0(String action, Object data)

    void read(int id, Consumer<Packet> consumer)

    Packet read(int id, int timeout)
}