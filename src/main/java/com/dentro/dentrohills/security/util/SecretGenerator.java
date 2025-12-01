package com.dentro.dentrohills.security.util;

import java.security.SecureRandom;
import java.util.Base64;
public class SecretGenerator {
    public static String generateSecret() {
        byte[] secret = new byte[32];  // 32 bytes = 256 bits
        new SecureRandom().nextBytes(secret);
        return Base64.getUrlEncoder().encodeToString(secret);  // Return base64 URL encoded secret
    }
}
