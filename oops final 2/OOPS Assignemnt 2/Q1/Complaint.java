import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Student complaint, including its date and administrator resolution details. */
public class Complaint {
    final int id;
    final String studentEmail;
    final String description;
    final LocalDateTime createdAt;
    String status = "Pending";
    String resolutionDetails = "";

    public Complaint(int id, String studentEmail, String description) {
        this(id, studentEmail, description, LocalDateTime.now());
    }

    /** Used when a complaint row is reconstructed from the database. */
    public Complaint(int id, String studentEmail, String description, LocalDateTime createdAt) {
        this.id = id;
        this.studentEmail = studentEmail;
        this.description = description;
        this.createdAt = createdAt;
    }

    public String dateText() {
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}