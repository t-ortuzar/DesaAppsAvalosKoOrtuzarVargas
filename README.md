# ArgenGamer

Una aplicación Android moderna para trackear precios de videojuegos en múltiples tiendas digitales, con precios reales para Argentina y soporte para juegos Free-to-Play.

## 🎮 Características Principales

### 1. **Catálogo de Juegos**
- **140+ juegos**: pagos + 14 Free-to-Play (League of Legends, Valorant, Fortnite, CS2, Dota 2, etc.)
- Imágenes reales vía **Steam CDN** para juegos en Steam
- **Imágenes dinámicas desde Epic Games Store** (GraphQL `keyImages`) para juegos exclusivos de Epic (ej. Alan Wake 2)
- Búsqueda por nombre y filtro por tags (Action, RPG, Horror, Free2Play, etc.)
- Sistema de favoritos persistido en Room DB
- Tags: 27+ categorías incluyendo Free2Play, MOBA, Battle Royale
- Plataformas correctamente mapeadas por juego: cada juego sólo consulta las tiendas donde realmente está disponible

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
- Descuentos activos, juegos F2P, gratis temporales, mínimos históricos, favoritos en descuento

### 5. **Detalle de Juego**
- Imagen a ancho completo, sección de precios unificada (💰 Precios), toggle ARS/USD, DLCs
- Cards de precio con etiquetas especiales: **✓ Xbox Game Pass**, **✓ EA Play**, countdown de ofertas
- Cards `isVerifiedLink` para tiendas donde el precio no pudo obtenerse (link directo a la tienda)

### 6. **Autenticación y Sesión**
- Registro e inicio de sesión con usuario + contraseña
- **Firebase Authentication** (backend en la nube, Android-compatible)
- **Firebase Firestore** para sincronización de favoritos entre dispositivos
- Modo invitado disponible (sin cuenta) — no muestra botón de cerrar sesión
- **Reset de datos al cerrar sesión**: al hacer sign-out o entrar como invitado, se borran favoritos (Room DB), se resetean preferencias (DataStore: nombre → "Player", tema → oscuro) y no se cargan datos del usuario anterior

### 7. **Noticias**
- 60 noticias sobre descuentos, actualizaciones y eventos. Filtro por categoría y favoritos.

### 8. **Configuración**
- Nombre de usuario, email, selector de país, soporte multi-idioma (English / Español), modo oscuro/claro, notificaciones
- Las preferencias persisten mientras la sesión está activa
- Al cerrar sesión o usar modo invitado, todo vuelve a los valores por defecto

---

## 🌐 APIs Integradas

| API | Uso | Endpoint principal |
|-----|-----|--------------------|
| **Steam Store** | Precios AR + imágenes | `store.steampowered.com/api/appdetails?appids={id}&cc=ar` |
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
| **Firebase Auth** | Registro / Login | Firebase Authentication SDK |
| **Firebase Firestore** | Perfil de usuario + favoritos | Firebase Firestore SDK |

---

## 🏗️ Arquitectura (Clean Architecture + MVVM)

### Capa de Presentación
- **Screens**: `GamesScreen`, `GameDetailScreen`, `OffersScreen`, `NewsScreen`, `NewsDetailScreen`, `FavoritesScreen`, `SettingsScreen`, `LoginScreen`, `MainScreen`, `NotificationsScreen`
- **ViewModels**: `GamesViewModel`, `OffersViewModel`, `NewsViewModel`, `SettingsViewModel`, `AuthViewModel`
- **Components**: `StorePriceCard`, `OfferCountdown`, `TagChips`, `SectionHeader`, `LabeledSwitchRow`

### Capa de Dominio
- **Models**: `Game`, `DLC`, `DiscountedGame`, `News`, `PriceHistory`, `UserSettings`, `AppUser`, `StorePrice`
- **Use Cases**: `GetAllGamesUseCase`, `SearchGamesUseCase`, `GetFavoritesUseCase`, `GetCurrentDiscountsUseCase`, etc.

### Capa de Datos
- **Price Services**: `SteamPriceService`, `EpicPriceService`, `GogPriceService`, `XboxPriceService`, `UbisoftPriceService`, `BattleNetPriceService`, `EAPriceService`
- **FirebaseAuthService**: Registro, login y sync de favoritos via Firebase
- **PriceRefreshManager**: Orquesta todas las consultas de precios, cache con Room, filtrado por plataforma, fallbacks `isVerifiedLink`
- **Room Database** (v12): Cache de precios, imágenes, favoritos locales, historial de precios
- **DataStore**: Preferencias de usuario (nombre, tema, idioma, país, notificaciones)
- **GameCatalog**: Catálogo curado de 140+ juegos con metadata, slugs verificados, product IDs, URLs por tienda

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
│   ├── api/              # 7 price services + DolarService + PriceRefreshManager + StorePrice
│   ├── catalog/          # GameCatalog.kt (140+ juegos: slugs, product IDs, URLs verificadas)
│   ├── local/            # Room DB v12 (precios, imágenes, favoritos, historial) + DataStore
│   ├── remote/
│   │   └── FirebaseAuthService.kt   # Firebase Auth + Firestore
│   └── repository/       # Implementaciones de repositorios
├── domain/
│   ├── model/            # Game, AppUser, StorePrice, DiscountedGame, News, ...
│   ├── repository/       # Interfaces
│   └── usecase/          # Casos de uso
├── presentation/
│   ├── screen/           # 10 pantallas (+ NotificationsScreen)
│   ├── viewmodel/        # GamesViewModel, AuthViewModel, SettingsViewModel, ...
│   └── component/        # Componentes reutilizables
├── ui/theme/             # Material 3 Dark Theme
├── MainActivity.kt       # Entrada: auth state → MainScreen o LoginScreen
└── GameTrackerApp.kt
```

---

## 🛠️ Tecnologías

- **Kotlin** + **Jetpack Compose**
- **Firebase Authentication** (login/registro en la nube)
- **Firebase Firestore** (sync de favoritos entre dispositivos)
- **Room** v12 (cache local de precios, imágenes, favoritos, historial)
- **DataStore** (preferencias de usuario, reset en sign-out)
- **Coil** (carga de imágenes)
- **Kotlinx Serialization** (JSON parsing)
- **HttpURLConnection** (API calls a tiendas, scraping de páginas Next.js)
- **Material Design 3** (dark theme)
- **Coroutines + StateFlow** (MVVM reactivo, fetch de precios en paralelo con `async`)

---

## 🔧 Cómo Ejecutar

1. Clonar el repositorio
2. Abrir en Android Studio
3. Colocar `google-services.json` en la carpeta `app/` (obtener desde [Firebase Console](https://console.firebase.google.com) → proyecto ArgenGamer → configuración Android)
4. En Firebase Console: activar **Authentication → Email/Password** y crear **Firestore Database**
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

**Versión**: 6.0
**Última actualización**: Junio 2026
