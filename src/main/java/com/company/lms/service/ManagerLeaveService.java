package com.company.lms.service;

import com.company.lms.model.Employee;
import com.company.lms.model.LeaveRequest;
import com.company.lms.model.LeaveStatus;
import com.company.lms.repository.EmployeeRepository;
import com.company.lms.repository.LeaveBalanceRepository;
import com.company.lms.repository.LeaveRepository;
import com.company.lms.util.GreekHolidayUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ManagerLeaveService {

    // Injecting the Data Access Layer
    @Inject
    private LeaveRepository leaveRepo;

    @Inject
    private EmployeeRepository employeeRepo;

    @Inject
    private LeaveBalanceRepository leaveBalanceRepo;

    @Inject
    private AuditService auditService;

    @Inject
    private EmailService emailService;

    // Returns all leaves with PENDING status.
    public List<LeaveRequest> getPendingRequests(Employee manager) {
        if (manager == null) {
            throw new IllegalStateException("Απαιτείται σύνδεση διαχειριστή.");
        }

        return leaveRepo.findPendingByManagerId(manager.getId());
    }

    // Changes the status, reduces the balance, and logs to Audit.
    @Transactional
    public void approveLeave(Integer leaveId, Employee manager, String comment) {
        // Fetch using the repository
        LeaveRequest request = leaveRepo.findById(leaveId);
        
        if (request == null || request.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalArgumentException("Το αίτημα δεν βρέθηκε ή έχει ήδη επεξεργαστεί.");
        }

        validateManagerCanHandleRequest(request, manager);

        // 1. Update the request
        request.setStatus(LeaveStatus.APPROVED);
        request.setManagerComment(comment);

        // 2. Calculate working days & Deduct balance
        Employee employee = request.getEmployee();
        int workingDays = calculateWorkingDays(request.getStartDate(), request.getEndDate());

        int updatedRows = leaveBalanceRepo.deductBalance(employee.getId(), request.getLeaveType(), workingDays);
        if (updatedRows != 1) {
            throw new IllegalStateException("Ο υπάλληλος δεν έχει επαρκή υπολειπόμενη άδεια (τύπος: " + request.getLeaveType() + ").");
        }
        
        // 3. Log to Audit (Updated action string and passed comment)
        auditService.logAction(manager, "APPROVE", request.getId(), comment);
        
        // 4. Save changes via Repositories
        leaveRepo.update(request);
        
        // 5. Update employee and send notification
        employeeRepo.update(employee);
        emailService.sendLeaveApprovedEmailToEmployee(employee, request);
    }

    @Transactional
    public int approveLeaves(List<Integer> leaveIds, Employee manager, String comment) {
        if (leaveIds == null || leaveIds.isEmpty()) {
            throw new IllegalArgumentException("Δεν έχουν επιλεγεί αιτήματα.");
        }
        int count = 0;
        for (Integer id : leaveIds) {
            approveLeave(id, manager, comment);
            count++;
        }
        return count;
    }

    @Transactional
    public int rejectLeaves(List<Integer> leaveIds, Employee manager, String comment) {
        if (leaveIds == null || leaveIds.isEmpty()) {
            throw new IllegalArgumentException("Δεν έχουν επιλεγεί αιτήματα.");
        }
        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("Απαιτείται σχόλιο για την απόρριψη.");
        }
        int count = 0;
        for (Integer id : leaveIds) {
            rejectLeave(id, manager, comment);
            count++;
        }
        return count;
    }

    public List<LeaveRequest> getApprovedRequestsForTeam(Employee manager, LocalDate from, LocalDate to) {
        return leaveRepo.findApprovedByManagerIdInRange(manager.getId(), from, to);
    }

    public List<LeaveRequest> getOnLeaveThisWeek(Employee manager) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        return leaveRepo.findApprovedByManagerIdInRange(manager.getId(), weekStart, weekEnd);
    }

    public Long getOldestPendingDays(Employee manager) {
        return leaveRepo.findOldestPendingCreatedAt(manager.getId())
                .map(c -> java.time.Duration.between(c, java.time.LocalDateTime.now()).toDays())
                .orElse(0L);
    }

    public int getTeamApprovedDaysThisYear(Employee manager) {
        int year = LocalDate.now().getYear();
        List<LeaveRequest> approved = leaveRepo.findApprovedByManagerIdInRange(
                manager.getId(),
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 31));
        int total = 0;
        for (LeaveRequest r : approved) {
            total += GreekHolidayUtil.calculateWorkingDays(r.getStartDate(), r.getEndDate());
        }
        return total;
    }

    // FIXED: Removed the out-of-scope and unreachable code
    public int getTeamRemainingBalance(Employee manager) {
        return leaveBalanceRepo.sumTotalForManager(manager.getId());
    }

    @Transactional
    public void rejectLeave(Integer leaveId, Employee manager, String comment) {
        LeaveRequest request = leaveRepo.findById(leaveId);
        
        if (request == null || request.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalArgumentException("Το αίτημα δεν βρέθηκε ή έχει ήδη επεξεργαστεί.");
        }

        validateManagerCanHandleRequest(request, manager);

        // 1. Update the request
        request.setStatus(LeaveStatus.REJECTED);
        request.setManagerComment(comment);

        // 2. Log to Audit
        auditService.logAction(manager, "REJECT", request.getId(), comment);
        
        // 3. Save changes
        leaveRepo.update(request);

        emailService.sendLeaveRejectedEmailToEmployee(request.getEmployee(), request);
    }

    private int calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        int workingDays = 0;
        LocalDate currentDate = startDate;
        int cachedYear = -1;
        Set<LocalDate> holidays = null;

        while (!currentDate.isAfter(endDate)) {
            if (currentDate.getYear() != cachedYear) {
                cachedYear = currentDate.getYear();
                holidays = GreekHolidayUtil.getHolidays(cachedYear);
            }
            DayOfWeek dow = currentDate.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY && !holidays.contains(currentDate)) {
                workingDays++;
            }
            currentDate = currentDate.plusDays(1);
        }
        return workingDays;
    }

    private void validateManagerCanHandleRequest(LeaveRequest request, Employee manager) {

        if (manager == null) {
            throw new IllegalStateException("Απαιτείται σύνδεση διαχειριστή.");
        }

        if (request.getEmployee().getId().equals(manager.getId())) {
            throw new IllegalStateException("Δεν μπορείτε να διαχειριστείτε δικό σας αίτημα άδειας.");
        }

        if (request.getEmployee().getManager() == null ||
                !request.getEmployee().getManager().getId().equals(manager.getId())) {
            throw new IllegalStateException("Δεν έχετε δικαίωμα διαχείρισης αυτού του αιτήματος.");
        }
    }
}