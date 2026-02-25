package com.wikex.wikex.config;

import com.wikex.wikex.ext.OrdinalToEnumConverterFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


@Configuration
public class ApplicationEnumConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new OrdinalToEnumConverterFactory());
        super.addFormatters(registry);
    }


}
