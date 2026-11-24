import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-pie-pagina',
  imports: [RouterLink],
  template: `
    <footer class="pie mt-auto">
      <div class="container py-4">
        <div class="row g-3 align-items-center">
          <div class="col-12 col-md-6 d-flex align-items-center gap-2">
            <img src="logo.svg" alt="" width="28" height="28" />
            <span><strong>Ágora</strong> · Foros en línea para conversar con respeto</span>
          </div>
          <div class="col-12 col-md-6 text-md-end small">
            <a routerLink="/" class="me-3">Inicio</a>
            <a routerLink="/registro">Únete</a>
          </div>
        </div>
      </div>
    </footer>
  `,
})
export class PiePagina {}
