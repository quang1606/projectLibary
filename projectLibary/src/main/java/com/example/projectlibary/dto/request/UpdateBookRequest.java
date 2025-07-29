package com.example.projectlibary.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
public class UpdateBookRequest {
    // Tiêu đề: Có thể cập nhật, nhưng không được rỗng nếu được cung cấp
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    // Mô tả: Có thể cập nhật
    private String description;

    // ISBN: Có thể cập nhật, phải tuân thủ kích thước
    @Size(max = 20, message = "ISBN cannot exceed 20 characters")
    private String isbn;

    // ID Thể loại: Cập nhật bằng cách cung cấp ID mới
    private Long categoryId;

    // Nhà xuất bản: Có thể cập nhật
    @Size(max = 255, message = "Publisher cannot exceed 255 characters")
    private String publisher;

    // Năm xuất bản: Cập nhật với các ràng buộc về năm hợp lệ
    @Min(value = 1000, message = "Publication year must be a valid year")
    @Max(value = 9999, message = "Publication year must be a valid year")
    private Integer publicationYear;

    // Chi phí đền bù: Cập nhật với các ràng buộc về giá trị
    @DecimalMin(value = "0.0", inclusive = false, message = "Replacement cost must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid format for replacement cost")
    private BigDecimal replacementCost;

    // Danh sách ID tác giả: Cập nhật bằng cách cung cấp một danh sách ID mới
    // Có thể là danh sách rỗng để xóa tất cả tác giả, nhưng không được là null
    private Set<Long> authorIds;

    // Lưu ý: Các file (thumbnail, ebook) không được đưa vào DTO này.
    // Chúng sẽ được xử lý riêng dưới dạng MultipartFile trong Controller.
}

