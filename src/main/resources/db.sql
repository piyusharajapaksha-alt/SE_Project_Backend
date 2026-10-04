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

USE StaffHub;
GO

IF OBJECT_ID('dbo.companies', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.companies (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        company_code VARCHAR(50) NOT NULL,
        company_name VARCHAR(200) NOT NULL,
        email VARCHAR(150) NOT NULL,
        phone VARCHAR(30) NULL,
        address VARCHAR(MAX) NULL,
        industry VARCHAR(100) NULL,
        status VARCHAR(30) NOT NULL CONSTRAINT DF_companies_status DEFAULT 'Active',
        owner_employee_id BIGINT NOT NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_companies_created_at DEFAULT SYSDATETIME(),
        updated_at DATETIME2 NULL,

        CONSTRAINT UQ_companies_company_code UNIQUE (company_code),
        CONSTRAINT UQ_companies_email UNIQUE (email),
        CONSTRAINT UQ_companies_owner UNIQUE (owner_employee_id),
        CONSTRAINT FK_companies_owner
            FOREIGN KEY (owner_employee_id) REFERENCES dbo.employees(id)
    );
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_companies_owner_employee_id'
      AND object_id = OBJECT_ID('dbo.companies')
)
BEGIN
    CREATE INDEX IX_companies_owner_employee_id
        ON dbo.companies(owner_employee_id);
END;
GO

USE StaffHub;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

PRINT '============================================';
PRINT 'STAFFHUB OWNER SEPARATION REPAIR';
PRINT '============================================';


/* =========================================================
   STEP 1
   Make employee_id nullable in authentication table.
   Owners will have:
       employee_id = NULL
       owner_id     = actual owner ID
   Employees will have:
       employee_id = actual employee ID
       owner_id     = NULL
   ========================================================= */

PRINT '';
PRINT 'STEP 1: Making staffhub_auth_users.employee_id nullable...';

IF EXISTS
(
    SELECT 1
    FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.staffhub_auth_users')
      AND name = 'employee_id'
      AND is_nullable = 0
)
BEGIN
    ALTER TABLE dbo.staffhub_auth_users
    ALTER COLUMN employee_id BIGINT NULL;

    PRINT 'employee_id is now nullable.';
END
ELSE
BEGIN
    PRINT 'employee_id is already nullable.';
END;
GO


/* =========================================================
   STEP 2
   Fix the existing Owner authentication account.
   Owner must NOT reference an employee.
   ========================================================= */

PRINT '';
PRINT 'STEP 2: Fixing Owner authentication account...';

UPDATE a
SET a.employee_id = NULL
FROM dbo.staffhub_auth_users a
WHERE a.owner_id IS NOT NULL;

PRINT 'Owner authentication account updated.';
GO


/* =========================================================
   STEP 3
   Verify owner authentication records.
   ========================================================= */

PRINT '';
PRINT 'STEP 3: Checking authentication records...';

SELECT
    id,
    email,
    employee_id,
    owner_id,
    enabled
FROM dbo.staffhub_auth_users
ORDER BY id;
GO


/* =========================================================
   STEP 4
   Drop foreign keys referencing companies.owner_employee_id.
   We detect them automatically instead of assuming a name.
   ========================================================= */

PRINT '';
PRINT 'STEP 4: Removing foreign keys referencing owner_employee_id...';

DECLARE @sql NVARCHAR(MAX) = N'';

SELECT @sql = @sql +
    N'ALTER TABLE '
    + QUOTENAME(OBJECT_SCHEMA_NAME(fk.parent_object_id))
    + N'.'
    + QUOTENAME(OBJECT_NAME(fk.parent_object_id))
    + N' DROP CONSTRAINT '
    + QUOTENAME(fk.name)
    + N';'
    + CHAR(13) + CHAR(10)
FROM sys.foreign_keys fk
INNER JOIN sys.foreign_key_columns fkc
    ON fk.object_id = fkc.constraint_object_id
INNER JOIN sys.columns c
    ON c.object_id = fkc.parent_object_id
   AND c.column_id = fkc.parent_column_id
WHERE fk.parent_object_id = OBJECT_ID('dbo.companies')
  AND c.name = 'owner_employee_id';

IF @sql <> N''
BEGIN
    EXEC sys.sp_executesql @sql;
    PRINT 'Old owner_employee_id foreign key(s) removed.';
END
ELSE
BEGIN
    PRINT 'No foreign key found for owner_employee_id.';
END;
GO


/* =========================================================
   STEP 5
   Drop old unique constraint UQ_companies_owner if it exists.
   ========================================================= */

PRINT '';
PRINT 'STEP 5: Removing old UQ_companies_owner...';

IF EXISTS
(
    SELECT 1
    FROM sys.key_constraints
    WHERE parent_object_id = OBJECT_ID('dbo.companies')
      AND name = 'UQ_companies_owner'
)
BEGIN
    ALTER TABLE dbo.companies
    DROP CONSTRAINT UQ_companies_owner;

    PRINT 'UQ_companies_owner constraint dropped.';
