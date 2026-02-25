package com.wikex.wikex.admin.vo;

import com.wikex.wikex.admin.entity.AppRevision;
import com.wikex.wikex.constant.Platform;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;


@Data
public class AppRevisionCreate {

    private String remark;

    @NotBlank
    private String version;

    private String downloadUrl;

    @NotNull
    private Platform platform;

    public AppRevision transformation() {
        AppRevision appRevision = new AppRevision();
        appRevision.setRemark(this.remark);
        appRevision.setVersion(this.version);
        appRevision.setDownloadUrl(this.downloadUrl);
        appRevision.setPlatform(this.platform);
        return appRevision;
    }
}
