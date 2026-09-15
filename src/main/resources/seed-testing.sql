USE StaffHub;
GO

/* ============================================================
   STAFFHUB COMPLETE TEST DATA
   SQL SERVER / SSMS VERSION

   10 Employees
   6 Frontend Mock Authentication Accounts
   Training Management
   Training Assignments
   Training Registrations
   Training Attendance
   Training Completion
   Grievances
   Grievance Responses
   Performance Reviews
   Leave Requests
   Events
   Event Registrations

   IMPORTANT:
   - This script is for SQL Server.
   - It does NOT transfer PostgreSQL data.
   - It creates fresh test data.
   ============================================================ */


/* ============================================================
   0. START TRANSACTION
   ============================================================ */

BEGIN TRY

    BEGIN TRANSACTION;


/* ============================================================
   1. EMPLOYEES
   ============================================================ */

    INSERT INTO employees (
        employee_number,
        first_name,
        last_name,
        email,
        phone,
        department,
        position,
        role,
        employment_status,
        hire_date,
        address,
        emergency_contact,
        salary,
        gender
    )
    VALUES

    /* ========================================================
       MOCK AUTH ACCOUNT 1
       ======================================================== */

    (
        'EMP001',
        'Demo',
        'Employee',
        'tharindu.j@example.com',
        '',
        'Engineering',
        'Software Engineer',
        'Employee',
        'Active',
        '2024-01-15',
        'Colombo, Sri Lanka',
        '0711111111',
        150000.00,
        'Male'
    ),

    /* ========================================================
       MOCK AUTH ACCOUNT 2
       ======================================================== */

    (
        'EMP002',
        'Demo',
        'HR Manager',
        'hr@staffhub.com',
        '',
        'Human Resources',
        'HR Manager',
        'HR Manager',
        'Active',
        '2022-03-10',
        'Colombo, Sri Lanka',
        '0712222222',
        250000.00,
        'Female'
    ),

    /* ========================================================
       MOCK AUTH ACCOUNT 3
       ======================================================== */

    (
        'EMP003',
        'Demo',
        'Department Manager',
        'manager@staffhub.com',
        '',
        'Human Resources',
        'Department Manager',
        'Department Manager',
        'Active',
        '2022-06-20',
        'Nugegoda, Sri Lanka',
        '0713333333',
        220000.00,
        'Male'
    ),

    /* ========================================================
       MOCK AUTH ACCOUNT 4
       ======================================================== */

    (
        'EMP004',
        'Demo',
        'Training Coordinator',
        'training@staffhub.com',
        '',
        'Human Resources',
        'Training Coordinator',
        'Training Coordinator',
        'Active',
        '2023-02-01',
        'Maharagama, Sri Lanka',
        '0714444444',
        180000.00,
        'Female'
    ),

    /* ========================================================
       MOCK AUTH ACCOUNT 5
       ======================================================== */

    (
        'EMP005',
        'Demo',
        'Grievance Officer',
        'grievance@staffhub.com',
        '',
        'Human Resources',
        'Grievance Officer',
        'Grievance Officer',
        'Active',
        '2023-04-15',
        'Kotte, Sri Lanka',
        '0715555555',
        185000.00,
        'Male'
    ),

    /* ========================================================
       MOCK AUTH ACCOUNT 6
       ======================================================== */

    (
        'EMP006',
        'Demo',
        'Event Organizer',
        'event@staffhub.com',
        '',
        'Human Resources',
        'Event Organizer',
        'Event Organizer',
        'Active',
        '2023-05-10',
        'Dehiwala, Sri Lanka',
        '0716666666',
        175000.00,
        'Female'
    ),

    /* ========================================================
       ADDITIONAL EMPLOYEE 7
       ======================================================== */

    (
        'EMP007',
        'Sarah',
        'Williams',
        'sarah.williams@staffhub.com',
        '0777777777',
        'IT',
        'Senior Software Engineer',
        'Employee',
        'Active',
        '2021-08-12',
        'Colombo, Sri Lanka',
        '0717777777',
        210000.00,
        'Female'
    ),

    /* ========================================================
       ADDITIONAL EMPLOYEE 8
       ======================================================== */

    (
        'EMP008',
        'Nimal',
        'Bandara',
        'nimal.bandara@staffhub.com',
        '0788888888',
        'IT',
        'UI/UX Designer',
        'Employee',
        'Active',
        '2023-09-01',
        'Battaramulla, Sri Lanka',
        '0718888888',
        160000.00,
        'Male'
    ),

    /* ========================================================
       ADDITIONAL EMPLOYEE 9
       ======================================================== */

    (
        'EMP009',
        'Shanika',
        'De Silva',
        'shanika.desilva@staffhub.com',
        '0799999999',
        'Finance',
        'Accountant',
        'Employee',
        'On Leave',
        '2022-11-15',
        'Mount Lavinia, Sri Lanka',
        '0719999999',
        145000.00,
        'Female'
    ),

    /* ========================================================
       ADDITIONAL EMPLOYEE 10
       ======================================================== */

    (
        'EMP010',
        'Ravindu',
        'Perera',
        'ravindu.perera@staffhub.com',
        '0700000000',
        'Marketing',
        'Marketing Executive',
        'Employee',
        'Active',
        '2024-02-20',
        'Wattala, Sri Lanka',
        '0720000000',
        135000.00,
        'Male'
    );


