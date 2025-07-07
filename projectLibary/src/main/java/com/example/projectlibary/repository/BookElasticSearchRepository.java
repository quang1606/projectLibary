package com.example.projectlibary.repository;

import com.example.projectlibary.model.BookElasticSearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookElasticSearchRepository extends ElasticsearchRepository<BookElasticSearch,Long> {
}
