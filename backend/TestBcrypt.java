import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestBcrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Test the EXACT hashes from hash-passwords.sql
        String password123Hash = "$2a$10$N9qo8uLOickgx2ZMRZoMye1J5Ul2hp6M9A1.F0Q8MRfOYWpuMnH2q";
        String admin123Hash = "$2a$10$8kPbHFn5sGfU5Vwp8QpMTegQbODcVfSV2xVcMWw.hZAL9GVbL0g76";
        
        System.out.println("=== Testing EXACT hashes from hash-passwords.sql ===\n");
        
        System.out.println("Testing password123:");
        System.out.println("Hash: " + password123Hash);
        System.out.println("Matches 'password123': " + encoder.matches("password123", password123Hash));
        
        System.out.println("\nTesting admin123:");
        System.out.println("Hash: " + admin123Hash);
        System.out.println("Matches 'admin123': " + encoder.matches("admin123", admin123Hash));
        
        // Generate fresh hashes for comparison
        System.out.println("\n=== Generating fresh hashes for comparison ===");
        String fresh123 = encoder.encode("password123");
        String freshAdmin = encoder.encode("admin123");
        System.out.println("password123 -> " + fresh123);
        System.out.println("admin123 -> " + freshAdmin);
        
        // Test the fresh ones
        System.out.println("\n=== Testing fresh hashes ===");
        System.out.println("Fresh password123 matches: " + encoder.matches("password123", fresh123));
        System.out.println("Fresh admin123 matches: " + encoder.matches("admin123", freshAdmin));
    }
}
