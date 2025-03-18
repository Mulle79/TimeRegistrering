# Changelog

Alle væsentlige ændringer i projektet vil blive dokumenteret i denne fil.

Formatet er baseret på [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
og projektet følger [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0] - 2025-03-18

### Tilføjet

- GitHub Actions workflow til automatisk at bygge og udgive APK-filer
- Signing configuration til release builds
- Keystore properties template til app signing

### Ændret

- Opdateret README.md med instruktioner til GitHub Actions workflow
- Forbedret build.gradle.kts med betinget signing configuration

## [Unreleased]

### Tilføjet

- NotificationHelper til at vise notifikationer ved ankomst og afgang fra arbejdspladsen
- Forbedret geofencing-funktionalitet med automatisk tidsregistrering
- Integreret PowerManager til at håndtere batterioptimeringsindstillinger

### Ændret

- Opdateret AndroidManifest.xml for at fjerne ikke-eksisterende services
- Forbedret LocationModule med NotificationHelper-support
- Optimeret GeofenceBroadcastReceiver til at bruge separate notifikationer for ankomst og afgang

### Rettet

- Ikon-referencer i notifikationer
- Dependency injection for lokationsbaserede tjenester

## [1.1.0] - 2025-03-13

### Tilføjet

- NotificationHelper til at vise notifikationer ved ankomst og afgang fra arbejdspladsen
- Forbedret geofencing-funktionalitet med automatisk tidsregistrering
- Integreret PowerManager til at håndtere batterioptimeringsindstillinger

### Ændret

- Opdateret AndroidManifest.xml for at fjerne ikke-eksisterende services
- Forbedret LocationModule med NotificationHelper-support
- Optimeret GeofenceBroadcastReceiver til at bruge separate notifikationer for ankomst og afgang

### Rettet

- Ikon-referencer i notifikationer
- Dependency injection for lokationsbaserede tjenester

## [1.0.0] - 2025-02-21

### Tilføjet

- Grundlæggende timeregistrering
- Projekt håndtering
- Statistik visning med grafer
- Excel, PDF og CSV eksport
- Dark mode support
- Backup system
- Offline support
- Performance optimering med paging
- Automatiske tests
- CI/CD pipeline
- Lokationsbaseret tidsregistrering med geofencing
- Automatisk registrering ved ankomst og afgang fra arbejdspladsen

### Ændret

- Forbedret UI/UX med Material 3
- Optimeret database queries
- Forbedret error handling
- Opdateret lokationshåndtering med WorkLocation model

### Rettet

- Database migration bugs
- Offline sync problemer
- Performance issues med store datasæt
- Versionering problemer i build-processen

## [0.9.0] - 2025-02-01

### Tilføjet

- Initial beta release
- Grundlæggende funktionalitet
- Test suite setup
