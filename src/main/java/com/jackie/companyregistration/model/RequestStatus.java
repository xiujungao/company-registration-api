package com.jackie.companyregistration.model;

/**
 * Async registration job lifecycle. Stored as {@code status_code} on {@code registration_requests}
 * and {@code registration_request_status_history}; must match rows in {@code registration_request_statuses}
 * (see {@code db/ddl/data.sql}).
 */
public enum RequestStatus {
    /** Accepted, waiting for {@link com.jackie.companyregistration.service.RegistrationRequestWorker}. */
    PENDING("Pending", 1, false),
    /** Worker is running lookup / register logic. */
    PROCESSING("Processing", 2, false),
    /** Linked to an existing company or created a new one. */
    COMPLETED("Completed", 3, true),
    /** Lookup rejected or an unexpected error; see {@code error_message}. */
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
