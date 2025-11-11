import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class QuickTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String hash = encoder.encode(password);
        System.out.println("Generated hash: " + hash);
        System.out.println("Hash matches 'admin123': " + encoder.matches(password, hash));
        
        String existingHash = "$2a$10$8IaYq8uT5TadBDWzxub6MOcY9hTCSpuP0oQbZLUqKpVwog7GToCCu";
        System.out.println("\nTesting existing hash from seed-data.sql:");
        System.out.println("Hash: " + existingHash);
        System.out.println("Matches 'admin123': " + encoder.matches("admin123", existingHash));
    }
}
