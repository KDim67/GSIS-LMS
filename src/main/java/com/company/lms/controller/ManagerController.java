package com.company.lms.controller;

import com.company.lms.model.Employee;
import com.company.lms.model.LeaveRequest;
import com.company.lms.service.ManagerLeaveService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class ManagerController implements Serializable {

    @Inject
    private ManagerLeaveService managerService;

    private List<LeaveRequest> pendingRequests;
    private LeaveRequest selectedRequest;
    private String managerComment;
    private String pendingAction;

    @PostConstruct
    public void init() {
        loadRequests();
    }

    private void loadRequests() {
        pendingRequests = managerService.getPendingRequests();
    }

    public void approveLeave() {
        try {
            // TODO: Retrieve the actual logged-in manager from the SecurityContext
            Employee currentManager = null; 

            managerService.approveLeave(selectedRequest.getId(), currentManager, managerComment);
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Επιτυχία", "Η άδεια εγκρίθηκε"));
            
            // Reset and reload
            managerComment = null;
            selectedRequest = null;
            pendingAction = null; 
            loadRequests();
            
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Σφάλμα", e.getMessage()));
        }
    }

    public void rejectLeave() {
        try {
            if (managerComment == null || managerComment.trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Προειδοποίηση", "Απαιτείται σχόλιο για την απόρριψη."));
                return;
            }

            // TODO: Retrieve the actual logged-in manager from the SecurityContext
            Employee currentManager = null;

            managerService.rejectLeave(selectedRequest.getId(), currentManager, managerComment);
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Επιτυχία", "Η άδεια απορρίφθηκε"));
            
            managerComment = null;
            selectedRequest = null;
            pendingAction = null;
            loadRequests();

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Σφάλμα", e.getMessage()));
        }
    }

    // Getters and Setters
    public List<LeaveRequest> getPendingRequests() { return pendingRequests; }
    public LeaveRequest getSelectedRequest() { return selectedRequest; }
    public void setSelectedRequest(LeaveRequest selectedRequest) { this.selectedRequest = selectedRequest; }
    public String getManagerComment() { return managerComment; }
    public void setManagerComment(String managerComment) { this.managerComment = managerComment; }
        public String getPendingAction() { return pendingAction; }
    public void setPendingAction(String pendingAction) { this.pendingAction = pendingAction; }
}