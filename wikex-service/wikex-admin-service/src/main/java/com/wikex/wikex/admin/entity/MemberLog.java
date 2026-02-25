package com.wikex.wikex.admin.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;

@Data
@Document(collection = "member_log")
@ToString
public class MemberLog {

    @JsonFormat(timezone = "GMX+8",pattern = "yyyy-MM-dd")
    private Date date ;

    private int year ;

    @Max(value = 12)
    @Min(value = 1)
    private int month ;

    @Max(value = 31)
    @Min(value = 1)
    private int day ;

    /**
     * Number of registrations on the current day
     */
    private int registrationNum = 0;

    /**
     * Number of real-name verifications on the current day
     */
    private int applicationNum = 0;

    /**
     * Number of certified merchants on the current day
     */
    private int bussinessNum = 0;
}
