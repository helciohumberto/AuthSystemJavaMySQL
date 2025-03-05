import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/auth_db";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

class UserService {
    public static void registerUser(String username, String password) {
        String hashedPassword = hashPassword(password);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")
        ) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.executeUpdate();
            System.out.println("Usuário registrado com sucesso!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean authenticateUser(String username, String password) {
        String hashedPassword = hashPassword(password);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")
        ) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao criar hash da senha", e);
        }
    }
}

public class AuthSystem {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("1. Registrar\n2. Login");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                System.out.print("Usuário: ");
                String username = scanner.nextLine();
                System.out.print("Senha: ");
                String password = scanner.nextLine();
                UserService.registerUser(username, password);
            } else if (choice == 2) {
                System.out.print("Usuário: ");
                String username = scanner.nextLine();
                System.out.print("Senha: ");
                String password = scanner.nextLine();
                if (UserService.authenticateUser(username, password)) {
                    System.out.println("Login bem-sucedido!");
                } else {
                    System.out.println("Falha no login. Verifique suas credenciais.");
                }
            }
        }
    }
}
