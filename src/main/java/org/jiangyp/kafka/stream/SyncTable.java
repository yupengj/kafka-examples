/*
 * Copyright (c) 2015—2030 GantSoftware.Co.Ltd. All rights reserved.
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * is not allowed to be distributed or copied without the license from
 * GantSoftware.Co.Ltd. Please contact the company for more information.
 */

package org.jiangyp.kafka.stream;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class SyncTable {
    /**
     * 主表的原始表名
     */
    private String mainTableName;
    /**
     * 扩展表的原始表名
     */
    private String extTableName;
    /**
     * 元数据中的表名，默认是 mainTableName 的值
     * <p>
     * 目前只有 part_assembly_bom 分区表在元数据中对应的表名都是 part_assembly_bom 表
     */
    private String metadataTableName;
    /**
     * 主表原始的主题名称
     */
    private String mainTableRawTopicName;
    /**
     * 扩展表原始的主题名称
     */
    private String extTableRawTopicName;
    /**
     * es 中的索引名称，也是 转换后的 kafka 主题名称
     */
    private String esIndexName;
    /**
     * 主表和扩展表所有的日期字段
     */
    private Set<String> dateFields;
    /**
     * 记录codeList信息
     * <p>
     * key:属性名称，value：是编码值和编码名称的映射
     */
    private Map<String, Map<String, String>> fieldAndCodeList;
}
