package dev.openmod.project.util;

import java.util.HashMap;
import java.util.Map;

public class Variables {
    public static Map<String, Object> variables = new HashMap<String, Object>();
    public static Object getVariable(String scope, String name) {
        return variables.get(scope + "-" + name);
    }

    public static void setVariable(String scope, String name, Object value) {
        variables.put(scope + "-" + name, value);
    }
}
