package lib;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import Implementation.UserRolesandPermissions;

public class SessionManager {
    private KeyPair keyPair;
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
        this.keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
        this.userRolesAndPermissions = new UserRolesandPermissions();
    }

    /**
     * Creates a new access token (JWT)
     * @param username Username
     * @param role User's role
     * @param permissions List of permissions
     * @return Access token (JWT)
     */
    public String createAccessToken(String username, String refreshToken) {
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
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();
        
        RefreshTokenData tokenData = refreshTokenStore.get(refreshToken);
        if (tokenData != null) {
            tokenData.addAccessTokenId(id);
        }

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
            if(isTokenExpired(token)){
                System.err.println("Server: Access token expired");
                invalidatedAccessTokens.put(token, accessTokenValidity);
                
                return null;
            }

            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(keyPair.getPublic())
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
        if (tokenData == null) {
            return null;
        } else if (tokenData.isExpired()){
            Set<String> accessTokenIds = tokenData.getAccessTokenIds();
            long expiryTime = tokenData.getExpiryTime();

            for (String accessTokenId : accessTokenIds) {
                invalidatedAccessTokens.put(accessTokenId, expiryTime);
            }
        }

        return tokenData.getUsername();
    }

    /**
     * Invlidates both tokens
     * @param refreshToken Refresh token to invalidate
     * @param accessToken Access token to invalidate
     */
    public void invalidateTokens(String refreshToken){
        RefreshTokenData tokenData = refreshTokenStore.remove(refreshToken);
        if (tokenData != null) {
            Set<String> accessTokenIds = tokenData.getAccessTokenIds();
            long expiryTime = tokenData.getExpiryTime();

            for (String accessTokenId : accessTokenIds) {
                invalidatedAccessTokens.put(accessTokenId, expiryTime);
            }
            cleanupExpiredTokens();
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

    private boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(keyPair.getPublic())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
    
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (JwtException e) {
            System.out.println("Invalid token");
            return true; 
        }
    } 

    /**
     * Extracts username from the expired access token
     * @param token Expired access token
     * @return String name otherwise null
     */
    public String extractUsernameFromExpiredToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(keyPair.getPublic())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * Cleans up expired tokens to prevent memory leaks
     */
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
        private Set<String> accessTokenIds;
    
        public RefreshTokenData(String username, long expiryTime) {
            this.username = username;
            this.expiryTime = expiryTime;
            this.accessTokenIds = ConcurrentHashMap.newKeySet();
        }
    
        public String getUsername() {
            return username;
        }
    
        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    
        public long getExpiryTime() {
            return this.expiryTime;
        }
    
        public Set<String> getAccessTokenIds() {
            return accessTokenIds;
        }
    
        public void addAccessTokenId(String tokenId) {
            this.accessTokenIds.add(tokenId);
        }
    } 
}
