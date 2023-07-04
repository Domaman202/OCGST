package ru.DmN.ocgst.api

import groovy.transform.CompileStatic
import groovy.transform.Synchronized
import ru.DmN.ocgst.util.Action
import ru.DmN.ocgst.util.Actions
import ru.DmN.ocgst.util.Status
import ru.DmN.ocgst.util.Utils

import java.util.function.Consumer

@CompileStatic
class OCConnection extends Thread {
    protected final Socket socket
    final BufferedInputStream is = new BufferedInputStream(socket.inputStream)
    final BufferedOutputStream os = new BufferedOutputStream(socket.outputStream)
    protected final Queue<Tuple2<Action, Consumer<OCConnection>>> pool = new ArrayDeque<>()
    Status status = Status.READY

    OCConnection(Socket socket) {
        this.socket = socket
    }

    @Override
    void run() {
        while (true) {
            switch (this.status) {
                case Status.READY -> {
                    if (this.pool.empty)
                        onSpinWait()
                    else {
                        this.status = Status.PROCESSING
                        var it = this.pool.remove()
                        this.send(it.v1.action, it.v1.fs, it.v1.path)
                        if (it.v2 != null) {
                            it.v2.accept(this)
                        }
                    }
                }
                case Status.PROCESSING -> {
                    this.status = Status.values()[this.is.read()]
                }
            }
        }
    }

    void pushAction(Actions action, int fs, String path) {
        this.pool.add(new Tuple2<>(new Action(action, fs, path), null))
    }

    void pushAction(Actions action, int fs, String path, Consumer<OCConnection> callback) {
        this.pool.add(new Tuple2<>(new Action(action, fs, path), callback))
    }

    @Synchronized("is")
    byte[] read() {
        var size = Utils.bti(is.readNBytes(4))
        return this.is.readNBytes(size)
    }

    @Synchronized("os")
    void write(byte[] bytes) {
        this.os.write(Utils.itb(bytes.length))
        this.os.write(bytes)
    }

    @Synchronized("os")
    void send(Actions action, int fs, String path) {
        this.os.write(action.ordinal())
        this.os.write(fs)
        this.write(path.bytes)
        this.os.flush()
    }
}
