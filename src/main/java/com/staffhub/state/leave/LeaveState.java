package com.staffhub.state.leave;

/**
 * State interface for the Leave Management State Pattern.
 *
 * Each leave status has different allowed operations.
 *
 * Example:
 *
 * Pending:
 *   - edit     -> allowed
 *   - approve  -> allowed
 *   - reject   -> allowed
 *   - cancel   -> allowed
 *
 * Approved:
 *   - edit     -> not allowed
 *   - approve  -> not allowed
 *   - reject   -> not allowed
 *   - cancel   -> not allowed
 */
public interface LeaveState {

    /**
     * Returns the database/application status represented
     * by this state.
     */
    String getStatus();

    /**
     * Determines whether the leave request can be edited.
     */
    boolean canEdit();

    /**
     * Determines whether the leave request can be approved.
     */
    boolean canApprove();

    /**
     * Determines whether the leave request can be rejected.
     */
    boolean canReject();

    /**
     * Determines whether the leave request can be cancelled.
     */
    boolean canCancel();
}