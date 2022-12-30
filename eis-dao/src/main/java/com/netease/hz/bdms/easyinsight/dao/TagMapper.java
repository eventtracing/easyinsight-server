package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.Tag;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TagMapper {
  Integer insertTag(Tag tag);

  List<Tag> selectTags(@Param("tagIds")Collection<Long> tagIds);

  List<Tag> searchTags(@Param("keyword") String keyword,
                       @Param("type") Integer type,
                       @Param("appId") Long appId,
                       @Param("offset") Integer offset,
                       @Param("count") Integer count,
                       @Param("orderBy") String orderBy,
                       @Param("orderRule") String orderRule);

  Integer searchTagSize(@Param("appId") Long appId,
                        @Param("type") Integer type,
                        @Param("keyword") String keyword);


  List<Tag> selectTagByName(@Param("appId") Long appId,
                            @Param("type") Integer type,
                            @Param("name") String name);

  List<Tag> search(Tag query);

  List<Tag> selectAllTagByAppId(@Param("appId") Long appId);
}
