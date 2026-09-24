/* ============================================================
   STAFFHUB - SQL SERVER DATABASE
   Converted from PostgreSQL
   SQL Server / SSMS version
   ============================================================ */

CREATE DATABASE StaffHub;
GO

USE StaffHub;
GO


/* ============================================================
   EMPLOYEES
   ============================================================ */

CREATE TABLE employees (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,

    employee_number VARCHAR(50) NOT NULL UNIQUE,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,

    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),

    department VARCHAR(100),
    position VARCHAR(100),
    role VARCHAR(50),

    employment_status VARCHAR(30),

    hire_date DATE,
    address VARCHAR(MAX),
    emergency_contact VARCHAR(100),
    salary DECIMAL(12,2),

    gender VARCHAR(20)
);
GO


/* ============================================================
   TRAINING MANAGEMENT
   ============================================================ */

CREATE TABLE training_programs (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,

    title VARCHAR(200) NOT NULL,
    description VARCHAR(MAX),

    trainer VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL,

    start_date DATE NOT NULL,
    end_date DATE,

    location VARCHAR(200) NOT NULL,

    capacity INT NOT NULL,

    /*
        PostgreSQL TEXT[] converted to JSON text.

        Example:
        ["Engineering"]
        ["Engineering","Human Resources"]
        ["Engineering","Human Resources","IT"]
    */
    training_for VARCHAR(MAX) NOT NULL
        CONSTRAINT df_training_for DEFAULT '[]',

    status VARCHAR(30) NOT NULL,

    CONSTRAINT chk_training_capacity
        CHECK (capacity > 0),

    CONSTRAINT chk_training_status
        CHECK (
            status IN (
                'Upcoming',
                'Ongoing',
                'Completed',
                'Cancelled'
            )
        ),

    CONSTRAINT chk_training_for_json
        CHECK (ISJSON(training_for) = 1)
);
GO


/* ============================================================
   GRIEVANCES
   ============================================================ */

CREATE TABLE grievances (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,

    employee_id VARCHAR(50) NOT NULL,

    category VARCHAR(100) NOT NULL,

    priority VARCHAR(30) NOT NULL
        CONSTRAINT df_grievance_priority DEFAULT 'Medium',

    description VARCHAR(MAX) NOT NULL,

    status VARCHAR(30) NOT NULL
        CONSTRAINT df_grievance_status DEFAULT 'New',

    assigned_to VARCHAR(50),

    created_at DATETIME2 NOT NULL
        CONSTRAINT df_grievance_created_at DEFAULT SYSDATETIME(),

    CONSTRAINT fk_grievance_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_number)
);
GO


/* ============================================================
   GRIEVANCE RESPONSES
   ============================================================ */

CREATE TABLE grievance_responses (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,

    grievance_id BIGINT NOT NULL,

    employee_id VARCHAR(50) NOT NULL,

    response_text VARCHAR(MAX) NOT NULL,

    created_at DATETIME2 NOT NULL
        CONSTRAINT df_grievance_response_created_at
        DEFAULT SYSDATETIME(),

    CONSTRAINT fk_response_grievance
        FOREIGN KEY (grievance_id)
        REFERENCES grievances(id)
        ON DELETE CASCADE,

    /*
        No ON DELETE CASCADE here because SQL Server can
        detect multiple cascade paths through grievances.
    */
    CONSTRAINT fk_response_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_number)
);
GO


/* ============================================================
   PERFORMANCE MANAGEMENT
   ============================================================ */

