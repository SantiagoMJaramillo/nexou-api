package com.cesde.nexou.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cesde.nexou.model.entity.Usuario;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/** Genera y valida tokens JWT (HS256). */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final String SECRETO_DESARROLLO = "nexou-secreto-solo-para-desarrollo-cambialo-en-produccion";

    private final byte[] secreto;
    private final long horasExpiracion;

    public JwtService(@Value("${app.jwt.secret:}") String secreto,
                      @Value("${app.jwt.expiracion-horas:24}") long horasExpiracion) {
        if (secreto == null || secreto.isBlank()) {
            log.warn("JWT_SECRET no está definido: se usa un secreto de desarrollo. Defínelo en producción.");
            secreto = SECRETO_DESARROLLO;
        }
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres");
        }
        this.secreto = bytes;
        this.horasExpiracion = horasExpiracion;
    }

    public String generar(Usuario usuario) {
        try {
            Instant ahora = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(String.valueOf(usuario.getId()))
                    .claim("usuario", usuario.getNombreUsuario())
                    .claim("rol", usuario.getRol().name())
                    .issueTime(Date.from(ahora))
                    .expirationTime(Date.from(ahora.plusSeconds(horasExpiracion * 3600)))
                    .build();
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(secreto));
            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el token", e);
        }
    }

    /** Devuelve el id del usuario si el token es válido y no ha expirado; si no, null. */
    public Long validarYObtenerUsuarioId(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!jwt.verify(new MACVerifier(secreto))) {
                return null;
            }
            Date expira = jwt.getJWTClaimsSet().getExpirationTime();
            if (expira == null || expira.before(new Date())) {
                return null;
            }
            return Long.valueOf(jwt.getJWTClaimsSet().getSubject());
        } catch (Exception e) {
            return null;
        }
    }
}
