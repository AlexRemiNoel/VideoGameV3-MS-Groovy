package com.champsoft.purchasemanagement.DataAccess;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderId {
    @Column(name = "order_id")
    private String uuid;
}
