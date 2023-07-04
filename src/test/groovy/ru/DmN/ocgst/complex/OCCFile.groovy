package ru.DmN.ocgst.complex

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.apache.commons.lang3.stream.Streams
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import ru.DmN.ocgst.api.IOCFile
import ru.DmN.ocgst.api.OCConnection
import ru.DmN.ocgst.api.OCFile
import ru.DmN.ocgst.util.Actions
import ru.DmN.ocgst.util.Utils

import java.nio.ByteBuffer

@ToString
@Canonical
@CompileStatic
@EqualsAndHashCode
class OCCFile implements IOCFile {
    protected final Tuple2<OCConnection, Integer>[] connections
    protected String path

    OCCFile(Tuple2<OCConnection, Integer>[] connections, String path) {
        this.connections = connections
        this.path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path
    }

    @Override
    String getName() {
        return this.path.substring(this.path.lastIndexOf("/"))
    }

    @Override
    String getAbsolutePath() {
        return this.path
    }

    @Override
    IOCFile subfile(String path) {
        return new OCCFile(this.connections, this.getAbsolutePath() + "/" + path)
    }

    @Override
    void mkdir() {
        for (final def connection : this.connections) {
            connection.v1.pushAction(Actions.MKDIR, connection.v2, this.path)
        }
    }

    @Override
    boolean isDirectory() {
        var connection = this.connections[0]
        var result = null
        connection.v1.pushAction(Actions.ISDIR, connection.v2, this.path, { result = it.is.read() == 1 })
        return result
    }

    @Override
    void delete() {
        for (final def connection : this.connections) {
            connection.v1.pushAction(Actions.DELETE, connection.v2, this.path)
        }
    }

    @Override
    InputStream openInputStream() {
        var c = this.connections.length
        var bytes = new byte[c][]
        for (i in 0..<c) {
            final j = i
            final connection = this.connections[i]
            connection.v1.pushAction(Actions.READ, connection.v2, this.getAbsolutePath(), {
                bytes[j] = it.read()
                null
            })
        }

        var len = 0
        for (i in 0..<bytes.length) {
            while (bytes[i] == null)
                Thread.onSpinWait()
            len += bytes[i].length
        }

        byte[] result = new byte[len]
        int currentIndex = 0
        for (byte[] arr : bytes) {
            System.arraycopy(arr, 0, result, currentIndex, arr.length)
            currentIndex += arr.length
        }

        return new OCFile.FileInputStream(result)
    }

    @Override
    OutputStream openOutputStream() {
        return new FileOutputStream()
    }

    class FileOutputStream extends ByteArrayOutputStream {
        @Override
        void flush() throws IOException {
            var bytes = Utils.split(this.toByteArray(), OCCFile.this.connections.length)
            for (i in 0..<bytes.length) {
                final j = i
                final connection = OCCFile.this.connections[i]
                connection.v1.pushAction(Actions.WRITE, connection.v2, OCCFile.this.getAbsolutePath(), {
                    connection.v1.write(bytes[j])
                    connection.v1.os.flush()
                })
            }
        }

        @Override
        void close() throws IOException {
            this.flush()
        }
    }
}
