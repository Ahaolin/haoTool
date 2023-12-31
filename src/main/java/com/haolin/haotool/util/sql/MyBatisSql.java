package com.haolin.haotool.util.sql;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.ibatis.logging.jdbc.BaseJdbcLogger;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;

import java.util.*;

public class MyBatisSql {

    /**
     * 运行期 sql
     */
    private String sql;

    /**
     * 参数 数组
     */
    private Object[] parameters;

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public MyBatisSql() {
    }

    public MyBatisSql(String sql, Object[] parameters) {
        this.sql = sql;
        this.parameters = parameters;
    }

    private final static String EMPTY_SPACE_STR = " ";

    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * 是否以分号结尾
     * @param endWithSemicolon
     * @return
     */
    public String toString(boolean endWithSemicolon) {
        if (parameters == null || parameters.length == 0) {
            if (StrUtil.isNotBlank(sql)) return  endWithSemicolon ? StrUtil.removeAllLineBreaks(sql) + ";" : StrUtil.removeAllLineBreaks(sql);
            return EMPTY_SPACE_STR;
        }
        List<Object> parametersArray = Arrays.asList(parameters);
        List<Object> list = new ArrayList<>(parametersArray);
        while (sql.contains("?") && list.size() > 0 && parameters.length > 0) {
            sql = sql.replaceFirst("\\?", obj2ToString(list.get(0)));
            list.remove(0);
        }
//        return sql.replaceAll("(\r?\n(\\s*\r?\n)+)", "\r\n");
//        sql.replaceAll("(\r?\n(\\s*\r?\n)+)", "\r\n") + ";"
        return endWithSemicolon ? removeBreakingWhitespace(sql) + ";" : removeBreakingWhitespace(sql);
    }

    public String obj2ToString(Object obj) {
        if (obj instanceof Date) {
            return StrUtil.format("'{}'", DateUtil.format((Date) obj,"yyyyMM" /*"yyyy-MM-dd HH:mm:ss.S"*//*DatePattern.NORM_DATETIME_MS_PATTERN*/));
        }
        return StrUtil.format("'{}'", obj.toString());
    }


    /**
     * mybatis 官方打印日志 {@link ConnectionLogger#invoke(Object, java.lang.reflect.Method, Object[])}
     *  下的{@link BaseJdbcLogger#removeBreakingWhitespace(String)}
     */
    protected String removeBreakingWhitespace(String original) {
        StringTokenizer whitespaceStripper = new StringTokenizer(original);
        StringBuilder builder = new StringBuilder();
        while(whitespaceStripper.hasMoreTokens()) {
            builder.append(whitespaceStripper.nextToken());
            builder.append(" ");
        }
        return builder.toString();
    }
}