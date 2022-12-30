package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.common.vo.logcheck.BranchCoverageDetailVO;
import com.netease.hz.bdms.easyinsight.dao.EisRealtimeBranchIgnoreMapper;
import com.netease.hz.bdms.easyinsight.dao.model.EisRealtimeBranchIgnore;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RealtimeBranchIgnoreService {

    @Resource
    private EisRealtimeBranchIgnoreMapper eisRealtimeBranchIgnoreMapper;

    public void removeAll(String conversationId) {
        eisRealtimeBranchIgnoreMapper.removeAll(conversationId);
    }

    public List<BranchCoverageDetailVO> listAll(String conversationId) {
        if (StringUtils.isBlank(conversationId)) {
            return new ArrayList<>(0);
        }
        List<EisRealtimeBranchIgnore> eisRealtimeBranchIgnores = eisRealtimeBranchIgnoreMapper.listAll(conversationId);
        if (CollectionUtils.isEmpty(eisRealtimeBranchIgnores)) {
            return new ArrayList<>(0);
        }
        return eisRealtimeBranchIgnores.stream().map(o -> {
            if (o == null || o.getBranchKey() == null) {
                return null;
            }
            String[] split = o.getBranchKey().split("::");
            if (split.length != 4) {
                return null;
            }
            BranchCoverageDetailVO vo = new BranchCoverageDetailVO();
            vo.setSpm(split[0]);
            vo.setEventCode(split[1]);
            vo.setParamCode(split[2]);
            vo.setParamValue(split[3]);
            return vo;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void add(String conversationId, List<BranchCoverageDetailVO> branches) {
        UserDTO user = EtContext.get(ContextConstant.USER);
        if (user == null || StringUtils.isBlank(user.getEmail())) {
            throw new CommonException("未登陆");
        }
        if (CollectionUtils.isEmpty(branches) || StringUtils.isBlank(conversationId)) {
            return;
        }
        branches = branches.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(branches)) {
            return;
        }
        Set<String> oldIgnoreKeys = listAll(conversationId).stream().map(RealtimeBranchIgnoreService::getIgnoreKey).collect(Collectors.toSet());
        List<EisRealtimeBranchIgnore> ignoreList = branches.stream().map(o -> {
            // 过滤已有的
            String ignoreKey = getIgnoreKey(o);
            if (oldIgnoreKeys.contains(ignoreKey)) {
                return null;
            }
            EisRealtimeBranchIgnore ignore = new EisRealtimeBranchIgnore();
            ignore.setConversationId(conversationId);
            ignore.setBranchKey(ignoreKey);
            ignore.setContent(JsonUtils.toJson(new IgnoreInfo().setEmail(user.getEmail())));
            return ignore;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(ignoreList)) {
            return;
        }
        eisRealtimeBranchIgnoreMapper.batchInsert(ignoreList);
    }

    private static String getIgnoreKey(BranchCoverageDetailVO branchCoverageDetailVO) {
        return branchCoverageDetailVO.getSpm() + "::" + branchCoverageDetailVO.getEventCode() + "::" + branchCoverageDetailVO.getParamCode() + "::" + branchCoverageDetailVO.getParamValue();
    }

    @Accessors(chain = true)
    @Data
    public static class IgnoreInfo {
        private String email;
    }
}
