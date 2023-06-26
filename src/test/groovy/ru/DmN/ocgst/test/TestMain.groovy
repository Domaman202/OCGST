package ru.DmN.ocgst.test

import ru.DmN.ocgst.Main
import ru.DmN.ocgst.impl.OCComplexFileImpl

class TestMain {
    static void main(String[] args) {
        Main.main()
        //
        sleep(5000)
        //
        var drive = Main.ocgst.connections[0].getDrives()[0]
        var file = new OCComplexFileImpl(drive, "/test")
        var bw = new File("test/test.test").bytes
        println(bw)
        file.write(bw)
        var br = (file.read() as List<Integer>).collect { (byte) it }.toArray(Byte[]::new)
        println(br)
        try (var stream = new FileOutputStream("test/test.test.out")) {
            stream.write(br as byte[])
        }
    }
}
