package ru.DmN.ocgst.util

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@Canonical
@CompileStatic
@EqualsAndHashCode
class Action {
    final Actions action
    final int fs
    final String path

    Action(Actions action, int fs, String path) {
        this.action = action
        this.path = path
        this.fs = fs
    }
}