CREATE TABLE performance_reviews (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,

    employee_id VARCHAR(50) NOT NULL,

    review_period VARCHAR(7) NOT NULL,

    quality_of_work INT NOT NULL,
    productivity INT NOT NULL,
    teamwork INT NOT NULL,
    communication INT NOT NULL,
    responsibility INT NOT NULL,
    problem_solving INT NOT NULL,

    overall_rating DECIMAL(3,2) NOT NULL,

    manager_feedback VARCHAR(MAX),

    areas_for_improvement VARCHAR(MAX),

    status VARCHAR(30) NOT NULL
        CONSTRAINT df_performance_status
        DEFAULT 'Pending Review',

    created_at DATETIME2 NOT NULL
        CONSTRAINT df_performance_created_at
        DEFAULT SYSDATETIME(),

    updated_at DATETIME2 NOT NULL
        CONSTRAINT df_performance_updated_at
        DEFAULT SYSDATETIME(),

    CONSTRAINT fk_performance_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_number),

    CONSTRAINT unique_employee_review_period
        UNIQUE (employee_id, review_period),

    CONSTRAINT chk_quality_of_work
        CHECK (quality_of_work BETWEEN 1 AND 5),

    CONSTRAINT chk_productivity
        CHECK (productivity BETWEEN 1 AND 5),

    CONSTRAINT chk_teamwork
        CHECK (teamwork BETWEEN 1 AND 5),

    CONSTRAINT chk_communication
        CHECK (communication BETWEEN 1 AND 5),

    CONSTRAINT chk_responsibility
        CHECK (responsibility BETWEEN 1 AND 5),

    CONSTRAINT chk_problem_solving
        CHECK (problem_solving BETWEEN 1 AND 5),

    CONSTRAINT chk_overall_rating
        CHECK (overall_rating BETWEEN 1 AND 5),

    CONSTRAINT chk_performance_status
        CHECK (
            status IN (
                'Pending Review',
                'Completed'
            )
        ),

    /*
        PostgreSQL regex validation converted to SQL Server.
        Valid examples:
        2026-01
        2026-09
        2026-12
    */
    CONSTRAINT chk_review_period
        CHECK (
            review_period LIKE
                '[0-9][0-9][0-9][0-9]-[0-9][0-9]'
            AND
            TRY_CONVERT(
                DATE,
                review_period + '-01'
            ) IS NOT NULL
        )
);
GO


/* ============================================================
   LEAVE MANAGEMENT
   ============================================================ */

CREATE TABLE leave_requests (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,

    employee_id VARCHAR(50) NOT NULL,

    leave_type VARCHAR(50) NOT NULL,

    start_date DATE NOT NULL,
    end_date DATE NOT NULL,

    reason VARCHAR(MAX) NOT NULL,

    approver_id VARCHAR(50),

    status VARCHAR(30) NOT NULL
        CONSTRAINT df_leave_status
        DEFAULT 'Pending',

    comment VARCHAR(MAX),

    created_at DATETIME2 NOT NULL
        CONSTRAINT df_leave_created_at
        DEFAULT SYSDATETIME(),

    updated_at DATETIME2 NOT NULL
        CONSTRAINT df_leave_updated_at
        DEFAULT SYSDATETIME(),

    CONSTRAINT fk_leave_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_number),

    CONSTRAINT fk_leave_approver
        FOREIGN KEY (approver_id)
        REFERENCES employees(employee_number)
        ON DELETE SET NULL,

    CONSTRAINT chk_leave_dates
        CHECK (end_date >= start_date),

    CONSTRAINT chk_leave_status
        CHECK (
            status IN (
                'Pending',
                'Approved',
                'Rejected',
                'Cancelled'
            )
        )
);
GO


/* ============================================================
   EVENT MANAGEMENT
   ============================================================ */

CREATE TABLE events (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,

    title VARCHAR(200) NOT NULL,

    description VARCHAR(MAX),

    organizer_id VARCHAR(50) NOT NULL,

    category VARCHAR(100) NOT NULL,

    event_date DATE NOT NULL,

    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    location VARCHAR(200) NOT NULL,

    capacity INT NOT NULL,

    status VARCHAR(30) NOT NULL
        CONSTRAINT df_event_status
        DEFAULT 'Upcoming',

    created_at DATETIME2 NOT NULL
        CONSTRAINT df_event_created_at
        DEFAULT SYSDATETIME(),

    updated_at DATETIME2 NOT NULL
        CONSTRAINT df_event_updated_at
        DEFAULT SYSDATETIME(),

    CONSTRAINT fk_event_organizer
        FOREIGN KEY (organizer_id)
        REFERENCES employees(employee_number),

    CONSTRAINT chk_event_capacity
        CHECK (capacity > 0),

    CONSTRAINT chk_event_status
        CHECK (
            status IN (
                'Upcoming',
                'Ongoing',
                'Completed',
                'Cancelled'
            )
        ),

    CONSTRAINT chk_event_times
        CHECK (end_time > start_time)
);
GO


/* ============================================================
   EVENT REGISTRATIONS
   ============================================================ */

CREATE TABLE event_registrations (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,

    event_id BIGINT NOT NULL,

    employee_id VARCHAR(50) NOT NULL,

    registered_at DATETIME2 NOT NULL
        CONSTRAINT df_event_registration_created_at
        DEFAULT SYSDATETIME(),

    CONSTRAINT fk_event_registration_event
        FOREIGN KEY (event_id)
        REFERENCES events(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_event_registration_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_number),

    CONSTRAINT unique_event_employee
        UNIQUE (event_id, employee_id)
);
GO


