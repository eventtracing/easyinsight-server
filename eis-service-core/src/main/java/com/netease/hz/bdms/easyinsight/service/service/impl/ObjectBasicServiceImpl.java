package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.netease.eis.adapters.CacheAdapter;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.ListHolder;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.exception.ObjException;
import com.netease.hz.bdms.easyinsight.common.query.Search;
import com.netease.hz.bdms.easyinsight.common.util.CacheUtils;
import com.netease.hz.bdms.easyinsight.dao.ObjectBasicMapper;
import com.netease.hz.bdms.easyinsight.dao.model.ObjectBasic;
import com.netease.hz.bdms.easyinsight.service.service.ObjectBasicService;
import com.netease.hz.bdms.easyinsight.dao.model.ObjMappings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: xumengqiang
 * @date: 2021/12/9 15:38
 */
@Slf4j
@Service
public class ObjectBasicServiceImpl implements ObjectBasicService {

    @Autowired
    private ObjectBasicMapper objectBasicMapper;

    @Resource
    private CacheAdapter cacheAdapter;

    public ObjMappings getMapping(Long appId, Collection<Long> objIds) {
        List<ObjectBasic> all = new ArrayList<>();
        if (appId != null) {
            ObjectBasic objQuery = new ObjectBasic();
            objQuery.setAppId(appId);
            all.addAll(search(objQuery));
        }

        if (CollectionUtils.isNotEmpty(objIds)) {
            Set<Long> allObjIds = all.stream().map(ObjectBasic::getId).collect(Collectors.toSet());
            Set<Long> allNeedObjIds = new HashSet<>(objIds);
            allNeedObjIds.removeIf(allObjIds::contains);
            if (CollectionUtils.isNotEmpty(allNeedObjIds)) {
                List<ObjectBasic> missingOnes = getByIds(allNeedObjIds);
                all.addAll(missingOnes);
            }
        }

        return getObjMappingDTO(all);
    }

    public static ObjMappings getObjMappingDTO(List<ObjectBasic> all) {
        if (CollectionUtils.isEmpty(all)) {
            return new ObjMappings();
        }

        Map<String,String> allObjNameMap = new HashMap<>();
        Map<Long,String> objIdToOidMap = new HashMap<>();
        Map<String,Long> oidToObjIdMap = new HashMap<>();
        Map<Long,String> objIdToNameMap = new HashMap<>();
        for (ObjectBasic objectBasic : all) {
            allObjNameMap.put(objectBasic.getOid(), objectBasic.getName());
            objIdToOidMap.put(objectBasic.getId(), objectBasic.getOid());
            oidToObjIdMap.put(objectBasic.getOid(), objectBasic.getId());
            objIdToNameMap.put(objectBasic.getId(), objectBasic.getName());
        }

        ObjMappings result = new ObjMappings();
        result.setAllObjNameMap(allObjNameMap);
        result.setObjIdToOidMap(objIdToOidMap);
        result.setOidToObjIdMap(oidToObjIdMap);
        result.setObjIdToNameMap(objIdToNameMap);
        result.setObjs(all);
        return result;
    }


    /**
     * 按条件进行查询
     *
     * @param query 查询条件
     * @return
     */
    @Override
    public List<ObjectBasic> search(ObjectBasic query) {
        if(null != query){
            return objectBasicMapper.search(query);
        }
        return Lists.newArrayList();
    }

    /**
     * 给定产品Id和对象oid集合，查询对应的对象基本信息
     *
     * @param appId 产品ID
     * @param oids  对象oid集合
     * @return      ObjectBasic列表
     */
    @Override
    public List<ObjectBasic> getByOids(Long appId, Collection<String> oids) {
        Preconditions.checkArgument(null != appId, "未指定产品信息！");

        if(CollectionUtils.isNotEmpty(oids)){
            return objectBasicMapper.selectByOids(appId, oids);
        }
        return Lists.newArrayList();
    }

    @Override
    public List<ObjectBasic> getBySpecialType(String specialType) {
        if (specialType == null) {
            return new ArrayList<>(0);
        }
        return objectBasicMapper.selectBySpecialType(specialType);
    }

    @Override
    public List<ObjectBasic> getByLikeOid(Long appId, String oid) {
        Preconditions.checkArgument(null != appId, "未指定产品信息！");

        if(StringUtils.isNotEmpty(oid)){
            return objectBasicMapper.selectByLikeOid(appId, oid);
        }
        return Lists.newArrayList();
    }

    /**
     * 给定产品Id和对象oid，查询对应的对象基本信息
     *
     * @param appId 产品ID
     * @param oid   oid
     * @return      ObjectBasic列表
     */
    @Override
    public ObjectBasic getByOid(Long appId, String oid) {
        Preconditions.checkArgument(null != appId, "未指定产品信息！");
        List<ObjectBasic> basics = getByOids(appId, Arrays.asList(oid));
        if (CollectionUtils.isEmpty(basics)) {
            return null;
        }
        return basics.get(0);
    }

    @Override
    public List<ObjectBasic> listAllByPriority(Long appId, String priority) {
        Preconditions.checkArgument(null != appId, "未指定产品信息！");
        return objectBasicMapper.selectByPriorities(appId, Arrays.asList(priority));
    }

