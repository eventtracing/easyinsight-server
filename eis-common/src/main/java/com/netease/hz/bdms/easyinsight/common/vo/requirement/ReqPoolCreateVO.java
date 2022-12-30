package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import lombok.Data;

import java.util.List;

@Data
public class ReqPoolCreateVO {

    String name;

    List<UserDTO> owners;

    String desc;

}
