package com.example.lightblockviewer.client;

/**
 * Toggle state of the mod on the client.
 *
 * <p>The state is owned by the server: the client only requests a change and applies the state the
 * server confirms. Client-side predictions such as the block shape and collision therefore never
 * disagree with the server, and leaving a world always starts from a clean, disabled state.
 */
public final class ClientToggleState {

    /** Ticks to wait for the server to confirm a request before warning the player. */
    private static final int CONFIRMATION_TIMEOUT_TICKS = 40;

    private static boolean enabled = false;
    private static boolean desired = false;
    private static int pendingTicks = 0;

    private ClientToggleState() {}

    /** @return the state confirmed by the server, which drives rendering and client predictions */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Requests the opposite of the current state.
     *
     * @return the state to request from the server
     */
    public static boolean requestToggle() {
        desired = !desired;
        pendingTicks = CONFIRMATION_TIMEOUT_TICKS;
        return desired;
    }

    /** Applies the state confirmed by the server. */
    public static void confirm(boolean value) {
        enabled = value;
        desired = value;
        pendingTicks = 0;
    }

    /**
     * Ages an unanswered request by one tick.
     *
     * @return {@code true} once, when a request was not answered, which means the server did not
     *     accept it - usually because the mod is not installed there
     */
    public static boolean tickPending() {
        if (pendingTicks <= 0) {
            return false;
        }

        if (--pendingTicks > 0) {
            return false;
        }

        desired = enabled;
        return true;
    }

    /** Forgets all state, e.g. when leaving a world. */
    public static void reset() {
        enabled = false;
        desired = false;
        pendingTicks = 0;
    }
}