/* ============================================================
   2. TRAINING PROGRAMS

   training_for is stored as JSON.

   Example:
   ["Engineering"]
   ["Engineering","Human Resources"]
   ["Engineering","Human Resources","IT"]
   ============================================================ */

    INSERT INTO training_programs (
        title,
        description,
        trainer,
        category,
        start_date,
        end_date,
        location,
        capacity,
        training_for,
        status
    )
    VALUES

    (
        'Advanced React Development',
        'Advanced React concepts including hooks, state management, reusable components and performance optimization.',
        'Demo Training Coordinator',
        'Technical',
        '2026-09-20',
        '2026-09-20',
        'Training Room A',
        25,
        '["Engineering"]',
        'Upcoming'
    ),

    (
        'Workplace Communication',
        'Professional communication, teamwork, conflict resolution and effective workplace collaboration.',
        'Demo Training Coordinator',
        'Soft Skills',
        '2026-09-25',
        '2026-09-25',
        'Conference Room',
        30,
        '["Engineering","Human Resources"]',
        'Upcoming'
    ),

    (
        'Leadership Development',
        'Leadership skills, decision making, employee management and workplace leadership practices.',
        'Demo Training Coordinator',
        'Leadership',
        '2026-10-05',
        '2026-10-05',
        'Main Auditorium',
        20,
        '["Engineering","Human Resources","IT"]',
        'Upcoming'
    );


/* ============================================================
   3. TRAINING ASSIGNMENTS

   Automatically assign employees based on training_for JSON.
   ============================================================ */

    /* Advanced React Development
       Engineering employees */

    INSERT INTO training_assignments (
        training_id,
        employee_id
    )
    SELECT
        tp.id,
        e.employee_number
    FROM training_programs tp
    INNER JOIN employees e
        ON EXISTS (
            SELECT 1
            FROM OPENJSON(tp.training_for) j
            WHERE j.value = e.department
        )
    WHERE tp.title = 'Advanced React Development';


    /* Workplace Communication
       Engineering + Human Resources */

    INSERT INTO training_assignments (
        training_id,
        employee_id
    )
    SELECT
        tp.id,
        e.employee_number
    FROM training_programs tp
    INNER JOIN employees e
        ON EXISTS (
            SELECT 1
            FROM OPENJSON(tp.training_for) j
            WHERE j.value = e.department
        )
    WHERE tp.title = 'Workplace Communication';


    /* Leadership Development
       Engineering + Human Resources + IT */

    INSERT INTO training_assignments (
        training_id,
        employee_id
    )
    SELECT
        tp.id,
        e.employee_number
    FROM training_programs tp
    INNER JOIN employees e
        ON EXISTS (
            SELECT 1
            FROM OPENJSON(tp.training_for) j
            WHERE j.value = e.department
        )
    WHERE tp.title = 'Leadership Development';


