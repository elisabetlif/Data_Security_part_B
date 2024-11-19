package lib;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import Implementation.UserRolesandPermissions;

public class SessionManager {
    private Key secretKey;
    private long accessTokenValidity;   
    private long refreshTokenValidity;
    private SecureRandom secureRandom = new SecureRandom();
    private final UserRolesandPermissions userRolesAndPermissions;

    // Store refresh tokens server-side
    private Map<String, RefreshTokenData> refreshTokenStore = new ConcurrentHashMap<>();
    private Map<String, Long> invalidatedAccessTokens = new ConcurrentHashMap<>();


    public SessionManager(long accessTokenValidity, long refreshTokenValidity) {
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
        this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        this.userRolesAndPermissions = new UserRolesandPermissions();
    }

    /**
     * Creates a new access token (JWT)
     * @param username Username
     * @param role User's role
     * @param permissions List of permissions
     * @return Access token (JWT)
     */
    public String createAccessToken(String username ) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date expiry = new Date(nowMillis + accessTokenValidity);
        String id = generateSecureID();
        String role = userRolesAndPermissions.getRole(username);
        List<String> permissions = userRolesAndPermissions.getPermissions(username);


        String token = Jwts.builder()
                .setId(id)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("role", role) 
                .claim("permissions", permissions)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        return token;
    }

    /**
     * Creates a new refresh token (opaque token)
     * @param username Username
     * @return Refresh token
     */
    public String createRefreshToken(String username) {
        String refreshToken = generateSecureID();
        long expiryTime = System.currentTimeMillis() + refreshTokenValidity;

        // Store the refresh token with associated username and expiry time
        refreshTokenStore.put(refreshToken, new RefreshTokenData(username, expiryTime));

        return refreshToken;
    }

    /**
     * Validates the access token (JWT)
     * @param token Access token
     * @return Claims if valid; null otherwise
     */
    public Claims validateAccessToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            Claims claims = claimsJws.getBody();
            
            String tokenId = claims.getId();
            if (invalidatedAccessTokens.containsKey(tokenId)) {
                return null; // Token has been invalidated
            }

            return claims;
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * Validates the refresh token
     * @param refreshToken Refresh token
     * @return Username if valid; null otherwise
     */
    public String validateRefreshToken(String refreshToken) {
        RefreshTokenData tokenData = refreshTokenStore.get(refreshToken);

        if (tokenData == null || tokenData.isExpired()) {
            return null;
        }

        return tokenData.getUsername();
    }

    /**
     * Invlidates both tokens
     * @param refreshToken Refresh token to invalidate
     * @param accessToken Access token to invalidate
     */
    public void invalidateTokens(String refreshToken, String accessToken){
        invalidateAccessToken(accessToken);
        invalidateRefreshToken(refreshToken);
        cleanupExpiredTokens();
    }

    /**
     * Invalidates the refresh token
     * @param refreshToken Refresh token to invalidate
     */
    public void invalidateRefreshToken(String refreshToken) {
        refreshTokenStore.remove(refreshToken);
    }

    /**
     * Invalidates the access token
     * @param token Refresh token to invalidate
     */
    private void invalidateAccessToken(String token) {
        Claims claims = validateAccessToken(token);
        if (claims != null) {
            String tokenId = claims.getId();
            Date expiration = claims.getExpiration();
            invalidatedAccessTokens.put(tokenId, expiration.getTime());
        }
    }
    

    /**
     * Generates a secure random ID
     * @return Secure token as a hexadecimal string
     */
    private String generateSecureID() {
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);

        StringBuilder hexString = new StringBuilder(128);
        for (byte b : randomBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private void cleanupExpiredTokens() {
        long now = System.currentTimeMillis();
        invalidatedAccessTokens.entrySet().removeIf(entry -> entry.getValue() <= now);
        refreshTokenStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * Inner class to store refresh token data
     */
    private static class RefreshTokenData {
        private String username;
        private long expiryTime;

        public RefreshTokenData(String username, long expiryTime) {
            this.username = username;
            this.expiryTime = expiryTime;
        }

        public String getUsername() {
            return username;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
}
