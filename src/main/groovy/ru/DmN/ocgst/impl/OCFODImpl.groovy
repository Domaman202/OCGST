package ru.DmN.ocgst.impl

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import ru.DmN.ocgst.api.IOCDirectory
import ru.DmN.ocgst.api.IOCDrive
import ru.DmN.ocgst.api.IOCFOD
import ru.DmN.ocgst.util.OCGSTException
import ru.DmN.ocgst.util.Packet

class OCFODImpl implements IOCFOD<IOCFOD> {
    protected final IOCDrive drive
    protected String path

    OCFODImpl(IOCDrive drive, String path) {
        this.drive = drive
        this.path = path
    }

    String sub(String path) {
        return path.startsWith("/") ? path : "${this.path}/$path"
    }

    @Override
    IOCFOD get(String path) {
        return new OCFODImpl(this.drive, this.sub(path))
    }

    @Override
    IOCFOD[] list() {
        return this.list(".")
    }

    @Override
    IOCFOD[] list(String path) {
        return (this.send("list", [path: this.sub(path)]).data as List<String>).stream().map { new OCFODImpl(this.drive, this.sub(it)) }.toArray(OCFODImpl[]::new)
    }

    @Override
    IOCDirectory mkdir(String path) {
        var dir = this.sub(path)
        this.send("mkdir", [path: dir])
        return new OCFODImpl(this.drive, dir)
    }

    @Override
    String getName() {
        return this.path.substring(this.path.lastIndexOf('/'))
    }

    @Override
    boolean rename(String to) {
        this.path = this.path.substring(0, this.path.lastIndexOf('/') - 1) + to
        return this.send("rename", [to: to])
    }

    @Override
    IOCDirectory getDirectory() {
        return this.drive.root().get(this.path.substring(0, this.path.lastIndexOf('/')))
    }

    @Override
    boolean rename(String from, String to) {
        return this.send("rename", [path: this.sub(from), to: to])
    }

    @Override
    String getPath() {
        return this.path
    }

    @Override
    boolean write(Object data) {
        if (this.isDirectory())
            throw new OCGSTException("Невозможно записать в \"${this.path}\" потому что это директорию.")
        return this.send("write", [data: JsonOutput.toJson(data)]).data
    }

    @Override
    Object read() {
        if (!this.exists())
            throw new OCGSTException("Файл \"${this.path}\" не существует.")
        if (this.isDirectory())
            throw new OCGSTException("Невозможно считать \"${this.path}\" потому что это директорию.")
        return new JsonSlurper().parseText(this.send("read").data as String)
    }

    @Override
    boolean delete() {
        return this.send("rm").data
    }

    @Override
    boolean delete(String path) {
        return this.send("rm", [path: this.sub(path)]).data
    }

    @Override
    boolean exists() {
        return this.send("exists").data
    }

    @Override
    boolean isDirectory() {
        return this.send("isdir").data
    }

    Packet send(String action) {
        return this.send(action, [:])
    }

    Packet send(String action, Object data) {
        if (data["path"] == null)
            data["path"] = path
        return this.drive.send(action, data)
    }

    @Override
    String toString() {
        return "OCFile{drive=${this.drive.name},path=\"${this.path}\"}"
    }

    @Override
    int compareTo(IOCFOD o) {
        return this.name <=> o.name
    }
}
