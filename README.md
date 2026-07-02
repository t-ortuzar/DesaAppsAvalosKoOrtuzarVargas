# ArgenGamer

Una aplicación Android moderna para trackear precios de videojuegos en múltiples tiendas digitales, con precios reales para Argentina y soporte para juegos Free-to-Play.

## 🎮 Características Principales

### 1. **Catálogo de Juegos**
- **140 juegos**: pagos + Free-to-Play (League of Legends, Valorant, Fortnite, CS2, Dota 2, Genshin Impact, etc.)
- Imágenes reales vía **Steam CDN** para juegos en Steam
- **Imágenes dinámicas desde Epic Games Store** (GraphQL `keyImages`) para juegos exclusivos de Epic (ej. Alan Wake 2)
- **Búsqueda por nombre** y **búsqueda por voz** (micrófono con Speech-to-Text de Android)
- Filtro por tags (Action, RPG, Horror, Free2Play, etc.) y por tienda
- **Sistema de favoritos sincronizado con Firebase** (persiste entre dispositivos y reinicios)
- Tags: 27+ categorías incluyendo Free2Play, MOBA, Battle Royale
- Plataformas correctamente mapeadas por juego: cada juego sólo consulta las tiendas donde realmente está disponible
- **Banner offline**: aviso visual cuando no hay conexión a internet

### 2. **Precios Reales para Argentina 🇦🇷**
- **Steam Store API** (`cc=ar`): Precios regionales en ARS con detección de EA Play (precio=0 vía suscripción)
- **Epic Games Store GraphQL API** (`country=AR`): Precios en ARS con filtro por edición base (evita ediciones Gold/Ultimate)
- **Epic page scraping** (`__NEXT_DATA__` SSR): Fallback cuando el GraphQL falla; prefiere precio de compra standalone sobre precio EA Play ($0)
- **GOG Catalog API** (`countryCode=AR`): Precios en USD/ARS
- **Xbox/Microsoft storeedgefd API** (`market=AR`, `deviceFamily=Windows.Desktop`): Precios en ARS, validación PC via Xbox website `PlayWith=PC`
- **EA App page scraping** (`ea.com/es/games/...`): Scraping de `__NEXT_DATA__` con estrategia `_next/data` de Next.js
- **Ubisoft Store** (Demandware Product API + scraping): Precio directo via endpoint `Product-Variation?pid={id}&format=ajax`
- **Battle.net Shop** (scraping + `_next/data` API): Precios vía slug de producto con cookies AR

### 3. **Conversión de Moneda**
- **DolarAPI** (`dolarapi.com/v1/dolares/tarjeta`): Cotización del dólar tarjeta en tiempo real
- Toggle **ARS 🇦🇷 / USD 🇺🇸** dentro de cada juego
- Conversión USD→ARS usando dólar tarjeta (incluye IVA + Ganancias)

### 4. **Sección de Ofertas**
- **5 tabs**: Descuentos activos, Favoritos en descuento, Mínimos históricos, Juegos gratuitos (temporales y F2P), **Price Drops** (juegos cuyo precio bajó recientemente)

### 5. **Detalle de Juego**
- Imagen a ancho completo, sección de precios unificada (💰 Precios), toggle ARS/USD, DLCs
- Cards de precio con etiquetas especiales: **✓ Xbox Game Pass**, **✓ EA Play**, countdown de ofertas
- Cards `isVerifiedLink` para tiendas donde el precio no pudo obtenerse (link directo a la tienda)

### 6. **Autenticación y Sesión**
- Registro e inicio de sesión con usuario + contraseña
- **Firebase Authentication** (backend en la nube, Android-compatible)
- **Firebase Firestore** para sincronización completa de preferencias entre dispositivos (ver sección de Sincronización)
- Modo invitado disponible (sin cuenta) — muestra botón "Iniciar sesión" en Configuración
- **Reset de datos al cerrar sesión**: al hacer sign-out (con diálogo de confirmación) o entrar como invitado, se borran favoritos, se resetean todas las preferencias (nombre → "Player", tema → oscuro) y no se cargan datos del usuario anterior

### 7. **Sincronización de Preferencias con Firebase 🔄**
- **Todas las preferencias persisten en Firestore** y se sincronizan entre dispositivos:
  - Lista de favoritos
  - Nombre de usuario (displayName)
  - Email
  - Tema (oscuro/claro)
  - Idioma (en/es)
  - País y código de país
  - Notificaciones globales
