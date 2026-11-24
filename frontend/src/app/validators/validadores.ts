import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Reglas de seguridad de la contraseña (las mismas en registro, recuperación y perfil):
 * 8 a 20 caracteres (Validators.minLength/maxLength), mayúscula, minúscula, número,
 * carácter especial y sin espacios.
 */
export const PASSWORD_MIN = 8;
export const PASSWORD_MAX = 20;

export function passwordSegura(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const valor: string = control.value ?? '';
    if (!valor) return null;
    const errores: ValidationErrors = {};
    if (!/[A-ZÁÉÍÓÚÑ]/.test(valor)) errores['sinMayuscula'] = true;
    if (!/[a-záéíóúñ]/.test(valor)) errores['sinMinuscula'] = true;
    if (!/\d/.test(valor)) errores['sinNumero'] = true;
    if (!/[^A-Za-z0-9ÁÉÍÓÚáéíóúÑñ\s]/.test(valor)) errores['sinEspecial'] = true;
    if (/\s/.test(valor)) errores['conEspacios'] = true;
    return Object.keys(errores).length ? errores : null;
  };
}

/** Validador de grupo: el campo de confirmación debe coincidir con la contraseña. */
export function coinciden(campo: string, confirmacion: string): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const a = grupo.get(campo)?.value;
    const b = grupo.get(confirmacion)?.value;
    return a && b && a !== b ? { noCoinciden: true } : null;
  };
}

/** Rechaza textos formados solo por espacios (Validators.required los acepta). */
export function sinSoloEspacios(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null =>
    typeof control.value === 'string' && control.value.length > 0 && control.value.trim().length === 0
      ? { soloEspacios: true }
      : null;
}

/** Nombre de persona: letras (con tildes), espacios, apóstrofo o guion. */
export const PATRON_NOMBRE = /^[A-Za-zÁÉÍÓÚáéíóúÑñÜü' -]+$/;
/** Nombre de usuario: igual que en ms-usuarios. */
export const PATRON_USERNAME = /^[a-zA-Z0-9._-]{3,50}$/;
/** Correo con dominio y extensión (Validators.email acepta "a@b"). */
export const PATRON_EMAIL = /^[^\s@]+@[^\s@]+\.[A-Za-z]{2,}$/;

/** Mensajes en español para cada error de validación. */
export const MENSAJES: Record<string, (e: any) => string> = {
  required: () => 'Este campo es obligatorio',
  email: () => 'Ingrese un correo válido',
  minlength: (e) => `Debe tener al menos ${e.requiredLength} caracteres`,
  maxlength: (e) => `No puede superar los ${e.requiredLength} caracteres`,
  min: (e) => `El valor mínimo es ${e.min}`,
  sinMayuscula: () => 'Debe incluir al menos una letra mayúscula',
  sinMinuscula: () => 'Debe incluir al menos una letra minúscula',
  sinNumero: () => 'Debe incluir al menos un número',
  sinEspecial: () => 'Debe incluir al menos un carácter especial (por ejemplo ! # $ %)',
  conEspacios: () => 'No puede contener espacios',
  soloEspacios: () => 'No puede contener solo espacios',
  usernameTomado: () => 'Ese nombre de usuario ya está registrado',
  emailTomado: () => 'Ese correo ya está registrado',
  codigo: () => 'El código tiene 6 dígitos',
  terminos: () => 'Debe aceptar las normas de la comunidad',
};
