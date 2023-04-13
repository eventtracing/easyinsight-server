package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindValueSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.ParamBindValueMapper;
import com.netease.hz.bdms.easyinsight.dao.model.ParamBindValue;
import com.netease.hz.bdms.easyinsight.service.service.ParamBindValueService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ParamBindValueServiceImpl implements ParamBindValueService {

    @Autowired
    private ParamBindValueMapper paramBindValueMapper;


    private ParamBindValueSimpleDTO do2Dto(ParamBindValue paramBindValue) {
        ParamBindValueSimpleDTO paramBindValueSimpleDTO = BeanConvertUtils
                .convert(paramBindValue, ParamBindValueSimpleDTO.class);
        if (paramBindValueSimpleDTO != null) {
            UserSimpleDTO updater = new UserSimpleDTO(paramBindValue.getUpdateEmail(),
                    paramBindValue.getUpdateName());
            UserSimpleDTO creator = new UserSimpleDTO(paramBindValue.getCreateEmail(),
                    paramBindValue.getCreateName());

            paramBindValueSimpleDTO.setCreator(creator)
                    .setUpdater(updater);
        }
        return paramBindValueSimpleDTO;
    }

    private ParamBindValue dto2Do(ParamBindValueSimpleDTO paramBindValueSimpleDTO) {
        ParamBindValue paramBindValue = BeanConvertUtils
                .convert(paramBindValueSimpleDTO, ParamBindValue.class);
        if (paramBindValue != null) {
            UserSimpleDTO updater = paramBindValueSimpleDTO.getUpdater();
            UserSimpleDTO creator = paramBindValueSimpleDTO.getCreator();

            if (creator != null) {
                paramBindValue.setCreateEmail(creator.getEmail())
                        .setCreateName(creator.getUserName());
            }
            if (updater != null) {
                paramBindValue.setUpdateEmail(updater.getEmail())
                        .setUpdateName(updater.getUserName());
            }
        }
        return paramBindValue;
    }


    @Override
    public void createParamBindValue(List<ParamBindValueSimpleDTO> paramBindValueSimpleDTOS) {
        if (CollectionUtils.isNotEmpty(paramBindValueSimpleDTOS)) {
            UserDTO currUser = EtContext.get(ContextConstant.USER);
            Long appId = EtContext.get(ContextConstant.APP_ID);
            Preconditions.checkArgument(null != currUser, "无当前用户信息");
            Preconditions.checkArgument(null != appId, "未指定产品信息");

            List<ParamBindValue> paramBindValues = Lists.newArrayList();
            for (ParamBindValueSimpleDTO paramBindValueSimpleDTO : paramBindValueSimpleDTOS) {
                // 数据转化
                ParamBindValue paramBindValue = dto2Do(paramBindValueSimpleDTO);
                if (paramBindValue == null) {
                    throw new CommonException("paramBindValue为空");
                }
                // 公共信息设置
                paramBindValue.setCreateName(currUser.getUserName());
                paramBindValue.setCreateEmail(currUser.getEmail());
                paramBindValue.setUpdateName(currUser.getUserName());
                paramBindValue.setUpdateEmail(currUser.getEmail());
                paramBindValue.setCreateTime(new Timestamp(System.currentTimeMillis()));
                paramBindValue.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                paramBindValue.setAppId(appId);
                paramBindValue.setDescription("");

                paramBindValues.add(paramBindValue);
            }
            paramBindValueMapper.batchInsert(paramBindValues);
        }
    }

    @Override
    public Integer deleteByBindIds(List<Long> bindIds) {
        if(CollectionUtils.isNotEmpty(bindIds)){
            return paramBindValueMapper.deleteByBindIds(bindIds);
        }
        return 0;
    }

    @Override
    public Integer deleteByBindId(Long bindId) {
        Preconditions.checkArgument(null != bindId, "参数绑定ID不能为空");
        return paramBindValueMapper.deleteByBindId(bindId);
    }

    @Override
    public List<ParamBindValueSimpleDTO> getByBindIds(Set<Long> bindIds) {
        if (CollectionUtils.isNotEmpty(bindIds)) {
            List<ParamBindValue> paramBindValues = paramBindValueMapper.selectByBindIds(bindIds);
            return paramBindValues.stream().map(this::do2Dto).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    /**
     * 判断指定的参数值集合是否被使用
     *
     * @param paramValueIds 参数值ID集合
     * @return 参数值的使用情况，key表示参数值ID，value表示是否使用，true即使用，false即未使用
     */
    @Override
    public Map<Long, Boolean> getParamValueUsed(Collection<Long> paramValueIds) {
        Map<Long, Boolean> result = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(paramValueIds)) {
            List<ParamBindValue> paramBinds = paramBindValueMapper.selectByParamValueIds(paramValueIds);
            Set<Long> usedParamValueIds = paramBinds.stream().map(ParamBindValue::getParamValueId).collect(Collectors.toSet());

            for (Long paramValueId : paramValueIds) {
                if (usedParamValueIds.contains(paramValueId)) {
                    result.put(paramValueId, true);
                } else {
                    result.put(paramValueId, false);
                }
            }
        }
        return result;
    }
}
