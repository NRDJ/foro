import { Baneo, Categoria, Comentario, Tema, Usuario } from '../models/modelos';

/**
 * Datos estáticos de la aplicación (listas/arreglos). Son los mismos registros que carga
 * database/02_esquema_y_datos.sql en Oracle. El frontend no se comunica con el backend.
 * Clave de todos los usuarios de prueba: Foro2026!
 */

export const USUARIOS_INICIALES: Usuario[] = [
  { id: 1, username: 'admin', email: 'admin@foro.cl', nombre: 'Administración del Foro', password: 'Foro2026!', rol: 'ADMIN', fechaCreacion: '2026-08-01T10:00:00' },
  { id: 2, username: 'camila.rojas', email: 'camila.rojas@foro.cl', nombre: 'Camila Rojas', password: 'Foro2026!', rol: 'USUARIO', fechaCreacion: '2026-08-03T12:30:00' },
  { id: 3, username: 'diego.soto', email: 'diego.soto@foro.cl', nombre: 'Diego Soto', password: 'Foro2026!', rol: 'USUARIO', fechaCreacion: '2026-08-05T09:15:00' },
  { id: 4, username: 'valentina.diaz', email: 'valentina.diaz@foro.cl', nombre: 'Valentina Díaz', password: 'Foro2026!', rol: 'USUARIO', fechaCreacion: '2026-08-09T18:45:00' },
];

export const CATEGORIAS: Categoria[] = [
  { id: 1, nombre: 'Tecnología', descripcion: 'Programación, hardware y novedades tecnológicas', icono: 'bi-cpu', imagen: 'img/categorias/tecnologia.svg' },
  { id: 2, nombre: 'Videojuegos', descripcion: 'Lanzamientos, guías y comunidades de jugadores', icono: 'bi-controller', imagen: 'img/categorias/videojuegos.svg' },
  { id: 3, nombre: 'Deportes', descripcion: 'Fútbol, tenis, running y actividad física', icono: 'bi-trophy', imagen: 'img/categorias/deportes.svg' },
  { id: 4, nombre: 'Cine y Series', descripcion: 'Estrenos, críticas y recomendaciones', icono: 'bi-film', imagen: 'img/categorias/cine.svg' },
  { id: 5, nombre: 'Música', descripcion: 'Artistas, conciertos y descubrimientos musicales', icono: 'bi-music-note-beamed', imagen: 'img/categorias/musica.svg' },
];

export const TEMAS_INICIALES: Tema[] = [
  { id: 1, titulo: '¿Spring Boot o Quarkus para microservicios?', contenido: 'Estoy partiendo un proyecto nuevo y quiero conocer sus experiencias con ambos frameworks.', categoriaId: 1, autorId: 2, autorUsername: 'camila.rojas', estado: 'VISIBLE', fechaCreacion: '2026-09-01T10:00:00', fechaActualizacion: '2026-09-01T10:00:00' },
  { id: 2, titulo: 'Consejos para aprender Oracle SQL', contenido: '¿Qué recursos recomiendan para practicar consultas y modelamiento?', categoriaId: 1, autorId: 3, autorUsername: 'diego.soto', estado: 'VISIBLE', fechaCreacion: '2026-09-02T11:20:00', fechaActualizacion: '2026-09-02T11:20:00' },
  { id: 3, titulo: 'Mejores juegos cooperativos del año', contenido: 'Busco juegos para jugar con amigos en línea. ¿Cuáles recomiendan?', categoriaId: 2, autorId: 4, autorUsername: 'valentina.diaz', estado: 'VISIBLE', fechaCreacion: '2026-09-03T16:05:00', fechaActualizacion: '2026-09-03T16:05:00' },
  { id: 4, titulo: 'Rutinas para correr tu primera maratón', contenido: 'Empecé a entrenar hace tres meses y me gustaría armar un plan realista.', categoriaId: 3, autorId: 3, autorUsername: 'diego.soto', estado: 'VISIBLE', fechaCreacion: '2026-09-04T07:40:00', fechaActualizacion: '2026-09-04T07:40:00' },
  { id: 5, titulo: 'Series de ciencia ficción imperdibles', contenido: 'Compartan sus series favoritas del género y por qué las recomiendan.', categoriaId: 4, autorId: 2, autorUsername: 'camila.rojas', estado: 'VISIBLE', fechaCreacion: '2026-09-05T21:10:00', fechaActualizacion: '2026-09-05T21:10:00' },
  { id: 6, titulo: 'Bandas chilenas para descubrir', contenido: '¿Qué bandas nacionales nuevas están escuchando últimamente?', categoriaId: 5, autorId: 4, autorUsername: 'valentina.diaz', estado: 'VISIBLE', fechaCreacion: '2026-09-06T19:00:00', fechaActualizacion: '2026-09-06T19:00:00' },
  { id: 7, titulo: 'GANA DINERO FÁCIL SIN SALIR DE CASA', contenido: 'Haz clic en este enlace y multiplica tus ahorros en una semana.', categoriaId: 1, autorId: 3, autorUsername: 'diego.soto', estado: 'BANEADO', fechaCreacion: '2026-09-07T03:12:00', fechaActualizacion: '2026-09-07T03:12:00' },
  { id: 8, titulo: 'Vendo cuentas de juegos baratas', contenido: 'Cuentas con todo desbloqueado, escríbeme por interno.', categoriaId: 2, autorId: 4, autorUsername: 'valentina.diaz', estado: 'BANEADO', fechaCreacion: '2026-09-08T02:50:00', fechaActualizacion: '2026-09-08T02:50:00' },
];

