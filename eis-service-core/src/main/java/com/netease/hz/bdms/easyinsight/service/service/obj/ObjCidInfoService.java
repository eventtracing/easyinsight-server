package com.netease.hz.bdms.easyinsight.service.service.obj;

import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.param.tag.CidTagInfo;
import com.netease.hz.bdms.easyinsight.dao.EisCidInfoMapper;
import com.netease.hz.bdms.easyinsight.dao.model.CidInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ObjCidInfoService {

    private static final String BIND_TYPE = "OBJECT";

    @Resource
    private EisCidInfoMapper eisCidInfoMapper;

    public List<CidTagInfo> getCidTagInfos(Long appId, Long objId) {
        Map<String, String> m = listAll(appId, objId);
        if (MapUtils.isEmpty(m)) {
            return new ArrayList<>(0);
        }
        return m.entrySet().stream().map(o -> new CidTagInfo().setCid(o.getKey()).setName(o.getValue())).collect(Collectors.toList());
    }

    public Map<String, String> listAll(Long appId, Long objId) {
        if (appId == null || objId == null) {
            return new HashMap<>();
        }
        List<CidInfo> cidInfos = eisCidInfoMapper.listAll(BIND_TYPE, String.valueOf(objId), appId);
        if (CollectionUtils.isEmpty(cidInfos)) {
            return new HashMap<>();
        }
        Map<String, String> result = new HashMap<>();
        cidInfos.forEach(cidInfo -> {
            result.put(cidInfo.getCid(), cidInfo.getCidName());
        });
        return result;
    }

    public String getCidName(Long appId, Long objId, String cid) {
        if (appId == null || objId == null || StringUtils.isBlank(cid)) {
            return null;
        }
        CidInfo cidInfo = eisCidInfoMapper.get(BIND_TYPE, String.valueOf(objId), appId, cid);
        if (cidInfo == null) {
            return null;
        }
        return cidInfo.getCidName();
    }

    public void add(Long appId, Long objId, String cid, String name) {
        if (appId == null || objId == null || StringUtils.isBlank(cid)) {
            throw new CommonException("绑定CID名时，参数无效");
        }
        eisCidInfoMapper.insert(new CidInfo()
                .setCid(cid)
                .setCidName(name)
                .setAppId(appId)
                .setExt("{}")
                .setTarget(String.valueOf(objId))
                .setBindType(BIND_TYPE)
        );
    }

    @Transactional
    public void update(Long appId, Long objId, List<CidTagInfo> cidTagInfos) {
        // 1. 删除老的
        List<CidInfo> origins = eisCidInfoMapper.listAll(BIND_TYPE, String.valueOf(objId), appId);
        if (CollectionUtils.isNotEmpty(origins)) {
            List<Long> idsToDelete = origins.stream().map(CidInfo::getId).collect(Collectors.toList());
            eisCidInfoMapper.deleteByIds(idsToDelete);
        }
        // 2. 写入新的
        if (CollectionUtils.isNotEmpty(cidTagInfos)) {
            List<CidInfo> toInsert = cidTagInfos.stream().map(o -> {
                CidInfo cidInfo = new CidInfo();
                cidInfo.setAppId(appId);
                cidInfo.setTarget(String.valueOf(objId));
                cidInfo.setBindType(BIND_TYPE);
                cidInfo.setCid(o.getCid());
                cidInfo.setCidName(o.getName());
                cidInfo.setExt("{}");
                return cidInfo;
            }).collect(Collectors.toList());
            eisCidInfoMapper.batchInsert(toInsert);
        }
    }
}
