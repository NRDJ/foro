/** Modelos del foro: misma forma que las respuestas JSON de los microservicios. */

export type Rol = 'ADMIN' | 'USUARIO';
export type EstadoContenido = 'VISIBLE' | 'BANEADO';
export type TipoContenido = 'TEMA' | 'COMENTARIO';

export interface Usuario {
  id: number;
  username: string;
  email: string;
  nombre: string;
  /** Sin backend, la clave se guarda tal cual en el arreglo; ms-usuarios la guarda con BCrypt. */
  password: string;
  rol: Rol;
  fechaCreacion: string;
}

/** Datos de la sesión: nunca incluye la clave. */
export type UsuarioSesion = Omit<Usuario, 'password'>;

export interface Categoria {
  id: number;
  nombre: string;
  descripcion: string;
  icono: string;
  imagen: string;
}

export interface Tema {
  id: number;
  titulo: string;
  contenido: string;
  categoriaId: number;
  autorId: number;
  autorUsername: string;
  estado: EstadoContenido;
  fechaCreacion: string;
  fechaActualizacion: string;
}

export interface Comentario {
  id: number;
  temaId: number;
  contenido: string;
  autorId: number;
  autorUsername: string;
  estado: EstadoContenido;
  fechaCreacion: string;
}

export interface Baneo {
  id: number;
  tipo: TipoContenido;
  contenidoId: number;
  motivo: string;
  moderadorUsername: string;
  fechaCreacion: string;
}

/** Resultado de una operación: ok, o el mensaje que se muestra al usuario. */
export type Resultado = { ok: true } | { ok: false; error: string };
