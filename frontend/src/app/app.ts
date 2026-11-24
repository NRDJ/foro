import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { BarraNavegacion } from './components/barra-navegacion';
import { PiePagina } from './components/pie-pagina';

@Component({
  imports: [RouterOutlet, BarraNavegacion, PiePagina],
  selector: 'app-root',
  template: `
    <app-barra-navegacion />
    <main class="container py-4 flex-grow-1">
      <router-outlet />
    </main>
    <app-pie-pagina />
  `,
  host: { class: 'd-flex flex-column min-vh-100' },
})
export class App {}
