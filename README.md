*  Timeregistrering App

En Android-app til nem timeregistrering med automatisk Excel-generering og lokationsbaserede påmindelser.

## Projektbeskrivelse

Appen er designet til at forenkle timeregistrering ved at:

1. Automatisk registrere arbejdstider baseret på lokation
2. Integrere med Google Calendar for møder
3. Generere Excel-timesedler i det korrekte format

### Excel Skabelon Format

Timesedlen indeholder følgende kolonner:

- Dato
- Fra kl.
- Til kl.
- Timer i alt
- Mer-arbejde
- Delt tjeneste
- Over-arbejde 50%
- Mangl. varsel
- Hverdage 17-06 og Lørdag 00-06
- Lør 06 - man 06
- Helligdage 00-24
- Aften- og nat-tjeneste 17-06
- Hverdage
- Søn- og helligdage
- Ferie
- Ferie-fri
- Sygdom
- Barsel
- Afspadsering (Indtjent/Forbrugt)
- Bemærkning

## Funktioner

### Ugeskema

- Indtastning af arbejdstider for hver ugedag (mandag-fredag)
- Automatisk udfyldning baseret på normal arbejdsuge
- Springer automatisk weekender og helligdage over
- Beregner automatisk timer i alt

### Møderegistrering

- Integration med Google Calendar
- Mulighed for manuel indtastning af møder
- Automatisk registrering af mødetimer i Excel
- Tilføjelse af mødenoter i bemærkningsfeltet

### Lokationsbaserede Funktioner

- Automatisk registrering når man ankommer til arbejdspladsen
- Notifikation ved ankomst/afgang
- Mulighed for manuel justering af tider
- Geofencing til præcis lokationsdetektering

### Statistik og Visualisering
- Detaljerede projekt statistikker
- Pie charts for projektfordeling
- Bar charts for ugentlig timefordeling
- Line charts for månedlige trends
- Interaktive grafer med MPAndroidChart
- Tilpasset farvetema og styling

### Data Eksport
- Excel eksport med formatering og formler
- PDF rapporter med grafer og statistik
- CSV eksport for data analyse
- Automatisk versionering af eksporterede filer
- Batch eksport mulighed
- Konfigurérbare eksport skabeloner

### Excel Integration

