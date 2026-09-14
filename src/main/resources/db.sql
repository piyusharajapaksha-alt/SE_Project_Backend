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