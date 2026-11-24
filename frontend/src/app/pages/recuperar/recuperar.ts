import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ErrorCampo } from '../../components/error-campo';
import { RequisitosPassword } from '../../components/requisitos-password';
import { AuthService } from '../../services/auth.service';
import { PASSWORD_MAX, PASSWORD_MIN, PATRON_EMAIL, coinciden, passwordSegura } from '../../validators/validadores';

/** Recuperación en dos pasos. Sin servidor de correo, el código se muestra en pantalla (simulado). */
@Component({
  selector: 'app-recuperar',
  imports: [ReactiveFormsModule, RouterLink, ErrorCampo, RequisitosPassword],
  templateUrl: './recuperar.html',
})
export class Recuperar {
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder).nonNullable;

  protected readonly paso = signal<1 | 2 | 3>(1);
  protected readonly error = signal('');
  protected readonly codigoSimulado = signal('');

  protected readonly formCorreo = this.fb.group({
    email: ['', [Validators.required, Validators.maxLength(120), Validators.pattern(PATRON_EMAIL)]],
  });

  protected readonly formClave = this.fb.group(
    {
      codigo: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
      password: ['', [Validators.required, Validators.minLength(PASSWORD_MIN), Validators.maxLength(PASSWORD_MAX), passwordSegura()]],
      confirmar: ['', Validators.required],
    },
    { validators: coinciden('password', 'confirmar') },
  );

  enviarCodigo(): void {
    this.error.set('');
    if (this.formCorreo.invalid) {
      this.formCorreo.markAllAsTouched();
      return;
    }
    const r = this.auth.solicitarRecuperacion(this.formCorreo.getRawValue().email);
    if (!r.ok) {
      this.error.set(r.error);
      return;
    }
    this.codigoSimulado.set(r.codigo);
    this.paso.set(2);
  }

  cambiarClave(): void {
    this.error.set('');
    if (this.formClave.invalid) {
      this.formClave.markAllAsTouched();
      return;
    }
    const { codigo, password } = this.formClave.getRawValue();
    const r = this.auth.restablecerPassword(this.formCorreo.getRawValue().email, codigo, password);
    if (!r.ok) {
      this.error.set(r.error);
      return;
    }
    this.paso.set(3);
  }
}
