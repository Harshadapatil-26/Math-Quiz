import java.sql.*;
import java.util.*;

public class MathQuizApp {

    // Score tracking variables
    private int correctAnswers = 0;
    private int totalQuestions = 0;

    // Database connection
    private Connection connection;

    // Constructor to initialize database connection
    public MathQuizApp() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:MathQuizDB.db");
            initializeDatabase();
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
    }

    // Initialize database tables
    private void initializeDatabase() throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS Scores (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "totalQuestions INTEGER, " +
                "correctAnswers INTEGER, " +
                "percentage REAL, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
        }
    }

    // Save score to database (without quiz_id)
    private void saveScoreToDatabase() {
        if (totalQuestions == 0) {
            System.out.println("No questions answered. Score will not be saved.");
            return;
        }

        String insertSQL = "INSERT INTO Scores (totalQuestions, correctAnswers, percentage) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            double percentage = (totalQuestions > 0) ? (correctAnswers / (double) totalQuestions) * 100 : 0;
            pstmt.setInt(1, totalQuestions);  // Set totalQuestions
            pstmt.setInt(2, correctAnswers);  // Set correctAnswers
            pstmt.setDouble(3, percentage);   // Set percentage
            pstmt.executeUpdate();
            System.out.println("Score saved successfully.");
        } catch (SQLException e) {
            System.err.println("Error saving score to database: " + e.getMessage());
        }
    }

    // View all past scores from database
    private void viewAllScores() {
        String querySQL = "SELECT * FROM Scores ORDER BY id DESC"; // Fetch scores ordered by primary key (id)
        try (PreparedStatement pstmt = connection.prepareStatement(querySQL)) {
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nAll Past Scores:");
            System.out.printf("%-5s %-15s %-15s %-10s %-20s\n", "ID", "Total Questions", "Correct Answers", "Percentage", "Timestamp");
            while (rs.next()) {
                System.out.printf("%-5d %-15d %-15d %-10.2f %-20s\n", 
                    rs.getInt("id"), 
                    rs.getInt("totalQuestions"), 
                    rs.getInt("correctAnswers"), 
                    rs.getDouble("percentage"), 
                    rs.getString("timestamp"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving scores: " + e.getMessage());
        }
    }

    // Main menu
    public void displayMenu() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Math Quiz Application!");

        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1. Start Quiz");
            System.out.println("2. View Score Summary");
            System.out.println("3. View All Past Scores");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        startQuiz();
                        saveScoreToDatabase();
                        break;
                    case 2:
                        viewScoreSummary();
                        break;
                    case 3:
                        viewAllScores();
                        break;
                    case 4:
                        System.out.println("Thank you for using the Math Quiz Application!");
                        closeDatabaseConnection();
                        scanner.close();
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    // Start the quiz
    private void startQuiz() {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        System.out.println("\nStarting the quiz...");
        System.out.print("Enter the number of questions: ");

        try {
            int numQuestions = Integer.parseInt(scanner.nextLine());

            for (int i = 1; i <= numQuestions; i++) {
                int num1 = random.nextInt(10) + 1;
                int num2 = random.nextInt(10) + 1;
                char operator = getRandomOperator();
                int correctAnswer = calculateAnswer(num1, num2, operator);

                System.out.printf("Question %d: %d %c %d = ?\n", i, num1, operator, num2);
                System.out.print("Your answer: ");

                try {
                    int userAnswer = Integer.parseInt(scanner.nextLine());

                    if (userAnswer == correctAnswer) {
                        System.out.println("Correct!");
                        correctAnswers++;
                    } else {
                        System.out.println("Wrong! The correct answer is " + correctAnswer);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a valid number.");
                }

                totalQuestions++;
            }

            System.out.println("\nQuiz completed!");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        }
    }

    // View score summary
    private void viewScoreSummary() {
        System.out.println("\nScore Summary:");
        System.out.println("Total Questions: " + totalQuestions);
        System.out.println("Correct Answers: " + correctAnswers);
        if (totalQuestions > 0) {
            double percentage = (correctAnswers / (double) totalQuestions) * 100;
            System.out.printf("Score Percentage: %.2f%%\n", percentage);
        } else {
            System.out.println("No questions answered yet.");
        }
    }

    // Generate a random operator
    private char getRandomOperator() {
        char[] operators = {'+', '-', '*'};
        Random random = new Random();
        return operators[random.nextInt(operators.length)];
    }

    // Calculate the answer based on operator
    private int calculateAnswer(int num1, int num2, char operator) {
        return switch (operator) {
            case '+' -> num1 + num2;
            case '-' -> num1 - num2;
            case '*' -> num1 * num2;
            default -> throw new IllegalArgumentException("Invalid operator: " + operator);
        };
    }

    // Close database connection
    private void closeDatabaseConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing the database connection: " + e.getMessage());
        }
    }

    // Main method
    public static void main(String[] args) {
        MathQuizApp app = new MathQuizApp();
        app.displayMenu();
    }
}
