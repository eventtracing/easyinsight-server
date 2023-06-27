package com.netease.hz.bdms.easyinsight.dao.model;

import javax.persistence.*;
import java.util.Date;

@Table(name = "eis_req_obj_change_history")
public class EisReqObjChangeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "objId")
    private Long objId;

    @Column(name = "reqPoolId")
    private Long reqPoolId;

    @Column(name = "changeType")
    private Integer changeType;

    @Column(name = "preChangeId")
    private String preChangeId;

    @Column(name = "newTrackerInfo")
    private String newTrackerInfo;

    @Column(name = "createEmail")
    private String createEmail;

    @Column(name = "createName")
    private String createName;

    @Column(name = "updateEmail")
    private String updateEmail;

    @Column(name = "updateName")
    private String updateName;

    @Column(name = "createTime")
    private Date createTime;

    @Column(name = "updateTime")
    private Date updateTime;

    @Column(name = "extInfo")
    private String extInfo;

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
     * @return obj_id
     */
    public Long getObjId() {
        return objId;
    }

    /**
     * @param objId
     */
    public void setObjId(Long objId) {
        this.objId = objId;
    }

    /**
     * @return create_email
     */
    public String getCreateEmail() {
        return createEmail;
    }

    /**
     * @param createEmail
     */
    public void setCreateEmail(String createEmail) {
        this.createEmail = createEmail;
    }

    /**
     * @return create_name
     */
    public String getCreateName() {
        return createName;
    }

    /**
     * @param createName
     */
    public void setCreateName(String createName) {
        this.createName = createName;
    }

    /**
     * @return update_email
     */
    public String getUpdateEmail() {
        return updateEmail;
    }

    /**
     * @param updateEmail
     */
    public void setUpdateEmail(String updateEmail) {
        this.updateEmail = updateEmail;
    }

    /**
     * @return update_name
     */
    public String getUpdateName() {
        return updateName;
    }

    /**
     * @param updateName
     */
    public void setUpdateName(String updateName) {
        this.updateName = updateName;
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


    public Long getReqPoolId() {
        return reqPoolId;
    }

    public void setReqPoolId(Long reqPoolId) {
        this.reqPoolId = reqPoolId;
    }

    public Integer getChangeType() {
        return changeType;
    }

    public void setChangeType(Integer changeType) {
        this.changeType = changeType;
    }

    public String getExtInfo() {
        return extInfo;
    }

    public void setExtInfo(String extInfo) {
        this.extInfo = extInfo;
    }

    public String getPreChangeId() {
        return preChangeId;
    }

    public void setPreChangeId(String preChangeId) {
        this.preChangeId = preChangeId;
    }

    public String getNewTrackerInfo() {
        return newTrackerInfo;
    }

    public void setNewTrackerInfo(String newTrackerInfo) {
        this.newTrackerInfo = newTrackerInfo;
    }
}