- **Al iniciar sesión**: Firestore descarga todas las preferencias y las aplica localmente (DataStore + GameRepository en memoria)
- **Al volver la app al frente (ON_RESUME)**: se re-descarga Firestore para mantener ambos dispositivos sincronizados automáticamente
- **Ante cualquier cambio de preferencia o favorito**: se sube todo a Firestore en un único write atómico (`set + merge`)

### 8. **Notificaciones y Favoritos**
- Pantalla unificada con **2 tabs**: Alertas (notificaciones de precio) y Gestión de favoritos
- **Preferencias de notificación por juego**: toggle individual de Ofertas / Noticias / Mínimo histórico para cada juego favorito
- **Badge de no leídas** en el tab con contador de notificaciones pendientes
- Notificaciones de tipo: precio en oferta, mínimo histórico, noticias de juego

### 9. **Noticias**
- **Steam News API**: noticias reales y actualizadas sobre los juegos del catálogo, con filtro por categoría y favoritos
- **Calendario de ventas próximas**: eventos de descuentos futuros (Steam Summer Sale, etc.) con fecha y descripción

### 10. **Configuración**
- Nombre de usuario, email, selector de país, soporte multi-idioma (English / Español), modo oscuro/claro, notificaciones globales
- **Todas las preferencias se persisten en Firebase Firestore** y se sincronizan entre dispositivos
- Al cerrar sesión o usar modo invitado, todo vuelve a los valores por defecto
- En modo invitado: botón "Iniciar sesión" que redirige al Login

---

## 🌐 APIs Integradas

| API | Uso | Endpoint principal |
|-----|-----|--------------------|
| **Steam Store** | Precios AR + imágenes | `store.steampowered.com/api/appdetails?appids={id}&cc=ar` |
| **Steam News** | Noticias reales de juegos | `api.steampowered.com/ISteamNews/GetNewsForApp/v0002/` |
| **Epic Games GraphQL** | Precios AR + imágenes | `store.epicgames.com/graphql` |
| **Epic product page** | Precios AR (fallback scraping) | `store.epicgames.com/p/{slug}` (`__NEXT_DATA__`) |
| **GOG Catalog** | Precios AR/USD | `catalog.gog.com/v1/catalog?countryCode=AR` |
| **Xbox storeedgefd** | Búsqueda PC + precios AR | `storeedgefd.dsx.mp.microsoft.com/v9.0/search?deviceFamily=Windows.Desktop` |
| **Xbox Display Catalog** | Precios directos por product ID | `displaycatalog.mp.microsoft.com/v7.0/products?bigIds={id}&market=AR` |
| **Xbox PC filter** | Validación disponibilidad en PC | `xbox.com/es-AR/search/results/games?q={q}&PlayWith=PC` |
| **EA App** | Precios AR (Next.js scraping) | `ea.com/es/games/...` + `/_next/data/{buildId}/...json` |
| **Ubisoft Demandware** | Precios AR (API directa) | `store.ubisoft.com/.../Product-Variation?pid={id}&format=ajax` |
| **Battle.net** | Precios AR (scraping + API) | `us.shop.battle.net/es-ar/product/{slug}` + `_next/data` |
| **DolarAPI** | Cotización dólar tarjeta | `dolarapi.com/v1/dolares/tarjeta` |
| **SalesCalendarService** | Calendario de ventas próximas | Curado internamente con fechas de eventos de tiendas |
| **Firebase Auth** | Registro / Login | Firebase Authentication SDK |
| **Firebase Firestore** | Todas las preferencias de usuario | Firebase Firestore SDK (`users/{uid}`) |

---

## 🏗️ Arquitectura (Clean Architecture + MVVM)

### Capa de Presentación
- **Screens**: `GamesScreen`, `GameDetailScreen`, `OffersScreen`, `NewsScreen`, `NewsDetailScreen`, `NotificationsScreen`, `SettingsScreen`, `LoginScreen`, `MainScreen`
- **ViewModels**: `GamesViewModel`, `OffersViewModel`, `NewsViewModel`, `SettingsViewModel`, `AuthViewModel`
- **Components**: `StorePriceCard`, `OfferCountdown`, `TagChips`, `SectionHeader`, `LabeledSwitchRow`, `GameCard`, `DiscountCard`, `NewsCard`, `UpcomingSaleCard`, `LoadingContent`, `CardHeaderImage`

