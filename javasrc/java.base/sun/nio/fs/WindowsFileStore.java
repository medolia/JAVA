/*
 * Copyright (c) 2008, 2019, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package sun.nio.fs;

import java.nio.file.*;
import java.nio.file.attribute.*;
import java.io.IOException;

import static sun.nio.fs.WindowsConstants.*;
import static sun.nio.fs.WindowsNativeDispatcher.*;

/**
 * Windows implementation of FileStore.
 */

class WindowsFileStore
    extends FileStore
{
    private final String root;
    private final VolumeInformation volInfo;
    private final int volType;
    private final String displayName;   // returned by toString

    private WindowsFileStore(String root) throws WindowsException {
        assert root.charAt(root.length()-1) == '\\';
        this.root = root;
        this.volInfo = GetVolumeInformation(root);
        this.volType = GetDriveType(root);

        // file store "display name" is the volume name if available
        String vol = volInfo.volumeName();
        if (!vol.isEmpty()) {
            this.displayName = vol;
        } else {
            // TBD - should we map all types? Does this need to be localized?
            this.displayName = (volType == DRIVE_REMOVABLE) ? "Removable Disk" : "";
        }
    }

    static WindowsFileStore create(String root, boolean ignoreNotReady)
        throws IOException
    {
        try {
            return new WindowsFileStore(root);
        } catch (WindowsException x) {
            if (ignoreNotReady && x.lastError() == ERROR_NOT_READY)
                return null;
            x.rethrowAsIOException(root);
            return null; // keep compiler happy
        }
    }

    static WindowsFileStore create(WindowsPath file) throws IOException {
        try {
            // if the file is a link then GetVolumePathName returns the
            // volume that the link is on so we need to call it with the
            // final target
            String target = WindowsLinkSupport.getFinalPath(file, true);
            try {
                return createFromPath(target);
            } catch (WindowsException e) {
                if (e.lastError() != ERROR_DIR_NOT_ROOT)
                    throw e;
                target = WindowsLinkSupport.getFinalPath(file);
                if (target == null)
                    throw new FileSystemException(file.getPathForExceptionMessage(),
                            null, "Couldn't resolve path");
                return createFromPath(target);
            }
        } catch (WindowsException x) {
            x.rethrowAsIOException(file);
            return null; // keep compiler happy
        }
    }

    private static WindowsFileStore createFromPath(String target) throws WindowsException {
        String root = GetVolumePathName(target);
        return new WindowsFileStore(root);
    }

    VolumeInformation volumeInformation() {
        return volInfo;
    }

    int volumeType() {
        return volType;
    }

    @Override
    public String name() {
        return volInfo.volumeName();   // "SYSTEM", "DVD-RW", ...
    }

    @Override
    public String type() {
        return volInfo.fileSystemName();  // "FAT", "NTFS", ...
    }

    @Override
    public boolean isReadOnly() {
        return ((volInfo.flags() & FILE_READ_ONLY_VOLUME) != 0);
    }

    // read the free space info
    private DiskFreeSpace readDiskFreeSpaceEx() throws IOException {
        try {
            return GetDiskFreeSpaceEx(root);
        } catch (WindowsException x) {
            x.rethrowAsIOException(root);
            return null;
        }
    }

    private DiskFreeSpace readDiskFreeSpace() throws IOException {
        try {
            return GetDiskFreeSpace(root);
        } catch (WindowsException x) {
            x.rethrowAsIOException(root);
            return null;
        }
    }

    @Override
    public long getTotalSpace() throws IOException {
        long space = readDiskFreeSpaceEx().totalNumberOfBytes();
        return space >= 0 ? space : Long.MAX_VALUE;
    }

    @Override
    public long getUsableSpace() throws IOException {
        long space = readDiskFreeSpaceEx().freeBytesAvailable();
        return space >= 0 ? space : Long.MAX_VALUE;
    }

    @Override
    public long getUnallocatedSpace() throws IOException {
        long space = readDiskFreeSpaceEx().freeBytesAvailable();
        return space >= 0 ? space : Long.MAX_VALUE;
    }

    @Override
    public long getBlockSize() throws IOException {
        return readDiskFreeSpace().bytesPerSector();
    }

    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
        if (type == null)
            throw new NullPointerException();
        return (V) null;
    }

    @Override
    public Object getAttribute(String attribute) throws IOException {
        // standard
        if (attribute.equals("totalSpace"))
            return getTotalSpace();
        if (attribute.equals("usableSpace"))
            return getUsableSpace();
        if (attribute.equals("unallocatedSpace"))
            return getUnallocatedSpace();
        if (attribute.equals("bytesPerSector"))
            return getBlockSize();
        // windows specific for testing purposes
        if (attribute.equals("volume:vsn"))
            return volInfo.volumeSerialNumber();
        if (attribute.equals("volume:isRemovable"))
            return volType == DRIVE_REMOVABLE;
        if (attribute.equals("volume:isCdrom"))
            return volType == DRIVE_CDROM;
        throw new UnsupportedOperationException("'" + attribute + "' not recognized");
    }

    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
        if (type == null)
            throw new NullPointerException();
        if (type == BasicFileAttributeView.class || type == DosFileAttributeView.class)
            return true;
        if (type == AclFileAttributeView.class || type == FileOwnerAttributeView.class)
            return ((volInfo.flags() & FILE_PERSISTENT_ACLS) != 0);
        if (type == UserDefinedFileAttributeView.class)
            return ((volInfo.flags() & FILE_NAMED_STREAMS) != 0);
        return false;
    }

    @Override
    public boolean supportsFileAttributeView(String name) {
        if (name.equals("basic") || name.equals("dos"))
            return true;
        if (name.equals("acl"))
            return supportsFileAttributeView(AclFileAttributeView.class);
        if (name.equals("owner"))
            return supportsFileAttributeView(FileOwnerAttributeView.class);
        if (name.equals("user"))
            return supportsFileAttributeView(UserDefinedFileAttributeView.class);
        return false;
    }

    @Override
    public boolean equals(Object ob) {
        if (ob == this)
            return true;
        if (!(ob instanceof WindowsFileStore))
            return false;
        WindowsFileStore other = (WindowsFileStore)ob;
        return root.equals(other.root);
    }

    @Override
    public int hashCode() {
        return root.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(displayName);
        if (sb.length() > 0)
            sb.append(" ");
        sb.append("(");
        // drop trailing slash
        sb.append(root.subSequence(0, root.length()-1));
        sb.append(")");
        return sb.toString();
    }
 }
