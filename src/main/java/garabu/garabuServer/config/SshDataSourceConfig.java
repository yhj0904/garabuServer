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
@Profile({"default", "local"})  // default와 local 프로파일에서 활성화
@RequiredArgsConstructor
public class SshDataSourceConfig {

    private final SshTunnelInit initializer;

    @Bean("dataSource")
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        log.info("Datasource Properties URL: {}", properties.getUrl());
        log.info("Datasource Properties Username: {}", properties.getUsername());
        log.info("Datasource Properties Driver ClassName: {}", properties.getDriverClassName());
        
        Integer localPort = initializer.buildSshConnection();
        
        // SSH 터널링이 비활성화되어 있으면 기본 DataSource 사용
        if (localPort == null) {
            log.info("SSH 터널링이 비활성화되어 있습니다. 기본 DataSource 설정을 사용합니다.");
            return DataSourceBuilder.create()
                    .url(properties.getUrl())
                    .username(properties.getUsername())
                    .password(properties.getPassword())
                    .driverClassName(properties.getDriverClassName())
                    .build();
        }
        
        // SSH 터널링이 활성화되어 있으면 localhost로 포트 포워딩
        log.info("SSH 터널링이 활성화되어 있습니다. 포트 {}를 사용합니다.", localPort);
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
