package com.netease.eis.adapters;

import java.util.Map;

/**
 * 给用户发送提醒，如IM提醒
 */
public interface NotifyUserAdapter {

    /**
     * 发送文本信息
     * @param userEmail 收件人
     * @param title 标题
     * @param content 文本内容
     */
    void sendNotifyTextContent(String userEmail, String title, String content);

    /**
     * 发送邮件通知
     * @param userEmail 收件人
     * @param title 标题
     * @param content 文本内容
     */
    void sendNotifyMail(String userEmail, String title, String content);

    /**
     * 发送带格式信息
     * @param userEmail 收件人
     * @param topic 模版topic
     * @param title 标题
     * @param data 模版topic对应的数据
     */
    void sendNotifyTextTemplateContent(String userEmail, String topic, String title, Map<String, Object> data);
}
