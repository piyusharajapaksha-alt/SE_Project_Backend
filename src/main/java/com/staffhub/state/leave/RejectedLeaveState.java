package com.staffhub.state.leave;

/**
 * Rejected state.
 *
 * A rejected leave request is considered final
 * in the current StaffHub workflow.
 */
public class RejectedLeaveState implements LeaveState {

    @Override
    public String getStatus() {
        return "Rejected";
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