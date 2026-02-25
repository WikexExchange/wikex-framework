package com.wikex.wikex.screen;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author 43494
 */
@Data
public class ConvertOrderScreen extends PageParam{

    private String fromUnit;
    private String toUnit;

    private Long memberId ;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date startTime;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date endTime;

}