/* ============================================================
   TRAINING EMPLOYEE ASSIGNMENTS
   ============================================================ */

CREATE TABLE training_assignments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,

    training_id BIGINT NOT NULL,

    employee_id VARCHAR(50) NOT NULL,

    assigned_at DATETIME2 NOT NULL
        CONSTRAINT df_training_assignment_created_at
        DEFAULT SYSDATETIME(),

    CONSTRAINT fk_training_assignment_training
        FOREIGN KEY (training_id)
        REFERENCES training_programs(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_training_assignment_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_number),

    CONSTRAINT unique_training_employee_assignment
        UNIQUE (training_id, employee_id)
);
GO


/* ============================================================
   TRAINING REGISTRATIONS
   ============================================================ */

CREATE TABLE training_registrations (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,

    training_id BIGINT NOT NULL,

    employee_id VARCHAR(50) NOT NULL,

    registered_at DATETIME2 NOT NULL
        CONSTRAINT df_training_registration_created_at
        DEFAULT SYSDATETIME(),

    CONSTRAINT fk_training_registration_training
        FOREIGN KEY (training_id)
        REFERENCES training_programs(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_training_registration_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_number),

    CONSTRAINT unique_training_employee_registration
        UNIQUE (training_id, employee_id)
);
GO


/* ============================================================
   TRAINING ATTENDANCE
   ============================================================ */

CREATE TABLE training_attendance (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,

    training_id BIGINT NOT NULL,

    employee_id VARCHAR(50) NOT NULL,

    status VARCHAR(30) NOT NULL
        CONSTRAINT df_training_attendance_status
        DEFAULT 'Pending',

    marked_at DATETIME2 NOT NULL
        CONSTRAINT df_training_attendance_marked_at
        DEFAULT SYSDATETIME(),

    CONSTRAINT fk_training_attendance_training
        FOREIGN KEY (training_id)
        REFERENCES training_programs(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_training_attendance_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_number),

    CONSTRAINT chk_training_attendance_status
        CHECK (
            status IN (
                'Present',
                'Absent',
                'Pending'
            )
        ),

    CONSTRAINT unique_training_employee_attendance
        UNIQUE (training_id, employee_id)
);
GO


/* ============================================================
   TRAINING COMPLETION
   ============================================================ */

CREATE TABLE training_completion (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,

    training_id BIGINT NOT NULL,

    employee_id VARCHAR(50) NOT NULL,

    status VARCHAR(30) NOT NULL
        CONSTRAINT df_training_completion_status
        DEFAULT 'Pending',

    completed_at DATETIME2,

    CONSTRAINT fk_training_completion_training
        FOREIGN KEY (training_id)
        REFERENCES training_programs(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_training_completion_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_number),

    CONSTRAINT chk_training_completion_status
        CHECK (
            status IN (
                'Completed',
                'Not Completed',
                'Pending'
            )
        ),

    CONSTRAINT unique_training_employee_completion
        UNIQUE (training_id, employee_id)
);
GO


/* ============================================================
   EVENT INDEXES
   ============================================================ */

CREATE INDEX idx_events_date
    ON events(event_date);
GO

CREATE INDEX idx_events_status
    ON events(status);
GO

CREATE INDEX idx_events_category
    ON events(category);
GO

CREATE INDEX idx_event_registrations_event
    ON event_registrations(event_id);
GO

CREATE INDEX idx_event_registrations_employee
    ON event_registrations(employee_id);
GO


/* ============================================================
   TRAINING INDEXES
   ============================================================ */

CREATE INDEX idx_training_assignments_training
    ON training_assignments(training_id);
GO

CREATE INDEX idx_training_assignments_employee
    ON training_assignments(employee_id);
GO

CREATE INDEX idx_training_registrations_training
    ON training_registrations(training_id);
GO

CREATE INDEX idx_training_registrations_employee
    ON training_registrations(employee_id);
GO

CREATE INDEX idx_training_attendance_training
    ON training_attendance(training_id);
GO

CREATE INDEX idx_training_completion_training
    ON training_completion(training_id);
GO


/* ============================================================
   VERIFICATION
   ============================================================ */

SELECT
    TABLE_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;
GO



USE StaffHub;
GO

