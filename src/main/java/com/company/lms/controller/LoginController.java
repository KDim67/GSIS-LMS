package com.company.lms.controller;

import com.company.lms.model.Employee;
import com.company.lms.service.AuthService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

@Named
@SessionScoped
public class LoginController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private AuthService authService;

    private String email;
    private String password;

    private String firstName;
    private String lastName;
    private String registerEmail;
    private String registerPassword;

    private Employee loggedInUser;

    public String login() {

        loggedInUser = authService.login(email, password);

        if (loggedInUser == null) {

            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Λάθος email ή κωδικός.",
                            null
                    )
            );

            return null;
        }

        String role = loggedInUser.getRole().getRoleName();

        if ("MANAGER".equals(role)) {
            return "/manager/requests.xhtml?faces-redirect=true";
        }

        return "/employee/dashboard.xhtml?faces-redirect=true";
    }

    public String register() {

        try {

            authService.register(
                    firstName,
                    lastName,
                    registerEmail,
                    registerPassword
            );

            clearRegisterForm();

            FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getFlash()
                    .setKeepMessages(true);

            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_INFO,
                            "Η εγγραφή ολοκληρώθηκε. Μπορείς να συνδεθείς.",
                            null
                    )
            );

            return "/login.xhtml?faces-redirect=true";

        } catch (IllegalArgumentException e) {

            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            e.getMessage(),
                            null
                    )
            );

            return null;
        }
    }

    public String logout() {

        FacesContext.getCurrentInstance()
                .getExternalContext()
                .invalidateSession();

        return "/login.xhtml?faces-redirect=true";
    }

    public boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public String getLoggedInUserRole() {

        if (loggedInUser == null || loggedInUser.getRole() == null) {
            return null;
        }

        return loggedInUser.getRole().getRoleName();
    }

    private void clearRegisterForm() {

        firstName = null;
        lastName = null;
        registerEmail = null;
        registerPassword = null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRegisterEmail() {
        return registerEmail;
    }

    public void setRegisterEmail(String registerEmail) {
        this.registerEmail = registerEmail;
    }

    public String getRegisterPassword() {
        return registerPassword;
    }

    public void setRegisterPassword(String registerPassword) {
        this.registerPassword = registerPassword;
    }

    public Employee getLoggedInUser() {
        return loggedInUser;
    }
}