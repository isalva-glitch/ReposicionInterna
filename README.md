# Reposición Interna

Aplicación Android para gestionar pedidos de reposición de cristales: captura de datos, control de procesos por cara y vista previa del PDF antes de guardarlo y enviarlo por correo.

## Vista previa de la pantalla principal

![Vista previa mock de la pantalla principal](docs/ui-preview.svg)

La maqueta refleja el layout actual en el repo (fondo verde claro, dos tarjetas principales y botones de "Vista previa" / "Guardar y enviar"), con controles para fecha, número de pedido, responsable, sector, material, dimensiones, procesos por cara, DVH, origen del corte y motivo.

## Datos maestros

La app carga los maestros desde `app/src/main/assets/maestros_reposicion.csv` para poblar Material, Responsable y Sector. Podés editar ese archivo con tus valores o reemplazarlo por uno exportado desde tu sistema actual (formato `Material;Responsable;Sector`).

## Funcionalidades actuales

- **Carga de pedidos** con controles para cara 1 y cara 2 (pulido/templado), DVH, motivo y origen de corte.
- **Vista previa y generación de PDF** antes de compartirlo por correo o apps instaladas.
- **Persistencia local** con Room (`ReposicionDatabase` + DAO) para dejar trazabilidad de cada pedido enviado sin perder datos en upgrades.
- **Historial de registros** accesible desde el botón *Ver registros*, que lista cada pedido guardado con su material, procesos y responsable.

### Pantalla de historial

La actividad `RecordsActivity` abre un listado (`ListView`) con cada registro almacenado y usa `RecordsAdapter` para mostrar un resumen legible: fecha y número de pedido, material y origen de corte, procesos por cara y responsable/sector. El botón *Volver* permite regresar rápido a la pantalla principal para cargar un nuevo pedido.
