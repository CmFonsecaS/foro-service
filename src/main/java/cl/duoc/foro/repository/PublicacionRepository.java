package cl.duoc.foro.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.duoc.foro.model.EstadoContenido;
import cl.duoc.foro.model.Publicacion;

public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {

    // Busqueda de publicaciones por estado, categoria (opcional) y texto en el titulo (opcional)
    @Query("""
        select p from Publicacion p
        join fetch p.autor
        join fetch p.categoria c
        where p.estado = :estado
          and (:idCategoria is null or c.idCategoria = :idCategoria)
          and (:texto is null or lower(p.titulo) like :texto)
        order by p.fechaCreacion desc
        """)
    List<Publicacion> buscar(
            @Param("estado") EstadoContenido estado,
            @Param("idCategoria") Long idCategoria,
            @Param("texto") String texto);

    // Publicaciones de un usuario (incluye las baneadas para que el autor las vea)
    @Query("""
        select p from Publicacion p
        join fetch p.autor
        join fetch p.categoria
        where p.autor.idUsuario = :idUsuario
        order by p.fechaCreacion desc
        """)
    List<Publicacion> buscarPorAutor(@Param("idUsuario") Long idUsuario);
}
