package net.crashcraft.crashclaim.compatability;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.crashcraft.crashclaim.compatability.versions.Wrapper1_16_R1;
import net.crashcraft.crashclaim.compatability.versions.inherits.Wrapper1_16_R2;
import net.crashcraft.crashclaim.compatability.versions.inherits.Wrapper1_16_R3;
import net.crashcraft.crashclaim.compatability.versions.Wrapper1_17_R1;
import net.crashcraft.crashclaim.config.GlobalConfig;
import org.bukkit.Bukkit;

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
            Wrapper1_16_R1.class,
            Wrapper1_16_R2.class,
            Wrapper1_16_R3.class,
            Wrapper1_17_R1.class
    );

    public CompatabilityManager(ProtocolManager manager){
        protocolManager = manager;

        String forcedVersion = GlobalConfig.forcedVersionString;
        if (forcedVersion != null && !forcedVersion.equals("")){
            wrapper = match(forcedVersion);
        } else {
            wrapper = match(Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1));
        }
    }

    private CompatabilityWrapper match(String serverVersion) {
        try {
            return versions.stream()
                    .filter(version -> version.getSimpleName().substring(7).equals(serverVersion))
                    .findFirst().orElseThrow(() -> new RuntimeException("Your server version [" + serverVersion + "] isn't supported in CrashClaim! Setting use-this-version-instead to a version string, like 1_17_R1, will skip this check and might work. Proceed with caution."))
                    .getDeclaredConstructor().newInstance();
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
