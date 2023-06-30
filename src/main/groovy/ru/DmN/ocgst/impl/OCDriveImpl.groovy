package ru.DmN.ocgst.impl

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.TypeChecked
import ru.DmN.ocgst.api.IOCDrive
import ru.DmN.ocgst.api.IOCFile
import ru.DmN.ocgst.util.OCGSTTimeoutException
import ru.DmN.ocgst.util.Packet

import java.util.concurrent.TimeoutException
import java.util.function.Consumer
import java.util.function.Function

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
        return this.connection.send(action, data)
    }

    @Override
    void sendSubscribe(String action, Object data, Function<Packet, Boolean> consumer, int timeout) {
        var packet = this.send(action, data)
        while (consumer.apply(packet)) {
            packet = this.connection.read(packet.id, timeout)
        }
    }

    @Override
    String toString() {
        return "OCDriveImpl{name=$name}"
    }
}
