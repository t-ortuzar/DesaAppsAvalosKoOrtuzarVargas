# Game Tracker App - Demo

Una aplicación Android moderna para trackear descuentos de videojuegos en múltiples plataformas como Steam, Epic Games Store, GOG, EA Play, Ubisoft+, Battle.net, G2A y Eneba.

## 🎮 Características Principales

### 1. **Catálogo de Juegos**
- Visualiza un catálogo de 100 juegos populares (sin F2P)
- Imágenes reales de cada juego vía **Steam CDN** (`cdn.akamai.steamstatic.com`) con fallback a CDNs alternativos (ej. `gaming-cdn.com` para exclusivos de Epic)
- Cada juego muestra:
  - Portada/Imagen real del juego
  - Título y descripción
  - Calificación (rating)
  - Tags del juego (Action, RPG, Horror, etc.)
  - Descuento histórico más grande
  - Precios actuales en diferentes plataformas
- Busca juegos por nombre
- Filtra por tags
- Marca juegos como favoritos

### 2. **Sección de Noticias**
- Visualiza noticias actualizadas sobre descuentos y actualizaciones
- Filtra noticias por:
  - Todas las noticias
  - Noticias de tus juegos favoritos
  - Noticias por juego específico
- Categorías de noticias:
  - Descuentos
  - Actualizaciones
  - Estrenos
  - Eventos

### 3. **Sección de Ofertas/Descuentos**
- **Todos los Descuentos**: Muestra todos los descuentos actuales ordenados por porcentaje
- **Descuentos en Favoritos**: Solo descuentos de tus juegos favoritos
- **Mínimos Históricos**: Juegos con descuento histórico más grande (70% o más)
- **Juegos Gratis**: Al menos 20 juegos que están gratis en diferentes plataformas
- Información de descuento:
  - Precio original vs precio actual
  - Porcentaje de descuento
  - Plataforma
  - Indicador de mínimo histórico

### 4. **Configuración (Settings)**
- Nombre de usuario y email editables
- **Selector de país/región** con banderas emoji generadas dinámicamente (Unicode Regional Indicators)
  - Argentina 🇦🇷 (por defecto), Brasil 🇧🇷, Chile 🇨🇱, Colombia 🇨🇴, México 🇲🇽, Estados Unidos 🇺🇸, España 🇪🇸, Uruguay 🇺🇾, Perú 🇵🇪, Paraguay 🇵🇾
- Notificaciones globales y por juego
- Preferencias de notificación granulares:
  - Ofertas
  - Noticias
  - Mínimos históricos

### 5. **Detalle de Juego**
- Portada a tamaño completo
- Información completa del juego
- **DLCs & Expansiones**: Cards compactas (solo texto, sin imagen) mostrando nombre, descripción y precio
- Precios en todas las plataformas disponibles
- Botón de favoritos

### 6. **Notificaciones In-App**
- Campana de notificaciones en el header
- Badge con contador de no leídas
- Tipos: descuentos, mínimos históricos, noticias, juegos gratis
- Panel deslizable con historial de notificaciones

## 🌐 Integración con APIs

### **CheapShark API**
- Búsqueda de juegos y deals reales
- Top deals actuales
- Precios reales de tiendas digitales
- **Nota**: Los precios provienen de tiendas de EE.UU. (CheapShark no soporta pricing regional nativamente). El selector de país permite personalizar la experiencia pero los precios mostrados son en USD de tiendas norteamericanas.

### **Steam CDN**
- Imágenes de portada: `https://cdn.akamai.steamstatic.com/steam/apps/{STEAM_APP_ID}/header.jpg`
- IDs de Steam verificados manualmente para los 100 juegos
- Fallback a CDNs alternativos para juegos no disponibles en Steam (ej. Alan Wake 2 → `gaming-cdn.com`)

## 🏗️ Arquitectura (Clean Architecture)

La aplicación sigue el patrón de **Clean Architecture** dividida en 3 capas:

