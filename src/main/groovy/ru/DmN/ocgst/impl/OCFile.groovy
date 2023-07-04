package ru.DmN.ocgst.impl

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import ru.DmN.ocgst.api.IOCFile
import ru.DmN.ocgst.api.OCConnection
import ru.DmN.ocgst.util.Actions

@ToString
@Canonical
@CompileStatic
@EqualsAndHashCode
class OCFile implements IOCFile {
    protected final OCConnection connection
    protected final int fs
    protected String path

    OCFile(OCConnection connection, int fs, String path) {
        this.connection = connection
        this.path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path
        this.fs = fs
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
    OCFile subfile(String path) {
        return new OCFile(this.connection, this.fs, this.getAbsolutePath() + "/" + path)
    }

    @Override
    void mkdir() {
        this.connection.pushAction(Actions.MKDIR, this.fs, this.path)
    }

    @Override
    boolean isDirectory() {
        var result = null
        this.connection.pushAction(Actions.ISDIR, this.fs, this.path, { result = it.is.read() == 1 })
        return result
    }

    @Override
    void delete() {
        this.connection.pushAction(Actions.DELETE, this.fs, this.path)
    }

    @Override
    InputStream openInputStream() {
        byte[] bytes = null
        this.connection.pushAction(Actions.READ, this.fs, this.getAbsolutePath(), { bytes = it.read() })
        while (bytes == null) Thread.onSpinWait()
        return new FileInputStream(bytes)
    }

    @Override
    OutputStream openOutputStream() {
        return new FileOutputStream()
    }

    static class FileInputStream extends ByteArrayInputStream {
        FileInputStream(byte[] buf) {
            super(buf)
        }
    }

    class FileOutputStream extends ByteArrayOutputStream {
        @Override
        void flush() throws IOException {
            OCFile.this.connection.pushAction(Actions.WRITE, OCFile.this.fs, OCFile.this.getAbsolutePath(), {
                OCFile.this.connection.write(this.toByteArray())
                OCFile.this.connection.os.flush()
            })
        }

        @Override
        void close() throws IOException {
            this.flush()
        }
    }
}
