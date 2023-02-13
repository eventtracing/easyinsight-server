package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.dao.model.rbac.User;

import java.util.Collection;
import java.util.List;

public interface UserService {

    Long create(String email, String name);

    List<User> getByIds(Collection<Long> ids);

    User getByEmail(String email);

    List<User> getByEmails(Collection<String> emails);

    Integer update(UserDTO userDto);

    UserDTO getByUserId(Long userId);

    List<UserSimpleDTO> searchUser(Long domainId, Long appId, String orderBy, String orderRule, String search);
}
