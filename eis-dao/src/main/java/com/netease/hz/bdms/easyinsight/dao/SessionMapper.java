package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.Session;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface SessionMapper {
  /**
   * 添加会话
   *
   * @param session 会话
   * @return 添加条数
   */
  int add(Session session);

  /**
   * 获取会话
   *
   * @param token token
   * @return 会话
   */
  Session getByToken(@Param("domainId") Long domainId, String token);

  /**
   * 获取最近的会话
   *
   * @param userId 用户id
   * @return 最近的会话
   */
  Session getLatestByUserId(@Param("domainId") Long domainId, @Param("userId") Long userId);

  /**
   * 删除会话
   *
   * @param token token
   * @return 删除条数
   */
  int removeByToken(@Param("domainId") Long domainId, @Param("token") String token);

  /**
   * 删除会话
   *
   * @param userId 用户id
   * @return 删除条数
   */
  int removeByUserId(@Param("domainId") Long domainId, @Param("userId") Long userId);

}
