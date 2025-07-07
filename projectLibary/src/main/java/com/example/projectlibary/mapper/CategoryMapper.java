package com.example.projectlibary.mapper;

import com.example.projectlibary.dto.reponse.CategoryResponse;
import com.example.projectlibary.model.Category;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {
    public CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }
      return  CategoryResponse.builder()
              .id(category.getId())
              .name(category.getName())
                .build();
    }
    public List<CategoryResponse> toResponseList(List<Category> categories) {
        if (categories == null) {
            return Collections.emptyList(); // Hoặc List.of() cho Java 9+
        }
        return categories.stream()
                .map(this::toResponse) // Tái sử dụng phương thức toResponse đã có
                .collect(Collectors.toList());
    }
}
