import { Injectable } from '@angular/core';
import { BANEOS_INICIALES } from '../data/datos-iniciales';
import { Baneo, TipoContenido } from '../models/modelos';
import { Coleccion } from './coleccion';

/** Registro de baneos (equivale a ms-moderacion). Singleton por providedIn 'root'. */
@Injectable({ providedIn: 'root' })
export class ModeracionService {
  readonly baneos = new Coleccion<Baneo>('agora.baneos', BANEOS_INICIALES);

  buscarPorContenido(tipo: TipoContenido, contenidoId: number): Baneo | undefined {
    return this.baneos.valor.find((b) => b.tipo === tipo && b.contenidoId === contenidoId);
  }

  registrar(tipo: TipoContenido, contenidoId: number, motivo: string, moderador: string): void {
    this.baneos.agregar({
      id: this.baneos.siguienteId(),
      tipo,
      contenidoId,
      motivo: motivo.trim(),
      moderadorUsername: moderador,
      fechaCreacion: new Date().toISOString(),
    });
  }

  actualizarMotivo(id: number, motivo: string, moderador: string): void {
    this.baneos.actualizar(id, { motivo: motivo.trim(), moderadorUsername: moderador });
  }

  eliminar(id: number): void {
    this.baneos.eliminarDonde((b) => b.id === id);
  }

  /** Al eliminar un tema o comentario, su baneo deja de tener sentido. */
  eliminarDeContenido(tipo: TipoContenido, ids: number[]): void {
    this.baneos.eliminarDonde((b) => b.tipo === tipo && ids.includes(b.contenidoId));
  }
}
