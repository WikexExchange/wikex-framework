package com.wikex.wikex.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CorsResponseHeaderFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(CorsResponseHeaderFilter.class);

    private static final String ANY = "*";

    @Override
    public int getOrder() {
        // Specify that this filter runs after NettyWriteResponseFilter
        // That is, process response headers after response body is processed
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER + 1;
    }

    @Override
    @SuppressWarnings("serial")
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            exchange.getResponse().getHeaders().entrySet().stream()
                    .filter(kv -> (kv.getValue() != null && kv.getValue().size() > 1))
                    .filter(kv -> (kv.getKey().equals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)
                            || kv.getKey().equals(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS)
                            || kv.getKey().equals(HttpHeaders.VARY)))
                    .forEach(kv ->
                    {
                        // For Vary header, just remove duplicates
                        if(kv.getKey().equals(HttpHeaders.VARY)) {
                            List<String> collect = kv.getValue().stream().distinct().collect(Collectors.toList());
                            if(kv.getValue().size() > collect.size()) {
                                kv.setValue(collect);
                            }
                        } else {
                            List<String> value = new ArrayList<>();
                            if(kv.getValue().contains(ANY)) {  // If contains '*', keep '*'
                                value.add(ANY);
                                kv.setValue(value);
                            } else {
                                value.add(kv.getValue().get(0)); // Otherwise, take the first value
                                kv.setValue(value);
                            }
                        }
                    });
        }));
    }
}
