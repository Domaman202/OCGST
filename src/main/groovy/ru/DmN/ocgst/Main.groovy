package ru.DmN.ocgst


import ru.ind.tgs.GroovyShellService

class Main {
    static final def ocgst = new OCGST()
    static final def ocgstThread = new Thread({ ocgst.main() })
    static final def service = new GroovyShellService()

    static void main(String[] args) {
        ocgstThread.setName("OCGST")
        ocgstThread.start()
        service.setListenPort(25586)
        service.setBindings(new Binding([connections: ocgst.connections]))
        service.start()
    }
}
