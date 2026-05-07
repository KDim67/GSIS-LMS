package com.company.lms.service;

import com.company.lms.model.LeaveStatus;
import com.company.lms.repository.StatisticsRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class StatisticsService {

    @Inject
    private StatisticsRepository statsRepo;

    public Map<String, Long> getLeavesByStatus() {
        List<Object[]> results = statsRepo.countByStatus();
        Map<String, Long> map = new HashMap<>();

        for (Object[] result : results) {
            LeaveStatus status = (LeaveStatus) result[0];
            map.put(status.getDisplayName(), (Long) result[1]);
        }

        return map;
    }

    public Map<String, Long> getLeavesByType() {
        List<Object[]> results = statsRepo.countByType();
        Map<String, Long> map = new HashMap<>();

        for (Object[] result : results) {
            map.put((String) result[0], (Long) result[1]);
        }

        return map;
    }
}