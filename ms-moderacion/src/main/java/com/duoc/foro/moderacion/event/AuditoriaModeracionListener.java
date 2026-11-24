package com.duoc.foro.moderacion.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Patrón Observer: escucha los eventos de moderación sin que el servicio sepa que existe.
 * Solo reacciona cuando la transacción se confirmó, así no registra baneos que fallaron.
 * Otros observadores (por ejemplo, notificar al autor) se agregarían sin tocar BaneoServiceImpl.
 */
@Slf4j
@Component
public class AuditoriaModeracionListener {

    @TransactionalEventListener
    public void registrar(ContenidoModeradoEvent evento) {
        log.info("AUDITORIA moderación: {} {} {} por {} (motivo: {})", evento.tipo(), evento.contenidoId(),
                evento.accion(), evento.moderador(), evento.motivo());
    }
}
