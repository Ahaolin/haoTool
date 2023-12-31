package com.haolin.haotool.util.sql;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;

@Slf4j
public class SqlSessionFactoryUtil {

    @Value("spring.sql.config.path")
    private static String  resourcePath;


    public static class Inner{

        private static  SqlSessionFactory sqlSessionFactory;
        static {
             try {
                 InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
                 sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
             }catch (Exception e){
                log.error("sqlSessionFactory is error!",e);
             }
        }
    }

    public static SqlSessionFactory getSqlSessionFactory(){
        return Inner.sqlSessionFactory;
    }

    public static SqlSession getSqlSession(){
        return getSqlSession(true);
    }

    public static SqlSession getSqlSession(boolean autoCommit) {
        return getSqlSessionFactory().openSession(autoCommit);
    }

}
