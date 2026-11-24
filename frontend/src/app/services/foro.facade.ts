import { Injectable, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Baneo, Comentario, Resultado, Tema, TipoContenido } from '../models/modelos';
import { AuthService } from './auth.service';
import { ForoService } from './foro.service';
import { ModeracionService } from './moderacion.service';

export interface FiltroTemas {
  texto?: string;
  categoriaId?: number | null;
  autorId?: number | null;
}

/**
 * Patrón Facade: los componentes solo conocen esta clase. Por dentro coordina AuthService
 * (quién es el usuario y su rol), ForoService (temas y comentarios) y ModeracionService
 * (baneos), y aplica las mismas reglas de permisos que los microservicios.
 */
@Injectable({ providedIn: 'root' })
export class ForoFacade {
  private readonly auth = inject(AuthService);
  private readonly foro = inject(ForoService);
  private readonly moderacion = inject(ModeracionService);

  readonly categorias = this.foro.categorias;
  readonly usuario = this.auth.actual;
  readonly esAdmin = this.auth.esAdmin;

  // Observer: cada lista se convierte en signal y la vista se actualiza al cambiar.
  private readonly temas = toSignal(this.foro.temas.cambios$, { requireSync: true });
  private readonly comentarios = toSignal(this.foro.comentarios.cambios$, { requireSync: true });
  readonly baneos = toSignal(this.moderacion.baneos.cambios$, { requireSync: true });

  // ---- Consultas ----

  /** Búsqueda pública: solo temas visibles, más recientes primero. */
  buscarTemas(filtro: FiltroTemas = {}): Tema[] {
    const texto = (filtro.texto ?? '').trim().toLowerCase();
    return this.temas()
      .filter((t) => t.estado === 'VISIBLE')
      .filter((t) => !filtro.categoriaId || t.categoriaId === filtro.categoriaId)
      .filter((t) => !filtro.autorId || t.autorId === filtro.autorId)
      .filter((t) => !texto || t.titulo.toLowerCase().includes(texto) || t.contenido.toLowerCase().includes(texto))
      .sort((a, b) => b.fechaCreacion.localeCompare(a.fechaCreacion) || b.id - a.id);
  }

  /** Un tema baneado no existe para el resto; el administrador lo sigue viendo. */
  tema(id: number): Tema | undefined {
    const tema = this.temas().find((t) => t.id === id);
    return tema && (tema.estado === 'VISIBLE' || this.esAdmin()) ? tema : undefined;
  }

  comentariosDe(temaId: number): Comentario[] {
    return this.comentarios()
      .filter((c) => c.temaId === temaId && (c.estado === 'VISIBLE' || this.esAdmin()))
      .sort((a, b) => a.fechaCreacion.localeCompare(b.fechaCreacion) || a.id - b.id);
  }

  totalComentarios(temaId: number): number {
    return this.comentarios().filter((c) => c.temaId === temaId && c.estado === 'VISIBLE').length;
  }

  totalTemas(categoriaId: number): number {
    return this.temas().filter((t) => t.categoriaId === categoriaId && t.estado === 'VISIBLE').length;
  }

  nombreCategoria(id: number): string {
    return this.foro.categoria(id)?.nombre ?? 'Sin categoría';
  }

  /** El autor o un administrador pueden modificar y eliminar. */
  puedeGestionar(autorId: number): boolean {
    const yo = this.usuario();
    return !!yo && (yo.rol === 'ADMIN' || yo.id === autorId);
  }

  // ---- Publicar y comentar ----

  publicarTema(datos: { titulo: string; contenido: string; categoriaId: number }): Resultado & { id?: number } {
    const yo = this.usuario();
    if (!yo) return { ok: false, error: 'Debe iniciar sesión para publicar' };
    if (!this.foro.categoria(datos.categoriaId)) return { ok: false, error: 'La categoría no existe' };
    return { ok: true, id: this.foro.publicarTema(datos, yo).id };
  }

  editarTema(id: number, datos: { titulo: string; contenido: string; categoriaId: number }): Resultado {
    const tema = this.tema(id);
    if (!tema) return { ok: false, error: 'El tema no existe' };
    if (!this.puedeGestionar(tema.autorId)) return { ok: false, error: 'Solo el autor o un administrador puede editarlo' };
    this.foro.editarTema(id, datos);
    return { ok: true };
  }

