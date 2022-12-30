package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.Image;
import com.netease.hz.bdms.easyinsight.dao.model.ImageRelation;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRelationMapper {

  /**
   * 插入图片与实体的关联记录
   *
   * @param imageRelation 图片与实体的关联记录
   */
  Integer insert(ImageRelation imageRelation);

  Integer batchInsert(@Param("imageRelations") Collection<ImageRelation> imageRelations);

  Integer update(ImageRelation imageRelation);

  /**
   * 查询图片与实体的关联记录
   *
   * @param id 关联ID
   * @return 图片与实体的关联记录
   */
  Image selectByPrimaryKey(Long id);

  /**
   * 删除图片与实体的关联记录
   *
   * @param id 关联ID
   * @return 删除的记录数
   */
  Integer delete(Long id);


  List<ImageRelation> selectByEntityId(@Param("entityIds") Collection<Long> entityIds);

  Integer deleteByEntityId(@Param("entityIds") Collection<Long> entityIds,
      @Param("entityType") Integer entityType);
}
