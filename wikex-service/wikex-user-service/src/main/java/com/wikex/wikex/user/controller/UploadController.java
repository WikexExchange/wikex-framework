package com.wikex.wikex.user.controller;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.config.AliyunConfig;
import com.wikex.wikex.user.config.S3Config;
import com.wikex.wikex.util.FileUtil;
import com.wikex.wikex.util.GeneratorUtil;
import com.wikex.wikex.util.MessageResult;
import com.wikex.wikex.util.UploadFileUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@Slf4j
public class UploadController {

    private String allowedFormat = ".jpg,.gif,.png,.jpeg";

    @Autowired
    private AliyunConfig aliyunConfig;
    @Autowired
    private S3Config s3Config;
    @Autowired
    private LocaleMessageSourceService sourceService;

    @Value("${oss.name}")
    private String ossName;

    @ApiOperation(value = "Upload image to Aliyun OSS")
    @PermissionOperation
    @RequestMapping(value = "upload/oss/image", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult uploadOssImage(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("file") MultipartFile file) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        Assert.isTrue(ServletFileUpload.isMultipartContent(request), sourceService.getMessage("FORM_FORMAT_ERROR"));
        Assert.isTrue(file != null, sourceService.getMessage("NOT_FIND_FILE"));

        String fileType = UploadFileUtil.getFileType(file.getInputStream());
        System.out.println("fileType=" + fileType);
        // String directory = new SimpleDateFormat("yyyy/MM/dd/").format(new Date());

        try {
            String fileName = file.getOriginalFilename();
            String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
            System.out.println("suffix=" + suffix);
            if (!allowedFormat.contains(suffix.trim().toLowerCase())) {
                return MessageResult.error(sourceService.getMessage("FORMAT_NOT_SUPPORT"));
            }
            if (fileType == null || !allowedFormat.contains(fileType.trim().toLowerCase())) {
                return MessageResult.error(sourceService.getMessage("FORMAT_NOT_SUPPORT"));
            }
            // String key = directory + GeneratorUtil.getUUID() + suffix;
            String key = GeneratorUtil.getUUID() + suffix;
            System.out.println(key);
            // Compress file
            String path = request.getSession().getServletContext().getRealPath("/") + "upload/"
                    + file.getOriginalFilename();
            File tempFile = new File(path);
            FileUtils.copyInputStreamToFile(file.getInputStream(), tempFile);

            UploadFileUtil.zipWidthHeightImageFile(tempFile, tempFile, 425, 638, 0.7f);

            return doUpload(file, key);

        } catch (OSSException oe) {
            return MessageResult.error(500, oe.getErrorMessage());
        } catch (ClientException ce) {
            System.out.println("Error Message: " + ce.getMessage());
            return MessageResult.error(500, ce.getErrorMessage());
        } catch (Throwable e) {
            e.printStackTrace();
            return MessageResult.error(500, sourceService.getMessage("SYSTEM_ERROR"));
        }
    }

    @ApiOperation(value = "Upload image to local server")
    @RequestMapping(value = "upload/local/image", method = RequestMethod.POST)
    @ResponseBody
    @PermissionOperation
    public MessageResult uploadLocalImage(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("file") MultipartFile file) throws IOException {

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        Assert.isTrue(ServletFileUpload.isMultipartContent(request), sourceService.getMessage("FORM_FORMAT_ERROR"));
        Assert.isTrue(file != null, sourceService.getMessage("NOT_FIND_FILE"));
        // Verify file type
        String fileName = file.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
        if (!allowedFormat.contains(suffix.trim().toLowerCase())) {
            return MessageResult.error(sourceService.getMessage("FORMAT_NOT_SUPPORT"));
        }
        String result = UploadFileUtil.uploadFile(file, fileName);
        if (result != null) {
            MessageResult mr = new MessageResult(0, sourceService.getMessage("UPLOAD_SUCCESS"));
            mr.setData(result);
            return mr;
        } else {
            MessageResult mr = new MessageResult(0, sourceService.getMessage("FAILED_TO_WRITE"));
            mr.setData(result);
            return mr;
        }
    }

