/** Course catalogue entry. A separate short name distinguishes lab batches. */
public class Course {
    String code;
    String title;
    String shortName;
    String professorName;
    String professorEmail = "";
    String prerequisite = "";
    int semester;
    double credits;
    double totalHours;
    double scheduledHours;
    String remarks = "";

    // Details not included in the supplied timetable; admin/professor can fill these in.
    String day = "";
    String timing = "";
    String room = "";
    String syllabus = "Not provided";
    String officeHours = "Not provided";
    int enrollmentLimit = 60;

    public Course(String code, String title, String shortName, int semester,
                  double credits, double totalHours, double scheduledHours,
                  String professorName, String remarks) {
        this.code = code;
        this.title = title;
        this.shortName = shortName;
        this.semester = semester;
        this.credits = credits;
        this.totalHours = totalHours;
        this.scheduledHours = scheduledHours;
        this.professorName = professorName;
        this.remarks = remarks == null ? "" : remarks;
    }

    /** Unique internal identity when the timetable repeats a course code for batches. */
    public String uniqueKey() {
        return code.toUpperCase() + "#" + shortName.toUpperCase();
    }

    public String displayCode() {
        return code + " [" + shortName + "]";
    }

    public void display() {
        System.out.printf("%-9s | %-43s | %-12s | Sem %-2d | %5.2f credits | %-27s | Total: %5.1f | Scheduled: %5.1f%n",
                code, title, shortName, semester, credits, professorName, totalHours, scheduledHours);
        System.out.println("           Prerequisite: " + (prerequisite.isEmpty() ? "None" : prerequisite)
                + " | Timing: " + displayOrNotSet(day + (timing.isEmpty() ? "" : " " + timing))
                + " | Room: " + displayOrNotSet(room)
                + " | Remarks: " + (remarks.isEmpty() ? "-" : remarks));
    }

    private String displayOrNotSet(String value) {
        return value == null || value.trim().isEmpty() ? "Not set" : value;
    }
}
