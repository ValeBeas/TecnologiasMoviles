# Super Ahorro

Trabajo Práctico Integrador — Tecnologías Móviles 2026
Valentina Beas y Mirko Bubica Hundt — UNDEF

App Android para registrar y analizar gastos de supermercado.

---

## Tecnologías

- Kotlin
- Jetpack Compose
- Navigation Compose
- Material Design 3
- Room (estructura lista para Entrega 2)
- DataStore
- Retrofit (estructura lista para Entrega 2)
- Corrutinas

---

## Arquitectura

Clean Architecture con MVVM. Todo el código está en español.

Las capas son:
- `ui` — pantallas, componentes, navegación y viewmodels
- `domain` — modelos y contratos de repositorio
- `data` — Room, DataStore, Retrofit e implementaciones mock

Single Activity Architecture: hay una sola Activity (MainActivity) y todas
las pantallas son Composables dentro de un NavHost.

---

## Pantallas

- Splash
- Login y Registro
- Home (dashboard con resumen del mes)
- Lista de Compras
- Detalle de Compra
- Nueva Compra
- Agregar Producto
- Mis Productos
- Historial
- Estadísticas
- Perfil
- Configuración

---

## Funcionalidades implementadas

**Nueva Compra**
El usuario elige el supermercado (con opción "Otro" para escribir uno libre),
carga la fecha y agrega productos uno por uno con nombre, cantidad, costo
y código de barras opcional. El total se calcula automáticamente sumando
los productos. Se puede adjuntar la foto del ticket con la cámara o la galería.

**Agregar Producto**
Pantalla con dos tabs: formulario manual y catálogo de productos frecuentes.
Al elegir del catálogo se autocompletan los campos. Solo se puede acceder
desde Nueva Compra, no desde el historial.

**Historial**
Muestra todas las compras agrupadas por mes y año, ordenadas de la más
reciente a la más antigua. Sin filtros.

**Estadísticas**
Filtros por Semana, Mes, 3 Meses y Año. Cada filtro tiene su propia función
de cálculo. Muestra total gastado, cantidad de compras, promedio y
supermercado favorito.

**Modo oscuro**
El toggle en Configuración cambia el tema de toda la app en tiempo real.

**Compartir compra**
Desde el detalle de una compra se puede compartir el resumen por WhatsApp,
email u otras apps usando un Intent implícito ACTION_SEND.

---

## Datos de prueba

El repositorio tiene 17 compras mockeadas distribuidas entre noviembre 2025
y mayo 2026 para que todos los filtros de estadísticas muestren resultados.
Los totales de cada compra se calculan sumando sus productos, no son
números inventados.

## Presentacion Canvas
https://canva.link/den9i19ftkpf571