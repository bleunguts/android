package org.syncloud.apps.sam;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.syncloud.common.model.Result;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.SshResult;

import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.join;
import static org.syncloud.common.model.Result.error;
import static org.syncloud.common.model.Result.value;

public class Sam {
    public static final ObjectMapper JSON = new ObjectMapper();
    private Ssh ssh;
    private Release release;

    public Sam(Ssh ssh, Release release) {
        this.ssh = ssh;
        this.release = release;
    }

    public static String cmd(String... arguments) {
        List<String> cmd = asList(arguments);
        return "sam "+ join(cmd, " ");
    }

    private <TContent> Result<TContent> runTyped(final TypeReference type, Device device, String... arguments) {
        return ssh.execute(device, cmd(arguments)).flatMap(new Result.Function<String, Result<TContent>>() {
            @Override
            public Result<TContent> apply(String v) throws Exception {
                SshResult<TContent> result = JSON.readValue(v, type);
                return value(result.data);
            }
        });
    }

    private Result<List<AppVersions>> appsVersions(Device device, String... arguments) {
        return runTyped(new TypeReference<SshResult<List<AppVersions>>>() {}, device, arguments);
    }

    public Result<String> run(Device device, String... arguments) {
        return ssh.execute(device, cmd(arguments)).flatMap(new Result.Function<String, Result<String>>() {
            @Override
            public Result<String> apply(String v) throws Exception {
                SshResult result = JSON.readValue(v, SshResult.class);
                if (result.success)
                    return value(result.message);
                else
                    return error(result.message);
            }
        });
    }

    public Result<List<AppVersions>> update(Device device) {
        return appsVersions(device, Commands.update, "--release", release.getVersion());
    }

    public Result<List<AppVersions>> list(Device device) {
        return appsVersions(device, Commands.list);
    }
}
