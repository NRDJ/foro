import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-no-encontrado',
  imports: [RouterLink],
  template: `
    <div class="text-center py-5">
      <p class="display-1 fw-bold texto-primario">404</p>
      <h1 class="h4">Esta página no existe</h1>
      <a routerLink="/" class="btn btn-primary mt-3">Volver al inicio</a>
    </div>
  `,
})
export class NoEncontrado {}
