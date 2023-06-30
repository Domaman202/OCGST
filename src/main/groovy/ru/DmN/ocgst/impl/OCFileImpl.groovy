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
import ru.DmN.ocgst.util.Utils

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
        this.open()
        var result = this.write0(data)
        this.close()
        return result
    }

    boolean write0(Object data) {
        var str = JsonOutput.toJson(data)
        // cleanup
        this.list().stream().filter { it.startsWith("ocgst.") && it.endsWith(".data") }.map { this.subFile(it) }.forEach(IOCFile::delete)
        // split
        var arrs = Utils.split(str.chars.toList(), OCGST.FILE_MAX_SIZE)
        // write
        for (i in 0..<arrs.size()) {
            this.writeHard(new String(arrs[i].toArray() as char[]))
        }
        //
        return true
    }

    @Override
    boolean writeHard(String data) {
        return this.send("write", [data: data])
    }

    @Override
    Object read() {
        if (this.isDirectory())
            throw new OCGSTException(this.toString())
        this.open()
        var result = this.read0()
        this.close()
        return result
    }

    Object read0() {
        var text = new StringBuilder()
        while (true) {
            var read = this.readHard()
            if (read == "nil")
                break
            text.append(read)
        }
        return new JsonSlurper().parseText(text.toString())
    }

    @Override
    String readHard() {
        return this.send("read", [:]).data
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

    void open() {
        if (this.handle == -1) {
            this.handle = this.send("open", [:]).data as int
        }
    }

    void close() {
        if (this.handle != -1) {
            this.drive.send("close", [handle:handle])
            this.handle = -1
        }
    }

    @Override
    int compareTo(IOCFile o) {
        return this.getName() <=> o.getName()
    }

    @Override
    String toString() {
        return "OCFileImpl{drive=$drive,path=$path}"
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize()
        this.close()
    }
}
