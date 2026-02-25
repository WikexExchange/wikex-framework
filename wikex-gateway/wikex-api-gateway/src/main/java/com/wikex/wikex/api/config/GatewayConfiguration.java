package com.wikex.wikex.api.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class GatewayConfiguration {

    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    public GatewayConfiguration(ObjectProvider<List<ViewResolver>> viewResolversProvider,
                                ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    /**
     * Circuit breaker fallback exception handler
     * @return
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        // Register block exception handler for Spring Cloud Gateway.
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

    /**
     * Get current Route and apply Sentinel flow control rules accordingly
     * @return
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }


    /**
     * Load rules and APIs
     */
    @PostConstruct
    public void doInit(){
        initCustomizedApis();
        initGatewayRules();
    }

    /**
     * Define API groups
     */
    private void initCustomizedApis(){
        // Define a set to store API groups to be defined
        Set<ApiDefinition> definitions = new HashSet<ApiDefinition>();

        // Create each API and configure related rules
        ApiDefinition cartApi = new ApiDefinition("mall_cart_api")
                .setPredicateItems(new HashSet<ApiPredicateItem>(){{
                        // /cart/list
                        add(new ApiPathPredicateItem().setPattern("/cart/list"));
                        // /cart/** (matches all under /cart/)
                        add(new ApiPathPredicateItem().setPattern("/cart/**")
                        // Match strategy by prefix
                        .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});

        // Add the created API to the API set
        definitions.add(cartApi);
        // Manually load API definitions to Sentinel
        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
    }


    /**
     * Define rate limiting rules
     */
    public void initGatewayRules(){
        // Create a set to store all rules
        Set<GatewayFlowRule> rules = new HashSet<GatewayFlowRule>();

        // Create a new rule and add it to the set
        rules.add(new GatewayFlowRule("goods_route")
                // Request threshold
                .setCount(6)
                // Additional burst capacity
                .setBurst(2)
                // Rate limiting behavior
                // CONTROL_BEHAVIOR_RATE_LIMITER: smooth warm-up
                // CONTROL_BEHAVIOR_DEFAULT: fail fast
                //.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER)
                // Max queueing timeout (ms)
                //.setMaxQueueingTimeoutMs(10000)
                // Statistical time window in seconds, default is 1 second
                .setIntervalSec(30));

        // Create another new rule and add it to the set
        rules.add(new GatewayFlowRule("mall_cart_api")
                // Request threshold
                .setCount(2)
                // Statistical time window in seconds, default is 1 second
                .setIntervalSec(2));
        // Manually load the rules configuration
        GatewayRuleManager.loadRules(rules);
    }
}
