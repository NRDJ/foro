package com.duoc.foro.moderacion.service;

import com.duoc.foro.moderacion.dto.BaneoRequest;
import com.duoc.foro.moderacion.dto.BaneoResponse;
import com.duoc.foro.moderacion.dto.MotivoRequest;
import com.duoc.foro.moderacion.event.ContenidoModeradoEvent;
import com.duoc.foro.moderacion.event.ContenidoModeradoEvent.Accion;
import com.duoc.foro.moderacion.exception.ConflictoException;
import com.duoc.foro.moderacion.exception.RecursoNoEncontradoException;
import com.duoc.foro.moderacion.model.Baneo;
import com.duoc.foro.moderacion.model.TipoContenido;
import com.duoc.foro.moderacion.repository.BaneoRepository;
import com.duoc.foro.moderacion.security.UsuarioActual;
import com.duoc.foro.moderacion.service.estrategia.EstrategiaModeracion;
import com.duoc.foro.moderacion.service.estrategia.EstrategiaModeracionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BaneoServiceImpl implements BaneoService {

    private final BaneoRepository repository;
    private final EstrategiaModeracionFactory estrategias;
    private final ApplicationEventPublisher eventos;

    @Override
    @Transactional(readOnly = true)
    public List<BaneoResponse> listar(TipoContenido tipo) {
        return repository.listar(tipo).stream().map(BaneoResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BaneoResponse obtener(Long id) {
        return BaneoResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public BaneoResponse banear(BaneoRequest r, UsuarioActual admin, String token) {
        if (repository.existsByTipoAndContenidoId(r.tipo(), r.contenidoId())) {
            throw new ConflictoException("Ese contenido ya está baneado");
        }
        EstrategiaModeracion estrategia = estrategias.obtener(r.tipo());
        // Primero se oculta en ms-foros: si el contenido no existe, ms-foros responde 404 y no se guarda nada.
        estrategia.banear(r.contenidoId(), token);

        Baneo baneo = new Baneo();
        baneo.setTipo(r.tipo());
        baneo.setContenidoId(r.contenidoId());
        baneo.setMotivo(r.motivo().trim());
        baneo.setModeradorId(admin.id());
        baneo.setModeradorUsername(admin.username());
        Baneo guardado;
        try {
            guardado = repository.saveAndFlush(baneo);
        } catch (RuntimeException e) {
            // Compensación: si no se pudo registrar, el contenido vuelve a quedar visible.
            log.warn("No se pudo registrar el baneo de {} {}; se restaura en ms-foros", r.tipo(), r.contenidoId());
            estrategia.restaurar(r.contenidoId(), token);
            throw e;
        }
        eventos.publishEvent(new ContenidoModeradoEvent(r.tipo(), r.contenidoId(), Accion.BANEADO, admin.username(), guardado.getMotivo()));
        return BaneoResponse.desde(guardado);
    }

    @Override
    @Transactional
    public BaneoResponse actualizarMotivo(Long id, MotivoRequest r, UsuarioActual admin) {
        Baneo baneo = buscar(id);
        baneo.setMotivo(r.motivo().trim());
        baneo.setModeradorId(admin.id());
        baneo.setModeradorUsername(admin.username());
        log.info("Motivo del baneo {} actualizado por {}", id, admin.username());
        return BaneoResponse.desde(repository.saveAndFlush(baneo));
    }

    @Override
    @Transactional
    public void revertir(Long id, UsuarioActual admin, String token) {
        Baneo baneo = buscar(id);
        try {
            estrategias.obtener(baneo.getTipo()).restaurar(baneo.getContenidoId(), token);
        } catch (RecursoNoEncontradoException e) {
            // El autor eliminó el contenido después del baneo: ya no hay nada que restaurar.
            log.warn("El {} {} ya no existe en ms-foros; solo se elimina el registro", baneo.getTipo(), baneo.getContenidoId());
        }
        repository.delete(baneo);
        eventos.publishEvent(new ContenidoModeradoEvent(baneo.getTipo(), baneo.getContenidoId(), Accion.RESTAURADO, admin.username(), baneo.getMotivo()));
    }

    private Baneo buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el baneo con id " + id));
    }
}
