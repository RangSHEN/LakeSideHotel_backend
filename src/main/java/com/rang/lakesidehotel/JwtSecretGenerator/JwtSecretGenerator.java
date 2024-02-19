package com.rang.lakesidehotel.JwtSecretGenerator;

import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;

import java.util.Base64;

public class JwtSecretGenerator {
    public static void main(String[] args) {
        BytesKeyGenerator keyGenerator = KeyGenerators.secureRandom(32);
        byte[] secretBytes = keyGenerator.generateKey();
        String jwtSecret = Base64.getEncoder().encodeToString(secretBytes);

        System.out.println("Generated JWT Secret: " + jwtSecret);
    }
}
