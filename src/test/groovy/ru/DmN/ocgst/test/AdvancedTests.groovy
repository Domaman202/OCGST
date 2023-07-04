package ru.DmN.ocgst.test

import groovy.transform.CompileStatic
import ru.DmN.ocgst.Main
import ru.DmN.ocgst.api.OCConnection
import ru.DmN.ocgst.impl.OCCFile

@CompileStatic
class AdvancedTests {
    private static final int CONNECTIONS_COUNT = 64

    static void main(String[] args) {
        Main.main()
        while (Main.ocgst.connections.size() < CONNECTIONS_COUNT) Thread.onSpinWait()
        //
        var fss = new Tuple2<OCConnection, Integer>[CONNECTIONS_COUNT]
        for (i in 0..<CONNECTIONS_COUNT) {
            fss[i] = new Tuple2<>(Main.ocgst.connections[i], 1)
        }
        //
        var dir$test = new OCCFile(fss, "test")
        dir$test.mkdir()
        //
        MainTests.test(fss.collect { it.v1 }, dir$test)
        //
        dir$test.delete()
    }
}
