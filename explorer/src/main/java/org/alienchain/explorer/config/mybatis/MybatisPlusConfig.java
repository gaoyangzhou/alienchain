package org.alienchain.explorer.config.mybatis;

import com.baomidou.mybatisplus.mapper.MetaObjectHandler;
import com.baomidou.mybatisplus.plugins.OptimisticLockerInterceptor;
import com.baomidou.mybatisplus.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.plugins.PerformanceInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c)2019
 * Company: alienchain
 *
 * @author chen
 */
@Configuration
public class MybatisPlusConfig {

    /***
     * mybatis plus sql execute result: execute time,sql
     * @return
     */
    @Bean
    @Profile({"dev", "test"})// set dev test env open
    public PerformanceInterceptor performanceInterceptor() {
        PerformanceInterceptor performanceInterceptor = new PerformanceInterceptor();
        /*<!-- SQL execute Performance analysis ，dev。 maxTime  sql execute time -->*/
        performanceInterceptor.setMaxTime(1000);
        /*<!--SQL is format, default :false-->*/
        performanceInterceptor.setFormat(true);
        performanceInterceptor.setWriteInLog(true);        //execute log write to file
        return performanceInterceptor;
    }

    /**
     * mybatis plus version plugin@Version
     *
     * @return
     */
    @Bean
    public OptimisticLockerInterceptor optimisticLockerInterceptor() {
        return new OptimisticLockerInterceptor();
    }

    /**
     * mybatis-plus  pagination plugin
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor page = new PaginationInterceptor();
        page.setDialectType("mysql");
        return page;
    }

}