### Capa de Dominio
- **Models**: `Game`, `DLC`, `DiscountedGame`, `News`, `PriceHistory`, `UserSettings`, `AppUser`, `StorePrice`, `InAppNotification`, `GameNotificationPref`
- **Use Cases**: `GetAllGamesUseCase`, `SearchGamesUseCase`, `GetFavoritesUseCase`, `GetCurrentDiscountsUseCase`, `GetPriceDropsUseCase`, `GetInAppNotificationsUseCase`, `GetUnreadNotificationCountUseCase`, `MarkNotificationReadUseCase`, `GenerateDiscountNotificationsUseCase`, `UpdateGameNotificationPrefUseCase`, etc.

### Capa de Datos
- **Price Services**: `SteamPriceService`, `EpicPriceService`, `GogPriceService`, `XboxPriceService`, `UbisoftPriceService`, `BattleNetPriceService`, `EAPriceService`
- **Other Services**: `SteamNewsService`, `SalesCalendarService`, `DolarService`, `ArgentineTaxCalculator`
- **FirebaseAuthService**: Registro, login, sincronización de **todas** las preferencias via Firestore (`syncAllUserData`, `getUserById`)
- **PriceRefreshManager**: Orquesta todas las consultas de precios, cache con Room, filtrado por plataforma, fallbacks `isVerifiedLink`
- **Room Database** (v12): Cache de precios, imágenes, favoritos locales, historial de precios
- **DataStore**: Preferencias de usuario locales (espejo del estado Firestore, recargado en cada resume)
- **GameCatalog**: Catálogo curado de 140 juegos con metadata, slugs verificados, product IDs, URLs por tienda

---

## 🔄 Flujo de Sincronización Firebase

```
┌─────────────────────────────────────────────────────────────────┐
│                        FIRESTORE (users/{uid})                  │
│  displayName · email · favorites · darkMode · languageCode      │
│  country · countryCode · globalNotifications                    │
└────────────────────┬────────────────────┬───────────────────────┘
                     │ download            │ upload
          ┌──────────▼──────────┐  ┌──────▼──────────────────┐
          │  Al iniciar sesión  │  │  Al cambiar cualquier   │
          │  Al volver al frente│  │  preferencia o favorito │
          │  (ON_RESUME)        │  │  → syncAll() en un único│
          └──────────┬──────────┘  │    write atómico        │
                     │             └─────────────────────────┘
          ┌──────────▼──────────────────────────────────────┐
          │         DataStore + GameRepository (local)       │
          │  → settingsViewModel.loadSettings()              │
          │  → gamesViewModel.loadFavorites()                │
          └─────────────────────────────────────────────────┘
```

---

## 🔍 Estrategia de Fetch de Precios por Tienda

### Epic Games
1. **Page scrape** de la URL verificada del catálogo (`__NEXT_DATA__` SSR) — garantiza edición base
2. **GraphQL search** con `category: "games/edition/base"` — evita ediciones Gold/Ultimate
3. **GraphQL search** sin filtro de categoría — fallback amplio
4. **`isVerifiedLink`** — si todo falla pero el slug es conocido, muestra link directo
- Detección EA Play en Epic: si el resultado tiene `originalPrice=0` Y el juego está en plataforma EA, se descarta y se muestra el link (para que el usuario vea el precio de compra, no de suscripción)
- Preferencia de precio no-cero: `findEpicTotalPriceFiltered` busca primero un `totalPrice` con `originalPrice > 0` antes de aceptar un precio cero
- Fallback `isVerifiedLink` garantizado: movido fuera del try/catch para que siempre se ejecute aunque ocurra una excepción en los intentos anteriores

### Xbox / Microsoft Store
- Búsqueda via `storeedgefd` con `deviceFamily=Windows.Desktop`
- **Validación PC obligatoria**: cross-check contra `xbox.com/es-AR/search?PlayWith=PC` — si el product ID no aparece en la página SSR del buscador con filtro PC, el juego es exclusivo de consola y se descarta
- Para juegos con product ID conocido: consulta directa a `displaycatalog.mp.microsoft.com`
- URLs sin sufijo `/0010` (evita 404 para productos sin ese SKU)

### EA App
- Scraping de `ea.com/es/games/{slug}` con extracción de `__NEXT_DATA__` vía string search
- Estrategia `_next/data`: intenta 4 variantes de URL (`es-ar/buy`, `es/buy`, `es-ar`, `es`)
- Fallback `isVerifiedLink` con URL verificada del catálogo

### Ubisoft
- **Demandware Product API**: extrae el ID de 24 caracteres de la URL del catálogo, llama a `Product-Variation?pid={id}&format=ajax`
- Fallback: scraping de meta tags, JSON-LD, scripts en la página
- Fallback final: `isVerifiedLink`

