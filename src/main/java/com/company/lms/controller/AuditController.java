package com.company.lms.controller;

import com.company.lms.model.AuditLog;
import com.company.lms.service.AuditService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class AuditController implements Serializable {

    @Inject
    private AuditService auditService;

    private List<AuditLog> auditLogs;

    @PostConstruct
    public void init() {
        auditLogs = auditService.getAllLogs();
    }

    // Getter
    public List<AuditLog> getAuditLogs() { return auditLogs; }
}