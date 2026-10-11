# Formátování Java kódu ve VS Code

Microcol používá stejný Eclipse formatter jako HestiaStore:
[`.vscode/eclipse-formatter.xml`](../.vscode/eclipse-formatter.xml), profil
`mujSunFormatter`. Soubor byl převzat beze změn **11. října 2026** z projektu
HestiaStore, revize `6da25f8d2edfcb35fc090cb0bcf177026066b7a3`.

Profil určuje čtyři mezery pro odsazení, cílovou šířku řádku a komentářů
80 znaků a dvě úrovně odsazení pokračujícího řádku. Podporuje značky
`@formatter:off` a `@formatter:on`. Autoritativní jsou všechny hodnoty v XML;
tento text je pouze jejich stručný přehled.

## Použití

1. Otevřít ve VS Code kořenovou složku `microcol`, aby editor načetl projektové
   [`.vscode/settings.json`](../.vscode/settings.json).
2. Mít povolené rozšíření **Language Support for Java by Red Hat** (`redhat.java`).
3. Uložit Java soubor nebo spustit příkaz **Format Document**.

Projektové nastavení vybere XML a profil `mujSunFormatter`. Pro Java soubory
zvolí formatter `redhat.java`, zapne formátování při uložení, použije čtyři
mezery a vypne automatické odhadování odsazení ze stávajícího souboru.
Tyto volby jsou omezené blokem `[java]`; jiné jazyky si ponechají své nastavení.

Formátování při uložení může upravit i starší odsazení a zalomení v právě
uloženém souboru. Před commitem zkontrolovat diff. Formátovat soubory dotčené
úkolem; plošné přeformátování repozitáře má být samostatný domluvený úkol.
U funkčních testů nadále zachovat [čitelnou strukturu scénářů](functional-tests.md#stala-a-citelna-struktura-scenaru),
jejich ID, pořadí kroků a očekávané výsledky.

## Rozsah a ověření přenosu

Přenesl se formatter a související editorové volby. Debugger, Maven panel,
zdrojové cesty a nastavení analýzy z HestiaStore patří jeho vlastnímu projektu.
Tento profil se automaticky nespouští v Maven buildu ani v CI.

Dne 11. října 2026 byla ověřena platnost JSON a XML, shoda názvu profilu,
existence relativní cesty a shoda všech 394 nastavení se zdrojem. SHA-256
obou XML souborů je
`016e59100b8e4a51856eab46e71f646b8de06801f66495f31863f6e90784bd86`.
Místní instalace obsahuje rozšíření `redhat.java` 1.56.0, jehož schéma podporuje
obě použité volby `java.format.settings.*`. Interaktivní uložení ve VS Code
nebylo součástí tohoto ověření. Přenos nepřeformátoval existující Java soubory.
