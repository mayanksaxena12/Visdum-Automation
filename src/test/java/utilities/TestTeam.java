package utilities;

/** Immutable value object describing a team created by a test fixture. */
public class TestTeam {

    public final String name;
    public final String description;

    public TestTeam(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
