import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ErrorCampo } from '../../components/error-campo';
import { RequisitosPassword } from '../../components/requisitos-password';
import { AuthService } from '../../services/auth.service';
import {
  PASSWORD_MAX, PASSWORD_MIN, PATRON_EMAIL, PATRON_NOMBRE, PATRON_USERNAME, coinciden, passwordSegura, sinSoloEspacios,
} from '../../validators/validadores';

@Component({
  selector: 'app-registro',
  imports: [ReactiveFormsModule, RouterLink, ErrorCampo, RequisitosPassword],
  templateUrl: './registro.html',
})
export class Registro {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly error = signal('');
  protected readonly form = inject(FormBuilder).nonNullable.group(
    {
      nombre: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100), Validators.pattern(PATRON_NOMBRE), sinSoloEspacios()]],
      username: ['', [Validators.required, Validators.pattern(PATRON_USERNAME),
        (c: AbstractControl) => (this.auth.existeUsername(c.value ?? '') ? { usernameTomado: true } : null)]],
      email: ['', [Validators.required, Validators.maxLength(120), Validators.pattern(PATRON_EMAIL),
        (c: AbstractControl) => (this.auth.existeEmail(c.value ?? '') ? { emailTomado: true } : null)]],
      password: ['', [Validators.required, Validators.minLength(PASSWORD_MIN), Validators.maxLength(PASSWORD_MAX), passwordSegura()]],
      confirmar: ['', Validators.required],
      terminos: [false, Validators.requiredTrue],
    },
    { validators: coinciden('password', 'confirmar') },
  );

  registrar(): void {
    this.error.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { nombre, username, email, password } = this.form.getRawValue();
    const r = this.auth.registrar({ nombre, username, email, password });
    if (!r.ok) {
      this.error.set(r.error);
      return;
    }
    this.auth.login(username, password);
    this.router.navigate(['/'], { queryParams: { bienvenida: 1 } });
  }
}