export const COMENTARIOS_INICIALES: Comentario[] = [
  { id: 1, temaId: 1, contenido: 'Spring Boot tiene una comunidad enorme; para aprender es lo más cómodo.', autorId: 3, autorUsername: 'diego.soto', estado: 'VISIBLE', fechaCreacion: '2026-09-01T12:00:00' },
  { id: 2, temaId: 1, contenido: 'Quarkus arranca muy rápido, pero tiene menos material en español.', autorId: 4, autorUsername: 'valentina.diaz', estado: 'VISIBLE', fechaCreacion: '2026-09-01T13:30:00' },
  { id: 3, temaId: 2, contenido: 'La documentación oficial de Oracle y LiveSQL sirven mucho para practicar.', autorId: 2, autorUsername: 'camila.rojas', estado: 'VISIBLE', fechaCreacion: '2026-09-02T14:00:00' },
  { id: 4, temaId: 4, contenido: 'Recomiendo aumentar la distancia semanal de a poco para evitar lesiones.', autorId: 2, autorUsername: 'camila.rojas', estado: 'VISIBLE', fechaCreacion: '2026-09-04T09:00:00' },
  { id: 5, temaId: 5, contenido: 'Si les gusta la ciencia ficción dura, vale la pena partir por los clásicos.', autorId: 3, autorUsername: 'diego.soto', estado: 'VISIBLE', fechaCreacion: '2026-09-05T22:00:00' },
  { id: 6, temaId: 6, contenido: 'Vayan a las tocatas de su barrio: ahí aparecen las mejores sorpresas.', autorId: 2, autorUsername: 'camila.rojas', estado: 'VISIBLE', fechaCreacion: '2026-09-06T20:15:00' },
  { id: 7, temaId: 2, contenido: 'Esto no sirve, son todos unos ignorantes.', autorId: 4, autorUsername: 'valentina.diaz', estado: 'BANEADO', fechaCreacion: '2026-09-02T15:45:00' },
];

export const BANEOS_INICIALES: Baneo[] = [
  { id: 1, tipo: 'TEMA', contenidoId: 7, motivo: 'Spam: promociona ganancias fáciles con enlaces externos', moderadorUsername: 'admin', fechaCreacion: '2026-09-07T08:00:00' },
  { id: 2, tipo: 'TEMA', contenidoId: 8, motivo: 'Comercio no permitido: venta de cuentas', moderadorUsername: 'admin', fechaCreacion: '2026-09-08T08:30:00' },
  { id: 3, tipo: 'COMENTARIO', contenidoId: 7, motivo: 'Lenguaje ofensivo hacia otros usuarios', moderadorUsername: 'admin', fechaCreacion: '2026-09-02T16:00:00' },
];
