package net.crashcraft.crashclaim.localization;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

public class LocalizationLoader {
    static MiniMessage parser;
    static MiniMessage userParser;
    static PlaceholderManager placeholderManager;

    public static void initialize(){
        parser = MiniMessage.builder().build();

        userParser = MiniMessage.builder().tags(
                TagResolver.builder()
                        .resolver(StandardTags.color())
                        .resolver(StandardTags.decorations())
                        .resolver(StandardTags.rainbow())
                        .resolver(StandardTags.reset())
                        .resolver(StandardTags.gradient())
                        .build()
        ).build();

        placeholderManager = new PlaceholderManager();

        Localization.rebuildCachedMessages();
    }

    public static void register(){
        placeholderManager.registerPlaceholders();
    }
}
