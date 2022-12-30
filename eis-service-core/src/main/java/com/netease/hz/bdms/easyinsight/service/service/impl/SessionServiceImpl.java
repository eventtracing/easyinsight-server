package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.netease.hz.bdms.easyinsight.common.dto.common.SessionDTO;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.SessionMapper;
import com.netease.hz.bdms.easyinsight.dao.model.Session;
import com.netease.hz.bdms.easyinsight.service.service.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SessionServiceImpl implements SessionService {

  @Autowired
  private SessionMapper sessionMapper;

  @Override
  public Boolean add(SessionDTO SessionDTO) {
    Preconditions.checkArgument(null != SessionDTO);
    Session session = BeanConvertUtils.convert(SessionDTO, Session.class, true);
    return 0 != sessionMapper.add(session);
  }

  @Override
  public SessionDTO getByToken(Long domainId, String token) {
    if (StringUtils.isBlank(token) || null == domainId) {
      return null;
    }

    Session session = sessionMapper.getByToken(domainId, token);
    return BeanConvertUtils.convert(session, SessionDTO.class, true);
  }

  @Override
  public SessionDTO getLatestByUserId(Long domainId, Long userId) {
    if (null == userId || null == domainId) {
      return null;
    }

    Session session = sessionMapper.getLatestByUserId(domainId, userId);
    return BeanConvertUtils.convert(session, SessionDTO.class, true);
  }

  @Override
  public Boolean removeByToken(Long domainId, String token) {
    if (StringUtils.isBlank(token) || null == domainId) {
      return true;
    }
    return 0 != sessionMapper.removeByToken(domainId, token);
  }

}
