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