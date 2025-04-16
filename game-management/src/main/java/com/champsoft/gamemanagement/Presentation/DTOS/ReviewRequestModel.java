package com.champsoft.gamemanagement.Presentation.DTOS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestModel {
    String comment;
    String rating;
}
