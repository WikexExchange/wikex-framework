package com.wikex.wikex.admin.vo;

import com.wikex.wikex.admin.entity.DataDictionary;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;


@Data
public class DataDictionaryCreate {
    @NotBlank
    private String bond;
    @NotBlank
    private String value;
    private String comment;


    public DataDictionary transformation() {
        return new DataDictionary(bond, value, comment);
    }
}
