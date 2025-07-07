package com.example.projectlibary.dto.reponse;

import lombok.*;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link com.example.projectlibary.model.Book}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookSummaryResponse implements Serializable {
     Long id;
     String title;
     String isbn;
     Integer publicationYear;
     String thumbnail;
    // Làm phẳng, chỉ lấy tên tác giả
     Set<String> authorNames;
    // Làm phẳng, chỉ lấy tên thể loại
     String categoryName;
     Long loanCount;
    double averageRating;
}