package com.company.lms.repository;

import com.company.lms.model.Employee;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

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

    public List<Employee> findByManagerId(Integer managerId) {
        return em.createQuery(
                "SELECT e FROM Employee e WHERE e.manager.id = :managerId",
                Employee.class)
                .setParameter("managerId", managerId)
                .getResultList();
    }
}