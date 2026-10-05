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
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }


    // ==========================================================
    // GET ACTIVE DEPARTMENTS FOR COMPANY
    // ==========================================================

    public List<Department> findAll(
            Long companyId
    ) {

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

                    Department department =
                            new Department();

                    department.setId(
                            rs.getLong("id")
                    );

                    department.setCompanyId(
                            rs.getLong("company_id")
                    );

                    department.setName(
                            rs.getString("name")
                    );

                    department.setActive(
                            rs.getBoolean("active")
                    );

                    return department;
                },
                companyId
        );
    }


    // ==========================================================
    // FIND CANONICAL DEPARTMENT NAME
    // ==========================================================

    public String findCanonicalName(
            String name,
            Long companyId
    ) {

        if (
                name == null ||
                name.trim().isEmpty()
        ) {
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
                    name.trim()
            );

        } catch (
                EmptyResultDataAccessException exception
        ) {

            return null;
        }
    }


    // ==========================================================
    // NORMALIZE MULTIPLE DEPARTMENTS
    // ==========================================================

    public List<String> findCanonicalNames(
            List<String> names,
            Long companyId
    ) {

        List<String> result =
                new ArrayList<>();

        if (
                names == null ||
                names.isEmpty()
        ) {
            return result;
        }

        for (String name : names) {

            if (
                    name == null ||
                    name.trim().isEmpty()
            ) {
                continue;
            }

            String canonical =
                    findCanonicalName(
                            name,
                            companyId
                    );

            if (canonical == null) {

                throw new IllegalArgumentException(
                        "Department does not exist in the current company: "
                                + name.trim()
                );
            }

            boolean duplicate =
                    result.stream()
                            .anyMatch(
                                    existing ->
                                            existing.equalsIgnoreCase(
                                                    canonical
                                            )
                            );

            if (!duplicate) {
                result.add(canonical);
            }
        }

        return result;
    }
}