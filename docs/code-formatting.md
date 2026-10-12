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

## Pravidlo pro agenty a Qwen

Od **12. října 2026** platí stejný profil pro hlavního agenta i všechny workery,
včetně lokálního Qwen. Pravidlo je součástí [AGENTS.md](../AGENTS.md),
[role workeru](../.agents/skills/local-worker/references/worker-role.md)
a [šablony zadání](../.agents/skills/local-worker/references/worker-instructions.md).
Nevytvářet zvláštní styl pro kód generovaný modelem.

Při předání Java úkolu uvést požadavky na formátování, zpřístupnit XML profil
v pracovním stromu a určit konkrétní příkaz omezený na změněné Java soubory.
Hlavní agent po uvolnění Qwen z paměti použije sdílený formatter, zkontroluje
diff a teprve potom provede ověření a přijetí změny. Totéž platí pro jeho vlastní
Java úpravy před commitem. Zachovat kroky, komentáře a aserce funkčních scénářů;
nesouvisející soubory nepřeformátovávat.

Zápis přes CLI nebo patch **nespouští** formátování při uložení ve VS Code.
V předání výsledku rozlišit skutečně provedené formátování od práce čekající
na hlavního agenta. Nedostupný formatter je překážka dokončení, nikoli důvod
k použití jiného stylu. Jde o pravidlo pracovního postupu, ne nový automatický
hook v CLI, Maven nebo CI; tato změna instrukcí sama neověřuje chování Qwen.

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
nebylo součástí tohoto ověření. Samotný přenos nepřeformátoval existující Java
soubory; následné výslovně zadané formátování je popsáno níže.

## Plošné formátování 11. října 2026

Formátovačem prošlo všech **802 verzovaných souborů `*.java`**, včetně testů
a `package-info.java`; 650 se změnilo a 152 už odpovídalo profilu. Rozsah byl
určen příkazem `git ls-files '*.java'`, bez generovaných souborů v `target/`.

| Modul | Zkontrolované Java soubory | Změněné |
| --- | --- | --- |
| microcol-game | 745 | 594 |
| microcol-test | 42 | 42 |
| microcol-build-tests | 11 | 11 |
| microcol-dist | 2 | 2 |
| microcol-module | 2 | 1 |

Použit byl již nainstalovaný Eclipse JDT Core **3.47.0.v20260828-1141**
z `redhat.java` 1.56.0, všech 394 voleb profilu `mujSunFormatter` a zdrojová
úroveň Java 11 podle Maven konfigurace. Jednorázový běh přes
`ToolFactory.createCodeFormatter` zahrnoval komentáře; odstranil také koncové
mezery na řádcích. Profil XML ani závislosti projektu se nezměnily.

Kontrola všech souborů porovnala před a po úpravě přesnou posloupnost Java
tokenů včetně literálů. Odpovídal také obsah komentářů po odstranění jejich
formátovacího odsazení a zalomení. Opakování stejného formátování a odstranění
koncových mezer už výsledek nezmění. Scénáře TC zachovaly své identifikátory,
`@Start`, fixtures, pořadí anglických kroků i aserce; TC18 byl zkontrolován
také ručně. Jde o změnu formátu, nikoli pravidel nebo očekávání testů.

Lokálně na macOS/JDK 17 prošlo **603 herních testů**, bez selhání, chyb
a vynechání, příkazem `mvn -B -ntp -pl microcol-game -am test`. Úplnou
funkční sadu a nativní distribuce ověřuje povinný workflow publikované revize
podle [funkčního kontraktu](functional-tests.md#spousteni-a-ochrana-publikace).
Formátovač zůstává editorovou a jednorázově použitou pomůckou; tento úkol
nezavedl nový automatický formatter do Maven/CI.
