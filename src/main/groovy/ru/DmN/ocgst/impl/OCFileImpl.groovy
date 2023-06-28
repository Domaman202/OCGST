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
import ru.DmN.ocgst.util.ComplexFileComparator
import ru.DmN.ocgst.util.Utils
import ru.DmN.ocgst.util.OCGSTException
import ru.DmN.ocgst.util.Packet

@CompileStatic
@Canonical
@TypeChecked
@EqualsAndHashCode
class OCFileImpl implements IOCFile {
    protected final IOCDrive drive
    protected String path

    OCFileImpl(IOCDrive drive, String path) {
        this.drive = drive
        this.path = path
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
        this.updateCache()
        return this.send("rm", [:]).data
    }

    @Override
    boolean write(Object data) {
        if (this.isDirectory())
            throw new OCGSTException(this.toString())
        this.send("mkdir", [:])
        this.drive.send("write", [path: this.subFile("ocgst").getAbsolutePath(), data: "f"])
        this.write0(data)
    }

    boolean write0(Object data) {
        var str = JsonOutput.toJson(data)
        var fcount = Math.ceil(str.length() / OCGST.FILE_MAX_SIZE as float) as int
        // cleanup
        this.list().stream().filter { it.startsWith("ocgst.") && it.endsWith(".data") }.map { this.subFile(it) }.forEach(IOCFile::delete)
        // split
        var arrs = Utils.split(str.chars.toList(), OCGST.FILE_MAX_SIZE)
        // write
        for (i in 0..<arrs.size()) {
            this.subFile("ocgst.${i}.data").writeHard(new String(arrs[i].toArray() as char[]))
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
        if (this.send("isdir", [:]).data) {
            var properties = this.subFile("ocgst")
            if (properties.exists() && properties.readHard() == "f") {
                return read0();
            }
        }
        throw new OCGSTException(this.toString())
    }

    Object read0() {
        var files = this.list().stream().filter { it.startsWith("ocgst.") && it.endsWith(".data") }.sorted(new ComplexFileComparator()::compare).toList()
        var text = new StringBuilder()
        files.stream().map { this.subFile(it) }.forEach {
            text.append(it.readHard())
        }
        return new JsonSlurper().parseText(text.toString())
    }

    @Override
    String readHard() {
        return this.send("read", [:]).data
    }

    @Override
    boolean isDirectory() {
        if (this.send("isdir", [:]).data) {
            var properties = this.subFile("ocgst")
            return properties.exists() && properties.readHard() == "d"
        } else return false
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