### **Capa de Presentación (Presentation Layer)**
- **Components**: Composables reutilizables
  - `GameCard`: Tarjeta para mostrar un juego
  - `NewsCard`: Tarjeta para mostrar noticia
  - `DiscountCard`: Tarjeta para mostrar descuento
  - `SharedComponents.kt`: 9 componentes extraídos para reutilización:
    - `CardHeaderImage`: Imagen header reutilizable para cards
    - `LoadingContent<T>`: Handler genérico de estados loading/empty/content
    - `SettingsCard`: Wrapper consistente para secciones de settings
    - `DetailSection`: Sección título+valor para pantallas de detalle
    - `SectionHeader`: Título de sección en negrita
    - `TagChips`: Chips de tags con scroll horizontal
    - `PriceBadge`: Badge coloreado para precios/descuentos
    - `LabeledSwitchRow`: Fila con label + switch
    - `FavoriteButton`: Botón de favoritos reutilizable
  
- **Constants**: Constantes centralizadas
  - `AppColors`: Colores de la app (F2PBlue, FreeGreen, HistoricalGold, UrgentOrange)
  - `POPULAR_TAGS`: Lista de tags populares
  - `STORE_PLATFORMS`: Lista de plataformas de tiendas
  
- **Screens**: Pantallas principales
  - `GamesScreen`: Catálogo de juegos con búsqueda y filtros por tag
  - `NewsScreen`: Sección de noticias
  - `OffersScreen`: Sección de ofertas/descuentos (4 tabs)
  - `GameDetailScreen`: Detalle de un juego con DLCs compactos
  - `NewsDetailScreen`: Detalle de una noticia
  - `SettingsScreen`: Configuración de usuario, país y notificaciones
  - `MainScreen`: Pantalla principal con navegación inferior (4 tabs)
  
- **ViewModels**: Lógica de presentación
  - `GamesViewModel`: Gestiona estado de juegos y favoritos
  - `NewsViewModel`: Gestiona estado de noticias
  - `OffersViewModel`: Gestiona estado de descuentos
  - `SettingsViewModel`: Gestiona configuración de usuario y notificaciones

### **Capa de Dominio (Domain Layer)**
- **Models**: Entidades de dominio
  - `Game`: Representa un videojuego
  - `DLC`: Representa un DLC/expansión
  - `News`: Representa una noticia
  - `DiscountedGame`: Representa un juego en descuento
  - `PriceHistory`: Historial de precios
  - `UserSettings`: Configuración del usuario (nombre, email, país, countryCode, notificaciones)
  - `CountryInfo`: Info de país (name, code, steamCc, currency)
  - `InAppNotification`: Notificación in-app
  - `GameNotificationPref`: Preferencias de notificación por juego
  
- **Repositories (Interfaces)**: Contratos para acceso a datos
  - `GameRepository`
  - `NewsRepository`
  - `DiscountRepository`
  - `UserSettingsRepository`
  
- **Use Cases**: Casos de uso de negocio
  - `GetAllGamesUseCase`
  - `AddToFavoritesUseCase`
  - `GetCurrentDiscountsUseCase`
  - `UpdateCountryUseCase`
  - `UpdateNotificationPrefsUseCase`
  - etc.

### **Capa de Datos (Data Layer)**
- **API**: Servicios externos
  - `CheapSharkService`: Cliente HTTP para CheapShark API (búsqueda, deals, precios)
  
- **Database (Room)**: Base de datos local
  - Entidades: `GameEntity`, `NewsEntity`, `DiscountEntity`, `FavoriteEntity`, `PriceHistoryEntity`
  - DAOs: Acceso a datos
  - `AppDatabase`: Base de datos SQLite
  
- **Repositories (Implementations)**: Implementación de interfaces
  - `GameRepositoryImpl`
  - `NewsRepositoryImpl`
  - `DiscountRepositoryImpl`
  - `UserSettingsRepositoryImpl`
  
- **Mock Data**: Generador de datos de demostración
  - 100 juegos populares con Steam App IDs verificados
  - DLCs y expansiones para juegos principales
  - 50+ descuentos
  - 20+ juegos gratis
  - Historial de precios
  - Noticias relacionadas
  - Sistema de tags (27 tags disponibles)

