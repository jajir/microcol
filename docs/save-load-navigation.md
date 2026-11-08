# Ukládání, načítání a herní nabídka

Návrh čitelných názvů souborů a upřesnění organizace adresářů z 8. listopadu
2026 sleduje [R-0432](requirements/R-0432.md). Jde o analýzu před rozhodnutím
o implementaci; současná pravidla níže zůstávají platná.

**Implementováno a lokálně ověřeno 26. října 2026.**
[R-0298](requirements/done/R-0298.md) navazuje na původní návrh z tohoto dne.
Níže je smlouva implementovaného chování. Výsledky testů patří do
[funkční evidence](functional-tests.md), původní prohlídka zdrojů a kontrola
návrhových diagramů zůstává v [publikační evidenci](publishing.md#r-0298-save-load-navigation-2026-10-26).

**Implementováno 1. listopadu 2026; závěrečný smoke série čeká:**
[R-0376](requirements/done/R-0376.md) odstraňuje samostatnou položku Body obnovy
z hlavní nabídky. Obnova je dostupná přes Načíst hru a stávající filtr Body
obnovy; ukládání bodů, nastavení autosave a Pokračovat se nemění. Navigace
níže už tuto položku neobsahuje.

**Implementováno 1. listopadu 2026; závěrečný smoke série čeká:**
[R-0377](requirements/done/R-0377.md) vkládá Nastavení a Načíst hru do hlavního
okna. Obrazovka, která je otevřela, určuje hostitele (mořská oblast menu nebo
herní rám) i návrat tlačítkem Zpět a klávesou Esc. Načíst otevírá také ⌘O na
macOS a Ctrl+O jinde; ⌘S / Ctrl+S na mapě otevírá dosavadní dialog Uložit.
Hlavní menu i herní nabídka uvádějí zkratku platformy v textu položky.
Chyba načtení, zrušený výběr souboru i zrušená ochrana postupu ponechají
stránku otevřenou. Zkratky nereagují při psaní do textového pole, při
otevřeném dialogu, nabídce či výběru souboru ani během souborové operace.

Cílené Docker/Xvfb scénáře a prohlídka snímků prošly; modelové ověření má pouze známé selhání kontrastu FRIGATE, pro které uživatel schválil [výjimku pro tuto sérii](publishing.md#local-acceptance-policy). Nativní macOS ověření je pouze volitelné ruční ověření. Přesné výsledky vlastní [funkční evidence](functional-tests.md). Historické snímky a diagramy z 26. října zachycují předchozí dialogový přehled.

## Názvy a adresáře schválené 8. listopadu 2026

1. Schválený cíl [R-0432](requirements/R-0432.md), zatím bez implementace:
   všechna nová uložení jsou v jednom společném adresáři, bez podsložek pro
   jednotlivé partie. Návrh umístění je dosavadní `~/.akropia/saves/`;
   nastavení zůstává samostatně v `~/.akropia`.
2. Pět automatických souborů se jmenuje `autosave-001.akropia` až
   `autosave-005.akropia`. Číslo 001 vždy označuje nejnovější stav.
   Spravované autosave soubory se při rotaci přepisují bez potvrzení.
   Důsledek společného adresáře: historie je společná pro všechny partie;
   nová partie postupně vytlačí automatické pozice předchozí partie.
   Trvale zachovaný stav si hráč uloží ručně pod vlastním názvem.
3. Výchozí název ruční pozice vychází z národnosti, za kterou uživatel hraje,
   a herního roku, například `duch-1600.akropia`; nejde o jméno hráče.
   Zadaný vlastní název má přednost a použije se v názvu souboru.
4. Přepisování při shodě bylo potvrzeno pro autosave. Kolize ručních názvů,
   nepovolené názvy a osud samostatných bodů obnovy i rychlého slotu
   zůstávají k rozhodnutí v R-0432. Stávající soubory se touto analýzou nemění.

## Současné automatické zápisy — kontrola zdrojů 8. listopadu 2026

1. Kontrola zdrojů revize `596540bc`, nikoli nový běhový test:
   `RecoveryCheckpointPolicy`, `TurnStartedListener`, `PersistingService`
   a `GameModelDao` ukládají při zapnutém autosave připravený začátek nové
   hry a každý další čerstvě připravený lidský tah po zpracování světa/AI,
   před akcemi hráče. Opakované oznámení téhož tahu další kopii nevytvoří.
2. V každém takovém bodě nyní vzniká snímek v historii dané partie
   (`saves/<UUID>/turn-NNNNNNNNNN.akropia`, posledních pět úspěšných zápisů)
   a jeho kopie `~/.akropia/latest-turn.akropia`.
3. Při přechodu do roku dělitelného deseti od roku 1500 se navíc obnoví
   `~/.akropia/previous-decade.akropia`. Je to stav na začátku právě
   dosaženého desetiletí, ne snímek vytvořený zpětně o deset let dříve.
   Pouhé zahájení nové hry tento druhý soubor nevytváří.
4. Načtení, prohlížení seznamu, pohyby jednotek a plynutí skutečného času
   samy neukládají. S vypnutým autosave nevznikají ani uvedené body obnovy.
   Odchod do menu, zavření okna či nahrazení partie neukládají potichu:
   při neuložených změnách volba Uložit a pokračovat otevře ruční ukládání.
5. Rychlé uložení zapisuje `quick.akropia` pouze na příkaz hráče; ruční
   uložení a export také vyžadují jeho akci. `autosave.akropia` je dosavadní
   označení starého slotu poslední pozice a záložní cesta pro starou hodnotu
   Pokračovat; současný tok uvedený výše ho automaticky nezapisuje.
   Předvolby nejsou herní pozice a ukládají se samostatně.

## Pravidla ukládání schválená 1. listopadu 2026

1. Uživatel přijal návrh [R-0387](requirements/done/R-0387.md): při opuštění nebo
   nahrazení partie se ptát jen tehdy, kdy by se ztratil neuložený herní stav.
   Úspěšné ruční, rychlé i automatické uložení aktuálního stavu odstraní potřebu
   výzvy až do další herní změny. Neúspěšný zápis ani zrušení ji neodstraní.
2. Prohlížení mapy, výběr jednotky a otevření/zavření nastavení nejsou změnou
   herního postupu. Předvolby se ukládají samostatně. Herní akce, rozkazy,
   obchod a zpracování tahu změnou jsou; nestačí sledovat pohyb jednotek.
3. Odchod nabídne Uložit a pokračovat / Odejít bez uložení / Zpět, pouze je-li
   to potřeba. **Upřesnění uživatele 1. listopadu 2026:** Ano / Uložit a pokračovat
   otevře běžný ruční dialog Uložit, kde hráč vybere cílovou pozici nebo potvrdí
   její přepsání. Platí i pro ⌘Q, zavření okna a ostatní opuštění živé partie.
   Tichý autosave ani zápis poslední pozice tuto volbu nenahrazuje. Původní
   odchod pokračuje až po úspěšném uložení; Zrušit, Escape, zavření dialogu
   nebo chyba zápisu ponechá hru i okno otevřené. Zůstávají oddělené ruční
   pozice, rychlý slot a body obnovy. Zdroj načtené hry se nikdy nepřepíše potichu.
4. Při zapnutém autosave vznikne počáteční bezpečný checkpoint a potom
   uložení každého připraveného lidského tahu po dokončení zpracování světa.
   Nejde o každý pohyb jednotky. Pokud nová partie dosud nemá uloženou kopii
   (autosave vypnuto nebo selhalo), odchod nabídne její zachování i bez prvního
   herního kroku. Po načtení nezměněné uložené hry se výzva neukáže.
5. Uživatel 1. listopadu 2026 schválil [R-0388](requirements/done/R-0388.md):
   při zapnutém autosave uchovat pět posledních úspěšných snímků tahu každé
   partie včetně počátečního bezpečného stavu. Desetiletý bod, quick save,
   ruční uložení a poslední záchranné uložení při odchodu jsou mimo tento limit.
   Načtení starší pozice nic nezapisuje ani nemaže; další bezpečné začátky tahů
   přidávají snímky do stejné historie podle pořadí úspěšných zápisů, ne podle
   herního roku. Nový snímek se bezpečně zapíše před odstraněním nejstaršího.
   Chyba zápisu zachová předchozí soubory a cíl Continue; chyba mazání se oznámí
   a může dočasně ponechat více než pět snímků, s dalším pokusem o úklid při
   příštím úspěšném autosave. Vypnutí autosave zachová historii; zapnutí pokračuje
   na příští bezpečné hranici tahu bez doplňování minulosti. Podrobná akceptace
   a zobrazení výsledku jsou v R-0388. Jde o schválené chování, ne ověřený běh.
6. Kandidátní implementace (1. listopadu 2026, `ready for development` do
   závěrečné dávkové akceptace): uloženým stavem je produkční JSON herního stavu
   z posledního úspěšného uložení nebo ověřeného a spuštěného načtení,
   porovnávaný strukturně; vyjmuté jsou jen název uložení a zaměřené pole mapy.
   Stav chrání jen tehdy, když jeho soubor stále existuje se stejnou velikostí
   a časem změny. Snímky tahů leží v `saves/<id-partie>/turn-NNNNNNNNNN.akropia`;
   pořadí určuje číslo zápisu ze jmen souborů. Nejnovější snímek se kopíruje do
   bodu Poslední tah, případně Předchozí desetiletí. Testy jsou připravené, ale
   tato kandidátní změna je dosud nespustila; viz kapitolu 3 obou požadavků.

## Náhledy a soubor uložené hry — schváleno 31. října 2026

**Implementováno 1. listopadu 2026; cílené testy a závěrečná přejímka dávky
čekají:** R-0382 a R-0383 jsou zapracované v kódu podle bodů níže. Až
zaznamenané výsledky ve [funkční evidenci](functional-tests.md) jsou ověřením;
samotná implementace jím není.

1. Každá hráčská pozice (ruční, rychlá, bod obnovy, uložení při odchodu
   i export) je ZIP s položkami `metadata.json`, `game.json` a volitelným
   `preview.png`. `game.json` je beze změny JSON současného datového modelu.
   Metadata nesou verzi formátu 1, verzi hry, název, druh, čas uložení v UTC,
   identitu rozehrané hry a souhrn lidského hráče (jméno, národ, obtížnost,
   rok a období, zlato, podporu nezávislosti, počty osob, kolonií a lodí,
   kampaň a misi). Souhrn slouží jen k zobrazení; načtení ho nepoužije.
2. Čtení odmítá jiné, zdvojené či adresářové položky, chybějící metadata nebo
   hru, nepodporovanou verzi a překročení limitů 1 MiB metadat, 64 MiB hry,
   8 MiB obrázku a 80 MiB souboru, počítané podle skutečně přečtených bajtů.
   Náhled nad 960 × 540 px či vadný náhled se ignoruje bez zneplatnění pozice.
3. Načíst, Importovat, Pokračovat i rychlé načtení přijímají jen úplný archiv
   s platnými metadaty: verzí, neprázdnou identitou sestavení, názvem, druhem,
   časem, platnou identitou rozehrané hry a úplným souhrnem. Identita a klíče
   kampaně/mise v metadatech musí odpovídat hernímu JSONu. Prostý JSON není
   hráčská pozice; přehled ho ukáže jako neplatný. Autorské scénáře a editor
   používají samostatné čtení a zápis prostého JSONu stejného datového modelu.
   Pozice bez ručního názvu dostane čitelný název ze jména hráče a data hry;
   povinný je i u bodů obnovy a exportu. Volitelný je jen náhled.
4. Náhled kreslí GUI bez scény JavaFX z prozkoumaných polí lidského hráče
   v barvách minimapy; jednotky, kolonie ani dialogy neobsahuje. Souhrn
   a náhled vznikají ze stejného stavu jako `game.json`.
5. Načíst hru má seznam vlevo a detail vpravo v poměru přibližně 1 : 2.
   Řádek ukazuje název (dlouhý zkrácený), druh a čas uložení; detail celý
   název, náhled nebo „Náhled není k dispozici“ a souhrn. Bez výběru vyzve
   k volbě a Načíst hru je neaktivní; změna filtru či zmizení pozice detail
   vymaže. Nečitelná či nepodporovaná pozice ukáže důvod a nelze ji potvrdit.
6. Uložit ukazuje stav nového uložení se souhrnem a náhledem, předvyplněný
   upravitelný název a zvlášť vybraný cíl přepsání. Potvrzení přepsání
   i smazání uvádí název a čas cílové pozice.

7. [R-0383](requirements/done/R-0383.md), stav `ready for development`, vlastní schválený seznam
   uložených pozic vlevo a náhled mapy se souhrnem vpravo. Výběr pouze zobrazí
   detail; teprve Načíst hru ověří a aktivuje pozici. Souhrn obsahuje národ,
   zlato, rok, podporu nezávislosti a počty vlastních osob, kolonií a lodí.
   Náhled respektuje znalost mapy v uloženém okamžiku.
8. [R-0382](requirements/done/R-0382.md), stav `ready for development`, vlastní ukládání a formát souboru.
   Výslovně zachovat osvědčený postup: herní stav se převede na perzistenční
   datový model a ten do JSONu; načtení provede opačný převod s modelovou
   validací. Nový `.akropia` je pouze ZIP obálka pro `metadata.json`, dosavadní
   herní JSON v `game.json` a `preview.png`. Seznam čte samostatná metadata;
   údaje pro hru nadále určuje úplný datový model. Zachová se atomický zápis,
   ochrana rozpracované hry a zákaz změny souboru pouhým otevřením.
9. Na žádost uživatele je načítací obrazovka samostatným rozsahem R-0383;
   R-0382 nadále vlastní význam uložených počtů, tvorbu náhledu, ukládání a formát.
   Vložení do stránek a návraty vlastní R-0377. Četnost autosave se nemění.

## Co už existuje

1. Akropia již má serializaci, atomický zápis a tři body obnovy: poslední
   pozici, poslední tah a předchozí desetiletí. [R-0068](requirements/done/R-0068.md)
   nadále určuje jejich četnost a nastavení automatického ukládání.
2. Načtení musí ověřit celý nový stav před výměnou rozehrané hry podle
   [R-0067](requirements/done/R-0067.md). Pouhé prohlížení ani otevření soubor
   nepřepisuje. Současná vývojová politika nevyžaduje migrace historických uložených her.
3. Nová nabídka využívá stejné JavaFX popup prvky a společné styly jako výběr
   kolonií. Poslední ovládací prvek hlavní herní lišty otevírá **Nabídku**.
   Menu se otevře také klávesou **Esc**, pokud se právě neruší rozpracovaný
   příkaz na mapě. Zavření nabídky vrátí hráče do mapy.

## Podoba rozhraní

Snímky z Docker/Xvfb scénáře TC191 dne 26. října 2026 ukazují skutečné
rozhraní v angličtině; nejde o maketu. České ovládací texty pokrývá také TC43.

![Herní nabídka](images/save-load-2026-10-26/game-menu.png)

![Přehled uložených her](images/save-load-2026-10-26/saved-games.png)

## 1. Celková navigace

![Mapa obrazovek](diagrams/save-load/navigation.svg)

[Zdroj PlantUML](diagrams/save-load/navigation.puml)

Po změně zdroje (naposledy nadpis v R-0389) se SVG obnovuje opakovatelně
z kořene repozitáře; druhý příkaz obnoví vložený font stejně jako u ostatních
diagramů této stránky:

```sh
plantuml -tsvg docs/diagrams/save-load/navigation.puml
target/graphics-venv/bin/python scripts/graphics/svg-font.py docs/diagrams/save-load/navigation.svg
```

1. Hlavní nabídka zachovává Pokračovat, Načíst, Novou neomezenou hru, Kampaň,
   Síň slávy, Nastavení a Ukončit. Samostatná položka Body obnovy již
   není (R-0376); body obnovy ukáže filtr Body obnovy na stránce Načíst.
2. Herní nabídka obsahuje Zpět do hry, Uložit, Načíst, Rychle uložit, Rychle
   načíst, Nastavení, Hlavní nabídku a Ukončit. V kampani navíc nabízí
   **Začít misi znovu**, která po potvrzení otevře původní scénář mise.
3. Nová hra a výběr mise zůstávají samostatnými vstupy hlavní nabídky.
   Pokračování z uložené pozice obnovuje stav rozehrané mise; restart
   zahajuje původní misi a není pokračováním uložené pozice.
4. Z Nastavení a Načíst se tlačítkem Zpět i klávesou Esc vrací hráč na
   obrazovku, ze které je otevřel: do menu, na mapu se stejným pohledem a
   výběrem, do stejné kolonie, Evropy, přehledu nebo Nastavení (R-0377).
   Existence hry na pozadí hostitele nemění. Návrat sám hru neukládá.

## 2. Okolní herní obrazovky

![Herní obrazovky](diagrams/save-load/game-screens.svg)

[Zdroj PlantUML](diagrams/save-load/game-screens.puml)

1. Diagram z 26. října 2026 zachycuje 13 tehdy popsaných hodnot
   [výčtu Screen](../akropia-game/src/main/java/org/akropia/gui/screen/Screen.java).
   Výčet má nyní 16 hodnot; kandidát R-0377 přidává `LOAD` pro stránku
   Načíst hru, kterou diagram zatím neukazuje.
   Kolonie, Evropa, nákup/prodej, statistiky, hlášení tahu a cíle zachovávají
   své návraty; nejde o nové uložené hry. Herní dialogy zůstávají uvnitř
   volající obrazovky. Vývojový editor používá samostatný systémový výběr.
2. Zavření hlavního okna z libovolné obrazovky prochází společnou ochranou
   rozehrané hry. Zrušení ponechá otevřené okno a původní obrazovku.
3. Odchod a ukončení nejsou odchodem do důchodu a nezapisují skóre;
   [R-0194](requirements/done/R-0194.md) zůstává samostatnou herní akcí.

## 3. Uložit hru

![Ukládání](diagrams/save-load/save.svg)

[Zdroj PlantUML](diagrams/save-load/save.puml)

1. **Uložit / ⌘S na macOS, Ctrl+S jinde** otevře přehled s názvem nového uložení. Potvrzení
   vytvoří nový soubor; shodný název sám o sobě nic nepřepíše. Okrajové
   Unicode mezery se oříznou, vnitřní mezery, diakritika a interpunkce zůstanou.
   Název musí mít 1–64 Unicode znaků bez řídicích znaků. Název není cesta.
2. Vybrané ruční uložení lze výslovně přepsat nebo smazat. Obě operace mají
   potvrzení s názvem a časem cíle. Rychlé a automatické pozice takto přepsat ani
   smazat nelze. Chyba zápisu ponechá starý soubor a nabídne další pokus.
3. **F5 / Rychle uložit** zapíše jeden rychlý bod dané rozehrané hry. Nová
   hra dostane vlastní identitu; načtení uložené hry ji zachová. Jména a
   identita přehledu jsou metadata obálky, nikoli náhrada povinných identit
   hráčů, kolonií či jednotek. Autorské scénáře je nemusejí mít.
4. Spravované soubory jsou v `~/.akropia/saves/<identita-hry>/`;
   rychlý soubor se jmenuje `quick.akropia`, ruční názvy souborů generuje
   hra. Starší ruční soubory a body obnovy v `~/.akropia` jsou viditelné
   ve stejném přehledu. Export umožní zvolit vlastní soubor.
5. **Otevřít složku uložení** otevře `~/.akropia` systémovým správcem souborů.
   Pokud to prostředí nepodporuje, hra oznámí chybu a zachová přehled.
6. Ukládání a rychlé akce jsou běžně dostupné během připraveného lidského
   tahu. Model stále odmítá neúplnou přípravu tahu. Potvrzený zápis zobrazí
   zprávu ve stavovém řádku; zápisy jsou synchronní a dialog brání opakované akci.
7. **Potvrzeno 2. listopadu 2026:** čekající volbu přistěhovalce ani daně nelze
   ručně uložit. Při odchodu bez uložení se **Pokračovat** vrátí k poslednímu
   úspěšně uloženému stavu; nepotvrzené ani již provedené volby v neuloženém
   rozpracovaném tahu se nepřenášejí. Výjimka pro odloženou volbu skóre zůstává.
   [R-0399](requirements/done/R-0399.md) upravuje starší regresní scénáře podle
   tohoto rozhodnutí.

## 4. Načíst a pokračovat

![Načítání](diagrams/save-load/load.svg)

[Zdroj PlantUML](diagrams/save-load/load.puml)

1. Jeden seznam nabízí filtr Všechna uložení / Ruční / Rychlé / Body obnovy.
   Řádky jsou od nejnovějšího a ukazují název, typ a skutečný čas uložení;
   misi, rok, obtížnost, jméno hráče, národ a další souhrn ukazuje detail
   vybrané pozice (R-0383).
   Chybějící soubory se nevypisují; prázdný filtr má vlastní vysvětlení.
2. Poškozené údaje nezablokují prohlížení ostatních řádků. Výběr souboru
   je pouze čtení. Nečitelnou nebo nepodporovanou pozici nelze potvrdit.
   Až Načíst rekonstruuje a ověří celý kandidátní stav;
   chyba se ohlásí nad stránkou Načíst, která zůstane otevřená pro jinou
   volbu, a zachová původní hru i zdroj (R-0377).
3. **Importovat soubor** otevírá systémový výběr. Zrušení přehledu,
   systémového výběru či potvrzení nic neaktivuje. U kandidáta R-0377 zrušený
   výběr i odmítnutý importovaný soubor ponechají stránku Načíst otevřenou;
   teprve Zpět nebo Esc vrátí hráče na původní obrazovku.
4. **Pokračovat** používá poslední úspěšně uloženou nebo načtenou cestu.
   Po konci hry zůstává tato starší rozehraná pozice dostupná; samotný konec
   ji nepřepíše ukončenou hrou.
   Úspěšný automatický checkpoint ji také aktualizuje. **F9 / Rychle načíst**
   obnovuje rychlý bod aktuální rozehrané hry; chybějící bod ohlásí bez změny hry.
5. Před aktivací jiné pozice se u živé hry použije ochrana postupu. Kandidát
   je ověřený před potvrzením, takže neplatný soubor nevyvolá zbytečnou výzvu
   k opuštění hry.

## 5. Ochrana postupu a odchod

![Ochrana postupu](diagrams/save-load/leave.svg)

[Zdroj PlantUML](diagrams/save-load/leave.puml)

1. Načtení jiné pozice, restart mise, návrat do hlavní nabídky, ukončení i
   zavření okna sdílejí jedno pravidlo: je-li současný herní stav bezpečně
   uložen, přechod proběhne bez výzvy; jinak nabídne **Uložit a pokračovat /
   Odejít bez uložení / Zpět**. Hotová hra se dál opouští bez výzvy (R-0374).
2. Uložit a pokračovat otevře týž ruční dialog Uložit jako herní nabídka:
   nové pojmenované uložení, nebo potvrzené přepsání vybraného ručního uložení.
   Teprve úspěšný zápis provede původní přechod; u načtení se ověřená pozice
   použije až potom. Zrušit, Esc, zavření dialogu, chyba zápisu, nepoužitelná
   složka nebo neúplný tah zachová rozehranou hru, okno, stránku Načíst s výběrem
   i neuložený stav; žádný bod obnovy se místo toho nezapíše. Hráč může později
   výslovně odejít bez uložení.
3. Zpět a Esc ruší přechod. Odchod bez uložení vyžaduje samostatnou výslovnou
   volbu. Bez rozehrané hry se ochrana neukazuje. Nová partie, která nikdy nebyla
   uložena, to ve výzvě vysvětlí.
4. Kandidátní implementace R-0387 porovnává herní stav s posledním úspěšným
   uložením či načtením (viz pravidlo 6 výše); pohyb kamery, výběr ani nastavení
   stav nemění. Do závěrečné akceptace jde o neověřený kandidát.
5. Cloud a grafické seskupování podle rozehrané hry nejsou součástí této
   implementace. Rotaci snímků tahů popisuje [R-0388](requirements/done/R-0388.md).
   Náhledy a souhrny popisuje
   [oddíl R-0382/R-0383](#nahledy-a-soubor-ulozene-hry-schvaleno-31-rijna-2026).
