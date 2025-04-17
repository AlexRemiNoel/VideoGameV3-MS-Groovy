package com.champsoft.gamemanagement.Presentation.DTOS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameRequestModel {
    private String title;
    private double price;
    private String description;
    private String publisher;
    private String developer;
    private String genre;
}
