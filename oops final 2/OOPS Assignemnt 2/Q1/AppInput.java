import java.util.Scanner;

/** Shared console input helpers. */
public final class AppInput {
    private static final Scanner SCANNER = new Scanner(System.in);
    private AppInput() { }

    public static String read(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine().trim();
    }

    public static int readInt(String prompt) {
        while (true) {
            try { return Integer.parseInt(read(prompt)); }
            catch (NumberFormatException e) { System.out.println("Enter a valid whole number."); }
        }
    }

    public static double readDouble(String prompt) {
        while (true) {
            try { return Double.parseDouble(read(prompt)); }
            catch (NumberFormatException e) { System.out.println("Enter a valid number."); }
        }
    }

    public static double readPositiveDouble(String prompt) {
        while (true) {
            double value = readDouble(prompt);
            if (value > 0) return value;
            System.out.println("Value must be greater than zero.");
        }
    }

    public static void line() {
        System.out.println("----------------------------------------------------------------------------------------------------");
    }
}
