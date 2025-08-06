package com.example.projectlibary.repository;

import com.example.projectlibary.common.PaymentStatus;
import com.example.projectlibary.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {


    List<Payment> findByUser_IdAndStatus(Long userId, PaymentStatus status);

    List<Payment> findByIdInAndUser_IdAndStatus(Collection<Long> ids, Long userId, PaymentStatus status);


    List<Payment> findByStatusAndTransactionId(PaymentStatus status, String transactionId);
}