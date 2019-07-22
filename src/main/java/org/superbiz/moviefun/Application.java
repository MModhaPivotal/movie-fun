package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
     public DatabaseServiceCredentials databaseServiceCredentials (@Value("${VCAP_SERVICES}") String vcapServicesJson){
        return new DatabaseServiceCredentials(vcapServicesJson);
    }

    @Bean
    public HikariDataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {

        HikariDataSource ds = new HikariDataSource();
        MysqlDataSource dataSource = new MysqlDataSource();
        ds.setDataSource(dataSource);
        dataSource.setURL(serviceCredentials.jdbcUrl("albums-mysql"));
        return ds;
    }

    @Bean
    public HikariDataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariDataSource ds = new HikariDataSource();
        MysqlDataSource dataSource = new MysqlDataSource();
        ds.setDataSource(dataSource);
        dataSource.setURL(serviceCredentials.jdbcUrl("movies-mysql"));
        return ds;
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter (){
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        return hibernateJpaVendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean movielocalEntityManagerFactoryBean(HibernateJpaVendorAdapter hibernateJpaVendorAdapter, DataSource moviesDataSource) {
        LocalContainerEntityManagerFactoryBean movielocalEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        //localEntityManagerFactoryBean.setPersistenceProvider(dataSource);
        movielocalEntityManagerFactoryBean.setDataSource(moviesDataSource);
        movielocalEntityManagerFactoryBean.setPackagesToScan("org.superbiz.moviefun.movies");

        movielocalEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        movielocalEntityManagerFactoryBean.setPersistenceUnitName("movieUnit");
        return movielocalEntityManagerFactoryBean;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean albumlocalEntityManagerFactoryBean(HibernateJpaVendorAdapter hibernateJpaVendorAdapter, DataSource albumsDataSource) {
        LocalContainerEntityManagerFactoryBean albumslocalEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        //localEntityManagerFactoryBean.setPersistenceProvider(dataSource);
        albumslocalEntityManagerFactoryBean.setDataSource(albumsDataSource);
        albumslocalEntityManagerFactoryBean.setPackagesToScan("org.superbiz.moviefun.albums");

        albumslocalEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);

        albumslocalEntityManagerFactoryBean.setPersistenceUnitName("albumUnit");
        return albumslocalEntityManagerFactoryBean;
    }

    @Bean
    public PlatformTransactionManager albumPlatformTransactionManager (EntityManagerFactory albumlocalEntityManagerFactoryBean){
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager(albumlocalEntityManagerFactoryBean);
        return jpaTransactionManager;
    }

    @Bean
    public PlatformTransactionManager moviePlatformTransactionManager (EntityManagerFactory movielocalEntityManagerFactoryBean){
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager(movielocalEntityManagerFactoryBean);
        return jpaTransactionManager;
    }



}
