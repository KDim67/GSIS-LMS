package com.company.lms.model;

public enum LeaveStatus {
    APPROVED, PENDING, REJECTED;

    public String getDisplayName() {
        return switch (this) {
            case APPROVED -> "Εγκρίθηκε";
            case PENDING  -> "Εκκρεμεί";
            case REJECTED -> "Απορρίφθηκε";
        };
    }
}