### Battle.net
- Scraping de `us.shop.battle.net/es-ar/product/{slug}` con cookies AR
- Estrategia `_next/data` con buildId dinámico
- Fallback `isVerifiedLink`

---

## 📁 Estructura del Proyecto

```
app/src/main/java/.../
├── data/
│   ├── api/              # 7 price services + DolarService + SteamNewsService + SalesCalendarService + PriceRefreshManager + ArgentineTaxCalculator
│   ├── catalog/          # GameCatalog.kt (140 juegos: slugs, product IDs, URLs verificadas)
│   ├── local/            # Room DB v12 (precios, imágenes, favoritos, historial) + DataStore
│   ├── remote/
│   │   └── FirebaseAuthService.kt   # Firebase Auth + Firestore (todas las preferencias)
│   └── repository/       # Implementaciones de repositorios
├── domain/
│   ├── model/            # Game, AppUser, StorePrice, DiscountedGame, News, InAppNotification, GameNotificationPref, ...
│   ├── repository/       # Interfaces
│   └── usecase/          # Casos de uso
├── presentation/
│   ├── screen/           # 9 pantallas (GamesScreen, OffersScreen, NotificationsScreen, NewsScreen, SettingsScreen, GameDetailScreen, NewsDetailScreen, LoginScreen, MainScreen)
│   ├── viewmodel/        # GamesViewModel, AuthViewModel, SettingsViewModel, OffersViewModel, NewsViewModel
│   └── component/        # Componentes reutilizables
├── ui/theme/             # Material 3 Dark Theme
├── MainActivity.kt       # Entrada: auth state → MainScreen o LoginScreen; lifecycle observer ON_RESUME → refreshFromFirebase()
└── GameTrackerApp.kt
```

---

## 🛠️ Tecnologías

- **Kotlin** + **Jetpack Compose**
- **Firebase Authentication** (login/registro en la nube)
- **Firebase Firestore** (sync completo de preferencias entre dispositivos: favoritos, tema, nombre, idioma, país, notificaciones)
- **Room** v12 (cache local de precios, imágenes, favoritos, historial)
- **DataStore** (espejo local de preferencias Firestore; se recarga en cada resume y login)
- **Coil** (carga de imágenes desde Steam CDN y Epic Store)
- **Kotlinx Serialization** (JSON parsing)
- **HttpURLConnection** (API calls a tiendas, scraping de páginas Next.js)
- **Material Design 3** (dark theme)
- **Coroutines + StateFlow** (MVVM reactivo, fetch de precios en paralelo con `async`)
- **Android Speech-to-Text** (búsqueda por voz en el catálogo)

---

## 🔧 Cómo Ejecutar

1. Clonar el repositorio
2. Abrir en Android Studio
3. Colocar `google-services.json` en la carpeta `app/` (obtener desde [Firebase Console](https://console.firebase.google.com) → proyecto ArgenGamer → configuración Android)
4. En Firebase Console: activar **Authentication → Email/Password** y crear **Firestore Database** con nombre `argengamer`
5. `Gradle Sync` → Conectar dispositivo o emulador → Ejecutar

---

## 🧪 Tests

```bash
./gradlew testDebugUnitTest
```

Tests incluidos: StorePrice, ArgentineTaxCalculator, EpicDataClasses, GameCatalog, domain models, use cases, repositorios, ViewModels.

---

## 📊 Estrategia de Caché y Refresh

1. **On demand** (al abrir un juego): siempre busca precios frescos online; cache si offline
2. **Background batch**: cada 15min para ofertas, cada 1h para catálogo general (batches de 5 juegos)
3. **Filtrado por plataforma**: cada juego sólo consulta las tiendas donde está disponible (reduce llamadas innecesarias y evita falsos positivos)
4. **`isVerifiedLink`**: cuando el precio no puede obtenerse pero la URL es conocida, se muestra un card con link directo a la tienda (no se cachea en DB)
5. **Detección EA Play**: Steam precio=0 + plataforma EA → marcado como `isEaPlay` (muestra "✓ EA Play" en lugar de "$0")
6. **Detección Game Pass**: Xbox precio=0 con strikethrough → marcado como `isGamePass` (muestra "✓ Xbox Game Pass")

---

## 👨‍💻 Equipo

Avalos, Ko, Ortuzar, Vargas

---

**Versión**: 7.0
**Última actualización**: Julio 2026
