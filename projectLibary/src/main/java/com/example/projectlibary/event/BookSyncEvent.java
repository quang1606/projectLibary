package com.example.projectlibary.event;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookSyncEvent {
    private String evenType;
    private Long id;
    private String title;
    private String isbn;
    private String description;
    private Integer availableCopyCount;
    private Set<String> authors;
    private Integer publicationYear;
    private String categoryName;

}
