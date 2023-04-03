package com.netease.hz.bdms.easyinsight.common.util;

import java.util.Arrays;
import java.util.List;

/**
 * 数组工具
 */
public class ArrayUtils {

    public static String[] reverse(String a[]) {
        //算出数组长度
        int n = a.length;
        //声明新的数组
        String[] b = new String[n];
        //将字符串长度即字符串元素数赋值给j，作为辅助计数器(直接用n的话，n的值在运算中不固定，结果不正确)
        int j = n;
        //遍历n次,即数组有多少元素遍历多少次。
        for(int i=0;i<n;i++) {
            //将a数组的第一个元素(索引为0)赋值给相同长度的b数组的最后一个元素(索引为b数组长度j-1)
            b[j-1]=a[i];
            //b数组的倒数第一个元素确定，辅助计数器(j-1)得到b数组倒数第二个元素。
            j=j-1;
        }
        //遍历完成后b数组所有索引位置都被赋值。
        return b;
    }
    public static String reverse(String str) {
        String[] a=str.split("\\|");
        //算出数组长度
        int n = a.length;
        //声明新的数组
        String[] b = new String[n];
        //将字符串长度即字符串元素数赋值给j，作为辅助计数器(直接用n的话，n的值在运算中不固定，结果不正确)
        int j = n;
        //遍历n次,即数组有多少元素遍历多少次。
        for(int i=0;i<n;i++) {
            //将a数组的第一个元素(索引为0)赋值给相同长度的b数组的最后一个元素(索引为b数组长度j-1)
            b[j-1]=a[i];
            //b数组的倒数第一个元素确定，辅助计数器(j-1)得到b数组倒数第二个元素。
            j=j-1;
        }
        //遍历完成后b数组所有索引位置都被赋值。
        StringBuffer newStr=new StringBuffer();
        List<String> list = Arrays.asList(b);
        list.forEach(subOid -> newStr.append(subOid).append("|"));
        return  newStr.substring(0, newStr.length() - 1);
    }
}
