import { Injectable } from '@angular/core';
import { CATEGORIAS, COMENTARIOS_INICIALES, TEMAS_INICIALES } from '../data/datos-iniciales';
import { Categoria, Comentario, EstadoContenido, Tema, UsuarioSesion } from '../models/modelos';
import { Coleccion } from './coleccion';

/** Temas y comentarios del foro (equivale a ms-foros). Singleton por providedIn 'root'. */
@Injectable({ providedIn: 'root' })
export class ForoService {
  readonly categorias: readonly Categoria[] = CATEGORIAS;
  readonly temas = new Coleccion<Tema>('agora.temas', TEMAS_INICIALES);
  readonly comentarios = new Coleccion<Comentario>('agora.comentarios', COMENTARIOS_INICIALES);

  categoria(id: number): Categoria | undefined {
    return this.categorias.find((c) => c.id === id);
  }

  publicarTema(datos: { titulo: string; contenido: string; categoriaId: number }, autor: UsuarioSesion): Tema {
    const ahora = new Date().toISOString();
    const tema: Tema = {
      id: this.temas.siguienteId(),
      titulo: datos.titulo.trim(),
      contenido: datos.contenido.trim(),
      categoriaId: datos.categoriaId,
      autorId: autor.id,
      autorUsername: autor.username,
      estado: 'VISIBLE',
      fechaCreacion: ahora,
      fechaActualizacion: ahora,
    };
    this.temas.agregar(tema);
    return tema;
  }

  editarTema(id: number, datos: { titulo: string; contenido: string; categoriaId: number }): void {
    this.temas.actualizar(id, {
      titulo: datos.titulo.trim(),
      contenido: datos.contenido.trim(),
      categoriaId: datos.categoriaId,
      fechaActualizacion: new Date().toISOString(),
    });
  }

  /** Igual que en Oracle (ON DELETE CASCADE): al eliminar un tema se eliminan sus comentarios. */
  eliminarTema(id: number): void {
    this.comentarios.eliminarDonde((c) => c.temaId === id);
    this.temas.eliminarDonde((t) => t.id === id);
  }

  comentar(temaId: number, contenido: string, autor: UsuarioSesion): void {
    this.comentarios.agregar({
      id: this.comentarios.siguienteId(),
      temaId,
      contenido: contenido.trim(),
      autorId: autor.id,
      autorUsername: autor.username,
      estado: 'VISIBLE',
      fechaCreacion: new Date().toISOString(),
    });
  }

  editarComentario(id: number, contenido: string): void {
    this.comentarios.actualizar(id, { contenido: contenido.trim() });
  }

  eliminarComentario(id: number): void {
    this.comentarios.eliminarDonde((c) => c.id === id);
  }

  cambiarEstadoTema(id: number, estado: EstadoContenido): void {
    this.temas.actualizar(id, { estado });
  }

  cambiarEstadoComentario(id: number, estado: EstadoContenido): void {
    this.comentarios.actualizar(id, { estado });
  }
}
