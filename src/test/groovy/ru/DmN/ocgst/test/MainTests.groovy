package ru.DmN.ocgst.test

import com.mkyong.io.image.ImageUtils
import org.apache.commons.lang3.time.StopWatch
import ru.DmN.ocgst.Main
import ru.DmN.ocgst.api.IOCFile
import ru.DmN.ocgst.api.OCFile
import ru.DmN.ocgst.util.Actions

import javax.imageio.ImageIO

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
        test(dir$test)
        //
        dir$test.delete()
    }

    static void test(IOCFile dir$test) {
        test0(dir$test, "A.txt")
        test1(dir$test, "A")
        test0(dir$test, "B.txt")
        test1(dir$test, "B")
        test1(dir$test, "C")
        test0(dir$test, "D.txt")
        test0(dir$test, "E.mp3")
    }

    static void test0(IOCFile dir$test, String file) {
        var file$tf = dir$test.subfile("text${file}.data")
        byte[] bw
        try (var input = new FileInputStream("test/in${file}")) {
            bw = input.readAllBytes()
        }
        //
        var sw = new StopWatch()
        sw.start()
        //
        try (var os = file$tf.openOutputStream()) {
            os.write(bw)
        }
        //
        byte[] br
        try (var is = file$tf.openInputStream()) {
            br = is.readAllBytes()
        }
        //
        sw.stop()
        println("[File = text${file}][Time = $sw]")
        //
        try (var output = new FileOutputStream("test/out${file}")) {
            output.write(br)
        }
    }

    static void test1(IOCFile dir$test, String file) {
        var file$tf = dir$test.subfile("image${file}.png")
        var bw = ImageUtils.toByteArray(ImageIO.read(new File("test/in${file}.png")), "PNG")
//        println(bw)
        //
        var sw = new StopWatch()
        sw.start()
        //
        try (var os = file$tf.openOutputStream()) {
            os.write(bw)
        }
        //
        byte[] br
        try (var is = file$tf.openInputStream()) {
            br = is.readAllBytes()
        }
        //
        sw.stop()
        println("[File = image${file}.png][Time = $sw]")
        //
//        println(br)
        ImageIO.write(ImageUtils.toBufferedImage((br as List<Byte>).toArray() as byte[]), "PNG", new File("test/out${file}.png"))
    }
}
