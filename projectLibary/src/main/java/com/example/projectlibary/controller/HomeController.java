package com.example.projectlibary.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class HomeController {
    // Lắng nghe các request GET đến đường dẫn gốc "/"
    @GetMapping("api/auth")
    public String homePage(Model model) {

        // (Tùy chọn) Thêm một thuộc tính vào Model để truyền dữ liệu sang View
        model.addAttribute("message", "Đây là thông điệp từ Controller!");

        // Trả về tên của file template HTML (không bao gồm đuôi .html)
        // Spring Boot sẽ tự động tìm file /resources/templates/index.html
        return "index";
    }
}
