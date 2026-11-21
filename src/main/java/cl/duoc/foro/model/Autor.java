package cl.duoc.foro.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

/**
 * Vista de solo lectura de la tabla USUARIOS (la administra usuarios-service).
 * Solo se usa para mostrar el nombre del autor.
 */
@Entity
@Immutable
@Table(name = "USUARIOS")
public class Autor {

    @Id
    @Column(name = "ID_USUARIO")
    private Long idUsuario;

    @Column(name = "NOMBRE")
    private String nombre;

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }
}
