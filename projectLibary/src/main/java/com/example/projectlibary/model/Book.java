package com.example.projectlibary.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "books")
@Builder
public class Book extends AbstractEntity {

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "isbn", length = 20, unique = true)
    private String isbn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_books_category",
            foreignKeyDefinition = "FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL ON UPDATE CASCADE"))
    private Category category;

    @Column(name = "publisher", length = 255)
    private String publisher;

    @Column(name = "publication_year") // SQL YEAR type
    private Integer publicationYear;

    @Column(name = "ebook_url", length = 512)
    private String ebookUrl; // URL/Path đến file sách điện tử

    @Column(name = "replacement_cost", precision = 10, scale = 2)
    private BigDecimal replacementCost; // Chi phí đền bù nếu mất/hỏng

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", foreignKey = @ForeignKey(name = "fk_books_created_by",
            foreignKeyDefinition = "FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE"))
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", foreignKey = @ForeignKey(name = "fk_books_updated_by",
            foreignKeyDefinition = "FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE"))
    private User updatedBy;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id"),
            foreignKey = @ForeignKey(name = "fk_bookauthors_book"),
            inverseForeignKey = @ForeignKey(name = "fk_bookauthors_author")
    )
    private Set<Author> authors = new HashSet<>();

    @OneToMany(mappedBy = "book")
    private Set<BookCopy> bookCopies;

    @OneToMany(mappedBy = "book")
    private Set<BookReservation> reservations;

    @OneToMany(mappedBy = "book")
    private Set<BookReview> reviews;

    @OneToMany(mappedBy = "book")
    private Set<ReadingSession> readingSessions;
}