  eliminarTema(id: number): Resultado {
    const tema = this.tema(id);
    if (!tema) return { ok: false, error: 'El tema no existe' };
    if (!this.puedeGestionar(tema.autorId)) return { ok: false, error: 'Solo el autor o un administrador puede eliminarlo' };
    const idsComentarios = this.comentarios().filter((c) => c.temaId === id).map((c) => c.id);
    this.moderacion.eliminarDeContenido('TEMA', [id]);
    this.moderacion.eliminarDeContenido('COMENTARIO', idsComentarios);
    this.foro.eliminarTema(id);
    return { ok: true };
  }

  comentar(temaId: number, contenido: string): Resultado {
    const yo = this.usuario();
    if (!yo) return { ok: false, error: 'Debe iniciar sesión para comentar' };
    if (!this.tema(temaId)) return { ok: false, error: 'El tema no existe' };
    this.foro.comentar(temaId, contenido, yo);
    return { ok: true };
  }

  editarComentario(comentario: Comentario, contenido: string): Resultado {
    if (!this.puedeGestionar(comentario.autorId)) return { ok: false, error: 'Solo el autor o un administrador puede editarlo' };
    this.foro.editarComentario(comentario.id, contenido);
    return { ok: true };
  }

  eliminarComentario(comentario: Comentario): Resultado {
    if (!this.puedeGestionar(comentario.autorId)) return { ok: false, error: 'Solo el autor o un administrador puede eliminarlo' };
    this.moderacion.eliminarDeContenido('COMENTARIO', [comentario.id]);
    this.foro.eliminarComentario(comentario.id);
    return { ok: true };
  }

  // ---- Moderación (solo ADMIN) ----

  banear(tipo: TipoContenido, contenidoId: number, motivo: string): Resultado {
    const yo = this.usuario();
    if (!yo || yo.rol !== 'ADMIN') return { ok: false, error: 'Solo un administrador puede banear contenido' };
    if (this.moderacion.buscarPorContenido(tipo, contenidoId)) return { ok: false, error: 'Ese contenido ya está baneado' };
    const existe = tipo === 'TEMA'
      ? this.temas().some((t) => t.id === contenidoId)
      : this.comentarios().some((c) => c.id === contenidoId);
    if (!existe) return { ok: false, error: 'El contenido no existe' };

    if (tipo === 'TEMA') this.foro.cambiarEstadoTema(contenidoId, 'BANEADO');
    else this.foro.cambiarEstadoComentario(contenidoId, 'BANEADO');
    this.moderacion.registrar(tipo, contenidoId, motivo, yo.username);
    return { ok: true };
  }

  revertirBaneo(baneo: Baneo): Resultado {
    if (!this.esAdmin()) return { ok: false, error: 'Solo un administrador puede revertir un baneo' };
    if (baneo.tipo === 'TEMA') this.foro.cambiarEstadoTema(baneo.contenidoId, 'VISIBLE');
    else this.foro.cambiarEstadoComentario(baneo.contenidoId, 'VISIBLE');
    this.moderacion.eliminar(baneo.id);
    return { ok: true };
  }

  actualizarMotivo(baneo: Baneo, motivo: string): Resultado {
    const yo = this.usuario();
    if (!yo || yo.rol !== 'ADMIN') return { ok: false, error: 'Solo un administrador puede editar un baneo' };
    this.moderacion.actualizarMotivo(baneo.id, motivo, yo.username);
    return { ok: true };
  }

  baneoDe(tipo: TipoContenido, contenidoId: number): Baneo | undefined {
    return this.baneos().find((b) => b.tipo === tipo && b.contenidoId === contenidoId);
  }

  /** Texto corto para identificar el contenido baneado en la tabla de moderación. */
  describir(baneo: Baneo): { texto: string; temaId?: number } {
    if (baneo.tipo === 'TEMA') {
      const t = this.temas().find((x) => x.id === baneo.contenidoId);
      return t ? { texto: t.titulo, temaId: t.id } : { texto: '(tema eliminado)' };
    }
    const c = this.comentarios().find((x) => x.id === baneo.contenidoId);
    return c ? { texto: c.contenido, temaId: c.temaId } : { texto: '(comentario eliminado)' };
  }

  restablecerDatos(): void {
    this.auth.usuarios.restablecer();
    this.foro.temas.restablecer();
    this.foro.comentarios.restablecer();
    this.moderacion.baneos.restablecer();
  }
}
