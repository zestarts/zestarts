package com.calculator.service;

import com.calculator.model.OperationLog;
import com.calculator.model.OperationType;
import com.calculator.repository.OperationLogRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserActivityService {

    private final OperationLogRepository operationLogRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public UserActivityService(OperationLogRepository operationLogRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.operationLogRepository = operationLogRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void recordAndBroadcast(String username, OperationType operation,
                                   String expression, String result) {
        OperationLog log = new OperationLog(username, operation, expression, result);
        operationLogRepository.save(log);

        Map<String, Object> activityData = new HashMap<>();
        activityData.put("username", username);
        activityData.put("operation", operation.name());
        activityData.put("expression", expression);
        activityData.put("result", result);
        activityData.put("timestamp", log.getTimestamp().toString());

        messagingTemplate.convertAndSend("/topic/activity", activityData);
    }

    public List<OperationLog> getRecentLogs() {
        return operationLogRepository.findTop50ByOrderByTimestampDesc();
    }

    public List<OperationLog> getUserLogs(String username) {
        return operationLogRepository.findByUsernameOrderByTimestampDesc(username);
    }
}