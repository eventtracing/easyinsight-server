package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import lombok.Data;

import java.util.List;

@Data
public class ReqPoolEditShowVO {

    String name;

    List<UserDTO> dataOwners;

    String desc;

}