/* ============================================================
   4. TRAINING REGISTRATIONS
   ============================================================ */

    INSERT INTO training_registrations (
        training_id,
        employee_id
    )
    SELECT
        tp.id,
        e.employee_number
    FROM training_programs tp
    INNER JOIN employees e
        ON e.employee_number IN (
            'EMP001',
            'EMP007',
            'EMP008'
        )
    WHERE tp.title = 'Advanced React Development';


    INSERT INTO training_registrations (
        training_id,
        employee_id
    )
    SELECT
        tp.id,
        e.employee_number
    FROM training_programs tp
    INNER JOIN employees e
        ON e.employee_number IN (
            'EMP001',
            'EMP002',
            'EMP003'
        )
    WHERE tp.title = 'Workplace Communication';


/* ============================================================
   5. TRAINING ATTENDANCE
   ============================================================ */

    INSERT INTO training_attendance (
        training_id,
        employee_id,
        status
    )
    SELECT
        tp.id,
        e.employee_number,
        CASE
            WHEN e.employee_number IN ('EMP001', 'EMP007')
                THEN 'Present'

            WHEN e.employee_number = 'EMP008'
                THEN 'Absent'

            ELSE 'Pending'
        END
    FROM training_programs tp
    INNER JOIN employees e
        ON e.employee_number IN (
            'EMP001',
            'EMP007',
            'EMP008'
        )
    WHERE tp.title = 'Advanced React Development';


    INSERT INTO training_attendance (
        training_id,
        employee_id,
        status
    )
    SELECT
        tp.id,
        e.employee_number,
        'Pending'
    FROM training_programs tp
    INNER JOIN employees e
        ON e.employee_number IN (
            'EMP001',
            'EMP002',
            'EMP003'
        )
    WHERE tp.title = 'Workplace Communication';


/* ============================================================
   6. TRAINING COMPLETION
   ============================================================ */

    INSERT INTO training_completion (
        training_id,
        employee_id,
        status,
        completed_at
    )
    SELECT
        tp.id,
        e.employee_number,

        CASE
            WHEN e.employee_number IN ('EMP001', 'EMP007')
                THEN 'Completed'

            WHEN e.employee_number = 'EMP008'
                THEN 'Not Completed'

            ELSE 'Pending'
        END,

        CASE
            WHEN e.employee_number IN ('EMP001', 'EMP007')
                THEN SYSDATETIME()

            ELSE NULL
        END

    FROM training_programs tp
    INNER JOIN employees e
        ON e.employee_number IN (
            'EMP001',
            'EMP007',
            'EMP008'
        )
    WHERE tp.title = 'Advanced React Development';


/* ============================================================
   7. GRIEVANCES
   ============================================================ */

    /* EMP001 = NEW
       Used for employee Edit/Delete testing. */

    INSERT INTO grievances (
        employee_id,
        category,
        priority,
        description,
        status,
        assigned_to
    )
    VALUES (
        'EMP001',
        'Workplace',
        'Medium',
        'I would like to report an issue with the current workstation setup. The equipment provided for my daily development work is causing productivity issues and requires attention.',
        'New',
        NULL
    );


    /* EMP002 = UNDER REVIEW */

    INSERT INTO grievances (
        employee_id,
        category,
        priority,
        description,
        status,
        assigned_to
    )
    VALUES (
        'EMP002',
        'Work Environment',
        'High',
        'There are recurring issues with the office environment that are affecting normal HR operations. The matter requires review by the responsible department.',
        'Under Review',
        'EMP005'
    );


    /* EMP003 = ASSIGNED */

    INSERT INTO grievances (
        employee_id,
        category,
        priority,
        description,
        status,
        assigned_to
    )
    VALUES (
        'EMP003',
        'Workplace',
        'Medium',
        'There is an issue with the meeting room scheduling system. Several department meetings have been affected because of conflicting bookings.',
        'Assigned',
        'EMP005'
    );


    /* EMP004 = RESOLVED */

    INSERT INTO grievances (
        employee_id,
        category,
        priority,
        description,
        status,
        assigned_to
    )
    VALUES (
        'EMP004',
        'Training',
        'Low',
        'The training room booking information was previously incorrect. The issue was reported and has now been corrected.',
        'Resolved',
        'EMP005'
    );


