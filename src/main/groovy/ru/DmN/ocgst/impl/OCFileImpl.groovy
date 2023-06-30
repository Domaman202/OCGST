package ru.DmN.ocgst.impl

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.TypeChecked
import ru.DmN.ocgst.OCGST
import ru.DmN.ocgst.api.IOCDrive
import ru.DmN.ocgst.api.IOCFile
import ru.DmN.ocgst.util.OCGSTException
import ru.DmN.ocgst.util.Packet

@TypeChecked
@CompileStatic
@Canonical
@EqualsAndHashCode
class OCFileImpl implements IOCFile {
    protected final IOCDrive drive
    protected String path
    protected int handle

    OCFileImpl(IOCDrive drive, String path) {
        this.drive = drive
        this.path = path
        this.handle = -1
    }

    @Override
    String getName() {
        return path.substring(path.lastIndexOf('/'))
    }

    @Override
    boolean rename(String name) {
        if (this.send("rename", [to:name])) {
            this.path = path.substring(0, path.lastIndexOf('/')) + '/' + name
            return true
        } else return false
    }

    @Override
    String getPathToFile() {
        return this.send("isdir", [:]) ? this.path : this.path.substring(0, this.path.lastIndexOf('/'))
    }

    @Override
    String getAbsolutePath() {
        return this.path
    }

    @Override
    IOCFile subFile(String path) {
        var ptf = this.getPathToFile()
        if (!ptf.endsWith('/'))
            ptf += "/"
        return this.drive.getFile(ptf + path)
    }

    @Override
    boolean mkdir() {
        return this.send("mkdir", [:]).data && this.subFile("ocgst").writeHard("d")
    }

    @Override
    List<String> list() {
        return this.send("list", [:]).data as List<String>
    }

    @Override
    boolean delete() {
        return this.send("rm", [:]).data
    }

    @Override
    boolean write(Object data) {
        if (this.isDirectory())
            throw new OCGSTException(this.toString())
        var text = JsonOutput.toJson(data)
        return this.writeHard(text)
    }

    @Override
    boolean writeHard(String data) {
        return this.send("write", [data: data])
    }

    @Override
    Object read() {
        if (this.isDirectory())
            throw new OCGSTException(this.toString())
        var text = this.readHard()
        return new JsonSlurper().parseText(text)
    }

    @Override
    String readHard() {
        var text = new StringBuilder()
        this.drive.sendSubscribe("read", [path:this.path], packet -> {
            if (packet.data == null)
                return false
            text.append(packet.data)
            return true
        })
        return text.toString()
    }

    @Override
    boolean isDirectory() {
        return this.send("isdir", [:]).data
    }

    @Override
    boolean exists() {
        return this.send("exists", [:]).data
    }

    @Override
    Packet send(String action, Object data) {
        if (data["path"] == null)
            data["path"] = this.path
        this.drive.send(action, data)
    }

    @Override
    int compareTo(IOCFile o) {
        return this.getName() <=> o.getName()
    }

    @Override
    String toString() {
        return "OCFileImpl{drive=$drive,path=$path}"
    }
}
