package ru.DmN.ocgst.test

class OtherTests {
    static void main(String[] args) {
        var input = new BufferedInputStream(new FileInputStream("test/inE.mp3"))
        var out = new FileOutputStream("test/outE.mp3")
        out.write(input.readAllBytes() as Byte[] as byte[])
        out.flush()
        out.close()
    }
}
