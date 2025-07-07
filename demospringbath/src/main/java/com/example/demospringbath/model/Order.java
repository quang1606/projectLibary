package com.example.demospringbath.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Entity
@Table(name = "orders") // Tên bảng trong database
@Data // Lombok: tự tạo getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: tự tạo constructor không tham số
@AllArgsConstructor // Lombok: tự tạo constructor với tất cả tham số
public class Order {
    @Id
    private long id;
    private String name;
    private int quantity;
    private double price;
}
