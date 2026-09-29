package utilities;

/**
 * Single source of truth for the Data Streams AG-Grid columns, mirrored from
 * src/app/pages/data-streams/table/_columns.ts (tableColumns).
 *
 * <p>All 6 data columns are sortable and filterable. The "Action" column is excluded since it
 * does not support sort or filter.
 */
public enum DataStreamColumn {

    NAME("name", "Stream Name", true, true),
    SOURCE("source", "Source", true, true),
    STREAM_STATUS("stream_status", "Stream Status", true, true),
    STREAM_TYPE("stream_type", "Stream Type", true, true),
    DEPARTMENT_NAME("department_name", "Department", true, true),
    LAST_REFRESHED("last_refreshed", "Last Refreshed", true, true);

    public final String colId;
    public final String headerName;
    public final boolean sortable;
    public final boolean filterable;

    DataStreamColumn(String colId, String headerName, boolean sortable, boolean filterable) {
        this.colId = colId;
        this.headerName = headerName;
        this.sortable = sortable;
        this.filterable = filterable;
    }
}