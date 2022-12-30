package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public enum CauseCheckResultEnum {
  /**
   * 无refer，不需要校验
   */
  IGNORE(0, "无需校验"),
  /**
   * 校验OK
   */
  OK(1, "校验通过"),
  /**
   * SPM非法
   */
  SPM_INVALID(2, "SPM非法"),
  /**
   * 不合理SPM(非白名单)
   */
  SPM_UNREASONABLE(3, "不合理SPM(非白名单)"),
  /**
   * 降级到页面
   */
  FALLBACK_TO_PAGE(4, "降级到页面"),
  /**
   * 格式错误
   */
  FORM_INVALID(5, "格式错误"),
  ;
  private Integer result;

  private String displayName;

  CauseCheckResultEnum(Integer result, String displayName) {
    this.result = result;
    this.displayName = displayName;
  }

  public static CauseCheckResultEnum fromResult(Integer result) {
    for(CauseCheckResultEnum resultEnum : values()) {
      if(resultEnum.getResult().equals(result)) {
        return resultEnum;
      }
    }
    throw new ServerException(result+"不能转换为CheckResultEnum");
  }
}
