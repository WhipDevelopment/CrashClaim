package net.crashcraft.crashclaim.nms;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

/**
 * Inspired by AnvilGUI
 * https://github.com/WesJD/AnvilGUI/blob/master/api/src/main/java/net/wesjd/anvilgui/version/VersionMatcher.java
 */
public class NMSManager {
    private static ProtocolManager protocolManager;

    private static final WrappedDataWatcher.Serializer byteSerializer = WrappedDataWatcher.Registry.get(Byte.class);
    private static final WrappedDataWatcher.Serializer integerSerializer = WrappedDataWatcher.Registry.get(Integer.class);

    private final NMSHandler handler;

    public NMSManager(ProtocolManager manager){
        protocolManager = manager;
        handler = new NMSHandler();
    }


    public NMSHandler getHandler() {
        return handler;
    }

    public static ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public static WrappedDataWatcher.Serializer getByteSerializer() {
        return byteSerializer;
    }

    public static WrappedDataWatcher.Serializer getIntegerSerializer() {
        return integerSerializer;
    }
}
