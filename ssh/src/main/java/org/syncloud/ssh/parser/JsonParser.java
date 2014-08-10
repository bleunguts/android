package org.syncloud.ssh.parser;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.syncloud.common.model.Result;
import org.syncloud.ssh.model.SshResult;

import java.util.ArrayList;
import java.util.List;

public class JsonParser {

    //TODO: Migrate spm to single json reply and remove this class
    public static <T> Result<List<T>> parse(SshResult sshResult, Class<T> valueType) {

        if (!sshResult.ok())
            return Result.error(sshResult.getMessage());

        try {
            List<T> objs = new ArrayList<T>();
            ObjectMapper mapper = new ObjectMapper();
            String[] lines = sshResult.getMessage().split("\\r?\\n");
            for (String line : lines) {
                if (line.trim().startsWith("{"))
                    objs.add(mapper.readValue(line, valueType));
            }
            return Result.value(objs);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}
