import { DatePipe } from '@angular/common';
import { Component, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Tema } from '../models/modelos';
import { ForoFacade } from '../services/foro.facade';

@Component({
  selector: 'app-tarjeta-tema',
  imports: [RouterLink, DatePipe],
  template: `
    @let t = tema();
    <article class="card tarjeta-tema h-100">
      <div class="card-body d-flex flex-column">
        <div class="d-flex flex-wrap gap-2 mb-2">
          <a class="badge etiqueta-categoria text-decoration-none" [routerLink]="['/categorias', t.categoriaId]">
            {{ foro.nombreCategoria(t.categoriaId) }}
          </a>
          @if (t.estado === 'BANEADO') {
            <span class="badge text-bg-danger"><i class="bi bi-slash-circle"></i> Baneado</span>
          }
        </div>
        <h3 class="h5 card-title">
          <a class="stretched-link enlace-tema" [routerLink]="['/temas', t.id]">{{ t.titulo }}</a>
        </h3>
        <p class="card-text text-secondary resumen">{{ t.contenido }}</p>
        <div class="mt-auto d-flex flex-wrap justify-content-between gap-2 small text-secondary">
          <span><i class="bi bi-person-circle"></i> {{ t.autorUsername }}</span>
          <span><i class="bi bi-chat-dots"></i> {{ foro.totalComentarios(t.id) }}</span>
          <span><i class="bi bi-calendar3"></i> {{ t.fechaCreacion | date: 'dd-MM-yyyy' }}</span>
        </div>
      </div>
    </article>
  `,
})
export class TarjetaTema {
  readonly tema = input.required<Tema>();
  protected readonly foro = inject(ForoFacade);
}
