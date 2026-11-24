import { Routes } from '@angular/router';
import { conSesion, soloAdmin, soloInvitado } from './guards/guards';
import { AdminModeracion } from './pages/admin-moderacion/admin-moderacion';
import { AdminUsuarios } from './pages/admin-usuarios/admin-usuarios';
import { PaginaCategoria } from './pages/categoria/categoria';
import { Inicio } from './pages/inicio/inicio';
import { Login } from './pages/login/login';
import { NoEncontrado } from './pages/no-encontrado';
import { Perfil } from './pages/perfil/perfil';
import { Recuperar } from './pages/recuperar/recuperar';
import { Registro } from './pages/registro/registro';
import { TemaDetalle } from './pages/tema-detalle/tema-detalle';
import { TemaForm } from './pages/tema-form/tema-form';

export const routes: Routes = [
  { path: '', component: Inicio, title: 'Ágora · Foros en línea' },
  { path: 'login', component: Login, canActivate: [soloInvitado], title: 'Iniciar sesión · Ágora' },
  { path: 'registro', component: Registro, canActivate: [soloInvitado], title: 'Crear cuenta · Ágora' },
  { path: 'recuperar', component: Recuperar, canActivate: [soloInvitado], title: 'Recuperar contraseña · Ágora' },
  { path: 'perfil', component: Perfil, canActivate: [conSesion], title: 'Mi perfil · Ágora' },
  { path: 'categorias/:id', component: PaginaCategoria, title: 'Categoría · Ágora' },
  { path: 'temas/nuevo', component: TemaForm, canActivate: [conSesion], title: 'Publicar tema · Ágora' },
  { path: 'temas/:id', component: TemaDetalle, title: 'Tema · Ágora' },
  { path: 'temas/:id/editar', component: TemaForm, canActivate: [conSesion], title: 'Editar tema · Ágora' },
  { path: 'admin/moderacion', component: AdminModeracion, canActivate: [soloAdmin], title: 'Moderación · Ágora' },
  { path: 'admin/usuarios', component: AdminUsuarios, canActivate: [soloAdmin], title: 'Usuarios · Ágora' },
  { path: '**', component: NoEncontrado, title: 'Página no encontrada · Ágora' },
];
