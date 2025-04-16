
package com.champsoft.purchasemanagement.BusinessLogic;


import com.champsoft.gamemanagement.DataAccess.Game;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class OrderGameClient {
  private RestTemplate restTemplate;
  private ObjectMapper objectMapper;
  private final String USER_MANAGEMENT_BASE_URL = "http://localhost:8080/api/v1/user";
  private final String GAME_MANAGEMENT_BASE_URL = "http://localhost:8080/api/v1/game";


  public OrderGameClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
  }

  public User getUserFromUserManagement(String uuid) {
    String url = USER_MANAGEMENT_BASE_URL + "/" + uuid;
    try {
      String response = restTemplate.getForObject(url, String.class);
      return objectMapper.readValue(response, User.class);
    } catch (HttpClientErrorException ex) {
      if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
        return null;
      } else {
        throw new RuntimeException("Error fetching user: " + ex.getMessage()); // Or a custom exception
      }
    } catch (Exception ex) {
      throw new RuntimeException("Error fetching user: " + ex.getMessage()); // Or a custom exception
    }
  }

  public void putUserBalance(String uuid, double newBalance) {
    String url = USER_MANAGEMENT_BASE_URL + "/" + uuid + "/balance/" + newBalance;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> payload = Collections.singletonMap("balance", newBalance);

    HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

    try {
      ResponseEntity<User> responseEntity = restTemplate.exchange(
              url,
              HttpMethod.PUT, // Using PUT
              requestEntity,
              User.class // Expecting the updated User object back
      );
    } catch (HttpClientErrorException ex) {
      if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
        System.err.println("User not found for balance update (PUT): " + uuid);
        System.err.println(ex.getMessage());
      } else {
        System.err.println("HTTP Error putting balance: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
        throw new RuntimeException("Error putting user balance: Status " + ex.getStatusCode(), ex);
      }
    } catch (Exception ex) {
      System.err.println("Generic error putting balance: " + ex.getMessage());
      throw new RuntimeException("Error putting user balance: " + ex.getMessage(), ex);
    }
  }

  public List<Game> getGamesFromGameList(List<String> gameIds){
    List<Game> games = new ArrayList<>();
    for (String gameId : gameIds) {
      games.add(getGameFromGameManagement(gameId));
    }
    return games;

  }

  public Game getGameFromGameManagement(String uuid) {
    String url = GAME_MANAGEMENT_BASE_URL + "/" + uuid;
    try {
      String response = restTemplate.getForObject(url, String.class);
      return objectMapper.readValue(response, Game.class);
    } catch (HttpClientErrorException ex) {
      if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
        return null;
      } else {
        throw new RuntimeException("Error fetching user: " + ex.getMessage()); // Or a custom exception
      }
    } catch (Exception ex) {
      throw new RuntimeException("Error fetching user: " + ex.getMessage()); // Or a custom exception
    }
  }

}
