package org.syncloud.apps.sam;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;
import org.syncloud.common.model.Result;

import java.util.List;

import static java.util.Arrays.asList;

public class Sam {
    public static final ObjectMapper JSON = new ObjectMapper();
    private static final String SAM_BOOTSTRAP_COMMAND =
            "wget -qO- https://raw.githubusercontent.com/syncloud/apps/0.7/sam | bash -s install";
    private static final String SAM_EXIST_COMMAND = "type sam";

    public static Result<String> run(Device device, List<String> arguments) {
        Result<String> installResult = ensureSamInstalled(device);
        if (installResult .hasError())
            return installResult;

        String command = StringUtils.join(arguments, " ");
        command = "sam" + " " + command;
        return Ssh.execute(device, command);
    }

    private static Result<String> ensureSamInstalled(Device device) {
        Result<String> exists = Ssh.execute(device, SAM_EXIST_COMMAND);
        if (exists.hasError())
            return Ssh.execute(device, SAM_BOOTSTRAP_COMMAND);
        return exists;
    }

    public static Result<String> updateSpm(Device device) {
        Result<String> installResult = ensureSamInstalled(device);
        if (installResult .hasError())
            return installResult;

        return run(device, asList(Commands.Install, "sam"));
    }

    public static Result<Boolean> ensureAdminToolsInstalled(Device device, Function<String, String> progress) {

        Result<String> installResult = ensureSamInstalled(device);
        if (installResult .hasError())
            return installResult.map(new Result.Function<String, Boolean>() {
                @Override
                public Boolean apply(String input) throws Exception {
                    return true;
                }
            });

        progress.apply("getting list of apps");
        Result<List<AppVersions>> list = list(device);
        if (list.hasError())
            return Result.error(list.getError());

        for (AppVersions appVersions : filter(list.getValue(), App.Type.admin)) {

            String command;
            if(!appVersions.installed()) {
                command = Commands.Install;
            } else {
                if (!appVersions.current_version.equals(appVersions.installed_version))
                    command = Commands.Upgrade;
                else
                    continue;
            }

            progress.apply("installing " + appVersions.app.name);
            Result<String> install = run(device, asList(command, appVersions.app.id));
            if (install.hasError())
                return Result.error(install.getError());
        }

        return Result.value(true);
    }

    public static Result<List<AppVersions>> list(Device device) {

        return run(device, asList("list"))
                .flatMap(new Result.Function<String, Result<List<AppVersions>>>() {
                    @Override
                    public Result<List<AppVersions>> apply(String input) throws Exception {
                        List<AppVersions> apps = JSON.readValue(input, AppListReply.class).data;
                        apps = filterNot(apps, App.Type.system);
                        return Result.value(apps);
                    }
                });

    }

    private static List<AppVersions> filterNot(List<AppVersions> apps, final App.Type type) {
        return  Lists.newArrayList(Iterables.filter(apps, new Predicate<AppVersions>() {
            @Override
            public boolean apply(AppVersions input) {
                return input.app.appType() != type;
            }
        }));
    }

    private static List<AppVersions> filter(List<AppVersions> apps, final App.Type type) {
        return  Lists.newArrayList(Iterables.filter(apps, new Predicate<AppVersions>() {
            @Override
            public boolean apply(AppVersions input) {
                return input.app.appType() == type;
            }
        }));
    }
}
