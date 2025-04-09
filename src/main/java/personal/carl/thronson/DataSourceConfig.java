package personal.carl.thronson;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

// hello

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;

@Configuration
public class DataSourceConfig {

  @Autowired
  private Environment env;

  private String driverClassName;
  private String user;
  private String password;
  private String url;

  @PostConstruct
  public void init() {
    driverClassName = env.getProperty("spring.datasource.driverClassName");
    user = env.getProperty("spring.datasource.username");
    password = env.getProperty("spring.datasource.password");
    url = env.getProperty("spring.datasource.url");
  }

  @Bean
  public DataSource getDataSource() {
    DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
    dataSourceBuilder.driverClassName(driverClassName);
    dataSourceBuilder.url(url);
    dataSourceBuilder.username(user);
    dataSourceBuilder.password(password);
    return dataSourceBuilder.build();
  }
}

