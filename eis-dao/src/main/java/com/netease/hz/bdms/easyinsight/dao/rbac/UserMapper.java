package com.netease.hz.bdms.easyinsight.dao.rbac;

import com.netease.hz.bdms.easyinsight.dao.model.rbac.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserMapper {
  int insertSelective(User record);

  void insertBatch(@Param("users") List<User> users);

  void insertById(User user);

  User selectByPrimaryKey(Long id);

  List<User> getByIds(@Param("ids") Collection<Long> ids);

  User selectByEmail(String email);

  /**
   * 根据email查看用户信息
   */
  List<User> selectByEmails(@Param("emails") Collection<String> emails);

  /**
   * 更新用户
   *
   * @param user 用户
   * @return 更新条数
   */
  int update(User user);

  /**
   * 根据范围查询用户集合
   *
   * @param roleType  角色类型
   * @param typeId    类型 ID
   * @param roleId    角色 ID
   * @param orderBy   排序的列名
   * @param orderRule 排序规则:升序或降序
   * @param search    搜索关键字
   * @return 用户集合
   */
  List<User> selectByRange(@Param("roleType") Integer roleType,
                           @Param("typeId") Long typeId,
                           @Param("roleId") Long roleId,
                           @Param("orderBy") String orderBy,
                           @Param("orderRule") String orderRule,
                           @Param("search") String search);

  void deleteById(long id);
}
