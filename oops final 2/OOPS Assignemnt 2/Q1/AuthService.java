/** Signup and login for Students, Professors, Administrator and Teaching Assistants. */
public final class AuthService {
    private AuthService() { }

    public static void studentEntry() {
        System.out.println("\n1. Sign up\n2. Login");
        int choice = AppInput.readInt("Choose: ");
        if (choice == 1) studentSignup();
        else if (choice == 2) {
            String email = AppInput.read("Email: ").toLowerCase();
            String password = AppInput.read("Password: ");
            Student student = DataStore.students.get(email);
            if (student != null && student.password.equals(password)) StudentService.menu(student);
            else System.out.println("Invalid email or password.");
        } else System.out.println("Invalid choice.");
    }

    private static void studentSignup() {
        String name = AppInput.read("Full name: ");
        String email = AppInput.read("Email: ").toLowerCase();
        if (name.isEmpty() || !validEmail(email) || emailUsed(email)) {
            System.out.println("Enter a name and a valid, unused email."); return;
        }
        String password = AppInput.read("Create password: ");
        if (password.length() < 4) { System.out.println("Password must contain at least 4 characters."); return; }
        Student student = new Student(name, email, password);
        student.phone = AppInput.read("Phone number (optional): ");
        student.address = AppInput.read("Address (optional): ");
        if (student.phone.isEmpty()) student.phone = "Not provided";
        if (student.address.isEmpty()) student.address = "Not provided";
        DataStore.students.put(email, student);
        DataStore.saveAll();
        System.out.println("Student account created. You can now log in.");
    }

    public static void professorEntry() {
        System.out.println("\n1. Sign up\n2. Login");
        int choice = AppInput.readInt("Choose: ");
        if (choice == 1) professorSignup();
        else if (choice == 2) {
            String email = AppInput.read("Email: ").toLowerCase();
            String password = AppInput.read("Password: ");
            Professor professor = DataStore.professors.get(email);
            if (professor != null && professor.password.equals(password)) ProfessorService.menu(professor);
            else System.out.println("Invalid email or password.");
        } else System.out.println("Invalid choice.");
    }

    private static void professorSignup() {
        String name = AppInput.read("Full name (use the faculty name from the catalogue): ");
        String email = AppInput.read("Email: ").toLowerCase();
        if (name.isEmpty() || !validEmail(email) || emailUsed(email)) {
            System.out.println("Enter a name and a valid, unused email."); return;
        }
        String password = AppInput.read("Create password: ");
        if (password.length() < 4) { System.out.println("Password must contain at least 4 characters."); return; }
        Professor professor = new Professor(name, email, password);
        professor.phone = AppInput.read("Phone number (optional): ");
        professor.expertise = AppInput.read("Area of expertise (optional): ");
        if (professor.phone.isEmpty()) professor.phone = "Not provided";
        if (professor.expertise.isEmpty()) professor.expertise = "Not provided";
        DataStore.professors.put(email, professor);
        DataStore.saveAll();
        System.out.println("Professor account created. Ask the administrator to assign your courses.");
    }

    /** TA accounts are preloaded and have a separate login; they cannot sign up as students. */
    public static void teachingAssistantLogin() {
        String email = AppInput.read("TA email: ").toLowerCase();
        String password = AppInput.read("TA password: ");
        TeachingAssistant ta = DataStore.teachingAssistants.get(email);
        if (ta != null && ta.password.equals(password)) TeachingAssistantService.menu(ta);
        else System.out.println("Invalid TA email or password.");
    }

    public static void adminLogin() {
        String email = AppInput.read("Admin email: ");
        String password = AppInput.read("Admin password: ");
        if (email.equalsIgnoreCase(DataStore.ADMIN_EMAIL) && password.equals(DataStore.ADMIN_PASSWORD))
            AdminService.menu();
        else System.out.println("Invalid administrator credentials.");
    }

    private static boolean emailUsed(String email) {
        return DataStore.students.containsKey(email) || DataStore.teachingAssistants.containsKey(email)
                || DataStore.professors.containsKey(email)
                || email.equalsIgnoreCase(DataStore.ADMIN_EMAIL);
    }

    private static boolean validEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}