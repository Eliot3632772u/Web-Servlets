package fr._42.cinema.repositories;

import fr._42.cinema.repositories.*;
import fr._42.cinema.models.*;
import javax.sql.*;
import java.util.*;
import org.springframework.jdbc.core.*;
import org.springframework.dao.*;
import org.springframework.stereotype.*;

@Component
public class UsersRepositoryImpl implements UsersRepository
{
    private JdbcTemplate jdbcTemplate;
    private RowMapper<User> mapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPhone(rs.getString("phone"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        return user;
    };

    public UsersRepositoryImpl(JdbcTemplate jdbcTemplates)
    {
        this.jdbcTemplate = jdbcTemplates;
    }

    public Optional<User> findByPhone(String phone)
    {
        String sql = "SELECT * FROM users WHERE phone = ?";

        try {

            User res = jdbcTemplate.queryForObject(sql, mapper, phone);

            return Optional.of(res);

        } catch(EmptyResultDataAccessException e) {

            return Optional.empty();
        }
    }

    public User findById(Long id)
    {
        String sql = "SELECT * FROM users WHERE id = ?";

        return  jdbcTemplate.queryForObject(sql, mapper, id);
    }

    public List<User> findAll()
    {
        String sql = "SELECT * FROM users";

        List<User> res = jdbcTemplate.query(sql, mapper);

        return res;
    }

    public void save(User entity) 
    {
        String sql = "INSERT INTO users (first_name, last_name, phone, password, email) " +
                    "VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql, 
            entity.getFirstName(), 
            entity.getLastName(), 
            entity.getPhone(), 
            entity.getPassword(),
            entity.getEmail()
        );
    }

    public void update(User entity) 
    {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, phone = ?, password = ?, email = ? WHERE id = ?";

        jdbcTemplate.update(sql, 
            entity.getFirstName(), 
            entity.getLastName(), 
            entity.getPhone(), 
            entity.getPassword(), 
            entity.getEmail(),
            entity.getId()
        );
    }

    public void delete(Long id)
    {
        String sql = "DELETE FROM users WHERE id = ?";

        jdbcTemplate.update(sql, id);
    }
} 