package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ParamInvalidException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum  ImageStoreEnum {
  NOS("nos"),
  MySQL("mysql"),
  ;

  private String location;

  public static ImageStoreEnum fromLocation(String location) {
    for(ImageStoreEnum imageStoreEnum : values()) {
      if(imageStoreEnum.location.equals(location)) {
        return imageStoreEnum;
      }
    }
    throw new ParamInvalidException("location配置不正确");
  }
}
