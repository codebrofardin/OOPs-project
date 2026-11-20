/** Professor login and profile. */
public class Professor {
    String name;
    final String email;
    String password;
    String phone;
    String expertise;

    public Professor(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = "Not provided";
        this.expertise = "Not provided";
    }
}
