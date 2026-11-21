package cl.duoc.foro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PublicacionRequest(

    @NotBlank(message = "El titulo es obligatorio")
    @Size(min = 5, max = 200, message = "El titulo debe tener entre 5 y 200 caracteres")
    String titulo,

    @NotBlank(message = "El contenido es obligatorio")
    @Size(min = 10, max = 4000, message = "El contenido debe tener entre 10 y 4000 caracteres")
    String contenido,

    @NotNull(message = "La categoria es obligatoria")
    @Positive(message = "El ID de la categoria debe ser positivo")
    Long idCategoria

) {
}
