package ru.DmN.ocgst

import ru.DmN.ocgst.api.IOCConnection
import ru.DmN.ocgst.impl.OCConnectionImpl

class OCGST {
    static final def TIMEOUT = 1000
    final def connections = new ArrayList<IOCConnection>()

    void main(String[] args) {
        var socket = new ServerSocket(25585)
        while (true) {
            var connection = new OCConnectionImpl(socket.accept())
            connections.add(connection)
            new Thread(connection::run).start()
        }
    }
}
