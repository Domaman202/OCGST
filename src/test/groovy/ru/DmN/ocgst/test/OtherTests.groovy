package ru.DmN.ocgst.test

import ru.DmN.ocgst.api.OCConnection
import ru.DmN.ocgst.api.OCFile
import ru.DmN.ocgst.util.Actions

class OtherTests {
    static void main(String[] args) {
        try (final def server = new ServerSocket(25585)) {
            final def connection = new OCConnection(server.accept())
            connection.start()

            connection.pushAction(Actions.DRIVE_LIST.ordinal(), "", 0, { println(new String(it.read())) })

            final def file = new OCFile(connection, 2, "test")
            try (final def os = file.openOutputStream()) {
                for (i in 0..<1000)
                    os.write("Hello, user$i!\n".bytes)
                os.write("Meow!".bytes)
            }
            try (final def is = file.openInputStream()) {
                println(new String(is.readAllBytes()))
            }
        }
    }
}
