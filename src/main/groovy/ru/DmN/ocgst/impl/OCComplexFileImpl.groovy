package ru.DmN.ocgst.impl

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import ru.DmN.ocgst.api.IOCDirectory
import ru.DmN.ocgst.api.IOCDrive
import ru.DmN.ocgst.api.IOCFile
import ru.DmN.ocgst.util.OCGSTException
import ru.DmN.ocgst.util.Packet

class OCComplexFileImpl implements IOCFile<IOCFile> {
    protected final IOCDirectory directory

    OCComplexFileImpl(IOCDrive drive, String path) {
        this.directory = new OCFODImpl(drive, path)
    }

    @Override
    String getName() {
        return this.directory.name
    }

    @Override
    String getPath() {
        return this.directory.path
    }

    @Override
    boolean rename(String to) {
        return this.directory.rename(to)
    }

    @Override
    IOCDirectory getDirectory() {
        return (this.directory as OCFODImpl).drive.root().get(this.path.substring(0, this.path.lastIndexOf('/')))
    }

    @Override
    boolean write(Object data) {
        if (this.isDirectory())
            throw new OCGSTException("Невозможно записать в \"${this.path}\" потому что это директорию")
        //
        this.directory.get(".cf").write("OCGST")
        //
        var str = JsonOutput.toJson(data)
        var fcount = Math.ceil(str.length() / 255)
        // cleanup
        var files = this.directory.list()
        if (files.length > fcount) {
            this.directory.delete()
        }
        //
        if (!this.directory.exists())
            this.directory.mkdir(".")
        // split & write
        for (i in 0..<fcount) {
            var size = Math.min(255, str.length() - i * 255)
            var arr = new char[size]
            for (j in 0..<size)
                arr[j] = str.charAt(i * 255 + j)
            this.directory.get("${i}.data").write(new String(arr))
        }
        //
        return true
    }

    @Override
    Object read() {
        if (!this.exists())
            throw new OCGSTException("Файл \"${this.path}\" не существует")
        if (this.isDirectory())
            throw new OCGSTException("Невозможно считать \"${this.path}\" потому что это директорию")
        // check
        if (this.directory.get(".cf").read() != "OCGST")
            throw new OCGSTException("Файл \"${this.path}\" не является комлпексным")
        //
        var files = this.directory.list().sort()
        var chars = new char[files.length * 255]
        for (i in 0..<files.length) {
            var arr = files[i].read() as String
            for (j in 0..<arr.length()) {
                chars[i * 255 + j] = arr.charAt(j)
            }
        }
        return new JsonSlurper().parse(chars)
    }

    @Override
    boolean delete() {
        return this.directory.delete()
    }

    @Override
    boolean exists() {
        return this.directory.exists()
    }

    Packet send(String action, Object data) {
        return (this.directory as OCFODImpl).drive.send(action, data)
    }

    @Override
    int compareTo(IOCFile o) {
        return this.name <=> o.name
    }
}
