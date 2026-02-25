package com.wikex.wikex.robot.normal.config;

import com.wikex.wikex.robot.normal.utils.BigDecimalToDecimal128Converter;
import com.wikex.wikex.robot.normal.utils.Decimal128ToBigDecimalConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MongoConvertConfig {
    
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        List<Converter<?, ?>> converterList = new ArrayList<>();
        converterList.add(new BigDecimalToDecimal128Converter());
        converterList.add(new Decimal128ToBigDecimalConverter());
        return new MongoCustomConversions(converterList);
    }
}
