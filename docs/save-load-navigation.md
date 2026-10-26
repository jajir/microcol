# Ukládání a načítání — návrh obrazovek a přechodů

**Návrh z 26. října 2026, dosud neimplementovaný.** Zadáním je nakreslit
hráčskou cestu; tato stránka neschvaluje změnu chování hry. Navazující
[R-0298](requirements/R-0298.md) zůstává `new`. Diagramy jsou PlantUML,
obrázky jsou lokálně vygenerované SVG, takže je dokumentace zobrazí bez
externího serveru a bez nového rozšíření MkDocs.

## Co už existuje

1. Kontrola zdrojů na `7d51a92078506781f3a6507798246d7f4c6318f7`
   (26. října 2026): ruční **Uložit / Načíst** otevírá systémový výběr souboru;
   **Pokračovat** načítá poslední pozici. Není tedy potřeba zavádět samotnou
   serializaci, ale navrhnout ucelenou herní nabídku nad ní. Viz
   [PersistingService](../microcol-game/src/main/java/org/microcol/gui/dialog/PersistingService.java)
   a [nabídka hry](../microcol-game/src/main/java/org/microcol/gui/screen/menu/ButtonsPanelPresenter.java).
2. Existují tři body obnovy: **Poslední pozice**, **Poslední tah** a
   **Předchozí desetiletí**. Dohodnutou četnost, vypínání autosave a atomické
   zápisy zachovává [R-0068](requirements/done/R-0068.md); návrh je sjednocuje
   do seznamu, nepřidává další rotující sloty. Neplatné načtení nesmí změnit
   rozehranou hru ani čtený soubor podle [R-0067](requirements/done/R-0067.md).
3. **Odejít** dnes ukládá poslední pozici a vrací do menu; chyba zápisu
   ponechá hru otevřenou. Zavření okna je odlišná cesta přes
   [QuitGameListener](../microcol-game/src/main/java/org/microcol/gui/event/QuitGameListener.java),
   která aplikaci ukončí bez tohoto ukládání. Společná ochrana před ztrátou
   postupu níže je návrh změny, nikoli popis dnešního chování.
4. Toto je prohlídka zdrojů, nikoli nové ověření běžící hry. Historické výsledky
   patří do [funkční evidence](functional-tests.md).

## 1. Celková navigace

Kliknutím lze každý vektorový diagram otevřít a zvětšit. Přehled obsahuje všechny hlavní obrazovky ze současného
[výčtu Screen](../microcol-game/src/main/java/org/microcol/gui/screen/Screen.java),
doplňkové vstupy a navržené nabídky pro ukládání. Podrobné herní dialogy
(boj, obchodní potvrzení, výběr jednotky apod.) zůstávají uvnitř své obrazovky;
nejsou dalšími kroky ukládání. Obrazovka Nastavení si v návrhu pamatuje,
zda byla otevřená z hlavního nebo herního menu.

[![Přehled obrazovek od spuštění po hlavní menu a rozehranou hru](diagrams/save-load/navigation.svg)](diagrams/save-load/navigation.svg)

[Zdroj PlantUML — celková navigace](diagrams/save-load/navigation.puml).

[![Detail mapy, kolonie, Evropy, obchodních obrazovek a přehledů](diagrams/save-load/game-screens.svg)](diagrams/save-load/game-screens.svg)

[Zdroj PlantUML — herní obrazovky](diagrams/save-load/game-screens.puml).

1. **Herní menu** se otevře z mapy tlačítkem Menu nebo Escape. Escape nejprve
   zavře aktuální dialog či podnabídku; nezahodí rozpracovanou akci ani neotevře
   další menu přes ni. Návrat z kolonie či Evropy vede nejprve na mapu.
2. **Pokračovat** v hlavním menu načte Poslední pozici; **Zpět do hry**
   v herním menu pouze zavře nabídku. Chybějící poslední pozice znamená
   nedostupné Pokračovat s vysvětlením. Poškozená pozice vede do chyby načtení.
3. **Nová hra**, kampaň a editor se otevírají z hlavního menu, až po vyřešení
   případného odchodu z rozehrané partie. Editor zůstává zvláštním vývojovým
   vstupem se současným souborovým dialogem; správa hráčských pozic jej nemění.
