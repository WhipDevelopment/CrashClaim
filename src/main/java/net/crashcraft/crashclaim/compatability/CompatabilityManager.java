package net.crashcraft.crashclaim.compatability;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.compatability.versions.*;
import net.crashcraft.crashclaim.config.GlobalConfig;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

/**
 * Inspired by AnvilGUI
 * https://github.com/WesJD/AnvilGUI/blob/master/api/src/main/java/net/wesjd/anvilgui/version/VersionMatcher.java
 */
public class CompatabilityManager {
    private static ProtocolManager protocolManager;

    private static final WrappedDataWatcher.Serializer byteSerializer = WrappedDataWatcher.Registry.get(Byte.class);
    private static final WrappedDataWatcher.Serializer integerSerializer = WrappedDataWatcher.Registry.get(Integer.class);

    private final CompatabilityWrapper wrapper;

    private final List<Class<? extends CompatabilityWrapper>> versions = Arrays.asList(
            Wrapper1_16_0.class,
            Wrapper1_16_1.class,
            Wrapper1_16_2.class,
            Wrapper1_16_3.class,
            Wrapper1_16_4.class,
            Wrapper1_16_5.class,
            Wrapper1_17_0.class,
            Wrapper1_17_1.class,
            Wrapper1_18_0.class,
            Wrapper1_18_1.class,
            Wrapper1_18_2.class,
            Wrapper1_19_0.class,
            Wrapper1_19_1.class,
            Wrapper1_19_2.class,
            Wrapper1_19_3.class,
            Wrapper1_19_4.class,
            Wrapper1_20_0.class
    );

    public CompatabilityManager(ProtocolManager manager){
        protocolManager = manager;

        String forcedVersion = GlobalConfig.forcedVersionString;
        if (forcedVersion != null && !forcedVersion.equals("")){
            wrapper = match(forcedVersion);
        } else {
            wrapper = match(ProtocolLibrary.getProtocolManager().getMinecraftVersion().getVersion().replace(".", "_"));
        }
    }

    private CompatabilityWrapper match(String serverVersion) {
        try {
            for (Class<? extends CompatabilityWrapper> version : versions){
                if (version.getSimpleName().substring(7).equals(serverVersion)){
                    return version.getDeclaredConstructor().newInstance();
                }
            }

            // Find partial match if other not possible.

            final String partialMatchVersion = serverVersion.substring(0, serverVersion.length() - 2);
            for (int x = versions.size() - 1; x >= 0; x--){ // Iterate backwards so 1.16.3 mappings are used over 1.16.1 ones
                Class<? extends CompatabilityWrapper> version = versions.get(x);

                if (version.getSimpleName().substring(7, version.getSimpleName().length() - 2).equals(partialMatchVersion)){
                    CrashClaim.getPlugin().getLogger().severe("Your server version [" + serverVersion + "] is not fully supported in CrashClaim! Defaulting to closest mapped subversion. Report this.");
                    return version.getDeclaredConstructor().newInstance();
                }
            }

            final String latestVersion = versions.get(versions.size() - 1).getSimpleName().substring(7);

            throw new RuntimeException("Your server version [" + serverVersion + "] isn't supported in CrashClaim! Setting use-this-version-instead to a version string, like " + latestVersion + ", will skip this check and might work. Proceed with caution.");
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    public CompatabilityWrapper getWrapper() {
        return wrapper;
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
