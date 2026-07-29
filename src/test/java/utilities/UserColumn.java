package utilities;

/**
 * Single source of truth for the Users AG-Grid columns, mirrored from
 * src/app/pages/users/table/_columns.tsx (tableColumns()), so Page Objects/tests never hard-code a
 * column id or header text that could drift from the frontend.
 *
 * <p>"S No" (row number, no {@code field}) and "Actions" are intentionally excluded here since
 * neither is sortable nor filterable in the real colDefs.
 */
public enum UserColumn {

    NAME("name", "Name"),
    EMAIL("email", "E-mail"),
    ROLE("role", "Role"),
    USER_REFERENCE_ID("user_reference_id", "User Ref Id"),
    CURRENCY("currency", "Currency"),
    EMPLOYEE_NUMBER("employee_number", "Employee Id"),
    DATE_OF_JOINING("date_of_joining", "Joining Date"),
    DESIGNATION("designation", "Designation"),
    MANAGER("manager", "Manager"),
    TEAM("team", "Team"),
    DEPARTMENT("department", "Department"),
    ACTIVE_SINCE("created_at", "Active Since"),
    NOTIFICATION_SENT("send_mail", "Notification Sent"),
    BANK_DETAILS("bank_info", "Bank Details"),
    TAX_DETAILS("tax_info", "Tax Details"),
    LAST_WORKING_DAY("last_working_day", "Last Working Day"),
    STATUS("status", "Status");

    public final String colId;
    public final String headerName;

    UserColumn(String colId, String headerName) {
        this.colId = colId;
        this.headerName = headerName;
    }
}
