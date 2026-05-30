package com.jackie.companyregistration.model;

public enum RequestStatus {
    PENDING("Pending", 1, false),
    PROCESSING("Processing", 2, false),
    COMPLETED("Completed", 3, true),
    FAILED("Failed", 4, true);

    private final String displayName;
    private final int sortOrder;
    private final boolean terminal;

    RequestStatus(String displayName, int sortOrder, boolean terminal) {
        this.displayName = displayName;
        this.sortOrder = sortOrder;
        this.terminal = terminal;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public boolean isTerminal() {
        return terminal;
    }

}
