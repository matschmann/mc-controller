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

## Installation und Start

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

## Features

- Auto-Refresh alle 30 Sekunden
- Responsive Design für Mobile und Desktop
- Deutsche Benutzeroberfläche
- Echtzeit-Feedback bei Aktionen
- Fehlerbehandlung mit Benutzer-Feedback
