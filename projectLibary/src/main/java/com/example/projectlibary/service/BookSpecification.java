package com.example.projectlibary.service;

import com.example.projectlibary.common.SearchOperation;
import com.example.projectlibary.model.Author;
import com.example.projectlibary.model.Book;
import com.example.projectlibary.model.Category;
import com.example.projectlibary.model.SearchCriteria;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    private BookSpecification() {
    }


    public static Specification<Book> fromCriteria(List<SearchCriteria> criteriaList) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            for (SearchCriteria criteria : criteriaList) {
                String key = criteria.getKey();
                String valueStr = criteria.getValue().toString();
                SearchOperation operation = criteria.getOperation();

                if ("authorName".equalsIgnoreCase(key) || "authors".equalsIgnoreCase(key)) {
                    Join<Book, Author> authorJoin = root.join("authors");
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(authorJoin.get("fullName")),
                            "%" + valueStr.toLowerCase() + "%"
                    ));

                    continue;
                }

                if ("categoryName".equalsIgnoreCase(key) || "category".equalsIgnoreCase(key)) {
                    Join<Book, Category> categoryJoin = root.join("category");
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(categoryJoin.get("name")),
                            "%" + valueStr.toLowerCase() + "%"
                    ));
        
                    continue;
                }
                try {
                   
                    Object value = convertValueForBookEntity(key, valueStr);

                    switch (operation) {
                        case EQUAL:
                            predicates.add(criteriaBuilder.equal(root.get(key), value));
                            break;
                        case LIKE:
                            if (root.get(key).getJavaType() == String.class) {
                                predicates.add(criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get(key)),
                                        "%" + value.toString().toLowerCase() + "%"));
                            }
                            break;
                        case GREATER_THAN:
                            if (value instanceof Comparable) {
                                predicates.add(criteriaBuilder.greaterThan(root.get(key), (Comparable) value));
                            }
                            break;
                        case LESS_THAN:
                            if (value instanceof Comparable) {
                                predicates.add(criteriaBuilder.lessThan(root.get(key), (Comparable) value));
                            }
                            break;
                        case GREATER_THAN_OR_EQUAL:
                            if (value instanceof Comparable) {
                                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(key), (Comparable) value));
                            }
                            break;
                        case LESS_THAN_OR_EQUAL:
                            if (value instanceof Comparable) {
                                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(key), (Comparable) value));
                            }
                            break;
                    }
                } catch (Exception e) {
                    // Log lỗi nếu có vấn đề với việc chuyển đổi hoặc tìm trường
                    System.err.println("Skipping filter due to error: " + e.getMessage());
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    // CÁC HÀM HELPER NÀY CHỈ LÀM VIỆC VỚI BOOK ENTITY
    private static Class<?> getFieldTypeOfBookEntity(String fieldName) {
        try {
            return Book.class.getDeclaredField(fieldName).getType();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Could not find field '" + fieldName + "' in class Book", e);
        }
    }

    private static Object convertValueForBookEntity(String fieldName, String rawValue) {
        Class<?> fieldType = getFieldTypeOfBookEntity(fieldName);

        if (fieldType == String.class) return rawValue;
        if (fieldType == Integer.class || fieldType == int.class) return Integer.parseInt(rawValue);
        if (fieldType == Long.class || fieldType == long.class) return Long.parseLong(rawValue);
        if (fieldType == BigDecimal.class) return new BigDecimal(rawValue);
        if (fieldType == Double.class || fieldType == double.class) return Double.parseDouble(rawValue);
        if (fieldType == Boolean.class || fieldType == boolean.class) return Boolean.parseBoolean(rawValue);
        if (fieldType == LocalDate.class) return LocalDate.parse(rawValue);
        if (fieldType == LocalDateTime.class) return LocalDateTime.parse(rawValue);

        // Dòng này chỉ được gọi nếu fieldType không được xử lý ở trên
        throw new IllegalArgumentException("Could not convert value '" + rawValue + "' to field type '" + fieldType.getName() + "' for field '" + fieldName + "'");
    }
}
