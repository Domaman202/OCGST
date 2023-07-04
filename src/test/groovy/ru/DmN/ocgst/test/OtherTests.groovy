package ru.DmN.ocgst.test

import groovy.transform.CompileStatic

@CompileStatic
class OtherTests {
    static void main(String[] args) {
        var input = new BufferedInputStream(new FileInputStream("test/inE.mp3"))
        var out = new FileOutputStream("test/outE.mp3")
        out.write(input.readAllBytes() as Byte[] as byte[])
        out.flush()
        out.close()
    }
}
