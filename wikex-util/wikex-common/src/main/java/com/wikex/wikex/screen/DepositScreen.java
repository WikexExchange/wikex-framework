package com.wikex.wikex.screen;

import com.wikex.wikex.constant.CommonStatus;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
public class DepositScreen extends PageParam{

    private CommonStatus status;

}
