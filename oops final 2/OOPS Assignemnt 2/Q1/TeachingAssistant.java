import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A Teaching Assistant is a specialized Student.
 * It inherits every student capability and additionally receives a limited
 * set of course assignments for grading assistance.
 */
public class TeachingAssistant extends Student {
    private final Set<String> assignedCourseKeys = new LinkedHashSet<>();

    public TeachingAssistant(String name, String email, String password) {
        super(name, email, password);
    }

    public Set<String> getAssignedCourseKeys() {
        return Collections.unmodifiableSet(assignedCourseKeys);
    }

    public boolean isAssignedTo(String courseKey) {
        return assignedCourseKeys.contains(courseKey);
    }

    public void assignCourse(String courseKey) {
        assignedCourseKeys.add(courseKey);
    }

    public void unassignCourse(String courseKey) {
        assignedCourseKeys.remove(courseKey);
    }
}
