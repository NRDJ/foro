-- =====================================================================
-- Sistema de Foros en Linea - Creacion del usuario (esquema) en Oracle XE local
-- Ejecutar UNA vez con un usuario administrador:
--   sqlplus / as sysdba @database/01_crear_usuario.sql
-- =====================================================================
ALTER SESSION SET CONTAINER = XEPDB1;

DECLARE
    v_existe NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_existe FROM dba_users WHERE username = 'FORO';
    IF v_existe = 0 THEN
        EXECUTE IMMEDIATE 'CREATE USER FORO IDENTIFIED BY foro_local_2026 DEFAULT TABLESPACE USERS QUOTA UNLIMITED ON USERS';
    END IF;
END;
/

GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO FORO;

EXIT;
