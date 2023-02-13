package com.netease.hz.bdms.easyinsight.dao.model;

import javax.persistence.*;
import java.util.Date;

@Table(name = "eis_task_process")
public class EisTaskProcess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "req_pool_id")
    private Long reqPoolId;

    @Column(name = "req_pool_entity_id")
    private Long reqPoolEntityId;

    @Column(name = "req_pool_type")
    private Integer reqPoolType;

    @Column(name = "task_id")
    private Long taskId;

    private Integer status;

    @Column(name = "obj_id")
    private Long objId;

    @Column(name = "spm_by_obj_id")
    private String spmByObjId;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "owner_email")
    private String ownerEmail;

    @Column(name = "verifier_name")
    private String verifierName;

    @Column(name = "verifier_email")
    private String verifierEmail;

    @Column(name = "create_email")
    private String createEmail;

    @Column(name = "create_name")
    private String createName;

    @Column(name = "update_email")
    private String updateEmail;

    @Column(name = "update_name")
    private String updateName;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
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
     * @return req_pool_id
     */
    public Long getReqPoolId() {
        return reqPoolId;
    }

    /**
     * @param reqPoolId
     */
    public void setReqPoolId(Long reqPoolId) {
        this.reqPoolId = reqPoolId;
    }

    public Long getReqPoolEntityId() {
        return reqPoolEntityId;
    }

    public void setReqPoolEntityId(Long reqPoolEntityId) {
        this.reqPoolEntityId = reqPoolEntityId;
    }

    /**
     * @return req_pool_type
     */
    public Integer getReqPoolType() {
        return reqPoolType;
    }

    /**
     * @param reqPoolType
     */
    public void setReqPoolType(Integer reqPoolType) {
        this.reqPoolType = reqPoolType;
    }

    /**
     * @return task_id
     */
    public Long getTaskId() {
        return taskId;
    }

    /**
     * @param taskId
     */
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    /**
     * @return status
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * @param status
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getObjId() {
        return objId;
    }

    public void setObjId(Long objId) {
        this.objId = objId;
    }

    public String getSpmByObjId() {
        return spmByObjId;
    }

    public void setSpmByObjId(String spmByObjId) {
        this.spmByObjId = spmByObjId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getVerifierName() {
        return verifierName;
    }

    public void setVerifierName(String verifierName) {
        this.verifierName = verifierName;
    }

    public String getVerifierEmail() {
        return verifierEmail;
    }

    public void setVerifierEmail(String verifierEmail) {
        this.verifierEmail = verifierEmail;
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
}