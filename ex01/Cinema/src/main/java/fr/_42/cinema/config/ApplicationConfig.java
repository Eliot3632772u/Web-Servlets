package fr._42.cinema.config;

import fr._42.cinema.repositories.*;
import fr._42.cinema.models.*;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.PropertySource; 
import javax.sql.*;
import com.zaxxer.hikari.*;
import org.springframework.jdbc.core.*;
import org.springframework.security.crypto.password.*;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@ComponentScan(basePackages = "fr._42.cinema")
@PropertySource("classpath:application.properties")
public class ApplicationConfig
{
    @Autowired
    Environment env;

    @Bean
    public DataSource hickariDataSource()
    {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(env.getProperty("db.url"));
        config.setUsername(env.getProperty("db.user"));
        config.setPassword(env.getProperty("db.password"));
        config.setDriverClassName(env.getProperty("db.driver.name"));

        return new HikariDataSource(config);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource ds)
    {
        return new JdbcTemplate(ds);
    }

    @Bean
    public PasswordEncoder bCryptPasswordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}
