package com.netease.hz.bdms.eistest.ws.dto;

import com.netease.hz.bdms.eistest.ws.dto.BuryPointMetaInfo;
import com.netease.hz.bdms.eistest.ws.dto.Storage;
import lombok.Getter;
import lombok.Setter;

/**
 * @author sguo
 */
public class PcStorage implements Storage {
    @Getter
    @Setter
    private boolean logOnly;
    @Getter
    @Setter
    private BuryPointMetaInfo metaInfo;

    @Override
    public void close() {

    }
}
