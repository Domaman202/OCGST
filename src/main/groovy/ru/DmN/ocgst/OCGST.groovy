package ru.DmN.ocgst

import ru.DmN.ocgst.api.IOCConnection
import ru.DmN.ocgst.impl.OCConnectionImpl

class OCGST {
    static final int TIMEOUT = 60000 * 5
    static final int MIN_TIMEOUT = 10
    static final int BUFFER_READ_TIMEOUT = 10
    static final int BUFFER_MAX = 25
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
