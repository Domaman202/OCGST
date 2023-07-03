package ru.DmN.ocgst

import groovy.transform.CompileStatic
import ru.DmN.ocgst.api.OCConnection


@CompileStatic
class OCGST {
    final List<OCConnection> connections = new ArrayList<OCConnection>()

    void main(String[] args) {
        var socket = new ServerSocket(25585)
        while (true) {
            var connection = new OCConnection(socket.accept())
            connections.add(connection)
            connection.start()
        }
    }
}
