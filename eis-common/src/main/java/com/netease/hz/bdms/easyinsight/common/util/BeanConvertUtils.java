package com.netease.hz.bdms.easyinsight.common.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import java.beans.FeatureDescriptor;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Bean转换工具类
 *
 * @author ginger
 * @create 2019-04-26 13:43
 */
public class BeanConvertUtils {

  /**
   * 将一个对象的内容拷贝到另一个对象（忽略null值）
   *
   * @param from 要拷贝的原始对象
   * @param to 要拷贝的目标对象
   * @param <FROM> 原始对象泛型
   * @param <TO> 目标对象泛型
   * @return 拷贝后的对象
   */
  public static <FROM, TO> TO copy(FROM from, TO to) {
    return copy(from, to, true);
  }
  /**
   * 将一个对象的内容拷贝到另一个对象（忽略null值）
   *
   * @param from 要拷贝的原始对象
   * @param to 要拷贝的目标对象
   * @param ignoreNull 是否忽略空值
   * @param <FROM> 原始对象泛型
   * @param <TO> 目标对象泛型
   * @return 拷贝后的对象
   */
  public static <FROM, TO> TO copy(FROM from, TO to, Boolean ignoreNull) {
    Preconditions.checkArgument(null != from && null != to);
    ignoreNull = null == ignoreNull ? false : ignoreNull;
    try {
      if (ignoreNull) {
        BeanUtils.copyProperties(from, to, getNullPropertyNames(from));
      } else {
        BeanUtils.copyProperties(from, to);
      }
      return to;
    } catch (Exception e) {
      throw new ServerException("can not copy", e);
    }
  }

  /**
   * 单个对象复制（忽略null值）
   *
   * @param from 要转换的原始对象
   * @param toClass 要转换的目标对象
   * @param <FROM> 原始对象泛型
   * @param <TO> 目标对象泛型
   * @return 转换后的对象
   */
  public static <FROM, TO> TO copy(FROM from, Class<TO> toClass) {
    try {
      if (null == from) {
        return null;
      }
      TO to = toClass.newInstance();
      BeanUtils.copyProperties(from, to, getNullPropertyNames(from));
      return to;
    } catch (Exception e) {
      throw new ServerException("can not convert", e);
    }
  }

  /**
   * 将一个对象的内容拷贝到另一个对象（忽略null值）
   *
   * @param from 要拷贝的原始对象
   * @param toClass 要拷贝的目标对象类
   * @param ignoreNull 是否忽略空值
   * @param <FROM> 原始对象泛型
   * @param <TO> 目标对象泛型
   * @return 拷贝后的对象
   */
  public static <FROM, TO> TO copy(FROM from, Class<TO> toClass, Boolean ignoreNull) {
    Preconditions.checkArgument(null != from && null != toClass);
    ignoreNull = null == ignoreNull ? false : ignoreNull;
    try {
      TO to = toClass.newInstance();
      if (ignoreNull) {
        BeanUtils.copyProperties(from, to, getNullPropertyNames(from));
      } else {
        BeanUtils.copyProperties(from, to);
      }
      return to;
    } catch (Exception e) {
      throw new ServerException("can not copy", e);
    }
  }

  /**
   * 单个对象转换（忽略null值）
   *
   * @param from 要转换的原始对象
   * @param toClass 要转换的目标对象
   * @param <FROM> 原始对象泛型
   * @param <TO> 目标对象泛型
   * @return 转换后的对象
   */
  public static <FROM, TO> TO convert(FROM from, Class<TO> toClass) {
    try {
      if (null == from) {
        return null;
      }
      TO to = toClass.newInstance();
      BeanUtils.copyProperties(from, to, getNullPropertyNames(from));
      return to;
    } catch (Exception e) {
      throw new ServerException("can not convert", e);
    }
  }

