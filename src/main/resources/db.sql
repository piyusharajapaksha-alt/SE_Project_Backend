CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,

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
    address TEXT,
    emergency_contact VARCHAR(100),
    salary NUMERIC(12, 2),

    gender VARCHAR(20)
);



-- ============================================================
-- TRAINING MANAGEMENT
-- ============================================================

CREATE TABLE training_programs (
    id BIGSERIAL PRIMARY KEY,

    title VARCHAR(200) NOT NULL,
    description TEXT,

    trainer VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL,

    start_date DATE NOT NULL,
    end_date DATE,

    location VARCHAR(200) NOT NULL,

    capacity INTEGER NOT NULL CHECK (capacity > 0),

    training_for TEXT[] NOT NULL DEFAULT '{}',

    status VARCHAR(30) NOT NULL
        CHECK (status IN (
            'Upcoming',
            'Ongoing',
            'Completed',
            'Cancelled'
        ))
);

CREATE TABLE IF NOT EXISTS grievances (
    id BIGSERIAL PRIMARY KEY,

    employee_id VARCHAR(50) NOT NULL,

    category VARCHAR(100) NOT NULL,

    priority VARCHAR(30) NOT NULL DEFAULT 'Medium',

    description TEXT NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'New',

    assigned_to VARCHAR(50),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_grievance_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_number)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS grievance_responses (
    id BIGSERIAL PRIMARY KEY,

    grievance_id BIGINT NOT NULL,

    employee_id VARCHAR(50) NOT NULL,

    response_text TEXT NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_response_grievance
        FOREIGN KEY (grievance_id)
        REFERENCES grievances(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_response_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_number)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);


-- ============================================================
-- PERFORMANCE MANAGEMENT
-- ============================================================

CREATE TABLE IF NOT EXISTS performance_reviews (
    id BIGSERIAL PRIMARY KEY,

    employee_id VARCHAR(50) NOT NULL,

    review_period VARCHAR(7) NOT NULL,

    quality_of_work INTEGER NOT NULL
        CHECK (quality_of_work BETWEEN 1 AND 5),

    productivity INTEGER NOT NULL
        CHECK (productivity BETWEEN 1 AND 5),

    teamwork INTEGER NOT NULL
        CHECK (teamwork BETWEEN 1 AND 5),

    communication INTEGER NOT NULL
        CHECK (communication BETWEEN 1 AND 5),

    responsibility INTEGER NOT NULL
        CHECK (responsibility BETWEEN 1 AND 5),

    problem_solving INTEGER NOT NULL
        CHECK (problem_solving BETWEEN 1 AND 5),

    overall_rating NUMERIC(3,2) NOT NULL
        CHECK (overall_rating BETWEEN 1 AND 5),

    manager_feedback TEXT,

    areas_for_improvement TEXT,

    status VARCHAR(30) NOT NULL DEFAULT 'Pending Review'
        CHECK (
            status IN (
                'Pending Review',
                'Completed'
            )
        ),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_performance_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_number)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT unique_employee_review_period
        UNIQUE (employee_id, review_period),

    CONSTRAINT valid_review_period
        CHECK (
            review_period ~ '^[0-9]{4}-(0[1-9]|1[0-2])$'
        )
);


-- ============================================================
-- LEAVE MANAGEMENT
-- ============================================================

CREATE TABLE IF NOT EXISTS leave_requests (
    id BIGSERIAL PRIMARY KEY,

    employee_id VARCHAR(50) NOT NULL,

    leave_type VARCHAR(50) NOT NULL,

    start_date DATE NOT NULL,

    end_date DATE NOT NULL,

    reason TEXT NOT NULL,

    approver_id VARCHAR(50),

    status VARCHAR(30) NOT NULL DEFAULT 'Pending',

    comment TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_leave_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_number)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_leave_approver
        FOREIGN KEY (approver_id)
        REFERENCES employees(employee_number)
        ON UPDATE CASCADE
        ON DELETE SET NULL,

    CONSTRAINT valid_leave_dates
        CHECK (end_date >= start_date),

    CONSTRAINT valid_leave_status
        CHECK (
            status IN (
                'Pending',
                'Approved',
                'Rejected',
                'Cancelled'
            )
        )
);

-- ============================================================
-- EVENT MANAGEMENT
-- ============================================================

