package com.moveinsync.sentimentengine.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateNewUserHashes {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Generate hash for new admin password: "newadmin123"
        String adminHash = encoder.encode("newadmin123");
        System.out.println("New Admin Password Hash (newadmin123): " + adminHash);
        
        // Generate hash for new employee password: "employee123"
        String employeeHash = encoder.encode("employee123");
        System.out.println("New Employee Password Hash (employee123): " + employeeHash);
        
        // Verify hashes work
        System.out.println("\nVerification:");
        System.out.println("Admin hash matches: " + encoder.matches("newadmin123", adminHash));
        System.out.println("Employee hash matches: " + encoder.matches("employee123", employeeHash));
    }
}