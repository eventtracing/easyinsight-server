package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.common.query.Search;
import com.netease.hz.bdms.easyinsight.dao.model.ObjectBasic;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/12/9 14:39
 */
@Repository
public interface ObjectBasicMapper {

    /**
     * 插入对象基本信息
     *
     * @param objectBasic 对象基本信息
     * @return
     */
    Integer insert(ObjectBasic objectBasic);


    /**
     * 指定条件进行查询
     *
     * @param query 查询条件
     * @return
     */
    List<ObjectBasic> search(ObjectBasic query);

    /**
     * 模糊查询
     * @param search
     * @return
     */
    List<ObjectBasic> searchLike(Search search);

    /**
     * 给定产品ID和对象oid集合，查询对应的对象基本信息
     *
     * @param appId 产品ID
     * @param oids  对象oid集合
     * @return      对象基本信息集合
     */
    List<ObjectBasic> selectByOids(@Param("appId") Long appId, @Param("oids") Collection<String> oids);

    /**
     * 列出指定type的所有数据
     */
    List<ObjectBasic> selectByType(@Param("type") Integer type);

    List<ObjectBasic> selectBySpecialType(@Param("specialType") String specialType);

    List<ObjectBasic> selectByLikeOid(@Param("appId") Long appId, @Param("oid") String oid);

    List<ObjectBasic> selectAllByAppId(@Param("appId") Long appId);

    /**
     * 按优先级查询
     *
     * @param appId 产品ID
     * @param priorities  对象oid集合
     * @return      对象基本信息集合
     */
    List<ObjectBasic> selectByPriorities(@Param("appId") Long appId, @Param("priorities") Collection<String> priorities);


    /**
     * 给定产品ID和对象名称集合，查询对应的对象基本信息
     *
     * @param appId 产品ID
     * @param names 对象名称集合
     * @return      对象基本信息集合
     */
    List<ObjectBasic> selectByNames(@Param("appId") Long appId, @Param("names") Collection<String> names);


    /**
     * 按主键 批量查询
     *
     * @param objectIds 对象基本信息ID集合
     * @return
     */
    List<ObjectBasic> selectByIds(@Param("ids") Collection<Long> objectIds);

    /**
     * 按主键 批量查询appIds
     *
     * @param objectIds 对象基本信息ID集合
     * @return
     */
    List<Long> selectDistinctAppIds(@Param("ids") Collection<Long> objectIds);

    /**
     * 主键查询
     *
     * @param id
     * @return
     */
    ObjectBasic selectById(Long id);


    /**
     * 更新对象基本信息（只支持部分字段的更新）
     *
     * @param objectBasic 对象基本信息
     */
    void update(ObjectBasic objectBasic);

    /**
     * 根据主键ID，批量删除
     *
     * @param ids 主键集合
     * @return 删除数量
     */
    Integer deleteByIds(@Param("ids") Collection<Long> ids);

    void insertBatch(@Param("list") List<ObjectBasic> list);

    /**
     * 流量罗盘查询对象列表
     * @param search
     * @return
     */
    List<ObjectBasic> searchLike4Compass(Search search);
}
