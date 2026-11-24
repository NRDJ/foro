import { DatePipe, NgTemplateOutlet } from '@angular/common';
import { Component, computed, inject, input, numberAttribute, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ErrorCampo } from '../../components/error-campo';
import { Comentario, TipoContenido } from '../../models/modelos';
import { ForoFacade } from '../../services/foro.facade';
import { sinSoloEspacios } from '../../validators/validadores';

@Component({
  selector: 'app-tema-detalle',
  imports: [ReactiveFormsModule, RouterLink, DatePipe, NgTemplateOutlet, ErrorCampo],
  templateUrl: './tema-detalle.html',
})
export class TemaDetalle {
  protected readonly foro = inject(ForoFacade);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder).nonNullable;

  /** Parámetro :id de la ruta (withComponentInputBinding). */
  readonly id = input(0, { transform: numberAttribute });

  protected readonly tema = computed(() => this.foro.tema(this.id()));
  protected readonly comentarios = computed(() => this.foro.comentariosDe(this.id()));
  protected readonly aviso = signal<{ tipo: 'success' | 'danger'; texto: string } | null>(null);

  /** Comentario que se está editando, y contenido o comentario que se está baneando. */
  protected readonly editando = signal<number | null>(null);
  protected readonly baneando = signal<{ tipo: TipoContenido; id: number } | null>(null);

  protected readonly formComentario = this.fb.group({
    contenido: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(1000), sinSoloEspacios()]],
  });
  protected readonly formEdicion = this.fb.group({
    contenido: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(1000), sinSoloEspacios()]],
  });
  protected readonly formBaneo = this.fb.group({
    motivo: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(255), sinSoloEspacios()]],
  });

  comentar(): void {
    if (this.formComentario.invalid) {
      this.formComentario.markAllAsTouched();
      return;
    }
    this.mostrar(this.foro.comentar(this.id(), this.formComentario.getRawValue().contenido), 'Comentario publicado.');
    this.formComentario.reset();
  }

  editar(c: Comentario): void {
    this.editando.set(c.id);
    this.formEdicion.setValue({ contenido: c.contenido });
  }

  guardarEdicion(c: Comentario): void {
    if (this.formEdicion.invalid) {
      this.formEdicion.markAllAsTouched();
      return;
    }
    this.mostrar(this.foro.editarComentario(c, this.formEdicion.getRawValue().contenido), 'Comentario actualizado.');
    this.editando.set(null);
  }

  eliminarComentario(c: Comentario): void {
    if (confirm('¿Eliminar este comentario?')) {
      this.mostrar(this.foro.eliminarComentario(c), 'Comentario eliminado.');
    }
  }

  eliminarTema(): void {
    if (!confirm('¿Eliminar este tema y todos sus comentarios?')) return;
    const r = this.foro.eliminarTema(this.id());
    if (r.ok) this.router.navigateByUrl('/');
    else this.mostrar(r, '');
  }

  abrirBaneo(tipo: TipoContenido, id: number): void {
    this.baneando.set({ tipo, id });
    this.formBaneo.reset();
  }

  confirmarBaneo(): void {
    const objetivo = this.baneando();
    if (!objetivo) return;
    if (this.formBaneo.invalid) {
      this.formBaneo.markAllAsTouched();
      return;
    }
    const texto = objetivo.tipo === 'TEMA' ? 'Tema baneado: ya no lo ven los demás usuarios.' : 'Comentario baneado.';
    this.mostrar(this.foro.banear(objetivo.tipo, objetivo.id, this.formBaneo.getRawValue().motivo), texto);
    this.baneando.set(null);
  }

  restaurar(tipo: TipoContenido, id: number): void {
    const baneo = this.foro.baneoDe(tipo, id);
    if (baneo) this.mostrar(this.foro.revertirBaneo(baneo), 'Contenido restaurado: vuelve a ser visible.');
  }

  private mostrar(r: { ok: true } | { ok: false; error: string }, exito: string): void {
    this.aviso.set(r.ok ? { tipo: 'success', texto: exito } : { tipo: 'danger', texto: r.error });
  }
}
