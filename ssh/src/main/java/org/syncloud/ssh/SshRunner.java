package org.syncloud.ssh;

import com.google.common.base.Optional;
import com.google.common.io.ByteStreams;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.syncloud.ssh.model.ConnectionPoint;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.Endpoint;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static com.google.common.collect.Lists.newArrayList;

public class SshRunner {
    public static final int SSH_SERVER_PORT = 22;

    private static Logger logger = Logger.getLogger(SshRunner.class);

    public static String[] cmd(String... arguments) {
        return arguments;
    }

    public static String quotedCmd(String[] arguments) {
        List<String> quotedParams = newArrayList();
        for (String p: arguments)
            quotedParams.add("'"+p+"'");
        String quotedCommand = StringUtils.join(quotedParams, " ");
        return quotedCommand;
    }

    public Optional<String> run(ConnectionPointProvider connectionPoint, String[] command) {
        return run(connectionPoint.get(), command);
    }

    public Optional<String> run(ConnectionPoint connectionPoint, String[] command) {
        Endpoint endpoint = connectionPoint.endpoint();
        Credentials credentials = connectionPoint.credentials();

        String quotedCommand = quotedCmd(command);
        logger.info("executing: " + quotedCommand);
        JSch jsch = new JSch();

        try {

            Session session = jsch.getSession(credentials.login(), endpoint.host(), endpoint.port());
            session.setTimeout(10000);
            logger.info("Endpoint: " + endpoint);
            if (credentials.key() == null) {
                logger.info("Password authentication");
                session.setPassword(credentials.password());
            } else {
                logger.info("Public key authentication");
                jsch.addIdentity(credentials.login(), credentials.key().getBytes(), null, new byte[0]);
                session.setUserInfo(new EmptyUserInfo());
            }

            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            session.setConfig(prop);

            try {
                logger.info("session connecting");
                session.connect();
                logger.info("session connected");

                ChannelExec channel = (ChannelExec) session.openChannel("exec");

                channel.setOutputStream(new LogOutputStream() {
                    @Override
                    protected void processLine(String line, int level) {
                        logger.info(line);
                    }
                });

                channel.setErrStream(new LogOutputStream() {
                    @Override
                    protected void processLine(String line, int level) {
                        logger.error(line);
                    }
                });

                channel.setCommand(quotedCommand);
                InputStream inputStream = channel.getInputStream();

                try {
                    logger.info("channel connecting");
                    channel.connect();
                    logger.info("reading output");
                    String output = new String(ByteStreams.toByteArray(inputStream));
                    logger.info("waiting for exit code");
                    while (channel.getExitStatus() == -1) {
                        try {
                            logger.info("sleeping");
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            logger.error("got exception while sleeping", e);
                        }
                    }
                    int exitCode = channel.getExitStatus();
                    logger.info("got exit code: " + exitCode);
                    if (exitCode == 0)
                        return Optional.of(output);
                    else {
                        logger.error(output);
                        return Optional.absent();
                    }
                } finally {
                    if (channel.isConnected())
                        channel.disconnect();
                }
            } finally {
                if (session.isConnected())
                    session.disconnect();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Optional.absent();
        }
    }
}
