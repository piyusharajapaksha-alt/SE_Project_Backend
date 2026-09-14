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