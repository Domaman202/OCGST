package ru.DmN.ocgst.impl


import groovy.transform.EqualsAndHashCode
import groovy.transform.TypeChecked
import ru.DmN.ocgst.api.IOCFile

@TypeChecked
@EqualsAndHashCode
class OCCachedDriveImpl extends OCDriveImpl {
    protected final WeakHashMap<String, IOCFile> files = new WeakHashMap<>()

    OCCachedDriveImpl(OCConnectionImpl connection, String name) {
        super(connection, name)
    }

    @Override
    IOCFile getFile(String path) {
        return this.files.get(path, new OCFileImpl(this, path))
    }
}
