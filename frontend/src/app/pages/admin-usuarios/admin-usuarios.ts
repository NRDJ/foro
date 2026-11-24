import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ErrorCampo } from '../../components/error-campo';
import { RequisitosPassword } from '../../components/requisitos-password';
import { Rol, Usuario } from '../../models/modelos';
import { AuthService } from '../../services/auth.service';
import {
  PASSWORD_MAX, PASSWORD_MIN, PATRON_EMAIL, PATRON_NOMBRE, PATRON_USERNAME, passwordSegura, sinSoloEspacios,
} from '../../validators/validadores';

/** Gestión de usuarios para el rol ADMIN (equivale a los endpoints de administración de ms-usuarios). */
@Component({
  selector: 'app-admin-usuarios',
  imports: [ReactiveFormsModule, DatePipe, ErrorCampo, RequisitosPassword],
  templateUrl: './admin-usuarios.html',
})
export class AdminUsuarios {
  protected readonly auth = inject(AuthService);
  private readonly usuarios = toSignal(this.auth.usuarios.cambios$, { requireSync: true });

  protected readonly texto = signal('');
  protected readonly rol = signal<Rol | ''>('');
  protected readonly creando = signal(false);
  protected readonly aviso = signal<{ tipo: 'success' | 'danger'; texto: string } | null>(null);

  protected readonly lista = computed(() => {
    const t = this.texto().trim().toLowerCase();
    return this.usuarios()
      .filter((u) => !this.rol() || u.rol === this.rol())
      .filter((u) => !t || u.username.includes(t) || u.email.includes(t) || u.nombre.toLowerCase().includes(t))
      .sort((a, b) => a.username.localeCompare(b.username));
  });

  protected readonly form = inject(FormBuilder).nonNullable.group({
    nombre: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100), Validators.pattern(PATRON_NOMBRE), sinSoloEspacios()]],
    username: ['', [Validators.required, Validators.pattern(PATRON_USERNAME),
      (c: AbstractControl) => (this.auth.existeUsername(c.value ?? '') ? { usernameTomado: true } : null)]],
    email: ['', [Validators.required, Validators.maxLength(120), Validators.pattern(PATRON_EMAIL),
      (c: AbstractControl) => (this.auth.existeEmail(c.value ?? '') ? { emailTomado: true } : null)]],
    password: ['', [Validators.required, Validators.minLength(PASSWORD_MIN), Validators.maxLength(PASSWORD_MAX), passwordSegura()]],
    rol: ['USUARIO' as Rol, Validators.required],
  });

  crear(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { rol, ...datos } = this.form.getRawValue();
    const r = this.auth.registrar(datos, rol);
    this.aviso.set(r.ok ? { tipo: 'success', texto: `Usuario ${datos.username.toLowerCase()} creado con rol ${rol}.` } : { tipo: 'danger', texto: r.error });
    if (r.ok) {
      this.form.reset();
      this.creando.set(false);
    }
  }

  cambiarRol(u: Usuario, rol: string, selector: HTMLSelectElement): void {
    const r = this.auth.cambiarRol(u.id, rol as Rol);
    this.aviso.set(r.ok ? { tipo: 'success', texto: `${u.username} ahora es ${rol}.` } : { tipo: 'danger', texto: r.error });
    if (!r.ok) selector.value = u.rol;
  }

  eliminar(u: Usuario): void {
    if (!confirm(`¿Eliminar la cuenta de ${u.username}?`)) return;
    const r = this.auth.eliminarUsuario(u.id);
    this.aviso.set(r.ok ? { tipo: 'success', texto: `Cuenta de ${u.username} eliminada.` } : { tipo: 'danger', texto: r.error });
  }
}
