package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.common.OpenSource;
import com.netease.hz.bdms.easyinsight.dao.model.UserOuterResourceBinding;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@OpenSource(value = false)
public interface UserOuterResourceBindingMapper {

    UserOuterResourceBinding getByEmailAndType(String email, String type);

    List<UserOuterResourceBinding> getByEmailsAndType(Set<String> emails, String type);

    UserOuterResourceBinding getByTypeAndValue(String bindValue, String type);

    Integer insert(UserOuterResourceBinding binding);

    Integer updateBindValue(UserOuterResourceBinding binding);

    Integer deleteByEmailAndType(UserOuterResourceBinding binding);
}
