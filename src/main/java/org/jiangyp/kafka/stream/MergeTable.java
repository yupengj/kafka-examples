package org.jiangyp.kafka.stream;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class MergeTable {
    /**
     * 主表的表名
     */
    private String mainTable;
    /**
     * 扩展表的表名
     */
    private String extTable;
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