CREATE TABLE IF NOT EXISTS events (
    id BIGSERIAL PRIMARY KEY,

    title VARCHAR(200) NOT NULL,

    description TEXT,

    organizer_id VARCHAR(50) NOT NULL,

    category VARCHAR(100) NOT NULL,

    event_date DATE NOT NULL,

    start_time TIME NOT NULL,

    end_time TIME NOT NULL,

    location VARCHAR(200) NOT NULL,

    capacity INTEGER NOT NULL
        CHECK (capacity > 0),

    status VARCHAR(30) NOT NULL DEFAULT 'Upcoming'
        CHECK (
            status IN (
                'Upcoming',
                'Ongoing',
                'Completed',
                'Cancelled'
            )
        ),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_event_organizer
        FOREIGN KEY (organizer_id)
        REFERENCES employees(employee_number)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT valid_event_times
        CHECK (end_time > start_time)
);


-- ============================================================
-- EVENT REGISTRATIONS
-- ============================================================

CREATE TABLE IF NOT EXISTS event_registrations (
    id BIGSERIAL PRIMARY KEY,

    event_id BIGINT NOT NULL,

    employee_id VARCHAR(50) NOT NULL,

    registered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_event_registration_event
        FOREIGN KEY (event_id)
        REFERENCES events(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_event_registration_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_number)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT unique_event_employee
        UNIQUE (event_id, employee_id)
);


-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_events_date
    ON events(event_date);

CREATE INDEX IF NOT EXISTS idx_events_status
    ON events(status);

CREATE INDEX IF NOT EXISTS idx_events_category
    ON events(category);

CREATE INDEX IF NOT EXISTS idx_event_registrations_event
    ON event_registrations(event_id);

CREATE INDEX IF NOT EXISTS idx_event_registrations_employee
    ON event_registrations(employee_id);


    -- ============================================================
-- TRAINING EMPLOYEE ASSIGNMENTS
-- ============================================================

CREATE TABLE IF NOT EXISTS training_assignments (
    id BIGSERIAL PRIMARY KEY,

    training_id BIGINT NOT NULL,

    employee_id VARCHAR(50) NOT NULL,

    assigned_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_training_assignment_training
        FOREIGN KEY (training_id)
        REFERENCES training_programs(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_training_assignment_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_number)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT unique_training_employee_assignment
        UNIQUE (
            training_id,
            employee_id
        )
);


-- ============================================================
-- TRAINING REGISTRATIONS
-- ============================================================

CREATE TABLE IF NOT EXISTS training_registrations (
    id BIGSERIAL PRIMARY KEY,

    training_id BIGINT NOT NULL,

    employee_id VARCHAR(50) NOT NULL,

    registered_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_training_registration_training
        FOREIGN KEY (training_id)
        REFERENCES training_programs(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_training_registration_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_number)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT unique_training_employee_registration
        UNIQUE (
            training_id,
            employee_id
        )
);


-- ============================================================
-- TRAINING ATTENDANCE
-- ============================================================

CREATE TABLE IF NOT EXISTS training_attendance (
    id BIGSERIAL PRIMARY KEY,

    training_id BIGINT NOT NULL,

    employee_id VARCHAR(50) NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'Pending',

    marked_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_training_attendance_training
        FOREIGN KEY (training_id)
        REFERENCES training_programs(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_training_attendance_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_number)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT valid_training_attendance_status
        CHECK (
            status IN (
                'Present',
                'Absent',
                'Pending'
            )
        ),

    CONSTRAINT unique_training_employee_attendance
        UNIQUE (
            training_id,
            employee_id
        )
);


-- ============================================================
-- TRAINING COMPLETION
-- ============================================================

CREATE TABLE IF NOT EXISTS training_completion (
    id BIGSERIAL PRIMARY KEY,

    training_id BIGINT NOT NULL,

    employee_id VARCHAR(50) NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'Pending',

    completed_at TIMESTAMP,

    CONSTRAINT fk_training_completion_training
        FOREIGN KEY (training_id)
        REFERENCES training_programs(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_training_completion_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_number)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT valid_training_completion_status
        CHECK (
            status IN (
                'Completed',
                'Not Completed',
                'Pending'
            )
        ),

    CONSTRAINT unique_training_employee_completion
        UNIQUE (
            training_id,
            employee_id
        )
);


-- ============================================================
-- TRAINING INDEXES
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_training_assignments_training
    ON training_assignments(training_id);

CREATE INDEX IF NOT EXISTS idx_training_assignments_employee
    ON training_assignments(employee_id);

CREATE INDEX IF NOT EXISTS idx_training_registrations_training
    ON training_registrations(training_id);

CREATE INDEX IF NOT EXISTS idx_training_registrations_employee
    ON training_registrations(employee_id);

CREATE INDEX IF NOT EXISTS idx_training_attendance_training
    ON training_attendance(training_id);

CREATE INDEX IF NOT EXISTS idx_training_completion_training
    ON training_completion(training_id);