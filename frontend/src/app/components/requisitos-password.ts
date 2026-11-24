import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { AbstractControl } from '@angular/forms';
import { PASSWORD_MAX, PASSWORD_MIN } from '../validators/validadores';

/** Lista en vivo de las reglas de seguridad de la contraseña. */
@Component({
  selector: 'app-requisitos-password',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <ul class="requisitos list-unstyled small mt-2 mb-0" aria-label="Requisitos de la contraseña">
      @for (r of reglas(); track r.texto) {
        <li [class.cumple]="r.cumple">
          <i class="bi" [class.bi-check-circle-fill]="r.cumple" [class.bi-circle]="!r.cumple" aria-hidden="true"></i>
          {{ r.texto }}
        </li>
      }
    </ul>
  `,
})
export class RequisitosPassword {
  readonly control = input.required<AbstractControl>();

  reglas() {
    const v: string = this.control().value ?? '';
    return [
      { texto: `Entre ${PASSWORD_MIN} y ${PASSWORD_MAX} caracteres`, cumple: v.length >= PASSWORD_MIN && v.length <= PASSWORD_MAX },
      { texto: 'Una letra mayúscula', cumple: /[A-ZÁÉÍÓÚÑ]/.test(v) },
      { texto: 'Una letra minúscula', cumple: /[a-záéíóúñ]/.test(v) },
      { texto: 'Un número', cumple: /\d/.test(v) },
      { texto: 'Un carácter especial', cumple: /[^A-Za-z0-9ÁÉÍÓÚáéíóúÑñ\s]/.test(v) },
      { texto: 'Sin espacios', cumple: v.length > 0 && !/\s/.test(v) },
    ];
  }
}
