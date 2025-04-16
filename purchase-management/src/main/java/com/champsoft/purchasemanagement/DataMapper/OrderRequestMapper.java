package com.champsoft.purchasemanagement.DataMapper;



import com.champsoft.purchasemanagement.DataAccess.Order;
import com.champsoft.purchasemanagement.DataAccess.OrderId;
import com.champsoft.purchasemanagement.Presentation.OrderRequestModel;
import com.champsoft.usermanagement.DataAccess.UserId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UUID.class, OrderId.class, LocalDateTime.class, UserId.class })
public interface OrderRequestMapper {
    @Mapping(target = "orderId", expression = "java(new OrderId(UUID.randomUUID().toString()))")
    @Mapping(target = "orderDate", expression = "java(LocalDateTime.now())")
    @Mapping(target = "user", ignore = true)
    Order orderRequestModelToOrder(OrderRequestModel orderRequestModel, String uuid);
}
