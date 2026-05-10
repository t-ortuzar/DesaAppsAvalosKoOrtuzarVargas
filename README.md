# Game Tracker App - Demo

Una aplicación Android moderna para trackear descuentos de videojuegos en múltiples plataformas como Steam, Epic Games Store, GOG, EA Play, Ubisoft+, Battle.net, G2A y Eneba.

## 🎮 Características Principales

### 1. **Catálogo de Juegos**
- Visualiza un catálogo de 100 juegos populares sin F2P
- Cada juego muestra:
  - Portada/Imagen del juego
  - Título y descripción
  - Calificación (rating)
  - Descuento histórico más grande
  - Precios actuales en diferentes plataformas
- Busca juegos por nombre
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

## 🏗️ Arquitectura (Clean Architecture)

La aplicación sigue el patrón de **Clean Architecture** dividida en 3 capas:

### **Capa de Presentación (Presentation Layer)**
- **Components**: Composables reutilizables
  - `GameCard`: Tarjeta para mostrar un juego
  - `NewsCard`: Tarjeta para mostrar noticia
  - `DiscountCard`: Tarjeta para mostrar descuento
  
- **Screens**: Pantallas principales
  - `GamesScreen`: Catálogo de juegos
  - `NewsScreen`: Sección de noticias
  - `OffersScreen`: Sección de ofertas/descuentos
  - `GameDetailScreen`: Detalle de un juego
  - `NewsDetailScreen`: Detalle de una noticia
  - `MainScreen`: Pantalla principal con navegación inferior
  
- **ViewModels**: Lógica de presentación
  - `GamesViewModel`: Gestiona estado de juegos
  - `NewsViewModel`: Gestiona estado de noticias
  - `OffersViewModel`: Gestiona estado de descuentos

### **Capa de Dominio (Domain Layer)**
- **Models**: Entidades de dominio
  - `Game`: Representa un videojuego
  - `News`: Representa una noticia
  - `DiscountedGame`: Representa un juego en descuento
  - `PriceHistory`: Historial de precios
  - `Platform`: Información de plataforma
  
- **Repositories (Interfaces)**: Contratos para acceso a datos
  - `GameRepository`
  - `NewsRepository`
  - `DiscountRepository`
  
- **Use Cases**: Casos de uso de negocio
  - `GetAllGamesUseCase`
  - `AddToFavoritesUseCase`
  - `GetCurrentDiscountsUseCase`
  - etc.

### **Capa de Datos (Data Layer)**
- **Database (Room)**: Base de datos local
  - Entidades: `GameEntity`, `NewsEntity`, `DiscountEntity`, `FavoriteEntity`, `PriceHistoryEntity`
  - DAOs: Acceso a datos
  - `AppDatabase`: Base de datos SQLite
  
- **Repositories (Implementations)**: Implementación de interfaces
  - `GameRepositoryImpl`
  - `NewsRepositoryImpl`
  - `DiscountRepositoryImpl`
  
- **Mock Data**: Generador de datos de demostración
  - 100 juegos populares
  - 50+ descuentos
  - 20+ juegos gratis
  - Historial de precios
  - Noticias relacionadas

## 🛠️ Tecnologías Utilizadas

- **Kotlin**: Lenguaje de programación
- **Jetpack Compose**: UI Framework
- **Hilt**: Inyección de dependencias
- **Room**: Base de datos local
- **Retrofit**: Cliente HTTP (preparado para API)
- **Kotlinx Serialization**: Serialización JSON
- **Coil**: Carga de imágenes desde URL
- **Material Design 3**: Diseño UI

## 📁 Estructura del Proyecto

```
app/src/main/java/com/example/desaappsavaloskoortuzarvargas/
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt
│   │   ├── DatabaseInitializer.kt
│   │   ├── dao/  (DAOs para cada entidad)
│   │   └── entity/  (Entidades Room)
│   ├── mock/
│   │   └── MockDataGenerator.kt
│   ├── mapper/
│   │   └── Mappers.kt
│   └── repository/  (Implementaciones)
├── domain/
│   ├── model/  (Entidades de dominio)
│   ├── repository/  (Interfaces)
│   └── usecase/  (Use cases)
├── presentation/
│   ├── component/  (Composables reutilizables)
│   ├── screen/  (Pantallas principales)
│   └── viewmodel/  (ViewModels)
├── di/
│   └── Modules.kt  (Configuración Hilt)
├── MainActivity.kt
└── GameTrackerApp.kt  (Application class)
```

## 🚀 Funcionalidades por Pantalla

### **Pantalla de Catálogo (Catalog)**
- Lista de 100 juegos más populares
- Barra de búsqueda
- Cada tarjeta muestra:
  - Portada del juego
  - Titulo
  - Rating ⭐
  - 2 primeras plataformas con precios
  - Botón de favoritos ❤️
- Click en tarjeta → Ver detalle

### **Pantalla de Noticias (News)**
- Filtros por tipo de noticia
- Cada noticia muestra:
  - Miniatura de imagen
  - Titulo de noticia
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

### **Detalle de Juego**
- Portada a tamaño completo
- Información completa:
  - Fecha de lanzamiento
  - Rating detallado
  - Descripción completa
  - Precios en todas las plataformas
  - Descuento histórico
- Botón de favoritos en header

### **Navegación Inferior (Bottom Navigation)**
- 3 tabs principales
- Icons y labels
- Navegación rápida entre secciones

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
```

## 🚀 Próximas Mejoras Potenciales

- [ ] Integración con API real de Steam/Epic
- [ ] Notificaciones en tiempo real
- [ ] Sincronización con la nube
- [ ] Modo oscuro
- [ ] Múltiples idiomas
- [ ] Historial de precios en gráficos
- [ ] Comparador de precios entre plataformas
- [ ] Alertas personalizadas de descuentos
- [ ] Compartir descuentos con amigos
- [ ] Wishlist compartido

## 📝 Notas de Desarrollo

- **Clean Architecture**: La separación en 3 capas permite fácil testing y mantenimiento
- **MVVM + StateFlow**: Patrón reactivo con Compose
- **Room + Mock Data**: Los datos se regeneran en cada instalación (para demo)
- **Modular**: Fácil de escalar y agregar nuevas funcionalidades

## 👨‍💻 Autor

Proyecto de demostración de Clean Architecture con Jetpack Compose y Kotlin.

---

**Versión**: 1.0
**Última actualización**: 2024

