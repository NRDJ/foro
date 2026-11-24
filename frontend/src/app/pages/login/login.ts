import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ErrorCampo } from '../../components/error-campo';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, ErrorCampo],
  templateUrl: './login.html',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly ruta = inject(ActivatedRoute);

  protected readonly error = signal('');
  protected readonly verClave = signal(false);
  protected readonly form = inject(FormBuilder).nonNullable.group({
    usuario: ['', [Validators.required, Validators.maxLength(120)]],
    password: ['', [Validators.required, Validators.maxLength(72)]],
  });

  entrar(): void {
    this.error.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { usuario, password } = this.form.getRawValue();
    const r = this.auth.login(usuario, password);
    if (!r.ok) {
      this.error.set(r.error);
      return;
    }
    const volver = this.ruta.snapshot.queryParamMap.get('volver');
    // Solo rutas internas: evita redirigir a otro sitio.
    this.router.navigateByUrl(volver?.startsWith('/') && !volver.startsWith('//') ? volver : '/');
  }

  /** Atajo para la demostración: completa el formulario con un usuario de prueba. */
  usar(usuario: string): void {
    this.form.setValue({ usuario, password: 'Foro2026!' });
  }
}