## 🎨 Diseño

- **Tema oscuro** por defecto
- **Material Design 3**
- Navegación inferior con 4 tabs: Ofertas, Catálogo, Noticias, Configuración
- Banderas de país generadas con Unicode Regional Indicator Symbols (sin dependencia de imágenes)

## 🛠️ Tecnologías Utilizadas

- **Kotlin**: Lenguaje de programación
- **Jetpack Compose**: UI Framework
- **Hilt**: Inyección de dependencias
- **Room**: Base de datos local
- **HttpURLConnection**: Cliente HTTP para CheapShark API
- **Kotlinx Serialization**: Serialización JSON
- **Coil**: Carga de imágenes desde URL (Steam CDN, gaming-cdn.com)
- **Material Design 3**: Diseño UI con tema oscuro
- **JaCoCo**: Reporte de cobertura de tests
- **Mockito-Kotlin**: Mocking para tests unitarios
- **Kotlinx Coroutines Test**: Testing de coroutines

## 📁 Estructura del Proyecto

```
app/src/main/java/com/example/desaappsavaloskoortuzarvargas/
├── data/
│   ├── api/
│   │   └── CheapSharkService.kt          # API client + data classes + StoreRegionAvailability
│   ├── local/
│   │   └── SettingsDataStore.kt           # DataStore para persistencia de settings
│   ├── mock/
│   │   └── MockDataGenerator.kt           # Generador de datos de demo
│   └── repository/                        # Implementaciones de repositorios
├── domain/
│   ├── model/                             # Entidades de dominio
│   ├── repository/                        # Interfaces de repositorios
│   └── usecase/                           # Use cases
├── presentation/
│   ├── component/
│   │   ├── GameCard.kt
│   │   ├── NewsCard.kt
│   │   ├── DiscountCard.kt
│   │   └── SharedComponents.kt            # 9 componentes reutilizables extraídos
│   ├── screen/                            # 7 pantallas
│   ├── viewmodel/                         # 4 ViewModels
│   └── Constants.kt                       # Colores y constantes centralizadas
├── di/
│   └── ServiceLocator.kt                  # Inyección de dependencias
├── MainActivity.kt
└── GameTrackerApp.kt

app/src/test/java/com/example/desaappsavaloskoortuzarvargas/
├── data/
│   ├── api/
│   │   ├── CheapSharkServiceTest.kt       # Tests de getStoreName + data classes
│   │   ├── CheapSharkDataClassesTest.kt   # Tests de defaults de data classes
│   │   └── StoreRegionAvailabilityTest.kt # Tests de disponibilidad regional
│   ├── mock/
│   │   └── MockDataGeneratorTest.kt       # Tests de generación de datos
│   └── repository/
│       ├── GameRepositoryImplTest.kt      # Tests de repositorio de juegos
│       ├── DiscountRepositoryImplTest.kt  # Tests de repositorio de descuentos
│       └── NewsRepositoryImplTest.kt      # Tests de repositorio de noticias
├── domain/
│   ├── model/
│   │   ├── UserSettingsTest.kt            # Tests de modelos de settings
│   │   ├── GameModelTest.kt              # Tests de Game, DLC, PriceHistory
│   │   ├── DiscountModelTest.kt          # Tests de DiscountedGame
│   │   └── NewsModelTest.kt             # Tests de News
│   └── usecase/
│       ├── GameUseCaseTest.kt            # Tests de use cases de juegos
│       ├── DiscountUseCaseTest.kt        # Tests de use cases de descuentos
│       ├── NewsUseCaseTest.kt            # Tests de use cases de noticias
│       └── SettingsUseCaseTest.kt        # Tests de use cases de settings
└── presentation/
    ├── ConstantsTest.kt                  # Tests de constantes
    └── viewmodel/
        ├── GamesViewModelTest.kt         # Tests de GamesViewModel
        ├── OffersViewModelTest.kt        # Tests de OffersViewModel
        ├── NewsViewModelTest.kt          # Tests de NewsViewModel
        └── SettingsViewModelTest.kt      # Tests de SettingsViewModel
```

