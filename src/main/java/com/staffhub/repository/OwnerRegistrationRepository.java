package com.staffhub.repository;

import com.staffhub.model.Company;
import com.staffhub.model.Owner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
public class OwnerRegistrationRepository {

    private final JdbcTemplate jdbcTemplate;

    public OwnerRegistrationRepository(
            JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean ownerEmailExists(String email) {

        Integer count =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM company_owners
                        WHERE LOWER(email) = LOWER(?)
                        """,
                        Integer.class,
                        email.trim());

        return count != null && count > 0;
    }

    public boolean authEmailExists(String email) {

        Integer count =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM staffhub_auth_users
                        WHERE LOWER(email) = LOWER(?)
                        """,
                        Integer.class,
                        email.trim());

        return count != null && count > 0;
    }

    public Long insertOwner(Owner owner) {

        String sql = """
            INSERT INTO company_owners
            (
                first_name,
                last_name,
                email,
                phone,
                status
            )
            VALUES (?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder =
                new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {

            PreparedStatement ps =
                    connection.prepareStatement(
                            sql,
                            Statement.RETURN_GENERATED_KEYS);

            ps.setString(
                    1,
                    owner.getFirstName());

            ps.setString(
                    2,
                    owner.getLastName());

            ps.setString(
                    3,
                    owner.getEmail());

            ps.setString(
                    4,
                    owner.getPhone());

            ps.setString(
                    5,
                    owner.getStatus());

            return ps;

        }, keyHolder);

        if (keyHolder.getKey() == null) {
            throw new IllegalStateException(
                    "Unable to create owner");
        }

        return keyHolder.getKey().longValue();
    }

    public void insertAuthUser(
            Long ownerId,
            String email,
            String passwordHash) {

        jdbcTemplate.update(
                """
                INSERT INTO staffhub_auth_users
                (
                    employee_id,
                    owner_id,
                    email,
                    password_hash,
                    enabled
                )
                VALUES (NULL, ?, ?, ?, 1)
                """,
                ownerId,
                email,
                passwordHash);
    }
}