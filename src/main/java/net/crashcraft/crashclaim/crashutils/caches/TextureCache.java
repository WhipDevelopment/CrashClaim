package net.crashcraft.crashclaim.crashutils.caches;

import com.destroystokyo.paper.event.profile.FillProfileEvent;
import com.destroystokyo.paper.event.profile.PreFillProfileEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


//https://github.com/CrashCraftNetwork/CrashUtils/blob/master/src/main/java/dev/whip/crashutils/caches/TextureCache.java
public class TextureCache implements Listener {
    private final Cache<UUID, ProfileProperty> cache;

    public TextureCache(){
        cache = new Cache2kBuilder<UUID, ProfileProperty>() {}
                .name("TextureCache")
                .expireAfterWrite(3, TimeUnit.DAYS)
                .entryCapacity(10000)
                .disableStatistics(true)
                .build();
    }

    @EventHandler
    public void onPreFillProfileEvent(PreFillProfileEvent e){
        if (!e.getPlayerProfile().hasTextures()){
            ProfileProperty profileProperty = cache.peek(e.getPlayerProfile().getId());
            if (profileProperty != null) {
                e.getPlayerProfile().setProperty(profileProperty);
            }
        }
    }
    //TODO make sure this works
    @EventHandler
    public void onFillProfileEvent(FillProfileEvent e){
        final PlayerProfile profile = e.getPlayerProfile();
        if (profile.getId() != null && profile.hasTextures()){
            for (ProfileProperty profileProperty : profile.getProperties()){
                if (profileProperty.getName().equals("textures")){
                    cache.put(profile.getId(), profileProperty);
                    break;
                }
            }
        }
    }
}