END
ELSE
BEGIN
    PRINT 'UQ_companies_owner constraint does not exist.';
END;
GO


/* =========================================================
   STEP 6
   Drop old index IX_companies_owner_employee_id.
   ========================================================= */

PRINT '';
PRINT 'STEP 6: Removing old owner_employee_id index...';

IF EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE object_id = OBJECT_ID('dbo.companies')
      AND name = 'IX_companies_owner_employee_id'
)
BEGIN
    DROP INDEX IX_companies_owner_employee_id
    ON dbo.companies;

    PRINT 'IX_companies_owner_employee_id dropped.';
END
ELSE
BEGIN
    PRINT 'IX_companies_owner_employee_id does not exist.';
END;
GO


/* =========================================================
   STEP 7
   Drop old owner_employee_id column.
   ========================================================= */

PRINT '';
PRINT 'STEP 7: Removing companies.owner_employee_id...';

IF EXISTS
(
    SELECT 1
    FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.companies')
      AND name = 'owner_employee_id'
)
BEGIN
    ALTER TABLE dbo.companies
    DROP COLUMN owner_employee_id;

    PRINT 'companies.owner_employee_id removed.';
END
ELSE
BEGIN
    PRINT 'owner_employee_id column already removed.';
END;
GO


/* =========================================================
   STEP 8
   Make sure companies.owner_id exists.
   ========================================================= */

PRINT '';
PRINT 'STEP 8: Checking companies.owner_id...';

IF NOT EXISTS
(
    SELECT 1
    FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.companies')
      AND name = 'owner_id'
)
BEGIN
    ALTER TABLE dbo.companies
    ADD owner_id BIGINT NULL;

    PRINT 'companies.owner_id created.';
END
ELSE
BEGIN
    PRINT 'companies.owner_id already exists.';
END;
GO


/* =========================================================
   STEP 9
   Make sure the existing company points to the owner.
   ========================================================= */

PRINT '';
PRINT 'STEP 9: Checking company owner relationship...';

SELECT
    id,
    company_name,
    owner_id
FROM dbo.companies;
GO


/* =========================================================
   STEP 10
   Create FK companies.owner_id -> company_owners.id
   if it does not already exist.
   ========================================================= */

PRINT '';
PRINT 'STEP 10: Creating company owner foreign key...';

IF NOT EXISTS
(
    SELECT 1
    FROM sys.foreign_keys
    WHERE parent_object_id = OBJECT_ID('dbo.companies')
      AND name = 'FK_companies_owner'
)
BEGIN
    ALTER TABLE dbo.companies
    ADD CONSTRAINT FK_companies_owner
        FOREIGN KEY (owner_id)
        REFERENCES dbo.company_owners(id);

    PRINT 'FK_companies_owner created.';
END
ELSE
BEGIN
    PRINT 'FK_companies_owner already exists.';
END;
GO


/* =========================================================
   STEP 11
   Make sure one company cannot have duplicate owner.
   ========================================================= */

PRINT '';
PRINT 'STEP 11: Creating owner unique index...';

IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE object_id = OBJECT_ID('dbo.companies')
      AND name = 'UX_companies_owner_id'
)
BEGIN
    CREATE UNIQUE INDEX UX_companies_owner_id
    ON dbo.companies(owner_id)
    WHERE owner_id IS NOT NULL;

    PRINT 'UX_companies_owner_id created.';
END
ELSE
BEGIN
    PRINT 'UX_companies_owner_id already exists.';
END;
GO


/* =========================================================
   STEP 12
   Make sure owner_id foreign key exists in auth table.
   ========================================================= */

PRINT '';
PRINT 'STEP 12: Checking authentication owner foreign key...';

IF NOT EXISTS
(
    SELECT 1
    FROM sys.foreign_keys
    WHERE parent_object_id = OBJECT_ID('dbo.staffhub_auth_users')
      AND name = 'FK_staffhub_auth_users_owner'
)
BEGIN
    ALTER TABLE dbo.staffhub_auth_users
    ADD CONSTRAINT FK_staffhub_auth_users_owner
        FOREIGN KEY (owner_id)
        REFERENCES dbo.company_owners(id);

    PRINT 'FK_staffhub_auth_users_owner created.';
END
ELSE
BEGIN
    PRINT 'FK_staffhub_auth_users_owner already exists.';
END;
GO


/* =========================================================
   STEP 13
   Make sure only ONE account type can be used.
   Employee:
       employee_id != NULL
       owner_id = NULL

   Owner:
       employee_id = NULL
       owner_id != NULL
   ========================================================= */

PRINT '';
PRINT 'STEP 13: Creating account type validation...';

