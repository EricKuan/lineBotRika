package com.fet.lineBot.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Value("${spring.datasource.hikari.username}")
  private String userName;
  @Value("${spring.datasource.hikari.password}")
  private String passwd;
  @Value("${spring.datasource.hikari.maximumPoolSize}")
  private int maximumPoolSize;

  @Bean
  public DataSource dataSource() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(dbUrl);
    config.setUsername(userName);
    config.setPassword(passwd);
    config.setMaximumPoolSize(maximumPoolSize);
    config.setMinimumIdle(0);
    return new HikariDataSource(config);
  }
}
