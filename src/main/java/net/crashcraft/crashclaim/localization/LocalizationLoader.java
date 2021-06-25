package net.crashcraft.crashclaim.localization;

import net.crashcraft.crashclaim.CrashClaim;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class LocalizationLoader {
    static MiniMessage parser;

    public static void initialize(){
        parser = MiniMessage.builder()
                .parsingErrorMessageConsumer((s) -> CrashClaim.getPlugin().getLogger().warning("MiniMessage Parsing Error: " + s))
                .build();

        Localization.rebuildCachedMessages();
    }
}
