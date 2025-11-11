import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "password123";
        String hash = "$2a$10$N9qo8uLOickgx2ZMRZoMye1J5Ul2hp6M9A1.F0Q8MRfOYWpuMnH2q";
        
        boolean matches = encoder.matches(password, hash);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println("Matches: " + matches);
        
        String password2 = "admin123";
        String hash2 = "$2a$10$8kPbHFn5sGfU5Vwp8QpMTegQbODcVfSV2xVcMWw.hZAL9GVbL0g76";
        
        boolean matches2 = encoder.matches(password2, hash2);
        System.out.println("\nPassword: " + password2);
        System.out.println("Hash: " + hash2);
        System.out.println("Matches: " + matches2);
    }
}
