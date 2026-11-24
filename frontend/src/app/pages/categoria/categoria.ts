import { Component, computed, inject, input, numberAttribute, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TarjetaTema } from '../../components/tarjeta-tema';
import { ForoFacade } from '../../services/foro.facade';

@Component({
  selector: 'app-categoria',
  imports: [RouterLink, TarjetaTema],
  template: `
    @if (categoria(); as c) {
      <nav aria-label="Ruta" class="mb-3">
        <ol class="breadcrumb">
          <li class="breadcrumb-item"><a routerLink="/">Inicio</a></li>
          <li class="breadcrumb-item active" aria-current="page">{{ c.nombre }}</li>
        </ol>
      </nav>
      <section class="cabecera-categoria rounded-4 mb-4 overflow-hidden">
        <div class="row g-0 align-items-center">
          <div class="col-12 col-md-4"><img [src]="c.imagen" class="img-fluid w-100" [alt]="'Ilustración de ' + c.nombre" /></div>
          <div class="col-12 col-md-8 p-4">
            <h1 class="h2"><i class="bi" [class]="c.icono"></i> {{ c.nombre }}</h1>
            <p class="mb-3">{{ c.descripcion }}</p>
            <span class="badge etiqueta-categoria">{{ temas().length }} temas</span>
            @if (foro.usuario()) {
              <a class="btn btn-primary btn-sm ms-2" routerLink="/temas/nuevo" [queryParams]="{ categoria: c.id }">
                <i class="bi bi-plus-lg"></i> Nuevo tema aquí
              </a>
            }
          </div>
        </div>
      </section>

      <div class="mb-3 col-12 col-md-6 col-lg-4 px-0">
        <label for="filtro" class="visually-hidden">Buscar en {{ c.nombre }}</label>
        <input #f id="filtro" class="form-control" type="search" maxlength="100" [placeholder]="'Buscar en ' + c.nombre"
               (input)="texto.set(f.value)" />
      </div>
      <div class="row g-3">
        @for (t of temas(); track t.id) {
          <div class="col-12 col-md-6 col-xl-4"><app-tarjeta-tema [tema]="t" /></div>
        } @empty {
          <p class="text-secondary">No hay temas en esta categoría todavía.</p>
        }
      </div>
    } @else {
      <div class="text-center py-5">
        <h1 class="h4">La categoría no existe</h1>
        <a routerLink="/" class="btn btn-primary mt-2">Volver al inicio</a>
      </div>
    }
  `,
})
export class PaginaCategoria {
  protected readonly foro = inject(ForoFacade);
  /** Parámetro :id de la ruta (withComponentInputBinding). */
  readonly id = input(0, { transform: numberAttribute });
  protected readonly texto = signal('');

  protected readonly categoria = computed(() => this.foro.categorias.find((c) => c.id === this.id()));
  protected readonly temas = computed(() => this.foro.buscarTemas({ categoriaId: this.id(), texto: this.texto() }));
}
