package com.netease.hz.bdms.easyinsight.web.demo.adapters;

import com.netease.eis.adapters.RealtimeConfigAdapter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class DemoRealtimeConfigAdapter implements RealtimeConfigAdapter {

    private static final Map<String, String> configMap = new HashMap<>();

    static {
        configMap.put("eis.http.host", "http://intern.easyinsight-dev.bdms.netease.com");
        configMap.put("eis.backend-http.host", "http://easyinsight-dev-backend.service.163.org");
        configMap.put("default.alert.receivers", "xxx@corp.netease.com");
        configMap.put("checkObjImageNotEmpty", "{}");
        configMap.put("paramCheckScopeMap", "{}");
        configMap.put("managersOfApp", "{}");
        configMap.put("forbiddenParamCodes", "[]");
        configMap.put("sysProperties", "{}");
        configMap.put("logCodes", "[\"-1\"]");
    }

    @Override
    public void listenBoolean(String configKey, Consumer<Boolean> consumer) {
        String s = configMap.get(configKey);
        if (s == null) {
            consumer.accept(false);
            return;
        }
        consumer.accept("true".equals(s));
    }

    @Override
    public void listenString(String configKey, Consumer<String> consumer) {
        String s = configMap.get(configKey);
        consumer.accept(s);
    }

    @Override
    public void listenInteger(String configKey, Consumer<Integer> consumer) {
        String s = configMap.get(configKey);
        if (s == null) {
            consumer.accept(null);
            return;
        }
        consumer.accept(Integer.parseInt(s));
    }

    @Override
    public void listenJSON(String configKey, Consumer<String> consumer) {
        String s = configMap.get(configKey);
        if (s == null) {
            return;
        }
        consumer.accept(s);
    }
}
