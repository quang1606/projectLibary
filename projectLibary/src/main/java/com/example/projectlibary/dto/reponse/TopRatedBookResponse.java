package com.example.projectlibary.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
public interface  TopRatedBookResponse {
//    private Long bookId;
//    private Long authorId;
//    private double weightedRating;
    Long getBookId();
    Long getAuthorId();
}
