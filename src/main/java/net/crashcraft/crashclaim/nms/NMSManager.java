package net.crashcraft.crashclaim.nms;

/**
 * Inspired by AnvilGUI
 * https://github.com/WesJD/AnvilGUI/blob/master/api/src/main/java/net/wesjd/anvilgui/version/VersionMatcher.java
 */
public class NMSManager {

    private final NMSHandler handler;

    public NMSManager(){
        handler = new NMSHandler();
    }


    public NMSHandler getHandler() {
        return handler;
    }

}
