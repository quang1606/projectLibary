package com.example.projectlibary.service.eventservice;

import com.example.projectlibary.model.BookElasticSearch;
import com.example.projectlibary.event.BookSyncEvent;
import com.example.projectlibary.repository.BookElasticSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchSyncService {
private final BookElasticSearchRepository bookElasticSearchRepository;
    @KafkaListener(topics = "book_events", groupId = "library-sync-group")

    public void consumeBookEvent(BookSyncEvent event) {
        log.info("Received book event from Kafka: Type='{}', BookID='{}'", event.getEvenType(), event.getId());
        try {
            switch (event.getEvenType()) {
                case "CREATE":
                    case "UPDATE":
                        BookElasticSearch bookEs = BookElasticSearch.builder()
                                .id(event.getId())
                                .title(event.getTitle())
                                .description(event.getDescription())
                                .isbn(event.getIsbn())
                                .availableCopyCount(event.getAvailableCopyCount())
                                .authors(event.getAuthors())
                                .publicationYear(event.getPublicationYear())
                                .categoryName(event.getCategoryName())
                                .build();
                        // Lưu hoặc cập nhật (save hoạt động cho cả hai)
                        bookElasticSearchRepository.save(bookEs);
                        log.info("Successfully indexed book with ID: {}", event.getId());
                        break;
                case "DELETE":
                    bookElasticSearchRepository.deleteById(event.getId());
                    log.info("Successfully deleted book from index with ID: {}", event.getId());
                    break;
                default:
                    log.warn("Received unknown event type: {}", event.getEvenType());
            }
        } catch (Exception e) {
            log.error("Error processing book event for ID: {}", event.getId(), e);
        }
    }
}
