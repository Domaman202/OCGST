package ru.DmN.ocgst.util

import groovy.transform.CompileStatic

@CompileStatic
enum Actions {
    READ,       // 0
    WRITE,      // 1
    DRIVE_LIST, // 2
    ISDIR,      // 3
    MKDIR,      // 4
    DELETE      // 5
}
