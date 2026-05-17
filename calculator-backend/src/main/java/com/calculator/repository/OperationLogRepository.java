package com.calculator.repository;

import com.calculator.model.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    List<OperationLog> findTop50ByOrderByTimestampDesc();

    List<OperationLog> findByUsernameOrderByTimestampDesc(String username);
}