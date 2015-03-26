package org.syncloud.platform.insider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.platform.ssh.ConnectionPointProvider;
import org.syncloud.platform.ssh.SshRunner;
import org.syncloud.platform.ssh.model.StringResult;

import java.io.IOException;

import static org.syncloud.platform.ssh.SshRunner.cmd;

public class InsiderManager {

    private static Logger logger = Logger.getLogger(InsiderManager.class);


    private static final String INSIDER_BIN = "insider";
    public static final ObjectMapper JSON = new ObjectMapper();
    private SshRunner ssh;

    public InsiderManager() {
        this.ssh = new SshRunner();
    }

    public Optional<String> userDomain(ConnectionPointProvider connectionPoint) {
        Optional<String> execute = ssh.run(connectionPoint, cmd(INSIDER_BIN, "user_domain"));
        if (execute.isPresent()) {
            try {
                return Optional.of(JSON.readValue(execute.get(), StringResult.class).data);
            } catch (IOException e) {
                logger.error("unable to parse user domain reply");
            }
        }

        logger.error("unable to get user domain reply");
        return Optional.absent();
    }
}
