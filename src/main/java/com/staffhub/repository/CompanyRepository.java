package com.staffhub.repository;

import com.staffhub.model.Company;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class CompanyRepository {

    private final JdbcTemplate jdbcTemplate;

    public CompanyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsByEmail(String email) {

        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM companies
                WHERE LOWER(email) = LOWER(?)
                """,
                Integer.class,
                email.trim());

        return count != null && count > 0;
    }

    public Long insert(Company company) {

        String sql = """
            INSERT INTO companies
            (
                company_code,
                company_name,
                email,
                phone,
                address,
                industry,
                status,
                owner_id
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder =
                new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {

            PreparedStatement ps =
                    connection.prepareStatement(
                            sql,
                            Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, company.getCompanyCode());
            ps.setString(2, company.getCompanyName());
            ps.setString(3, company.getEmail());
            ps.setString(4, company.getPhone());
            ps.setString(5, company.getAddress());
            ps.setString(6, company.getIndustry());
            ps.setString(7, company.getStatus());
            ps.setLong(8, company.getOwnerId());

            return ps;

        }, keyHolder);

        if (keyHolder.getKey() == null) {
            throw new IllegalStateException(
                    "Unable to create company");
        }

        return keyHolder.getKey().longValue();
    }

    public Company findByOwnerEmail(String ownerEmail) {

        String sql = """
            SELECT
                c.id,
                c.company_code,
                c.company_name,
                c.email,
                c.phone,
                c.address,
                c.industry,
                c.status,
                c.owner_id,
                c.created_at,
                c.updated_at
            FROM companies c
            INNER JOIN company_owners o
                ON o.id = c.owner_id
            WHERE LOWER(o.email) = LOWER(?)
            """;

        List<Company> results =
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) -> {

                            Company c =
                                    new Company();

                            c.setId(
                                    rs.getLong("id"));

                            c.setCompanyCode(
                                    rs.getString(
                                            "company_code"));

                            c.setCompanyName(
                                    rs.getString(
                                            "company_name"));

                            c.setEmail(
                                    rs.getString("email"));

                            c.setPhone(
                                    rs.getString("phone"));

                            c.setAddress(
                                    rs.getString(
                                            "address"));

                            c.setIndustry(
                                    rs.getString(
                                            "industry"));

                            c.setStatus(
                                    rs.getString(
                                            "status"));

                            c.setOwnerId(
                                    rs.getLong(
                                            "owner_id"));

                            if (rs.getTimestamp(
                                    "created_at") != null) {

                                c.setCreatedAt(
                                        rs.getTimestamp(
                                                "created_at")
                                                .toLocalDateTime());
                            }

                            if (rs.getTimestamp(
                                    "updated_at") != null) {

                                c.setUpdatedAt(
                                        rs.getTimestamp(
                                                "updated_at")
                                                .toLocalDateTime());
                            }

                            return c;
                        },
                        ownerEmail.trim());

        return results.isEmpty()
                ? null
                : results.get(0);
    }
}