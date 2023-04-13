package com.netease.hz.bdms.easyinsight.service.service.obj;

import com.netease.hz.bdms.easyinsight.dao.EisTrackerContentMapper;
import com.netease.hz.bdms.easyinsight.dao.model.TrackerContent;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrackerContentService {

    @Autowired
    private EisTrackerContentMapper eisTrackerContentMapper;

    public List<TrackerContent> getAllByTrackerId(Long trackerId) {
        if (trackerId == null) {
            return new ArrayList<>(0);
        }
        List<TrackerContent> trackerContents = eisTrackerContentMapper.listAll(trackerId);
        return trackerContents == null ? new ArrayList<>(0) : trackerContents;
    }

    /**
     * 删除所有trackerId下的tackerContents，替换为新列表
     */
    @Transactional
    public void updateAll(Long trackerId, List<TrackerContent> trackerContents) {
        // 先删
        List<TrackerContent> allByTrackerId = getAllByTrackerId(trackerId);
        if (CollectionUtils.isNotEmpty(allByTrackerId)) {
            eisTrackerContentMapper.deleteByIds(allByTrackerId.stream().map(TrackerContent::getId).collect(Collectors.toSet()));
        }
        // 再增
        if (CollectionUtils.isNotEmpty(trackerContents)) {
            eisTrackerContentMapper.batchInsert(trackerContents);
        }
    }

    public static Map<String, String> fromTrackerContents(List<TrackerContent> trackerContents) {
        if (CollectionUtils.isEmpty(trackerContents)) {
            return new HashMap<>();
        }
        Map<String, String> result = new HashMap<>();
        trackerContents.forEach(o -> result.put(o.getType(), o.getContent()));
        return result;
    }

    public static List<TrackerContent> toTrackerContents(Long trackerId, Map<String, String> map) {
        if (MapUtils.isEmpty(map)) {
            return new ArrayList<>(0);
        }
        return map.entrySet().stream().map(o -> {
            TrackerContent content = new TrackerContent();
            content.setTrackerId(trackerId);
            content.setType(o.getKey());
            content.setContent(o.getValue());
            content.setCreateTime(new Timestamp(System.currentTimeMillis()));
            content.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            return content;
        }).collect(Collectors.toList());
    }
}
