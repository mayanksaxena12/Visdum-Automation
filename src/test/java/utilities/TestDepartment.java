package utilities;

/** Immutable value object describing a department created by a test fixture (mirrors TestTeam). */
public class TestDepartment {

    public final String name;
    public final String description;

    public TestDepartment(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
