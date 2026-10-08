This is a Kotlin Multiplatform project targeting Android, iOS.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
## Manejo de errores

La aplicacion implementa un manejo controlado de errores para las operaciones CRUD de productos. Las llamadas REST se realizan mediante Ktor y los errores HTTP son transformados a tipos de dominio mediante ErrorApi.

### Tipos de error controlados

- ErrorApi.Validacion: errores HTTP 400 enviados por el servidor y asociados a campos del formulario.
- ErrorApi.NoEncontrado: respuestas HTTP 404 cuando el recurso solicitado no existe.
- ErrorApi.Conflicto: respuestas HTTP 409 producidas por conflictos o reglas de negocio.
- ErrorApi.Servidor: errores inesperados provenientes del servidor.
- ErrorApi.SinConexion: se utiliza cuando no es posible establecer comunicacion con el servidor.
- ErrorApi.TiempoAgotado: operaciones que superan el tiempo maximo configurado para la solicitud.

### Validacion local

Antes de enviar determinados datos al servidor, los casos de uso realizan validaciones locales. Por ejemplo, un precio igual o menor a cero es rechazado antes de realizar la peticion HTTP y el mensaje se muestra directamente en el formulario.

### Cancelacion de operaciones

CancellationException se vuelve a lanzar y no se transforma en un error de conexion. Esto evita que una cancelacion de coroutine sea presentada al usuario como un fallo de red.

### Pruebas

Las pruebas compartidas se encuentran en shared/src/commonTest. FakeProductoRepository permite probar el comportamiento de ProductoViewModel sin depender del servidor real.

Para ejecutar la suite completa en Windows:

.\gradlew.bat :shared:allTests

Las pruebas verifican inventario vacio, carga de productos, errores de validacion, registro de productos, configuracion de dependencias y solicitudes realizadas mediante Ktor.

## Capacidades nativas

En la Sesion 09 se implementaron capacidades nativas multiplataforma utilizando expect/actual e inyeccion de dependencias.

### Formato de moneda

Se implemento `formatearSoles` mediante `expect/actual`.

- `commonMain`: declaracion comun de `formatearSoles`.
- `androidMain`: implementacion usando `NumberFormat` y `Locale("es", "PE")`.
- `iosMain`: implementacion usando `NSNumberFormatter` y `NSLocale("es_PE")`.

La capa de presentacion utiliza esta funcion para mostrar los precios en soles sin depender directamente de APIs de Android o iOS.

### Compartir productos

Se definio la interfaz `Compartidor` en `commonMain`.

- Android: `CompartidorAndroid` mediante `Intent.ACTION_SEND`.
- iOS: `CompartidorIos` mediante `UIActivityViewController`.

Las implementaciones se registran mediante Koin en el `platformModule` correspondiente a cada plataforma.

En Android se verifico el funcionamiento del selector nativo para compartir la informacion de un producto.

## Código específico de plataforma

PharmaMobil utiliza Kotlin Multiplatform para compartir código entre Android e iOS. Las implementaciones nativas se encuentran separadas por plataforma.

### 1. Formato de moneda (expect/actual)

- commonMain: platform/Formato.kt declara formatearSoles.
- androidMain: platform/Formato.android.kt utiliza NumberFormat y Locale("es", "PE").
- iosMain: platform/Formato.ios.kt utiliza NSNumberFormatter y NSLocale("es_PE").

### 2. Compartir productos (interfaz e inyección de dependencias)

- commonMain: domain/platform/Compartidor.kt define la interfaz.
- androidMain: platform/CompartidorAndroid.kt utiliza Intent.ACTION_SEND.
- iosMain: platform/CompartidorIos.kt utiliza UIActivityViewController.

Koin selecciona la implementación correspondiente a cada plataforma.

### 3. Información del dispositivo (expect/actual)

- commonMain: platform/InfoDispositivo.kt declara expect class InfoDispositivo.
- androidMain: platform/InfoDispositivo.android.kt utiliza Build.VERSION.RELEASE.
- iosMain: platform/InfoDispositivo.ios.kt utiliza UIDevice.currentDevice.systemVersion.

La pantalla Inicio muestra la tarjeta "Acerca del dispositivo".
En el emulador se verificó Android 13.

### 4. Configuración de dependencias

- androidMain: di/PlatformModule.android.kt configura las dependencias de Android.
- iosMain: di/PlatformModule.ios.kt configura las dependencias de iOS.

### 5. Aislamiento y compilación

Se comprobó que commonMain no contiene importaciones directas de android.* ni platform.*.

Al desactivar temporalmente InfoDispositivo.android.kt, el compilador mostró:

Expected InfoDispositivo has no actual declaration in module <commonMain> for JVM

Se restauró el archivo y la compilación Android finalizó correctamente.

### 6. Estado de verificación

- Android: compilación y ejecución verificadas.
- iOS: implementación escrita; prueba pendiente en macOS con Xcode.

Rama: feature/autonoma09-huiza

