package slf;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * 将 mybatis plus 的 wrapper 转换为sql模板
 * <p>
 * 例如 select a,b from c where d=e
 * <p>
 * 如果泛型 T 是有效的 DAO，使用了 TableName 注解标注表名，则可以直接解析出表名
 * <p>
 * 如果使用 SimpleQueryWrapper 或泛型类没有使用 TableName 注解标注表名，则表名由 ? 占位
 * <p>
 * 使用 SimpleQueryWrapper 时，泛型 T 设置为 Object
 *
 * @author guoquanrui
 * @date 2023/4/6 17:31
 */
@Slf4j
public class MBPWrapperUtil<T> {

    private final T entity;

    public MBPWrapperUtil(T entity) {
        this.entity = entity;
    }

    public String toSqlTemplate(QueryWrapper<T> wrapper) {

        StringBuilder stringBuilder = new StringBuilder();

//        select 部分
        stringBuilder.append("select ");
        if (StringUtils.isNotEmpty(wrapper.getSqlSelect())) {
            stringBuilder.append(wrapper.getSqlSelect());
        } else {
            stringBuilder.append("*");
        }
        stringBuilder.append(" from ");

//        表名
        Annotation[] annotations = entity.getClass().getAnnotations();
        Optional<String> tableNameOptional = Arrays.stream(annotations)
                .filter(annotation -> annotation instanceof TableName)
                .map(annotation -> {
                    TableName tableName = (TableName) annotation;
                    return StringUtils.isBlank(tableName.value()) ? "?" : tableName.value();
                })
                .findFirst();
        stringBuilder.append(tableNameOptional.orElse("?"));

//        查询条件
        stringBuilder.append(" where ");
        List<String> conditions = Splitter.on("?").splitToList(wrapper.getTargetSql());
        Map<String, Object> paramNameValuePairs = wrapper.getParamNameValuePairs();
        HashMap<Integer, Object> paramNameValuePairMap = new HashMap<>();
        paramNameValuePairs.forEach((k, v) -> {
            int index = Integer.parseInt(k.replace("MPGENVAL", "")) - 1;
            paramNameValuePairMap.put(index, v);
        });
        if (conditions.size() != paramNameValuePairMap.size() + 1) {
            throw new RuntimeException("conditions.size()!=paramNameValuePairMap.size()+1");
        }
        for (int i = 0; i < conditions.size() - 1; i++) {
            stringBuilder.append(conditions.get(i));
            if (paramNameValuePairMap.get(i) instanceof String) {
                stringBuilder.append("'");
                stringBuilder.append(paramNameValuePairMap.get(i));
                stringBuilder.append("'");
            } else {
                stringBuilder.append(paramNameValuePairMap.get(i));
            }
        }
        stringBuilder.append(conditions.get(conditions.size() - 1));
        return stringBuilder.toString();
    }

    /**
     * 格式化 sql 模板
     * <p>
     * 例如 format("select * from {0} where {1}={2}", "table", "a", "b")
     * <p>
     * 返回 select * from table where a=b
     *
     * @author guoquanrui
     * @date 2023/4/6 22:47
     */
    public String format(String sqlTemplate, Object... args) {
        int length = args.length;
        if (length == 0) {
            return sqlTemplate;
        }
        for (int i = 0; i < length; i++) {
            sqlTemplate = sqlTemplate.replace("{" + i + "}", args[i].toString());
        }
        return sqlTemplate;
    }

    /**
     * 简单queryWrapper
     * <p>
     * 当没有对应 DAO 或懒得加泛型时，可以使用该 queryWrapper
     *
     * @author guoquanrui
     * @date 2023/4/6 18:54
     */
    public static class SimpleQueryWrapper extends QueryWrapper<Object> {
    }

}
