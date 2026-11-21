package cl.duoc.foro.service;

import java.util.List;

import cl.duoc.foro.dto.CategoriaResponse;
import cl.duoc.foro.dto.ComentarioRequest;
import cl.duoc.foro.dto.ComentarioResponse;
import cl.duoc.foro.dto.PublicacionDetalleResponse;
import cl.duoc.foro.dto.PublicacionRequest;
import cl.duoc.foro.dto.PublicacionResponse;
import cl.duoc.foro.security.UsuarioActual;

/**
 * Patron Service Layer: contrato de la logica de negocio del foro para usuarios.
 */
public interface ForoService {

    List<CategoriaResponse> listarCategorias();

    List<PublicacionResponse> buscarPublicaciones(String texto, Long idCategoria);

    PublicacionDetalleResponse verPublicacion(Long idPublicacion);

    List<PublicacionResponse> misPublicaciones(UsuarioActual usuario);

    PublicacionResponse publicar(PublicacionRequest request, UsuarioActual usuario);

    PublicacionResponse editarPublicacion(Long idPublicacion, PublicacionRequest request, UsuarioActual usuario);

    void eliminarPublicacion(Long idPublicacion, UsuarioActual usuario);

    ComentarioResponse comentar(Long idPublicacion, ComentarioRequest request, UsuarioActual usuario);

    void eliminarComentario(Long idComentario, UsuarioActual usuario);
}
