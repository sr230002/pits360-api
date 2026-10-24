# GUION DE VIDEO — SEGUNDO PARCIAL PRÁCTICO
## PITS360 · Desarrollo de Aplicaciones Móviles II
**Duración objetivo: 4:25 (máx. 4:30)**

---

## [0:00 – 0:15] INTRO

**Visual:** Home de la app (o logo).

**Narración:**
> "Hola, soy [NOMBRE], carnet [CARNET]. Este es el proyecto PITS360 para el Segundo Parcial Práctico de Desarrollo de Aplicaciones Móviles II. Voy a demostrar los cinco criterios de evaluación: splash screen, login con persistencia local y en Firebase, CRUD de una entidad, sincronización offline y mapa."

---

## [0:15 – 0:25] CRITERIO 1 — SPLASH SCREEN (5%)

**Acción:** Cerrar la app por completo y abrirla de nuevo.

**Narración:**
> "Criterio uno. Al iniciar la aplicación se muestra el splash screen con el logo de PITS360 antes de entrar al login."

---

## [0:25 – 1:15] CRITERIO 2 — LOGIN Y REGISTRO (15%)

### 2.1 — Registro con validaciones

**Acción:** Tocar "Registrarme", llenar campos y provocar los errores a propósito.

**Narración:**
> "Criterio dos. Primero, el registro de usuario. Voy a mostrar las validaciones: email con formato inválido, contraseña menor a ocho caracteres, y confirmación distinta."

**Acción:** Corregir los datos y guardar.

**Narración:**
> "Corrijo los campos y guardo el registro. Todos los campos son validados y la contraseña se encripta."

### 2.2 — Verificar persistencia

**Acción:** Mostrar Room / Database Inspector y luego Firestore Console.

**Narración:**
> "El usuario se guarda en la base local Room, con la contraseña encriptada, y se sincroniza a Firestore. Aquí se ve el mismo registro en ambas bases de datos."

### 2.3 — Login con guardar credenciales

**Acción:** Activar el Chip "Recordar usuario", iniciar sesión, cerrar y reabrir la app.

**Narración:**
> "Activo el chip para recordar credenciales, las cuales se guardan en SharedPreferences. Al reabrir la aplicación, los datos quedan precargados."

---

## [1:15 – 2:25] CRITERIO 3 — CRUD (40%)

### 3.1 — CREATE

**Acción:** Ir al módulo de Citas → Nueva cita → Guardar.

**Narración:**
> "Criterio tres, el de mayor peso. Creo una cita nueva."

### 3.2 — Verificar persistencia doble

**Acción:** Mostrar Database Inspector y Firestore Console.

**Narración:**
> "La cita quedó guardada en SQLite local y en Firestore."

### 3.3 — READ + UPDATE

**Acción:** Abrir la cita, editar un campo, guardar, volver a la lista.

**Narración:**
> "Leo la cita, actualizo sus datos y el cambio se refleja en la lista."

### 3.4 — DELETE

**Acción:** Eliminar la cita y confirmar.

**Narración:**
> "Elimino la cita y se borra también de ambas bases de datos."

**Cierre del criterio:**
> "CRUD completo funcionando en local y remoto."

---

## [2:25 – 3:25] CRITERIO 4 — OFFLINE Y SINCRONIZACIÓN (25%)

### 4.1 — Activar modo avión

**Acción:** Modo avión ON.

**Narración:**
> "Criterio cuatro. Simulo la pérdida de internet activando el modo avión."

### 4.2 — Operación offline

**Acción:** Crear o editar una cita, guardar, mostrar estado pendiente.

**Narración:**
> "Realizo una nueva operación. Se guarda en la base local con estado pendiente de sincronizar. Firestore todavía no tiene el cambio."

**Acción:** Mostrar Database Inspector (flag pendiente) y Firestore (sin el cambio).

**Narración:**
> "Aquí se ve el flag de pendiente en local, y en Firestore aún no aparece."

### 4.3 — Reconectar y sincronizar

**Acción:** Modo avión OFF → Perfil → Sincronizar pendientes → refrescar Firestore.

**Narración:**
> "Desactivo el modo avión, entro al perfil y sincronizo los pendientes. El cambio se sube a Firestore y el estado local se marca como sincronizado."

---

## [3:25 – 3:50] CRITERIO 5 — MAPA (15%)

**Acción:** Abrir el módulo Mapa, mostrar el pin y el InfoWindow.

**Narración:**
> "Criterio cinco. La aplicación integra Google Maps y muestra el punto del negocio PITS360 con su marcador y dirección."

---

## [3:50 – 4:10] EXTRA — ACTUALIZACIÓN DE PERFIL

**Acción:** Menú Toolbar → Perfil → cambiar nombre → confirmar.

**Narración:**
> "Además, desde el perfil se puede actualizar el nombre del usuario, y el cambio se refleja simultáneamente en la base local y en Firestore."

---

## [4:10 – 4:25] CIERRE

**Visual:** Volver al Home.

**Narración:**
> "Con esto quedan demostrados los cinco criterios: splash screen, login con persistencia dual, CRUD completo, sincronización offline con recuperación, y mapa. Gracias."

---

## RESUMEN DE TIEMPOS

| Bloque | Criterio | Duración |
|:---:|:---|---:|
| Intro | — | 0:15 |
| 1 | Splash screen | 0:10 |
| 2 | Login + Registro + SharedPrefs | 0:50 |
| 3 | CRUD | 1:10 |
| 4 | Offline + Sync | 1:00 |
| 5 | Mapa | 0:25 |
| Extra | Perfil | 0:20 |
| Cierre | — | 0:15 |
| **TOTAL** | | **4:25** |

---

## NOTAS PARA GRABAR

- Narra el objetivo, no cada clic.
- Ten abiertas las pestañas en orden: App → Database Inspector → Firestore Console.
- Pre-carga un usuario y una cita para no empezar desde cero.
- No cortes la secuencia "guardar → verificar en DB": esa es la evidencia.
- Graba en una sola toma y recorta silencios después.
- Haz zoom a las tablas al editar para que el profe pueda leer los registros.