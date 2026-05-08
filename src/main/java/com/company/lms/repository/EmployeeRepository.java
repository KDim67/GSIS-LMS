package com.company.lms.repository;

import com.company.lms.model.Employee;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class EmployeeRepository {

    @PersistenceContext
    private EntityManager em;

    public Employee findById(Integer id) {
        return em.find(Employee.class, id);
    }

    public void update(Employee employee) {
        em.merge(employee);
    }
}