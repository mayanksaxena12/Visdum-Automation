package utilities;

/**
 * Single source of truth for the Departments AG-Grid columns, mirrored from
 * src/app/pages/department/table/_columns.ts.
 *
 * <p>Note the difference vs Teams: the "S No" (id) column is NOT sortable here, and only
 * "Department Name" and "Status" are sortable/filterable.
 */
public enum DepartmentColumn {

    S_NO("id", "S No", false, false),
    NAME("name", "Department Name", true, true),
    DEPARTMENT_MEMBERS("teamMembers", "Department Members", false, false),
    STATUS("status", "Status", true, true);

    public final String colId;
    public final String headerName;
    public final boolean sortable;
    public final boolean filterable;

    DepartmentColumn(String colId, String headerName, boolean sortable, boolean filterable) {
        this.colId = colId;
        this.headerName = headerName;
        this.sortable = sortable;
        this.filterable = filterable;
    }
}
