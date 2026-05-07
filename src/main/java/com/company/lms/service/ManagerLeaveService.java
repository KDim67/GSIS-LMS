package com.company.lms.service;

import com.company.lms.model.Employee;
import com.company.lms.model.LeaveRequest;
import com.company.lms.model.LeaveStatus;
import com.company.lms.repository.EmployeeRepository;
import com.company.lms.repository.LeaveRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ManagerLeaveService {

    // Injecting the Data Access Layer
    @Inject
    private LeaveRepository leaveRepo;

    @Inject
    private EmployeeRepository employeeRepo;

    @Inject
    private AuditService auditService;

    // Returns all leaves with PENDING status.
    public List<LeaveRequest> getPendingRequests() {
        return leaveRepo.findByStatus(LeaveStatus.PENDING);
    }

    // Changes the status, reduces the balance, and logs to Audit.
    @Transactional
    public void approveLeave(Integer leaveId, Employee manager, String comment) {
        // Fetch using the repository
        LeaveRequest request = leaveRepo.findById(leaveId);
        
        if (request == null || request.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalArgumentException("Το αίτημα δεν βρέθηκε ή έχει ήδη επεξεργαστεί.");
        }

        // 1. Update the request
        request.setStatus(LeaveStatus.APPROVED);
        request.setManagerComment(comment);

        // 2. Calculate working days & Deduct balance
        Employee employee = request.getEmployee();
        int workingDays = calculateWorkingDays(request.getStartDate(), request.getEndDate());
        
        if (employee.getAnnualLeaveBalance() < workingDays) {
            throw new IllegalStateException("Ο υπάλληλος δεν έχει επαρκή υπολειπόμενη άδεια.");
        }
        
        employee.setAnnualLeaveBalance(employee.getAnnualLeaveBalance() - workingDays);

        // 3. Log to Audit (Updated action string and passed comment)
        auditService.logAction(manager, "APPROVE", request.getId(), comment);
        
        // 4. Save changes via Repositories
        leaveRepo.update(request);
        employeeRepo.update(employee);
    }

    @Transactional
    public void rejectLeave(Integer leaveId, Employee manager, String comment) {
        LeaveRequest request = leaveRepo.findById(leaveId);
        
        if (request == null || request.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalArgumentException("Το αίτημα δεν βρέθηκε ή έχει ήδη επεξεργαστεί.");
        }

        // 1. Update the request
        request.setStatus(LeaveStatus.REJECTED);
        request.setManagerComment(comment);

        // 2. Log to Audit
        auditService.logAction(manager, "REJECT", request.getId(), comment);
        
        // 3. Save changes
        leaveRepo.update(request);
    }

    // Calculate working days (Subtract weekends).
    private int calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        int workingDays = 0;
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            if (currentDate.getDayOfWeek() != DayOfWeek.SATURDAY && 
                currentDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDays++;
            }
            currentDate = currentDate.plusDays(1);
        }
        return workingDays;
    }
}