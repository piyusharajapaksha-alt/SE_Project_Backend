package com.staffhub.state.leave;

/**
 * Factory responsible for creating the correct LeaveState
 * object from the leave status stored in the database.
 *
 * Example:
 *
 * "Pending"   -> PendingLeaveState
 * "Approved"  -> ApprovedLeaveState
 * "Rejected"  -> RejectedLeaveState
 * "Cancelled" -> CancelledLeaveState
 */
public final class LeaveStateFactory {

    private LeaveStateFactory() {
        // Utility class.
    }

    public static LeaveState getState(String status) {

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Leave status is required");
        }

        String normalizedStatus = status.trim();

        return switch (normalizedStatus) {

            case "Pending" ->
                    new PendingLeaveState();

            case "Approved" ->
                    new ApprovedLeaveState();

            case "Rejected" ->
                    new RejectedLeaveState();

            case "Cancelled" ->
                    new CancelledLeaveState();

            default ->
                    throw new IllegalArgumentException(
                            "Unknown leave status: "
                                    + normalizedStatus);
        };
    }
}