IF NOT EXISTS
(
    SELECT 1
    FROM sys.check_constraints
    WHERE parent_object_id = OBJECT_ID('dbo.staffhub_auth_users')
      AND name = 'CK_staffhub_auth_users_one_principal'
)
BEGIN
    ALTER TABLE dbo.staffhub_auth_users
    ADD CONSTRAINT CK_staffhub_auth_users_one_principal
    CHECK
    (
        (employee_id IS NOT NULL AND owner_id IS NULL)
        OR
        (employee_id IS NULL AND owner_id IS NOT NULL)
    );

    PRINT 'Account type check constraint created.';
END
ELSE
BEGIN
    PRINT 'Account type check constraint already exists.';
END;
GO


/* =========================================================
   STEP 14
   Ensure one owner has only one authentication account.
   ========================================================= */

PRINT '';
PRINT 'STEP 14: Creating unique owner authentication index...';

IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE object_id = OBJECT_ID('dbo.staffhub_auth_users')
      AND name = 'UX_staffhub_auth_users_owner_id'
)
BEGIN
    CREATE UNIQUE INDEX UX_staffhub_auth_users_owner_id
    ON dbo.staffhub_auth_users(owner_id)
    WHERE owner_id IS NOT NULL;

    PRINT 'UX_staffhub_auth_users_owner_id created.';
END
ELSE
BEGIN
    PRINT 'UX_staffhub_auth_users_owner_id already exists.';
END;
GO


/* =========================================================
   FINAL VALIDATION
   ========================================================= */

PRINT '';
PRINT '============================================';
PRINT 'FINAL OWNER SEPARATION CHECK';
PRINT '============================================';

PRINT '';
PRINT '1. Authentication accounts:';

SELECT
    id,
    email,
    employee_id,
    owner_id,
    enabled
FROM dbo.staffhub_auth_users
ORDER BY id;


PRINT '';
PRINT '2. Company owners:';

SELECT
    id,
    first_name,
    last_name,
    email,
    phone
FROM dbo.company_owners
ORDER BY id;


PRINT '';
PRINT '3. Companies:';

SELECT
    id,
    company_name,
    owner_id
FROM dbo.companies
ORDER BY id;


PRINT '';
PRINT '4. Checking old owner_employee_id column...';

IF EXISTS
(
    SELECT 1
    FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.companies')
      AND name = 'owner_employee_id'
)
BEGIN
    THROW 50001, 'ERROR: owner_employee_id still exists.', 1;
END
ELSE
BEGIN
    PRINT 'OK: owner_employee_id has been removed.';
END;


PRINT '';
PRINT '5. Checking Owner authentication account...';

IF EXISTS
(
    SELECT 1
    FROM dbo.staffhub_auth_users
    WHERE owner_id IS NOT NULL
      AND employee_id IS NOT NULL
)
BEGIN
    THROW 50002, 'ERROR: Owner authentication account still references an employee.', 1;
END
ELSE
BEGIN
    PRINT 'OK: Owner authentication account does not reference an employee.';
END;


PRINT '';
PRINT '============================================';
PRINT 'OWNER SEPARATION REPAIR COMPLETED';
PRINT '============================================';
GO


USE StaffHub;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

PRINT '============================================================';
PRINT ' STAFFHUB MULTI-COMPANY ISOLATION MIGRATION - CORRECTED';
PRINT ' SQL SERVER / SSMS';
PRINT '============================================================';
PRINT '';

