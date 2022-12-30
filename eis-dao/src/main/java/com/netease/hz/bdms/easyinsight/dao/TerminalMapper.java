package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.Terminal;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TerminalMapper {

  /**
   * 根据名称获取终端对象
   * @param name 终端name
   * @param appId 产品主键ID
   * @return 终端对象集合
   */
  List<Terminal> selectByName(@Param("name") String name, @Param("appId") Long appId);

  /**
   * 根据主键ID获取终端对象
   * @param id 主键ID
   * @return 终端对象集合
   */
  Terminal selectByPrimaryKey(Long id);

  /**
   * 插入终端对象
   * @param terminal 终端对象
   */
  Integer insert(Terminal terminal);

  Integer batchInsert(@Param("terminals") List<Terminal> terminals);

  /**
   * 修改终端对象
   * @param terminal 终端对象
   */
  Integer update(Terminal terminal);

  /**
   * 删除终端对象
   * @param id 终端对象ID
   */
  Integer delete(Long id);


  Integer searchTerminalSize(@Param("search") String search,
      @Param("terminalTypes") List<Integer> terminalTypes,
      @Param("preset") Boolean preset,
      @Param("appId") Long appId);

  List<Terminal> searchTerminal(@Param("search") String search,
      @Param("terminalTypes") List<Integer> terminalTypes,
      @Param("preset") Boolean preset,
      @Param("appId") Long appId,
      @Param("orderBy") String orderBy, @Param("orderRule") String orderRule,
      @Param("offset") Integer offset, @Param("count") Integer count);

  List<Terminal> selectByIds(@Param("terminalIds") Collection<Long> terminalIds);

  List<Terminal> selectByAppId(@Param("appId") Long appId);

  List<Terminal> selectAll();
}
