package com.wikex.wikex.api.limit;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/***
 * Rate limiting based on IP
 */
public class IpKeyResolver implements KeyResolver {

    /**
     * Use IP as the rate limiting key
     * @param exchange
     * @return
     */
    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        // Get client IP
        return Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
    }
}
