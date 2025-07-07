package com.example.projectlibary.service.implement;

//import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.example.projectlibary.common.SearchOperation;
import com.example.projectlibary.dto.reponse.*;
import com.example.projectlibary.exception.AppException;
import com.example.projectlibary.exception.ErrorCode;
import com.example.projectlibary.mapper.BookMapper;
import com.example.projectlibary.model.Book;
import com.example.projectlibary.model.BookElasticSearch;
import com.example.projectlibary.model.SearchCriteria;
import com.example.projectlibary.repository.BookRepository;
import com.example.projectlibary.repository.BookSpecification;
import com.example.projectlibary.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


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
//    private final BookSpecification bookSpecification;
    private static final Logger logger = LogManager.getLogger(BookServiceImplement.class);


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
            Specification<Book> specification =BookSpecification.fromCriteria(searchCriteria);
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


}


