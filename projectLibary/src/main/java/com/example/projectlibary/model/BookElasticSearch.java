package com.example.projectlibary.model;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

@Document(indexName="book")
@Setting(settingPath = "elasticsearch/analyzer.json") // Trỏ đến file cấu hình analyzer
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookElasticSearch implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    private Long id;
    @Field(name = "title",type = FieldType.Text,analyzer = "vietnamese_analyzer",searchAnalyzer = "vietnamese_analyzer")
    private String title;
    @Field(name = "isbn", type = FieldType.Keyword)
    private String isbn;
    @Field(name = "description",type = FieldType.Text,analyzer = "vietnamese_analyzer",searchAnalyzer = "vietnamese_analyzer")
    private String description;
    @Field(name = "available_copy_count", type = FieldType.Integer)
    private Integer availableCopyCount;
    // Lưu một danh sách tên tác giả, hỗ trợ tìm kiếm theo nhiều tác giả
    @Field(name = "authors", type = FieldType.Text, analyzer = "vietnamese_analyzer", searchAnalyzer = "vietnamese_analyzer")
    private Set<String> authors;

    @Field(name = "publication_year", type = FieldType.Integer)
    private Integer publicationYear;

    @Field(name = "category_name", type = FieldType.Keyword)
    private String categoryName;

}
