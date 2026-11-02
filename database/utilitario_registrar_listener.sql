-- =====================================================================
-- Usar solo si la conexion falla con ORA-12514 con el listener ya iniciado.
-- Hace que la instancia vuelva a resolver su listener y se registre en el.
-- No cambia la configuracion guardada (scope=memory).
--   sqlplus / as sysdba @database/utilitario_registrar_listener.sql
-- =====================================================================
ALTER SYSTEM SET LOCAL_LISTENER = 'LISTENER_XE' SCOPE = MEMORY;
ALTER SYSTEM REGISTER;
EXIT;
