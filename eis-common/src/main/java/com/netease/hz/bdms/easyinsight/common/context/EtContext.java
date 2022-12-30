package com.netease.hz.bdms.easyinsight.common.context;

import com.google.common.collect.Maps;
import java.util.Map;

/**
 * description:
 *
 * @author: gaoshuangchao
 * @createDate: 2020-05-21
 * @version: 1.0
 */
public class EtContext {

  private static final ThreadLocal<Map<String,Object>> MAP_THREAD_LOCAL = ThreadLocal.withInitial(
      Maps::newHashMap);

  public static void clear() {
    MAP_THREAD_LOCAL.remove();
  }

  public static void put(String key, Object value) {
    MAP_THREAD_LOCAL.get().put(key, value);
  }

  @SuppressWarnings("unchecked")
  public static <T> T remove(String key) {
    return (T) MAP_THREAD_LOCAL.get().remove(key);
  }

  @SuppressWarnings("unchecked")
  public static <T> T get(String key) {
    return (T) MAP_THREAD_LOCAL.get().get(key);
  }

  /**
   * 直接设置上下文内容
   */
  public static void setContextMap(Map<String, Object> contextMap) {
    if (null == contextMap) {
      contextMap = Maps.newHashMap();
    }
    MAP_THREAD_LOCAL.set(contextMap);
  }

  /**
   * 获取线程上下文的拷贝对象
   */
  public static Map<String, Object> getCopyOfContextMap() {
    return Maps.newHashMap(MAP_THREAD_LOCAL.get());
  }

}
