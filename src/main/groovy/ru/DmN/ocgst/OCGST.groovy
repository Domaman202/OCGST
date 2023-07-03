package ru.DmN.ocgst

import ru.DmN.ocgst.api.OCConnection


class OCGST {
    final def connections = new ArrayList<OCConnection>()

    void main(String[] args) {
        var socket = new ServerSocket(25585)
        while (true) {
            var connection = new OCConnection(socket.accept())
            connections.add(connection)
            connection.start()
        }
    }
}
