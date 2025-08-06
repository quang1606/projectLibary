package com.example.projectlibary.service.eventservice;

import com.example.projectlibary.common.BookCopyStatus;
import com.example.projectlibary.event.BookStatusChangedEvent;
import com.example.projectlibary.repository.BookCopyRepository;
import com.example.projectlibary.repository.BookElasticSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchIndexUpdater {
    private final BookElasticSearchRepository bookElasticSearchRepository;
    private final BookCopyRepository bookCopyRepository; // Để tính toán lại

    @KafkaListener(topics = "book-status-changed-events", groupId = "elasticsearch-updater-group")
    public void handleBookStatusChange(BookStatusChangedEvent event) {
        log.info("Received book status change event for book ID: {}", event.getBookId());

        // 1. Tìm document hiện có trong Elasticsearch
        bookElasticSearchRepository.findById(event.getBookId()).ifPresentOrElse(bookEs -> {

            long newAvailableCount = bookCopyRepository.countByBook_IdAndStatus(event.getBookId(), BookCopyStatus.AVAILABLE);

            // 3. Cập nhật lại document
            bookEs.setAvailableCopyCount((int) newAvailableCount);
            bookElasticSearchRepository.save(bookEs);

            log.info("Updated availableCopyCount to {} for book ID: {}", newAvailableCount, event.getBookId());
        }, () -> {
            log.warn("Book with ID {} not found in Elasticsearch. Cannot update stats.", event.getBookId());
        });
    }
}
