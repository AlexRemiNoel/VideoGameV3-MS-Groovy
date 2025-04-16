package com.champsoft.purchasemanagement.BusinessLogic;


import com.champsoft.gamemanagement.DataAccess.Game;
import com.champsoft.purchasemanagement.DataAccess.CartRepository;
import com.champsoft.purchasemanagement.DataAccess.Order;
import com.champsoft.purchasemanagement.DataMapper.OrderRequestMapper;
import com.champsoft.purchasemanagement.Presentation.OrderRequestModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@AllArgsConstructor
public class CartService {
    private CartRepository cartRepository;
    private final OrderRequestMapper orderRequestMapper;
    private final OrderGameClient orderGameClient;


    public void makePurchase(String cartId, String userId) {
        List<String> gameIds = cartRepository.findCartByCartId_Uuid(cartId).getGames();
        List<Game> games = orderGameClient.getGamesFromGameList(gameIds);
        User user = orderGameClient.getUserFromUserManagement(userId);
        double total = 0;
        for(Game game : games){
            total += game.getPrice();
        }

        OrderRequestModel orderRequestModel = new OrderRequestModel(total);
        Order order = orderRequestMapper.orderRequestModelToOrder(orderRequestModel, userId);
        order.setUser(user);


        orderGameClient.putUserBalance(userId, user.getBalance()-total);

    }

    public void addGame(String cartId, String gameId){
        cartRepository.findCartByCartId_Uuid(cartId).getGames().add(gameId);
    }

}