4. **Ukončit s výsledkem** zůstává samostatnou herní akcí podle
   [R-0194](requirements/done/R-0194.md), včetně vlastního potvrzení a výsledku.
   Odejít do menu ani zavření aplikace nesmějí přidat skóre do síně slávy.

## 2. Uložení pozice

[![Uložení nové pozice, potvrzení přepsání a zotavení z chyby zápisu](diagrams/save-load/save.svg)](diagrams/save-load/save.svg)

[Zdroj PlantUML — uložení](diagrams/save-load/save.puml).

1. **Uložit hru** nabídne seznam ručních pozic a akci **Nová pozice**.
   Automatické body obnovy zde nejsou cílem ručního přepsání.
2. Formulář nové pozice má předvyplněný upravitelný název. Platí společný
   [kontrakt textových vstupů](architecture.md#identita-kolonii-a-textove-vstupy):
   oříznutí okolních Unicode mezer, nejvýše 64 kódových bodů Unicode, zachování
   vnitřních mezer a diakritiky. Prázdný či příliš dlouhý název ukáže chybu
   přímo v poli. Název je popisek; nesmí umožnit zápis do libovolné cesty.
3. Přepsání je explicitní výběr existující ruční pozice a potvrzení jejího
   názvu a data. Shodný název nové pozice sám nic nepřepisuje; datum a herní
   údaje pomáhají stejnojmenné záznamy odlišit.
4. Během zápisu nelze spustit další zápis, načítání ani odchod. Úspěch má
   krátké viditelné potvrzení. Chyba nabízí opakování, jiný cíl a zrušení;
   původní soubor i rozehraná partie zůstávají nedotčené.
5. Běžné uložení se vrací do herního menu. Uložení vyvolané ochranou postupu
   dokončí čekající odchod nebo načtení pouze po úspěchu. Zrušení se vrátí
   k této ochraně, nikoli k původně požadovanému odchodu.

## 3. Načtení, obnova a smazání

[![Výběr pozice, načtení kandidáta, chyby a potvrzené smazání](diagrams/save-load/load.svg)](diagrams/save-load/load.svg)

[Zdroj PlantUML — načtení a správa](diagrams/save-load/load.puml).

1. Jedna obrazovka **Načíst hru** má záložky **Ruční pozice** a
   **Automatická obnova**. Řádek a jeho detail ukazují název, druh pozice,
   skutečný čas uložení, herní rok/tah, národ a typ hry. Náhled mapy není
   podmínkou první verze. Metadata se čtou bez spuštění či přepsání hry.
2. Prázdný seznam výslovně řekne, že zde žádná pozice není. Načíst a Smazat
   jsou bez výběru nedostupné. Chybějící či nečitelný záznam dostane vysvětlení;
   chyba čtení složky není vydávána za prázdný seznam.
3. Načítání nejprve ověří odděleného kandidáta. Teprve po úspěchu a případné
   ochraně rozehrané hry jej aktivuje. Poškození, neplatný formát, zmizení
   souboru nebo chyba aktivace vedou ke srozumitelnému dialogu, odkud lze
   opakovat pokus, vybrat jinou pozici nebo se vrátit. Předchozí hra zůstane
   dostupná; neprovádí se automatická oprava ani migrace starých uložených her.
4. **Smazat** se nabízí pouze pro vybranou ruční pozici. Potvrzení uvádí název
   a datum a upozorní, že smazání nelze v této nabídce vrátit. Zrušení nic
   nemění; chyba mazání ponechá seznam i soubor dostupné podle skutečného
   výsledku a nabídne nové načtení seznamu. Automatické body se v tomto návrhu
   ručně nemažou. Smazání souboru neukončuje případnou rozehranou partii.
5. Pokračovat používá stejný postup načtení, jen přeskočí výběr. Jeho chyba
   nabídne také seznam automatické obnovy. Úspěšné načtení se vrátí do mapy;
   čekající herní volby a hlášení se obnoví podle uloženého stavu bez opakování
   již provedených účinků.

## 4. Ochrana postupu a odchod

[![Společná ochrana při načtení jiné hry, návratu do menu a ukončení aplikace](diagrams/save-load/leave.svg)](diagrams/save-load/leave.svg)

[Zdroj PlantUML — ochrana postupu](diagrams/save-load/leave.puml).

1. Jeden dialog obslouží **Do hlavního menu**, **Ukončit aplikaci**, zavření
   okna i aktivaci jiné pozice. Text vždy pojmenuje cíl a dopad:
   **Uložit a pokračovat**, **Pokračovat bez uložení**, **Zrušit**.
   „Bez uložení“ zůstává výslovně destruktivní volbou, nikoli výchozím tlačítkem.
2. Bez rozehrané hry nebo při bezpečně uloženém aktuálním stavu se potvrzení
   přeskočí. Pouhé otevření nabídky, pohyb mapou či výběr jednotky není důvod
   označit partii za změněnou. Nová rozehraná dosud neuložená partie ochranu má.
3. Zrušit nebo Escape ponechá původní obrazovku a stav. Při chybě ukládání
   se cílový přechod neprovede. Při načítání jiné pozice zůstává připravený
   kandidát oddělený; ukládací nabídka nedovolí přepsat právě načítaný soubor.
4. Odchod bez uložení nepřepíše poslední platnou pozici. Úspěšné ruční uložení
   v návrhu určí také poslední pozici pro Pokračovat; dokud není tato obnova
   bezpečně dostupná, ochrana nesmí hlásit dokončení. Podrobné provedení
   je předmětem budoucího návrhu persistence, nikoli tohoto diagramu.
5. Během probíhajícího zpracování tahu jsou ukládání a výměna hry nedostupné
   s vysvětlením. Běžná cesta počká na bezpečný stav. Pokud příprava tahu
   selže, ochrana vysvětlí, proč nejde uložit, a ponechá zrušení nebo výslovný
   přechod bez uložení (včetně načtení platného bodu obnovy). Nevytváří neplatný
   nový snapshot a neuvězní hráče v poškozené partii.
6. Automatické ukládání není samostatná obrazovka. Při bezpečném bodu ukáže
   stav a případný úspěch/chybu; jeho selhání nezahodí dřívější bod obnovy
   a nezablokuje ruční pokus. Četnost i přepínač zůstávají podle R-0068.

## Rozsah návrhu a další rozhodnutí

1. Zelená znamená novou nebo změněnou navigaci, šedá stávající okolní
   obrazovky, žlutá potvrzení, červená chybový stav. Každý diagram pojmenovává
   akce i zrušení; barva nenese význam sama. S a L odkazují na detailní
   diagramy, O na společnou ochranu. Čekající cíl a místo návratu se uchovají
   po celou operaci.
2. Návrhem je vlastní seznam ručních pozic, sjednocená obnova a jednotná ochrana
   postupu. Nezavádí cloud, profily, rychlé uložení/načtení ani nový počet
   autosave slotů. Existující soubory se nepřesouvají ani nemažou pouhým
   otevřením nabídky. Výběr souboru mimo herní složku zůstává dostupný přes
   **Otevřít soubor…**, tedy současný systémový dialog.
3. Otevřené produktové volby jsou pouze v [kapitole 4 R-0298](requirements/R-0298.md#4-open-questions).
   Před implementací je třeba potvrdit především nahrazení dnešního
   automatického uložení při odchodu tímto dialogem. Tato dokumentace změnu
   nespouští; ani měnění vzhledu menu v jiných požadavcích ji automaticky nepřejímá.

## Úprava a ověření diagramů

Zdrojové `.puml` i jejich SVG jsou dokumentační aktiva. Po změně přegenerovat
obrázky lokálním PlantUML (zde 1.2026.1), prohlédnout popisky a provést
[dokumentační kontroly](publishing.md#local-preview-and-validation):

```sh
plantuml -charset UTF-8 -tsvg docs/diagrams/save-load/*.puml
target/docs-venv/bin/python -m unittest discover -s scripts/docs -p 'test_*.py'
target/docs-venv/bin/python -m mkdocs build --strict
```

Skutečné výsledky kontrol tohoto návrhu zaznamenává
[evidence dokumentace](publishing.md#r-0298-save-load-navigation-2026-10-26).