/* ============================================================
   8. GRIEVANCE RESPONSES
   ============================================================ */

    INSERT INTO grievance_responses (
        grievance_id,
        employee_id,
        response_text
    )
    SELECT
        g.id,
        'EMP005',
        'Your grievance has been received and is currently being reviewed by the Grievance Officer.'
    FROM grievances g
    WHERE g.employee_id = 'EMP002';


    INSERT INTO grievance_responses (
        grievance_id,
        employee_id,
        response_text
    )
    SELECT
        g.id,
        'EMP005',
        'The issue has been assigned for investigation. We will provide an update once the review is completed.'
    FROM grievances g
    WHERE g.employee_id = 'EMP003';


    INSERT INTO grievance_responses (
        grievance_id,
        employee_id,
        response_text
    )
    SELECT
        g.id,
        'EMP005',
        'The reported issue has been investigated and the required correction has been completed.'
    FROM grievances g
    WHERE g.employee_id = 'EMP004';


/* ============================================================
   9. PERFORMANCE REVIEWS

   EMP001 intentionally has NO September 2026 review.
   This allows a new September review to be created for EMP001.
   ============================================================ */

    INSERT INTO performance_reviews (
        employee_id,
        review_period,
        quality_of_work,
        productivity,
        teamwork,
        communication,
        responsibility,
        problem_solving,
        overall_rating,
        manager_feedback,
        areas_for_improvement,
        status
    )
    VALUES

    (
        'EMP002',
        '2026-09',
        5,
        4,
        5,
        4,
        5,
        4,
        4.50,
        'Strong HR leadership and effective employee management.',
        'Continue developing strategic HR planning skills.',
        'Completed'
    ),

    (
        'EMP003',
        '2026-09',
        4,
        4,
        5,
        4,
        4,
        5,
        4.33,
        'Demonstrates strong department coordination and decision making.',
        'Further improve delegation and long-term planning.',
        'Completed'
    ),

    (
        'EMP004',
        '2026-09',
        5,
        5,
        4,
        5,
        5,
        4,
        4.67,
        'Excellent training coordination and communication skills.',
        'Continue expanding technical training knowledge.',
        'Completed'
    );


    /* Historical review - EMP001 */

    INSERT INTO performance_reviews (
        employee_id,
        review_period,
        quality_of_work,
        productivity,
        teamwork,
        communication,
        responsibility,
        problem_solving,
        overall_rating,
        manager_feedback,
        areas_for_improvement,
        status
    )
    VALUES (
        'EMP001',
        '2026-08',
        4,
        4,
        5,
        4,
        4,
        4,
        4.17,
        'Good technical performance and teamwork.',
        'Improve documentation and technical planning.',
        'Completed'
    );


    /* Historical review - EMP007 */

    INSERT INTO performance_reviews (
        employee_id,
        review_period,
        quality_of_work,
        productivity,
        teamwork,
        communication,
        responsibility,
        problem_solving,
        overall_rating,
        manager_feedback,
        areas_for_improvement,
        status
    )
    VALUES (
        'EMP007',
        '2026-08',
        5,
        5,
        4,
        4,
        5,
        5,
        4.67,
        'Excellent software engineering performance.',
        'Continue mentoring junior team members.',
        'Completed'
    );


