package ru.DmN.ocgst.impl

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import ru.DmN.ocgst.OCGST
import ru.DmN.ocgst.api.IOCConnection
import ru.DmN.ocgst.api.IOCDrive
import ru.DmN.ocgst.util.Packet

import java.util.concurrent.TimeoutException
import java.util.function.Consumer

class OCConnectionImpl implements IOCConnection, Runnable {
    protected String name
    protected final Socket socket
    protected final BufferedOutputStream os
    protected final BufferedInputStream is
    protected volatile int lastId
    protected final List<Packet> ibuffer
    protected final List<Packet> obuffer
    protected final Map<Integer, Consumer<Packet>> listeners

    OCConnectionImpl(Socket socket) {
        this.socket = socket
        this.os = new BufferedOutputStream(socket.outputStream)
        this.is = new BufferedInputStream(socket.inputStream)
        this.ibuffer = new ArrayList<>()
        this.obuffer = new ArrayList<>()
        this.listeners = new HashMap<>()

        this.read(this.send0("hello", []), { this.name = it.data })
    }

    @Override
    String getName() {
        return this.name
    }

    @Override
    IOCDrive[] getDrives() {
        return (this.send("drives", []).data as List<String>).stream().map { new OCDriveImpl(this, it) }.toArray(OCDriveImpl[]::new)
    }

    @Override
    Packet send(String action, Object data) {
        return this.read(this.send0(action, data))
    }

    @Override
    synchronized int send0(String action, Object data) {
        this.obuffer.add(new Packet(++this.lastId, action, data));
        return this.lastId
    }

    @Override
    void read(int id, Consumer<Packet> consumer) {
        this.listeners.put(id, consumer)
    }

    Packet read(int id) {
        return this.read(id, OCGST.TIMEOUT)
    }

    Packet read(int id, int timeout) {
        Packet packet
        synchronized (this.ibuffer) {
            packet = this.ibuffer.find { it.id == id }
        }
        if (packet)
            return packet
        if (timeout > 0) {
            var sleepTime = Math.min(timeout, 100)
            sleep(sleepTime)
            return read(id, timeout - sleepTime)
        } else throw new TimeoutException("Превышено время ожидания пакета")
    }

    @Override
    void run() {
        try {
            while (true) {
                //
                synchronized (this.ibuffer) {
                    for (packet in this.ibuffer) {
                        var listener = this.listeners[packet.id]
                        if (listener) {
                            listener.accept(packet)
                            this.listeners.remove(packet.id)
                        }
                    }
                }
                //
                synchronized (this.ibuffer) {
                    if (this.ibuffer.size() >= 25) {
                        this.ibuffer.remove(0)
                    }
                }
                //
                var lock = false
                while (true) {
                    if (this.is.available()) {
                        sleep(10)
                        break
                    } else if (this.obuffer.isEmpty() || lock) {
                        Thread.onSpinWait()
                    } else {
                        this.os.write(JsonOutput.toJson(this.obuffer.removeAt(0)).getBytes())
                        this.os.flush()
                        lock = true
                    }
                }
                //
                var input = new String(this.is.readNBytes(this.is.available()))
                println("[Input]\t$input")
                synchronized (this.ibuffer) {
                    this.ibuffer.add(new JsonSlurper().parseText(input) as Packet)
                }
            }
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    String toString() {
        return "OCConnection{name=${this.name}}"
    }
}
