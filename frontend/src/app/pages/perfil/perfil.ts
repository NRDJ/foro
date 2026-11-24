import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ErrorCampo } from '../../components/error-campo';
import { RequisitosPassword } from '../../components/requisitos-password';
import { TarjetaTema } from '../../components/tarjeta-tema';
import { AuthService } from '../../services/auth.service';
import { ForoFacade } from '../../services/foro.facade';
import {
  PASSWORD_MAX, PASSWORD_MIN, PATRON_EMAIL, PATRON_NOMBRE, coinciden, passwordSegura, sinSoloEspacios,
} from '../../validators/validadores';

@Component({
  selector: 'app-perfil',
  imports: [ReactiveFormsModule, RouterLink, DatePipe, ErrorCampo, RequisitosPassword, TarjetaTema],
  templateUrl: './perfil.html',
})
export class Perfil {
  private readonly auth = inject(AuthService);
  private readonly foro = inject(ForoFacade);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder).nonNullable;

  protected readonly usuario = this.auth.actual;
  protected readonly misTemas = computed(() => {
    const yo = this.usuario();
    return yo ? this.foro.buscarTemas({ autorId: yo.id }) : [];
  });
  protected readonly avisoDatos = signal<{ tipo: 'success' | 'danger'; texto: string } | null>(null);
  protected readonly avisoClave = signal<{ tipo: 'success' | 'danger'; texto: string } | null>(null);

  protected readonly formDatos = this.fb.group({
    nombre: [this.usuario()?.nombre ?? '', [Validators.required, Validators.minLength(3), Validators.maxLength(100), Validators.pattern(PATRON_NOMBRE), sinSoloEspacios()]],
    email: [this.usuario()?.email ?? '', [Validators.required, Validators.maxLength(120), Validators.pattern(PATRON_EMAIL),
      (c: AbstractControl) => (this.auth.existeEmail(c.value ?? '', this.usuario()?.id) ? { emailTomado: true } : null)]],
  });

  protected readonly formClave = this.fb.group(
    {
      actual: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(PASSWORD_MIN), Validators.maxLength(PASSWORD_MAX), passwordSegura()]],
      confirmar: ['', Validators.required],
    },
    { validators: coinciden('password', 'confirmar') },
  );

  guardarDatos(): void {
    this.avisoDatos.set(null);
    if (this.formDatos.invalid) {
      this.formDatos.markAllAsTouched();
      return;
    }
    const r = this.auth.actualizarPerfil(this.formDatos.getRawValue());
    this.avisoDatos.set(r.ok ? { tipo: 'success', texto: 'Tus datos se actualizaron.' } : { tipo: 'danger', texto: r.error });
    if (r.ok) this.formDatos.markAsPristine();
  }

  cambiarClave(): void {
    this.avisoClave.set(null);
    if (this.formClave.invalid) {
      this.formClave.markAllAsTouched();
      return;
    }
    const { actual, password } = this.formClave.getRawValue();
    const r = this.auth.cambiarPassword(actual, password);
    this.avisoClave.set(r.ok ? { tipo: 'success', texto: 'Tu contraseña se cambió.' } : { tipo: 'danger', texto: r.error });
    if (r.ok) this.formClave.reset();
  }

  eliminarCuenta(): void {
    const yo = this.usuario();
    if (!yo || !confirm('¿Eliminar tu cuenta? Tus publicaciones se conservarán con tu nombre de usuario.')) return;
    const r = this.auth.eliminarUsuario(yo.id);
    if (!r.ok) {
      this.avisoDatos.set({ tipo: 'danger', texto: r.error });
      return;
    }
    this.router.navigateByUrl('/');
  }
}
