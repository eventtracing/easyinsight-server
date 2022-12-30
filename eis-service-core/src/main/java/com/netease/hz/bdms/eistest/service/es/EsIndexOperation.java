package com.netease.hz.bdms.eistest.service.es;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class EsIndexOperation {

    @Resource
    private RestHighLevelClient client ;

    private final RequestOptions options = RequestOptions.DEFAULT;
    /**

     * 判断索引是否存在

     */

    public boolean checkIndex (String index) {

        try {

            return client.indices().exists(new GetIndexRequest(index), options);

        } catch (IOException e) {

            e.printStackTrace();

        }

        return Boolean.FALSE ;

    }
    /**

     * 创建索引

     */

    public boolean createIndex (String indexName ,Map<String, Object> columnMap){

        try {

            if(!checkIndex(indexName)){

                CreateIndexRequest request = new CreateIndexRequest(indexName);

                if (columnMap != null && columnMap.size()>0) {

                    Map<String, Object> source = new HashMap<>();

                    source.put("properties", columnMap);

                    request.mapping(source);

                }

                this.client.indices().create(request, options);

                return Boolean.TRUE ;

            }

        } catch (IOException e) {

            e.printStackTrace();

        }

        return Boolean.FALSE;

    }
    /**

     * 删除索引

     */

    public boolean deleteIndex(String indexName) {

        try {

            if(checkIndex(indexName)){

                DeleteIndexRequest request = new DeleteIndexRequest(indexName);

                AcknowledgedResponse response = client.indices().delete(request, options);

                return response.isAcknowledged();

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return Boolean.FALSE;

    }

}