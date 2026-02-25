package com.wikex.wikex.api.filter;

import com.wikex.wikex.api.permission.AuthorizationInterceptor;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.permission.entity.Permission;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ApiFilter implements GlobalFilter, Ordered {

    @Autowired
    private AuthorizationInterceptor authorizationInterceptor;

    /***
     * Execute interception processing
     * e.g. http://localhost:9001/mall/seckill/order?id&num
     * JWT token based
     * 
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Check if access is forbidden
        Permission forbidUrl = authorizationInterceptor.isForbidUrl(exchange);
        if (forbidUrl != null) {
            return endProcess(exchange, 403, "Not allowed to pass～url bad");
        }

        // Check if interception is needed
        Permission permission = authorizationInterceptor.isIntercept(exchange);
        if (permission == null) {
            return chain.filter(exchange);
        }
        // check lang in header
        ServerHttpRequest request = exchange.getRequest();
        String lang = request.getHeaders().getFirst("lang");
        if (lang == null) {
            lang = "en_US"; // default language or handle accordingly
            request = request.mutate().header("lang", lang).build();
        }
        // Token validation
        Map<String, Object> resultMap = authorizationInterceptor.tokenIntercept(exchange);

        if (resultMap == null) {
            // || !authorizationInterceptor.rolePermission(resultMap,permission) no longer
            // checked
            // Token validation failed or no permission
            return endProcess(exchange, 401, "Access denied");
        }

        ServerHttpRequest host = null;
        try {
            host = exchange.getRequest().mutate().header(SysConstant.SESSION_MEMBER,
                    URLEncoder.encode(resultMap.get(SysConstant.SESSION_MEMBER).toString(), "UTF-8")).build();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        // Replace the current request with the mutated one
        exchange = exchange.mutate().request(host).build();

        // Seckill filter (commented out)
        // if(uri.equals("/seckill/order")){
        // seckillFilter(exchange, request, resultMap.get("username").toString());
        // return chain.filter(exchange);
        // }

        // NOT_HOT handled directly by backend service
        return chain.filter(exchange);
    }

    /****
     * End processing
     * 
     * @param exchange
     * @param code
     * @param message
     */
    public Mono<Void> endProcess(ServerWebExchange exchange, Integer code, String message) {
        // Response status code 200
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("code", code);
        resultMap.put("message", message);
        return Mono.defer(() -> {
            byte[] bytes = new byte[0];
            try {
                bytes = new ObjectMapper().writeValueAsBytes(resultMap);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            ServerHttpResponse response = exchange.getResponse();
            response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Flux.just(buffer));
        });

    }

    @Override
    public int getOrder() {
        return 10001;
    }
}
