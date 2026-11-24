import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TarjetaTema } from '../../components/tarjeta-tema';
import { ForoFacade } from '../../services/foro.facade';

const POR_PAGINA = 6;

@Component({
  selector: 'app-inicio',
  imports: [FormsModule, RouterLink, TarjetaTema],
  templateUrl: './inicio.html',
})
export class Inicio {
  protected readonly foro = inject(ForoFacade);
  protected readonly bienvenida = inject(ActivatedRoute).snapshot.queryParamMap.has('bienvenida');

  protected readonly texto = signal('');
  protected readonly categoriaId = signal<number | null>(null);
  protected readonly mostrados = signal(POR_PAGINA);

  protected readonly resultados = computed(() =>
    this.foro.buscarTemas({ texto: this.texto(), categoriaId: this.categoriaId() }),
  );
  protected readonly visibles = computed(() => this.resultados().slice(0, this.mostrados()));

  filtrar(texto: string, categoriaId: number | null): void {
    this.texto.set(texto.slice(0, 100));
    this.categoriaId.set(categoriaId);
    this.mostrados.set(POR_PAGINA);
  }

  verMas(): void {
    this.mostrados.update((n) => n + POR_PAGINA);
  }
}
