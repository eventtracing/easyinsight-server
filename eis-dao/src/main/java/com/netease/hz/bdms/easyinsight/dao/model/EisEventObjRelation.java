package com.netease.hz.bdms.easyinsight.dao.model;

import javax.persistence.*;
import java.util.Date;

@Table(name = "eis_event_obj_relation")
public class EisEventObjRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "eventPoolEntityId")
    private Long eventPoolEntityId;

    @Column(name = "terminalId")
    private Long terminalId;

    @Column(name = "objId")
    private Long objId;

    @Column(name = "createTime")
    private Date createTime;

    @Column(name = "updateTime")
    private Date updateTime;

    /**
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return terminal
     */
    public Long getTerminalId() {
        return terminalId;
    }

    /**
     * @param terminalId
     */
    public void setTerminalId(Long terminalId) {
        this.terminalId = terminalId;
    }

    /**
     * @return create_time
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * @param createTime
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * @return update_time
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * @param updateTime
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getEventPoolEntityId() {
        return eventPoolEntityId;
    }

    public void setEventPoolEntityId(Long eventPoolEntityId) {
        this.eventPoolEntityId = eventPoolEntityId;
    }

    public Long getObjId() {
        return objId;
    }

    public void setObjId(Long objId) {
        this.objId = objId;
    }
}