package cl.duoc.foro.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.foro.dto.CategoriaResponse;
import cl.duoc.foro.dto.ComentarioRequest;
import cl.duoc.foro.dto.ComentarioResponse;
import cl.duoc.foro.dto.PublicacionDetalleResponse;
import cl.duoc.foro.dto.PublicacionRequest;
import cl.duoc.foro.dto.PublicacionResponse;
import cl.duoc.foro.security.UsuarioActual;
import cl.duoc.foro.service.ForoService;

import jakarta.validation.Valid;

/**
 * Endpoints del foro para usuarios: busqueda, visualizacion, publicacion y comentarios.
 */
@RestController
@RequestMapping("/api/foro")
public class ForoController {

    private final ForoService foroService;

    public ForoController(ForoService foroService) {
        this.foroService = foroService;
    }

    // GET: categorias del foro (publico)
    @GetMapping("/categorias")
    public List<CategoriaResponse> listarCategorias() {
        return foroService.listarCategorias();
    }

    // GET: buscar publicaciones activas por texto y/o categoria (publico)
    @GetMapping("/publicaciones")
    public List<PublicacionResponse> buscarPublicaciones(
            @RequestParam(name = "q", required = false) String texto,
            @RequestParam(required = false) Long idCategoria) {

        return foroService.buscarPublicaciones(texto, idCategoria);
    }

    // GET: ver una publicacion con sus comentarios (publico)
    @GetMapping("/publicaciones/{id}")
    public PublicacionDetalleResponse verPublicacion(@PathVariable Long id) {
        return foroService.verPublicacion(id);
    }

    // GET: publicaciones del usuario autenticado
    @GetMapping("/mis-publicaciones")
    public List<PublicacionResponse> misPublicaciones(@AuthenticationPrincipal Jwt jwt) {
        return foroService.misPublicaciones(UsuarioActual.desde(jwt));
    }

    // POST: publicar un tema
    @PostMapping("/publicaciones")
    public ResponseEntity<PublicacionResponse> publicar(
            @Valid @RequestBody PublicacionRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(foroService.publicar(request, UsuarioActual.desde(jwt)));
    }

    // PUT: editar una publicacion propia
    @PutMapping("/publicaciones/{id}")
    public PublicacionResponse editarPublicacion(
            @PathVariable Long id,
            @Valid @RequestBody PublicacionRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        return foroService.editarPublicacion(id, request, UsuarioActual.desde(jwt));
    }

    // DELETE: eliminar una publicacion propia
    @DeleteMapping("/publicaciones/{id}")
    public ResponseEntity<Void> eliminarPublicacion(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        foroService.eliminarPublicacion(id, UsuarioActual.desde(jwt));
        return ResponseEntity.noContent().build();
    }

    // POST: comentar una publicacion
    @PostMapping("/publicaciones/{id}/comentarios")
    public ResponseEntity<ComentarioResponse> comentar(
            @PathVariable Long id,
            @Valid @RequestBody ComentarioRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(foroService.comentar(id, request, UsuarioActual.desde(jwt)));
    }

    // DELETE: eliminar un comentario propio
    @DeleteMapping("/comentarios/{id}")
    public ResponseEntity<Void> eliminarComentario(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        foroService.eliminarComentario(id, UsuarioActual.desde(jwt));
        return ResponseEntity.noContent().build();
    }
}
