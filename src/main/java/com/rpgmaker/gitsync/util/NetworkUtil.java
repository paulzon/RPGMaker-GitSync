package com.rpgmaker.gitsync.util;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;

/**
 * Simple Internet-Check (DNS/Reachable).
 */
public final class NetworkUtil {

    private NetworkUtil() {
    }

    public static boolean hasInternetConnection() {
        try {
            InetAddress github = InetAddress.getByName("github.com");
            return github.isReachable((int) Duration.ofSeconds(2).toMillis());
        } catch (IOException ex) {
            return false;
        }
    }
}
