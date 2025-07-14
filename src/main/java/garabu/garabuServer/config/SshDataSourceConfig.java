package garabu.garabuServer.config;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@Profile("!prod")  // prod 프로파일이 아닐 때 활성화 (로컬 개발용)
@RequiredArgsConstructor
public class SshDataSourceConfig {

    private final SshTunnelInit initializer;

    @Bean("dataSource")
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        log.info("Datasource Properties URL: {}", properties.getUrl());
        log.info("Datasource Properties Username: {}", properties.getUsername());
        log.info("Datasource Properties Password: {}", properties.getPassword());
        log.info("Datasource Properties Driver ClassName: {}", properties.getDriverClassName());
        int localPort = initializer.buildSshConnection();
        String url = String.format(
                "jdbc:p6spy:mysql://localhost:%d/garabu?serverTimezone=Asia/Seoul&characterEncoding=UTF-8",
                localPort);
        return DataSourceBuilder.create()
                .url(url)
                .username(properties.getUsername())
                .password(properties.getPassword())
                .driverClassName(properties.getDriverClassName())
                .build();
    }
}
