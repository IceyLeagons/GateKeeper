package net.iceyleagons.gatekeeper.sqlite

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.jdbc.datasource.DriverManagerDataSource
import javax.sql.DataSource

@Configuration
class SQLiteDatasourceConfig(val environment: Environment) {

    @Bean
    fun dataSource(): DataSource {
        val ds = DriverManagerDataSource()

        ds.setDriverClassName(environment.getProperty("driverClassName")!!)
        ds.url = environment.getProperty("url")
        ds.username = environment.getProperty("user")
        ds.password = environment.getProperty("password")

        return ds
    }
}