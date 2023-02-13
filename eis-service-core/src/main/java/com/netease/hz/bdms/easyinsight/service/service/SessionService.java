package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.common.SessionDTO;

public interface SessionService {

  /**
   * 添加会话
   *
   * @param SessionDTO 会话
   * @return 是否成功
   */
  Boolean add(SessionDTO SessionDTO);

  /**
   * 获取会话
   *
   * @param token token
   * @return 会话
   */
  SessionDTO getByToken(Long domainId, String token);

  /**
   * 获取最近的会话
   *
   * @param userId 用户id
   * @return 最新的会话
   */
  SessionDTO getLatestByUserId(Long domainId, Long userId);

  /**
   * 删除会话
   *
   * @param token token
   * @return 是否成功
   */
  Boolean removeByToken(Long domainId, String token);
}
