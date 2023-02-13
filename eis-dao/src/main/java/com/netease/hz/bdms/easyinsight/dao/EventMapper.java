package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.Event;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventMapper {

  /**
   * 根据名称获取事件类型对象
   * @param code 事件类型
   * @return 事件类型对象集合
   */
  List<Event> selectByCode(@Param("code") String code, @Param("appId") Long appId);

  /**
   * 根据名称获取事件类型对象
   * @param codes 事件类型
   * @return 事件类型对象集合
   */
  List<Event> selectByCodes(@Param("codes") Set<String> codes, @Param("appId") Long appId);

  List<Event> selectAllByAppId(@Param("appId") Long appId);

  /**
   * 根据主键ID获取事件类型对象
   * @param id 事件类型ID
   * @return 事件类型对象集合
   */
  Event selectByPrimaryKey(Long id);

  /**
   * 插入事件类型对象
   * @param event 事件类型对象
   */
  Integer insert(Event event);

  /**
   * 修改事件类型对象
   * @param event 事件类型对象
   */
  Integer update(Event event);

  /**
   * 删除事件类型对象
   * @param id 主键ID
   */
  Integer delete(Long id);

  Integer deleteByAppId(Long appId);

  Integer searchEventSize(@Param("search") String search,
                          @Param("appId") Long appId);

  List<Event> searchEvent(@Param("search") String search, @Param("appId") Long appId,
                          @Param("orderBy") String orderBy, @Param("orderRule") String orderRule,
                          @Param("offset") Integer offset, @Param("count") Integer count);

  List<Event> selectByIds(@Param("eventIds")Collection<Long> eventIds);
}