BEGIN TRY

    BEGIN TRANSACTION;

    /* ========================================================
       0. BASIC VALIDATION
       ======================================================== */

    IF OBJECT_ID('dbo.companies', 'U') IS NULL
        THROW 50001, 'dbo.companies does not exist.', 1;

    IF OBJECT_ID('dbo.employees', 'U') IS NULL
        THROW 50002, 'dbo.employees does not exist.', 1;

    IF OBJECT_ID('dbo.staffhub_auth_users', 'U') IS NULL
        THROW 50003, 'dbo.staffhub_auth_users does not exist.', 1;


    /* ========================================================
       1. ENSURE companies.owner_id EXISTS

       IMPORTANT:
       We do NOT use owner_employee_id.
       ======================================================== */

    IF COL_LENGTH('dbo.companies', 'owner_id') IS NULL
    BEGIN
        ALTER TABLE dbo.companies
        ADD owner_id BIGINT NULL;

        PRINT 'Added companies.owner_id';
    END
    ELSE
    BEGIN
        PRINT 'companies.owner_id already exists';
    END;


    /* ========================================================
       2. ENSURE company_owners EXISTS

       Only create it if the current database genuinely does
       not contain it.
       ======================================================== */

    IF OBJECT_ID('dbo.company_owners', 'U') IS NULL
    BEGIN

        CREATE TABLE dbo.company_owners
        (
            id BIGINT IDENTITY(1,1) NOT NULL
                CONSTRAINT PK_company_owners PRIMARY KEY,

            first_name VARCHAR(100) NOT NULL,

            last_name VARCHAR(100) NOT NULL,

            email VARCHAR(150) NOT NULL,

            phone VARCHAR(30) NULL,

            created_at DATETIME2 NOT NULL
                CONSTRAINT DF_company_owners_created_at
                DEFAULT SYSDATETIME(),

            updated_at DATETIME2 NULL,

            CONSTRAINT UQ_company_owners_email
                UNIQUE (email)
        );

        PRINT 'Created dbo.company_owners';

    END
    ELSE
    BEGIN
        PRINT 'dbo.company_owners already exists';
    END;


    /* ========================================================
       3. ENSURE staffhub_auth_users.owner_id EXISTS
       ======================================================== */

    IF COL_LENGTH('dbo.staffhub_auth_users', 'owner_id') IS NULL
    BEGIN

        ALTER TABLE dbo.staffhub_auth_users
        ADD owner_id BIGINT NULL;

        PRINT 'Added staffhub_auth_users.owner_id';

    END
    ELSE
    BEGIN
        PRINT 'staffhub_auth_users.owner_id already exists';
    END;


    /* ========================================================
       4. MAKE employee_id NULLABLE

       Owner accounts:
           employee_id = NULL
           owner_id    = owner

       Employee accounts:
           employee_id = employee
           owner_id    = NULL
       ======================================================== */

    IF EXISTS
    (
        SELECT 1
        FROM sys.columns
        WHERE object_id = OBJECT_ID('dbo.staffhub_auth_users')
          AND name = 'employee_id'
          AND is_nullable = 0
    )
    BEGIN

        ALTER TABLE dbo.staffhub_auth_users
        ALTER COLUMN employee_id BIGINT NULL;

        PRINT 'Made staffhub_auth_users.employee_id nullable';

    END;


    /* ========================================================
       5. ADD employees.company_id
       ======================================================== */

    IF COL_LENGTH('dbo.employees', 'company_id') IS NULL
    BEGIN

        ALTER TABLE dbo.employees
        ADD company_id BIGINT NULL;

        PRINT 'Added employees.company_id';

    END
    ELSE
    BEGIN
        PRINT 'employees.company_id already exists';
    END;


    /* ========================================================
       6. VERIFY EXISTING COMPANY OWNERSHIP

       We intentionally DO NOT use owner_employee_id.

       If companies.owner_id already contains valid owners,
       leave it untouched.
       ======================================================== */

    IF EXISTS
    (
        SELECT 1
        FROM dbo.companies c
        WHERE c.owner_id IS NOT NULL
          AND NOT EXISTS
          (
              SELECT 1
              FROM dbo.company_owners o
              WHERE o.id = c.owner_id
          )
    )
    BEGIN

        THROW 50004,
        'companies contains owner_id values that do not exist in company_owners. Fix those owner records before continuing.',
        1;

    END;


    /* ========================================================
       7. VERIFY AUTH OWNER REFERENCES
       ======================================================== */

    IF EXISTS
    (
        SELECT 1
        FROM dbo.staffhub_auth_users a
        WHERE a.owner_id IS NOT NULL
          AND NOT EXISTS
          (
              SELECT 1
              FROM dbo.company_owners o
              WHERE o.id = a.owner_id
          )
    )
    BEGIN

        THROW 50005,
        'staffhub_auth_users contains invalid owner_id values.',
        1;

    END;


    /* ========================================================
       8. CREATE companies -> company_owners FK
       ======================================================== */

    IF NOT EXISTS
    (
        SELECT 1
        FROM sys.foreign_keys
        WHERE parent_object_id = OBJECT_ID('dbo.companies')
          AND referenced_object_id = OBJECT_ID('dbo.company_owners')
    )
    BEGIN

        ALTER TABLE dbo.companies
        ADD CONSTRAINT FK_companies_owner
            FOREIGN KEY (owner_id)
            REFERENCES dbo.company_owners(id);

        PRINT 'Created companies.owner_id foreign key';

    END;


    /* ========================================================
       9. CREATE auth -> owner FK
       ======================================================== */

    IF NOT EXISTS
    (
        SELECT 1
        FROM sys.foreign_keys
        WHERE parent_object_id =
              OBJECT_ID('dbo.staffhub_auth_users')
          AND referenced_object_id =
              OBJECT_ID('dbo.company_owners')
          AND EXISTS
          (
              SELECT 1
              FROM sys.foreign_key_columns fkc
              INNER JOIN sys.columns pc
                  ON pc.object_id = fkc.parent_object_id
                 AND pc.column_id = fkc.parent_column_id
              WHERE fkc.constraint_object_id =
                    sys.foreign_keys.object_id
                AND pc.name = 'owner_id'
          )
    )
    BEGIN

        ALTER TABLE dbo.staffhub_auth_users
        ADD CONSTRAINT FK_staffhub_auth_users_owner
            FOREIGN KEY (owner_id)
            REFERENCES dbo.company_owners(id);

        PRINT 'Created auth owner foreign key';

    END;


    /* ========================================================
       10. CREATE account principal validation
       ======================================================== */

    IF NOT EXISTS
    (
        SELECT 1
        FROM sys.check_constraints
        WHERE parent_object_id =
              OBJECT_ID('dbo.staffhub_auth_users')
          AND name =
              'CK_staffhub_auth_users_one_principal'
    )
    BEGIN

        ALTER TABLE dbo.staffhub_auth_users
        ADD CONSTRAINT CK_staffhub_auth_users_one_principal
        CHECK
        (
            (
                employee_id IS NOT NULL
                AND owner_id IS NULL
            )
            OR
            (
                employee_id IS NULL
                AND owner_id IS NOT NULL
            )
        );

        PRINT 'Created auth principal validation';

    END;


    /* ========================================================
       11. CREATE COMPANY INDEX ON EMPLOYEES
       ======================================================== */

    IF NOT EXISTS
    (
        SELECT 1
        FROM sys.indexes
        WHERE object_id = OBJECT_ID('dbo.employees')
          AND name = 'IX_employees_company_id'
    )
    BEGIN

        CREATE INDEX IX_employees_company_id
        ON dbo.employees(company_id);

        PRINT 'Created IX_employees_company_id';

    END;


    /* ========================================================
       12. BACKFILL EMPLOYEE COMPANY IDs

       We ONLY use information that already exists.

       Rule:
       If an employee's authentication account somehow already
       identifies an owner/company relationship, use it.

       We never guess between multiple companies.
       ======================================================== */

    /*
       Employees whose auth account has employee_id and whose
       owner_id is also populated are inconsistent.
    */

    IF EXISTS
    (
        SELECT 1
        FROM dbo.staffhub_auth_users
        WHERE employee_id IS NOT NULL
          AND owner_id IS NOT NULL
    )
    BEGIN

        THROW 50006,
        'Invalid auth data: an account has both employee_id and owner_id.',
        1;

    END;


    /* ========================================================
       13. COMPANY-SCOPED TABLES

       Add company_id to tables that do not already have it.
       ======================================================== */

    IF COL_LENGTH('dbo.training_programs', 'company_id') IS NULL
    BEGIN
        ALTER TABLE dbo.training_programs
        ADD company_id BIGINT NULL;

        PRINT 'Added training_programs.company_id';
    END;


    IF COL_LENGTH('dbo.grievances', 'company_id') IS NULL
    BEGIN
        ALTER TABLE dbo.grievances
        ADD company_id BIGINT NULL;

        PRINT 'Added grievances.company_id';
    END;


    IF COL_LENGTH('dbo.performance_reviews', 'company_id') IS NULL
    BEGIN
        ALTER TABLE dbo.performance_reviews
        ADD company_id BIGINT NULL;

        PRINT 'Added performance_reviews.company_id';
    END;


    IF COL_LENGTH('dbo.leave_requests', 'company_id') IS NULL
    BEGIN
        ALTER TABLE dbo.leave_requests
        ADD company_id BIGINT NULL;

        PRINT 'Added leave_requests.company_id';
    END;


    IF COL_LENGTH('dbo.events', 'company_id') IS NULL
    BEGIN
        ALTER TABLE dbo.events
        ADD company_id BIGINT NULL;

        PRINT 'Added events.company_id';
    END;


    IF COL_LENGTH('dbo.event_registrations', 'company_id') IS NULL
    BEGIN
        ALTER TABLE dbo.event_registrations
        ADD company_id BIGINT NULL;

        PRINT 'Added event_registrations.company_id';
    END;


    IF COL_LENGTH('dbo.grievance_responses', 'company_id') IS NULL
    BEGIN
        ALTER TABLE dbo.grievance_responses
        ADD company_id BIGINT NULL;

        PRINT 'Added grievance_responses.company_id';
    END;


    IF COL_LENGTH('dbo.training_assignments', 'company_id') IS NULL
    BEGIN
        ALTER TABLE dbo.training_assignments
        ADD company_id BIGINT NULL;

        PRINT 'Added training_assignments.company_id';
    END;


    IF COL_LENGTH('dbo.training_registrations', 'company_id') IS NULL
    BEGIN
        ALTER TABLE dbo.training_registrations
        ADD company_id BIGINT NULL;

        PRINT 'Added training_registrations.company_id';
    END;


    IF COL_LENGTH('dbo.training_attendance', 'company_id') IS NULL
    BEGIN
        ALTER TABLE dbo.training_attendance
        ADD company_id BIGINT NULL;

        PRINT 'Added training_attendance.company_id';
    END;


    IF COL_LENGTH('dbo.training_completion', 'company_id') IS NULL
    BEGIN
        ALTER TABLE dbo.training_completion
        ADD company_id BIGINT NULL;

        PRINT 'Added training_completion.company_id';
    END;


    IF COL_LENGTH('dbo.attendance_records', 'company_id') IS NULL
    BEGIN
        ALTER TABLE dbo.attendance_records
        ADD company_id BIGINT NULL;

        PRINT 'Added attendance_records.company_id';
    END;


    IF COL_LENGTH('dbo.attendance_events', 'company_id') IS NULL
    BEGIN
        ALTER TABLE dbo.attendance_events
        ADD company_id BIGINT NULL;

        PRINT 'Added attendance_events.company_id';
    END;


    IF COL_LENGTH('dbo.attendance_monitor', 'company_id') IS NULL
    BEGIN
        ALTER TABLE dbo.attendance_monitor
        ADD company_id BIGINT NULL;

        PRINT 'Added attendance_monitor.company_id';
    END;


    IF COL_LENGTH('dbo.attendance_schedules', 'company_id') IS NULL
    BEGIN
        ALTER TABLE dbo.attendance_schedules
        ADD company_id BIGINT NULL;

        PRINT 'Added attendance_schedules.company_id';
    END;


    IF COL_LENGTH('dbo.attendance_monitor_sessions', 'company_id') IS NULL
    BEGIN
        ALTER TABLE dbo.attendance_monitor_sessions
        ADD company_id BIGINT NULL;

        PRINT 'Added attendance_monitor_sessions.company_id';
    END;


    /* ========================================================
       14. SAFE BACKFILL THROUGH EMPLOYEE RELATIONSHIPS
       ======================================================== */

    /* ---------- LEAVE ---------- */

    UPDATE l
    SET l.company_id = e.company_id
    FROM dbo.leave_requests l
    INNER JOIN dbo.employees e
        ON e.employee_number = l.employee_id
    WHERE l.company_id IS NULL
      AND e.company_id IS NOT NULL;


    /* ---------- PERFORMANCE ---------- */

    UPDATE p
    SET p.company_id = e.company_id
    FROM dbo.performance_reviews p
    INNER JOIN dbo.employees e
        ON e.employee_number = p.employee_id
    WHERE p.company_id IS NULL
      AND e.company_id IS NOT NULL;


    /* ---------- GRIEVANCE ---------- */

    UPDATE g
    SET g.company_id = e.company_id
    FROM dbo.grievances g
    INNER JOIN dbo.employees e
        ON e.employee_number = g.employee_id
    WHERE g.company_id IS NULL
      AND e.company_id IS NOT NULL;


    /* ---------- GRIEVANCE RESPONSES ---------- */

    UPDATE r
    SET r.company_id = g.company_id
    FROM dbo.grievance_responses r
    INNER JOIN dbo.grievances g
        ON g.id = r.grievance_id
    WHERE r.company_id IS NULL
      AND g.company_id IS NOT NULL;


    /* ---------- EVENTS ---------- */

    UPDATE ev
    SET ev.company_id = e.company_id
    FROM dbo.events ev
    INNER JOIN dbo.employees e
        ON e.employee_number = ev.organizer_id
    WHERE ev.company_id IS NULL
      AND e.company_id IS NOT NULL;


    /* ---------- EVENT REGISTRATIONS ---------- */

    UPDATE er
    SET er.company_id = ev.company_id
    FROM dbo.event_registrations er
    INNER JOIN dbo.events ev
        ON ev.id = er.event_id
    WHERE er.company_id IS NULL
      AND ev.company_id IS NOT NULL;


    /* ---------- TRAINING ASSIGNMENTS ---------- */

    UPDATE ta
    SET ta.company_id = e.company_id
    FROM dbo.training_assignments ta
    INNER JOIN dbo.employees e
        ON e.employee_number = ta.employee_id
    WHERE ta.company_id IS NULL
      AND e.company_id IS NOT NULL;


    /*
       Training itself has no employee_id.

       Therefore we derive its company only when ALL of its
       existing assignments belong to the SAME company.
    */

    UPDATE t
    SET t.company_id = x.company_id
    FROM dbo.training_programs t
    INNER JOIN
    (
        SELECT
            ta.training_id,
            MIN(e.company_id) AS company_id
        FROM dbo.training_assignments ta
        INNER JOIN dbo.employees e
            ON e.employee_number = ta.employee_id
        WHERE e.company_id IS NOT NULL
        GROUP BY ta.training_id
        HAVING COUNT(DISTINCT e.company_id) = 1
    ) x
        ON x.training_id = t.id
    WHERE t.company_id IS NULL;


    /* ---------- TRAINING REGISTRATIONS ---------- */

    UPDATE tr
    SET tr.company_id = t.company_id
    FROM dbo.training_registrations tr
    INNER JOIN dbo.training_programs t
        ON t.id = tr.training_id
    WHERE tr.company_id IS NULL
      AND t.company_id IS NOT NULL;


    /* ---------- TRAINING ATTENDANCE ---------- */

    UPDATE ta
    SET ta.company_id = t.company_id
    FROM dbo.training_attendance ta
    INNER JOIN dbo.training_programs t
        ON t.id = ta.training_id
    WHERE ta.company_id IS NULL
      AND t.company_id IS NOT NULL;


    /* ---------- TRAINING COMPLETION ---------- */

    UPDATE tc
    SET tc.company_id = t.company_id
    FROM dbo.training_completion tc
    INNER JOIN dbo.training_programs t
        ON t.id = tc.training_id
    WHERE tc.company_id IS NULL
      AND t.company_id IS NOT NULL;


    /* ---------- ATTENDANCE ---------- */

    UPDATE ar
    SET ar.company_id = e.company_id
    FROM dbo.attendance_records ar
    INNER JOIN dbo.employees e
        ON e.id = ar.employee_id
    WHERE ar.company_id IS NULL
      AND e.company_id IS NOT NULL;


    /* ---------- ATTENDANCE EVENTS ---------- */

    UPDATE ae
    SET ae.company_id = e.company_id
    FROM dbo.attendance_events ae
    INNER JOIN dbo.employees e
        ON e.id = ae.employee_id
    WHERE ae.company_id IS NULL
      AND e.company_id IS NOT NULL;


    /* ---------- MONITOR ---------- */

    /*
       A monitor has no employee relationship.

       If there is exactly ONE company, it is safe to assign it.
       If there are multiple companies, we refuse to guess.
    */

    IF
    (
        SELECT COUNT(*)
        FROM dbo.companies
    ) = 1
    BEGIN

        UPDATE am
        SET am.company_id =
        (
            SELECT TOP 1 id
            FROM dbo.companies
        )
        WHERE am.company_id IS NULL;

        UPDATE ass
        SET ass.company_id =
        (
            SELECT TOP 1 id
            FROM dbo.companies
        )
        WHERE ass.company_id IS NULL;

        UPDATE ams
        SET ams.company_id =
        (
            SELECT TOP 1 id
            FROM dbo.companies
        )
        WHERE ams.company_id IS NULL;

    END;


    /* ========================================================
       15. CHECK FOR UNRESOLVED EMPLOYEE OWNERSHIP
       ======================================================== */

    IF EXISTS
    (
        SELECT 1
        FROM dbo.employees
        WHERE company_id IS NULL
    )
    BEGIN

        PRINT '';
        PRINT '============================================================';
        PRINT 'WARNING: SOME EMPLOYEES HAVE NO COMPANY';
        PRINT '============================================================';

        SELECT
            id,
            employee_number,
            first_name,
            last_name,
            email
        FROM dbo.employees
        WHERE company_id IS NULL
        ORDER BY id;

        THROW 50007,
        'Migration stopped: one or more employees have no company_id. Assign those employees to the correct company before rerunning.',
        1;

    END;


    /* ========================================================
       16. CHECK FOR CROSS-COMPANY EVENT REGISTRATIONS
       ======================================================== */

    IF EXISTS
    (
        SELECT 1
        FROM dbo.event_registrations er
        INNER JOIN dbo.events ev
            ON ev.id = er.event_id
        INNER JOIN dbo.employees e
            ON e.employee_number = er.employee_id
        WHERE ev.company_id IS NOT NULL
          AND e.company_id IS NOT NULL
          AND ev.company_id <> e.company_id
    )
    BEGIN

        THROW 50008,
        'Cross-company event registration detected. Existing data must be corrected before isolation can be enforced.',
        1;

    END;


    /* ========================================================
       17. CHECK TRAINING CROSS-COMPANY ASSIGNMENTS
       ======================================================== */

    IF EXISTS
    (
        SELECT 1
        FROM dbo.training_assignments ta
        INNER JOIN dbo.training_programs t
            ON t.id = ta.training_id
        INNER JOIN dbo.employees e
            ON e.employee_number = ta.employee_id
        WHERE t.company_id IS NOT NULL
          AND e.company_id IS NOT NULL
          AND t.company_id <> e.company_id
    )
    BEGIN

        THROW 50009,
        'Cross-company training assignment detected. Existing data must be corrected before isolation can be enforced.',
        1;

    END;


    /* ========================================================
       18. INDEXES
       ======================================================== */

    IF NOT EXISTS
    (
        SELECT 1 FROM sys.indexes
        WHERE object_id = OBJECT_ID('dbo.training_programs')
          AND name = 'IX_training_programs_company_id'
    )
        CREATE INDEX IX_training_programs_company_id
        ON dbo.training_programs(company_id);


    IF NOT EXISTS
    (
        SELECT 1 FROM sys.indexes
        WHERE object_id = OBJECT_ID('dbo.events')
          AND name = 'IX_events_company_id'
    )
        CREATE INDEX IX_events_company_id
        ON dbo.events(company_id);


    IF NOT EXISTS
    (
        SELECT 1 FROM sys.indexes
        WHERE object_id = OBJECT_ID('dbo.leave_requests')
          AND name = 'IX_leave_requests_company_id'
    )
        CREATE INDEX IX_leave_requests_company_id
        ON dbo.leave_requests(company_id);


    IF NOT EXISTS
    (
        SELECT 1 FROM sys.indexes
        WHERE object_id = OBJECT_ID('dbo.performance_reviews')
          AND name = 'IX_performance_reviews_company_id'
    )
        CREATE INDEX IX_performance_reviews_company_id
        ON dbo.performance_reviews(company_id);


    IF NOT EXISTS
    (
        SELECT 1 FROM sys.indexes
        WHERE object_id = OBJECT_ID('dbo.grievances')
          AND name = 'IX_grievances_company_id'
    )
        CREATE INDEX IX_grievances_company_id
        ON dbo.grievances(company_id);


    IF NOT EXISTS
    (
        SELECT 1 FROM sys.indexes
        WHERE object_id = OBJECT_ID('dbo.attendance_records')
          AND name = 'IX_attendance_records_company_id'
    )
        CREATE INDEX IX_attendance_records_company_id
        ON dbo.attendance_records(company_id);


    IF NOT EXISTS
    (
        SELECT 1 FROM sys.indexes
        WHERE object_id = OBJECT_ID('dbo.attendance_events')
          AND name = 'IX_attendance_events_company_id'
    )
        CREATE INDEX IX_attendance_events_company_id
        ON dbo.attendance_events(company_id);


    IF NOT EXISTS
    (
        SELECT 1 FROM sys.indexes
        WHERE object_id = OBJECT_ID('dbo.attendance_monitor')
          AND name = 'IX_attendance_monitor_company_id'
    )
        CREATE INDEX IX_attendance_monitor_company_id
        ON dbo.attendance_monitor(company_id);


    IF NOT EXISTS
    (
        SELECT 1 FROM sys.indexes
        WHERE object_id = OBJECT_ID('dbo.attendance_schedules')
          AND name = 'IX_attendance_schedules_company_id'
    )
        CREATE INDEX IX_attendance_schedules_company_id
        ON dbo.attendance_schedules(company_id);


    IF NOT EXISTS
    (
        SELECT 1 FROM sys.indexes
        WHERE object_id = OBJECT_ID('dbo.attendance_monitor_sessions')
          AND name = 'IX_attendance_monitor_sessions_company_id'
    )
        CREATE INDEX IX_attendance_monitor_sessions_company_id
        ON dbo.attendance_monitor_sessions(company_id);


    /* ========================================================
       19. FINAL VALIDATION
       ======================================================== */

    PRINT '';
    PRINT '============================================================';
    PRINT 'FINAL COMPANY COUNTS';
    PRINT '============================================================';

    SELECT
        c.id AS company_id,
        c.company_code,
        c.company_name,
        COUNT(e.id) AS employee_count
    FROM dbo.companies c
    LEFT JOIN dbo.employees e
        ON e.company_id = c.id
    GROUP BY
        c.id,
        c.company_code,
        c.company_name
    ORDER BY c.id;


    PRINT '';
    PRINT '============================================================';
    PRINT 'UNASSIGNED DATA CHECK';
    PRINT '============================================================';

    SELECT 'employees' AS table_name, COUNT(*) AS unassigned
    FROM dbo.employees
    WHERE company_id IS NULL

    UNION ALL

    SELECT 'training_programs', COUNT(*)
    FROM dbo.training_programs
    WHERE company_id IS NULL

    UNION ALL

    SELECT 'events', COUNT(*)
    FROM dbo.events
    WHERE company_id IS NULL

    UNION ALL

    SELECT 'leave_requests', COUNT(*)
    FROM dbo.leave_requests
    WHERE company_id IS NULL

    UNION ALL

    SELECT 'performance_reviews', COUNT(*)
    FROM dbo.performance_reviews
    WHERE company_id IS NULL

    UNION ALL

    SELECT 'grievances', COUNT(*)
    FROM dbo.grievances
    WHERE company_id IS NULL

    UNION ALL

    SELECT 'attendance_records', COUNT(*)
    FROM dbo.attendance_records
    WHERE company_id IS NULL;


    COMMIT TRANSACTION;

    PRINT '';
    PRINT '============================================================';
    PRINT ' MULTI-COMPANY DATABASE MIGRATION SUCCESSFUL';
    PRINT '============================================================';

END TRY
BEGIN CATCH

    IF XACT_STATE() <> 0
        ROLLBACK TRANSACTION;

    PRINT '';
    PRINT '============================================================';
    PRINT ' MIGRATION FAILED - ALL CHANGES ROLLED BACK';
    PRINT '============================================================';

    PRINT 'Error number: ' + CAST(ERROR_NUMBER() AS VARCHAR(20));
    PRINT 'Error line:   ' + CAST(ERROR_LINE() AS VARCHAR(20));
    PRINT 'Error message: ' + ERROR_MESSAGE();

    THROW;

END CATCH;
GO