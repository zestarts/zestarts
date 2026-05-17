package com.calculator.repository;

import com.calculator.model.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    List<OperationLog> findTop50ByOrderByTimestampDesc();

    List<OperationLog> findByUsernameOrderByTimestampDesc(String username);

    Optional<OperationLog> findTopByUsernameOrderByTimestampDesc(String username);

    @Query("SELECT o.username, MAX(o.timestamp) FROM OperationLog o GROUP BY o.username")
    List<Object[]> findLastActivityPerUser();
}