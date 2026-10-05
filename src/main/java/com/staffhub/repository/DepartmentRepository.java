package com.staffhub.repository;

import com.staffhub.model.Department;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class DepartmentRepository {

    private final JdbcTemplate jdbcTemplate;

    public DepartmentRepository(
            JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ==========================================================
    // GET ACTIVE DEPARTMENTS FOR CURRENT COMPANY
    // ==========================================================

    public List<Department> findAll(
            Long companyId) {

        String sql = """
                SELECT
                    id,
                    company_id,
                    name,
                    active
                FROM dbo.departments
                WHERE company_id = ?
                  AND active = 1
                ORDER BY name
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    Department department = new Department();

                    department.setId(
                            rs.getLong("id"));

                    department.setCompanyId(
                            rs.getLong("company_id"));

                    department.setName(
                            rs.getString("name"));

                    department.setActive(
                            rs.getBoolean("active"));

                    return department;
                },
                companyId);
    }

    // ==========================================================
    // FIND CANONICAL DEPARTMENT NAME
    // ==========================================================

    public String findCanonicalName(
            String name,
            Long companyId) {

        if (name == null ||
                name.trim().isEmpty()) {
            return null;
        }

        try {

            return jdbcTemplate.queryForObject(
                    """
                            SELECT TOP 1 name
                            FROM dbo.departments
                            WHERE company_id = ?
                              AND active = 1
                              AND LOWER(LTRIM(RTRIM(name))) =
                                  LOWER(LTRIM(RTRIM(?)))
                            """,
                    String.class,
                    companyId,
                    name.trim());

        } catch (EmptyResultDataAccessException exception) {

            return null;
        }
    }

    // ==========================================================
    // FIND MULTIPLE CANONICAL NAMES
    // ==========================================================

    public List<String> findCanonicalNames(
            List<String> names,
            Long companyId) {

        List<String> result = new ArrayList<>();

        if (names == null ||
                names.isEmpty()) {
            return result;
        }

        for (String name : names) {

            if (name == null ||
                    name.trim().isEmpty()) {
                continue;
            }

            String canonical = findCanonicalName(
                    name,
                    companyId);

            if (canonical == null) {

                throw new IllegalArgumentException(
                        "Department does not exist in the current company: "
                                + name.trim());
            }

            boolean duplicate = result.stream()
                    .anyMatch(
                            existing -> existing.equalsIgnoreCase(
                                    canonical));

            if (!duplicate) {
                result.add(canonical);
            }
        }

        return result;
    }

    // ==========================================================
    // CREATE DEPARTMENT
    // ==========================================================

    public Department create(String name, Long companyId) {

        String cleanName = name.trim();

        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM dbo.departments
                        WHERE company_id = ?
                          AND LOWER(LTRIM(RTRIM(name))) = LOWER(LTRIM(RTRIM(?)))
                        """,
                Integer.class,
                companyId,
                cleanName);

        if (count != null && count > 0) {
            throw new IllegalArgumentException(
                    "Department already exists: " + cleanName);
        }

        Long id = jdbcTemplate.queryForObject(
                """
                        INSERT INTO dbo.departments
                            (company_id, name, active)
                        OUTPUT INSERTED.id
                        VALUES (?, ?, 1)
                        """,
                Long.class,
                companyId,
                cleanName);

        return findById(id, companyId);
    }

    // ==========================================================
    // CHECK EMPLOYEE USAGE
    // ==========================================================

    public int countEmployeesUsingDepartment(
            String departmentName,
            Long companyId) {

        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM dbo.employees
                        WHERE company_id = ?
                          AND LOWER(LTRIM(RTRIM(department))) =
                              LOWER(LTRIM(RTRIM(?)))
                        """,
                Integer.class,
                companyId,
                departmentName);

        return count == null ? 0 : count;
    }

    // ==========================================================
    // DELETE DEPARTMENT
    //
    // We soft-delete it by setting active = 0.
    //
    // This is safer because employee records may still contain
    // the department name.
    // ==========================================================

    public void delete(Long id, Long companyId) {
        jdbcTemplate.update(
                """
                        DELETE FROM dbo.departments
                        WHERE id = ?
                          AND company_id = ?
                        """,
                id,
                companyId);
    }

    // ==========================================================
    // FIND DEPARTMENT BY ID
    // ==========================================================

    public Department findById(
            Long id,
            Long companyId) {

        try {

            return jdbcTemplate.queryForObject(
                    """
                            SELECT
                                id,
                                company_id,
                                name,
                                active
                            FROM dbo.departments
                            WHERE id = ?
                              AND company_id = ?
                            """,
                    (rs, rowNum) -> {

                        Department department = new Department();

                        department.setId(
                                rs.getLong("id"));

                        department.setCompanyId(
                                rs.getLong("company_id"));

                        department.setName(
                                rs.getString("name"));

                        department.setActive(
                                rs.getBoolean("active"));

                        return department;
                    },
                    id,
                    companyId);

        } catch (EmptyResultDataAccessException exception) {

            return null;
        }
    }
}