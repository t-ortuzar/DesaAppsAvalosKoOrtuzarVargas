# Game Tracker App

Una aplicación Android moderna para trackear precios de videojuegos en múltiples tiendas digitales, con precios reales para Argentina y soporte para juegos Free-to-Play.

## 🎮 Características Principales

### 1. **Catálogo de Juegos**
- **114 juegos**: 100 juegos pagos + 14 Free-to-Play (League of Legends, Valorant, Fortnite, CS2, Dota 2, etc.)
- Imágenes reales vía **Steam CDN** para juegos en Steam
- **Imágenes dinámicas desde Epic Games Store** (GraphQL `keyImages`) para juegos no disponibles en Steam (ej. Alan Wake 2)
- Búsqueda por nombre y filtro por tags (Action, RPG, Horror, Free2Play, etc.)
- Sistema de favoritos
- Tags: 27+ categorías incluyendo Free2Play, MOBA, Battle Royale

### 2. **Precios Reales para Argentina 🇦🇷**
- **Steam Store API** (`cc=ar`): Precios regionales en ARS fijados por publishers
- **Epic Games Store GraphQL API** (`country=AR`): Precios en ARS
- **GOG Catalog API** (`countryCode=AR`): Precios en USD/ARS
- **Xbox/Microsoft displaycatalog API** (`market=AR`): Precios en ARS
- **EA Origin API** (`AR/es_AR`): Precios regionales
- **Ubisoft Store API** (`es_AR`): Precios regionales
- **Battle.net Shop API** (`country=AR`): Precios regionales

### 3. **Conversión de Moneda**
- **DolarAPI** (`dolarapi.com/v1/dolares/tarjeta`): Cotización del dólar tarjeta en tiempo real
- Toggle **ARS 🇦🇷 / USD 🇺🇸** dentro de cada juego para alternar la moneda de visualización
- Conversión USD→ARS usando dólar tarjeta (incluye IVA + Ganancias)
- Los precios de Steam/Epic ya vienen en ARS regional (no requieren conversión)

### 4. **Sección de Ofertas**
- **Descuentos**: 50+ descuentos de juegos pagos
- **F2P**: 23 juegos Free-to-Play permanentes
- **Gratis temporal**: 20 juegos temporalmente gratis (con fecha de expiración)
- **Mínimos históricos**: Descuentos ≥70%
- **Favoritos en descuento**: Solo descuentos de juegos favoritos

### 5. **Detalle de Juego**
- Imagen a ancho completo (sin recorte)
- **Fondo oscuro** consistente con el tema de la app
- **Sección de precios unificada** (💰 Precios): precios live de APIs + precios de referencia del catálogo, todo bajo una sola categoría
- Toggle ARS/USD con cotización del dólar tarjeta
- DLCs & Expansiones con precios que respetan el toggle de moneda
- Plataformas disponibles, tags, rating, descripción

### 6. **Juegos Free-to-Play**
- 14 juegos F2P en el catálogo principal (no solo en ofertas)
- Muestran "Disponible en [plataforma]" en vez de precios
- Plataformas específicas: Riot Games, Epic Games, Steam, HoYoverse, EA

### 7. **Noticias**
- 60 noticias sobre descuentos, actualizaciones y eventos
- Filtro por categoría y por favoritos
- Detalle de noticia con fondo oscuro

### 8. **Configuración**
- Nombre de usuario y email editables
- Selector de país con banderas emoji (Argentina por defecto)
- Soporte **multi-idioma** (English / Español) con cambio dinámico
- Notificaciones globales y por juego favorito

## 🌐 APIs Integradas

| API | Uso | Endpoint |
|-----|-----|----------|
| **Steam Store** | Precios regionales AR + imágenes | `store.steampowered.com/api/appdetails` |
| **Epic Games GraphQL** | Precios AR + imágenes (`keyImages`) | `graphql.epicgames.com/graphql` |
| **GOG Catalog** | Precios AR/USD | `catalog.gog.com/v1/catalog` |
| **Xbox/Microsoft** | Precios AR para PC | `displaycatalog.mp.microsoft.com/v7.0/products` |
| **EA Origin** | Precios AR | `api2.origin.com/ecommerce2/public/supercat/AR` |
| **Ubisoft Store** | Precios AR | `store.ubisoft.com/.../es_AR/Search-GetSuggestions` |
| **Battle.net** | Precios AR | `us.shop.battle.net/api/catalog?country=AR` |
| **DolarAPI** | Cotización dólar tarjeta | `dolarapi.com/v1/dolares/tarjeta` |
| **Steam CDN** | Imágenes de juegos | `cdn.akamai.steamstatic.com/steam/apps/{id}/header.jpg` |

## 🏗️ Arquitectura (Clean Architecture + MVVM)

### Capa de Presentación
- **Screens**: `GamesScreen`, `GameDetailScreen`, `OffersScreen`, `NewsScreen`, `NewsDetailScreen`, `FavoritesScreen`, `SettingsScreen`, `MainScreen`
- **Components**: `GameCard`, `DiscountCard`, `NewsCard`, `SharedComponents` (9 componentes reutilizables)
- **ViewModels**: `GamesViewModel`, `OffersViewModel`, `NewsViewModel`, `SettingsViewModel`

### Capa de Dominio
- **Models**: `Game`, `DLC`, `DiscountedGame`, `News`, `PriceHistory`, `UserSettings`, `InAppNotification`
- **Use Cases**: `GetAllGamesUseCase`, `SearchGamesUseCase`, `GetFavoritesUseCase`, `GetCurrentDiscountsUseCase`, etc.
- **Repositories** (interfaces): `GameRepository`, `NewsRepository`, `DiscountRepository`, `UserSettingsRepository`

