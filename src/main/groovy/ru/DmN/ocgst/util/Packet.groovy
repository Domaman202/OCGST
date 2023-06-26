package ru.DmN.ocgst.util

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@Canonical
@EqualsAndHashCode
class Packet {
    int id
    String action
    Object data
}