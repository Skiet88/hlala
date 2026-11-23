package com.hlala.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class ChecklistItemResult {

    private String item;      // e.g. "Room exists", "Toilet", "Meter"
    private boolean passed;
    private String note;

    protected ChecklistItemResult() {
    }

    public ChecklistItemResult(String item, boolean passed, String note) {
        this.item = item;
        this.passed = passed;
        this.note = note;
    }

    public String getItem() {
        return item;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getNote() {
        return note;
    }
}
