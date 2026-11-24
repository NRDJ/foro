import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/** Exige sesión iniciada; si no hay, envía al login y luego vuelve a la página pedida. */
export const conSesion: CanActivateFn = (_ruta, estado) => {
  const auth = inject(AuthService);
  return auth.actual() ? true : inject(Router).createUrlTree(['/login'], { queryParams: { volver: estado.url } });
};

/** Exige rol ADMIN. */
export const soloAdmin: CanActivateFn = (_ruta, estado) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.actual()) return router.createUrlTree(['/login'], { queryParams: { volver: estado.url } });
  return auth.esAdmin() ? true : router.createUrlTree(['/']);
};

/** Login, registro y recuperación no tienen sentido con la sesión iniciada. */
export const soloInvitado: CanActivateFn = () => (inject(AuthService).actual() ? inject(Router).createUrlTree(['/']) : true);
