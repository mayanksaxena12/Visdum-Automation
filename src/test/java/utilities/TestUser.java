package utilities;

/** Immutable value object describing a user created by a test fixture. */
public class TestUser {

    public final String name;
    public final String email;
    public final String referenceId;
    public final String employeeNumber;
    public final String password;

    public TestUser(String name, String email, String referenceId, String employeeNumber,
            String password) {
        this.name = name;
        this.email = email;
        this.referenceId = referenceId;
        this.employeeNumber = employeeNumber;
        this.password = password;
    }
}
