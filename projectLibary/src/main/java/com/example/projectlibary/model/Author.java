package com.example.projectlibary.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "authors")
public class Author implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;
    @Column(name = "avatar")
    private String avatar;
    @Lob // Thích hợp cho kiểu TEXT
    @Column(name = "bio", columnDefinition = "TEXT") // columnDefinition là tùy chọn
    private String bio; // Tiểu sử tác giả
    @ManyToMany(mappedBy = "authors")
    private Set<Book> books = new HashSet<>();
}
