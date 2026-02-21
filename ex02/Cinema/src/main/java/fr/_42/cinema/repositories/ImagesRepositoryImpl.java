package fr._42.cinema.repositories;

import fr._42.cinema.repositories.*;
import fr._42.cinema.models.*;
import javax.sql.*;
import java.util.*;
import org.springframework.jdbc.core.*;
import org.springframework.dao.*;
import org.springframework.stereotype.*;

@Component
public class ImagesRepositoryImpl implements CrudRepository<Image>
{
    private JdbcTemplate jdbcTemplate;
    private RowMapper<Image> mapper = (rs, rowNum) -> {
        Image image = new Image();
        image.setId(rs.getLong("id"));
        image.setFilename(rs.getString("file_name"));
        image.setFilepath(rs.getString("file_path"));
        image.setUserId(rs.getLong("user_id"));
        image.setFileSize(rs.getString("file_size"));
        return image;
    };

    public ImagesRepositoryImpl(JdbcTemplate jdbcTemplates)
    {
        this.jdbcTemplate = jdbcTemplates;
    }

    public Image findById(Long id)
    {
        String sql = "SELECT * FROM images WHERE id = ?";

        return  jdbcTemplate.queryForObject(sql, mapper, id);
    }

    public List<Image> findAll()
    {
        String sql = "SELECT * FROM images";

        List<Image> res = jdbcTemplate.query(sql, mapper);

        return res;
    }

    public void save(Image entity) 
    {
        String sql = "INSERT INTO images (file_name, file_path, user_id, file_size) " +
                    "VALUES (?, ?, ?, ?)";

        jdbcTemplate.update(sql, 
            entity.getFilename(), 
            entity.getFilepath(), 
            entity.getUserId(), 
            entity.getFileSize()
        );
    }

    public void update(Image entity) 
    {
        String sql = "UPDATE images SET file_name = ?, file_path = ?, user_id = ?, file_size = ? WHERE id = ?";

        jdbcTemplate.update(sql, 
            entity.getFilename(), 
            entity.getFilepath(), 
            entity.getUserId(), 
            entity.getFileSize(),
            entity.getId()
        );
    }

    public void delete(Long id)
    {
        String sql = "DELETE FROM images WHERE id = ?";

        jdbcTemplate.update(sql, id);
    }

} 