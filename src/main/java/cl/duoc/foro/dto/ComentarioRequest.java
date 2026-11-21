package cl.duoc.foro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComentarioRequest(

    @NotBlank(message = "El comentario es obligatorio")
    @Size(min = 2, max = 1000, message = "El comentario debe tener entre 2 y 1000 caracteres")
    String contenido

) {
}
