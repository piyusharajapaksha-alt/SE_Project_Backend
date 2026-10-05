package com.staffhub.state.leave;

/**
 * Cancelled state.
 *
 * A cancelled leave request is final in the current
 * StaffHub workflow.
 */
public class CancelledLeaveState implements LeaveState {

    @Override
    public String getStatus() {
        return "Cancelled";
    }

    @Override
    public boolean canEdit() {
        return false;
    }

    @Override
    public boolean canApprove() {
        return false;
    }

    @Override
    public boolean canReject() {
        return false;
    }

    @Override
    public boolean canCancel() {
        return false;
    }
}