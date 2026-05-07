package com.company.lms.service;

import com.company.lms.model.AuditLog;
import com.company.lms.model.Employee;
import com.company.lms.repository.AuditRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class AuditService {

    @Inject
    private AuditRepository auditRepo;

    @Transactional(Transactional.TxType.MANDATORY)
    public void logAction(Employee user, String action, Integer targetId, String comment) {
        AuditLog log = new AuditLog(user, action, targetId, comment);
        auditRepo.save(log);
    }

    public List<AuditLog> getAllLogs() {
        return auditRepo.findAll();
    }
}