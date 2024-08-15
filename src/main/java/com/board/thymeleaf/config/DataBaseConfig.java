package com.board.thymeleaf.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

import lombok.RequiredArgsConstructor;

@org.springframework.context.annotation.Configuration("board.db.config")
@MapperScan(annotationClass = BoardMapper.class, basePackages = "com.board.thymeleaf.repository", sqlSessionFactoryRef = "boardSqlSessionFactory")
@RequiredArgsConstructor
public class DataBaseConfig {

  private final MybatisProperties mybatisProperties;

  @Bean(name = "boardDataSource", destroyMethod = "close")
  @ConfigurationProperties(prefix = "thymeleaf.datasource.db-board")
  public DataSource boardDataSource() {
    HikariDataSource ds = (HikariDataSource) DataSourceBuilder.create().build();
    ds.setMaximumPoolSize(4);
    ds.setPoolName("boardDataSourcePool");
    return ds;
  }

  @Bean(name="boardSqlSessionFactory")
  public SqlSessionFactory boardSqlSessionFactory(DataSource dataSource) throws Exception {

    SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
    sqlSessionFactoryBean.setDataSource(dataSource);
    
    sqlSessionFactoryBean.setVfs(SpringBootVFS.class);
    sqlSessionFactoryBean.setConfiguration(mybatisProperties.getConfiguration());
    sqlSessionFactoryBean.setTypeAliasesPackage(mybatisProperties.getTypeAliasesPackage());
    sqlSessionFactoryBean.setMapperLocations(mybatisProperties.resolveMapperLocations());
    return sqlSessionFactoryBean.getObject();

  }

  @Bean(name="boardTxManager")
  public PlatformTransactionManager boardTxManager(DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }


}
