package com.netease.hz.bdms.easyinsight.service.facade;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageSerializable;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingResultDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingSortDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.common.util.CheckUtils;
import com.netease.hz.bdms.easyinsight.dao.model.rbac.User;
import com.netease.hz.bdms.easyinsight.service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
public class UserFacade {

    @Autowired
    private UserService userService;

    public Long createUser(UserSimpleDTO userSimpleDTO) {
        Preconditions.checkArgument(null != userSimpleDTO, "用户不能为空");
        String email = userSimpleDTO.getEmail();
        String userName = userSimpleDTO.getUserName();
        Preconditions.checkArgument(StringUtils.isNotBlank(email) && CheckUtils
                .isEmail(email), "用户邮箱不能为空, 且应满足邮箱规范");
        Preconditions.checkArgument(StringUtils.isNotBlank(userName), "中文名称不能为空");

        User existsUser = userService.getByEmail(email);
        Preconditions.checkArgument(null == existsUser, "当前用户已存在，添加失败");
        return userService.create(email, userName);
    }

    public Integer updateUser(UserSimpleDTO userSimpleDTO) {
        Preconditions.checkArgument(null != userSimpleDTO, "用户不能为空");
        String email = userSimpleDTO.getEmail();
        String userName = userSimpleDTO.getUserName();
        Preconditions.checkArgument(StringUtils.isNotBlank(email) && CheckUtils
                .isEmail(email), "用户邮箱不能为空, 且应满足邮箱规范");
        Preconditions.checkArgument(StringUtils.isNotBlank(userName), "中文名称不能为空");

        User existsUser = userService.getByEmail(email);
        Preconditions.checkArgument(null != existsUser, "用户不存在，修改失败");

        // TODO: 修改其他表中存在的用户
        Long domainId = EtContext.get(ContextConstant.DOMAIN_ID);

        UserDTO userDTO = BeanConvertUtils.convert(userSimpleDTO, UserDTO.class);
        return userService.update(userDTO);
    }

    public Integer deleteUser(String email) {
        Preconditions.checkArgument(StringUtils.isNotBlank(email), "用户不能为空");

        // TODO: 检查其他表是否有此用户
        Long domainId = EtContext.get(ContextConstant.DOMAIN_ID);

//    UserDTO userDTO = BeanConvertUtils.convert(userSimpleDTO, UserDTO.class);
//    return  userService.update(userDTO);
        return null;
    }

    public List<UserSimpleDTO> getUser() {
        Long domainId = EtContext.get(ContextConstant.DOMAIN_ID);
        Long appId = EtContext.get(ContextConstant.APP_ID);
        return userService.searchUser(domainId, appId, null, null, null);
    }

    public PagingResultDTO<UserSimpleDTO> listUser(String search, PagingSortDTO pagingSortDTO) {
        Integer currentPage = pagingSortDTO.getCurrentPage();

        PagingResultDTO<UserSimpleDTO> result = new PagingResultDTO<>();
        result.setPageNum(currentPage);
        result.setTotalNum(0);
        result.setList(Lists.newArrayList());

        // 设置分页参数
        PageHelper.startPage(currentPage, pagingSortDTO.getPageSize());

        Long domainId = EtContext.get(ContextConstant.DOMAIN_ID);
        Long appId = EtContext.get(ContextConstant.APP_ID);

        List<UserSimpleDTO> userSimpleDTOList = userService.searchUser(domainId, appId,
                pagingSortDTO.getOrderBy(), pagingSortDTO.getOrderRule(), search);
        if (CollectionUtils.isNotEmpty(userSimpleDTOList)) {
            PageSerializable<UserSimpleDTO> userPageSerializable = PageSerializable.of(userSimpleDTOList);
            result.setTotalNum(Long.valueOf(userPageSerializable.getTotal()).intValue());
            result.setList(userSimpleDTOList);
        }

        return result;
    }

}