## 🚀 Funcionalidades por Pantalla

### **Pantalla de Catálogo (Catalog)**
- Lista de 100 juegos más populares
- Barra de búsqueda
- Filtro por tags (Action, RPG, Horror, Open World, etc.)
- Cada tarjeta muestra:
  - Portada real del juego (Steam CDN)
  - Título
  - Rating ⭐
  - Tags del juego
  - 2 primeras plataformas con precios
  - Botón de favoritos ❤️
- Click en tarjeta → Ver detalle

### **Pantalla de Noticias (News)**
- Filtros por tipo de noticia
- Cada noticia muestra:
  - Miniatura de imagen
  - Título de noticia
  - Resumen del contenido
  - Plataforma y fecha
- Click en noticia → Ver detalle completo

### **Pantalla de Ofertas (Offers)**
- 4 tabs:
  1. **All Discounts**: Todos los descuentos actuales
  2. **Favorite Discounts**: Descuentos de juegos favoritos
  3. **Historical Low**: Mínimos históricos (> 70%)
  4. **Free**: Juegos gratis (mín. 20 juegos)
- Cada tarjeta muestra:
  - Portada del juego
  - Badge de descuento (-X%) o "FREE"
  - Precio original tachado
  - Precio actual en verde
  - Indicador de mínimo histórico ⭐

### **Pantalla de Configuración (Settings)**
- Edición de perfil (nombre, email)
- Selector de país con banderas emoji 🇦🇷🇧🇷🇨🇱🇨🇴🇲🇽🇺🇸🇪🇸🇺🇾🇵🇪🇵🇾
- Toggle de notificaciones globales
- Configuración de notificaciones por juego favorito

### **Detalle de Juego**
- Portada a tamaño completo
- Información completa:
  - Fecha de lanzamiento
  - Rating detallado
  - Tags
  - Descripción completa
  - Precios en todas las plataformas
  - Descuento histórico
- **DLCs & Expansiones**: Sección colapsable con cards compactas (texto only)
- Botón de favoritos en header

### **Navegación Inferior (Bottom Navigation)**
- 4 tabs principales: Ofertas, Catálogo, Noticias, Settings
- Icons y labels
- Campana de notificaciones con badge

## 📊 Datos de Demo

### **100 Juegos Populares (sin F2P)**
Algunos ejemplos:
- Elden Ring
- Baldur's Gate 3
- The Witcher 3: Wild Hunt
- Cyberpunk 2077
- Final Fantasy XVI
- Starfield
- Alan Wake 2
- Hades
- Dead Cells
- Y 90 más...

### **Juegos Gratis (20+)**
- Dota 2
- Team Fortress 2
- Warframe
- Path of Exile
- Lost Ark
- Apex Legends
- Valorant
- Counter-Strike 2
- Y más...

### **Plataformas Incluidas**
- Steam
- Epic Games Store
- GOG
- EA Play
- Ubisoft+
- Battle.net
- G2A
- Eneba

### **Países Soportados**
| País | Código | Bandera |
|------|--------|---------|
| Argentina | AR | 🇦🇷 |
| Brasil | BR | 🇧🇷 |
| Chile | CL | 🇨🇱 |
| Colombia | CO | 🇨🇴 |
| México | MX | 🇲🇽 |
| Estados Unidos | US | 🇺🇸 |
| España | ES | 🇪🇸 |
| Uruguay | UY | 🇺🇾 |
| Perú | PE | 🇵🇪 |
| Paraguay | PY | 🇵🇾 |

## 🔧 Cómo Ejecutar

1. Clona el repositorio
2. Abre el proyecto en Android Studio
3. Ejecuta `Gradle Sync`
4. Conecta un dispositivo o emulador
5. Ejecuta la app

## 📦 Dependencias Principales

```kotlin
// Compose & UI
implementation(libs.androidx.compose.material3)
implementation(libs.androidx.activity.compose)

// Hilt DI
implementation(libs.hilt.android)
implementation(libs.hilt.navigation.compose)

// Room Database
implementation(libs.room.runtime)
implementation(libs.room.ktx)

// Coil - Image Loading
implementation(libs.coil.compose)

// Serialization
implementation(libs.kotlinx.serialization.json)

// Testing
testImplementation(libs.junit)
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
testImplementation("org.mockito:mockito-core:5.11.0")
testImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1")
testImplementation("app.cash.turbine:turbine:1.1.0")
```

