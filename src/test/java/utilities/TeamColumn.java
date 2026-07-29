package utilities;

/**
 * Single source of truth for the Teams AG-Grid columns, mirrored from
 * src/app/pages/teams/table/_columns.tsx, so Page Objects/tests never hard-code a column id or
 * header text that could drift from the frontend.
 *
 * <p>Unlike the Users grid (where almost every column is both sortable and filterable), Teams only
 * has 3 sortable columns and 2 filterable columns -- flags are tracked explicitly per column rather
 * than assumed.
 */
public enum TeamColumn {

    S_NO("id", "S No", true, false),
    NAME("name", "Team Name", true, true),
    TEAM_MEMBERS("teamMembers", "Team Members", false, false),
    STATUS("status", "Status", true, true);

    public final String colId;
    public final String headerName;
    public final boolean sortable;
    public final boolean filterable;

    TeamColumn(String colId, String headerName, boolean sortable, boolean filterable) {
        this.colId = colId;
        this.headerName = headerName;
        this.sortable = sortable;
        this.filterable = filterable;
    }
}
