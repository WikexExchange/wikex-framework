package com.wikex.wikex.api.permission;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.api.util.IpUtil;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.permission.entity.Permission;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.JwtToken;
import com.wikex.wikex.util.MD5;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class AuthorizationInterceptor {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /****
     * Check if the URI is valid path
     * 
     * @param uri
     * @return
     */
    public Boolean isInvalid(String uri) {
        RBloomFilter<String> uriBloomFilterArray = redissonClient.getBloomFilter("UriBloomFilterArray");
        return uriBloomFilterArray.contains(uri);
    }

    /***
     * Permission verification
     */
    public Boolean rolePermission(Map<String, Object> token, Permission permission) {
        // Match -> get roles set
        String[] roles = token.get("roles").toString().split(",");

        // Loop through roles to check permission
        for (String role : roles) {

            Set<Integer> permissions = (Set<Integer>) redisTemplate.boundHashOps("RolePermissionMap")
                    .get("Role_" + role);

            if (permissions == null) {
                continue;
            }
            // Check if permissions contain the permission id
            if (permissions.contains(permission.getId())) {
                return true;
            }
        }
        return false;
    }

    /***
     * Token validation
     */
    public Map<String, Object> tokenIntercept(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        // Validate other addresses
        String clientIp = IpUtil.getIp(request);
        // Get token
        String token = request.getHeaders().getFirst("x-auth-token");
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        // Verify token
        Map<String, Object> resultMap = this.jwtVerify(token, clientIp);
        return resultMap;
    }

    /***
     * Check if interception and validation are needed
     * true: need to intercept
     * false: do not intercept
     */
    public Permission isIntercept(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        // Get uri, e.g. /cart/list
        String uri = request.getURI().getPath();
        // HTTP method GET/POST/*
        String method = request.getMethodValue();
        // Service name
        URI routerUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        String servicename = routerUri.getHost();

        // Match from Redis cache
        // 0: exact match
        // List<Permission> permissionsMatch0 = (List<Permission>)
        // redisTemplate.boundHashOps("RolePermissionAll").get("PermissionMatch0");
        // Permission permission = null;
        // if(permissionsMatch0!=null){
        // 1: wildcard match
        Permission permission = match0(uri, method, servicename, "permission");
        // }
        // If permission is null, do wildcard match
        if (permission == null) {
            // Wildcard match like /cat/{string}/{integer}
            List<Permission> permissionsMatch1 = (List<Permission>) redisTemplate.boundHashOps("permission:1").values();
            if (permissionsMatch1 != null) {
                permission = match1(permissionsMatch1, uri, method, servicename);
            }
            // If wildcard match is also null, current request does not need permission
            // validation
        }
        // if(permission==null){
        // return false;
        // }
        return permission;
    }

    /***
     * Check if URL is forbidden
     * true: forbidden url
     * false: allowed url
     */
    public Permission isForbidUrl(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        // Get uri, e.g. /cart/list
        String uri = request.getURI().getPath();
        // HTTP method GET/POST/*
        String method = request.getMethodValue();
        // Service name
        URI routerUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        String servicename = routerUri.getHost();

        // Match from Redis cache
        // 0: exact match
        // List<Permission> permissionsMatch0 = (List<Permission>)
        // redisTemplate.boundHashOps("RolePermissionAll").get("PermissionMatch0");
        // Permission permission = null;
        // if(permissionsMatch0!=null){
        // 1: wildcard match
        Permission permission = match0(uri, method, servicename, "forbid");
        // }
        // If permission is null, do wildcard match
        if (permission == null) {
            // Wildcard match like /cat/{string}/{integer}
            List<Permission> permissionsMatch1 = (List<Permission>) redisTemplate.boundHashOps("forbid:1").values();
            if (permissionsMatch1 != null) {
                permission = match1(permissionsMatch1, uri, method, servicename);
            }
            // If wildcard match is also null, current request does not need permission
            // validation
        }
        // if(permission==null){
        // return false;
        // }
        return permission;
    }

    /***
     * Match method: exact match
     */
    public Permission match0(String uri, String method, String serviceName, String prefix) {
        Object p = redisTemplate.boundHashOps(prefix + ":0").get(serviceName + uri);
        if (!ObjectUtils.isEmpty(p)) {
            Permission permission = (Permission) p;
            String matchMethod = permission.getMethod();
            if ("*".equals(matchMethod) && serviceName.equals(permission.getServiceName())) {
                return permission;
            }
            if (matchMethod.equals(method) && serviceName.equals(permission.getServiceName())) {
                return permission;
            }
        }
        return null;
    }

    /***
     * Match method: wildcard match
     */
    public Permission match1(List<Permission> permissionsMatch1, String uri, String method, String serviceName) {
        for (Permission permission : permissionsMatch1) {
            String matchUrl = permission.getUrl();
            String matchMethod = permission.getMethod();
            if (matchUrl(matchUrl, uri)) {
                // Check method and service match
                if (matchMethod.equals(method) && serviceName.equals(permission.getServiceName())) {
                    return permission;
                }
                if ("*".equals(matchMethod) && serviceName.equals(permission.getServiceName())) {
                    return permission;
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String url = "member/u/{id}";
        PathMatcher matcher = new AntPathMatcher();
        System.out.println(matcher.match(url, "member/u/123"));
    }

    public boolean matchUrl(String patternUrl, String requestUrl) {
        if (StringUtils.isEmpty(patternUrl) || StringUtils.isEmpty(requestUrl)) {
            return false;
        }
        PathMatcher matcher = new AntPathMatcher();
        return matcher.match(patternUrl, requestUrl);
    }

    /*****
     * Token parsing
     */
    public Map<String, Object> jwtVerify(String token, String clientIp) {
        try {
            // Parse token
            Map<String, Object> dataMap = JwtToken.parseToken(token);

            if (dataMap != null) {
                // Get user token
                AuthMember member = JSON.parseObject(dataMap.get(SysConstant.SESSION_MEMBER).toString(),
                        AuthMember.class);
                String key = SysConstant.TOKEN_MEMBER + member.getId() + "_" + clientIp;
                Object rToke = redisTemplate.boundValueOps(key).get();
                if (rToke != null && token.equals(rToke.toString())) {
                    String ip = dataMap.get("ip").toString();
                    if (ip.equals(clientIp)) {
                        return dataMap;
                    } else {
                        System.out.println("IP address mismatch");
                        // 401 error
                        return null;
                    }
                } else
                    return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
