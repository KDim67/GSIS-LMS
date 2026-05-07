package com.company.lms.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@ApplicationScoped
public class StatisticsRepository {

    @PersistenceContext
    private EntityManager em;

    // Fetches raw count data grouped by status
    public List<Object[]> countByStatus() {
        return em.createQuery("SELECT l.status, COUNT(l) FROM LeaveRequest l GROUP BY l.status", Object[].class).getResultList();
    }

    // Fetches raw count data grouped by leave type
    public List<Object[]> countByType() {
        return em.createQuery("SELECT l.leaveType, COUNT(l) FROM LeaveRequest l GROUP BY l.leaveType", Object[].class).getResultList();
    }
}