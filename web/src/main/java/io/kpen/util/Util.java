package io.kpen.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class Util {

    public static String map2str(Map<String,Object> m) {
        return StringUtils.join(m.entrySet(), "\n\t");
    }
}
