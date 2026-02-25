package com.wikex.wikex.config;

import com.wikex.wikex.util.IdWorkByTwitter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;


@Configuration
public class SystemConfig {

    @Bean
    public IdWorkByTwitter idWorkByTwitter(@Value("${spark.system.work_id:0}")long workId, @Value("${spark.system.data_center_id:0}")long dataCenterId){
        return new IdWorkByTwitter(workId, dataCenterId);
    }

}
