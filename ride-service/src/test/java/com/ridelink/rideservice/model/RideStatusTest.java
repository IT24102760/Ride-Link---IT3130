package com.ridelink.rideservice.model;

import org.junit.jupiter.api.Test;

import static com.ridelink.rideservice.model.RideStatus.*;
import static org.junit.jupiter.api.Assertions.*;

class RideStatusTest {

    @Test
    void happyPath_isAllowedStepByStep() {
        assertTrue(REQUESTED.canTransitionTo(ASSIGNED));
        assertTrue(ASSIGNED.canTransitionTo(ACCEPTED));
        assertTrue(ACCEPTED.canTransitionTo(IN_PROGRESS));
        assertTrue(IN_PROGRESS.canTransitionTo(COMPLETED));
    }

    @Test
    void cancel_isAllowedBeforeTheTripStarts() {
        assertTrue(REQUESTED.canTransitionTo(CANCELLED));
        assertTrue(ASSIGNED.canTransitionTo(CANCELLED));
        assertTrue(ACCEPTED.canTransitionTo(CANCELLED));
    }

    @Test
    void cancel_isNotAllowedOnceTheTripHasStarted() {
        assertFalse(IN_PROGRESS.canTransitionTo(CANCELLED));
    }

    @Test
    void skippingSteps_isNotAllowed() {
        assertFalse(REQUESTED.canTransitionTo(IN_PROGRESS));
        assertFalse(ASSIGNED.canTransitionTo(COMPLETED));
        assertFalse(ACCEPTED.canTransitionTo(COMPLETED));
    }

    @Test
    void completedAndCancelled_areFinal() {
        for (RideStatus next : RideStatus.values()) {
            assertFalse(COMPLETED.canTransitionTo(next));
            assertFalse(CANCELLED.canTransitionTo(next));
        }
    }
}