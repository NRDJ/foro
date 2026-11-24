import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-barra-navegacion',
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav class="navbar navbar-expand-lg navbar-dark barra sticky-top" aria-label="Navegación principal">
      <div class="container">
        <a class="navbar-brand d-flex align-items-center gap-2" routerLink="/" (click)="cerrar()">
          <img src="logo.svg" alt="" width="36" height="36" />
          <span class="marca">Ágora</span>
        </a>
        <button class="navbar-toggler" type="button" (click)="abierto.set(!abierto())"
                [attr.aria-expanded]="abierto()" aria-controls="menu-principal" aria-label="Abrir menú">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div id="menu-principal" class="collapse navbar-collapse" [class.show]="abierto()">
          <ul class="navbar-nav me-auto">
            <li class="nav-item">
              <a class="nav-link" routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }" (click)="cerrar()">
                <i class="bi bi-house"></i> Inicio
              </a>
            </li>
            @if (auth.actual()) {
              <li class="nav-item">
                <a class="nav-link" routerLink="/temas/nuevo" routerLinkActive="active" (click)="cerrar()">
                  <i class="bi bi-plus-circle"></i> Publicar
                </a>
              </li>
            }
            @if (auth.esAdmin()) {
              <li class="nav-item">
                <a class="nav-link" routerLink="/admin/moderacion" routerLinkActive="active" (click)="cerrar()">
                  <i class="bi bi-shield-check"></i> Moderación
                </a>
              </li>
              <li class="nav-item">
                <a class="nav-link" routerLink="/admin/usuarios" routerLinkActive="active" (click)="cerrar()">
                  <i class="bi bi-people"></i> Usuarios
                </a>
              </li>
            }
          </ul>
          <ul class="navbar-nav">
            @if (auth.actual(); as u) {
              <li class="nav-item dropdown">
                <!-- El desplegable usa el JavaScript de Bootstrap (data-bs-toggle) -->
                <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                  <i class="bi bi-person-circle"></i> {{ u.nombre }}
                  <span class="badge ms-1" [class.text-bg-warning]="u.rol === 'ADMIN'" [class.text-bg-light]="u.rol !== 'ADMIN'">{{ u.rol }}</span>
                </a>
                <ul class="dropdown-menu dropdown-menu-end">
                  <li><a class="dropdown-item" routerLink="/perfil" (click)="cerrar()"><i class="bi bi-person-gear"></i> Mi perfil</a></li>
                  <li><hr class="dropdown-divider" /></li>
                  <li><button class="dropdown-item" type="button" (click)="salir()"><i class="bi bi-box-arrow-right"></i> Cerrar sesión</button></li>
                </ul>
              </li>
            } @else {
              <li class="nav-item">
                <a class="nav-link" routerLink="/login" routerLinkActive="active" (click)="cerrar()">Iniciar sesión</a>
              </li>
              <li class="nav-item">
                <a class="btn btn-acento ms-lg-2 mt-2 mt-lg-0" routerLink="/registro" (click)="cerrar()">Crear cuenta</a>
              </li>
            }
          </ul>
        </div>
      </div>
    </nav>
  `,
})
export class BarraNavegacion {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  protected readonly abierto = signal(false);

  cerrar(): void {
    this.abierto.set(false);
  }

  salir(): void {
    this.auth.logout();
    this.cerrar();
    this.router.navigateByUrl('/login');
  }
}
