package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.query.Search;
import com.netease.hz.bdms.easyinsight.dao.model.ObjMappings;
import com.netease.hz.bdms.easyinsight.dao.model.ObjectBasic;

import java.util.Collection;
import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/12/9 14:14
 */
public interface ObjectBasicService {

    /**
     * 返回各种对象映射关系
     * @param appId 把appId下所有都查出来
     * @param appId 把这些objId都查出来下所有都查出来
     * @return
     */
    ObjMappings getMapping(Long appId, Collection<Long> objIds);

    /**
     * 按条件进行查询
     *
     * @param query 查询条件
     * @return
     */
    List<ObjectBasic> search(ObjectBasic query);

    /**
     * 给定产品Id和对象oid集合，查询对应的对象基本信息
     *
     * @param appId 产品ID
     * @param oids  对象oid集合
     * @return      ObjectBasicDTO列表
     */
    List<ObjectBasic> getByOids(Long appId, Collection<String> oids);

    List<ObjectBasic> getBySpecialType(String specialType);

    List<ObjectBasic> getByLikeOid(Long appId, String oid);

    /**
     * 给定产品Id和对象oid，查询对应的对象基本信息
     *
     * @param appId 产品ID
     * @param oid   oid
     * @return      ObjectBasic列表
     */
    ObjectBasic getByOid(Long appId, String oid);

    /**
     * 按优先级查询
     *
     * @param appId 产品ID
     * @param priority   priority
     * @return      ObjectBasic列表
     */
    List<ObjectBasic> listAllByPriority(Long appId, String priority);


    /**
     * 给定产品ID和对象名称集合，查询对应的对象基本信息
     *
     * @param appId 产品ID
     * @param names 对象名称集合
     * @return      ObjectBasicDTO列表
     */
    List<ObjectBasic> getByNames(Long appId, Collection<String> names);

    /**
     * 依据主键查询
     *
     * @param objId 主键ID
     * @return
     */
    ObjectBasic getById(Long objId);


    /**
     * 依据对象主键ID集合，批量查询
     *
     * @param objIds  对象主键ID集合
     * @return        ObjectBasicDTO列表
     */
        List<ObjectBasic> getByIds(Collection<Long> objIds);


    /**
     * 插入对象基本信息
     *
     * @param objectBasic 对象基本信息
     * @return               对象主键
     */
    Long insert(ObjectBasic objectBasic);


    /**
     * 更新对象基本信息
     *
     * @param objectBasic 待更新的对象基本信息
     */
    void update(ObjectBasic objectBasic);


    /**
     * 按主键 批量删除
     *
     * @param ids 主键集合
     * @return
     */
    Integer deleteByIds(Collection<Long> ids);

    /**
     * 批量插入
     * @param list
     */
    void insertBatch(List<ObjectBasic> list);

    /**
     * 模糊查询
     * @return
     */
    List<ObjectBasic> searchLike(Search search);

    List<String> getAllOids(Long appId);

    List<Long> getAllAppIds(Collection<Long> objIds);

    /**
     * 流量罗盘用来查询对象列表
     * @param search
     * @return
     */
    List<ObjectBasic> searchLike4Compass(Search search);
}
