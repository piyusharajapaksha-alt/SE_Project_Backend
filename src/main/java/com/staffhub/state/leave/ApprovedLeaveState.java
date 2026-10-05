package com.staffhub.state.leave;

/**
 * Approved state.
 *
 * Once a leave request has been approved,
 * it cannot be edited, rejected, cancelled,
 * or approved again.
 */
public class ApprovedLeaveState implements LeaveState {

    @Override
    public String getStatus() {
        return "Approved";
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