/* ============================================================
   ATTENDANCE MONITOR
   ============================================================ */

IF OBJECT_ID('dbo.attendance_monitor', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.attendance_monitor (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,

        activation_code VARCHAR(6) NOT NULL,

        active BIT NOT NULL DEFAULT 0,

        activation_type VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
        -- MANUAL / SCHEDULE

        activated_by VARCHAR(50) NULL,

        activated_at DATETIME2 NULL,
        deactivated_at DATETIME2 NULL,

        current_qr_token VARCHAR(100) NULL,
        qr_sequence INT NOT NULL DEFAULT 0,
        qr_created_at DATETIME2 NULL,
        qr_expires_at DATETIME2 NULL,

        created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END;
GO


/* ============================================================
   ATTENDANCE RECORD
   One row per employee per working day.
   ============================================================ */

IF OBJECT_ID('dbo.attendance_records', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.attendance_records (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,

        employee_id BIGINT NOT NULL,

        attendance_date DATE NOT NULL,

        check_in DATETIME2 NULL,
        check_out DATETIME2 NULL,

        status VARCHAR(30) NOT NULL DEFAULT 'PRESENT',
        -- PRESENT / LATE / HALF_DAY / ABSENT / ON_LEAVE / OPEN

        check_in_method VARCHAR(30) NULL,
        check_out_method VARCHAR(30) NULL,

        qr_session_id BIGINT NULL,

        manual_correction BIT NOT NULL DEFAULT 0,

        correction_reason VARCHAR(500) NULL,

        created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        updated_at DATETIME2 NULL,

        CONSTRAINT FK_attendance_employee
            FOREIGN KEY (employee_id)
            REFERENCES dbo.employees(id),

        CONSTRAINT UQ_attendance_employee_date
            UNIQUE (employee_id, attendance_date)
    );
END;
GO


/* ============================================================
   QR SCAN / AUDIT EVENTS
   ============================================================ */

IF OBJECT_ID('dbo.attendance_events', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.attendance_events (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,

        monitor_id BIGINT NULL,

        attendance_record_id BIGINT NULL,

        employee_id BIGINT NULL,

        action VARCHAR(30) NOT NULL,
        -- CHECK_IN / CHECK_OUT / QR_ROTATED /
        -- MONITOR_ACTIVATED / MONITOR_DEACTIVATED /
        -- SCHEDULE_ACTIVATED / SCHEDULE_DEACTIVATED /
        -- MANUAL_CORRECTION

        event_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

        qr_sequence INT NULL,

        performed_by VARCHAR(50) NULL,

        details VARCHAR(1000) NULL,

        CONSTRAINT FK_event_employee
            FOREIGN KEY (employee_id)
            REFERENCES dbo.employees(id)
    );
END;
GO


/* ============================================================
   MONITOR SCHEDULE
   ============================================================ */

IF OBJECT_ID('dbo.attendance_schedules', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.attendance_schedules (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,

        schedule_name VARCHAR(100) NOT NULL,

        day_of_week INT NOT NULL,
        -- 1 = Monday ... 7 = Sunday

        start_time TIME NOT NULL,

        end_time TIME NOT NULL,

        enabled BIT NOT NULL DEFAULT 1,

        created_by VARCHAR(50) NULL,

        created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

        updated_at DATETIME2 NULL
    );
END;
GO

USE StaffHub;
GO

/* ============================================================
   ATTENDANCE MONITOR SESSION HISTORY
   One row = one activated monitor session
   ============================================================ */

IF OBJECT_ID('dbo.attendance_monitor_sessions', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.attendance_monitor_sessions
    (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,

        monitor_id BIGINT NOT NULL,

        activation_type VARCHAR(20) NOT NULL,
        -- MANUAL / SCHEDULE

        activated_by VARCHAR(50) NULL,

        activated_at DATETIME2 NOT NULL
            DEFAULT SYSDATETIME(),

        deactivated_by VARCHAR(50) NULL,

        deactivated_at DATETIME2 NULL,

        deactivation_type VARCHAR(20) NULL,
        -- MANUAL / SCHEDULE

        CONSTRAINT FK_monitor_session_monitor
            FOREIGN KEY (monitor_id)
            REFERENCES dbo.attendance_monitor(id)
    );
END;
GO


/* ============================================================
   ADD SESSION FK TO ATTENDANCE RECORDS
   ============================================================ */

IF NOT EXISTS
(
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'FK_attendance_session'
)
BEGIN
    ALTER TABLE dbo.attendance_records
    ADD CONSTRAINT FK_attendance_session
        FOREIGN KEY (qr_session_id)
        REFERENCES dbo.attendance_monitor_sessions(id);
END;
GO


/* ============================================================
   AUDIT INDEXES
   ============================================================ */

IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_attendance_events_event_time'
      AND object_id = OBJECT_ID('dbo.attendance_events')
)
BEGIN
    CREATE INDEX IX_attendance_events_event_time
        ON dbo.attendance_events(event_time DESC);
END;
GO


IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_attendance_records_date'
      AND object_id = OBJECT_ID('dbo.attendance_records')
)
BEGIN
    CREATE INDEX IX_attendance_records_date
        ON dbo.attendance_records(attendance_date);
END;
GO

-- ============================================================
-- StaffHub Attendance Schedule Migration
-- Old schema -> Current backend schema
-- ============================================================

-- 1. Add schedule_type
IF COL_LENGTH('attendance_schedules', 'schedule_type') IS NULL
BEGIN
    ALTER TABLE attendance_schedules
    ADD schedule_type VARCHAR(20) NULL;
END;
GO

-- 2. Add schedule_date
IF COL_LENGTH('attendance_schedules', 'schedule_date') IS NULL
BEGIN
    ALTER TABLE attendance_schedules
    ADD schedule_date DATE NULL;
END;
GO


-- ============================================================
-- 3. Add temporary column for the old INT day_of_week
-- ============================================================

IF COL_LENGTH('attendance_schedules', 'day_of_week_old') IS NULL
BEGIN
    ALTER TABLE attendance_schedules
    ADD day_of_week_old INT NULL;
END;
GO


-- ============================================================
-- 4. Copy existing INT day values
-- ============================================================

UPDATE attendance_schedules
SET day_of_week_old = day_of_week
WHERE day_of_week_old IS NULL;
GO


-- ============================================================
-- 5. Remove old INT day_of_week column
-- ============================================================

ALTER TABLE attendance_schedules
DROP COLUMN day_of_week;
GO


-- ============================================================
-- 6. Create new VARCHAR day_of_week column
-- ============================================================

ALTER TABLE attendance_schedules
ADD day_of_week VARCHAR(20) NULL;
GO


-- ============================================================
-- 7. Convert old numbers to day names
--
-- 1 = Monday
-- 2 = Tuesday
-- 3 = Wednesday
-- 4 = Thursday
-- 5 = Friday
-- 6 = Saturday
-- 7 = Sunday
-- ============================================================

UPDATE attendance_schedules
SET day_of_week =
    CASE day_of_week_old
        WHEN 1 THEN 'MONDAY'
        WHEN 2 THEN 'TUESDAY'
        WHEN 3 THEN 'WEDNESDAY'
        WHEN 4 THEN 'THURSDAY'
        WHEN 5 THEN 'FRIDAY'
        WHEN 6 THEN 'SATURDAY'
        WHEN 7 THEN 'SUNDAY'
        ELSE NULL
    END;
GO


-- ============================================================
-- 8. Remove temporary column
-- ============================================================

ALTER TABLE attendance_schedules
DROP COLUMN day_of_week_old;
GO


-- ============================================================
-- 9. Set schedule type for existing records
--
-- Existing records with a day_of_week
-- become WEEKLY.
--
-- Records without a day become DAILY.
-- ============================================================

UPDATE attendance_schedules
SET schedule_type =
    CASE
        WHEN day_of_week IS NOT NULL
             THEN 'WEEKLY'
        ELSE 'DAILY'
    END
WHERE schedule_type IS NULL;
GO


-- ============================================================
-- 10. Verify final structure
-- ============================================================

SELECT
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'attendance_schedules'
ORDER BY ORDINAL_POSITION;
GO


-- ============================================================
-- 11. Verify existing data
-- ============================================================

SELECT *
FROM attendance_schedules
ORDER BY id;
GO

CREATE TABLE staffhub_auth_users (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    enabled BIT NOT NULL CONSTRAINT DF_staffhub_auth_users_enabled DEFAULT 1,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_staffhub_auth_users_created_at DEFAULT GETDATE(),
    updated_at DATETIME2 NULL,

    CONSTRAINT UQ_staffhub_auth_users_employee UNIQUE (employee_id),
    CONSTRAINT UQ_staffhub_auth_users_email UNIQUE (email),

    CONSTRAINT FK_staffhub_auth_users_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(id)
);