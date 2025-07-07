package com.example.demospringbath.config;

import com.example.demospringbath.model.Order;
import com.example.demospringbath.processor.OrderProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing // Bật tính năng Spring Batch
public class BatchConfiguration {



    // 1. ItemReader: Đọc dữ liệu từ file CSV
    @Bean
    public FlatFileItemReader<Order> reader() {
        return new FlatFileItemReaderBuilder<Order>()
                .name("orderItemReader")
                .resource(new ClassPathResource("orders.csv"))
                .linesToSkip(1)
                .delimited()
                .names(new String[]{"id", "name", "quantity", "price"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Order>() {{
                    setTargetType(Order.class);
                }})
                .build();
    }

    @Bean
    public OrderProcessor processor() {
        return new OrderProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Order> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Order>()
                .sql("INSERT INTO orders (id, name, quantity, price) VALUES (:id, :name, :quantity, :price)")
                .dataSource(dataSource)
                .beanMapped() // Cách viết gọn hơn cho .itemSqlParameterSourceProvider
                .build();
    }

    // --- CẬP NHẬT CÁCH ĐỊNH NGHĨA STEP ---
    @Bean
    public Step importOrderStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                FlatFileItemReader<Order> reader, OrderProcessor processor, JdbcBatchItemWriter<Order> writer) {
        return new StepBuilder("importOrderStep", jobRepository) // Thay thế stepBuilderFactory.get()
                .<Order, Order>chunk(10, transactionManager) // truyền transactionManager vào chunk
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    // --- CẬP NHẬT CÁCH ĐỊNH NGHĨA JOB ---
    @Bean
    public Job importOrderJob(JobRepository jobRepository, Step importOrderStep) {
        return new JobBuilder("importOrderJob", jobRepository) // Thay thế jobBuilderFactory.get()
                .incrementer(new RunIdIncrementer())
                .flow(importOrderStep)
                .end()
                .build();
    }
}