package cl.duoc.foro.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.foro.model.Autor;

public interface AutorRepository extends JpaRepository<Autor, Long> {
}
