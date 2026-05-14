package com.company.lms.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "leave_balances",
       uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "leave_type"}))
public class LeaveBalance implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "leave_type", nullable = false, length = 60)
    private String leaveType;

    @Column(nullable = false)
    private int balance;

    public LeaveBalance() {}

    public LeaveBalance(Employee employee, String leaveType, int balance) {
        this.employee = employee;
        this.leaveType = leaveType;
        this.balance = balance;
    }

    public Integer getId() { return id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public int getBalance() { return balance; }
    public void setBalance(int balance) { this.balance = balance; }
}
