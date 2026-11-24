import { Component, OnInit, inject, input, numberAttribute, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ErrorCampo } from '../../components/error-campo';
import { ForoFacade } from '../../services/foro.facade';
import { sinSoloEspacios } from '../../validators/validadores';

/** Publicar un tema nuevo (/temas/nuevo) o editar uno existente (/temas/:id/editar). */
@Component({
  selector: 'app-tema-form',
  imports: [ReactiveFormsModule, RouterLink, ErrorCampo],
  templateUrl: './tema-form.html',
})
export class TemaForm implements OnInit {
  protected readonly foro = inject(ForoFacade);
  private readonly router = inject(Router);
  private readonly ruta = inject(ActivatedRoute);

  /** Parámetro :id; vale 0 al crear un tema nuevo. */
  readonly id = input(0, { transform: numberAttribute });
  protected readonly error = signal('');
  protected readonly noEncontrado = signal(false);

  protected readonly form = inject(FormBuilder).group({
    titulo: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(150), sinSoloEspacios()]],
    categoriaId: [null as number | null, Validators.required],
    contenido: ['', [Validators.required, Validators.minLength(20), Validators.maxLength(1000), sinSoloEspacios()]],
  });

  ngOnInit(): void {
    if (this.id()) {
      const tema = this.foro.tema(this.id());
      if (!tema || !this.foro.puedeGestionar(tema.autorId)) {
        this.noEncontrado.set(true);
        return;
      }
      this.form.setValue({ titulo: tema.titulo, categoriaId: tema.categoriaId, contenido: tema.contenido });
    } else {
      const categoria = Number(this.ruta.snapshot.queryParamMap.get('categoria'));
      if (this.foro.categorias.some((c) => c.id === categoria)) this.form.controls.categoriaId.setValue(categoria);
    }
  }

  guardar(): void {
    this.error.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const datos = { titulo: v.titulo!, contenido: v.contenido!, categoriaId: Number(v.categoriaId) };
    if (this.id()) {
      const r = this.foro.editarTema(this.id(), datos);
      if (!r.ok) return this.error.set(r.error);
      this.router.navigate(['/temas', this.id()]);
    } else {
      const r = this.foro.publicarTema(datos);
      if (!r.ok) return this.error.set(r.error);
      this.router.navigate(['/temas', r.id]);
    }
  }
}
