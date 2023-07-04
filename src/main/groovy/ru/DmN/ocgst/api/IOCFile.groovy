package ru.DmN.ocgst.api

interface IOCFile {
    String getName()
    String getAbsolutePath()
    IOCFile subfile(String path)
    void mkdir()
    boolean isDirectory()
    void delete()
    InputStream openInputStream()
    OutputStream openOutputStream()
}