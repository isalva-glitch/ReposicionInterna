# Propuestas de mejora

## Diseño (UI/UX)
- **Jerarquía visual y consistencia Material 3**: aplicar una `MaterialToolbar` en la cabecera con acciones primarias (nuevo, historial, salir) y colores de tema, evitando botones planos desalineados en las primeras filas.
- **Agrupación y ritmo visual**: usar `TextInputLayout` con `startIcon`/`endIcon` para fecha y número de pedido, y estilos `OutlinedBox` para los spinners; reduce la densidad visual de bordes blancos y mejora estados de error accesibles.
- **Claridad en procesos por vidrio**: reemplazar checkboxes por `MaterialChipGroup` (selección múltiple) y añadir etiquetas "Opcional" a notas; así se distinguen mejor acciones aplicadas a cada vidrio y se acorta la altura de los campos multilinea.
- **Estado vacío y feedback**: en el card de vista previa, mostrar un placeholder ilustrado o skeleton cuando no hay datos; en el historial, agregar mensaje "Aún no hay registros" cuando la lista esté vacía para evitar pantalla en blanco.
- **Accesibilidad**: asegurar contraste suficiente entre el fondo verde claro y el texto (p. ej., usando un `ColorStateList` con tono más oscuro) y aumentar el `touch target` de radio buttons/checkboxes; añadir `contentDescription` a los íconos y a la imagen de vista previa.
- **Adaptabilidad**: aprovechar layouts `sw600dp`/`land` para mostrar columnas lado a lado (formulario a la izquierda, vista previa a la derecha) y reducir el scroll en tablets.

## Funcionalidad
- **Validación avanzada**: añadir reglas para dimensiones (obligar alto/ancho numéricos cuando hay procesos), evitar pedidos duplicados por número+fecha y resaltar errores con `TextInputLayout.setError` en lugar de solo `Toast`.
- **Persistencia robusta**: migrar de `SQLiteOpenHelper` a Room con entidades/DAOs y migraciones para no perder datos en upgrades; aprovechar corrutinas y `Flow` para listas reactivas y filtrado (últimos N registros, búsqueda por responsable).
- **Gestión de PDFs**: almacenar el PDF junto al registro (o regenerarlo bajo demanda) y permitir renombrar asunto/destinatario antes de enviar; añadir envío a múltiples canales (WhatsApp/Drive) usando un `Chooser` con texto configurable.
- **Resumen en vivo más legible**: convertir `tvResumen` en una lista de chips o bullets para leer mejor procesos y medidas; recalcular en tiempo real al modificar campos en lugar de solo tras validar, evitando sorpresas al final.
- **Historial utilitario**: mostrar contador total y filtro por fecha/estado DVH; permitir abrir detalle o reenviar el PDF desde cada ítem (añadiendo botones en `item_record`).
- **Respaldo y configuración**: exponer en ajustes la edición de maestros (cargar CSV nuevo o editar en la app) y copia de seguridad/exportación del CSV de registros, facilitando mantenimiento sin tocar assets.
