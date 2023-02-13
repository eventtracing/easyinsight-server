package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.constant.GlobalConst;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.RoleTypeEnum;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.model.rbac.User;
import com.netease.hz.bdms.easyinsight.dao.rbac.UserMapper;
import com.netease.hz.bdms.easyinsight.service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private static final Map<String, String> orderByMap = ImmutableMap
            .of("createTime", "create_time", "updateTime", "update_time");
    private static final Map<String, String> orderRuleMap = ImmutableMap
            .of("descend", "desc", "ascend", "asc");
    @Autowired
    private UserMapper userMapper;

    @Override
    public Long create(String email, String name) {
        Preconditions.checkArgument(StringUtils.isNotBlank(email), "邮箱不能为空");
        if (StringUtils.isBlank(name)) {
            // abc@xxx.com --> abc 为用户名
            name = StringUtils.substringBefore(email, GlobalConst.AT);
        }
        User user = new User(email, name);
        userMapper.insertSelective(user);
        return user.getId();
    }

    @Override
    public List<User> getByIds(Collection<Long> ids) {
        return CollectionUtils.isEmpty(ids) ? new ArrayList<>() : userMapper.getByIds(ids);
    }


    @Override
    public User getByEmail(String email) {
        Preconditions.checkArgument(StringUtils.isNotBlank(email), "邮箱不能为空");
        return userMapper.selectByEmail(email);
    }


    @Override
    public List<User> getByEmails(Collection<String> emails) {
        if (CollectionUtils.isNotEmpty(emails)) {
            return userMapper.selectByEmails(emails);
        } else {
            return Lists.newArrayList();
        }
    }

    @Override
    public Integer update(UserDTO userDTO) {
        Preconditions.checkArgument(null != userDTO);
        User user = transform(userDTO);
        return userMapper.update(user);
    }

    @Override
    public UserDTO getByUserId(Long userId) {
        Preconditions.checkArgument(null != userId);
        User user = userMapper.selectByPrimaryKey(userId);
        return do2Dto(user);
    }

    @Override
    public List<UserSimpleDTO> searchUser(Long domainId, Long appId, String orderBy, String orderRule, String search) {
        RoleTypeEnum roleTypeEnum = null;
        Long typeId = null;
        if (domainId != null) {
            roleTypeEnum = RoleTypeEnum.DOMAIN;
            typeId = domainId;
        }

        if (appId != null) {
            roleTypeEnum = RoleTypeEnum.APP;
            typeId = appId;
        }

        if (StringUtils.isNotBlank(orderBy)) {
            // u. 是 sql 里表名的前缀
            orderBy = "u." + orderBy;
        }

        Integer roleType = null;
        if (roleTypeEnum != null) {
            roleType = roleTypeEnum.getCode();
        }

        List<User> userList = userMapper.selectByRange(roleType, typeId, null, orderBy, orderRule, search);
        return userList.stream().map(this::do2SimpleDto).collect(Collectors.toList());
    }

    private UserDTO do2Dto(User user) {
        if (null == user) {
            return null;
        }
        return BeanConvertUtils.convert(user, UserDTO.class, true);
    }


    private UserSimpleDTO do2SimpleDto(User user) {
        if (null == user) {
            return null;
        }
        return BeanConvertUtils.convert(user, UserSimpleDTO.class, true);
    }

    private User transform(UserDTO userDto) {
        if (null == userDto) {
            return null;
        }
        return BeanConvertUtils.convert(userDto, User.class, true);
    }
}
