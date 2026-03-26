package com.example.lightblockviewer.state;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public final class PlayerToggleState {

    private static final Set<UUID> ENABLED_PLAYERS =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    // Client-side check callback, set by client initializer
    private static Predicate<Player> clientEnabledCheck = player -> false;

    private PlayerToggleState() {}

    public static void setClientEnabledCheck(Predicate<Player> check) {
        clientEnabledCheck = check;
    }

    public static boolean isClientEnabled(Player player) {
        return clientEnabledCheck.test(player);
    }

    public static void setEnabled(ServerPlayer player, boolean enabled) {
        if (enabled) {
            ENABLED_PLAYERS.add(player.getUUID());
        } else {
            ENABLED_PLAYERS.remove(player.getUUID());
        }
    }

    public static boolean isEnabled(ServerPlayer player) {
        return ENABLED_PLAYERS.contains(player.getUUID());
    }

    public static boolean isEnabled(UUID uuid) {
        return ENABLED_PLAYERS.contains(uuid);
    }

    public static void removePlayer(UUID uuid) {
        ENABLED_PLAYERS.remove(uuid);
    }
}
