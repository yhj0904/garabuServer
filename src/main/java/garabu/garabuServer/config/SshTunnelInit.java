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
@Profile({"default", "local"})  // default와 local 프로파일에서 활성화
@ConfigurationProperties(prefix = "ec2")
@Validated
@Setter
public class SshTunnelInit {

    private boolean enabled = false;  // SSH 터널링 활성화 여부
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
        if (!enabled) {
            log.info("SSH 터널링이 비활성화되어 있습니다. ec2.enabled=false");
            return null;
        }

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
