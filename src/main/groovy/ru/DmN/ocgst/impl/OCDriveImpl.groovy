package ru.DmN.ocgst.impl

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.TypeChecked
import ru.DmN.ocgst.api.IOCDrive
import ru.DmN.ocgst.api.IOCFile
import ru.DmN.ocgst.util.Packet

@Canonical
@TypeChecked
@EqualsAndHashCode
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
    IOCFile getRoot() {
        return this.getFile("/")
    }

    @Override
    IOCFile getFile(String path) {
        return  new OCFileImpl(this, path)
    }

    @Override
    Packet send(String action, Object data) {
        if (data["fs"] == null)
            data["fs"] = this.name
        return connection.send(action, data)
    }

    @Override
    String toString() {
        return "OCDriveImpl{name=$name}"
    }
}