### Capa de Datos
- **Price Services**: `SteamPriceService`, `EpicPriceService`, `GogPriceService`, `XboxPriceService`, `UbisoftPriceService`, `BattleNetPriceService`, `EAPriceService`
- **DolarService**: Cotización del dólar tarjeta desde DolarAPI
- **ArgentineTaxCalculator**: Conversión USD→ARS con dólar tarjeta
- **PriceRefreshManager**: Gestión de cache de precios con Room + refresh periódico
- **ConnectivityObserver**: Detección de conectividad para estrategia online/offline
- **Room Database**: Cache de precios, imágenes, favoritos
- **DataStore**: Persistencia de configuración de usuario
- **GameCatalog**: Catálogo curado de 114 juegos con metadata

## 📁 Estructura del Proyecto

```
app/src/main/java/com/example/desaappsavaloskoortuzarvargas/
├── data/
│   ├── api/
│   │   ├── SteamPriceService.kt         # Steam Store API (precios AR)
│   │   ├── EpicPriceService.kt          # Epic GraphQL API (precios + imágenes)
│   │   ├── GogPriceService.kt           # GOG Catalog API
│   │   ├── XboxPriceService.kt          # Microsoft Store API
│   │   ├── EAPriceService.kt            # EA/Origin API
│   │   ├── UbisoftPriceService.kt       # Ubisoft Store API
│   │   ├── BattleNetPriceService.kt     # Battle.net Shop API
│   │   ├── DolarService.kt             # DolarAPI (cotización)
│   │   ├── ArgentineTaxCalculator.kt    # Conversión USD→ARS
│   │   ├── PriceRefreshManager.kt       # Cache + refresh periódico
│   │   └── StorePrice.kt               # Modelo unificado de precio
│   ├── catalog/
│   │   └── GameCatalog.kt              # 114 juegos con metadata
│   ├── local/
│   │   ├── GameTrackerDatabase.kt       # Room DB
│   │   ├── ConnectivityObserver.kt      # Estado de red
│   │   ├── SettingsDataStore.kt         # Preferencias de usuario
│   │   ├── dao/                         # DAOs
│   │   └── entity/                      # Room entities
│   └── repository/                      # Implementaciones de repositorios
├── domain/
│   ├── model/                           # Entidades de dominio
│   ├── repository/                      # Interfaces
│   └── usecase/                         # Casos de uso
├── presentation/
│   ├── component/                       # Componentes UI reutilizables
│   ├── screen/                          # 8 pantallas
│   ├── viewmodel/                       # 4 ViewModels
│   └── Constants.kt                     # Colores y constantes
├── di/
│   └── ServiceLocator.kt               # Inyección de dependencias
├── ui/theme/                            # Material 3 Dark Theme
├── MainActivity.kt
└── GameTrackerApp.kt
```

## 🎨 Diseño

- **Tema oscuro** en toda la app (incluyendo pantallas de detalle)
- **Material Design 3** con `darkColorScheme`
- Fondo `#121212`, superficie `#1E1E1E`, texto blanco
- Navegación inferior con 4 tabs + badge de notificaciones
- Multi-idioma: English / Español

## 🛠️ Tecnologías

- **Kotlin** + **Jetpack Compose**
- **Room** (cache de precios e imágenes)
- **DataStore** (preferencias de usuario)
- **Coil** (carga de imágenes)
- **Kotlinx Serialization** (JSON parsing)
- **HttpURLConnection** (API calls)
- **Material Design 3** (dark theme)
- **Coroutines + StateFlow** (MVVM reactivo)

## 🧪 Tests

```bash
./gradlew testDebugUnitTest
```

### Tests Incluidos
- **StorePriceTest**: StorePrice, SteamGamePrice (ARS, USD, free, imageUrl)
- **ArgentineTaxCalculatorTest**: usdToArs, formatArs, calculateBreakdown
- **EpicDataClassesTest**: EpicKeyImage, EpicElement, EpicTotalPrice, response chain
- **GameCatalogTest**: 114 juegos, F2P defaults, images, DLCs, discounts, news, price history
- **GameModelTest, DiscountModelTest, NewsModelTest, UserSettingsTest**: Modelos de dominio
- **GameUseCaseTest, DiscountUseCaseTest, NewsUseCaseTest, SettingsUseCaseTest**: Use cases
- **GameRepositoryImplTest, DiscountRepositoryImplTest, NewsRepositoryImplTest**: Repositorios
- **GamesViewModelTest, OffersViewModelTest, NewsViewModelTest, SettingsViewModelTest**: ViewModels
- **ConstantsTest**: Constantes de la app

## 📊 Estrategia de Precios

1. **Online**: Siempre busca precios frescos de las APIs (sin shortcut de cache stale)
2. **Offline**: Muestra precios cacheados con warning visual
3. **Fallback**: Si la API falla, usa cache sin warning
4. **Refresh periódico**: Ofertas cada 30min, catálogo cada 2h, en batches de 5 juegos

## 🖼️ Estrategia de Imágenes

1. **Juegos en Steam** → Steam CDN usando Steam App ID (prioridad)
2. **Juegos no en Steam** → Imagen dinámica desde Epic Games Store GraphQL API (`keyImages`)
3. **Juegos Riot** → DDragon CDN
4. **Fallback estático** → URLs hardcodeadas en `alternativeImages`

## 🔧 Cómo Ejecutar

1. Clona el repositorio
2. Abre en Android Studio
3. `Gradle Sync`
4. Conecta dispositivo o emulador
5. Ejecuta la app

## 👨‍💻 Equipo

Avalos, Ko, Ortuzar, Vargas

---

**Versión**: 4.0
**Última actualización**: Mayo 2026
