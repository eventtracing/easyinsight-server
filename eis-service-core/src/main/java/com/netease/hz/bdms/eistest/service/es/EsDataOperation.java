package com.netease.hz.bdms.eistest.service.es;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EsDataOperation {

    @Resource
    private RestHighLevelClient client ;

    private final RequestOptions options = RequestOptions.DEFAULT;
    /**

     * 写入数据

     */

    public boolean insert (String indexName, Map<String,Object> dataMap){

        try {

            BulkRequest request = new BulkRequest();

            request.add(new IndexRequest(indexName,"doc").id(dataMap.get("index").toString()).opType("create").source(dataMap, XContentType.JSON));

            this.client.bulk(request, options);

            return Boolean.TRUE ;

        } catch (Exception e){

            e.printStackTrace();
            log.error("插入数据失败", e);

        }

        return Boolean.FALSE;

    }
    /**

     * 批量写入数据

     */

    public boolean batchInsert (String indexName, List<Map<String,Object>> userIndexList){

        try {

            BulkRequest request = new BulkRequest();

            for (Map<String,Object> dataMap:userIndexList){

                request.add(new IndexRequest(indexName,"_doc").source(dataMap,XContentType.JSON));

            }

            this.client.bulk(request, options);

            return Boolean.TRUE ;

        } catch (Exception e){

            e.printStackTrace();
            log.error("插入数据失败, index={}", indexName, e);
        }

        return Boolean.FALSE;

    }
    /**

     * 更新数据，可以直接修改索引结构

     */

    public boolean update (String indexName, Map<String,Object> dataMap){

        try {

            UpdateRequest updateRequest = new UpdateRequest(indexName,"doc", dataMap.remove("id").toString());

            updateRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

            updateRequest.doc(dataMap) ;

            this.client.update(updateRequest, options);

            return Boolean.TRUE ;

        } catch (Exception e){

            e.printStackTrace();

        }

        return Boolean.FALSE;

    }
    /**

     * 删除数据

     */

    public boolean delete (String indexName, String id){

        try {

            DeleteRequest deleteRequest = new DeleteRequest(indexName,"doc", id);

            this.client.delete(deleteRequest, options);

            return Boolean.TRUE ;

        } catch (Exception e){

            e.printStackTrace();

        }

        return Boolean.FALSE;

    }

}