    /**
     * 给定产品ID和对象名称集合，查询对应的对象基本信息
     *
     * @param appId 产品ID
     * @param names 对象名称集合
     * @return      ObjectBasic列表
     */
    @Override
    public List<ObjectBasic> getByNames(Long appId, Collection<String> names) {
        Preconditions.checkArgument(null != appId, "未指定产品信息！");

        if(CollectionUtils.isNotEmpty(names)){
            return objectBasicMapper.selectByNames(appId, names);
        }
        return Lists.newArrayList();
    }

    @Override
    public ObjectBasic getById(Long objId) {
        return objectBasicMapper.selectById(objId);
    }


    /**
     * 依据对象主键ID集合，批量查询
     *
     * @param objIds  对象主键ID集合
     * @return        ObjectBasic列表
     */
    @Override
    public List<ObjectBasic> getByIds(Collection<Long> objIds) {
        if(CollectionUtils.isNotEmpty(objIds)){
            return objectBasicMapper.selectByIds(objIds);
        }
        return Lists.newArrayList();
    }


    /**
     * 插入对象基本信息
     *
     * @param objectBasic 对象基本信息
     * @return               对象主键
     */
    @Override
    public Long insert(ObjectBasic objectBasic) {
        Preconditions.checkArgument(null != objectBasic, "对象基本信息不能为空！");

        // 信息填充
        UserDTO currentUser= EtContext.get(ContextConstant.USER);
        Long appId = EtContext.get(ContextConstant.APP_ID);

        if(null != currentUser) {
            objectBasic.setCreateName(currentUser.getUserName())
                    .setCreateEmail(currentUser.getEmail())
                    .setUpdateName(currentUser.getUserName())
                    .setUpdateEmail(currentUser.getEmail());
        }
        if(null != appId){
            objectBasic.setAppId(appId);
        }
        // 插入记录
        try{
            // 尝试插入对象基本信息
            objectBasicMapper.insert(objectBasic);
        }catch (DuplicateKeyException e){
            // 唯一键冲突
            log.error("the object with oid={} or name={} already exists!",
                    objectBasic.getOid(), objectBasic.getName());
            throw new ObjException("对象" + objectBasic.getOid() + "的oid或名称已存在");
        }
        return objectBasic.getId();
    }


    /**
     * 更新对象基本信息
     *
     * @param objectBasic 待更新的对象基本信息
     */
    @Override
    public void update(ObjectBasic objectBasic) {
        if (objectBasic == null || objectBasic.getId() == null) {
            throw new CommonException("对象更新信息不能为空且对象ID不能为空");
        }
        UserDTO currUser = EtContext.get(ContextConstant.USER);

        if(null != currUser){
            objectBasic.setUpdateName(currUser.getUserName())
                    .setUpdateEmail(currUser.getEmail());
        }
        objectBasicMapper.update(objectBasic);
    }

    /**
     * 根据主键ID 批量删除
     *
     * @param ids 主键集合
     * @return
     */
    @Override
    public Integer deleteByIds(Collection<Long> ids) {
        if(CollectionUtils.isNotEmpty(ids)){
            return objectBasicMapper.deleteByIds(ids);
        }
        return 0;
    }

    @Override
    public void insertBatch(List<ObjectBasic> list) {
        if(CollectionUtils.isNotEmpty(list)){
            objectBasicMapper.insertBatch(list);
        }
    }

    @Override
    public List<ObjectBasic> searchLike(Search search) {
        return Optional.ofNullable(objectBasicMapper.searchLike(search)).orElse(new ArrayList<>());
    }

    @Override
    public List<Long> getAllAppIds(Collection<Long> objIds) {
        if (CollectionUtils.isEmpty(objIds)) {
            return new ArrayList<>(0);
        }
        return objectBasicMapper.selectDistinctAppIds(objIds);
    }

    /**
     * 流量罗盘用来查询对象列表
     *
     * @param search
     * @return
     */
    @Override
    public List<ObjectBasic> searchLike4Compass(Search search) {
        return Optional.ofNullable(objectBasicMapper.searchLike4Compass(search)).orElse(new ArrayList<>());
    }

    @Override
    public List<String> getAllOids(Long appId) {
        ListHolder listHolder = CacheUtils.getAndSetIfAbsent(() -> "getAllOids" + appId,
                () -> doGetAllOids(appId),
                (key) -> cacheAdapter.get(key),
                (key, value) -> cacheAdapter.setWithExpireTime(key, value, 120),
                ListHolder.class);
        return listHolder == null ? new ArrayList<>(0) : listHolder.getList();
    }

    public ListHolder doGetAllOids(Long appId) {
        List<ObjectBasic> objectBasics = objectBasicMapper.selectAllByAppId(appId);
        if (CollectionUtils.isEmpty(objectBasics)) {
            return new ListHolder().setList(new ArrayList<>(0));
        }
        return new ListHolder().setList(objectBasics.stream().map(ObjectBasic::getOid).distinct().collect(Collectors.toList()));
    }
}