- Automatisk generering af Excel-filer
- Versionering af filer (navn-dato-ver#)
- Korrekt formatering med skrifttyper og farver
- Bevarer alle formler og beregninger

## Tekniske Detaljer

### Platforme

- Android (minimum SDK 26)
- Kotlin baseret
- Material Design 3 UI

### Dependencies

- AndroidX Core KTX
- AndroidX Compose
- Google Play Services (Location & Auth)
- Google Calendar API
- Apache POI (Excel håndtering)
- MPAndroidChart for visualiseringer
- JaCoCo for test coverage
- Dokka for API dokumentation

### Påkrævede Tilladelser

- Lokation (Fin og grov)
- Notifikationer
- Internet
- Google Calendar adgang

## Teknisk Arkitektur

### MVVM Arkitektur
Appen følger MVVM (Model-View-ViewModel) arkitekturmønstret:
- **Model**: Data klasser og repositories
- **View**: Jetpack Compose UI komponenter
- **ViewModel**: State management og business logic

### Build og CI/CD
- Gradle Kotlin DSL build scripts
- Version katalog i buildSrc
- Automatisk versioning baseret på Git tags
- GitHub Actions workflow for:
  - Automatisk build på hver commit
  - Test kørsel (unit, integration, UI)
  - Test coverage rapport med JaCoCo
  - API dokumentation generering med Dokka
  - Release management
  - Artifact publicering

### Test Coverage
- JaCoCo integration for:
  - Unit test coverage
  - Integration test coverage
  - UI test coverage
- Coverage rapporter i HTML og XML format
- Minimum coverage krav på CI pipeline
- Automatisk fail ved lav coverage

### Dokumentation
- Automatisk API dokumentation med Dokka
- Markdown baseret projekt dokumentation
- Arkitektur dokumentation med diagrammer
- Setup guides og eksempler
- Contributor guidelines i CONTRIBUTING.md
- Changelog vedligeholdelse

### Repository Pattern
Appen bruger Repository Pattern til at abstrahere datahåndtering:

#### Konsoliderede Repositories
- **CalendarRepository**: Central håndtering af al kalender-relateret funktionalitet
  - Google Calendar integration
  - Lokal møde-database synkronisering
  - Helligdags-håndtering
  - Token management via AuthRepository
- **AuthRepository**: Håndtering af authentication og tokens
- **TimeRegistrationRepository**: Håndtering af timeregistreringer

### Dependency Injection
Bruger Hilt til dependency injection med følgende moduler:
- **DatabaseModule**: Room database og DAOs
- **NetworkModule**: Netværkshåndtering
- **CalendarModule**: Google Calendar integration
- **RepositoryModule**: Data repositories

### Asynkron Programmering
- Kotlin Coroutines til asynkrone operationer
- Flow til reaktiv programmering
- WorkManager til baggrundsopgaver

### Persistence
- Room database til lokal data
- DataStore til brugerindstillinger
- File storage til Excel filer

### Sikkerhed
- API nøgler gemmes i local.properties
- OAuth2 til Google Calendar authentication
- ProGuard/R8 code obfuscation
- Sikker data encryption

### Testing
- Unit tests med JUnit og MockK
- Integration tests med Espresso
- Repository tests med test doubles
- UI tests med Compose testing

## Sikkerhed og Performance

### Sikkerhed
- Krypteret database backup
- Sikker lagring af brugerdata
- Android Keystore integration
- Proper permission håndtering
- Regelmæssig sikkerhedsaudit

### Performance
- Optimeret database adgang
- Effektiv memory håndtering
- Battery-aware geofencing
- Performance monitorering
- Caching af hyppigt brugte data

### Testing
- Unit tests for ViewModels og Repositories
- Integration tests for database og services
- UI tests for kritiske flows
- Edge case testing
- Performance profiling

## Edge Cases
Appen håndterer følgende special cases:
- Sommertid/vintertid skift
- Uger der går over nytår
- Database korruption og recovery
- Batteri optimering
- Concurrent database adgang

## Opsætning af Credentials

For at køre projektet skal du konfigurere følgende credentials:

1. Kopier `local.properties.template` til `local.properties`
2. Udfyld følgende værdier i `local.properties`:
   - `MAPS_API_KEY`: Din Google Maps API nøgle
   - `OAUTH_CLIENT_ID`: Dit OAuth Client ID
   - `GOOGLE_CLIENT_ID`: Dit Google Calendar API Client ID
   - `GOOGLE_CLIENT_SECRET`: Din Google Calendar API Client Secret
   - `GOOGLE_PROJECT_ID`: Dit Google Cloud Project ID

### Release Signing

For at bygge en release version skal du også konfigurere:

1. Opret en keystore fil til app signing
2. Tilføj følgende i `local.properties`:
   - `RELEASE_STORE_FILE`: Sti til din keystore fil
   - `RELEASE_STORE_PASSWORD`: Password til keystoren
   - `RELEASE_KEY_ALIAS`: Alias for din signing key
   - `RELEASE_KEY_PASSWORD`: Password til din signing key

**VIGTIGT**: Commit aldrig `local.properties` eller nogen keystore filer til version control!

## API Nøgler og Credentials

For at køre projektet skal du konfigurere følgende API nøgler i `local.properties`:

```properties
# Google Maps API Key
MAPS_API_KEY=your_maps_api_key_here

# Google OAuth
OAUTH_CLIENT_ID=your_oauth_client_id_here
GOOGLE_CLIENT_ID=your_google_client_id_here
GOOGLE_CLIENT_SECRET=your_google_client_secret_here

# Release Keystore (kun nødvendig for release builds)
RELEASE_STORE_FILE=keystore/release.keystore
RELEASE_STORE_PASSWORD=your_store_password_here
RELEASE_KEY_ALIAS=your_key_alias_here
RELEASE_KEY_PASSWORD=your_key_password_here
```

BEMÆRK:
- Commit ALDRIG `local.properties` til version control
- Placeholder værdier i `secrets.xml` og `google_maps_api.xml` er kun til udvikling
- Brug forskellige API nøgler til debug og release builds
- Hold dine API nøgler og credentials sikre og del dem aldrig

## Udvikler Guide

### Setup
1. Clone repository
2. Opret local.properties med følgende nøgler:
   ```properties
   MAPS_API_KEY=your-maps-api-key
   OAUTH_CLIENT_ID=your-oauth-client-id
   ```
3. Generer release keystore:
   ```bash
   keytool -genkey -v -keystore app/keystore/release.keystore -alias timeregistrering -keyalg RSA -keysize 2048 -validity 10000
   ```

### Build Varianter
- **Debug**: Udvikling og testing
- **Release**: Produktion med optimering

### Continuous Integration
- Automatiske tests ved hver commit
- Code coverage rapporter
- Statisk kodeanalyse

### Best Practices
- Følg Material Design 3 guidelines
- Implementer dark mode support
- Brug vector drawables
- Følg Android performance best practices

## Nye Funktioner

### Dark Mode Support
- Automatisk skift mellem lys og mørk tema
- Respekterer system indstillinger
- Mulighed for manuel kontrol
- Persistent tema præferencer

### Tablet Layout
- Optimeret UI for tablets
- Side-by-side visning på større skærme
- Adaptiv layout for foldbare enheder
- Responsivt design der tilpasser sig skærmstørrelse

### Backup & Restore
- Automatisk daglig backup af data
- Krypterede backups
- Behold seneste 5 backups
- Nem gendannelse af data
- Kører kun når enheden oplades

## Performance Optimering

### Caching
- Implementeret caching af ugedata
- Reduceret database forespørgsler
- Forbedret app responstid
- Intelligent cache invalidering

### Battery Management
- Optimeret geofencing
- Intelligent scheduling af backups
- Reduceret strømforbrug
- Battery optimization checks

## Testing

### Unit Tests
- ViewModels
- Repositories
- Utility klasser
- Data validering

### Integration Tests
- Database operationer
- File I/O
- API integrationer

### UI Tests
- Screen navigation
- User input
- Error states
- Edge cases

## Projektstruktur

### Hovedfiler

```
├── README.md                 # Projektdokumentation
├── settings.gradle.kts       # Gradle indstillinger
├── build.gradle.kts         # Root build konfiguration
└── local.properties         # Lokale properties
```

### Android App

```
app/
├── build.gradle.kts         # App build konfiguration
└── src/
    └── main/
        ├── AndroidManifest.xml              # App manifest
        ├── kotlin/com/example/timeregistrering/
        │   ├── MainActivity.kt              # Hoved-activity
        │   ├── calendar/                    # Google Calendar integration
        │   ├── database/                    # Room database
        │   ├── excel/                       # Excel generering
        │   ├── location/                    # Lokationsbaserede funktioner
        │   ├── ui/                          # UI komponenter
        │   ├── util/                        # Utility klasser
        │   └── viewmodel/                   # ViewModels
        └── res/
            └── values/
                ├── strings.xml              # App tekster
                └── google_maps_api.xml      # Google Maps API nøgle
```

## Ressourcefiler

```
app/src/main/res/
├── drawable/
│   └── ic_launcher_foreground.xml    # App ikon forgrund (ur-design)
├── mipmap-anydpi-v26/
│   ├── ic_launcher.xml              # Adaptivt app ikon
│   └── ic_launcher_round.xml        # Rundt adaptivt app ikon
└── values/
    ├── strings.xml                  # App tekster
    ├── themes.xml                   # App tema
    └── google_maps_api.xml          # Google Maps API nøgle
```

### App Tema

Appen bruger et moderne Material Design tema uden action bar:

- Transparent statusbar
- Transparent navigationbar
- Light theme som standard
- Understøtter system dark mode

### App Ikoner

- Adaptivt ikon der følger Android's designretningslinjer
- Forgrund: Stiliseret ur-design i hvid
- Baggrund: Turkis (#FF018786)
- Understøtter både standard og rund variant

## Installation

1. Installer Android Studio
2. Klon projektet
3. Konfigurer Google API credentials:
   - Google Maps API
   - Google Calendar API
4. Byg og installer på Android enhed

## Build Instruktioner

### I Android Studio

1. Åbn projektet i Android Studio
2. Vent på at Gradle sync er færdig (visning af fremskridtsindikator nederst)
3. Vælg build variant:

   - Klik på "Build Variants" i venstre side (eller View -> Tool Windows -> Build Variants)
   - Vælg "debug" for udvikling
4. Build projektet på én af følgende måder:

   - Klik på "Build" -> "Make Project"
   - Tryk Ctrl+F9
   - Klik på hammer-ikonet i værktøjslinjen
5. Kør appen:

   - Klik på "Run" -> "Run 'app'"
   - Tryk Shift+F10
   - Klik på den grønne play-knap i værktøjslinjen

### Troubleshooting

#### Android SDK Fejl

Hvis du får "Please select Android SDK" fejl:

1. Gå til File -> Project Structure
2. Vælg "SDK Location" i venstre side
3. Bekræft at Android SDK location er: C:\Users\rasmu\AppData\Local\Android\Sdk
4. Hvis ikke, klik på "Edit" og vælg den korrekte sti
5. Klik Apply og OK

#### Gradle Sync Fejl

Hvis Gradle sync fejler:

1. Tjek at local.properties indeholder korrekte stier:
   ```properties
   sdk.dir=C:\\Users\\rasmu\\AppData\\Local\\Android\\Sdk
   cmake.dir=C:\\Users\\rasmu\\AppData\\Local\\Android\\Sdk\\cmake\\3.22.1
   ndk.dir=C:\\Users\\rasmu\\AppData\\Local\\Android\\Sdk\\ndk
   ```
2. Gå til File -> Invalidate Caches / Restart
3. Vælg "Invalidate and Restart"

#### Run Configuration Fejl

Hvis "Edit Configuration" dialog vises:

1. Klik på "+" i øverste venstre hjørne
2. Vælg "Android App"
3. Under "Module", vælg "DanskProjekt.app.main"
4. Under "Installation Options", vælg "Default APK"
5. Under "Launch Options", vælg "Default Activity"
6. Klik "Apply" og "OK"

## Brug

1. Første gang:

   - Log ind med Google konto
   - Giv nødvendige tilladelser
   - Indstil arbejdsplads lokation
2. Daglig brug:

   - Appen registrerer automatisk ankomst/afgang
   - Tjek notifikationer for at verificere tider
   - Juster tider hvis nødvendigt
   - Tilføj møder manuelt eller via Google Calendar
3. Excel generering:

   - Vælg "Gem Timeregistrering"
   - Filen gemmes med dato og versionsnummer
   - Tidligere versioner bevares

## Beslutninger og Valg

1. Android Platform:

   - Valgt pga. nem adgang til lokationstjenester
   - Bedre integration med Google services
   - Mere pålidelig baggrundskørsel
2. Kotlin:

   - Moderne Android udviklingssprog
   - Bedre type-sikkerhed
   - Mere koncist end Java
3. UI Design:

   - Material Design 3 for konsistent look
   - Store, touch-venlige knapper
   - Simpel navigation med bundmenu
   - Optimeret for én-håndsbrug
4. Filhåndtering:

   - Automatisk versionering for sikkerhed
   - Bevarer tidligere versioner
   - Nem adgang til historik

## Database Integration

### Room Database

Appen bruger Room persistence library til at håndtere lokal data:

```kotlin
// Entiteter
@Entity
data class TimeRegistrering(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val dato: LocalDate,
    val startTid: LocalTime,
    val slutTid: LocalTime,
    val type: RegistreringsType,
    val bemærkning: String? = null
)

@Entity
data class UgeskemaIndstilling(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val ugedag: DayOfWeek,
    val standardStartTid: LocalTime,
    val standardSlutTid: LocalTime
)

// DAO'er
@Dao
interface TimeRegistreringDao {
    @Query("SELECT * FROM timeregistrering WHERE dato BETWEEN :fraDato AND :tilDato")
    fun hentRegistreringer(fraDato: LocalDate, tilDato: LocalDate): Flow<List<TimeRegistrering>>
    
    @Insert
    suspend fun indsætRegistrering(registrering: TimeRegistrering)
    
    @Delete
    suspend fun sletRegistrering(registrering: TimeRegistrering)
}
```

### Migration Strategi

Database migrationer håndteres ved hjælp af Room's migrations:

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            ALTER TABLE timeregistrering 
            ADD COLUMN bemærkning TEXT
        """)
    }
}
```

## Google Calendar Integration

### OAuth2 Authentication

1. Konfigurer Google Cloud Console projekt
2. Aktivér Google Calendar API
3. Opret OAuth 2.0-legitimationsoplysninger
4. Tilføj legitimationsoplysninger til app/src/main/res/values/secrets.xml:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="google_client_id">YOUR_CLIENT_ID</string>
    <string name="google_client_secret">YOUR_CLIENT_SECRET</string>
</resources>
```

### Calendar Sync Implementation

```kotlin
class GoogleCalendarSync @Inject constructor(
    private val calendar: Calendar
) {
    suspend fun syncMeetings(startDate: LocalDate, endDate: LocalDate) {
        val events = calendar.events().list("primary")
            .setTimeMin(startDate.atStartOfDay().toInstant())
            .setTimeMax(endDate.atTime(LocalTime.MAX).toInstant())
            .execute()
            
        events.items.forEach { event ->
            // Konverter til TimeRegistrering og gem i database
        }
    }
}
```

## Excel Rapport Generator

### Apache POI Integration

Appen bruger Apache POI til at generere Excel-rapporter:

```kotlin
class ExcelGenerator @Inject constructor(
    private val context: Context,
    private val timeRegistreringDao: TimeRegistreringDao
) {
    fun generateReport(month: YearMonth): File {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Timeregistrering")
        
        // Indlæs skabelon
        val template = context.assets.open("timeregistrering_template.xlsx")
        val templateWorkbook = XSSFWorkbook(template)
        
        // Kopier formatering fra skabelon
        copyStyles(templateWorkbook, workbook)
        
        // Udfyld data
        fillData(sheet, month)
        
        // Gem fil
        val file = File(context.getExternalFilesDir(null), 
            "timeregistrering_${month.format(DateTimeFormatter.ofPattern("yyyy_MM"))}.xlsx")
        FileOutputStream(file).use { 
            workbook.write(it)
        }
        
        return file
    }
}
```

### Excel Skabelon

Excel-skabelonen (`timeregistrering_template.xlsx`) indeholder:
- Foruddefinerede formler
- Betinget formatering
- Validering af input
- Beskyttelse af celler

## Fremtidige Forbedringer

1. iOS version
2. Web interface
3. Backup til cloud
4. Team-funktionalitet
5. Integration med flere kalendersystemer

## TODO Liste

### Højeste Prioritet
- [ ] Implementere widget til hurtig tidsregistrering
- [ ] Tilføje team/gruppe funktionalitet
- [ ] Forbedre offline sync med konflikt håndtering
- [ ] Implementere data eksport scheduling

### Medium Prioritet
- [ ] Tilføje flere statistik visualiseringer
- [ ] Implementere kalender integration
- [ ] Forbedre lokationsbaseret registrering
- [ ] Tilføje påmindelses system

### Lavere Prioritet
- [ ] Implementere tablet-optimeret UI
- [ ] Tilføje flere eksport formater
- [ ] Forbedre performance for meget store datasæt
- [ ] Tilføje flere tilpasningsmuligheder

## Bidrag
Se [CONTRIBUTING.md](CONTRIBUTING.md) for detaljer om hvordan du kan bidrage til projektet.

## Changelog
Se [CHANGELOG.md](CHANGELOG.md) for en liste over ændringer i hver version.
