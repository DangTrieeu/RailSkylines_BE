package com.fourt.RailSkylines.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class SecurityUtil {
    private final JwtEncoder jwtEncoder;

    public SecurityUtil(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;
    @Value("${raiskylines.jwt.base64-secret}")
    private String jwtKey;

    @Value("${raiskylines.jwt.token-validity-in-seconds}")
    private long jwtKeyExpiration;

    public String createToken(Authentication authentication) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.jwtKeyExpiration, ChronoUnit.SECONDS);

// @formatter:off
JwtClaimsSet claims = JwtClaimsSet.builder()
.issuedAt(now)
.expiresAt(validity)
.subject(authentication.getName())
.claim("railskylines", authentication)
.build();
JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader,
claims)).getTokenValue();
    }
}