/* ============================================================
   10. LEAVE REQUESTS
   ============================================================ */

    INSERT INTO leave_requests (
        employee_id,
        leave_type,
        start_date,
        end_date,
        reason,
        approver_id,
        status,
        comment
    )
    VALUES

    (
        'EMP001',
        'Annual Leave',
        '2026-10-12',
        '2026-10-14',
        'Personal vacation.',
        'EMP003',
        'Pending',
        NULL
    ),

    (
        'EMP002',
        'Medical Leave',
        '2026-09-21',
        '2026-09-22',
        'Medical appointment and recovery.',
        'EMP003',
        'Approved',
        'Leave approved by Department Manager.'
    ),

    (
        'EMP003',
        'Annual Leave',
        '2026-11-02',
        '2026-11-04',
        'Personal leave.',
        'EMP002',
        'Pending',
        NULL
    ),

    (
        'EMP004',
        'Annual Leave',
        '2026-10-20',
        '2026-10-22',
        'Personal vacation.',
        'EMP003',
        'Approved',
        'Leave approved.'
    ),

    (
        'EMP009',
        'Medical Leave',
        '2026-09-14',
        '2026-09-18',
        'Medical leave.',
        'EMP003',
        'Approved',
        'Medical leave approved.'
    );


/* ============================================================
   11. EVENTS
   ============================================================ */

    INSERT INTO events (
        title,
        description,
        organizer_id,
        category,
        event_date,
        start_time,
        end_time,
        location,
        capacity,
        status
    )
    VALUES

    (
        'Staff Annual Meetup 2026',
        'Annual StaffHub employee gathering and team networking event.',
        'EMP006',
        'Company Event',
        '2026-10-10',
        '09:00',
        '16:00',
        'Colombo Conference Center',
        100,
        'Upcoming'
    ),

    (
        'Technology Innovation Day',
        'A company event focused on technology innovation, AI and modern software development.',
        'EMP006',
        'Technology',
        '2026-10-24',
        '10:00',
        '15:00',
        'Main Auditorium',
        80,
        'Upcoming'
    ),

    (
        'Employee Wellness Workshop',
        'Employee wellness, work-life balance and workplace wellbeing session.',
        'EMP006',
        'Wellness',
        '2026-09-28',
        '13:00',
        '16:00',
        'Training Room A',
        50,
        'Upcoming'
    );


/* ============================================================
   12. EVENT REGISTRATIONS
   ============================================================ */

    INSERT INTO event_registrations (
        event_id,
        employee_id
    )
    SELECT
        ev.id,
        e.employee_number
    FROM events ev
    CROSS JOIN employees e
    WHERE ev.title = 'Staff Annual Meetup 2026'
      AND e.employee_number IN (
          'EMP001',
          'EMP002',
          'EMP003',
          'EMP004'
      );


    INSERT INTO event_registrations (
        event_id,
        employee_id
    )
    SELECT
        ev.id,
        e.employee_number
    FROM events ev
    CROSS JOIN employees e
    WHERE ev.title = 'Technology Innovation Day'
      AND e.employee_number IN (
          'EMP001',
          'EMP007',
          'EMP008'
      );


    INSERT INTO event_registrations (
        event_id,
        employee_id
    )
    SELECT
        ev.id,
        e.employee_number
    FROM events ev
    CROSS JOIN employees e
    WHERE ev.title = 'Employee Wellness Workshop'
      AND e.employee_number IN (
          'EMP001',
          'EMP004',
          'EMP009'
      );


/* ============================================================
   13. COMMIT
   ============================================================ */

    COMMIT TRANSACTION;

    PRINT 'StaffHub test data inserted successfully.';

END TRY

BEGIN CATCH

    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    PRINT 'ERROR: StaffHub test data insertion failed.';

    THROW;

END CATCH;
GO


/* ============================================================
   14. VERIFICATION
   ============================================================ */


/* EMPLOYEES */

SELECT
    employee_number,
    first_name,
    last_name,
    email,
    department,
    position,
    role,
    employment_status
FROM employees
ORDER BY employee_number;
GO


/* TRAINING PROGRAMS */

SELECT
    id,
    title,
    category,
    trainer,
    start_date,
    end_date,
    location,
    capacity,
    training_for,
    status
FROM training_programs
ORDER BY id;
GO


/* TRAINING ASSIGNMENTS */

SELECT
    ta.id,
    tp.title AS training,
    ta.employee_id,
    e.first_name,
    e.last_name,
    e.department
