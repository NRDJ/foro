import { BehaviorSubject, Observable } from 'rxjs';

/**
 * Patrón Observer: una lista guardada en un BehaviorSubject. Cada cambio se emite a todos
 * los componentes suscritos, que se actualizan solos.
 *
 * La lista parte desde los arreglos de datos-iniciales.ts y se respalda en localStorage para
 * que lo creado durante la demostración no se pierda al recargar la página.
 */
export class Coleccion<T extends { id: number }> {
  private readonly sujeto: BehaviorSubject<T[]>;
  readonly cambios$: Observable<T[]>;

  constructor(
    private readonly clave: string,
    private readonly iniciales: T[],
  ) {
    this.sujeto = new BehaviorSubject<T[]>(this.leer());
    this.cambios$ = this.sujeto.asObservable();
  }

  get valor(): T[] {
    return this.sujeto.value;
  }

  buscar(id: number): T | undefined {
    return this.valor.find((e) => e.id === id);
  }

  siguienteId(): number {
    return Math.max(0, ...this.valor.map((e) => e.id)) + 1;
  }

  agregar(elemento: T): void {
    this.publicar([...this.valor, elemento]);
  }

  actualizar(id: number, cambios: Partial<T>): void {
    this.publicar(this.valor.map((e) => (e.id === id ? { ...e, ...cambios } : e)));
  }

  eliminarDonde(condicion: (e: T) => boolean): void {
    this.publicar(this.valor.filter((e) => !condicion(e)));
  }

  restablecer(): void {
    this.publicar(structuredClone(this.iniciales));
  }

  private publicar(lista: T[]): void {
    this.sujeto.next(lista);
    try {
      localStorage.setItem(this.clave, JSON.stringify(lista));
    } catch {
      // Sin almacenamiento disponible (modo privado): la lista sigue funcionando en memoria.
    }
  }

  private leer(): T[] {
    try {
      const guardado = localStorage.getItem(this.clave);
      if (guardado) {
        return JSON.parse(guardado) as T[];
      }
    } catch {
      // Dato dañado o almacenamiento bloqueado: se usan los datos iniciales.
    }
    return structuredClone(this.iniciales);
  }
}
