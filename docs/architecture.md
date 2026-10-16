# Odpovědnosti Java balíčků

Tento přehled určuje, kam v MicroCol patří nová změna a které části mají
spolupracovat. Před zadáním práce vybrat vlastníka pravidla a dotčené balíčky
z této mapy; není potřeba pokaždé znovu odvozovat rozdělení celé hry ze zdrojů.
Konkrétní metody a jejich volající je před úpravou stále nutné přečíst.

**Mapa založená na stavu k 11. říjnu 2026; hranice modelu aktualizována
13. října 2026 v [R-0011](requirements/done/R-0011.md).** Mapa vychází ze zdrojů revize
[`dba28604`](https://github.com/jajir/microcol/commit/dba286043459eef872f5ef561e0da753417cdb3e).
Popisuje současné umístění kódu a hranice pro další změny. Známé odchylky
jsou uvedené níže; samotný zápis je neodstraňuje.

## Kam patří nová změna

| Otázka | Vlastník |
| --- | --- |
| Co hra dovoluje, kolik akce stojí a jak změní svět? | `model` a jeho příslušné podbalíčky. |
| Kterou povolenou akci zvolí počítačový hráč? | `ai`. |
| Jak člověk akci najde, vyvolá, zruší a uvidí výsledek? | `gui` a příslušná obrazovka. |
| Co musí zůstat zachované po uložení a načtení? | Herní objekty spolu s `model.store`; obálka hry v `model.campaign.po`. |
| Jaký cíl nebo scénář platí v konkrétní misi? | `model.campaign`. |
| Jak se text přeloží a jak se vybere obrázek? | `i18n`, respektive `gui.image`; význam herního výsledku zůstává v modelu. |
| Jak ověříme pravidlo a skutečnou cestu hráče? | Modelové testy a `microcol-test` podle [funkčního kontraktu](functional-tests.md). |

**Stav hráče patří do modelu pro člověka i robota.** `Player`, `PlayerHuman`
a `PlayerKing` popisují hráče a jeho herní stav. Strategie počítače patří do
`ai`, rozhodnutí člověka přichází přes UI. Povolení akce a její účtování mají mít
jednu společnou implementaci, kterou používá tlačítko, klávesa i AI.
Název `PlayerHuman` dnes neznamená výhradně člověka: `Player.make` tuto třídu
vytváří i pro počítačové kolonizační hráče podle příznaku `isComputer`.

### Platnost herní akce a výjimky

Model ověří všechny herní podmínky před první změnou stavu. Neplatné množství,
vlastnictví, poloha, zdroj nebo kapacita vyvolají výjimku; zamítnutá akce
nesmí provést část převodu. Používat existující výjimky a kontroly modelu
(`IllegalArgumentException`, případně `IllegalStateException`), bez vazby
na GUI. Chybějící programový argument zůstává chybou volajícího.

UI předem získá dovolené možnosti z modelu, omezí nabídku a zabrání potvrzení
neplatné akce. Nevytváří vlastní kopii výpočtu pravidla. Zrušení nevolá
měnící operaci. Model při provedení zkontroluje živý stav znovu; pokud se
během dialogu změnil, UI převod neprovede a vysvětlí výsledek. Zachytává
konkrétní očekávané zamítnutí, nikoli libovolnou programovou výjimku.

Každá měněná podmínka má modelové hraniční testy a odpovídající ochranu
skutečné cesty hráče v UI. Test modelu dokládá výjimku i nezměněný stav;
UI test dokládá omezenou nabídku, zrušení či srozumitelné odmítnutí a zásoby.

```text
gui: volba člověka ───┐
                     ├──> model: ověření a provedení akce
ai: volba počítače ───┘
```

Existujícím příkladem je pohyb: [GameModelController](../microcol-game/src/main/java/org/microcol/gui/event/model/GameModelController.java)
i [SimpleAiPlayer](../microcol-game/src/main/java/org/microcol/ai/SimpleAiPlayer.java)
volají `Model.moveUnit`. Diagram vyjadřuje hranici pro herní operace;
neznamená, že všechny současné akce už používají totožnou cestu.

### Evropské vztahy a životní cyklus hry

Implementované [R-0026](requirements/done/R-0026.md) ukládá jeden vztah míru/války
pro každou neuspořádanou dvojici kolonizačních hráčů. `Model` kontroluje živou
hru, aktuálního aktéra a registrované objekty hráčů; `EuropeanRelations` vlastní
úplnost dvojic a očekávaný předchozí stav. `model.store` uchovává interní klíče
hráčů, nikoli upravitelné zobrazované názvy. Královské hráče určuje jejich role.
Chybějící vztahy se při načítání nedoplňují odhadem.
Sdílený dotaz `Model.areHostile` odděluje diplomatické oprávnění od obsazení
polí cizími jednotkami. Cizí mírová jednotka nadále brání vstupu a vylodění.
Králové nemají evropský záznam míru/války; dotaz pro ně zachovává dosavadní
nepřátelství k cizím vlastníkům a vznik/řízení královských sil zůstává v cestě
vzpoury. AI používá stejné útočné a pohybové dotazy jednotek jako modelový
příkaz. Potvrzené porušení smlouvy útokem propojuje rozpracované R-0028,
jak je popsáno níže.
Před vstupem do dobývaného přístavu `Colony` naplánuje únik všech cizích
nepřátelských lodí. Neutrální obsazení nebo chybějící bezpečné sousední moře
vstup odmítnou před spotřebou pohybu či změnou vlastníka. AI používá tentýž
dotaz jako pohyb hráče. Dobývající jednotka zůstává vybavená na mapě.


`GameManager` odlišuje zahájení, dosažený herní konec a zahození instance přes
`Model.stop()`. Zastavení zneplatní další akce před oznámením posluchačům;
zahájený stav zůstává dostupný pro čtení a uložení. Výsledek herního konce
se ukládá jako `gameFinished`, aby načtení nepovolilo další tah. Samotné
zahození instance se neukládá: běžnou rozehranou pozici lze načíst do nové
instance. Integrace AI/UI a výsledky ověření jsou vedené v R-0026; tento popis
není potvrzením dokončení celého požadavku.


### Uložená obtížnost (R-0064)

`model.Difficulty` a neměnná hodnota v `Model` jsou společným zdrojem obtížnosti
pro všechny národy a budoucí pravidla. `ModelPo` ukládá její identitu;
chybějící nebo neznámá hodnota se odmítá před výměnou rozehrané hry.
`GameModelDao` nastaví volbu nové partie před sestavením modelu, zatímco
`LocalizationHelper` pouze překládá názvy pro nastavení a herní panel.
Nové partie mají výchozí Conquistador; načtené hry nemají náhradní výchozí
hodnotu. Číselné účinky a stav ověření mají své vlastní požadavky;
[R-0064](requirements/done/R-0064.md) přijímá pouze volbu a kontinuitu profilu.

### Bezpečné odmítnutí načítané hry (R-0067)

`GameModelPoDao` čte soubor bez oprav a zápisu. `GameModelDao` kontroluje
povinnou strukturu; `Cargo` odmítá prázdné položky a `WorldMapPo` neúplnou
či nesprávně rozměrnou mřížku terénu. DAO sestaví kandidáta běžným modelovým načtením ještě před
výměnou rozehrané hry. `GameController` převádí chyby čtení, JSON a odmítnutá
data na existující lokalizovaný dialog a neúspěšný výsledek. Načíst/Pokračovat
pak ponechají předchozí obrazovku a umožní další pokus. Chyby spuštění již
sestaveného kandidáta leží mimo tento blok; nejsou hlášené jako vadný soubor.
[Evidence R-0067](functional-tests.md#r-0067-load-recovery-2026-10-16) odlišuje
ověřené případy od úplné přejímky.

### Referenční výpravy (R-0066)

`model.campaign.ReferenceExpedition` sestavuje nový model ze samotného terénu,
obtížnosti a identity `EuropeanNation`. Nepřebírá jednotky, kolonie ani průzkum
zdrojového scénáře. Před sestavením ověří čtyři odlišné východní námořní starty
s vodní cestou k pobřeží. Vlastnictví, vybavení, náklad a mírové vztahy používají
běžné modelové struktury; pořadí tahů a AI zůstávají společné.
`GameModelDao` tuto cestu vybírá pouze pro `FreePlayMap.REFERENCE`.
Tři autorské scénáře se načítají dál beze změny. R-0063 předává z nabídky typovaný národ přes aplikační a herní řadič do DAO.
Autorské scénáře přijímají pouze Nizozemsko; nekompatibilní volba se odmítne
před výměnou rozehrané hry. Referenční profil přijímá všechny čtyři národy.
Jméno hráče zůstává nezávislé na interním národním klíči. Stav ověření uvádí
[evidence R-0066](functional-tests.md#r-0066-reference-expeditions-2026-10-16).

### Kandidáti a placená cesta do kolonií (R-0075)

`RecruitmentPool` patří jednomu koloniálnímu hráči a drží tři tokeny
kandidátů, jejich typy a samostatné počty placených a získaných přistěhovalců.
`RecruitmentOffer` je neměnný snímek konkrétního poolu a ceny; `Player.recruit`
před jedinou změnou ověřuje živou hru, vlastníka, kandidáta, cenu, peníze
a dostupnost nových identit. Výsledkem je jedna jednotka na molu a výměna
jediného kandidáta. Nákup odborníka používá nadále oddělenou operaci `buy`.
`RecruitmentPo` a `RecruitmentCandidatePo` ukládají explicitní stav;
načtení nikdy nelosuje náhradní kandidáty. Novou nabídku vytvářejí pouze
cesty zahájení nové hry. `UnitLaborer` zachovává služebníka/trestance přes
vybavení a přepravu; `ConstructionSlot` sdílí jejich výrobní úpravu mezi
náhledem, skutečným výstupem a spotřebou vstupů.
R-0076 doplňuje přičítání skutečného přírůstku křížů z `Colony.startTurn`
do národního poolu a vyřízení dosažených prahů v evropské fázi příštího
`Player.startTurn`. Místní sklad křížů zůstává evidencí produkce; jeho
kumulovaný obsah se znovu nepřičítá. Čtecí dotazy UI příchozí nevytvářejí.
Brewsterův účinek
vyžaduje skutečné členství z R-0104. Stav přejímky vede [R-0075](requirements/R-0075.md).

### Ceny opakovaných nákupů (R-0077)

`Player` vlastní počty nákupů podle typu a výpočet aktuální ceny; `UnitType`
zůstává zdrojem základní ceny. `PlayerPo.unitPurchaseCounts` ukládá řídkou mapu
jmen typů na počty, prázdná mapa znamená žádný nákup. Dialog předává zobrazenou
cenu do modelové operace, která ji znovu porovná před vytvořením jednotky,
změnou pokladny a počtu. Předem se ověřuje i rozsah příští ceny a identita nové
jednotky. Lodě používají růst ceny nyní; skutečný typ dělostřelectva musí při
R-0095 vstoupit do stejného pravidla. Stav ověření vede
[R-0077](requirements/R-0077.md).

### Evropské odbornosti (R-0074)

`UnitType.EUROPEAN_SPECIALISTS` je společný registr 19 evropsky dostupných
odborností a jejich cen. `UnitSpecialist` používá vybavení a přesuny běžného
kolonisty; existující `UnitMasterBlacksmith` zachovává svou identitu.
`UnitFactory` vytváří stejné typy při nákupu i načítání. `ColonyField` používá
terénově závislou potravinovou odbornost, `ColonyJobChoice` stejný výpočet
pro nabídku práce. Budovy dál používají společnou produkci a vstupní zásoby.
GUI pouze překládá typy a zobrazuje posuvnou nabídku; pravidla nákupu
zůstávají v `Player`. Starý nábor zůstává oddělený do R-0075.
Samotný registr neprokazuje dokončení vojenských, misionářských a dalších
neprodukčních schopností; jejich přejímku sleduje [R-0074](requirements/R-0074.md).

### Délka plavby přes Atlantik (R-0071)

`model.AtlanticVoyageDuration` vlastní výpočet běžné délky plavby z počasí,
směru, odjezdového okraje a základní rychlosti lodi. `AbstractUnit` losuje
počasí až po ověření odjezdu; královský transport používá stejný výpočet.
`PlaceHighSea` drží neměnné počasí a původní délku vedle zbývajících tahů.
`PlaceHighSeasPo` tyto hodnoty ukládá a `PlaceBuilderPo` odmítá neúplná nebo
nekonzistentní data. Načtení nelosuje znovu. Počet zbývajících tahů dál
zobrazuje existující evropská obrazovka; změna nevytváří druhý GUI výpočet.
[R-0071](requirements/done/R-0071.md) sleduje ověření; obrácení plavby a Magellan
mají vlastní navazující požadavky.

### Obrácení plavby (R-0072)

`Model.offerVoyageReversal` připraví potvrzovanou nabídku z registrované lodi,
aktuální plavby, polohy, směru a provozní revize. `Model.reverseVoyage` před
změnou znovu ověří běžící hru, připravený tah, vlastníka a celý snímek.
Revize brání opětovnému použití staré nabídky i po dvou otočeních. Uložený
`PlaceHighSea` odděluje původní směr od současného cíle a drží polohu na trase;
zbývající dobu odvozuje z polohy a délky. Načtení kontroluje původní faktory
R-0071 a shodu odvozeného času. `Ship.tryArriveFromHighSea` sdílí příchod
po běžném tahu i okamžitý návrat po otočení u počátku. Obsazené pobřeží dál
znamená čekání, bez dodatečného tahu nebo změny nákladu. Ověření sleduje
[R-0072](requirements/done/R-0072.md).

### Pořadí národních tahů (R-0052)

`GameManager` řadí kopii aktuálního registru podle interních národních klíčů;
králové následují podle svého národa. Nemění uložené pořadí a obnovení zachová
aktuálního aktéra. `Player.startTurn` nejprve posune plavby přes
`Ship.advanceVoyage` a vyřídí dosavadní imigraci, poté kolonie a nakonec pohyb
a jednotkové práce. `Ship` nadále vlastní pravidla plavby, `Colony` účtování
produkce a `AbstractUnit` obnovu pohybu. `GameManager` drží příznak nedokončené
přípravy; v této době model nepovolí
hráčské příkazy ani reentrantní snímek. `Model` synchronizuje zahájení, konec
tahu a tvorbu snímku na stejné instanci. Souběžný snímek počká na dokončení;
selhání přípravy ponechá instanci nehratelnou a neuložitelnou. DAO získá snímek
před otevřením souboru. GUI sdílí dotaz na připravený lidský tah pro myš,
klávesnici a další tah; při zamítnutí uložení přes `TurnPreparationException`
zachová starý soubor a umožní návrat do menu. `AbstractUnit` dokončuje běžící
práci interně, odděleně od veřejného příkazu pro novou práci.
Ověření a rozsah jsou vedené v [R-0052](requirements/done/R-0052.md);
[evidence hranice přípravy](functional-tests.md#r-0052-preparation-boundary-2026-10-15)
odlišuje cílené ověření a úplnou regresi.

### Jednotkové příkazy Čekat a Vynechat tah (R-0053)

`UnitStorage` vlastní stabilní pořadí aktivních jednotek a v běžném cyklu
vyřazuje jednotky s pokračující prací. Ruční výběr na mapě nadále dovoluje
prohlédnout jednotku bez pohybu. `Model.canSkipUnitTurn` a `skipUnitTurn`
ověřují připravený tah, identitu registrovaného vlastníka i jednotky,
umístění, pohyb a nepřítomnost práce. Příkaz využívá existující `noAction`
a vyčerpá pohyb; žádný nový trvalý příznak se neukládá.
`SelectedUnitManager` vlastní výběr další jednotky a používá modelový příkaz;
GUI navíc blokuje vstup během animace. Před vyčerpáním pohybu si zachová
následující jednotku v pořadí. Výběr ruší nepotvrzený náhled trasy.
Toolbar zobrazuje přeložené ovládání a modelovou dostupnost; klávesové filtry
zpracují Tab/Space před zaměřeným tlačítkem. R-0054 doplňuje předání Escape
před zaměřeným menu: presenter událost spotřebuje pouze při rušení aktivního
náhledu pohybu, jinak zůstává dostupná menu. Přijetí příkazů eviduje
[R-0053](requirements/done/R-0053.md), ověření Escape
[R-0054](requirements/done/R-0054.md).

### Potvrzení útoku (R-0028, rozpracováno)

1. `Unit.canAttack()` určuje schopnost podle aktuálního vybavení. Královská
   továrna v `Model` proto své nové vojáky vybavuje mušketami; nepoužívá
   výjimku pro neozbrojeného krále. Typ jednotky
   samostatně vymezuje přípustný druh cíle. `prepareAttack` pouze čte živý stav:
   aktuálního hráče, registrovanou jednotku, sousedství, známý cíl, terén, pohyb
   a vztahy. UI smí připravit mírový cíl k výslovnému vyhlášení války; běžná AI
   používá pouze již nepřátelské cíle.
2. Neměnný `AttackOrder` váže potvrzení na původní model, jednotky, vlastníky,
   polohy a očekávaný vztah. Při provedení znovu ověřuje všechny podmínky před
   vyhlášením a bojem. Nepronásleduje přesunutý cíl. Přijatý boj vyčerpá pohyb
   obou účastníků; přeživší jej obnoví při svém příštím běžném tahu.
3. Dialog zobrazuje skutečně vybraného obránce a před každým otevřením zahodí
   předchozí souhlas. Povinné potvrzení porušení míru platí i při skrytém rádci.
   GUI kontroluje výměnu modelu a překládá očekávané odmítnutí. Stav cílených
   testů, úplné regrese a publikace vlastní [R-0028](requirements/done/R-0028.md).

### Zakládání a okraj mapy (R-0027)

1. `Model.canFoundColonyAt` je sdílený dotaz na budoucí místo: kontroluje
   registrovaného zakladatele, pevninu mimo hory, rozestup kolonií a cizí
   obsazení. Nepožaduje, aby kolonista už na místě stál. Bezprostřední
   `canUnitBuildColony` navíc vyžaduje běžící hru, aktuálního vlastníka,
   umístění na mapě a zbývající pohyb. Strategie musí před čtením cíle
   respektovat hráčovu znalost mapy.
   Načtení světa zpřístupňuje každému vlastníkovi okolí jeho jednotek na mapě;
   samotné zahájení počítačového tahu neopravňuje k odhalení zbytku světa.
2. `buildColony` před přidělením ID a registrací připraví pracovní místo
   a ověří kapacitu pro vrácené vybavení. Zakladatel dostane dostupné pole
   s nejvyšším výnosem jídla; pokud žádné není, pracuje v radnici. Volba
   AI vyžadující životaschopné pobřeží není obecným zákazem zakládat na
   neúrodné pevnině. Výroba ze středu kolonie podle manuálu zůstává
   samostatnou mezerou.
3. Osm směrových slotů zůstává zachovaných i na okraji mapy. Slot mimo
   skutečnou mapu není platné pracovní místo, nevyrábí a UI jej nekreslí
   ani nepřijímá jako cíl přetažení. Počáteční umístění zakladatele nepřebírá
   pole již obhospodařované jinou kolonií; toto není tvrzení, že jsou
   vyřešené všechny ruční přesuny mezi sdílenými poli. Výsledky a zbývající
   práci vlastní [evidence R-0027](functional-tests.md#rival-first-colony-preparation-2026-10-14).

### První kolonie soupeře (R-0027)

1. `ai.FirstColonyPlanner` je čtecí výběr dalšího kroku přes známá dosažitelná
   pole. Pobřežní místo s dostupným pevninským jídlem řadí podle vzdálenosti,
   výnosu a souřadnic. Neodhaluje mapu a neprovádí pohyb ani založení.
2. `SimpleAiPlayer` vybírá skutečného zakladatele s nejnižším ID a případnou
   vlastní loď. Provádí jednotlivé sdílené modelové akce a mezi nimi čeká na
   dosavadní animační slot. Po vylodění zakladatel musí získat nový pohybový
   bod před založením. Jakmile soupeř vlastní kolonii, tento omezený cíl končí;
   neřeší obecnou expanzi, hospodářství ani obchod.
3. Výběr se obnovuje ze stavu jednotek a uložené znalosti, bez nových
   plánovacích dat v save formátu. `AbstractRobotPlayer.stop` zneplatní i
   již zařazený callback; po zastavení nebo výměně modelu nevydá další
   modelovou akci ani `endTurn`. Model nadále validuje každý příkaz.
   Referenční průchod, úspěšnou úplnou lokální regresi a stav publikace vlastní
   [R-0027](requirements/done/R-0027.md).

### Čtení seznamu jednotek při tahu AI

`UnitStorage` používá pro často čtené členství jednotek `CopyOnWriteArrayList`.
Každý průchod vidí stabilní seznam v původním pořadí i při souběžném přidání
nebo odebrání jednotky královskou AI. Toto omezené řešení neopravňuje k
souběžnému provádění celých herních akcí a nezajišťuje konzistentní snapshot
měnitelných atributů jednotlivých jednotek. Herní validace zůstává v modelu.
Regresní důkaz a stav úplné sady uvádí [evidence R-0023](functional-tests.md#expert-purchase-implementation-2026-10-13).

## Model a rozhodování počítače

V následujících tabulkách jsou názvy relativní k `org.microcol` v
`microcol-game/src/main/java`. Odkaz otevírá příslušný adresář.

| Balíček | Odpovědnost a orientační třídy | Hranice pro další změny |
| --- | --- | --- |
| [kořen org.microcol](../microcol-game/src/main/java/org/microcol/) | Vstup do aplikace, nastavení nativního toolkitu před startem, JavaFX životní cyklus a sestavení závislostí: `MicroCol`, `MicroColApplication`, `ExternalModule`, výměnné vazby pro testy. UI testy přebírají nastavení přes `MicroCol.configureJavaFx()`. | Pravidla a konkrétní obrazovky patří do svých balíčků. |
| [model](../microcol-game/src/main/java/org/microcol/model/) | Svět, hráči, jednotky, kolonie, ekonomika, cesty a tahy: `Model`, `Player`, `AbstractUnit`, `Colony`, `GoodsTrade`, `PathFinder`, `GameManager`, `Calendar`, neměnné evropské cenové nabídky `EuropeTradeQuote`; společná topologie kontinentů a `MicroColException`. | Autoritativní pravidla a změny stavu; nepřidávat závislosti na obrazovkách, JavaFX ani strategiích AI. Efektivní `Unit.getSightRadius` určuje dohled podle typu a výstroje; `getVisibleLocations` jej ořízne na mapu, `Visibility` uchovává objevený terén nezávisle na pohybu. |
| [model.unit](../microcol-game/src/main/java/org/microcol/model/unit/) | Konkrétní druhy jednotek, továrna a jednotkové akce, například `UnitFactory`, `Ship`, `UnitActionPlowField`. Sdílené `Unit`, `AbstractUnit` a `UnitType` jsou nadále v `model`. | Schopnosti a provedení akcí; rozhodování soupeře a animace patří jinam. |
| [model.event](../microcol-game/src/main/java/org/microcol/model/event/) | Události změn a přechodů modelu, včetně zrušitelných událostí před akcí; odběratelé používají `ModelListener`. | Popis herního děje bez UI ovládacích prvků. Nejde o uloženou historii zpráv pro hráče. |
| [model.turnevent](../microcol-game/src/main/java/org/microcol/model/turnevent/) | Zprávy aktuálního přehledu tahu o příjezdu lodí, hladu, ztrátě kolonie a dalších výsledcích: `TurnEvent`, `TurnEventStore`. | Záznamy se ukládají s hrou, protože je nelze spolehlivě obnovit z aktuálního světa. Při ukončení tahu se zprávy hráče čistí; nejde o trvalý archiv. Zobrazení vlastní `gui.screen.turnreport`. |
| [model.store](../microcol-game/src/main/java/org/microcol/model/store/) | Datové objekty uloženého světa a Gson převody: `ModelPo`, `UnitPo`, `CargoPo`, adaptéry akcí a typů. | Ukládaná reprezentace; výpočty herních pravidel zůstávají v modelových objektech. Tento balíček sám nezajišťuje celý souborový save/load. |
| [model.campaign](../microcol-game/src/main/java/org/microcol/model/campaign/) | Kampaně, mise, cíle a jejich posluchače; `GameModel` spojuje misi a `Model`, `GameModelDao` sestavuje a obnovuje hru. Obsahuje i volnou hru `FreePlay`; `FreePlayMap` vybírá přibalený svět a `GameModelDao.createFreePlay` jej připraví před nahrazením rozehrané hry. | Specifické podmínky mise; obecná ekonomika, boj a pohyb patří do společného modelu. |
| [model.campaign.po](../microcol-game/src/main/java/org/microcol/model/campaign/po/) | Obálka celé uložené hry: svět, jméno kampaně/mise, data cílů a souborové čtení/zápis přes `GameModelPoDao`. | Formát a převod dat; `PersistingTool` vlastní adresář a názvy save souborů i příponu. `RecoverySlot` rozlišuje tři body obnovy, `RecoveryCheckpointPolicy` vybírá bezpečné hranice a `GameModelPoDao` nahrazuje soubor atomicky. Nespouštět zde herní akce ani dialogy. |
| [model.campaign.store](../microcol-game/src/main/java/org/microcol/model/campaign/store/) | Uložení postupu kampaně, například dokončených misí, přes `CampaignsDao`. | Odlišovat postup kampaně od rozehrané pozice a od uživatelských nastavení. |
| [ai](../microcol-game/src/main/java/org/microcol/ai/) | Rozhodování evropských a královských soupeřů: `SimpleAiPlayer`, `KingRobotPlayer`, chování jednotek a volba cílů přes `ContinentTargets` nad topologií z modelu. | Volit akce nad společnými pravidly; neobcházet modelové operace a nepřidávat nové závislosti na GUI. Současné vazby na animace jsou výjimkou níže. |
| [i18n](../microcol-game/src/main/java/org/microcol/i18n/) | Překladové klíče, XML resource bundles, jejich načítání, cache a kontrola úplnosti. | Překlad významu; neurčuje herní pravidla, rozložení obrazovek ani okamžik otevření dialogu. |

## Uživatelské rozhraní a podpůrné služby

### Identita kolonií a textové vstupy

Implementace R-0014 odděluje `Player.getDisplayName()` od neměnného
`Player.getName()` používaného jako klíč národa/vlastnictví. Zobrazované jméno
se ukládá v existujícím `extraData`; validaci vlastní model a nastavení ji
používá před spuštěním. Náhled národa nepřepisuje identitu Nizozemska.
Modelové a hráčské cesty prošly úplnou Docker regresí; viz [evidence](functional-tests.md#free-play-implementation-2026-10-13).


Kontrakt [Z02](implementation-plan.md#z02-zalozeni-pojmenovane-kolonie)
odděluje neměnné ID kolonie od upravitelného jména. `Model` vlastní
přidělení ID, normalizaci, jedinečnost jmen a oprávnění k přejmenování;
UI pouze připraví návrh a zobrazí výsledek či chybu. `model.store` ukládá
ID a další volné ID, `PlaceBuilderPo` podle ID obnovuje vazby pracovníků.
Zprávy tahu nesou ID i historický text jména. Samotná shoda čísel z různých
her neznamená totožnost objektů; objektové equality se tímto řezem nemění.
Přetrvávající souborové čtení/zápis zůstává v `model.campaign.po`.

U [sdílených pracovních polí](gap-analysis.md#shared-colony-working-squares)
`ColonyField` poskytuje dotaz na přípustnost a ověření očekávaného pracovníka;
`AbstractUnit` ověřuje přesun před změnou místa a vybavení. `Model.make`
kontroluje jednoznačnost uložených pracovních vazeb před vytvořením živých
jednotek, nezávisle na oprávnění hráče v právě probíhajícím tahu.
`GameController` nahrazuje současnou hru až po úspěšném sestavení nové;
načítací UI zachová původní obrazovku a vysvětlí odmítnutí. Validace sama
nesmí upravovat vstupní DTO ani uložený soubor.

Textové vstupy hráče jsou Unicode a mají oříznuté krajní Unicode mezery.
Dohoda z 13. října 2026 stanoví výchozí limit **64 Unicode code points** pro
jméno hráče i další budoucí textové vstupy; na délku se znovu neptáme, pokud
uživatel výslovně nepožaduje výjimku. Jedinečnost názvů kolonií je samostatné pravidlo.
Zpětná kompatibilita save souborů není nyní požadována; aktuální fixtures
mají povinné identifikátory. Podrobnosti a limity vlastní výše uvedený kontrakt.

| Balíček | Odpovědnost a orientační třídy | Hranice pro další změny |
| --- | --- | --- |
| [gui](../microcol-game/src/main/java/org/microcol/gui/) | Sdílené UI služby a typy: hlavní okno, Guice vazby, barvy, pixelové souřadnice, panely jednotek, klávesové ovládání a hudba. | Prezentace a propojení aplikace; další ekonomické a herní výpočty patří do modelu. Hudba není obecná služba herních zvukových událostí; viz [audio](audio.md). |
| [gui.background](../microcol-game/src/main/java/org/microcol/gui/background/) | Dekorativní pozadí, pruhy a opakované obrazy. | Kreslení bez změn herního terénu a jeho vlastností. |
| [gui.buttonpanel](../microcol-game/src/main/java/org/microcol/gui/buttonpanel/) | Sdílené panely tlačítek a předání jejich akcí, například dalšího tahu. | Pravidla zpracování tahu vlastní model. |
| [gui.dialog](../microcol-game/src/main/java/org/microcol/gui/dialog/) | Dialogy a potvrzení; dnes také `ApplicationController` a `PersistingService`, které koordinují spuštění, načtení a uložení hry. | Dialog získá rozhodnutí hráče; výpočet výsledku a formát uložené hry řeší jejich vlastníci. |
| [gui.dock](../microcol-game/src/main/java/org/microcol/gui/dock/) | Sdílený přístav, nákladní sloty a drag & drop; konkrétní chování dodává kolonie nebo Evropa. | Kapacita, vlastnictví a přenos musí být platné i při jiném vstupu než přetažení myší. |
| [gui.event](../microcol-game/src/main/java/org/microcol/gui/event/) | Záměry UI a posluchače: založení kolonie, orání, jazyk, výběr a zobrazení. | Předat herní akci modelu; modelové události patří do `model.event`. |
| [gui.event.model](../microcol-game/src/main/java/org/microcol/gui/event/model/) | Most mezi hrou a UI: `GameModelController`, `GameController`, `ModelListenerImpl`, zapojení robotů a reakce na mise. | Integrační balíček uvnitř GUI, nikoli druhý model. Nepřidávat sem strategie soupeřů a výpočty pravidel. |
| [gui.image](../microcol-game/src/main/java/org/microcol/gui/image/) | Načítání, skládání a cache obrazů, pobřeží a kosmetických variant. | `WorldMapRandoms` vybírá obrazové varianty; negeneruje herní svět ani bojovou náhodu. |
| [gui.preferences](../microcol-game/src/main/java/org/microcol/gui/preferences/) | Uživatelské nastavení a jeho JSON persistence: jazyk, hlasitost, okno, animace a zobrazení. | Rozehraná partie a její pravidla patří do modelové persistence. |
| [gui.util](../microcol-game/src/main/java/org/microcol/gui/util/) | Sdílené UI komponenty, dialogové základy, clipboard, fonty, animace a souborové pomůcky. | Nepřidávat nesouvisející obecné třídy. `PathPlanning` interpoluje pixely animace; herní cestu hledá `model.PathFinder`. |

## Obrazovky

Všechny tyto balíčky zajišťují prezentaci a vstup. Model vlastní herní
výsledek; UI může ukázat náhled a důvod zamítnutí, ale nemá mít další vlastní
verzi pravidla. [UX](ux.md) a [grafika](graphics.md) popisují společné požadavky.

| Balíček | Konkrétní odpovědnost |
| --- | --- |
| [gui.screen](../microcol-game/src/main/java/org/microcol/gui/screen/) | Volba obrazovky, životní cyklus a hlavní kontejner. `MainPanelPresenter.stop()` ukončí aktuální obrazovku při JavaFX shutdownu; testovací fixture stejný úklid volá po každém scénáři. |
| [gui.screen.game](../microcol-game/src/main/java/org/microcol/gui/screen/game/) | Sestavení herní obrazovky a její klávesové vstupy. |
| [gui.screen.game.components](../microcol-game/src/main/java/org/microcol/gui/screen/game/components/) | Pravý panel, tlačítka, stavový řádek a údaje o poli a jednotkách. |
| [gui.screen.game.gamepanel](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/) | Canvas mapy, viditelná oblast, výběr, kurzor, posun, zvýraznění cesty a animace. |
| [gui.screen.colony](../microcol-game/src/main/java/org/microcol/gui/screen/colony/) | Pracovníci, pole, sklad, budovy a přístav na obrazovce kolonie. |
| [gui.screen.colony.buildingqueue](../microcol-game/src/main/java/org/microcol/gui/screen/colony/buildingqueue/) | Lokalizovaný výběr a explicitní potvrzení projektu, pořadí a odebrání z fronty. Výběr drží původní kolonii/model a kontroluje změnu zobrazeného stavu; pravidla dostupnosti, ceny a zpracování fronty zůstávají v modelu. |
| [gui.screen.europe](../microcol-game/src/main/java/org/microcol/gui/screen/europe/) | Evropský přístav, nákup a nábor jednotek, zboží a přehled plavby; nabídky vybavení na molu zobrazují ceny a důvody odmítnutí z modelu. |
| [gui.screen.market](../microcol-game/src/main/java/org/microcol/gui/screen/market/) | Obrazovky nákupu a prodeje a prezentace cen. |
| [gui.screen.campaign](../microcol-game/src/main/java/org/microcol/gui/screen/campaign/) | Výběr kampaně a mise. |
| [gui.screen.goals](../microcol-game/src/main/java/org/microcol/gui/screen/goals/) | Zobrazení cílů mise a jejich postupu. |
| [gui.screen.statistics](../microcol-game/src/main/java/org/microcol/gui/screen/statistics/) | Zobrazení herních statistik, grafů a základních evropských vztahů; potvrzení vyhlášení války se kontroluje proti původní hře a hráčům před modelovou operací. |
| [gui.screen.turnreport](../microcol-game/src/main/java/org/microcol/gui/screen/turnreport/) | Prezentace uložených zpráv `model.turnevent`. |
| [gui.screen.menu](../microcol-game/src/main/java/org/microcol/gui/screen/menu/) | Hlavní menu, navigace a nastavení volné hry (`ScreenFreePlay`); skutečná volba mapy, validovaný zobrazovaný název hráče a výslovně označený náhled národa. |
| [gui.screen.setting](../microcol-game/src/main/java/org/microcol/gui/screen/setting/) | Ovládání uživatelských nastavení. |
| [gui.screen.colonizopedia](../microcol-game/src/main/java/org/microcol/gui/screen/colonizopedia/) | Herní nápověda. |
| [gui.screen.editor](../microcol-game/src/main/java/org/microcol/gui/screen/editor/) | Nástroje pro editaci mapového podkladu a jeho PO dat. Editor záměrně mění mapu mimo běžné herní tahy; tato cesta není vzorem provádění akcí hráče či AI. |

## Testy a ostatní adresáře

| Místo | Odpovědnost |
| --- | --- |
| [`microcol-game/src/test/java`](../microcol-game/src/test/java/) | Jednotkové a modelové testy u odpovídajících balíčků; nezávislé očekávané výsledky pravidel. |
| [`microcol-test/src/test/java/org/microcol/test`](../microcol-test/src/test/java/org/microcol/test/) | Čitelné hráčské scénáře `TC_...`, jejich příprava a očekávání předávaná metodám `verify…`; bez přímých asercí ve třídách TC. Strukturální test kontroluje toto rozdělení. |
| [`microcol-test/src/test/java/org/microcol/page`](../microcol-test/src/test/java/org/microcol/page/) | Page objects: selektory, kliknutí, souřadnice a čekání na dokončení UI; oddělené metody `verify…` obsahují aserce a čtou skutečný stav. Akce/gettery neobsahují těla asercí. Viz [funkční kontrakt](functional-tests.md#stala-a-citelna-struktura-scenaru). |
| [`microcol-test/src/test/java/org/microcol/model`](../microcol-test/src/test/java/org/microcol/model/) | Pouze příprava souběžné modelové události vyžadující package access (například zánik lodi při otevřeném dialogu). Testovaná hráčská akce nadále prochází UI; adaptér není součástí hry. |
| [`microcol-test/src/main/java/org/microcol/mock`](../microcol-test/src/main/java/org/microcol/mock/) | Náhrady UI služeb potřebné pro testovací prostředí. |
| [`microcol-test/src/test/java/org/microcol/gui/screen/game/gamepanel`](../microcol-test/src/test/java/org/microcol/gui/screen/game/gamepanel/) | Testovací přístup ke skutečnému mapovému kompozitoru; `FogFrameProbe` vloží diagnostickou animaci a zachytí výsledek. Očekávání vyhodnocuje `FogPage`, nejde o produkční vykreslovací cestu. |
| [`microcol-test/src/test/scenarios`](../microcol-test/src/test/scenarios/) | Výchozí uložené pozice a nastavení scénářů; zachovávat podle [testového kontraktu](functional-tests.md). |
| [`microcol-build-tests/src/test/java/org/microcol/build`](../microcol-build-tests/src/test/java/org/microcol/build/) | Kontroly launcheru, publikace a nativních distribucí. |
| [`microcol-game/src/main/resources`](../microcol-game/src/main/resources/) | Herní a UI prostředky, texty, mapy a obrazové podklady; použití popisují příslušné katalogy. |
| [`docs`](index.md) | Autoritativní projektová dokumentace a zdroj veřejného webu. |

Odpovědnosti Maven modulů, distribucí, launcheru a zachovaného `microcol-site`
udržuje samostatný [přehled modulů](modules.md). Balíky P01–P32 v
[implementačním plánu](implementation-plan.md) jsou členění práce; jedna funkce
může zasáhnout několik Java balíčků. Nevytvářet balíček jen podle čísla úkolu.

## Současné přesahy a jejich údržba

Implementované [R-0021](requirements/done/R-0021.md) svěřuje `HighSea` určení pobřeží
a výchozího návratového pole. `Ship` uchovává místo a směr odjezdu a řeší
příjezd v tahu vlastníka; `AbstractUnit` ověřuje odplutí před spotřebou pohybu.
`UnitPo` ukládá oba údaje společně a `UnitFactory` odmítá chybějící původ
rozpluté lodi. Evropa pouze zobrazuje zbývající tahy či čekání na uvolnění cíle.
Úplná regrese i publikace buildu `dev-49-88573a85ac03` byly ověřeny
13. října 2026. Příkazy a výsledky vlastní [testová evidence](functional-tests.md#atlantic-voyage-implementation-2026-10-13).

Následující vazby jsou **pozorované ve zdrojích k uvedené revizi**, nikoli
doklad čistého oddělení nebo seznam oprav nutných před každou další funkcí.

| Současný přesah | Jak postupovat při relevantní změně |
| --- | --- |
| [`AbstractRobotPlayer`](../microcol-game/src/main/java/org/microcol/ai/AbstractRobotPlayer.java) a `SimpleAiPlayer` používají `AnimationLock`/`AnimationManager` z GUI. | Při práci na AI nebo řízení tahu oddělit rozhodování a herní dokončení od čekání na obraz; zachovat pořadí akcí a ověřit oba režimy. |
| `i18n` používá `gui.util.StreamReader`. | Historické umístění čtečky prostředků; tato zbývající vazba leží mimo model. |
| Založení/spuštění/uložení aplikace je rozdělené mezi `gui.event.model`, `gui.dialog.ApplicationController` a `PersistingService`. | Při změně projít celý tok; název `dialog` dnes nezaručuje pouze zobrazování oken. |
| Podmínky orání doplňuje také [`ButtonsGamePanelController`](../microcol-game/src/main/java/org/microcol/gui/screen/game/components/ButtonsGamePanelController.java); AI při nalodění používá jiný vstup než člověk v `GameModelController`. | Při změně těchto akcí sjednotit autoritativní kontrolu a provedení v modelu; ověřit tlačítko, klávesu a AI podle dotčeného rozsahu. Zápis není nově provedená runtime reprodukce. |

Nový kód má respektovat směr `gui → model` a `ai → model`. Stávající přesahy
neopravovat plošně v nesouvisejícím úkolu ani je nerozšiřovat. Vazby modelu
na jiné herní balíčky byly v R-0011 odstraněny; ostatní uvedené přesahy zůstávají.

## Automatická hranice modelu

1. [ModelArchitectureTest](../microcol-game/src/test/java/org/microcol/ModelArchitectureTest.java)
   používá [ArchUnit](https://www.archunit.org/userguide/html/000_Index.html)
   1.5.1 v rozsahu `test`. Běží jako běžný JUnit Jupiter test při `mvn test`,
   včetně povinného CI před publikací distribucí. Není součástí hry.
2. Produkční `org.microcol.model..` nesmí záviset na žádné třídě
   `org.microcol..` mimo vlastní podstrom. Zahrnuté jsou i podbalíčky kampaní,
   persistence a událostí. Testy se neimportují a prázdný výběr selže.
   Přímé vazby na `javafx..` jsou také zakázané. Java a ostatní externí
   knihovny zůstávají povolené; jde o kontrolu závislostí
   v bytecode, nikoli řetězců pro reflexi nebo nepoužitých importů.
3. Výjimka `MicroColException` je v `model`, souborová pomůcka `PersistingTool`
   v `model.campaign.po`. Topologie `Continent`, `Continents` a `ContinentTool`
   je společná; skórování a pořadí cílů v `ai.ContinentTargets` zachovává
   dosavadní rozhodování královské armády.
4. Události mise přenášejí pouze `Missions`. Překladové prostředky zachovávají
   původní české a anglické zprávy; `I18n` překládá enumy bez požadavku,
   aby model implementoval rozhraní překladové služby.
5. Ověření a dodání vlastní [R-0011](requirements/done/R-0011.md). Ručně lze
   spustit `mvn -pl microcol-game -am test -Dtest=ModelArchitectureTest
   -Dsurefire.failIfNoSpecifiedTests=false`. Pravidlo nemá seznam výjimek
   ani zmrazený seznam historických porušení.

## Jak udržet popis platný

1. Před změnou určit vlastníka pravidla, vstup člověka/AI, ukládaný stav
   a dotčené testy. Použít [šablonu zadání](implementation-plan.md#sablona-predani-prace).
2. Při práci v existujícím balíčku přečíst jeho `package-info.java`, pokud
   existuje, a relevantní řádek této mapy. Krátké anglické popisy u hlavních
   balíčků slouží jako orientace v IDE; tato stránka vlastní celková pravidla
   a seznam přesahů.
3. Při přidání balíčku nebo změně jeho odpovědnosti aktualizovat tuto mapu
   a příslušný `package-info.java` ve stejném commitu. Nový podbalíček má mít
   konkrétní ucelenou odpovědnost; další Maven modul není automaticky potřeba.
4. V review zkontrolovat sdílení pravidel a nové závislosti. Větší přesuny
   oddělit od změn chování, zachovat save/load a existující hráčská očekávání.
   Potřebné kontroly určuje [funkční kontrakt](functional-tests.md).
5. Při odstranění zdokumentovaného přesahu aktualizovat jeho řádek;
   neudržovat druhou odlišnou mapu v zadáních, README nebo agentích instrukcích.

### Vybavení kolonisty — ověřeno 13. 10. 2026

1. `UnitFreeColonist` vlastní důvod nedostupnosti, náhled změn zásob a úplnou
   validaci před vybavením i vrácením vybavení. `ColonyWarehouse` poskytuje
   kapacitu skladu; UI nepočítá vlastní limity. Kladné hodnoty náhledu znamenají
   vrácené zboží, záporné spotřebu.
2. `DialogEquipment` zobrazuje náhled a při změně zásob vyžaduje nové potvrzení.
   Konkrétní změnu stále provádí existující modelová operace. Přesun do pracovního
   místa musí ověřit cíl před automatickým vrácením vybavení.
3. Tato změna je součástí [R-0017](requirements/done/R-0017.md). Modelové hranice a hráčské cesty prošly
   [úplnou regresí](functional-tests.md#free-play-implementation-2026-10-13).

### Individuální přeprava pasažérů (R-0022)

1. Implementované R-0022 z 13. října 2026 soustřeďuje oprávnění a účtování převodu
   do `AbstractUnit`: `canEmbark`, `canUnitDisembarkAt` a
   `canDisembarkToEuropePier`. Mapa, přístav a AI používají stejné kontroly.
2. `Model.isOwnedCoastalPort` vymezuje vlastní kolonii sousedící s vodou v osmi
   směrech. Tento předpoklad pro R-0022 opravuje dřívější přijetí každé vlastní
   kolonie za přístav; neřeší spojitost oceánu. Uložení obnovuje vazby přes
   `PlaceBuilderPo` a nepouští herní příkazy při načítání.
3. Výběrový dialog vrací jednoho pasažéra nebo zrušení. Před potvrzeným převodem
   model znovu kontroluje živé objekty, místo, vlastnictví a kapacitu. Zápis
   obou stran vazby předchází události nalodění. Úplná regrese prošla
   lokálně i v CI (781 modelových a 98 funkčních/podpůrných případů).
   Po opravě synchronizace testovacího gesta vyšel build `dev-51-b3ba0efcf67e`. Výsledky vlastní [testová evidence](functional-tests.md#individual-passenger-transfers-2026-10-13).

### Odbornost najatého kolonisty (R-0023)

1. Implementované R-0023 používá `model.unit.UnitMasterBlacksmith` jako konkrétní
   podtyp `UnitFreeColonist`. Typ je dostupný i při konstrukci místa a ukládá
   se přes stávající `UnitPo.type`; vybavení zůstává oddělené od odbornosti.
2. Autoritativní dostupnost nákupu a účtování vlastní `Player`. Evropský
   dialog zobrazuje typ a cenu z modelu a váže jedno potvrzení na otevřenou
   hru a hráče. Odborník se nepřidává do existující nabídky imigračních kandidátů.
3. Vybrané modelové a nákupní UI cesty i skutečná cesta odborníka do kolonie
   a kovárny včetně jejího stavového popisu i nahrazení hry během nabídky
   prošly. Úplná regrese lokálně i v CI ověřila 794 modelových a 105
   funkčních/kontrolních případů; build `dev-52-2a464170c54d` je zveřejněný. Důkazy vlastní
   [záznam testů](functional-tests.md#expert-purchase-implementation-2026-10-13).


### Sdílené vykládání vozů a lodí (R-0024)

1. `Colony.getUnitsInPort` poskytuje vlastní nákladní dopravce přítomné
   v kolonii jako `UnitWithCargo`, ve stejném pořadí registru. Kolonijní
   panel tento seznam používá bez raw přetypování; evropský seznam se nemění.
2. `ColonyWarehouse` zůstává jediným vlastníkem kapacity a atomického převodu.
   Obrázek a názvy doplňuje GUI; tažení ikony celé jednotky zůstává pouze
   lodní operací. Před obnovenou nabídkou množství UI překreslí aktuální
   zásoby, aby odpovídaly modelu. Pravidla pasažérů stále vyžadují loď.
3. Cílené ověření prošlo 66 modelovými a 34 funkčními/kontrolními případy.
   Úplná regrese prošla 801 modelovými a 113 funkčními/kontrolními případy.
   Po opravě obnovení popisu mapového pole prošlo úplné CI 802 modelovými
   a 114 funkčními/kontrolními případy; build `dev-54-13001e6ac6b8` je publikovaný.
   Aktuální důkazy vlastní
   [evidence R-0024](functional-tests.md#wagon-unloading-implementation-2026-10-13).

### Hlídka a nalodění při odjezdu

Rozpracované [R-0073](requirements/done/R-0073.md) přidává do `model` kontrolované
příkazy Hlídka/Aktivovat. Trvající rozkaz vlastní `model.unit`, jeho přenos do
uložené hry `model.store`. Automatický výběr vynechává hlídku, výslovný výběr
rozkaz neruší. `AbstractUnit` propojuje odjezd lodě se stávajícími pravidly
nastoupení; kapacita, pořadí podle ID i cena pohybu zůstávají modelové operace.
Model také probouzí hlídku při viditelném cizím pohybu do sousedního pole.
Mapa a evropské molo pouze nabízejí platný příkaz a zobrazují jeho výsledek.
Stav ověření je v [evidenci](functional-tests.md#r-0073-sentry-and-automatic-boarding-2026-10-16).

### Chov koní a pořadí produkce (R-0078)

`Colony` sdílí výpočet chovu mezi náhledem a provedením. Výpočet používá
původní stav koní a stájí, volné místo skladu a jídlo zbývající po spotřebě
obyvatel a narození kolonistů. Syrová výrobní bilance zůstává oddělená od
konečného náhledu, aby se odečty růstu a chovu při provedení neopakovaly.
Chov se zapíše jednou před dokončením stavební fronty; nově dokončené stáje
proto ovlivní další tah. Při hladomoru se nechová, zatímco ostatní produkce
přeživších zachovává svůj dosavadní postup. UI čte společnou bilanci bez
mutace; uložené zboží a stavby nepotřebují další schéma. Přejímku vede
[R-0078](requirements/R-0078.md).

### Průmyslové vstupy a výstupy (R-0079)

`ConstructionType` určuje poměr každého stupně. `ConstructionSlot` sdílí
celočíselný výpočet vstupního omezení a zaokrouhlení s celou budovou;
`Construction` nejprve sečte rozpočty všech pracovníků. Nedostatek se tak
zaokrouhlí jednou za budovu, nikoli zvlášť za každého pracovníka. Stávající
pořadí zdrojů v `Colony` zpřístupní vyrobené nástroje zbrojnímu řetězci
ve stejném tahu. Náhled i skutečný sklad používají stejné pravidlo;
načtení nevyžaduje nové schéma. Přejímku vede [R-0079](requirements/R-0079.md).

### Pobřežní výrobní předpoklady (R-0080)

`ColonyField` vlastní dotaz na chybějící dok pro vodní práci, společný pro
nabídku, náhled i změnu pracovního místa. Dok, suchý dok a loděnice poskytují
stejnou rybářskou způsobilost. `Model` kontroluje tentýž předpoklad uloženého
rybáře. `UnitType` a `ColonyBuildingQueue` vyžadují pro galeonu a fregatu
loděnici a stávající pobřežní pravidlo `Model.isOwnedCoastalPort`; fronta
ověřuje podmínku při přijetí i dokončení. UI překládá konkrétní modelový
důvod a nepřevádí pracovníka ani materiál při odmítnutí.
Rozsah a přejímku vede [R-0080](requirements/R-0080.md).

### Tisk a společná produkce zvonů (R-0081)

`Construction.getProduction()` počítá jeden základní zvon radnice i bez
pracovníků, přičítá příspěvky státníků a aplikuje tiskárnu (+50 %) nebo
noviny (+100 %, nahrazují tiskárnu). Zaokrouhluje dolů jednou nad celým
součtem. Stejný výsledek používá náhled i změna skladu. Dokončení stavby
probíhá po produkci, takže nový bonus platí od následující aktualizace.
Tiskové budovy nejsou dalšími producenty a nepotřebují nové schéma uložení.
Další politické a otcovské bonusy zůstávají samostatnými požadavky.
Viz [R-0081](requirements/R-0081.md).

### Dobrovolný odchod z opevněné kolonie (R-0082)

`Colony.canVoluntarilyLoseResident()` vlastní pravidlo minimálně tří
vnitřních obyvatel pro palisádu, pevnost a vyšší opevnění. `Unit.canLeaveColony()`
je společný dotaz pro rozhraní a validační kontroly umístění jednotky.
Přesun mezi pracemi téže kolonie nesnižuje populaci; vnější jednotky se
nezapočítávají. Dobrovolné operace ověřují pravidlo před změnou místa či
výbavy. Hlad a boj používají vlastní vynucené ztráty; nepoužívají tento zákaz.
Přeživší kolonie si ponechá opevnění i při méně než třech obyvatelích,
včetně uložení a načtení. Viz [R-0082](requirements/R-0082.md).
