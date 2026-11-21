package cl.duoc.foro.dto;

import java.time.LocalDateTime;

public record ComentarioResponse(
    Long idComentario,
    Long idPublicacion,
    String contenido,
    Long idAutor,
    String autor,
    String estado,
    LocalDateTime fechaCreacion
) {
}
