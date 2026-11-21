package cl.duoc.foro.dto;

import java.util.List;

/**
 * Publicacion con sus comentarios activos.
 */
public record PublicacionDetalleResponse(
    PublicacionResponse publicacion,
    List<ComentarioResponse> comentarios
) {
}