  /**
   * 单个对象转换
   *
   * @param from 要转换的原始对象
   * @param toClass 要转换的目标对象
   * @param ignoreNull 是否忽略空值
   * @param <FROM> 原始对象泛型
   * @param <TO> 目标对象泛型
   * @return 转换后的对象
   */
  public static <FROM, TO> TO convert(FROM from, Class<TO> toClass, Boolean ignoreNull) {
    try {
      if (null == from) {
        return null;
      }
      ignoreNull = null == ignoreNull ? false : ignoreNull;
      TO to = toClass.newInstance();
      if (ignoreNull) {
        BeanUtils.copyProperties(from, to, getNullPropertyNames(from));
      } else {
        BeanUtils.copyProperties(from, to);
      }
      return to;
    } catch (Exception e) {
      throw new ServerException("can not convert", e);
    }
  }

  /**
   * 多个对象转换（忽略null值）
   *
   * @param froms 要转换的原始对象列表
   * @param toClass 要转换的目标对象
   * @param <FROM> 原始对象泛型
   * @param <TO> 目标对象泛型
   * @return 转换后的对象列表
   */
  public static <FROM, TO> List<TO> convert(List<FROM> froms, Class<TO> toClass) {
    try {
      if (CollectionUtils.isEmpty(froms)) {
        return Lists.newArrayListWithCapacity(0);
      }
      List<TO> tos = Lists.newArrayListWithCapacity(froms.size());
      for (FROM from : froms) {
        tos.add(convert(from, toClass));
      }
      return tos;
    } catch (Exception e) {
      throw new ServerException("can not convert", e);
    }
  }

  /**
   * 多个对象复制（忽略null值）
   *
   * @param froms 要转换的原始对象列表
   * @param toClass 要转换的目标对象
   * @param <FROM> 原始对象泛型
   * @param <TO> 目标对象泛型
   * @return 复制后的对象列表
   */
  public static <FROM, TO> List<TO> copy(List<FROM> froms, Class<TO> toClass) {
    try {
      if (CollectionUtils.isEmpty(froms)) {
        return Lists.newArrayListWithCapacity(0);
      }
      List<TO> tos = Lists.newArrayListWithCapacity(froms.size());
      for (FROM from : froms) {
        tos.add(copy(from, toClass));
      }
      return tos;
    } catch (Exception e) {
      throw new ServerException("can not convert", e);
    }
  }


  /**
   * 多个对象转换
   *
   * @param froms 要转换的原始对象列表
   * @param toClass 要转换的目标对象
   * @param ignoreNull 是否忽略空值
   * @param <FROM> 原始对象泛型
   * @param <TO> 目标对象泛型
   * @return 转换后的对象列表
   */
  public static <FROM, TO> List<TO> convert(List<FROM> froms, Class<TO> toClass, Boolean ignoreNull) {
    try {
      if (CollectionUtils.isEmpty(froms)) {
        return Lists.newArrayListWithCapacity(0);
      }
      List<TO> tos = Lists.newArrayListWithCapacity(froms.size());
      for (FROM from : froms) {
        tos.add(convert(from, toClass, ignoreNull));
      }
      return tos;
    } catch (Exception e) {
      throw new ServerException("can not convert", e);
    }
  }

  /**
   * 多个对象复制
   *
   * @param froms 要转换的原始对象列表
   * @param toClass 要转换的目标对象
   * @param ignoreNull 是否忽略空值
   * @param <FROM> 原始对象泛型
   * @param <TO> 目标对象泛型
   * @return 转换后的对象列表
   */
  public static <FROM, TO> List<TO> copy(List<FROM> froms, Class<TO> toClass, Boolean ignoreNull) {
    try {
      if (CollectionUtils.isEmpty(froms)) {
        return Lists.newArrayListWithCapacity(0);
      }
      List<TO> tos = Lists.newArrayListWithCapacity(froms.size());
      for (FROM from : froms) {
        tos.add(copy(from, toClass, ignoreNull));
      }
      return tos;
    } catch (Exception e) {
      throw new ServerException("can not convert", e);
    }
  }

  private static String[] getNullPropertyNames(Object source) {
    final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
    return Stream.of(wrappedSource.getPropertyDescriptors())
        .map(FeatureDescriptor::getName)
        .filter(propertyName -> wrappedSource.getPropertyValue(propertyName) == null)
        .toArray(String[]::new);
  }

}
