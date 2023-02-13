package com.netease.hz.bdms.eistest.service.es;

import com.netease.hz.bdms.eistest.service.BuryPointAnaysisService;
import com.netease.hz.bdms.eistest.service.BuryPointTestInfoCacheService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

@Component
public class ElasticsearchWriteService {

    @Resource
    private EsIndexOperation esIndexOperation;
    @Resource
    private BuryPointTestInfoCacheService buryPointTestInfoCacheService;
    @Resource
    private BuryPointAnaysisService buryPointAnaysisService;
    @Resource
    private EsDataOperation esDataOperation;

    public boolean createEsIndex(String indexName ,Map<String, Object> columnMap){

        return esIndexOperation.createIndex(indexName, columnMap);
    }

    public boolean deleteIndex(String indexName){

        return esIndexOperation.deleteIndex(indexName);
    }

    public boolean insertIntoEs(String indexName ,Map<String, Object> columnMap){

        return esDataOperation.insert(indexName, columnMap);
    }

    public boolean insertIntoEsBatch(String indexName , List<Map<String, Object>> columnMaps){

        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);

        return esDataOperation.batchInsert(indexName + "_" + year + "_" + month, columnMaps);
    }

    public boolean translateIntoEs(String indexName){

        for(int code = 1169; code >=1 ; code--) {
            int index = 0;
            for (int type = 3; type >=1 ; type--) {
                List<String> buryPointLogString = buryPointTestInfoCacheService.getInsightLogByPage(String.valueOf(code), type, 0);
                if(CollectionUtils.isNotEmpty(buryPointLogString)) {
                    List<Map<String, Object>> buryPointLogList = buryPointAnaysisService.parseCacheLogToES(buryPointLogString, type, code, index);
                    boolean ret = esDataOperation.batchInsert(indexName, buryPointLogList);
                    if(!ret){
                        return false;
                    }
                    index += buryPointLogList.size();
                }
            }
        }

        return true;
    }


    private static Map<String, Object> getObjectToMap(Object obj) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<String, Object>();
        Class<?> cla = obj.getClass();
        Field[] fields = cla.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String keyName = field.getName();
            Object value = field.get(obj);
            if (value == null)
                value = "";
            map.put(keyName, value);
        }
        return map;
    }

    //Map转Object
    public static Object getMapToObject(Map<Object, Object> map, Class<?> clas) throws Exception {
        if (map == null)
            return null;
        Object obj = clas.newInstance();
        Class<?> cla = obj.getClass();
        Field[] fields = cla.getDeclaredFields();
        for (Field field : fields) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                continue;
            }
            field.setAccessible(true);
            if (map.containsKey(field.getName())) {
                field.set(obj, map.get(field.getName()));
            }
        }
        return obj;
    }

}
