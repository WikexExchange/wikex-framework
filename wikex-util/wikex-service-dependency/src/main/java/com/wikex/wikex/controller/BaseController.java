package com.wikex.wikex.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.util.MessageResult;
import com.wikex.wikex.vo.SpecialPage;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


public class BaseController {
    private static Log log = LogFactory.getLog(BaseController.class);

    protected MessageResult success() {
        return new MessageResult(0, "SUCCESS");
    }

    protected MessageResult success(String msg) {
        return new MessageResult(0, msg);
    }

    protected MessageResult success(String msg, Object obj) {
        MessageResult mr = new MessageResult(0, msg);
        mr.setData(obj);
        return mr;
    }

    protected MessageResult success(Object obj) {
        MessageResult mr = new MessageResult(0, "SUCCESS");
        mr.setData(obj);
        return mr;
    }

    protected MessageResult error(String msg) {
        return new MessageResult(500, msg);
    }

    protected MessageResult error(int code, String msg) {
        return new MessageResult(code, msg);
    }

    protected <T> Page<T> IPage2Page(IPage<T> iPage){
        Page<T> page = new Page<T>() {
            @Override
            public int getTotalPages() {
                Long t = iPage.getPages();
                return t.intValue();
            }

            @Override
            public long getTotalElements() {
                return iPage.getTotal();
            }

            @Override
            public <U> org.springframework.data.domain.Page<U> map(Function<? super T, ? extends U> function) {
                
                List<T> content = getContent();

                
                List<U> mappedContent = content.stream()
                        .map(function)
                        .collect(Collectors.toList());

                
                return new org.springframework.data.domain.PageImpl<>(mappedContent, getPageable(), getTotalElements());
            }

            @Override
            public int getNumber() {
                Long number = iPage.getPages();
                return number.intValue();
            }

            @Override
            public int getSize() {
                Long size = iPage.getSize();
                return size.intValue();
            }

            @Override
            public int getNumberOfElements() {
                return 0;
            }

            @Override
            public List<T> getContent() {
                return iPage.getRecords();
            }

            @Override
            public boolean hasContent() {
                return iPage.getRecords().size()>0;
            }

            @Override
            public Sort getSort() {
                return Sort.by(Sort.Direction.DESC,"create_time");
            }

            @Override
            public boolean isFirst() {
                return false;
            }

            @Override
            public boolean isLast() {
                return false;
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }

            @Override
            public Pageable nextPageable() {
                return Pageable.unpaged();
            }

            @Override
            public Pageable previousPageable() {
                return Pageable.unpaged();
            }

            @Override
            public Iterator<T> iterator() {
                return iPage.getRecords().iterator();
            }
        };
        return page;

    }

    protected <T> SpecialPage<T> IPage2PageOtc(IPage<T> iPage){
        SpecialPage<T> page = new SpecialPage<T>() {
            @Override
            public int getTotalPage() {
                Long t = iPage.getPages();
                return t.intValue();
            }

            @Override
            public int getTotalElement() {
                Long total = iPage.getTotal();
                return total.intValue();
            }

            @Override
            public int getPageNumber() {
                Long number = iPage.getPages();
                return number.intValue();
            }

            @Override
            public int getCurrentPage() {
                Long current = iPage.getCurrent();
                return current.intValue();
            }

            @Override
            public List<T> getContext() {
                return iPage.getRecords();
            }


        };
        return page;

    }

    
    protected String request(HttpServletRequest request, String name) {
        return StringUtils.trimToEmpty(request.getParameter(name));
    }

    public String getRemoteIp(HttpServletRequest request) {
        if (StringUtils.isNotBlank(request.getHeader("X-Real-IP"))) {
            return request.getHeader("X-Real-IP");
        } else if (StringUtils.isNotBlank(request.getHeader("X-Forwarded-For"))) {
            return request.getHeader("X-Forwarded-For");
        } else if (StringUtils.isNotBlank(request.getHeader("Proxy-Client-IP"))) {
            return request.getHeader("Proxy-Client-IP");
        }
        return request.getRemoteAddr();
    }

    
    public boolean isMobile(HttpServletRequest request) {
        boolean isMobile = false;
        String[] mobileAgents = {"iphone", "android", "phone", "ipad", "mobile", "wap", "netfront", "java",
                "opera mobi", "opera mini", "ucweb", "windows ce", "symbian", "series", "webos", "sony", "blackberry",
                "dopod", "nokia", "samsung", "palmsource", "xda", "pieplus", "meizu", "midp", "cldc", "motorola",
                "foma", "docomo", "up.browser", "up.link", "blazer", "helio", "hosin", "huawei", "novarra", "coolpad",
                "webos", "techfaith", "palmsource", "alcatel", "amoi", "ktouch", "nexian", "ericsson", "philips",
                "sagem", "wellcom", "bunjalloo", "maui", "smartphone", "iemobile", "spice", "bird", "zte-", "longcos",
                "pantech", "gionee", "portalmmm", "jig browser", "hiptop", "benq", "haier", "^lct", "320x320",
                "240x320", "176x220", "w3c ", "acs-", "alav", "alca", "amoi", "audi", "avan", "benq", "bird", "blac",
                "blaz", "brew", "cell", "cldc", "cmd-", "dang", "doco", "eric", "hipt", "inno", "ipaq", "java", "jigs",
                "kddi", "keji", "leno", "lg-c", "lg-d", "lg-g", "lge-", "maui", "maxo", "midp", "mits", "mmef", "mobi",
                "mot-", "moto", "mwbp", "nec-", "newt", "noki", "oper", "palm", "pana", "pant", "phil", "play", "port",
                "prox", "qwap", "sage", "sams", "sany", "sch-", "sec-", "send", "seri", "sgh-", "shar", "sie-", "siem",
                "smal", "smar", "sony", "sph-", "symb", "t-mo", "teli", "tim-", "tsm-", "upg1", "upsi", "vk-v", "voda",
                "wap-", "wapa", "wapi", "wapp", "wapr", "webc", "winw", "winw", "xda", "xda-", "Googlebot-Mobile"};
        
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if ("SPARK_THEME".equals(cookie.getName().toUpperCase())) {
                    
                    isMobile = "WAP".equals(cookie.getValue());
                    break;
                }
            }
        }
        
        String userAgent = request.getHeader("User-Agent").toLowerCase();
        if (request.getHeader("User-Agent") != null) {
            for (String mobileAgent : mobileAgents) {
                if (userAgent.indexOf(mobileAgent) >= 0) {
                    
                    isMobile = true;
                    break;
                }
            }
        }
        return isMobile;
    }

}
