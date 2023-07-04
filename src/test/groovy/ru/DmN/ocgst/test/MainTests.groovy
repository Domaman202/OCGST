package ru.DmN.ocgst.test

import groovy.transform.CompileStatic
import org.apache.commons.lang3.time.StopWatch
import ru.DmN.ocgst.Main
import ru.DmN.ocgst.api.IOCFile
import ru.DmN.ocgst.api.OCConnection
import ru.DmN.ocgst.impl.OCFile
import ru.DmN.ocgst.util.Actions
import ru.DmN.ocgst.util.Status

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

@CompileStatic
class MainTests {
    static void main(String[] args) {
        Main.main()
        //
        while (Main.ocgst.connections.empty) Thread.onSpinWait()
        final def connection = Main.ocgst.connections[0]
        //
        connection.pushAction(Actions.DRIVE_LIST, 0, "", {
            var arr = new String(it.read()).split(";")
            for (i in 0..<arr.length) {
                println("[${i+1}] ${arr[i]}")
            }
        })
        //
        sleep(1000)
        print("Enter fs id: ")
        var fs = new Scanner(System.in).nextInt()
        //
        var dir$test = new OCFile(connection, fs, "test")
        dir$test.mkdir()
        //
        test([connection], dir$test)
        //
        dir$test.delete()
    }

    static void test(List<OCConnection> connection, IOCFile dir$test) {
        try (final def zin = new ZipInputStream(new FileInputStream("test/in/data.zip"))) {
            ZipEntry entry
            while ((entry = zin.getNextEntry()) != null) {
                testRaw(connection, dir$test, entry.name, zin)
                zin.closeEntry()
            }
        }
    }

    static void testRaw(List<OCConnection> connection, IOCFile dir$test, String name, InputStream input) {
        byte[] bw = input.readAllBytes()
        var br = test(connection, dir$test, name, bw)
        try (var output = new FileOutputStream("test/out/${name}")) {
            output.write(br)
        }
    }

    static byte[] test(List<OCConnection> connection, IOCFile dir$test, String name, byte[] bw) {
        var file$tf = dir$test.subfile("${name}")
        var sw0 = new StopWatch()
        var sw1 = new StopWatch()
        var sw2 = new StopWatch()
        //
        sw0.start()
        sw1.start()
        //
        try (var os = file$tf.openOutputStream()) {
            os.write(bw)
        }
        //
        connection.forEach {
            while (it.status == Status.PROCESSING) {
                Thread.onSpinWait()
            }
        }

        //
        sw1.stop()
        println("[File = ${name}]\t[Write]\t[Time = $sw1]")
        //
        sw2.start()
        //
        byte[] br
        try (var is = file$tf.openInputStream()) {
            br = is.readAllBytes()
        }
        //
        sw2.stop()
        sw0.stop()
        println("[File = ${name}]\t[Read]\t[Time = $sw2]")
        println("[File = ${name}]\t[Sum]\t[Time = $sw0]")
        //
        return br
    }

    static {
        new File("test/out").mkdir()
    }
}
