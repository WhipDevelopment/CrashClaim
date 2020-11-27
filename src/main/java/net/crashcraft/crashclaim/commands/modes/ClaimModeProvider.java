package net.crashcraft.crashclaim.commands.modes;

import java.util.UUID;

public interface ClaimModeProvider {
    void cleanup(UUID uuid);
}
