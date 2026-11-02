# Guion para el video en Kaltura (7 a 10 minutos)

Cubre los cuatro puntos del criterio 5 de la pauta: funcionamiento, características, diseño y funcionalidades con Postman. Los tiempos suman unos 9 minutos. Cuéntalo con tus palabras y muestra todo en tiempo real.

## Antes de grabar

- [ ] Listener de Oracle activo (ver «Puesta en marcha» en el README).
- [ ] Datos reiniciados: ejecutar `database/02_esquema_y_datos.sql` como `FORO`.
- [ ] SQL Developer abierto, conectado como `FORO` a `XEPDB1`.
- [ ] Colección importada en Postman.
- [ ] Ambos microservicios **detenidos**: los levantas durante el video.
- [ ] Repositorio de GitHub abierto en el navegador.

## Escenas

| Min | Punto | Qué mostrar | Qué decir |
|---|---|---|---|
| 0:00 – 0:45 | Presentación | Tu nombre y el README | El caso, los 2 microservicios elegidos y por qué |
| 0:45 – 2:00 | Diseño | Diagramas del README; carpetas `controller`, `service`, `repository`, `model` | Separación por capas, una responsabilidad por servicio, quién es dueño de cada tabla, Oracle local |
| 2:00 – 2:45 | Características | `database/02_esquema_y_datos.sql`, `SecurityConfig`, `GlobalExceptionHandler` | Tablas, 2 roles, 5 categorías, JWT, validaciones y errores JSON |
| 2:45 – 3:15 | Git | Historial y ramas en GitHub | Trabajo por ramas `feature/*`, commits y merges |
| 3:15 – 3:45 | Ejecución | Levantar ambos servicios; en SQL Developer, `SELECT` de las 4 tablas | Puertos 8081 y 8082, cada tabla con al menos 3 registros |
| 3:45 – 5:30 | Postman: usuarios | Carpetas 1 y 2 | Ver abajo |
| 5:30 – 7:45 | Postman: foro | Carpetas 3, 4 y 5 | Ver abajo |
| 7:45 – 8:30 | Verificar en Oracle | Las consultas de abajo, antes de ejecutar la limpieza | Lo creado desde Postman quedó guardado en Oracle |
| 8:30 – 9:00 | Cierre | Colección completa en el Runner con todo en verde | Resumen y lo que se podría mejorar |

## Demostraciones que no deben faltar

**Usuarios**

1. Login de `admin` y de `camila.rojas`: mostrar el token y el rol en el JSON.
2. Registro público enviando `"rol":"ADMIN"`: queda como `USUARIO`.
3. `GET /api/usuarios` tres veces: sin sesión (401), como USUARIO (403) y como ADMIN (200). **Esta es la prueba de los privilegios por rol.**
4. Crear, modificar (PUT) y eliminar (DELETE) un usuario, y confirmar el 404 posterior.
5. Datos inválidos (400) y usuario duplicado (409): mostrar el JSON de error.

**Foro**

1. Listar categorías: se ven las 5 con su total de temas.
2. Buscar `?q=oracle` y filtrar `?categoriaId=1&autorId=2`.
3. Ver el detalle `/api/temas/1` con sus comentarios.
4. Publicar un tema: el autor sale del token.
5. Diego intenta modificar el tema de Camila (403); Camila sí puede (200).
6. Comentar, modificar y eliminar el comentario como ADMIN.
7. Eliminar una categoría que tiene temas (409).

## Consultas para verificar en SQL Developer

```sql
SELECT id, username, email, rol FROM usuarios ORDER BY id;
SELECT id, nombre FROM categorias ORDER BY id;
SELECT id, titulo, autor_username, categoria_id FROM temas ORDER BY id;
SELECT id, tema_id, autor_username, contenido FROM comentarios ORDER BY id;
```

Haz un cambio en Postman, vuelve a ejecutar la consulta y muestra la fila nueva o modificada.

Comprueba la duración antes de subir: el video debe durar entre 7 y 10 minutos.
