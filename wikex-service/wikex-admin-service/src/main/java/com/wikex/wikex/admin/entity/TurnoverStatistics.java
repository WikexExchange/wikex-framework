package com.wikex.wikex.admin.entity;

import com.wikex.wikex.constant.TransactionTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Document(collection = "turnover_statistics")
@ToString
public class TurnoverStatistics {
    /**
     * Transaction date: counted with day as the smallest unit
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date date ;

    private int year ;

    @Max(value = 12)
    @Min(value = 1)
    private int month ;

    @Max(value = 31)
    @Min(value = 1)
    private int day ;

    @Enumerated(value = EnumType.STRING)
    private TransactionTypeEnum type ;

    /**
     * Trading pair unit: platform-designated legal currency
     */
    private String unit;

    /**
     * Trading volume of the current day: quantity of the coin (coinUnit)
     */
    @Column(name = "Salary1", columnDefinition = "decimal(18,8)")
    private BigDecimal amount ;

    /**
     * Transaction fee collected in coins on the current day
     */
    @Column(name = "Salary1", columnDefinition = "decimal(18,8)")
    private BigDecimal fee ;

}
