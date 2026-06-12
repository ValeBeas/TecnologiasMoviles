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
- Room — caché local de compras y productos
- DataStore — sesión persistente, modo oscuro, moneda
- Retrofit — API del dólar (bluelytics.com.ar)
- Supabase — base de datos en la nube, autenticación y storage
- Corrutinas

---

## Arquitectura

Clean Architecture con MVVM. Todo el código está en español.

Las capas son:
- `ui` — pantallas, componentes, navegación y viewmodels
- `domain` — modelos y contratos de repositorio
- `data` — Room, DataStore, Retrofit, Supabase y repositorios reales

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
- Editar Compra
- Historial
- Estadísticas
- Perfil
- Configuración

---

## Funcionalidades implementadas

**Autenticación real**
Login y registro contra Supabase Auth. La sesión persiste entre cierres
de la app usando DataStore — el usuario no tiene que loguearse cada vez
que abre la app. Al cerrar sesión se limpian los tokens y el caché local.

**Nueva Compra**
El usuario elige el supermercado (con opción "Otro" para escribir uno libre),
carga la fecha y hora con formato validado, y agrega productos uno por uno
con nombre, cantidad, costo y código de barras opcional. El total se calcula
automáticamente sumando los productos. Se puede adjuntar la foto del ticket
con la cámara o la galería. La compra se guarda en Supabase y en Room.

**Foto del ticket**
La cámara pide permiso en runtime antes de abrirse. La foto se sube a
Supabase Storage y la URL queda asociada a la compra. Se muestra en el
detalle solo si existe.

**Agregar Producto**
Pantalla con dos tabs: formulario manual y catálogo personal del usuario.
Al elegir del catálogo se autocompletan los campos. Solo se puede acceder
desde Nueva Compra, no desde el historial.

**Mis Productos**
Catálogo personal del usuario guardado en Supabase. Se puede agregar un
producto nuevo con el botón + y eliminar los existentes individualmente.

**Detalle de Compra**
Muestra los datos de la compra, la foto del ticket si tiene, la lista de
productos con precios y el total calculado. Permite editar, borrar con
confirmación y compartir la compra.

**Editar Compra**
Permite modificar el supermercado, fecha, hora y cantidades de productos
de una compra existente. Los cambios se guardan en Supabase y Room.

**Historial**
Muestra todas las compras agrupadas por mes y año, ordenadas de la más
reciente a la más antigua.

**Estadísticas**
Filtros por Semana, Mes, 3 Meses y Año. Muestra total gastado, cantidad
de compras, promedio y supermercado favorito. Gráfico de barras con
gastos por día y distribución por supermercado.

**Conversión de moneda**
La API del dólar se consulta una vez al abrir la app con Retrofit. Desde
Configuración el usuario puede ver todos los precios en ARS o USD.
La preferencia se guarda en DataStore.

**Modo oscuro**
El toggle en Configuración cambia el tema de toda la app en tiempo real.
La preferencia persiste entre sesiones gracias a DataStore.

**Compartir compra**
Desde el detalle de una compra se puede compartir el resumen por WhatsApp,
email u otras apps usando un Intent implícito ACTION_SEND.

**Perfil**
El usuario puede editar su nombre y apellido. Los cambios se guardan en
Supabase y en DataStore.

---

## Sincronización de datos

Room funciona como caché local de Supabase. Al abrir la app se sincroniza
Room completo desde Supabase. Las pantallas leen siempre de Room para
respuesta instantánea. Al hacer cambios se actualiza Supabase y Room
simultáneamente. Al cerrar sesión Room se limpia.

---

## Repositorio

https://github.com/ValeBeas/TecnologiasMoviles