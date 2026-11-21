package cl.duoc.foro.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cl.duoc.foro.dto.ComentarioRequest;
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

class ForoServiceImplTest {

    private CategoriaRepository categoriaRepository;
    private PublicacionRepository publicacionRepository;
    private ComentarioRepository comentarioRepository;
    private AutorRepository autorRepository;
    private ForoServiceImpl service;

    private final UsuarioActual ana = new UsuarioActual(2L, "ana@foro.test", "USUARIO");
    private final UsuarioActual carlos = new UsuarioActual(3L, "carlos@foro.test", "USUARIO");
    private final UsuarioActual admin = new UsuarioActual(1L, "admin@foro.test", "ADMIN");

    @BeforeEach
    void setUp() {
        categoriaRepository = mock(CategoriaRepository.class);
        publicacionRepository = mock(PublicacionRepository.class);
        comentarioRepository = mock(ComentarioRepository.class);
        autorRepository = mock(AutorRepository.class);

        service = new ForoServiceImpl(categoriaRepository, publicacionRepository,
                comentarioRepository, autorRepository, new ForoMapper());

        when(publicacionRepository.save(any(Publicacion.class))).thenAnswer(i -> i.getArgument(0));
        when(comentarioRepository.save(any(Comentario.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void buscarSinTextoEnviaPatronNulo() {
        when(publicacionRepository.buscar(EstadoContenido.ACTIVO, null, null)).thenReturn(List.of());

        service.buscarPublicaciones("  ", null);

        verify(publicacionRepository).buscar(EstadoContenido.ACTIVO, null, null);
    }

    @Test
    void buscarConTextoUsaPatronEnMinusculas() {
        when(publicacionRepository.buscar(any(), any(), any())).thenReturn(List.of());

        service.buscarPublicaciones("Java", 2L);

        verify(publicacionRepository).buscar(EstadoContenido.ACTIVO, 2L, "%java%");
    }

    @Test
    void publicarAsignaAutorDelTokenYEstadoActivo() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria(1L, "Tecnologia")));
        when(autorRepository.findById(2L)).thenReturn(Optional.of(autor(2L, "Ana Garcia")));

        PublicacionResponse respuesta = service.publicar(
                new PublicacionRequest("  Nuevo tema  ", "Contenido del nuevo tema", 1L), ana);

        assertEquals("Nuevo tema", respuesta.titulo());
        assertEquals(2L, respuesta.idAutor());
        assertEquals("ACTIVO", respuesta.estado());
    }

    @Test
    void publicarEnCategoriaInexistenteLanza404() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.publicar(
                new PublicacionRequest("Titulo valido", "Contenido valido", 99L), ana));
    }

    @Test
    void verPublicacionBaneadaLanza404() {
        Publicacion baneada = publicacion(10L, autor(2L, "Ana"), EstadoContenido.BANEADO);
        when(publicacionRepository.findById(10L)).thenReturn(Optional.of(baneada));

        assertThrows(RecursoNoEncontradoException.class, () -> service.verPublicacion(10L));
    }

    @Test
    void comentarPublicacionBaneadaNoSePermite() {
        Publicacion baneada = publicacion(10L, autor(2L, "Ana"), EstadoContenido.BANEADO);
        when(publicacionRepository.findById(10L)).thenReturn(Optional.of(baneada));

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.comentar(10L, new ComentarioRequest("Hola"), carlos));
        verify(comentarioRepository, never()).save(any());
    }

    @Test
    void otroUsuarioNoPuedeEditarPublicacionAjena() {
        Publicacion deAna = publicacion(10L, autor(2L, "Ana"), EstadoContenido.ACTIVO);
        when(publicacionRepository.findById(10L)).thenReturn(Optional.of(deAna));

        assertThrows(AccesoDenegadoException.class, () -> service.editarPublicacion(
                10L, new PublicacionRequest("Titulo nuevo", "Contenido nuevo", 1L), carlos));
    }

    @Test
    void noSePuedeEditarPublicacionBaneada() {
        Publicacion baneada = publicacion(10L, autor(2L, "Ana"), EstadoContenido.BANEADO);
        when(publicacionRepository.findById(10L)).thenReturn(Optional.of(baneada));

        assertThrows(ConflictoException.class, () -> service.editarPublicacion(
                10L, new PublicacionRequest("Titulo nuevo", "Contenido nuevo", 1L), ana));
    }

    @Test
    void autorEliminaPublicacionYSusComentarios() {
        Publicacion deAna = publicacion(10L, autor(2L, "Ana"), EstadoContenido.ACTIVO);
        when(publicacionRepository.findById(10L)).thenReturn(Optional.of(deAna));

        service.eliminarPublicacion(10L, ana);

        verify(comentarioRepository).eliminarPorPublicacion(10L);
        verify(publicacionRepository).delete(deAna);
    }

    @Test
    void administradorPuedeEliminarPublicacionAjena() {
        Publicacion deAna = publicacion(10L, autor(2L, "Ana"), EstadoContenido.ACTIVO);
        when(publicacionRepository.findById(10L)).thenReturn(Optional.of(deAna));

        service.eliminarPublicacion(10L, admin);

        verify(publicacionRepository).delete(eq(deAna));
    }

    @Test
    void tokenSinIdDeUsuarioEsRechazado() {
        UsuarioActual sinId = new UsuarioActual(null, "x@foro.test", "USUARIO");

        assertThrows(AccesoDenegadoException.class, () -> service.misPublicaciones(sinId));
    }

    @Test
    void usuarioActualSinClaimNoFalla() {
        UsuarioActual usuario = new UsuarioActual(null, null, null);
        assertNull(usuario.idUsuario());
    }

    // ===== Datos de prueba =====

    private static Categoria categoria(Long id, String nombre) {
        Categoria categoria = new Categoria();
        asignar(categoria, "idCategoria", id);
        asignar(categoria, "nombre", nombre);
        return categoria;
    }

    private static Autor autor(Long id, String nombre) {
        Autor autor = new Autor();
        asignar(autor, "idUsuario", id);
        asignar(autor, "nombre", nombre);
        return autor;
    }

    private static Publicacion publicacion(Long id, Autor autor, EstadoContenido estado) {
        Publicacion publicacion = new Publicacion();
        asignar(publicacion, "idPublicacion", id);
        publicacion.setTitulo("Titulo");
        publicacion.setContenido("Contenido");
        publicacion.setAutor(autor);
        publicacion.setCategoria(categoria(1L, "Tecnologia"));
        publicacion.setEstado(estado);
        return publicacion;
    }

    private static void asignar(Object objeto, String campo, Object valor) {
        try {
            Field field = objeto.getClass().getDeclaredField(campo);
            field.setAccessible(true);
            field.set(objeto, valor);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
