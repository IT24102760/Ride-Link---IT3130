package com.ridelink.rideservice.model;

public enum RideStatus {
    REQUESTED,
    ASSIGNED,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    /** Returns true if a ride in this status is allowed to move to the next status. */
    public boolean canTransitionTo(RideStatus next) {
        return switch (this) {
            case REQUESTED   -> next == ASSIGNED || next == CANCELLED;
            case ASSIGNED    -> next == ACCEPTED || next == CANCELLED;
            case ACCEPTED    -> next == IN_PROGRESS || next == CANCELLED;
            case IN_PROGRESS -> next == COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}