package com.champsoft.purchasemanagement.DataAccess;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
  @EmbeddedId
  @Column(name = "order_id")
  private OrderId orderId;
  private LocalDateTime orderDate;
  private double totalPrice;

  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "order_user_id", nullable = true)
  private User user;
}
