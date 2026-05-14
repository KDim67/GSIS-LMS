package com.company.lms.model;

public enum LeaveType {
    ANNUAL("Κανονική Άδεια",       "#0188ca", 20),
    SICK("Αναρρωτική Άδεια",       "#dc3545", 30),
    UNPAID("Άδεια άνευ Αποδοχών", "#6c757d", 30),
    PARENTAL("Γονική Άδεια",       "#9b59b6", 14),
    STUDY("Εκπαιδευτική Άδεια",   "#28a745",  5);

    private final String displayName;
    private final String color;
    private final int defaultBalance;

    LeaveType(String displayName, String color, int defaultBalance) {
        this.displayName = displayName;
        this.color = color;
        this.defaultBalance = defaultBalance;
    }

    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
    public int getDefaultBalance() { return defaultBalance; }
}
