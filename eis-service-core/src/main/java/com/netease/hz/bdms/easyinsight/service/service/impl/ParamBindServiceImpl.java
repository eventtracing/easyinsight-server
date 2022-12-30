package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.ParamBindMapper;
import com.netease.hz.bdms.easyinsight.dao.model.ParamBind;
import com.netease.hz.bdms.easyinsight.service.service.ParamBindService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ParamBindServiceImpl implements ParamBindService {

    private static final Map<String, String> orderByMap = ImmutableMap
            .of("createTime", "create_time", "updateTime", "update_time");
    private static final Map<String, String> orderRuleMap = ImmutableMap
            .of("descend", "desc", "ascend", "asc");
    @Autowired
    private ParamBindMapper paramBindMapper;

    private ParamBindSimpleDTO do2Dto(ParamBind paramBind) {
        ParamBindSimpleDTO paramBindSimpleDTO = BeanConvertUtils
                .convert(paramBind, ParamBindSimpleDTO.class);
        if (paramBindSimpleDTO != null) {
            UserSimpleDTO updater = new UserSimpleDTO(paramBind.getUpdateEmail(),
                    paramBind.getUpdateName());
            UserSimpleDTO creator = new UserSimpleDTO(paramBind.getCreateEmail(),
                    paramBind.getCreateName());

            paramBindSimpleDTO.setCreator(creator)
                    .setUpdater(updater);
        }
        return paramBindSimpleDTO;
    }

    private ParamBind dto2Do(ParamBindSimpleDTO paramBindSimpleDTO) {
        ParamBind paramBind = BeanConvertUtils.convert(paramBindSimpleDTO, ParamBind.class);
        if (paramBind != null) {
            UserSimpleDTO updater = paramBindSimpleDTO.getUpdater();
            UserSimpleDTO creator = paramBindSimpleDTO.getCreator();

            if (creator != null) {
                paramBind.setCreateEmail(creator.getEmail())
                        .setCreateName(creator.getUserName());
            }
            if (updater != null) {
                paramBind.setUpdateEmail(updater.getEmail())
                        .setUpdateName(updater.getUserName());
            }
        }
        return paramBind;
    }

    /**
     * 参数绑定对象集合
     *
     * @param entityIds   关联元素ID，必填元素
     * @param entityTypes 关联元素类型集合，必填元素
     * @param versionId   版本ID，非必填元素
     * @param appId       产品ID，必填元素
     * @return 参数绑定对象
     */
    @Override
    public List<ParamBindSimpleDTO> getByEntityIds(Collection<Long> entityIds,
                                                   Collection<Integer> entityTypes,
                                                   Long versionId, Long appId) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(entityIds)
                        && CollectionUtils.isNotEmpty(entityTypes), "参数绑定元素信息不能为空");

        List<ParamBind> paramBinds = paramBindMapper
                .selectByEntityId(entityIds, entityTypes, versionId, appId);

        return paramBinds.stream().map(this::do2Dto).collect(Collectors.toList());
    }

    @Override
    public List<ParamBindSimpleDTO> getByAppId(Long appId){
        // 批量查询
        ParamBind query = new ParamBind();
        query.setAppId(appId);
        List<ParamBind> paramBinds = paramBindMapper.select(query);
        return paramBinds.stream().map(this::do2Dto).collect(Collectors.toList());
    }

    /**
     * 参数绑定对象集合
     *
     * @param entityIds   关联元素ID，必填元素
     * @param entityTypes 关联元素类型集合，必填元素
     * @param versionId   版本ID，非必填元素
     * @param appId       产品ID，必填元素
     * @return 参数绑定对象
     */
    @Override
    public Integer getParamBindSizeByEntityIds(Collection<Long> entityIds, Collection<Integer> entityTypes,
                                               Long versionId, Long appId) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(entityIds)
                        && CollectionUtils.isNotEmpty(entityTypes), "参数绑定元素信息不能为空");

        return paramBindMapper.selectSizeByEntityId(entityIds, entityTypes, versionId, appId);
    }

    @Override
    public List<Long> getParamBindIdsByEntityIds(Collection<Long> entityIds, Integer entityType,
                                                 Long versionId, Long appId) {
        Preconditions
                .checkArgument(CollectionUtils.isNotEmpty(entityIds) && null != entityType,
                        "参数绑定元素信息不能为空");
        Preconditions.checkArgument(null != appId, "产品ID不能为空");

        return paramBindMapper
                .selectIdByEntityId(entityIds, entityType, versionId, appId);
    }

    @Override
    public ParamBindSimpleDTO getById(Long paramBindId) {
        Preconditions.checkArgument(null != paramBindId, "参数绑定ID不能为空");

        ParamBind paramBind = paramBindMapper.selectByPrimaryKey(paramBindId);
        return do2Dto(paramBind);
    }

    /**
     * 查询指定参数的参数绑定情况
     *
     * @param paramIds   参数ID， 必填参数
     * @param entityType 绑定元素类型
     * @return 参数绑定列表
     */
    @Override
    public List<ParamBindSimpleDTO> getParamBindByParamId(List<Long> paramIds, Integer entityType) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(paramIds), "参数ID不能为空");

        List<ParamBind> paramBinds = paramBindMapper.selectParamBindByParamId(paramIds, entityType);
        return paramBinds.stream().map(this::do2Dto).collect(Collectors.toList());
    }

    @Override
    public Long createParamBind(ParamBindSimpleDTO param) {
        Preconditions.checkArgument(null != param, "参数绑定信息不能为空");
        Preconditions.checkArgument(param.getParamId() != 0L, "绑定的参数ID不能为空");
        // 设置创建人、更新人信息
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        String userName = currUser.getUserName();
        String userEmail = currUser.getEmail();
        // 数据转化
        ParamBind paramBind = dto2Do(param);
        if (paramBind == null) {
            throw new CommonException("paramBind为空");
        }
        paramBind.setCreateName(userName);
        paramBind.setCreateEmail(userEmail);
        paramBind.setUpdateName(userName);
        paramBind.setUpdateEmail(userEmail);

        paramBindMapper.insert(paramBind);
        return paramBind.getId();
    }


    @Override
    public Integer updateParamBind(ParamBindSimpleDTO param) {
        ParamBind paramBind = dto2Do(param);
        Preconditions.checkArgument(null != paramBind, "参数绑定对象不能为空");
        Preconditions.checkArgument(null != paramBind.getId(), "参数绑定主键ID不能为空");

        return paramBindMapper.update(paramBind);
    }

    @Override
    public Integer deleteParamBind(Long paramBindId) {
        Preconditions.checkArgument(null != paramBindId, "终端绑定ID不能为空");
        return paramBindMapper.delete(paramBindId);
    }

    /**
     * 删除指定元素的参数绑定信息
     *
     * @param entityId   元素ID，必填参数
     * @param entityType 元素类型，必填参数
     * @param versionId  版本ID， 非必填参数
     * @param appId      产品ID，必填参数
     * @return 删除的记录数
     */
    @Override
    public Integer deleteParamBind(Long entityId, Integer entityType, Long versionId, Long appId) {
        Preconditions.checkArgument(null != entityId && null != entityType, "关联元素不能为空");
        Preconditions.checkArgument(null != appId, "产品ID不能为空");

        return paramBindMapper.deleteByEntityId(entityId, entityType, versionId, appId);
    }

    @Override
    public Integer searchParamBindSize(Long entityId, Integer entityType, Long versionId,
                                       Long appId) {
        return paramBindMapper.searchParamBindSize(entityId, entityType, versionId, appId);
    }

    @Override
    public List<ParamBindSimpleDTO> searchParamBind(Long entityId, Integer entityType, Long versionId,
                                                    Long appId, String orderBy, String orderRule, Integer offset, Integer pageSize) {
        String dbOrderBy = orderByMap.get(orderBy);
        String dbOrderRule = orderRuleMap.get(orderRule);

        List<ParamBind> terminals = paramBindMapper
                .searchParamBind(entityId, entityType, versionId, appId, dbOrderBy, dbOrderRule, offset,
                        pageSize);
        return terminals.stream().map(this::do2Dto).collect(Collectors.toList());
    }

    @Override
    public Integer deleteByIds(Set<Long> ids) {
        if(CollectionUtils.isNotEmpty(ids)) {
            return paramBindMapper.deleteByIds(ids);
        }
        return  0;
    }

}
