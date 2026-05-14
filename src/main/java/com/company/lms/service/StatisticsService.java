package com.company.lms.service;

import com.company.lms.model.LeaveStatus;
import com.company.lms.repository.LeaveRepository;
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

    @Inject
    private LeaveRepository leaveRepo;

    public Map<String, Long> getLeavesByStatus(Integer managerId) {
        List<Object[]> results = statsRepo.countByStatus(managerId);
        Map<String, Long> map = new HashMap<>();

        for (Object[] result : results) {
            LeaveStatus status = (LeaveStatus) result[0];
            map.put(status.getDisplayName(), (Long) result[1]);
        }

        return map;
    }

    public Map<String, Long> getLeavesByType(Integer managerId) {
        List<Object[]> results = statsRepo.countByType(managerId);
        Map<String, Long> map = new HashMap<>();

        for (Object[] result : results) {
            map.put((String) result[0], (Long) result[1]);
        }

        return map;
    }

    public Map<Integer, Map<String, Long>> getMonthlyTrendByType(Integer managerId, int year) {
        List<Object[]> results = leaveRepo.monthlyTrendByType(managerId, year);
        Map<Integer, Map<String, Long>> trend = new HashMap<>();

        for (Object[] result : results) {
            Integer month = ((Number) result[0]).intValue();
            String type = (String) result[1];
            Long count = (Long) result[2];
            trend.computeIfAbsent(month, ignored -> new HashMap<>()).put(type, count);
        }

        return trend;
    }
}