## 🧪 Tests y Cobertura

### Ejecución de Tests
```bash
# Ejecutar todos los tests unitarios
./gradlew testDebugUnitTest

# Generar reporte de cobertura JaCoCo
./gradlew jacocoTestReport
# Reporte HTML en: app/build/reports/jacoco/index.html
```

### Resultados
- **194 tests**, 0 failures, 0 errors
- **94% cobertura de instrucciones**, 71% cobertura de branches

### Cobertura por Paquete
| Paquete | Instrucciones | Branches |
|---------|:---:|:---:|
| `presentation` (Constants) | **100%** | n/a |
| `data.mock` (MockDataGenerator) | **99%** | 84% |
| `data.api` (data classes + StoreRegion) | **98%** | **100%** |
| `domain.model` | **97%** | **100%** |
| `presentation.viewmodel` | **86%** | 70% |
| `data.repository` | **80%** | 92% |
| `domain.usecase` | 79% | 50%* |

\* *El 50% de branches en use cases es una limitación conocida de JaCoCo con Kotlin coroutines — genera branches del state machine del compilador que no se pueden cubrir desde código de usuario.*

### Clases Excluidas de Cobertura (requieren tests de integración)
- UI/Compose: screens, components, themes
- Android Context: `ServiceLocator`, `SettingsDataStore`, `UserSettingsRepositoryImpl`
- Red HTTP: métodos de `CheapSharkService` (searchGame, getGameDeals, etc.)

## 🚀 Próximas Mejoras Potenciales

- [x] Imágenes reales de juegos vía Steam CDN
- [x] Integración con CheapShark API (precios reales)
- [x] Sistema de tags para juegos
- [x] DLCs y expansiones en detalle de juego
- [x] Notificaciones in-app
- [x] Selector de país con banderas emoji
- [x] Tema oscuro
- [x] Code cleanup: componentes compartidos, constantes centralizadas, eliminación de código muerto
- [x] Tests unitarios con 94% de cobertura (JaCoCo)
- [ ] Pricing regional real (IsThereAnyDeal API o Steam API con parámetro `cc`)
- [ ] Notificaciones push en tiempo real
- [ ] Sincronización con la nube
- [ ] Múltiples idiomas
- [ ] Historial de precios en gráficos
- [ ] Comparador de precios entre plataformas
- [ ] Alertas personalizadas de descuentos
- [ ] Compartir descuentos con amigos
- [ ] Wishlist compartido

## 📝 Notas de Desarrollo

- **Clean Architecture**: La separación en 3 capas permite fácil testing y mantenimiento
- **MVVM + StateFlow**: Patrón reactivo con Compose
- **Mock Data**: Los datos se regeneran en cada instalación (para demo)
- **Modular**: Fácil de escalar y agregar nuevas funcionalidades
- **Steam App IDs verificados**: Cada juego tiene su ID real de Steam para mostrar la imagen correcta
- **Banderas Unicode**: Las banderas se generan con Regional Indicator Symbols, sin necesidad de assets de imágenes
- **CheapShark API**: Precios reales de tiendas digitales (región US). Para pricing regional se requeriría IsThereAnyDeal API o Steam Store API con parámetro `cc`
- **Code Cleanup**: Componentes compartidos extraídos (`SharedComponents.kt`), constantes centralizadas (`Constants.kt`), código muerto eliminado
- **Testing**: 194 tests unitarios con JaCoCo coverage (94% instrucciones). Tests cubren modelos, use cases, ViewModels, repositorios y data classes
- **Disponibilidad regional de tiendas**: `StoreRegionAvailability` filtra tiendas según el país del usuario

## 👨‍💻 Equipo

Avalos, Ko, Ortuzar, Vargas

---

**Versión**: 3.0
**Última actualización**: Mayo 2026
