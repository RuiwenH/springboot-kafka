/*
 * Copyright (c) 2000-2019 All Rights Reserved.
 */
package com.reven.bo;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class Order {
    private String orderId;
    private Date orderDate;
    private BigDecimal amount;
    private String customerName;
}
