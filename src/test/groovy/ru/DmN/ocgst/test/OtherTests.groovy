package ru.DmN.ocgst.test

import com.mkyong.io.image.ImageUtils
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import ru.DmN.ocgst.impl.OCFileImpl
import ru.DmN.ocgst.util.Utils

import javax.imageio.ImageIO

class OtherTests {
    static void main(String[] args) {
//        test0()
        test1()
    }

    private static void test0() {
        var bw = ImageUtils.toByteArray(ImageIO.read(new File("test/inA.png")), "PNG")
        println(bw)
        var br = bw.toList()
        println(br)
        ImageIO.write(ImageUtils.toBufferedImage(br.toArray() as byte[]), "PNG", new File("test/outA.png"))
    }

    private static void test1() {
        var in$str = JsonOutput.toJson(ImageUtils.toByteArray(ImageIO.read(new File("test/inB.png")), "PNG"))
        var chars = Utils.split(in$str.chars.toList(), OCFileImpl.FILE_MAX_SIZE)
        var out$str = new StringBuilder()
        chars.forEach { it.forEach { out$str.append(it) } }
        ImageIO.write(ImageUtils.toBufferedImage((new JsonSlurper().parseText(out$str.toString()) as List<Byte>).toArray() as byte[]), "PNG", new File("test/outB.png"))
    }
}
