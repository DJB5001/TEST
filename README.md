# DJ HUB

Client-seitige Fabric-Mod fuer **Minecraft 1.21.11** (Mojang-Mappings).
`/dj` oder die belegte Taste oeffnet das Einstellungsmenue.

## Tabs

| Tab | Inhalt |
|---|---|
| Auto Tasten | Auto Sprint, Auto Walk, Auto Sneak, Auto Jump, **Auto Linksklick** |
| Ansicht | Max Helligkeit, Zoom, Weitwinkel, **Keine Abbau-Partikel** |
| AutoMiner | AutoMiner, Kamera-Lock, Auto-Zentrieren, Auf Y -54 graben, Ausweichen |
| **Auto Trade** | Item waehlen, beim Villager-Menue automatisch handeln |
| **Auto Chat** | Nachricht eingeben, Abstand waehlen, automatisch senden |
| HUD | FPS, Koordinaten, Blickrichtung, Geschwindigkeit, CPS, Uhrzeit |
| Sonstiges | **Anti Drop**, HUD rechts / Hintergrund / Textschatten, Chat-Meldungen, Menue-Sound |
| Tasten | Alle fuenf Tastenbelegungen frei aenderbar |

## Auto Linksklick

Haelt die linke Maustaste dauerhaft gedrueckt - zum Dauer-Abbauen oder Dauer-Angreifen.
Umschaltbar per Schalter im Menue **und** per Taste (Standard `G`).

Wird beim Beenden des Spiels bewusst **nicht** gespeichert, damit nach dem naechsten Start
nicht ungewollt losgeklickt wird. Bei offenem Bildschirm pausiert die Funktion.

Laeuft der AutoMiner gleichzeitig, hat dieser Vorrang - er darf die Maustaste
vor Lava weiterhin loslassen.

## Keine Abbau-Partikel

Schalter im Tab **Ansicht**. Solange er an ist, erzeugen Bloecke beim Abbauen keine
Partikel mehr - weder die Kruemel waehrend des Schlagens noch die Wolke beim Zerspringen.
Spart spuerbar FPS beim Minen.

Laeuft ueber einen Mixin auf `ParticleEngine.destroy` und `ParticleEngine.crack`, ebenfalls
mit `require = 0` abgesichert.

## Auto Trade

Im Tab **Auto Trade**:

1. Gewuenschtes Item in die Hand nehmen -> `Aus Hand uebernehmen`
   (alternativ oben einen Namensteil eintippen, z.B. `Diamant`)
2. Schalter `Auto Trade` an
3. Optional `Wiederholen` an

Ab dann wird beim Oeffnen eines Villager-Menues automatisch das passende Angebot
angeklickt und das Ergebnis ins Inventar geholt. Ohne `Wiederholen` genau einmal,
mit `Wiederholen` bis das Angebot ausverkauft ist oder du nicht mehr zahlen kannst
(maximal 64 Handel pro Menue).

Es wird 8 Ticks nach dem Oeffnen gewartet, damit der Server die Angebote geschickt hat.
Bleibt der Ergebnis-Slot leer, stoppt es sauber statt endlos zu klicken.

Technisch laeuft es ueber `handleInventoryButtonClick` - genau die Methode, die Vanilla
beim Anklicken eines Angebots auch benutzt.

## Auto Chat

Im Tab **Auto Chat**:

1. Nachricht in das Textfeld tippen (max. 256 Zeichen)
2. Abstand per Klick durchschalten: 5 / 10 / 15 / 20 / 30 / 60 Minuten (Standard 20)
3. Schalter oben an

Ab dann geht die Nachricht in dem Abstand automatisch in den Chat. Faengt sie mit `/` an,
wird sie als Befehl gesendet. `Jetzt senden` schickt sie sofort zum Ausprobieren.

Der Timer laeuft, solange die Funktion an ist und eine Nachricht eingetragen ist.
Ein **Serverwechsel** (z.B. Lobby -> Farmwelt auf OPSUCHT) unterbricht ihn nicht - die
Zeit laeuft normal weiter. Erst nach einer Minute ausserhalb einer Welt faengt er neu an.

Nach dem Ankommen auf einem Server wartet er 3 Sekunden, damit die Nachricht nicht
noch im Ladebildschirm rausgeht.

Text und Abstand werden gespeichert, der Schalter auch. Viele Server haben Regeln gegen
automatische Werbung im Chat - schau da vorher kurz rein.

## Anti Drop

Solange der Schalter an ist, wird die Drop-Taste komplett geschluckt, wenn **kein**
Bildschirm offen ist. Aus Versehen `Q` druecken kostet also kein Item mehr.

Bei offenem Inventar funktioniert das Wegwerfen normal weiter - dort ist es ja Absicht.
Wird ein Wurf blockiert, erscheint hoechstens alle 2 Sekunden ein kurzer Hinweis
ueber der Hotbar.

Technisch laeuft der Filter in `START_CLIENT_TICK`, also bevor Minecraft die Tasten
auswertet - die Drop-Anforderung kommt gar nicht erst an.

## Tasten belegen

Tab **Tasten** -> auf das Kaestchen rechts klicken -> gewuenschte Taste druecken.
`ESC` waehrend des Belegens loescht die Zuordnung. Doppelbelegungen werden automatisch geloest.

| Aktion | Standard |
|---|---|
| Menue oeffnen | Rechte Umschalttaste |
| Zoom (halten) | C |
| AutoMiner an/aus | . |
| Kamera-Lock an/aus | , |
| Auto Linksklick an/aus | G |

Die Belegung laeuft ueber ein eigenes System (GLFW direkt), nicht ueber die Vanilla-Steuerung -
deshalb taucht sie nicht unter "Optionen -> Steuerung" auf, sondern nur hier im Menue.

## AutoMiner

Uebernommen aus der eigenstaendigen Mod "AutoMiner v1" und auf Mojang-Mappings portiert:

- laeuft gerade aus und haelt Linksklick zum Minen
- graebt sich treppenartig auf Y -54 runter (abschaltbar)
- weicht Lava und fallenden Bloecken seitlich aus, sonst stehen bleiben + sneaken
- Pendel-Schutz: nach 5 erfolglosen Ausweichversuchen bleibt er stehen
- Kamera-Lock haelt den Blick waagerecht (Mixin auf `Entity.turn`)
- Auto-Zentrieren richtet dich bei ruhiger Maus auf 90-Grad-Schritte aus

Der Laufzustand wird nicht gespeichert - nach dem Spielstart ist er immer aus.

## Versionen

Exakt die Kombination, mit der Fabric API 1.21.11 selbst gebaut wird:

| | |
|---|---|
| Loom | 1.13.3 |
| Gradle | 9.1.0 |
| Loader | 0.17.3 |
| Fabric API | 0.141.6+1.21.11 |

## Bauen ueber GitHub

1. Ordner ins Repo hochladen.
2. Workflow muss im Repo-**Wurzelverzeichnis** liegen:
   `Add file` -> `Create new file` -> Pfad `.github/workflows/build.yml`.
3. Actions -> `Build DJ HUB` -> `Run workflow`.
4. Artifact `DJ-HUB-jar` herunterladen, entpacken.
5. `djhub-1.0.0.jar` + **Fabric API** in `.minecraft/mods`.

Die alte separate `autominer.jar` kann raus - sonst laufen beide gleichzeitig.
