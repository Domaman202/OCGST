package ru.DmN.ocgst.test

import com.mkyong.io.image.ImageUtils
import org.apache.commons.lang3.time.StopWatch
import ru.DmN.ocgst.Main
import ru.DmN.ocgst.api.IOCFile

import javax.imageio.ImageIO

class MainTests {
    static void main(String[] args) {
        Main.main()
        //
        sleep(5000)
        //
        var drive = Main.ocgst.connections[0].getDrives(true)[0]
        println(drive.name)
        var dir$test = drive.getRoot().subFile("test")
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

    private static void test0(IOCFile dir$test, String file) {
        var file$tf = dir$test.subFile("text${file}.txt")
        var bw = new File("test/in${file}.txt").bytes
//        println(bw)
        //
        var sw = new StopWatch()
        sw.start()
        //
        file$tf.write(bw)
        var br = file$tf.read()
        //
        sw.stop()
        println("[File = text${file}.txt][Time = $sw]")
        //
//        println(br)
        new File("test/out${file}.txt").newOutputStream().withWriter { writer -> (br as List<Byte>).forEach(writer::write)}
    }

    private static void test1(IOCFile dir$test, String file) {
        var file$tf = dir$test.subFile("image${file}.png")
        var bw = ImageUtils.toByteArray(ImageIO.read(new File("test/in${file}.png")), "PNG")
//        println(bw)
        //
        var sw = new StopWatch()
        sw.start()
        //
        file$tf.write(bw)
        var br = file$tf.read()
        //
        sw.stop()
        println("[File = image${file}.png][Time = $sw]")
        //
//        println(br)
        ImageIO.write(ImageUtils.toBufferedImage((br as List<Byte>).toArray() as byte[]), "PNG", new File("test/out${file}.png"))
    }
}
