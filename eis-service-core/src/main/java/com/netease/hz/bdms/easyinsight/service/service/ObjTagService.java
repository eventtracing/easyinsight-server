package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.tag.ObjTagSimpleDTO;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface ObjTagService {

  List<ObjTagSimpleDTO> getByTagIds(Set<Long> tagIds);

  ObjTagSimpleDTO getByObjectIdAndTagId(Long objId, Long tagId);

  void createBatch(Collection<ObjTagSimpleDTO> objTags);

  Integer deleteObjTagByObjIds(Collection<Long> objIds);

  List<ObjTagSimpleDTO> selectObjTags(Collection<Long> tagIds, Collection<Long> historyIds, Collection<Long> objIds);

  /**
   * 获取对象关联的标签信息
   *
   * @param objIds 对象ids
   * @return
   */
  List<ObjTagSimpleDTO> getByObjIds(Set<Long> objIds);

  List<ObjTagSimpleDTO> listAll();

  /**
   * 测试用方法
   */
  void check();
}
