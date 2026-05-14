package com.company.lms.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@ApplicationScoped
public class StatisticsRepository {

    @PersistenceContext
    private EntityManager em;

    public List<Object[]> countByStatus(Integer managerId) {
        return em.createQuery(
                "SELECT l.status, COUNT(l) FROM LeaveRequest l WHERE l.employee.manager.id = :managerId GROUP BY l.status",
                Object[].class)
                .setParameter("managerId", managerId)
                .getResultList();
    }

    public List<Object[]> countByType(Integer managerId) {
        return em.createQuery(
                "SELECT l.leaveType, COUNT(l) FROM LeaveRequest l WHERE l.employee.manager.id = :managerId GROUP BY l.leaveType",
                Object[].class)
                .setParameter("managerId", managerId)
                .getResultList();
    }
}