package garabu.garabuServer.config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import jakarta.annotation.PreDestroy;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Component
@Profile("!prod")  // prod 프로파일이 아닐 때 활성화 (로컬 개발용)
@ConfigurationProperties(prefix = "ec2")
@Validated
@Setter
public class SshTunnelInit {

    private String remoteJumpHost;
    private int sshPort;
    private String user;
    private String privateKeyPath;
    private String databaseEndpoint;
    private int databasePort;

    private Session session;

    @PreDestroy
    public void destroySSH() {
        if (session != null && session.isConnected())
            session.disconnect();
    }

    public Integer buildSshConnection() {
        Integer forwardPort = null;

        try {
            log.info("SSH 세션 설정 확인 → user: {}, host: {}, port: {}, key: {}, dbHost: {}, dbPort: {}",
                    user, remoteJumpHost, sshPort, privateKeyPath, databaseEndpoint, databasePort);

            log.info("Connecting to SSH with {}@{}:{} using privateKey at {}", user, remoteJumpHost,
                    sshPort, privateKeyPath);
            JSch jsch = new JSch();

            jsch.addIdentity(privateKeyPath);
            session = jsch.getSession(user, remoteJumpHost, sshPort);
            session.setConfig("StrictHostKeyChecking", "no");

            log.info("Starting SSH session connection...");
            session.connect();
            log.info("SSH session connected");

            forwardPort = session.setPortForwardingL(0, databaseEndpoint, databasePort);
            log.info("Port forwarding created on local port {} to remote port {}", forwardPort,
                    databasePort);
        } catch (JSchException e) {
            log.error(e.getMessage());
            this.destroySSH();
            throw new RuntimeException(e);
        }
        return forwardPort;

    }
}
