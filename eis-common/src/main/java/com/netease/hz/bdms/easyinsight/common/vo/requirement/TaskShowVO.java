package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import lombok.Data;

@Data
public class TaskShowVO {

    Long id;

    String name;

    Long terminalId;

    UserDTO owner;

    UserDTO verifier;

}
