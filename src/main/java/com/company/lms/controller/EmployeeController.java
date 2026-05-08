package com.company.lms.controller;

import com.company.lms.model.Employee;
import com.company.lms.model.LeaveRequest;
import com.company.lms.model.LeaveStatus;
import com.company.lms.service.EmployeeLeaveService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Named
@ViewScoped
public class EmployeeController implements Serializable {

    @Inject
    private EmployeeLeaveService employeeService;

    @Inject
    private LoginController loginController;

    private Employee employee;
    private List<LeaveRequest> leaveHistory;
    private LocalDate startDate;
    private LocalDate endDate;
    private String leaveType;
    private String reason;
    private LeaveRequest selectedRequest;

    @PostConstruct
    public void init() {
        loadEmployeeData();
    }

    private void loadEmployeeData() {
        Employee loggedInUser = loginController.getLoggedInUser();

        if (loggedInUser == null) {
            return;
        }

        employee = employeeService.getEmployee(loggedInUser.getId());
        leaveHistory = employeeService.getLeaveHistory(loggedInUser.getId());
    }

    public void submitLeaveRequest() {
        try {
            Employee loggedInUser = loginController.getLoggedInUser();

            if (loggedInUser == null) {
                throw new IllegalStateException("Απαιτείται σύνδεση χρήστη.");
            }

            employeeService.submitLeaveRequest(loggedInUser.getId(), startDate, endDate, leaveType, reason);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Επιτυχία", "Το αίτημα άδειας υποβλήθηκε."));

            startDate = null;
            endDate = null;
            leaveType = null;
            reason = null;
            loadEmployeeData();

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Σφάλμα", e.getMessage()));
        }
    }

    public int getRequestedWorkingDays() {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return 0;
        }

        return employeeService.calculateWorkingDays(startDate, endDate);
    }

    public long getPendingCount() {
        return countByStatus(LeaveStatus.PENDING);
    }

    public long getApprovedCount() {
        return countByStatus(LeaveStatus.APPROVED);
    }

    public long getRejectedCount() {
        return countByStatus(LeaveStatus.REJECTED);
    }

    private long countByStatus(LeaveStatus status) {
        return leaveHistory == null ? 0 : leaveHistory.stream()
                .filter(request -> request.getStatus() == status)
                .count();
    }

    public LocalDate getToday() { return LocalDate.now(); }

    public Employee getEmployee() { return employee; }
    public List<LeaveRequest> getLeaveHistory() { return leaveHistory; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LeaveRequest getSelectedRequest() { return selectedRequest; }
    public void setSelectedRequest(LeaveRequest selectedRequest) { this.selectedRequest = selectedRequest; }
}