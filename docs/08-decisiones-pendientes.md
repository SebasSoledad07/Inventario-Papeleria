# Decisiones pendientes

Vacíos que se detectaron al revisar el documento original. Cada uno trae una **propuesta**. Hasta que el dueño del proyecto las confirme, **el agente no debe inventar la regla**: debe preguntar o usar la propuesta marcándola con `// PENDIENTE-DECISION-n`.

Cuando una decisión se confirme, se convierte en una regla `RN-xx` en [02-reglas-de-negocio.md](02-reglas-de-negocio.md) y se borra de aquí.

## D-1. Prorrateo del descuento sobre el total

**Problema.** RN-33 permite descuentos sobre el total de la venta, pero el IVA se calcula y se redondea por línea (RN-11) y `detalle_venta` guarda el descuento por línea.

**Propuesta.** El descuento global se reparte entre las líneas en proporción a su `total_linea`. La diferencia por redondeo se asigna a la última línea. Después se calcula base e IVA de cada línea sobre su valor ya descontado. La suma de las líneas debe coincidir exactamente con el total de la venta.

**Prueba obligatoria.** `CalculadoraVenta` con descuentos que no se dividen exactamente (por ejemplo, 3 líneas y un descuento de $1.000).

## D-2. Anulación de una venta cuya caja ya cerró

**Problema.** RN-38 permite anular ventas, pero no dice qué pasa si la caja de esa venta ya está cerrada: el cuadre histórico cambiaría.

**Propuesta.** Solo se puede anular una venta mientras su caja esté `ABIERTA`. Pasado el cierre, no se anula; la corrección se hace con una venta nueva y un ajuste de inventario con motivo. Además, `efectivo_esperado` (RN-41) debe excluir las ventas `ANULADA`.

**Alternativa.** Permitir la anulación posterior y registrarla como egreso en la caja abierta del día. Es más flexible pero complica el cuadre.

## D-3. Conteo físico por sesiones (RF-18)

**Problema.** El esquema no tiene tablas para el conteo físico.

**Propuesta.** En la versión 1 el conteo se hace en **una sola sesión**: el estado del conteo vive en el navegador y, al terminar, `ConteoFisicoService` genera un ajuste (`AJUSTE_POSITIVO` o `AJUSTE_NEGATIVO`, motivo "conteo físico") por cada diferencia, en una sola transacción.

**Si se necesitan varias sesiones o varias personas**, se agregan las tablas `conteos` y `detalle_conteo` en una migración nueva.

## D-4. Administrador inicial

**Problema.** Sin un usuario no se puede iniciar sesión, y una contraseña fija dentro de una migración es un riesgo.

**Propuesta.** `AdminInicialRunner` (módulo `usuario`) se ejecuta al arrancar. Si la tabla `usuarios` está vacía, crea un `ADMIN` con el usuario y la clave de las variables `ADMIN_INICIAL_USUARIO` y `ADMIN_INICIAL_CLAVE`, cifrada con BCrypt (RN-43). Si la clave está vacía, no crea nada y escribe una advertencia en el log. Debe cambiarse en el primer inicio de sesión.

## D-5. Impresión de la tirilla

**Problema.** El documento exige impresora térmica de 58/80 mm (RF-26) pero no define cómo se imprime.

**Propuesta.** En la versión 1, una vista HTML con CSS `@page` (ancho 58 mm u 80 mm según configuración) impresa desde el navegador con el driver de la impresora. No se usa ESC/POS. Si el cajón monedero debe abrirse automáticamente, se evalúa en una versión posterior.

## D-6. Autorización de descuentos

**Problema.** RN-33 exige autorización de un administrador, pero no dice cómo se otorga durante la venta.

**Propuesta.** Si el descuento supera el límite, la caja pide usuario y clave de un `ADMIN` en una ventana emergente. El servicio valida las credenciales, y `ventas.descuento_autorizado_por` guarda ese usuario. La clave no se guarda ni se registra en ningún log.
