package com.company.lms.repository;

import com.company.lms.model.AuditLog;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@ApplicationScoped
public class AuditRepository {

    @PersistenceContext
    private EntityManager em;

    public void save(AuditLog log) {
        em.persist(log);
    }

    // Fetch all logs with the associated user details
    public List<AuditLog> findAll() {
        return em.createQuery("SELECT a FROM AuditLog a JOIN FETCH a.user ORDER BY a.timestamp DESC", AuditLog.class).getResultList();
    }
}