# MusicCast Controller

Eine Spring Boot Web-Anwendung zur Steuerung von Yamaha MusicCast Geräten in mehreren Räumen.

## Funktionen

- **Power Control**: Ein-/Ausschalten der Geräte
- **Wiedergabe-Steuerung**: Play, Pause, Stop, Nächster/Vorheriger Titel
- **Lautstärke-Regelung**: Lauter/Leiser, Mute, Slider-Steuerung
- **Echtzeit-Status**: Anzeige des aktuellen Status aller Räume
- **Responsive Design**: Funktioniert auf Desktop und Mobile

## Konfigurierte Räume

- **Wohnzimmer**: 192.168.178.21
- **Schlafzimmer**: 192.168.178.36
- **Küche**: 192.168.178.37

## Voraussetzungen

- Java 17 oder höher
- Maven 3.6 oder höher
- Yamaha MusicCast Geräte im Netzwerk
- Für Docker: Docker Desktop oder Docker Engine mit Buildx

## Installation und Start

### Option 1: Direkt mit Maven

1. **Projekt kompilieren:**
   ```bash
   mvn clean compile
   ```

2. **Anwendung starten:**
   ```bash
   mvn spring-boot:run
   ```

3. **Web-Interface öffnen:**
   ```
   http://localhost:8080
   ```

### Option 2: Docker (Multi-Architektur Support)

Das Docker-Setup unterstützt sowohl **amd64** als auch **arm64** Architekturen (z.B. Apple Silicon Macs, Raspberry Pi).

#### Einfacher Start mit Docker Compose:
```bash
docker-compose up --build
```

#### Multi-Architektur Build:
```bash
# Lokaler Build für aktuelle Architektur
./build-multiarch.sh

# Build für beide Architekturen (amd64 + arm64)
./build-multiarch.sh --push
```

#### Manueller Docker Build:
```bash
# Für aktuelle Architektur
docker build -t musiccast-controller .

# Für spezifische Architektur
docker build --platform linux/arm64 -t musiccast-controller:arm64 .
docker build --platform linux/amd64 -t musiccast-controller:amd64 .
```

#### Docker Run:
```bash
docker run -d \
  --name musiccast-controller \
  -p 8080:8080 \
  --restart unless-stopped \
  musiccast-controller
```

## IP-Adressen anpassen

Falls sich die IP-Adressen Ihrer MusicCast-Geräte ändern, bearbeiten Sie die Datei:
`src/main/java/com/example/musiccast/service/MusicCastService.java`

Ändern Sie die IP-Adressen in der `MusicCastService` Konstruktor-Methode:

```java
rooms.put("Wohnzimmer", new Room("Wohnzimmer", "192.168.178.21"));
rooms.put("Schlafzimmer", new Room("Schlafzimmer", "192.168.178.36"));
rooms.put("Küche", new Room("Küche", "192.168.178.37"));
```

## API Endpoints

Die Anwendung nutzt die Yamaha MusicCast HTTP API:

- **Power**: `/YamahaExtendedControl/v1/main/setPower`
- **Volume**: `/YamahaExtendedControl/v1/main/setVolume`
- **Playback**: `/YamahaExtendedControl/v1/netusb/setPlayback`
- **Status**: `/YamahaExtendedControl/v1/main/getStatus`

## Troubleshooting

1. **Geräte nicht erreichbar**: Überprüfen Sie die IP-Adressen und Netzwerkverbindung
2. **Keine Reaktion**: Stellen Sie sicher, dass die MusicCast-Geräte eingeschaltet sind
3. **Port-Konflikt**: Ändern Sie den Port in `application.properties` falls 8080 belegt ist
4. **Docker Multi-Arch Probleme**: Stellen Sie sicher, dass Docker Buildx aktiviert ist

### Docker-spezifische Probleme:
```bash
# Buildx Status prüfen
docker buildx ls

# Buildx installieren falls nicht verfügbar
docker buildx install

# Architektur des Images prüfen
docker buildx imagetools inspect musiccast-controller:latest
```

## Features

- Auto-Refresh alle 30 Sekunden
- Responsive Design für Mobile und Desktop
- Deutsche Benutzeroberfläche
- Echtzeit-Feedback bei Aktionen
- Fehlerbehandlung mit Benutzer-Feedback
- Multi-Architektur Docker Support (amd64/arm64)
