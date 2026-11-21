# Microservicio del Sistema de Foros en Línea

Proyecto generado con el arquetipo `cl.duoc:foro-microservicio-arquetipo:1.0.0`.

## Estructura por capas

| Paquete | Responsabilidad |
|---|---|
| `controller` | Endpoints REST (JSON) |
| `service` | Interfaz + implementación con la lógica de negocio (patrón Service Layer) |
| `repository` | Acceso a Oracle con Spring Data JPA (patrón Repository) |
| `model` | Entidades JPA |
| `dto` | Objetos de entrada/salida y Mapper (patrón DTO) |
| `config` | Seguridad JWT, roles y CORS |
| `exception` | Excepciones de negocio y `GlobalExceptionHandler` (errores en JSON) |

## Endpoints REST

| Método | Endpoint | Descripción | Acceso |
|---|---|---|---|
| GET | /api/foro/categorias | Listar categorías | Público |
| GET | /api/foro/publicaciones | Buscar publicaciones activas (texto y categoría) | Público |
| GET | /api/foro/publicaciones/{id} | Ver detalle de una publicación con sus comentarios | Público |
| GET | /api/foro/mis-publicaciones | Listar las publicaciones propias | Autenticado |
| POST | /api/foro/publicaciones | Publicar un tema | Autenticado |
| PUT | /api/foro/publicaciones/{id} | Editar una publicación propia | Autor |
| DELETE | /api/foro/publicaciones/{id} | Eliminar una publicación propia | Autor |
| POST | /api/foro/publicaciones/{id}/comentarios | Comentar una publicación | Autenticado |
| DELETE | /api/foro/comentarios/{id} | Eliminar un comentario propio | Autor |

Las publicaciones y comentarios baneados por moderación no se muestran en el foro (`404 Not Found`).

## Pruebas automatizadas

```powershell
.\mvnw.cmd clean test
```

16 pruebas (1 omitida: prueba de contexto que requiere conexión a Oracle), sin fallos.

## Scripts SQL

La carpeta `database/` contiene los scripts de creación del usuario, las tablas, los datos iniciales
(5 categorías), las consultas de verificación y el usuario MODERADOR de demostración.

## Configuración

Variables de entorno requeridas (ver `.env.example`): `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`.

## Ejecutar

```powershell
.\mvnw.cmd spring-boot:run
```

Con Docker (el wallet se monta en `/app/wallet`):

```bash
docker build -t mi-servicio .
docker run --env-file .env -v "$(pwd)/wallet:/app/wallet:ro" -p 8080:8080 mi-servicio
```

## Despliegue en AWS

El microservicio se ejecuta como contenedor Docker en una instancia Amazon EC2, junto con los demás
microservicios y el frontend. Nginx actúa como proxy inverso y expone todo bajo una única URL pública;
las solicitudes a `/api/foro/**` se envían a este servicio (puerto interno `8083`).

| Componente | Uso |
|---|---|
| Amazon ECR | Repositorio de imágenes `foro-online/foro-service` |
| Amazon EC2 | Host Docker + Nginx, creado con CloudFormation (`foro-frontend/ec2/foro-online-ec2-stack.yml`) |
| AWS Systems Manager | Despliegue remoto en la instancia, sin SSH |
| Amazon S3 | Bucket privado con el wallet de Oracle |
| AWS Secrets Manager | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET` |

El workflow `.github/workflows/publish-ecr.yml` se ejecuta en cada push a `main` que modifica código
(los cambios solo de documentación se omiten):

1. Ejecuta las pruebas con Maven.
2. Construye la imagen Docker y la publica en ECR con las etiquetas `latest` y el SHA del commit.
3. Despliega en EC2 mediante Systems Manager: descarga el wallet desde S3, lee las credenciales desde
   Secrets Manager, reemplaza el contenedor y verifica `/actuator/health`. Si el servicio no queda
   disponible, restaura la versión anterior.

Configuración del repositorio en GitHub:

- **Secrets:** `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN`.
- **Variables:** `AWS_REGION`, `EC2_INSTANCE_ID`, `ORACLE_CREDENTIALS_SECRET_ARN`,
  `ORACLE_WALLET_S3_BUCKET`, `ORACLE_WALLET_S3_KEY`.

En AWS Academy las credenciales del laboratorio son temporales, por lo que los secrets deben
actualizarse al iniciar cada sesión.

El nombre, puerto y ruta de prueba del servicio se configuran en `ec2/service.env`.

## Pruebas con Postman

La colección `postman/Foro-Online.postman_collection.json` cubre los tres microservicios del sistema:
39 solicitudes y 41 validaciones automáticas, incluidos los casos de error 400, 401, 403, 404 y 409.

| Carpeta | Contenido |
|---|---|
| 0. Autenticación | Login de ADMIN, MODERADOR y USUARIO; credenciales incorrectas |
| 1. usuarios-service | Registro, perfil, listado, cambio de rol, estado y eliminación |
| 2. foro-service | Búsqueda, detalle, publicación, edición y comentarios |
| 3. administracion-service | Banear y reactivar publicaciones y comentarios; CRUD de categorías |
| 4. Limpieza | Elimina los datos creados durante la prueba |

Uso:

1. Importar la colección en Postman.
2. Ajustar la variable de colección `baseUrl` con la URL pública de la instancia EC2
   (la IP cambia si la instancia se reinicia).
3. Ejecutar primero los logins de la carpeta `0. Autenticación`: guardan los tokens en variables
   que usan las demás solicitudes. También se puede ejecutar la colección completa con *Run collection*.

Desde la línea de comandos:

```bash
npx newman run postman/Foro-Online.postman_collection.json
```

## Flujo de trabajo Git

- `main` es la rama estable: cada cambio de código integrado en ella se despliega en AWS.
- Los cambios se desarrollan en ramas de corta duración con un prefijo según su tipo:
  `feature/`, `fix/`, `docs/`, `ci/`, `refactor/`.
- Cada rama se integra a `main` mediante un Pull Request (merge commit) y se elimina después del merge.
- Los mensajes de commit siguen la convención *Conventional Commits*: `feat:`, `fix:`, `docs:`,
  `test:`, `ci:`, `refactor:`.
