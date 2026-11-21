package cl.duoc.foro.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.foro.dto.CategoriaResponse;
import cl.duoc.foro.dto.ComentarioRequest;
import cl.duoc.foro.dto.ComentarioResponse;
import cl.duoc.foro.dto.PublicacionDetalleResponse;
import cl.duoc.foro.dto.PublicacionRequest;
import cl.duoc.foro.dto.PublicacionResponse;
import cl.duoc.foro.exception.AccesoDenegadoException;
import cl.duoc.foro.exception.ConflictoException;
import cl.duoc.foro.exception.RecursoNoEncontradoException;
import cl.duoc.foro.mapper.ForoMapper;
import cl.duoc.foro.model.Autor;
import cl.duoc.foro.model.Categoria;
import cl.duoc.foro.model.Comentario;
import cl.duoc.foro.model.EstadoContenido;
import cl.duoc.foro.model.Publicacion;
import cl.duoc.foro.repository.AutorRepository;
import cl.duoc.foro.repository.CategoriaRepository;
import cl.duoc.foro.repository.ComentarioRepository;
import cl.duoc.foro.repository.PublicacionRepository;
import cl.duoc.foro.security.UsuarioActual;

@Service
public class ForoServiceImpl implements ForoService {

    private final CategoriaRepository categoriaRepository;
    private final PublicacionRepository publicacionRepository;
    private final ComentarioRepository comentarioRepository;
    private final AutorRepository autorRepository;
    private final ForoMapper mapper;

    public ForoServiceImpl(
            CategoriaRepository categoriaRepository,
            PublicacionRepository publicacionRepository,
            ComentarioRepository comentarioRepository,
            AutorRepository autorRepository,
            ForoMapper mapper) {

        this.categoriaRepository = categoriaRepository;
        this.publicacionRepository = publicacionRepository;
        this.comentarioRepository = comentarioRepository;
        this.autorRepository = autorRepository;
        this.mapper = mapper;
    }

    // =====================================================
    // CONSULTAS PUBLICAS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicacionResponse> buscarPublicaciones(String texto, Long idCategoria) {

        String patron = (texto == null || texto.isBlank())
                ? null
                : "%" + texto.trim().toLowerCase(Locale.ROOT) + "%";

        return publicacionRepository.buscar(EstadoContenido.ACTIVO, idCategoria, patron)
                .stream()
                .map(this::conContador)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PublicacionDetalleResponse verPublicacion(Long idPublicacion) {

        Publicacion publicacion = buscarActiva(idPublicacion);

        List<ComentarioResponse> comentarios = comentarioRepository
                .buscarPorPublicacion(idPublicacion, EstadoContenido.ACTIVO)
                .stream()
                .map(mapper::toResponse)
                .toList();

        return new PublicacionDetalleResponse(
                mapper.toResponse(publicacion, comentarios.size()),
                comentarios);
    }

    // =====================================================
    // OPERACIONES DEL USUARIO AUTENTICADO
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<PublicacionResponse> misPublicaciones(UsuarioActual usuario) {
        return publicacionRepository.buscarPorAutor(idDe(usuario))
                .stream()
                .map(this::conContador)
                .toList();
    }

    @Override
    @Transactional
    public PublicacionResponse publicar(PublicacionRequest request, UsuarioActual usuario) {

        Publicacion publicacion = new Publicacion();
        publicacion.setTitulo(request.titulo().trim());
        publicacion.setContenido(request.contenido().trim());
        publicacion.setCategoria(buscarCategoria(request.idCategoria()));
        publicacion.setAutor(buscarAutor(usuario));
        publicacion.setEstado(EstadoContenido.ACTIVO);

        return mapper.toResponse(publicacionRepository.save(publicacion), 0);
    }

    @Override
    @Transactional
    public PublicacionResponse editarPublicacion(
            Long idPublicacion, PublicacionRequest request, UsuarioActual usuario) {

        Publicacion publicacion = buscarPublicacion(idPublicacion);
        validarAutor(publicacion.getAutor(), usuario, "Solo el autor puede editar la publicacion");

        if (publicacion.getEstado() == EstadoContenido.BANEADO) {
            throw new ConflictoException("No se puede editar una publicacion baneada");
        }

        publicacion.setTitulo(request.titulo().trim());
        publicacion.setContenido(request.contenido().trim());
        publicacion.setCategoria(buscarCategoria(request.idCategoria()));

        return conContador(publicacionRepository.save(publicacion));
    }

    @Override
    @Transactional
    public void eliminarPublicacion(Long idPublicacion, UsuarioActual usuario) {

        Publicacion publicacion = buscarPublicacion(idPublicacion);
        validarAutor(publicacion.getAutor(), usuario, "Solo el autor puede eliminar la publicacion");

        // Al eliminar una publicacion propia se eliminan tambien sus comentarios
        comentarioRepository.eliminarPorPublicacion(idPublicacion);
        publicacionRepository.delete(publicacion);
    }

    @Override
    @Transactional
    public ComentarioResponse comentar(
            Long idPublicacion, ComentarioRequest request, UsuarioActual usuario) {

        Publicacion publicacion = buscarActiva(idPublicacion);

        Comentario comentario = new Comentario();
        comentario.setContenido(request.contenido().trim());
        comentario.setPublicacion(publicacion);
        comentario.setAutor(buscarAutor(usuario));
        comentario.setEstado(EstadoContenido.ACTIVO);

        return mapper.toResponse(comentarioRepository.save(comentario));
    }

    @Override
    @Transactional
    public void eliminarComentario(Long idComentario, UsuarioActual usuario) {

        Comentario comentario = comentarioRepository.findById(idComentario)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un comentario con ID: " + idComentario));

        validarAutor(comentario.getAutor(), usuario, "Solo el autor puede eliminar el comentario");
        comentarioRepository.delete(comentario);
    }

    // =====================================================
    // METODOS DE APOYO
    // =====================================================

    private PublicacionResponse conContador(Publicacion publicacion) {
        long cantidad = comentarioRepository.countByPublicacionIdPublicacionAndEstado(
                publicacion.getIdPublicacion(), EstadoContenido.ACTIVO);
        return mapper.toResponse(publicacion, cantidad);
    }

    private Publicacion buscarPublicacion(Long idPublicacion) {
        return publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una publicacion con ID: " + idPublicacion));
    }

    // Las publicaciones baneadas no son visibles en el foro
    private Publicacion buscarActiva(Long idPublicacion) {
        Publicacion publicacion = buscarPublicacion(idPublicacion);
        if (publicacion.getEstado() != EstadoContenido.ACTIVO) {
            throw new RecursoNoEncontradoException(
                    "La publicacion " + idPublicacion + " no esta disponible");
        }
        return publicacion;
    }

    private Categoria buscarCategoria(Long idCategoria) {
        return categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la categoria con ID: " + idCategoria));
    }

    private Autor buscarAutor(UsuarioActual usuario) {
        return autorRepository.findById(idDe(usuario))
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario del token no existe"));
    }

    private Long idDe(UsuarioActual usuario) {
        if (usuario == null || usuario.idUsuario() == null) {
            throw new AccesoDenegadoException("El token no contiene el ID del usuario");
        }
        return usuario.idUsuario();
    }

    private void validarAutor(Autor autor, UsuarioActual usuario, String mensaje) {
        boolean esAutor = autor.getIdUsuario().equals(idDe(usuario));
        if (!esAutor && !usuario.esAdministrador()) {
            throw new AccesoDenegadoException(mensaje);
        }
    }
}
