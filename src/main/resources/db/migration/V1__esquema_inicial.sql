-- ============================================================
-- V1: Esquema inicial (docs/05). Moneda NUMERIC(12,0) (RN-02),
-- valores fijos VARCHAR + CHECK (mapean con @Enumerated STRING),
-- fechas TIMESTAMPTZ en America/Bogota (RN-39).
-- Una migracion aplicada no se edita: los cambios son V3, V4, ...
-- ============================================================

-- Extensiones para busqueda por nombre (RF-06): unaccent + pg_trgm
CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

--- Funcion inmutable para normalizar texto (sin tildes), indexable
CREATE OR REPLACE FUNCTION f_unaccent(text)
RETURNS text
LANGUAGE sql
IMMUTABLE
PARALLEL SAFE
STRICT
SET search_path = public
AS $$
SELECT public.unaccent('public.unaccent'::regdictionary, $1)
$$;

-- ------------------------------------------------------------
-- Usuarios (RF-43, RF-44) y roles
-- ------------------------------------------------------------
CREATE TABLE usuarios (
    id              BIGSERIAL PRIMARY KEY,
    usuario         VARCHAR(60)  NOT NULL UNIQUE,
    clave           VARCHAR(100) NOT NULL,  -- BCrypt (RN-43)
    nombre          VARCHAR(120) NOT NULL,
    rol             VARCHAR(20)  NOT NULL CHECK (rol IN ('ADMIN', 'CAJERO', 'BODEGUERO')),
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- Configuracion del negocio (RF-45) y parametros (RF-46)
-- ------------------------------------------------------------
CREATE TABLE configuracion (
    clave           VARCHAR(60)  PRIMARY KEY,
    valor           TEXT,
    descripcion     VARCHAR(255),
    actualizado_en  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- Auditoria de acciones sensibles (RN-45)
-- ------------------------------------------------------------
CREATE TABLE auditoria (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT      NOT NULL REFERENCES usuarios (id),
    accion      VARCHAR(80) NOT NULL,
    detalle     TEXT,
    fecha       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_auditoria_usuario_id ON auditoria (usuario_id);
CREATE INDEX idx_auditoria_fecha    ON auditoria (fecha);

-- ------------------------------------------------------------
-- Categorias y proveedores (RF-08)
-- ------------------------------------------------------------
CREATE TABLE categorias (
    id       BIGSERIAL PRIMARY KEY,
    nombre   VARCHAR(100) NOT NULL UNIQUE,
    activo   BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE proveedores (
    id           BIGSERIAL PRIMARY KEY,
    razon_social VARCHAR(150) NOT NULL UNIQUE,
    nit          VARCHAR(20)  NOT NULL UNIQUE,
    telefono     VARCHAR(30),
    direccion    VARCHAR(150),
    activo       BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- Productos (RF-01 a RF-07). El stock solo lo modifica el
-- modulo inventario (RN-20); el CHECK garantiza RN-19.
-- ------------------------------------------------------------
CREATE TABLE productos (
    id              BIGSERIAL PRIMARY KEY,
    codigo_barras   VARCHAR(13)  NOT NULL UNIQUE,   -- normalizado (RN-13, RN-14)
    nombre          VARCHAR(150) NOT NULL,
    marca           VARCHAR(100),
    categoria_id    BIGINT       NOT NULL REFERENCES categorias (id),
    proveedor_id    BIGINT       REFERENCES proveedores (id),
    unidad          VARCHAR(20)  NOT NULL CHECK (unidad IN ('UNIDAD', 'PAQUETE', 'RESMA', 'CAJA', 'PLIEGO', 'METRO', 'SERVICIO')),  -- RN-18
    tarifa_iva      VARCHAR(20)  NOT NULL CHECK (tarifa_iva IN ('19', '5', '0', 'EXCLUIDO')),  -- RN-09
    precio_venta    NUMERIC(12,0) NOT NULL CHECK (precio_venta > 0),  -- precio con IVA incluido (RN-04)
    costo           NUMERIC(12,0) NOT NULL DEFAULT 0 CHECK (costo >= 0),  -- costo promedio (RN-23)
    stock           INTEGER      NOT NULL DEFAULT 0 CHECK (stock >= 0),   -- RN-19
    stock_minimo    INTEGER,     -- si es NULL se usa el valor de configuracion (RN-25)
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,  -- RN-17
    creado_en       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Indice trigram/unaccent para busqueda por nombre (RF-06)
CREATE INDEX idx_productos_nombre_trgm
    ON productos USING gin (lower(f_unaccent(nombre)) gin_trgm_ops);
CREATE INDEX idx_productos_categoria_id ON productos (categoria_id);
CREATE INDEX idx_productos_proveedor_id ON productos (proveedor_id);

-- ------------------------------------------------------------
-- Historial de precios (RN-07)
-- ------------------------------------------------------------
CREATE TABLE historial_precios (
    id              BIGSERIAL PRIMARY KEY,
    producto_id     BIGINT       NOT NULL REFERENCES productos (id),
    precio_anterior NUMERIC(12,0) NOT NULL,
    precio_nuevo    NUMERIC(12,0) NOT NULL,
    usuario_id      BIGINT       NOT NULL REFERENCES usuarios (id),
    fecha           TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_historial_precios_producto_id ON historial_precios (producto_id);

-- ------------------------------------------------------------
-- Movimientos de inventario (kardex, RN-20) y tipos (RN-21)
-- ------------------------------------------------------------
CREATE TABLE movimientos_inventario (
    id               BIGSERIAL PRIMARY KEY,
    producto_id      BIGINT      NOT NULL REFERENCES productos (id),
    tipo             VARCHAR(30) NOT NULL CHECK (tipo IN ('ENTRADA_COMPRA', 'SALIDA_VENTA', 'DEVOLUCION_VENTA', 'AJUSTE_POSITIVO', 'AJUSTE_NEGATIVO', 'STOCK_INICIAL')),
    cantidad         INTEGER     NOT NULL,
    stock_resultante INTEGER     NOT NULL,
    referencia       VARCHAR(100),
    motivo           VARCHAR(150),
    usuario_id       BIGINT      NOT NULL REFERENCES usuarios (id),
    fecha            TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_movimientos_producto_id ON movimientos_inventario (producto_id);
CREATE INDEX idx_movimientos_fecha      ON movimientos_inventario (fecha);

-- ------------------------------------------------------------
-- Compras a proveedores (RF-14, RF-15)
-- ------------------------------------------------------------
CREATE TABLE compras (
    id              BIGSERIAL PRIMARY KEY,
    proveedor_id    BIGINT      NOT NULL REFERENCES proveedores (id),
    numero_factura  VARCHAR(30) NOT NULL,
    fecha           TIMESTAMPTZ NOT NULL DEFAULT now(),
    usuario_id      BIGINT      NOT NULL REFERENCES usuarios (id),
    observacion     TEXT,
    UNIQUE (proveedor_id, numero_factura)
);

CREATE INDEX idx_compras_proveedor_id ON compras (proveedor_id);
CREATE INDEX idx_compras_usuario_id   ON compras (usuario_id);

CREATE TABLE detalle_compra (
    id             BIGSERIAL PRIMARY KEY,
    compra_id      BIGINT       NOT NULL REFERENCES compras (id),
    producto_id    BIGINT       NOT NULL REFERENCES productos (id),
    cantidad       INTEGER      NOT NULL CHECK (cantidad > 0),
    costo_unitario NUMERIC(12,0) NOT NULL CHECK (costo_unitario >= 0)
);

CREATE INDEX idx_detalle_compra_compra_id   ON detalle_compra (compra_id);
CREATE INDEX idx_detalle_compra_producto_id ON detalle_compra (producto_id);

-- ------------------------------------------------------------
-- Clientes (RN-36, opcional en la venta)
-- ------------------------------------------------------------
CREATE TABLE clientes (
    id               BIGSERIAL PRIMARY KEY,
    tipo_documento   VARCHAR(12) NOT NULL CHECK (tipo_documento IN ('CC', 'NIT', 'CE', 'PASAPORTE')),
    numero_documento VARCHAR(20) NOT NULL,
    nombre           VARCHAR(150) NOT NULL,
    telefono         VARCHAR(30),
    creado_en        TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tipo_documento, numero_documento)
);

-- ------------------------------------------------------------
-- Cajas (RF-19, RF-29; RN-30, RN-40, RN-41, RN-42)
-- ------------------------------------------------------------
CREATE TABLE cajas (
    id               BIGSERIAL PRIMARY KEY,
    usuario_id       BIGINT        NOT NULL REFERENCES usuarios (id),
    estado           VARCHAR(10)   NOT NULL CHECK (estado IN ('ABIERTA', 'CERRADA')),
    base_efectivo    NUMERIC(12,0) NOT NULL DEFAULT 0,   -- RN-40
    apertura         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    cierre           TIMESTAMPTZ,
    efectivo_contado NUMERIC(12,0),                       -- RN-41
    diferencia       NUMERIC(12,0)
);

CREATE INDEX idx_cajas_usuario_id ON cajas (usuario_id);
CREATE INDEX idx_cajas_estado     ON cajas (estado);

-- ------------------------------------------------------------
-- Ventas (RF-25 a RF-28; RN-31, RN-37, RN-38)
-- El consecutivo es por la tabla consecutivos, NO una secuencia (RN-31).
-- ------------------------------------------------------------
CREATE TABLE ventas (
    id                       BIGSERIAL PRIMARY KEY,
    consecutivo              INTEGER       NOT NULL,
    caja_id                  BIGINT        NOT NULL REFERENCES cajas (id),
    usuario_id               BIGINT        NOT NULL REFERENCES usuarios (id),
    cliente_id               BIGINT        REFERENCES clientes (id),
    fecha                    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    estado                   VARCHAR(12)   NOT NULL DEFAULT 'CONFIRMADA' CHECK (estado IN ('CONFIRMADA', 'ANULADA')),
    subtotal                 NUMERIC(12,0) NOT NULL,
    descuento_total          NUMERIC(12,0) NOT NULL DEFAULT 0,
    iva_total                NUMERIC(12,0) NOT NULL,
    total                    NUMERIC(12,0) NOT NULL,
    cambio                   NUMERIC(12,0) NOT NULL DEFAULT 0,
    motivo_anulacion         VARCHAR(150),
    descuento_autorizado_por BIGINT        REFERENCES usuarios (id),  -- RN-33
    UNIQUE (consecutivo)
);

CREATE INDEX idx_ventas_caja_id     ON ventas (caja_id);
CREATE INDEX idx_ventas_usuario_id  ON ventas (usuario_id);
CREATE INDEX idx_ventas_cliente_id  ON ventas (cliente_id);
CREATE INDEX idx_ventas_fecha       ON ventas (fecha);
CREATE INDEX idx_ventas_estado      ON ventas (estado);

CREATE TABLE detalle_venta (
    id             BIGSERIAL PRIMARY KEY,
    venta_id       BIGINT       NOT NULL REFERENCES ventas (id),
    producto_id    BIGINT       NOT NULL REFERENCES productos (id),
    cantidad       INTEGER      NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(12,0) NOT NULL,  -- precio vigente al momento de la venta (RN-08)
    base           NUMERIC(12,0) NOT NULL,   -- RN-11
    iva            NUMERIC(12,0) NOT NULL,   -- RN-11
    descuento      NUMERIC(12,0) NOT NULL DEFAULT 0,
    total_linea    NUMERIC(12,0) NOT NULL
);

CREATE INDEX idx_detalle_venta_venta_id    ON detalle_venta (venta_id);
CREATE INDEX idx_detalle_venta_producto_id ON detalle_venta (producto_id);

CREATE TABLE pagos_venta (
    id       BIGSERIAL PRIMARY KEY,
    venta_id BIGINT       NOT NULL REFERENCES ventas (id),
    medio    VARCHAR(20)  NOT NULL CHECK (medio IN ('EFECTIVO', 'TARJETA_DEBITO', 'TARJETA_CREDITO', 'NEQUI', 'DAVIPLATA', 'TRANSFERENCIA')),  -- RN-34
    valor    NUMERIC(12,0) NOT NULL
);

CREATE INDEX idx_pagos_venta_venta_id ON pagos_venta (venta_id);

-- ------------------------------------------------------------
-- Consecutivos (RN-31): toma el siguiente numero con
-- UPDATE ... RETURNING dentro de la transaccion de la venta.
-- ------------------------------------------------------------
CREATE TABLE consecutivos (
    nombre VARCHAR(30) PRIMARY KEY,
    ultimo INTEGER NOT NULL DEFAULT 0
);

-- ------------------------------------------------------------
-- Datos iniciales
-- ------------------------------------------------------------
-- Parametros por defecto (RF-46); el admin los edita desde configuracion
INSERT INTO configuracion (clave, valor, descripcion) VALUES
    ('stock_minimo_defecto',   '5',  'Stock minimo por defecto si el producto no trae uno (RN-25)'),
    ('dias_agotarse_umbral',   '7',  'Umbral de dias para marcar un producto como POR_AGOTARSE (RN-26)'),
    ('dias_promedio_ventas',   '30', 'Dias de venta para calcular el promedio diario (RN-27)'),
    ('dias_cobertura_pedido',  '15', 'Dias de cobertura para la cantidad sugerida de compra (RN-28)'),
    ('limite_descuento',       '10', 'Limite (%) de descuento sin autorizacion de administrador (RN-33)'),
    ('razon_social',           '',   'Razon social del negocio para la tirilla (RF-45)'),
    ('nit',                    '',   'NIT del negocio (RF-45)'),
    ('direccion',              '',   'Direccion del negocio (RF-45)'),
    ('telefono',               '',   'Telefono del negocio (RF-45)'),
    ('mensaje_tirilla',        '',   'Mensaje al pie de la tirilla (RF-45)');

-- Consecutivo de ventas arranca en 0 (RN-31)
INSERT INTO consecutivos (nombre, ultimo) VALUES ('VENTA', 0);