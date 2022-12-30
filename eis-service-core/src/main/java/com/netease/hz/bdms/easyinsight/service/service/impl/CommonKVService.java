package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.netease.hz.bdms.easyinsight.dao.CommonKVMapper;
import com.netease.hz.bdms.easyinsight.dao.model.CommonKV;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * 基于DB的通用KV服务，适合简单存点少量数据
 */
@Service
public class CommonKVService {

    @Resource
    private CommonKVMapper commonKVMapper;

    @Transactional(rollbackFor = Throwable.class)
    public void set(String code, String k, String v) {
        if (v == null) {
            return;
        }
        CommonKV commonKV = commonKVMapper.get(code, k);
        if (commonKV == null) {
            commonKVMapper.insert(new CommonKV().setK(k).setV(v).setCode(code));
            return;
        }
        commonKVMapper.updateValue(commonKV.setV(v));
    }

    public String get(String code, String k) {
        CommonKV commonKV = commonKVMapper.get(code, k);
        if (commonKV == null) {
            return null;
        }
        return commonKV.getV();
    }

    public Map<String, String> multiGet(String code, Set<String> kSet) {
        if (CollectionUtils.isEmpty(kSet)) {
            return new HashMap<>();
        }
        List<CommonKV> gets = commonKVMapper.gets(code, kSet);
        if (CollectionUtils.isEmpty(gets)) {
            return new HashMap<>();
        }
        Map<String, String> result = new HashMap<>();
        gets.forEach(commonKV -> {
            if (commonKV != null && commonKV.getK() != null && commonKV.getV() != null) {
                result.put(commonKV.getK(), commonKV.getV());
            }
        });
        return result;
    }
}
