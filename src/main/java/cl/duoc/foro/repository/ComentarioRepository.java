package cl.duoc.foro.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.duoc.foro.model.Comentario;
import cl.duoc.foro.model.EstadoContenido;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    @Query("""
        select c from Comentario c
        join fetch c.autor
        where c.publicacion.idPublicacion = :idPublicacion
          and c.estado = :estado
        order by c.fechaCreacion asc
        """)
    List<Comentario> buscarPorPublicacion(
            @Param("idPublicacion") Long idPublicacion,
            @Param("estado") EstadoContenido estado);

    long countByPublicacionIdPublicacionAndEstado(Long idPublicacion, EstadoContenido estado);

    @Modifying
    @Query("delete from Comentario c where c.publicacion.idPublicacion = :idPublicacion")
    void eliminarPorPublicacion(@Param("idPublicacion") Long idPublicacion);
}
