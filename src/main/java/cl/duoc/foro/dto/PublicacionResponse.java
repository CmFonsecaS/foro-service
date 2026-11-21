package cl.duoc.foro.dto;

import java.time.LocalDateTime;

public record PublicacionResponse(
    Long idPublicacion,
    String titulo,
    String contenido,
    Long idAutor,
    String autor,
    Long idCategoria,
    String categoria,
    String estado,
    long cantidadComentarios,
    LocalDateTime fechaCreacion
) {
}
