package com.company.lms.repository;

import com.company.lms.model.LeaveRequest;
import com.company.lms.model.LeaveStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@ApplicationScoped
public class LeaveRepository {

    @PersistenceContext
    private EntityManager em;

    public List<LeaveRequest> findByStatus(LeaveStatus status) {
        return em.createQuery(
                "SELECT l FROM LeaveRequest l JOIN FETCH l.employee WHERE l.status = :status ORDER BY l.startDate ASC", 
                LeaveRequest.class)
                .setParameter("status", status)
                .getResultList();
    }

    public LeaveRequest findById(Integer id) {
        return em.find(LeaveRequest.class, id);
    }

    public void update(LeaveRequest request) {
        em.merge(request);
    }
}