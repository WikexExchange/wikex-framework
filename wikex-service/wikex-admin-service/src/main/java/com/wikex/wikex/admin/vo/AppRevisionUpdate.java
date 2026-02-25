package com.wikex.wikex.admin.vo;

import com.wikex.wikex.admin.entity.AppRevision;
import com.wikex.wikex.constant.Platform;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;


@Data
public class AppRevisionUpdate{

    private String remark;

    private String version;

    private String downloadUrl;

    private Platform platform;

    public AppRevision transformation(AppRevision appRevision) {
        if (StringUtils.isNotBlank(remark)) {
            appRevision.setRemark(remark);
        }
        if (StringUtils.isNotBlank(version)) {
            appRevision.setVersion(version);
        }
        if (StringUtils.isNotBlank(downloadUrl)) {
            appRevision.setDownloadUrl(downloadUrl);
        }
        if (platform != null) {
            appRevision.setPlatform(platform);
        }
        return appRevision;
    }


}
