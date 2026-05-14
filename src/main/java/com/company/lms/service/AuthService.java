package com.company.lms.service;

import com.company.lms.model.Employee;
import com.company.lms.model.LeaveBalance;
import com.company.lms.model.LeaveType;
import com.company.lms.model.Role;
import com.company.lms.util.PasswordUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuthService {

    @Inject
    private EmailService emailService;

    @PersistenceContext(unitName = "lmsPU")
    private EntityManager em;

    public Employee login(String email, String password) {
        try {
            Employee employee = em.createQuery(
                            "SELECT e FROM Employee e JOIN FETCH e.role WHERE e.email = :email",
                            Employee.class
                    )
                    .setParameter("email", email)
                    .getSingleResult();

            if (PasswordUtil.checkPassword(password, employee.getPassword())) {
                return employee;
            }

            return null;

        } catch (NoResultException e) {
            return null;
        }
    }

    @Transactional
    public void register(String firstName, String lastName, String email, String password) {
        if (emailExists(email)) {
            throw new IllegalArgumentException("Το email χρησιμοποιείται ήδη.");
        }

        Role employeeRole = em.createQuery(
                        "SELECT r FROM Role r WHERE r.roleName = :roleName",
                        Role.class
                )
                .setParameter("roleName", "EMPLOYEE")
                .getSingleResult();

        Employee employee = new Employee();
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(email);
        employee.setPassword(PasswordUtil.hashPassword(password));
        employee.setAnnualLeaveBalance(20);
        employee.setRole(employeeRole);

        em.persist(employee);

        for (LeaveType type : LeaveType.values()) {
            em.persist(new LeaveBalance(employee, type.getDisplayName(), type.getDefaultBalance()));
        }

        emailService.sendWelcomeEmail(employee);
    }

    public boolean emailExists(String email) {
        Long count = em.createQuery(
                        "SELECT COUNT(e) FROM Employee e WHERE e.email = :email",
                        Long.class
                )
                .setParameter("email", email)
                .getSingleResult();

        return count > 0;
    }
}