FROM training_assignments ta
INNER JOIN training_programs tp
    ON tp.id = ta.training_id
INNER JOIN employees e
    ON e.employee_number = ta.employee_id
ORDER BY ta.id;
GO


/* TRAINING REGISTRATIONS */

SELECT
    tr.id,
    tp.title AS training,
    tr.employee_id,
    e.first_name,
    e.last_name,
    tr.registered_at
FROM training_registrations tr
INNER JOIN training_programs tp
    ON tp.id = tr.training_id
INNER JOIN employees e
    ON e.employee_number = tr.employee_id
ORDER BY tr.id;
GO


/* TRAINING ATTENDANCE */

SELECT
    ta.id,
    tp.title AS training,
    ta.employee_id,
    e.first_name,
    e.last_name,
    ta.status,
    ta.marked_at
FROM training_attendance ta
INNER JOIN training_programs tp
    ON tp.id = ta.training_id
INNER JOIN employees e
    ON e.employee_number = ta.employee_id
ORDER BY ta.id;
GO


/* TRAINING COMPLETION */

SELECT
    tc.id,
    tp.title AS training,
    tc.employee_id,
    e.first_name,
    e.last_name,
    tc.status,
    tc.completed_at
FROM training_completion tc
INNER JOIN training_programs tp
    ON tp.id = tc.training_id
INNER JOIN employees e
    ON e.employee_number = tc.employee_id
ORDER BY tc.id;
GO


/* GRIEVANCES */

SELECT
    id,
    employee_id,
    category,
    priority,
    status,
    assigned_to,
    created_at
FROM grievances
ORDER BY id;
GO


/* GRIEVANCE RESPONSES */

SELECT
    gr.id,
    gr.grievance_id,
    gr.employee_id,
    gr.response_text,
    gr.created_at
FROM grievance_responses gr
ORDER BY gr.id;
GO


/* PERFORMANCE */

SELECT
    id,
    employee_id,
    review_period,
    quality_of_work,
    productivity,
    teamwork,
    communication,
    responsibility,
    problem_solving,
    overall_rating,
    status
FROM performance_reviews
ORDER BY id;
GO


/* LEAVE */

SELECT
    id,
    employee_id,
    leave_type,
    start_date,
    end_date,
    approver_id,
    status,
    comment
FROM leave_requests
ORDER BY id;
GO


/* EVENTS */

SELECT
    id,
    title,
    organizer_id,
    category,
    event_date,
    start_time,
    end_time,
    location,
    capacity,
    status
FROM events
ORDER BY id;
GO


/* EVENT REGISTRATIONS */

SELECT
    er.id,
    er.event_id,
    ev.title AS event,
    er.employee_id,
    e.first_name,
    e.last_name,
    er.registered_at
FROM event_registrations er
INNER JOIN events ev
    ON ev.id = er.event_id
INNER JOIN employees e
    ON e.employee_number = er.employee_id
ORDER BY er.id;
GO


/* ============================================================
   FINAL RECORD COUNTS
   ============================================================ */

SELECT 'employees' AS table_name, COUNT(*) AS record_count
FROM employees

UNION ALL

SELECT 'training_programs', COUNT(*)
FROM training_programs

UNION ALL

SELECT 'training_assignments', COUNT(*)
FROM training_assignments

UNION ALL

SELECT 'training_registrations', COUNT(*)
FROM training_registrations

UNION ALL

SELECT 'training_attendance', COUNT(*)
FROM training_attendance

UNION ALL

SELECT 'training_completion', COUNT(*)
FROM training_completion

UNION ALL

SELECT 'grievances', COUNT(*)
FROM grievances

UNION ALL

SELECT 'grievance_responses', COUNT(*)
FROM grievance_responses

UNION ALL

SELECT 'performance_reviews', COUNT(*)
FROM performance_reviews

UNION ALL

SELECT 'leave_requests', COUNT(*)
FROM leave_requests

UNION ALL

SELECT 'events', COUNT(*)
FROM events

UNION ALL

SELECT 'event_registrations', COUNT(*)
FROM event_registrations;
GO