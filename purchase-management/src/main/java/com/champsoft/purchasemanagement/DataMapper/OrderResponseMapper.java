package com.champsoft.purchasemanagement.DataMapper;



import com.champsoft.gamemanagement.DataAccess.Genre;
import com.champsoft.gamemanagement.Presentation.DTOS.GameRequestModel;
import com.champsoft.purchasemanagement.DataAccess.Order;
import com.champsoft.purchasemanagement.DataAccess.OrderId;
import com.champsoft.purchasemanagement.Presentation.OrderResponseModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UUID.class, GameRequestModel.class, OrderId.class, LocalDateTime.class, Genre.class})
public interface OrderResponseMapper {
    @Mapping(target = "orderId", expression = "java(order.getOrderId().getUuid())")
    @Mapping(target = "orderDate", expression = "java(order.getOrderDate().toString())")
    @Mapping(target = "userId", expression = "java(order.getUser().getUserId().getUuid())")
    OrderResponseModel orderToOrderResponseModel(Order order);
}
