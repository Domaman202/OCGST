package ru.DmN.ocgst

import groovy.transform.CompileStatic
import ru.ind.tgs.GroovyShellService

@CompileStatic
class Main {
    static final OCGST ocgst = new OCGST()
    static final Thread ocgstThread = new Thread({ ocgst.main() })
    static final GroovyShellService service = new GroovyShellService()

    static void main(String[] args) {
        ocgstThread.setName("OCGST")
        ocgstThread.start()
        service.setListenPort(25586)
        service.setBindings(new Binding([connections: ocgst.connections]))
        service.start()
    }
}
