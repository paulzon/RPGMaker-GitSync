package com.rpgmaker.gitsync.model;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * Represents Git credentials (Username + Token) f�r GUI/Backend.
 */
public class Credentials {

    private final String username;
    private final String token;

    public Credentials(String username, String token) {
        this.username = username == null ? "" : username;
        this.token = token == null ? "" : token;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public boolean isValid() {
        return !username.isBlank() && !token.isBlank();
    }

    public CredentialsProvider toProvider() {
        return new UsernamePasswordCredentialsProvider(username, token);
    }
}
