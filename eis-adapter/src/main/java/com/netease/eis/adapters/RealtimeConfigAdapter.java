package com.netease.eis.adapters;

import java.util.function.Consumer;

/**
 * 实时配置适配器
 *
 */
public interface RealtimeConfigAdapter {

    /**
     * 监听配置值
     * 1、服务启动时，需要可以触发consumer
     * 2、配置改变时，需要可以触发consumer
     * 3、建议在实现consumer，将日志打印
     *
     * @param configKey 配置KEY
     * @param consumer  配置变更操作
     */
    void listenBoolean(String configKey, Consumer<Boolean> consumer);

    /**
     * 监听配置值
     * 1、服务启动时，需要可以触发consumer
     * 2、配置改变时，需要可以触发consumer
     * 3、建议在实现consumer，将日志打印
     *
     * @param configKey 配置KEY
     * @param consumer  配置变更操作
     */
    void listenString(String configKey, Consumer<String> consumer);

    /**
     * 监听配置值
     * 1、服务启动时，需要可以触发consumer
     * 2、配置改变时，需要可以触发consumer
     * 3、建议在实现consumer，将日志打印
     *
     * @param configKey 配置KEY
     * @param consumer  配置变更操作
     */
    void listenInteger(String configKey, Consumer<Integer> consumer);

    /**
     * 监听配置值
     * 1、服务启动时，需要可以触发consumer
     * 2、配置改变时，需要可以触发consumer
     * 3、建议在实现consumer，将日志打印
     *
     * @param configKey 配置KEY
     * @param consumer  配置变更操作
     */
    void listenJSON(String configKey, Consumer<String> consumer);
}
