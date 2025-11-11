import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateBcryptHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password123 = encoder.encode("password123");
        String admin123 = encoder.encode("admin123");
        String manager123 = encoder.encode("manager123");
        
        System.out.println("password123 hash: " + password123);
        System.out.println("admin123 hash: " + admin123);
        System.out.println("manager123 hash: " + manager123);
    }
}
