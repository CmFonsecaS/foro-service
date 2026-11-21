package cl.duoc.foro.security;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Datos del usuario autenticado obtenidos desde los claims del JWT
 * emitido por usuarios-service (subject = email, userId, role).
 */
public record UsuarioActual(Long idUsuario, String email, String rol) {

    public static UsuarioActual desde(Jwt jwt) {
        Object id = jwt.getClaim("userId");
        Long idUsuario = (id instanceof Number numero) ? numero.longValue() : null;
        return new UsuarioActual(idUsuario, jwt.getSubject(), jwt.getClaimAsString("role"));
    }

    public boolean esAdministrador() {
        return "ADMIN".equals(rol);
    }
}
