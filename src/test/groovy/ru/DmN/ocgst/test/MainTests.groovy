package ru.DmN.ocgst.test

import com.mkyong.io.image.ImageUtils
import org.apache.commons.lang3.time.StopWatch
import ru.DmN.ocgst.Main
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
                println("[$i] ${arr[i]}")
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
        test0(dir$test, "A")
        test1(dir$test, "A")
        test0(dir$test, "B")
        test1(dir$test, "B")
        test1(dir$test, "C")
        test0(dir$test, "C")
        //
        dir$test.delete()
    }

    private static void test0(OCFile dir$test, String file) {
        var file$tf = dir$test.subfile("text${file}.txt")
        var bw = new File("test/in${file}.txt").bytes
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
        println("[File = text${file}.txt][Time = $sw]")
        //
//        println(br)
        new File("test/out${file}.txt").newOutputStream().withWriter { writer -> (br as List<Byte>).forEach(writer::write)}
    }

    private static void test1(OCFile dir$test, String file) {
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