    @RequestMapping(value = "/upload/oss/base64", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult base64UpLoad(@RequestParam String base64Data) {
        MessageResult result = new MessageResult();
        try {
            log.debug("Uploaded file data: " + base64Data);
            String dataPrix = "";
            String data = "";
            if (base64Data == null || "".equals(base64Data)) {
                throw new Exception(sourceService.getMessage("NOT_FIND_FILE"));
            } else {
                String[] d = base64Data.split("base64,");
                if (d != null && d.length == 2) {
                    dataPrix = d[0];
                    data = d[1];
                } else {
                    throw new Exception(sourceService.getMessage("DATA_ILLEGAL"));
                }
            }
            log.debug("Parse data to get filename and stream data");
            String suffix = "";
            if ("data:image/jpeg;".equalsIgnoreCase(dataPrix)) { // JPEG image data
                suffix = ".jpg";
            } else if ("data:image/x-icon;".equalsIgnoreCase(dataPrix)) { // ICON image data
                suffix = ".ico";
            } else if ("data:image/gif;".equalsIgnoreCase(dataPrix)) { // GIF image data
                suffix = ".gif";
            } else if ("data:image/png;".equalsIgnoreCase(dataPrix)) { // PNG image data
                suffix = ".png";
            } else {
                throw new Exception(sourceService.getMessage("FORMAT_NOT_SUPPORT"));
            }
            String directory = new SimpleDateFormat("yyyy/MM/dd/").format(new Date());
            String key = directory + GeneratorUtil.getUUID() + suffix;

            // Because of BASE64Decoder jar issue, use Spring framework's utility instead
            byte[] bs = Base64Utils.decodeFromString(data);
            return doUpload(key, bs);
        } catch (Exception e) {
            log.debug("Upload failed, " + e.getMessage());
            result.setCode(500);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    private MessageResult doUpload(MultipartFile file, String key) {
        log.debug("Upload selection " + ossName);
        if ("s3".equals(ossName)) {
            return s3Upload(file, key);
        } else {
            return ossUpload(file, key);
        }
    }

    private MessageResult doUpload(String key, byte[] bs) {
        if ("s3".equals(ossName)) {
            return s3Upload(key, bs);
        } else {
            return ossUpload(key, bs);
        }
    }

    private MessageResult s3Upload(MultipartFile file, String key) {
        log.debug("Start upload::" + ossName);
        try {
            // DigitalOcean Spaces endpoint
            String endpoint = "https://" + s3Config.getRegionsName() + ".digitaloceanspaces.com";

            AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                    .withEndpointConfiguration(
                            new AwsClientBuilder.EndpointConfiguration(endpoint, "us-east-1"))
                    .withCredentials(new AWSStaticCredentialsProvider(
                            new BasicAWSCredentials(s3Config.getAccessKeyId(), s3Config.getAccessKeySecret())))
                    .withPathStyleAccessEnabled(true)
                    .disableChunkedEncoding()
                    .build();

            File realFile = FileUtil.multipartFileToFile(file);

            PutObjectRequest putRequest = new PutObjectRequest(
                    s3Config.getBucketName(), key, realFile).withCannedAcl(CannedAccessControlList.PublicRead);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/jpeg");
            putRequest.setMetadata(metadata);

            s3.putObject(putRequest);

            String url = s3Config.toUrl(key);
            MessageResult mr = new MessageResult(0, sourceService.getMessage("UPLOAD_SUCCESS"));
            mr.setData(url);
            return mr;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, sourceService.getMessage("REQUEST_FAILED"));
        }
    }

    private MessageResult s3Upload(String key, byte[] bs) {
        String[] split = s3Config.getRegionsName().split("-");
        String regionName = "";
        for (String s : split) {
            regionName = regionName + s.toUpperCase() + "_";
        }
        regionName = regionName.substring(0, regionName.length() - 1);
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(s3Config.getAccessKeyId(),
                s3Config.getAccessKeySecret());
        AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(Regions.valueOf(regionName)).build();

        PutObjectRequest putRequest = null;
        try {
            InputStream is = new ByteArrayInputStream(bs);
            putRequest = new PutObjectRequest(s3Config.getBucketName(), key, is, new ObjectMetadata());
            s3.putObject(putRequest);
            String uri = s3Config.toUrl(key);
            MessageResult mr = new MessageResult(0, sourceService.getMessage("UPLOAD_SUCCESS"));
            mr.setData(uri);
            System.out.println("Upload successful...");
            return mr;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, sourceService.getMessage("REQUEST_FAILED"));
        }
    }

    private MessageResult ossUpload(MultipartFile file, String key) {
        OSSClient ossClient = new OSSClient(aliyunConfig.getOssEndpoint(), aliyunConfig.getAccessKeyId(),
                aliyunConfig.getAccessKeySecret());
        try {
            System.out.println(key);
            ossClient.putObject(aliyunConfig.getOssBucketName(), key, file.getInputStream());
            String uri = aliyunConfig.toUrl(key);
            MessageResult mr = new MessageResult(0, sourceService.getMessage("UPLOAD_SUCCESS"));
            mr.setData(uri);
            return mr;
        } catch (OSSException oe) {
            return MessageResult.error(500, oe.getErrorMessage());
        } catch (ClientException ce) {
            System.out.println("Error Message: " + ce.getMessage());
            return MessageResult.error(500, ce.getErrorMessage());
        } catch (Throwable e) {
            e.printStackTrace();
            return MessageResult.error(500, sourceService.getMessage("REQUEST_FAILED"));
        } finally {
            ossClient.shutdown();
        }
    }

    private MessageResult ossUpload(String key, byte[] bs) {
        OSSClient ossClient = new OSSClient(aliyunConfig.getOssEndpoint(), aliyunConfig.getAccessKeyId(),
                aliyunConfig.getAccessKeySecret());
        try {
            // Use Apache utility -> to handle stream
            InputStream is = new ByteArrayInputStream(bs);
            ossClient.putObject(aliyunConfig.getOssBucketName(), key, is);
            String uri = aliyunConfig.toUrl(key);
            MessageResult mr = new MessageResult(0, "Upload successful");
            mr.setData(uri);
            log.debug("Upload successful, key:{}", key);
            return mr;
        } catch (Exception ee) {
            ee.printStackTrace();
            return MessageResult.error(500, sourceService.getMessage("REQUEST_FAILED"));
        } finally {
            ossClient.shutdown();
        }
    }

}
