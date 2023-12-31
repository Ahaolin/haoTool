package com.haolin.haotool.util.sql;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.haolin.haotool.exections.ParseSQLException;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MyBatisSqlUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyBatisSqlUtils.class);


    public static MyBatisSql getMyBatisSql(Map<String, Object> paramMap, Class<?> clazz, String methodName) throws ParseSQLException {
        return getMyBatisSql(paramMap, StrUtil.concat(true, clazz.getName(), ".", methodName));
    }

    public static MyBatisSql getMyBatisSql(Map<String, Object> paramMap, String mid) throws ParseSQLException {
        try (SqlSession sqlSession = SqlSessionFactoryUtil.getSqlSession();) {
            return MyBatisSqlUtils.getMyBatisSql(mid, paramMap, sqlSession);
        } catch (Exception e) {
            //    LOGGER.error("get Mybatis boundSql error message======  mid:{}  paramMap : 【{}】", mid, paramMap, e);
            throw new ParseSQLException("Parse SQL ERROR!", e, paramMap, mid);
        }
    }


    /**
     * 运行期获取MyBatis执行的SQL及参数
     *
     * @param id                Mapper xml 文件里的select Id
     * @param parameterMap      参数
     *     如果是一个参数 直接 BeanUtil.beanToMap
     *     多个参数 建议在 final MapperMethod.ParamMap<Object> paramMap = new MapperMethod.ParamMap<>(); 进行put
     * @param sqlSession
     * @return  fixme ${paramName} 错误时此处不会打印日志
     */
    public static MyBatisSql getMyBatisSql(String id, Map<String, Object> parameterMap, SqlSession sqlSession) {
        MyBatisSql myBatisSql = new MyBatisSql();
        MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(id);
        BoundSql boundSql = ms.getBoundSql(parameterMap);
        myBatisSql.setSql(boundSql.getSql());
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings != null) {
            final DefaultReflectorFactory reflectorFactory = new DefaultReflectorFactory();
//            Object[] parameterArray = new Object[parameterMappings.size()];
            List<Object> parameterArray = Lists.newArrayListWithCapacity(parameterMappings.size());

            ParameterMapping parameterMapping = null;
            Object value = null;
            Object parameterObject = null;
            MetaObject metaObject = null;
            PropertyTokenizer prop = null;
            String propertyName = null;
            String[] names = null;
            for (int i = 0; i < parameterMappings.size(); i++) {
                parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() != ParameterMode.OUT) {
                    propertyName = parameterMapping.getProperty();
                    names = propertyName.split("\\.");
                    if (propertyName.contains(".") && names.length == 2) {
                        parameterObject = parameterMap.get(names[0]);
                        propertyName = names[1];
                    } else if (propertyName.contains(".") && names.length == 3) {
                        parameterObject = parameterMap.get(names[0]); // map
                        if (parameterObject instanceof Map) {
                            parameterObject = ((Map) parameterObject).get(names[1]);
                        }
                        propertyName = names[2];
                    } else {
                        parameterObject = parameterMap.get(propertyName);
                    }
                    metaObject = MetaObject.forObject(parameterObject, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, reflectorFactory);
                    prop = new PropertyTokenizer(propertyName);
                    if (parameterObject == null) {
                        value = null;
                    } else if (ms.getConfiguration().getTypeHandlerRegistry().hasTypeHandler(parameterObject.getClass())) {
                        value = parameterObject;
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX) && boundSql.hasAdditionalParameter(prop.getName())) {
                        value = boundSql.getAdditionalParameter(prop.getName());
                        if (value != null) {
                            value = MetaObject.forObject(value, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, reflectorFactory).getValue(propertyName.substring(prop.getName().length()));
                        }
                    } else {
                        value = metaObject.getValue(propertyName);
                    }
//                    parameterArray[i] = value;
                    if (Objects.nonNull(value)) parameterArray.add(value);
                    else LOGGER.warn("report sql param is null ! propertyName:[{}]", propertyName);
                }
            }
            myBatisSql.setParameters(parameterArray.toArray());
        }
        return myBatisSql;
    }

//    /**
//     * 运行期获取MyBatis执行的SQL及参数
//     *
//     * @param id             Mapper xml 文件里的select Id
//     * @param paramObj      参数对象
//     * @param sqlSessionFactory
//     * @return
//     */
//    public static MyBatisSql getMyBatisSql(String id, Object paramObj, SqlSessionFactory sqlSessionFactory) {
//        MyBatisSql myBatisSql = new MyBatisSql();
//        MappedStatement ms = sqlSessionFactory.getConfiguration().getMappedStatement(id);
//        BoundSql boundSql = ms.getBoundSql(paramObj);
//        myBatisSql.setSql(boundSql.getSql());
//        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
//        if (parameterMappings != null) {
//            final DefaultReflectorFactory reflectorFactory = new DefaultReflectorFactory();
//            Object[] parameterArray = new Object[parameterMappings.size()];
//            ParameterMapping parameterMapping = null;
//            Object value = null;
//            Object parameterObject = null;
//            MetaObject metaObject = null;
//            PropertyTokenizer prop = null;
//            String propertyName = null;
//            String[] names = null;
//            for (int i = 0; i < parameterMappings.size(); i++) {
//                parameterMapping = parameterMappings.get(i);
//                if (parameterMapping.getMode() != ParameterMode.OUT) {
//                    propertyName = parameterMapping.getProperty();
//                    names = propertyName.split("\\.");
//                    if (propertyName.contains(".") && names.length == 2) {
//                        parameterObject = parameterMap.get(names[0]);
//                        propertyName = names[1];
//                    } else if (propertyName.contains(".") && names.length == 3) {
//                        parameterObject = parameterMap.get(names[0]); // map
//                        if (parameterObject instanceof Map) {
//                            parameterObject = ((Map) parameterObject).get(names[1]);
//                        }
//                        propertyName = names[2];
//                    } else {
//                        parameterObject = parameterMap.get(propertyName);
//                    }
//                    metaObject = MetaObject.forObject(parameterObject, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, reflectorFactory);
//                    prop = new PropertyTokenizer(propertyName);
//                    if (parameterObject == null) {
//                        value = null;
//                    } else if (ms.getConfiguration().getTypeHandlerRegistry().hasTypeHandler(parameterObject.getClass())) {
//                        value = parameterObject;
//                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
//                        value = boundSql.getAdditionalParameter(propertyName);
//                    } else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX) && boundSql.hasAdditionalParameter(prop.getName())) {
//                        value = boundSql.getAdditionalParameter(prop.getName());
//                        if (value != null) {
//                            value = MetaObject.forObject(value, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, reflectorFactory).getValue(propertyName.substring(prop.getName().length()));
//                        }
//                    } else {
//                        value = metaObject.getValue(propertyName);
//                    }
//                    parameterArray[i] = value;
//                }
//            }
//            myBatisSql.setParameters(parameterArray);
//        }
//        return myBatisSql;
//    }
}
