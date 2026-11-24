import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { AbstractControl } from '@angular/forms';
import { MENSAJES } from '../validators/validadores';

/** Muestra el primer error de un campo cuando el usuario ya lo tocó. */
@Component({
  selector: 'app-error-campo',
  // Eager: se revisa junto con el formulario padre, aunque el control sea el mismo objeto.
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    @let c = control();
    @if (c && c.invalid && (c.touched || c.dirty)) {
      <div class="invalid-feedback d-block">{{ mensaje(c) }}</div>
    }
  `,
})
export class ErrorCampo {
  readonly control = input<AbstractControl | null>(null);
  /** Mensaje para el error "pattern", que depende de cada campo. */
  readonly patron = input('El formato no es válido');

  mensaje(c: AbstractControl): string {
    const errores = c.errors ?? {};
    const clave = Object.keys(errores)[0];
    if (clave === 'pattern') return this.patron();
    return MENSAJES[clave]?.(errores[clave]) ?? 'El valor no es válido';
  }
}
