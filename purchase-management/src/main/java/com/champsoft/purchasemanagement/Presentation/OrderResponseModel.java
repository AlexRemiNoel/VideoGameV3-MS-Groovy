package com.champsoft.purchasemanagement.Presentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseModel {
    private String orderId;
    private String orderDate;
    private double totalPrice;
    private String userId;
}
