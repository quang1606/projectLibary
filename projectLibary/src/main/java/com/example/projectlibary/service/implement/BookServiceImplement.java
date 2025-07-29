package com.example.projectlibary.service.implement;

//import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.example.projectlibary.common.SearchOperation;
import com.example.projectlibary.dto.reponse.*;
import com.example.projectlibary.dto.request.CreateBookRequest;
import com.example.projectlibary.dto.request.UpdateBookRequest;
import com.example.projectlibary.event.BookSyncEvent;
import com.example.projectlibary.exception.AppException;
import com.example.projectlibary.exception.ErrorCode;
import com.example.projectlibary.mapper.BookMapper;
import com.example.projectlibary.model.*;
import com.example.projectlibary.repository.*;
import com.example.projectlibary.service.BookService;
import com.example.projectlibary.service.BookSpecification;
import com.example.projectlibary.service.CloudinaryService;
import com.example.projectlibary.service.eventservice.KafkaProducerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.*;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImplement implements BookService {
    private final ElasticsearchOperations elasticsearchOperations;
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final KafkaProducerService kafkaProducerService;

    // Làm giàu danh sách BookSummaryResponse với thông tin về số lượt mượn (loanCount).
    public void enrichSummariesWithLoanCounts(List<BookSummaryResponse> bookSummaryResponses) {
        if (bookSummaryResponses == null || bookSummaryResponses.isEmpty()) {
            return;
        }
        List<Long> bookIds = bookSummaryResponses.stream().map(BookSummaryResponse::getId).toList();
        List<BookLoanCountResponse> loanCounts = bookRepository.findLoanCountsForBooks(bookIds);
        List<BookRatingResponse> ratingResponses = bookRepository.findAverageRatingForBook(bookIds);
        Map<Long, Long> loanCountMap = loanCounts.stream().collect(Collectors.toMap(BookLoanCountResponse::getId, BookLoanCountResponse::getLoanCount));
        Map<Long, Double> ratingMap = ratingResponses.stream().collect(Collectors.toMap(BookRatingResponse::getBookId, BookRatingResponse::getRating));

        bookSummaryResponses.forEach(summary -> {
            long loanCount = loanCountMap.getOrDefault(summary.getId(), 0L);
            double rating = ratingMap.getOrDefault(summary.getId(), 0.0);
            summary.setLoanCount(loanCount);
            summary.setAverageRating(rating);
        });
    }

    @Override
    public PageResponse<BookSummaryResponse> getAllBooks(int page, int size) { // Nhận Pageable

        Pageable pageable = PageRequest.of(page, size);
        Page<Book> booksPage = bookRepository.findAll(pageable);

        List<BookSummaryResponse> bookSummaryResponses = booksPage.getContent().stream()
                .map(bookMapper::toSummaryResponse)
                .toList();
        return PageResponse.from(booksPage, bookSummaryResponses);
    }

    //Lấy danh sách các sách có só lượng mượn nhiều nhất
    @Override
    public PageResponse<BookSummaryResponse> getMostBorrowedBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookLoanCountResponse> loanCountPage = bookRepository.findBookLoanCounts(pageable);

        List<Long> bookIds = loanCountPage.getContent().stream().map(BookLoanCountResponse::getId).toList();
        if (bookIds.isEmpty()) {
            return PageResponse.empty(page, size);
        }
        List<Book> books = bookRepository.findByIdInWithDetails(bookIds);

        Map<Long, Book> bookMap = books.stream().collect(Collectors.toMap(Book::getId, book -> book));
        Map<Long, Long> loanCountMap = loanCountPage.getContent().stream().collect(Collectors.toMap(BookLoanCountResponse::getId, BookLoanCountResponse::getLoanCount));

        // Bước cuối: Kết hợp dữ liệu và tạo ra danh sách response cuối cùng.
        List<BookSummaryResponse> finalResponseList = bookIds.stream()
                .map(id -> {
                    Book book = bookMap.get(id);
                    long loanCount = loanCountMap.getOrDefault(id, 0L);

                    BookSummaryResponse response = bookMapper.toSummaryResponse(book);
                    // Gán thêm thông tin số lượt mượn
                    response.setLoanCount(loanCount);
                    return response;
                })
                .toList();

        return PageResponse.from(loanCountPage, finalResponseList);
    }

    //Lấy danh sách sách mới xuất bản
    @Override
    public PageResponse<BookSummaryResponse> getNewBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> newBooksPage = bookRepository.findByOrderByPublicationYearDesc(pageable);

        List<BookSummaryResponse> finalResponseList = newBooksPage.getContent().stream()
                .map(bookMapper::toSummaryResponse).toList();
        enrichSummariesWithLoanCounts(finalResponseList);
        return PageResponse.from(newBooksPage, finalResponseList);
    }

    @Override
    public BookDetailResponse getBookById(long id) {
        // (1) Log INFO: Ghi nhận sự bắt đầu của một nghiệp vụ
        log.info("Bắt đầu tìm kiếm sách với ID: {}", id);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));

        log.info("Tìm thấy sách thành công với ID: {}", id);

        return bookMapper.toBookDetailResponse(book);
    }

    @Override
    public PageResponse<BookSummaryResponse> getTopRatedBookOfEachAuthor(int page, int size) {
        final double MIN_VOTES_REQUIRED = 1.0;
        Pageable pageable = PageRequest.of(page, size);
        Double overallAverageRating = bookRepository.getOverallAverageRating();
        if (overallAverageRating == null) {
            overallAverageRating = 3.0;
        }
        List<TopRatedBookResponse> topBooksResult = bookRepository.findTopRatedBookForEachAuthor(
                MIN_VOTES_REQUIRED,
                overallAverageRating
        );
        if (topBooksResult.isEmpty()) {
            return PageResponse.empty(page, size);
        }
        List<Long> bookIds = topBooksResult.stream()
                .map(TopRatedBookResponse::getBookId)
                .collect(Collectors.toList());
        // 3. Lấy chi tiết sách theo các ID đã có
        List<Book> books = bookRepository.findAllById(bookIds);
        Map<Long, Book> bookMap = books.stream().collect(Collectors.toMap(Book::getId, b -> b));
        // Giữ đúng thứ tự ban đầu từ câu query xếp hạng
        List<Book> sortedBooks = bookIds.stream()
                .map(bookMap::get)
                .filter(Objects::nonNull)
                .toList();
        // 4. Map sang DTO và làm giàu dữ liệu (nếu cần)
        List<BookSummaryResponse> bookSummaryResponses = sortedBooks.stream()
                .map(bookMapper::toSummaryResponse)
                .toList();
        enrichSummariesWithLoanCounts(bookSummaryResponses);

        // 5. PHÂN TRANG THỦ CÔNG TRONG JAVA
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), bookSummaryResponses.size());
        // Lấy ra danh sách con cho trang hiện tại
        List<BookSummaryResponse> pagedContent = bookSummaryResponses.subList(start, end);
        // Tạo một đối tượng Page thủ công từ danh sách đã phân trang
        Page<BookSummaryResponse> pageResult = new PageImpl<>(pagedContent, pageable, bookSummaryResponses.size());
        return PageResponse.from(pageResult, bookSummaryResponses);
    }

    @Override
    public PageResponse<BookElasticSearch> searchBooks(int page, int size, String keyWord) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyWord == null || keyWord.isBlank()) {

            // return bookElasticSearchRepository.findAll(pageable);
            // Hoặc dùng query match_all nếu muốn
            Query query = Query.of(q->q.matchAll(m->m));

            return searchWithQuery(query, pageable);
        }

        // Xây dựng một câu truy vấn Multi-match phức tạp và mạnh mẽ
        Query query = Query.of(q -> q
                .multiMatch(mm -> mm
                        .query(keyWord)
                        .fields("title^3", "authors^2", "description", "category_name") // Gán trọng số khác nhau
                        .type(TextQueryType.BestFields) // Kiểu truy vấn phù hợp cho việc tìm kiếm trên nhiều trường
                        .fuzziness("AUTO") // Cho phép lỗi chính tả
                        .minimumShouldMatch("70%") // Yêu cầu ít nhất 70% từ khóa phải khớp
                )
        );

        return searchWithQuery(query, pageable);
    }


    private PageResponse<BookElasticSearch> searchWithQuery(Query query, Pageable pageable) {
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .withPageable(pageable)
                .build();
        SearchHits<BookElasticSearch> searchHits = elasticsearchOperations.search(nativeQuery, BookElasticSearch.class);
        List<SearchHit<BookElasticSearch>> searchHitsList = searchHits.getSearchHits();
        List<BookElasticSearch> bookElasticSearchList = searchHitsList.stream().map(SearchHit::getContent).collect(Collectors.toList());
        SearchPage<BookElasticSearch> searchPage = SearchHitSupport.searchPageFor(searchHits,pageable);
        return PageResponse.from(searchPage,bookElasticSearchList);
    }

        @Override
        public PageResponse<BookSummaryResponse> filterBooks(int page, int size, List<String> filters) {
            List<SearchCriteria> searchCriteria = new ArrayList<>();
            if(filters != null) {
                Pattern pattern = Pattern.compile("(\\w+?)(>=|<=|==|>|<|:|~)(.+)");
                for (String filter : filters) {
                    Matcher matcher = pattern.matcher(filter);
                    if (matcher.find()) {
                        String key = matcher.group(1);
                        String operator = matcher.group(2);
                        String value = matcher.group(3);
                        SearchOperation operation = mapOperator(operator);
                        if (operation != null) {
                            searchCriteria.add(new SearchCriteria(key,operation,value));
                        }
                    }
                }
            }
            Pageable pageable = PageRequest.of(page, size);
            Specification<Book> specification = BookSpecification.fromCriteria(searchCriteria);
            Page<Book> books = bookRepository.findAll(specification, pageable);
            List<BookSummaryResponse> bookSummaryResponses = bookMapper.toSummaryResponseList(books.getContent());
            enrichSummariesWithLoanCounts(bookSummaryResponses);

            return PageResponse.from(books, bookSummaryResponses);
        }



    private SearchOperation mapOperator(String operator) {
            switch (operator) {
                case "==": return SearchOperation.EQUAL;
                case ">":   return SearchOperation.GREATER_THAN;
                case "<":   return SearchOperation.LESS_THAN;
                case ">=": return SearchOperation.GREATER_THAN_OR_EQUAL;
                case "<=": return SearchOperation.LESS_THAN_OR_EQUAL;
                case ":": return SearchOperation.LIKE;
                default:return null;
            }


    }


    @Override
    public PageResponse<BookSummaryResponse> getBooksByAuthor(Long authorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.findByAuthors_Id(authorId,pageable);
        List<BookSummaryResponse> bookSummaryResponses = bookMapper.toSummaryResponseList(bookPage.getContent());
        enrichSummariesWithLoanCounts(bookSummaryResponses);
        return PageResponse.from(bookPage,bookSummaryResponses);
    }

    @Override
    @Transactional
    public BookDetailResponse createBook(CreateBookRequest createBookRequest, MultipartFile pdf, MultipartFile thumbnail) {

        if (bookRepository.existsByIsbn(createBookRequest.getIsbn())) {
            throw new AppException(ErrorCode.ISBN_ALREADY_EXIST);
        }
        Category category = categoryRepository.findById(createBookRequest.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        Set<Author> authors = new HashSet<>(authorRepository.findAllById(createBookRequest.getAuthorIds()));
        if (authors.size() != createBookRequest.getAuthorIds().size()) {
            throw new AppException(ErrorCode.AUTHOR_NOT_FOUND);
        }
        String thumbnailUrl = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                // Upload ảnh bìa
                Map thumbnailResult = cloudinaryService.uploadFile(thumbnail, "library/thumbnails");
                thumbnailUrl = thumbnailResult.get("url").toString();
            } catch (IOException e) {
                log.error("Error uploading thumbnail to Cloudinary", e);
                throw new AppException(ErrorCode.FILE_UPLOAD_ERROR); // Tạo ErrorCode mới
            }
        }

        String ebookUrl = null;
        if (pdf != null && !pdf.isEmpty()) {
            try {
                // Upload file PDF
                Map pdfResult = cloudinaryService.uploadFile(pdf, "library/ebooks");
                ebookUrl = pdfResult.get("url").toString();
            } catch (IOException e) {
                log.error("Error uploading PDF to Cloudinary", e);
                throw new AppException(ErrorCode.FILE_UPLOAD_ERROR);
            }
        }
        Book newBook = Book.builder()
                .title(createBookRequest.getTitle())
                .description(createBookRequest.getDescription())
                .isbn(createBookRequest.getIsbn())
                .publisher(createBookRequest.getPublisher())
                .publicationYear(createBookRequest.getPublicationYear())
                .replacementCost(createBookRequest.getReplacementCost())
                .thumbnail(thumbnailUrl) // Gán URL đã upload
                .ebookUrl(ebookUrl)   // Gán URL đã upload
                .category(category)
                .authors(authors)
                .build();

        Book savedBook = bookRepository.save(newBook);
        log.info("Successfully created book with ID: {}", savedBook.getId());
        int initialAvailableCopies = 0;
        BookSyncEvent bookSyncEvent = BookSyncEvent.builder()
                .evenType("CREATE")
                .id(savedBook.getId())
                .title(savedBook.getTitle())
                .description(savedBook.getDescription())
                .isbn(savedBook.getIsbn())
                .availableCopyCount(initialAvailableCopies)
                .authors(savedBook.getAuthors().stream().map(Author::getName).collect(Collectors.toSet()))
                .publicationYear(savedBook.getPublicationYear())
                .categoryName(savedBook.getCategory().getName())
                .build();

        kafkaProducerService.senBookEvent(bookSyncEvent);
        return bookMapper.toBookDetailResponse(savedBook);
    }

    @Override
    @Transactional
    public BookDetailResponse updateBook(UpdateBookRequest request, MultipartFile pdfFile, MultipartFile thumbnail,Long id) {
        Book bookToUpdate = bookRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));
        boolean isUpdated = false;
        // 2. Kiểm tra ISBN (Logic đã sửa)
        String newIsbn = request.getIsbn();
        // Chỉ kiểm tra nếu ISBN được thay đổi
        if (newIsbn != null && !newIsbn.equals(bookToUpdate.getIsbn())) {
            // Kiểm tra xem ISBN mới có bị trùng với sách nào khác không
            bookRepository.findByIsbn(newIsbn).ifPresent(existingBook -> {
                if (!existingBook.getId().equals(id)) {
                    throw new AppException(ErrorCode.ISBN_ALREADY_EXIST);
                }
            });
            bookToUpdate.setIsbn(newIsbn);
        }
        // 3. Cập nhật các trường đơn giản (chỉ cập nhật nếu giá trị mới không null)
        if (request.getTitle() != null) bookToUpdate.setTitle(request.getTitle());
        if (request.getDescription() != null) bookToUpdate.setDescription(request.getDescription());
        if (request.getPublisher() != null) bookToUpdate.setPublisher(request.getPublisher());
        if (request.getPublicationYear() != null) bookToUpdate.setPublicationYear(request.getPublicationYear());
        if (request.getReplacementCost() != null) bookToUpdate.setReplacementCost(request.getReplacementCost());
        // 4. Cập nhật các quan hệ (nếu có thay đổi)
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            bookToUpdate.setCategory(category);
        }
        if (request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
            Set<Author> authors = new HashSet<>(authorRepository.findAllById(request.getAuthorIds()));
            if (authors.size() != request.getAuthorIds().size()) {
                throw new AppException(ErrorCode.AUTHOR_NOT_FOUND);
            }
            bookToUpdate.setAuthors(authors);
        }

        // 5. Cập nhật file Thumbnail (chỉ khi có file mới)
        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                Map thumbnailResult = cloudinaryService.uploadFile(thumbnail, "library/thumbnails");
                bookToUpdate.setThumbnail(thumbnailResult.get("url").toString());
            } catch (IOException e) {
                log.error("Error updating thumbnail to Cloudinary for book ID: {}", id, e);
                throw new AppException(ErrorCode.FILE_UPLOAD_ERROR);
            }
        }

        // 6. Cập nhật file Ebook (chỉ khi có file mới)
        if (pdfFile != null && !pdfFile.isEmpty()) {
            try {
                Map pdfResult = cloudinaryService.uploadFile(pdfFile, "library/ebooks");
                bookToUpdate.setEbookUrl(pdfResult.get("url").toString());
            } catch (IOException e) {
                log.error("Error updating PDF to Cloudinary for book ID: {}", id, e);
                throw new AppException(ErrorCode.FILE_UPLOAD_ERROR);
            }
        }


        // 7. Lưu lại (JPA Auditing sẽ tự động cập nhật updatedBy và updatedAt)
        Book updatedBook = bookRepository.save(bookToUpdate);
        log.info("Successfully updated book with ID: {}", updatedBook.getId());

        BookSyncEvent bookSyncEvent = BookSyncEvent.builder()
                .evenType("UPDATE")
                .id(updatedBook.getId())
                .title(updatedBook.getTitle())
                .description(updatedBook.getDescription())
                .isbn(updatedBook.getIsbn())
                .authors(updatedBook.getAuthors().stream().map(Author::getName).collect(Collectors.toSet()))
                .publicationYear(updatedBook.getPublicationYear())
                .categoryName(updatedBook.getCategory().getName())
                .build();
        kafkaProducerService.senBookEvent(bookSyncEvent);
        return bookMapper.toBookDetailResponse(updatedBook);
    }

    // service/implement/BookServiceImplement.java

    @Override
    @Transactional 
    public void deleteBook(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Tìm đối tượng Book cần xóa
        // Do có @Where, findById sẽ chỉ tìm thấy các sách chưa bị xóa
        Book bookToDelete = bookRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));


        bookToDelete.setDeletedBy(currentUser);

        // 4. Lưu lại để cập nhật 'deletedBy' trước khi 'xóa'
        // Mặc dù @SQLDelete không dùng đến trường này, nhưng việc lưu lại là một thực hành tốt
        // để đảm bảo trạng thái của entity là nhất quán.
        bookRepository.save(bookToDelete);
        if (!bookRepository.existsById(id)) {
            throw new AppException(ErrorCode.BOOK_NOT_FOUND);
        }
        // 2. Thực hiện xóa
        // Do bạn đã cấu hình cascade và khóa ngoại, Hibernate/JPA sẽ xử lý các bảng liên quan:
        // - Xóa các bản ghi trong bảng nối `book_authors`.
        // - Các hành động khác tùy thuộc vào `CascadeType` và `onDelete` của các quan hệ @OneToMany.
        BookSyncEvent bookSyncEvent = BookSyncEvent.builder()
                .evenType("DELETE")
                .id(id)
                .build();
        kafkaProducerService.senBookEvent(bookSyncEvent);
        bookRepository.deleteById(id);
        log.info("Successfully deleted book with ID: {}", id);
    }
    @Override
    @Transactional
    public void restoreBook(Long id) {
        log.info("Attempting to restore book with ID: {}", id);

        Book bookToRestore = bookRepository.findByIdIncludeDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));


        if (bookToRestore.getDeletedAt() == null) {
            throw new AppException(ErrorCode.BOOK_NOT_DELETED);
        }

        // 3. Thực hiện khôi phục: Set các trường xóa mềm về giá trị ban đầu
        bookToRestore.setDeletedAt(null);
        bookToRestore.setDeletedBy(null);

        // Lưu ý: JPA Auditing (@LastModifiedBy, @LastModifiedDate) sẽ tự động cập nhật
        // updated_by và updated_at khi chúng ta gọi save().

        // 4. Lưu lại sách đã được khôi phục vào DB
        Book restoredBook = bookRepository.save(bookToRestore);
        log.info("Successfully restored book with ID: {}", restoredBook.getId());

        // 5. Gửi sự kiện đến Kafka để đồng bộ lại với Elasticsearch
        BookSyncEvent bookSyncEvent = BookSyncEvent.builder()
                .evenType("UPDATE")
                .id(restoredBook.getId())
                .title(restoredBook.getTitle())
                .description(restoredBook.getDescription())
                .isbn(restoredBook.getIsbn())
                .authors(restoredBook.getAuthors().stream().map(Author::getName).collect(Collectors.toSet()))
                .publicationYear(restoredBook.getPublicationYear())
                .categoryName(restoredBook.getCategory().getName())
                .build();

        kafkaProducerService.senBookEvent(bookSyncEvent);
        log.info("Sent restore (UPDATE) event to Kafka for book ID: {}", restoredBook.getId());
    }


}


