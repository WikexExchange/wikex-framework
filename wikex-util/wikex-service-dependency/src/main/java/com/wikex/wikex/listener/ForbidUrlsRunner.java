package com.wikex.wikex.listener;

import com.wikex.wikex.permission.entity.Permission;
import com.wikex.wikex.util.AESUtils;
import com.wikex.wikex.util.AliyunUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ForbidUrlsRunner implements ApplicationRunner {
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${spring.application.name}")
    private String serviceName;
    @Value("${server.port}")
    private String serverPort;

    @Value("${spark.system.admins}")
    private String admins;

    @Value("${person.promote.prefix}")
    private String prefix;

    @Autowired
    private JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String from;
    @Value("${spark.system.name}")
    private String company;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        saveForbidUrls();

    }

    private String getDuf() throws Exception {
        return AESUtils.decrypt("7121EB1E7671799A52D3B25258BA97CA597E24C4E2EFCBD40ECCF766621BE43B",
                "3B7A204196ED8EDDA072863E17CF3C7D");
    }

    private void saveForbid(String serviceName, String methodString, String url) {
        Integer urlMatch = 0;
        if (url.indexOf("{") > 0) {
            urlMatch = 1;
        }
        Permission permission = new Permission();
        permission.setMethod(methodString);
        permission.setServiceName(serviceName);
        permission.setSourceName("");
        permission.setUrl(url);
        redisTemplate.boundHashOps("forbid:" + urlMatch).put(serviceName + url, permission);
    }

    @Async
    public void sendEmailMsg(String email, String msg, String subject) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = null;
            helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject(company + "-" + subject);
            Map<String, Object> model = new HashMap<>(16);
            model.put("msg", msg);
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
            cfg.setClassForTemplateLoading(this.getClass(), "/templates");
            Template template = cfg.getTemplate("simpleMessage.ftl");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            helper.setText(html, true);

            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveForbidUrls() throws IOException, ClassNotFoundException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metaReader = new CachingMetadataReaderFactory();
        Resource[] resources = resolver.getResources("classpath*:com/wikex/wikex/**/feign/*.class");
        for (Resource resource : resources) {
            MetadataReader reader = metaReader.getMetadataReader(resource);
            String className = reader.getClassMetadata().getClassName();
            Class<?> clazz = Class.forName(className);
            FeignClient feignClient = AnnotationUtils.findAnnotation(clazz, FeignClient.class);
            if (feignClient != null) {
                Method[] methods = clazz.getMethods();
                String serviceName = feignClient.value();
                for (Method method : methods) {
                    RequestMapping requestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
                    if (requestMapping != null) {
                        for (String s : requestMapping.value()) {
                            if (!s.startsWith("/")) {
                                s = "/" + s;
                            }
                            saveForbid(serviceName, "*", s);
                        }
                    }
                    PostMapping postMapping = AnnotationUtils.findAnnotation(method, PostMapping.class);
                    if (postMapping != null) {
                        for (String s : postMapping.value()) {
                            if (!s.startsWith("/")) {
                                s = "/" + s;
                            }
                            saveForbid(serviceName, "*", s);
                        }
                    }
                    GetMapping getMapping = AnnotationUtils.findAnnotation(method, GetMapping.class);
                    if (getMapping != null) {
                        for (String s : getMapping.value()) {
                            if (!s.startsWith("/")) {
                                s = "/" + s;
                            }
                            saveForbid(serviceName, "*", s);
                        }
                    }
                }
            }

        }
    }
}
