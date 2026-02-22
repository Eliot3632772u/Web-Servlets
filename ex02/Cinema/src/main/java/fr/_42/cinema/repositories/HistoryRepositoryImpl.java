package fr._42.cinema.repositories;

import fr._42.cinema.repositories.*;
import fr._42.cinema.models.*;
import javax.sql.*;
import java.util.*;
import org.springframework.jdbc.core.*;
import org.springframework.dao.*;
import org.springframework.stereotype.*;
import java.sql.Timestamp;

@Component
public class HistoryRepositoryImpl implements CrudRepository<History>
{
    private JdbcTemplate jdbcTemplate;

    private RowMapper<History> mapper = (rs, rowNum) -> {
        History history = new History();
        history.setId(rs.getLong("id"));
        history.setUserId(rs.getLong("user_id"));
        history.setIpAddress(rs.getString("ip_address"));
        history.setLoginAt(rs.getTimestamp("login_at").toLocalDateTime());
        return history;
    };

    public HistoryRepositoryImpl(JdbcTemplate jdbcTemplates)
    {
        this.jdbcTemplate = jdbcTemplates;
    }

    public History findById(Long id)
    {
        String sql = "SELECT * FROM history WHERE id = ?";

        return  jdbcTemplate.queryForObject(sql, mapper, id);
    }

    public List<History> findAll()
    {
        String sql = "SELECT * FROM history";

        List<History> res = jdbcTemplate.query(sql, mapper);

        return res;
    }

    public List<History> findAllByUserId(Long userId)
    {
        String sql = "SELECT * FROM history WHERE user_id = ?";

        List<History> res = jdbcTemplate.query(sql, mapper, userId);

        return res;
    }

    public void save(History entity) 
    {
        String sql = "INSERT INTO history (user_id, ip_address, login_at) " +
                    "VALUES (?, ?, ?)";

        jdbcTemplate.update(sql, 
            entity.getUserId(), 
            entity.getIpAddress(), 
            Timestamp.valueOf(entity.getLoginAt())
        );
    }

    public void update(History entity) 
    {
        String sql = "UPDATE history SET user_id = ?, ip_address = ?, login_at = ? WHERE id = ?";

        jdbcTemplate.update(sql, 
            entity.getUserId(), 
            entity.getIpAddress(), 
            Timestamp.valueOf(entity.getLoginAt()),
            entity.getId()
        );
    }

    public void delete(Long id)
    {
        String sql = "DELETE FROM history WHERE id = ?";

        jdbcTemplate.update(sql, id);
    }

}
       