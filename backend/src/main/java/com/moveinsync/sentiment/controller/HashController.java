package com.moveinsync.sentiment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Temporary controller to generate BCrypt hashes
 * DELETE THIS AFTER FIXING PASSWORDS
 */
@RestController
@RequestMapping("/public/hash")
@RequiredArgsConstructor
public class HashController {

    private final PasswordEncoder passwordEncoder;

    @GetMapping("/generate")
    public Map<String, String> generateHashes() {
        Map<String, String> hashes = new HashMap<>();
        
        String password123Hash = passwordEncoder.encode("password123");
        String admin123Hash = passwordEncoder.encode("admin123");
        
        hashes.put("password123", password123Hash);
        hashes.put("admin123", admin123Hash);
        hashes.put("note", "Use these hashes in the database");
        
        return hashes;
    }
    
    @PostMapping("/test")
    public Map<String, Object> testHash(@RequestBody Map<String, String> request) {
        String plaintext = request.get("plaintext");
        String hash = request.get("hash");
        
        Map<String, Object> result = new HashMap<>();
        result.put("plaintext", plaintext);
        result.put("hash", hash);
        result.put("matches", passwordEncoder.matches(plaintext, hash));
        
        return result;
    }
}
