package com.example.lightblockviewer.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientToggleStateTest {

    @AfterEach
    void resetState() {
        ClientToggleState.reset();
    }

    @Test
    void requestAloneDoesNotChangeTheState() {
        assertFalse(ClientToggleState.isEnabled());

        assertTrue(ClientToggleState.requestToggle());
        assertFalse(ClientToggleState.isEnabled(), "the client must wait for the server to confirm");

        ClientToggleState.confirm(true);
        assertTrue(ClientToggleState.isEnabled());
    }

    @Test
    void unansweredRequestWarnsAndKeepsTheState() {
        ClientToggleState.requestToggle();

        boolean warned = false;
        for (int tick = 0; tick < 200; tick++) {
            warned |= ClientToggleState.tickPending();
        }

        assertTrue(warned, "a request the server never answers must warn the player");
        assertFalse(ClientToggleState.isEnabled(), "an unanswered request must not enable anything");
        assertTrue(ClientToggleState.requestToggle(), "the next press must ask for ON again");
    }

    @Test
    void confirmedRequestNeverWarns() {
        ClientToggleState.requestToggle();
        ClientToggleState.confirm(true);

        for (int tick = 0; tick < 200; tick++) {
            assertFalse(ClientToggleState.tickPending());
        }
        assertTrue(ClientToggleState.isEnabled());
    }

    @Test
    void resetStartsFromDisabled() {
        ClientToggleState.requestToggle();
        ClientToggleState.confirm(true);

        ClientToggleState.reset();

        assertFalse(ClientToggleState.isEnabled());
        assertFalse(ClientToggleState.tickPending());
        assertTrue(ClientToggleState.requestToggle(), "after a reset the first press turns it on");
    }
}
