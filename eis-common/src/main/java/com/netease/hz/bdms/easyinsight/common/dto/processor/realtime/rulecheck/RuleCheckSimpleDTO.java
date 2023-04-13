package com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck;

import java.util.List;
import java.util.Map;

import lombok.*;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RuleCheckSimpleDTO {
  /**
   * 事件类型code
   */
  private String eventCode;
  /**
   * 事件类型名称
   */
  private String eventName;
  /**
   * spm
   */
  private String spm;
  /**
   * rootpage的oid
   */
  private String rootPageOid;
  /**
   * rootPage的名称
   */
  private String rootPageName;
  /**
   * 第一层对象的oid
   */
  private String firstObjOid;
  /**
   * 第一层对象的名称
   */
  private String firstObjName;
  /**
   * 日志获取时间
   */
  private Long logServerTime;
  /**
   * 验证结果
   * @see com.netease.hz.bdms.easyinsight.common.enums.CheckResultEnum
   */
  private Integer checkResult;

  /**
   * 失败key，用于对失败原因归类
   */
  private String failKey;
//  /**
//   * 日志
//   */
//  private CheckResultFormatSimpleDTO log;
//  /**
//   * 规则
//   */
//  private CheckResultFormatSimpleDTO rule;
  /**
   * 校验指标
   */
  private DetectionIndicatorsSimpleDTO detectionIndicator;


  /**
   * 日志结构统一为Map, key为String类型(如pList, eList, 或其他{key})，value为数组或单个值
   * 具体示例如下：
   * {
   * 		"pList": [
   *            {
   * 				"{key}": CompareItemDTO结构
   *      }
   * 		],
   * 		"eList": [
   *      {
   * 				"{key}": CompareItemDTO结构
   *      }
   * 		],
   * 		"{key}": CompareItemDTO结构
   * }
   */
  private Map<String, Object> log;
  /**
   * 规则统一为Map, key为String类型(如pList, eList, 或其他{key})，value为数组或单个值
   * 具体示例如下：
   * {
   * 		"pList": [
   *            {
   * 				"{key}": CompareItemDTO结构
   *      }
   * 		],
   * 		"eList": [
   *      {
   * 				"{key}": CompareItemDTO结构
   *      }
   * 		],
   * 		"{key}": CompareItemDTO结构
   * }
   */
  private Map<String, Object> rule;

  /**
   * 原始日志
   */
  private Map<String, Object> props;


  /**
   * 埋点ID
   */
  private Long trackerId;

  /**
   * 没有规则的参数
   */
  private List<String> unMatchedParamCode;
}
