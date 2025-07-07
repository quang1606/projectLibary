package com.example.projectlibary.repository;

import com.example.projectlibary.dto.reponse.BookLoanCountResponse;
import com.example.projectlibary.dto.reponse.BookRatingResponse;
import com.example.projectlibary.dto.reponse.TopRatedBookResponse;
import com.example.projectlibary.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> , JpaSpecificationExecutor<Book> {
    boolean existsByIsbn(String isbn);

    @Override
    @EntityGraph(attributePaths = {"authors", "category"})
    Page<Book> findAll(Pageable pageable);

    @Query(value = "SELECT NEW com.example.projectlibary.dto.reponse.BookLoanCountResponse(b.id, b.title, COUNT(bl.id)) " +
            "FROM Book b " +
            "LEFT JOIN b.bookCopies bc " +
            "LEFT JOIN bc.bookLoans bl " +
            "GROUP BY b.id, b.title " +
            "ORDER BY COUNT(bl.id) DESC, b.title ASC",
            countQuery = "SELECT COUNT(b) FROM Book b")
    Page<BookLoanCountResponse> findBookLoanCounts(Pageable pageable);

    @Query(value = "select b from Book b where b.id in :ids")
    List<Book> findByIdInWithDetails(@Param("ids") List<Long> ids);


    @EntityGraph(attributePaths = {"reviews"})
    Page<Book> findByOrderByPublicationYearDesc(Pageable pageable);

    @Query("SELECT NEW com.example.projectlibary.dto.reponse.BookLoanCountResponse(b.id, b.title, COUNT(bl.id)) " +
            "FROM Book b " +
            "LEFT JOIN b.bookCopies bc " +
            "LEFT JOIN bc.bookLoans bl " +
            "WHERE b.id IN :bookIds " +
            "GROUP BY b.id")
    List<BookLoanCountResponse> findLoanCountsForBooks(@Param("bookIds") List<Long> bookIds);

//    @Query( "select NEW com.example.projectlibary.dto.reponse.BookRatingResponse(b.id ,avg(bl.rating))"+
//            "from Book b "+
//            "left join BookReview bl "+
//            "WHERE b.id IN :bookIds " +
//             "GROUP BY b.id"   )
//    List<BookRatingResponse> findAverageRatingForBook(List<Long> bookIds);

// repository/BookRepository.java

// ...

    @Query("SELECT new com.example.projectlibary.dto.reponse.BookRatingResponse(b.id, COALESCE(AVG(r.rating), 0.0)) " +
            "FROM Book b LEFT JOIN b.reviews r " + // <--- SỬA LẠI THÀNH JOIN QUA THUỘC TÍNH 'reviews'
            "WHERE b.id IN :bookIds " +
            "GROUP BY b.id")
    List<BookRatingResponse> findAverageRatingForBook(@Param("bookIds") List<Long> bookIds);
    @Query("select avg(br.rating) from BookReview br")
    double getOverallAverageRating();
    // repository/BookRepository.java
    @Query(value = """
    -- CTE 1: Tính toán các chỉ số cơ bản cho mỗi cuốn sách
    WITH BookStats AS (
        SELECT
            b.id AS book_id,
            COALESCE(AVG(br.rating), 0.0) AS average_rating,
            COUNT(br.id) AS review_count
        FROM
            books b
        LEFT JOIN
            book_reviews br ON b.id = br.book_id
        GROUP BY
            b.id
    ),
    -- CTE 2: Xếp hạng các cuốn sách trong nhóm của mỗi tác giả
    RankedBooks AS (
        SELECT
            ba.author_id,
            bs.book_id,
            ROW_NUMBER() OVER(
                PARTITION BY ba.author_id 
                ORDER BY
                    ( (bs.review_count / (bs.review_count + :minVotesRequired)) * bs.average_rating ) + 
                    ( (:minVotesRequired / (bs.review_count + :minVotesRequired)) * :overallAverageRating ) 
                DESC
            ) as rn
        FROM
            BookStats bs
        JOIN
            book_authors ba ON bs.book_id = ba.book_id
        WHERE
            bs.review_count >= :minVotesRequired
    )
    -- Cuối cùng: Chỉ chọn những cuốn sách có xếp hạng là 1
    SELECT
        rb.book_id as bookId, -- Alias 'bookId' khớp với getBookId()
        rb.author_id as authorId -- Alias 'authorId' khớp với getAuthorId()
    FROM
        RankedBooks rb
    WHERE
        rb.rn = 1
    """, nativeQuery = true)

    List<TopRatedBookResponse> findTopRatedBookForEachAuthor(
            @Param("minVotesRequired") double minVotesRequired,
            @Param("overallAverageRating") double overallAverageRating
    );
}