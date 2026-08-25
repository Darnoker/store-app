package com.github.darnoker.userservice.identity;

import com.github.darnoker.userservice.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAccessTokenIssuer implements AccessTokenIssuer {

    private final JwtEncoder encoder;

    private final Clock clock;

    @Value("${identity.issuer}")
    String issuer;

    @Value("${identity.audience}")
    String audience;

    @Override
    public String issue(User user) {
        Instant now = Instant.now(clock);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(user.id().toString())
                .audience(java.util.List.of(audience))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim("user_id", user.id().toString())
                .claim("email", user.email())
                .claim("roles", List.of(user.role().name()))
                .build();

        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(), claims))
                .getTokenValue();
    }
}
