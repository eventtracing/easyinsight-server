package com.netease.hz.bdms.eistest.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum AppPushLogAction {
    LOG("log"),
    BASIC_INFO("basicInfo"),
    EXCEPTION("exception"),
    STATUS("status");

    private final String name;

    private static final Map<String, AppPushLogAction> ACTION_MAP = new ConcurrentHashMap<>();

    public static Map<String, AppPushLogAction> getActionMap() {
        if (ACTION_MAP.isEmpty()) {
            Map<String, AppPushLogAction> actionMap = Arrays.stream(values())
                    .collect(Collectors.toMap(
                            AppPushLogAction::getName,
                            Function.identity(),
                            (u, v) -> {
                                throw new IllegalStateException(String.format("Duplicate key %s", u));
                            },
                            ConcurrentHashMap::new)
                    );
            ACTION_MAP.putAll(actionMap);
        }
        return ACTION_MAP;
    }
}
