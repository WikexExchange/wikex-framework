package com.wikex.wikex.user.vo;

import lombok.Data;

@Data
public class PaymentTypeConfig {
    private String fieldName;    // Field name
    private Boolean require;     // Whether required
    private String showText;     // Display name
    private String placeholder;  // Placeholder text in input box
    private String type;         // Type: input (text box), image, tip (tooltip)
}
