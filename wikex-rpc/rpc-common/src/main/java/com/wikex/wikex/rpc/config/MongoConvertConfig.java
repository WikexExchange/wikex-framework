package com.wikex.wikex.rpc.config;

import com.wikex.wikex.rpc.converter.BigDecimalToDecimal128Converter;
import com.wikex.wikex.rpc.converter.Decimal128ToBigDecimalConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class MongoConvertConfig {
    /**
     * mongoCustomConversions will be managed by Spring.
     * According to the converters added, data types will be converted during database read/write operations.
     *
     * @return MongoCustomConversions instance
     */
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        List<Converter<?, ?>> converterList = new ArrayList<>();
        converterList.add(new BigDecimalToDecimal128Converter());
        converterList.add(new Decimal128ToBigDecimalConverter());
        return new MongoCustomConversions(converterList);
    }
}
