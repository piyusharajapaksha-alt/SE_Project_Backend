package com.staffhub.state.leave;

/**
 * Pending state.
 *
 * A newly submitted leave request starts in this state.
 *
 * Pending requests can:
 * - be edited
 * - be approved
 * - be rejected
 * - be cancelled
 */
public class PendingLeaveState implements LeaveState {

    @Override
    public String getStatus() {
        return "Pending";
    }

    @Override
    public boolean canEdit() {
        return true;
    }

    @Override
    public boolean canApprove() {
        return true;
    }

    @Override
    public boolean canReject() {
        return true;
    }

    @Override
    public boolean canCancel() {
        return true;
    }
}