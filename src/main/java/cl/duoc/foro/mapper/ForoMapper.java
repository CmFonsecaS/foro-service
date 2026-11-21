package cl.duoc.foro.mapper;

import org.springframework.stereotype.Component;

import cl.duoc.foro.dto.CategoriaResponse;
import cl.duoc.foro.dto.ComentarioResponse;
import cl.duoc.foro.dto.PublicacionResponse;
import cl.duoc.foro.model.Categoria;
import cl.duoc.foro.model.Comentario;
import cl.duoc.foro.model.Publicacion;

/**
 * Patron DTO + Mapper: convierte entidades JPA en respuestas de la API.
 */
@Component
public class ForoMapper {

    public CategoriaResponse toResponse(Categoria categoria) {
        return new CategoriaResponse(
            categoria.getIdCategoria(),
            categoria.getNombre(),
            categoria.getDescripcion()
        );
    }

    public PublicacionResponse toResponse(Publicacion publicacion, long cantidadComentarios) {
        return new PublicacionResponse(
            publicacion.getIdPublicacion(),
            publicacion.getTitulo(),
            publicacion.getContenido(),
            publicacion.getAutor().getIdUsuario(),
            publicacion.getAutor().getNombre(),
            publicacion.getCategoria().getIdCategoria(),
            publicacion.getCategoria().getNombre(),
            publicacion.getEstado().name(),
            cantidadComentarios,
            publicacion.getFechaCreacion()
        );
    }

    public ComentarioResponse toResponse(Comentario comentario) {
        return new ComentarioResponse(
            comentario.getIdComentario(),
            comentario.getPublicacion().getIdPublicacion(),
            comentario.getContenido(),
            comentario.getAutor().getIdUsuario(),
            comentario.getAutor().getNombre(),
            comentario.getEstado().name(),
            comentario.getFechaCreacion()
        );
    }
}
