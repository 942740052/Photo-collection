package com.photo.util;

import java.util.UUID;

public class ShareCodeUtil {
    
    public static String generateShareCode() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid.substring(0, 8);
    }
    
    public static String generateShareUrl(String baseUrl, String shareCode) {
        return baseUrl + "/share/" + shareCode;
    }
}
