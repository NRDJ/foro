import { Injectable, computed, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { USUARIOS_INICIALES } from '../data/datos-iniciales';
import { Resultado, Rol, Usuario, UsuarioSesion } from '../models/modelos';
import { Coleccion } from './coleccion';

const CLAVE_SESION = 'agora.sesion';

/**
 * Patrón Singleton: providedIn 'root' crea una sola instancia para toda la aplicación,
 * así la sesión y la lista de usuarios son las mismas en todas las páginas.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly usuarios = new Coleccion<Usuario>('agora.usuarios', USUARIOS_INICIALES);

  private readonly sesionId = signal<number | null>(this.leerSesion());
  private readonly listaUsuarios = toSignal(this.usuarios.cambios$, { requireSync: true });

  /** Usuario conectado (sin la clave), o null. Se recalcula si cambia la sesión o la lista. */
  readonly actual = computed<UsuarioSesion | null>(() => {
    const id = this.sesionId();
    const u = this.listaUsuarios().find((x) => x.id === id);
    return u ? this.sinClave(u) : null;
  });
  readonly esAdmin = computed(() => this.actual()?.rol === 'ADMIN');

  login(identificador: string, password: string): Resultado {
    const clave = identificador.trim().toLowerCase();
    const usuario = this.usuarios.valor.find((u) => u.username === clave || u.email === clave);
    if (!usuario || usuario.password !== password) {
      return { ok: false, error: 'Usuario o contraseña incorrectos' };
    }
    this.guardarSesion(usuario.id);
    return { ok: true };
  }

  logout(): void {
    this.guardarSesion(null);
  }

  registrar(datos: { username: string; email: string; nombre: string; password: string }, rol: Rol = 'USUARIO'): Resultado {
    const username = datos.username.trim().toLowerCase();
    const email = datos.email.trim().toLowerCase();
    if (this.existeUsername(username)) {
      return { ok: false, error: 'El nombre de usuario ya está registrado' };
    }
    if (this.existeEmail(email)) {
      return { ok: false, error: 'El correo ya está registrado' };
    }
    this.usuarios.agregar({
      id: this.usuarios.siguienteId(),
      username,
      email,
      nombre: datos.nombre.trim(),
      password: datos.password,
      rol,
      fechaCreacion: new Date().toISOString(),
    });
    return { ok: true };
  }

  existeUsername(username: string): boolean {
    return this.usuarios.valor.some((u) => u.username === username.trim().toLowerCase());
  }

  existeEmail(email: string, excepto?: number): boolean {
    const valor = email.trim().toLowerCase();
    return this.usuarios.valor.some((u) => u.email === valor && u.id !== excepto);
  }

  /** Simula el envío del correo: devuelve el código que en un sistema real llegaría por email. */
  solicitarRecuperacion(email: string): { ok: true; codigo: string } | { ok: false; error: string } {
    if (!this.existeEmail(email)) {
      return { ok: false, error: 'No hay ninguna cuenta registrada con ese correo' };
    }
    const codigo = String(Math.floor(100000 + Math.random() * 900000));
    sessionStorage.setItem('agora.recuperacion', JSON.stringify({ email: email.trim().toLowerCase(), codigo }));
    return { ok: true, codigo };
  }

  restablecerPassword(email: string, codigo: string, nueva: string): Resultado {
    const pendiente = JSON.parse(sessionStorage.getItem('agora.recuperacion') ?? 'null') as { email: string; codigo: string } | null;
    if (!pendiente || pendiente.email !== email.trim().toLowerCase() || pendiente.codigo !== codigo.trim()) {
      return { ok: false, error: 'El código de verificación no es correcto' };
    }
    const usuario = this.usuarios.valor.find((u) => u.email === pendiente.email)!;
    this.usuarios.actualizar(usuario.id, { password: nueva });
    sessionStorage.removeItem('agora.recuperacion');
    return { ok: true };
  }

  actualizarPerfil(datos: { nombre: string; email: string }): Resultado {
    const yo = this.actual();
    if (!yo) return { ok: false, error: 'Debe iniciar sesión' };
    if (this.existeEmail(datos.email, yo.id)) {
      return { ok: false, error: 'El correo ya está registrado por otro usuario' };
    }
    this.usuarios.actualizar(yo.id, { nombre: datos.nombre.trim(), email: datos.email.trim().toLowerCase() });
    return { ok: true };
  }

  cambiarPassword(actual: string, nueva: string): Resultado {
    const yo = this.actual();
    const usuario = yo && this.usuarios.buscar(yo.id);
    if (!usuario || usuario.password !== actual) {
      return { ok: false, error: 'La contraseña actual no es correcta' };
    }
    if (actual === nueva) {
      return { ok: false, error: 'La nueva contraseña debe ser distinta de la actual' };
    }
    this.usuarios.actualizar(usuario.id, { password: nueva });
    return { ok: true };
  }

  // ---- Administración (solo ADMIN) ----

  cambiarRol(id: number, rol: Rol): Resultado {
    if (!this.esAdmin()) return { ok: false, error: 'Solo un administrador puede cambiar roles' };
    const usuario = this.usuarios.buscar(id);
    if (!usuario) return { ok: false, error: 'El usuario no existe' };
    if (usuario.rol === 'ADMIN' && rol !== 'ADMIN' && this.totalAdmins() <= 1) {
      return { ok: false, error: 'No se puede quitar al único administrador del sistema' };
    }
    this.usuarios.actualizar(id, { rol });
    return { ok: true };
  }

  /** Un ADMIN elimina a cualquiera; un usuario solo su propia cuenta. */
  eliminarUsuario(id: number): Resultado {
    const yo = this.actual();
    if (!yo || (yo.rol !== 'ADMIN' && yo.id !== id)) {
      return { ok: false, error: 'No tiene permiso para eliminar esta cuenta' };
    }
    const usuario = this.usuarios.buscar(id);
    if (!usuario) return { ok: false, error: 'El usuario no existe' };
    if (usuario.rol === 'ADMIN' && this.totalAdmins() <= 1) {
      return { ok: false, error: 'No se puede eliminar al único administrador del sistema' };
    }
    this.usuarios.eliminarDonde((u) => u.id === id);
    if (yo.id === id) this.logout();
    return { ok: true };
  }

  private totalAdmins(): number {
    return this.usuarios.valor.filter((u) => u.rol === 'ADMIN').length;
  }

  private sinClave(u: Usuario): UsuarioSesion {
    const { password: _clave, ...resto } = u;
    return resto;
  }

  private guardarSesion(id: number | null): void {
    this.sesionId.set(id);
    try {
      if (id === null) localStorage.removeItem(CLAVE_SESION);
      else localStorage.setItem(CLAVE_SESION, String(id));
    } catch {
      // Sin almacenamiento: la sesión dura hasta recargar.
    }
  }

  private leerSesion(): number | null {
    try {
      const id = Number(localStorage.getItem(CLAVE_SESION));
      return Number.isInteger(id) && id > 0 ? id : null;
    } catch {
      return null;
    }
  }
}
