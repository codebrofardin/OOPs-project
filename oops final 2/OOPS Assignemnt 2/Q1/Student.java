import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Student account and academic information. */
public class Student {
    String name;
    final String email;
    String password;
    String phone;
    String address;
    int currentSemester = 1;
    final Map<String, Double> grades = new HashMap<>(); // Key: Course.uniqueKey()
    final Map<Integer, LinkedHashSet<String>> registrations = new HashMap<>();
    final Set<Integer> completedSemesters = new HashSet<>();

    public Student(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = "Not provided";
        this.address = "Not provided";
    }
}
