package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.CommonKV;
import com.netease.hz.bdms.easyinsight.dao.model.Event;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface CommonKVMapper {

  CommonKV get(@Param("code") String code, @Param("k") String k);

  List<CommonKV> gets(@Param("code") String code, @Param("ks") Collection<String> ks);

  Integer insert(CommonKV commonKV);

  Integer updateValue(CommonKV commonKV);

  Integer delete(Long id);
}
