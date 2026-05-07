package com.company.lms.repository;

import com.company.lms.model.Employee;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class EmployeeRepository {

    @PersistenceContext
    private EntityManager em;

    public void update(Employee employee) {
        em.merge(employee);
    }
}