package ru.DmN.ocgst.impl


import ru.DmN.ocgst.api.IOCDrive
import ru.DmN.ocgst.util.Packet

class OCDriveImpl implements IOCDrive {
    protected final OCConnectionImpl connection
    protected final String name

    OCDriveImpl(OCConnectionImpl connection, String name) {
        this.connection = connection
        this.name = name
    }

    @Override
    String getName() {
        return this.name
    }

    @Override
    Packet send(String action, Object data) {
        if (data["fs"] == null)
            data["fs"] = this.name
        return connection.send(action, data)
    }

    @Override
    String toString() {
        return "OCDrive{name=${this.name}}"
    }
}
