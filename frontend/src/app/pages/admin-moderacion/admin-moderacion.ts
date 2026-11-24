import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ErrorCampo } from '../../components/error-campo';
import { Baneo, TipoContenido } from '../../models/modelos';
import { ForoFacade } from '../../services/foro.facade';
import { sinSoloEspacios } from '../../validators/validadores';

/** Panel del administrador: baneos vigentes (equivale a ms-moderacion). */
@Component({
  selector: 'app-admin-moderacion',
  imports: [ReactiveFormsModule, RouterLink, DatePipe, ErrorCampo],
  templateUrl: './admin-moderacion.html',
})
export class AdminModeracion {
  protected readonly foro = inject(ForoFacade);
  protected readonly filtro = signal<TipoContenido | ''>('');
  protected readonly editando = signal<number | null>(null);
  protected readonly aviso = signal('');

  protected readonly baneos = computed(() => {
    const tipo = this.filtro();
    return this.foro.baneos()
      .filter((b) => !tipo || b.tipo === tipo)
      .sort((a, b) => b.fechaCreacion.localeCompare(a.fechaCreacion));
  });
  protected readonly totalTemas = computed(() => this.foro.baneos().filter((b) => b.tipo === 'TEMA').length);
  protected readonly totalComentarios = computed(() => this.foro.baneos().filter((b) => b.tipo === 'COMENTARIO').length);

  protected readonly formMotivo = inject(FormBuilder).nonNullable.group({
    motivo: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(255), sinSoloEspacios()]],
  });

  editar(b: Baneo): void {
    this.editando.set(b.id);
    this.formMotivo.setValue({ motivo: b.motivo });
  }

  guardar(b: Baneo): void {
    if (this.formMotivo.invalid) {
      this.formMotivo.markAllAsTouched();
      return;
    }
    const r = this.foro.actualizarMotivo(b, this.formMotivo.getRawValue().motivo);
    this.aviso.set(r.ok ? 'Motivo actualizado.' : r.error);
    this.editando.set(null);
  }

  revertir(b: Baneo): void {
    if (!confirm('¿Revertir el baneo? El contenido volverá a ser visible para todos.')) return;
    const r = this.foro.revertirBaneo(b);
    this.aviso.set(r.ok ? 'Baneo revertido: el contenido es visible otra vez.' : r.error);
  }

  restablecer(): void {
    if (confirm('¿Restablecer todos los datos de demostración? Se pierden los cambios hechos en el navegador.')) {
      this.foro.restablecerDatos();
      this.aviso.set('Datos de demostración restablecidos.');
    }
  }
}
