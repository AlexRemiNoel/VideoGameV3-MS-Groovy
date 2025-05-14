package com.champsoft.DomainClient.Dtos;

import lombok.Data;

@Data
public class GameClientResponseDto { // Mirrors GameResponseModel
    private String id;
    private String title;
    private double price;
    private String releaseDate;
    private String description;
    private String publisher;
    private String developer;
    private String genre;
    // private List<ReviewClientDto> reviews; // Probably not needed for summary
}