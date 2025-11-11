import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPasswordHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "Password123!";
        String hash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi";
        
        System.out.println("Testing password: " + password);
        System.out.println("Against hash: " + hash);
        System.out.println("Match: " + encoder.matches(password, hash));
        
        // Also test with admin123
        String adminPass = "admin123";
        String adminHash = "$2a$10$8IaYq8uT5TadBDWzxub6MOcY9hTCSpuP0oQbZLUqKpVwog7GToCCu";
        System.out.println("\nTesting password: " + adminPass);
        System.out.println("Against hash: " + adminHash);
        System.out.println("Match: " + encoder.matches(adminPass, adminHash));
        
        // Generate a new hash for Password123!
        System.out.println("\nGenerating new hash for Password123!:");
        System.out.println(encoder.encode(password));
    }
}
