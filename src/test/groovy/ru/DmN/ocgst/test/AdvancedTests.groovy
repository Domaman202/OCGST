package ru.DmN.ocgst.test

import ru.DmN.ocgst.Main
import ru.DmN.ocgst.api.OCConnection
import ru.DmN.ocgst.complex.OCCFile
import ru.DmN.ocgst.util.Actions

class AdvancedTests {
    private static final Scanner scanner = new Scanner(System.in)

    static void main(String[] args) {
        Main.main()
        while (Main.ocgst.connections.size() < 24) Thread.onSpinWait()
        /// Ручная
//        var fss = new Tuple2<OCConnection, Integer>[Main.ocgst.connections.size()]
//        for (j in 0..<Main.ocgst.connections.size()) {
//            var connection = Main.ocgst.connections[j]
//            connection.pushAction(Actions.DRIVE_LIST, 0, "", {
//                var arr = new String(it.read()).split(";")
//                for (i in 0..<arr.length) {
//                    println("[${i+1}] ${arr[i]}")
//                }
//            })
//            sleep(1000)
//            print("Select drive: ")
//            fss[j] = new Tuple2<>(connection, scanner.nextInt())
//        }
//        println(fss.v2)
        /// Авто
        var fss = Main.ocgst.connections.collect {new Tuple2<>(it, 1)}
        //
        var dir$test = new OCCFile(fss, "test")
        dir$test.mkdir()
        //
        MainTests.test(dir$test)
        //
        dir$test.delete()
    }
}
