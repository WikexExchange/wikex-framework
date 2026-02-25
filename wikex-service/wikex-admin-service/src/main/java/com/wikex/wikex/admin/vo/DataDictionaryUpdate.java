package com.wikex.wikex.admin.vo;

import com.wikex.wikex.admin.entity.DataDictionary;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;


@Data
public class DataDictionaryUpdate {
    @NotBlank
    private String value;
    private String comment;

    public DataDictionary transformation(DataDictionary dataDictionary) {
        dataDictionary.setValue(value);
        dataDictionary.setComment(comment);
        return dataDictionary;
    }
}
