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
R-0087 přidává uloženou nepotvrzenou mírovou nabídku v téže dvojici.
`Model` určuje viditelný sousední kontakt kolonisty, sílu armád a cenu;
`PeaceOffer` drží podmínky nabídky. Potvrzení znovu ověří kontakt, válku,
síly, pokladny a Franklina před jediným převodem zlata a uzavřením míru.
Členství Franklina při vstupu do běžné aktualizace hráče připraví nabídky
až po aktualizaci kolonií a jednotek. Existující nabídku v libovolném směru
zachová. Uložený `TurnEventPeaceOffer` pouze oznamuje novou nabídku;
zobrazení hlášení ani Continue nevyjednává a nepřevádí zlato.
GUI nabízí potvrzení, zrušení a odložení; načtení samo nabídku nepřijme.
R-0089 rozšiřuje stejného vlastníka o spojenectví, nabídku za 200 zlata,
historii skutečné agrese a čekání po ukončení. `Model` plánuje důsledky obrany
spojence před mutací boje; událost královské války zpracuje `GameManager` po
ukončení celého kola. Uložené číslo zpracovaného kola brání opakování při Continue.
`EuropeanRelations` drží pro bývalé spojence doklad společného pole;
`UnitStorage` ověřuje každou dvojici vlastníků přes pravidlo modelu. Doklad
povoluje zachování existující pozice při načtení, nikoli nový vstup. Ukládání
vynechá pole, kde již obě strany nestojí; načítání odmítne neplatné doklady.
Tyto cesty se ověřují v cílených testech R-0089; plná regrese náleží hranici dávky.
R-0088 používá stejný kontakt a vojenskou sílu pro tribut. `EuropeanRelations`
ukládá samostatný návrh tributu a poslední potvrzený požadavek dvojice;
`Model` před platbou ověří roli, deset tahů čekání i nezměněné podmínky.
Potvrzené odmítnutí zahájí stejné čekání, ale nemění zlato ani vztah.
Sdílený dotaz `Model.areHostile` odděluje diplomatické oprávnění od obsazení
polí cizími jednotkami. Cizí mírová jednotka nadále brání vstupu a vylodění.
Králové nemají evropský záznam míru/války; dotaz pro ně zachovává dosavadní
nepřátelství k cizím vlastníkům a vznik/řízení královských sil zůstává v cestě
vzpoury. AI používá stejné útočné a pohybové dotazy jednotek jako modelový
příkaz. Potvrzené porušení smlouvy útokem propojuje rozpracované R-0028,
jak je popsáno níže.
Před vstupem do dobývaného přístavu `Colony` naplánuje únik všech cizích
nepřátelských lodí. Neutrální obsazení nebo chybějící bezpečné sousední moře
útok odmítnou před losem, spotřebou pohybu či změnou vlastníka. AI používá
tentýž útočný dotaz jako hráč. Samotný pohyb cizí kolonii nedobývá;
převzetí následuje pouze po vyhraném boji. Dobývající jednotka zůstává na mapě.


`GameManager` odlišuje zahájení, dosažený herní konec a zahození instance přes
`Model.stop()`. Zastavení zneplatní další akce před oznámením posluchačům;
zahájený stav zůstává dostupný pro čtení a uložení. Výsledek herního konce
se ukládá jako `gameFinished`, aby načtení nepovolilo další tah. Samotné
zahození instance se neukládá: běžnou rozehranou pozici lze načíst do nové
instance. Integrace AI/UI a výsledky ověření jsou vedené v R-0026; tento popis
není potvrzením dokončení celého požadavku.


### Daňové rozhodnutí a bojkot (R-0102)

`Player` vlastní jediný `TaxState` kolonizačního národa: sazbu, četnost
požadavků, čekající rozhodnutí a dluhy podle zboží. Královský adaptér čte
stejnou sazbu. `TaxDemand` a `TaxProtest` popisují potvrzovaný výsledek;
model znovu ověřuje jeho platnost před změnou zásoby, podpory a dluhu.
`GameManager` rozlišuje uložitelnou pauzu po přistěhovalectví a před produkcí.
Vyřešení daně pokračuje zbývající přípravou, neopakuje příchody ani aktualizace.
`model.store` ukládá výslovná současná data; načtení nedoplňuje chybějící stav.

Bojkot kontrolují skutečné operace nákupu/prodeje, vybavení a dovozu nástrojů
pro dokončení stavby. Aritmetická cenová nabídka zůstává dostupná také pro
revoluční výjimku celnice; běžný vývoz bojkot respektuje. GUI zobrazuje společné
modelové možnosti, odložení a návrat k rozhodnutí. R-0103 přidává
`TaxArrearsQuote` a modelovou platbu jediného dluhu: ověřuje aktéra, částku,
generaci požadavku a pokladnu. Dluh odstraní před oznámením změny zlata,
aby posluchač viděl úplný výsledek a nemohl stejnou platbu opakovat.
`BackTaxDialog` zobrazuje nabídku, zrušení a očekávané zamítnutí; ceny trhu
nemění již uloženou výši dluhu.
Cílené ověření a hranice přijetí jsou vedené u [R-0102](requirements/done/R-0102.md).

### Prozkoumaný terén a současný dohled (R-0070)

`Visibility` uchovává trvale prozkoumaný terén. `Player.isCurrentlyVisible`
odvozuje současný dohled ze živých vlastních jednotek a okolí vlastních kolonií;
`Player` vlastní poslední pozorování cizích kolonií v neměnném `ColonySighting`.
`Model` aktualizuje pozorování při herních změnách, zatímco dotazy GUI jsou
jen pro čtení. `model.store.ColonySightingPo` ukládá polohu, název, vlastníka
a tah pozorování; načtení nepřepisuje údaje o skrytých koloniích živým stavem.
Mapa, panel a tooltip používají stejný povolený pohled. Cizí jednotky se mimo
dohled nezobrazují; zapamatovaná kolonie má označení posledního pozorování.
Filtrování událostí brání skrytým pohybům a cizím bojům prozradit neviditelná
místa animací. Útočný dotaz modelu požaduje současný dohled.
Stav ověření vede [R-0070](requirements/done/R-0070.md).

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
vyžaduje skutečné členství z R-0104. Stav přejímky vede [R-0075](requirements/done/R-0075.md).

### Ceny opakovaných nákupů (R-0077)

`Player` vlastní počty nákupů podle typu a výpočet aktuální ceny; `UnitType`
zůstává zdrojem základní ceny. `PlayerPo.unitPurchaseCounts` ukládá řídkou mapu
jmen typů na počty, prázdná mapa znamená žádný nákup. Dialog předává zobrazenou
cenu do modelové operace, která ji znovu porovná před vytvořením jednotky,
změnou pokladny a počtu. Předem se ověřuje i rozsah příští ceny a identita nové
jednotky. Lodě používají růst ceny nyní; skutečný typ dělostřelectva musí při
R-0095 vstoupit do stejného pravidla. Stav ověření vede
[R-0077](requirements/done/R-0077.md).

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
[R-0071](requirements/done/R-0071.md) uchovává ověření běžného výpočtu.
Navazující Magellanův účinek používá skutečné členství v Kongresu: efektivní
mapový příděl lodí získá +1, ale běžná délka stále používá základní rychlost.
Pouze nový odjezd ze západního okraje do Evropy půlí vypočtenou délku nahoru.
Již probíhající plavba si zachová původní délku i po příchodu otce; načítání
musí přijmout takovou dřívější běžnou délku. Stav nové přejímky vede
[evidence integrace](functional-tests.md#r-0104-magellan-integration-2026-10-17).

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
| [diagnostics](../microcol-game/src/main/java/org/microcol/diagnostics/) | Lokální omezený záznam obnovitelných chyb: pevné události, verze a platforma, bez zpráv výjimek a hráčského textu. Popis vlastní [diagnostika](diagnostics.md). Dialogy uložení, odchodu, načtení a nedokončeného tahu uvádějí složku záznamu. | Jen zápis záznamu; nesmí přerušit hru, uložení ani načtení. Model na něj nezávisí; volají ho GUI a spouštěcí kód. |

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
| [gui.preferences](../microcol-game/src/main/java/org/microcol/gui/preferences/) | Uživatelské nastavení a jeho JSON persistence: jazyk, hlasitost, okno, animace a zobrazení. `SettingService.load` při nedostupném úložišti vrací poslední známé nastavení (nebo výchozí hodnoty), znovu zkouší čtení a nic nepřepisuje ani nepřesouvá; `save`/`update` selhání hlásí. | Rozehraná partie a její pravidla patří do modelové persistence. Nastavení nesmí při ztrátě umístění uložených her přerušit probíhající hru. |
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
| [gui.screen.congress](../microcol-game/src/main/java/org/microcol/gui/screen/congress/) | Zobrazení neměnné nabídky otců, národních zvonů, cíle a členství. Výslovná volba používá hlídanou operaci hráče; UI nelosuje ani neuděluje odměny. |
| [gui.screen.turnreport](../microcol-game/src/main/java/org/microcol/gui/screen/turnreport/) | Prezentace uložených zpráv `model.turnevent`. |
| [gui.screen.menu](../microcol-game/src/main/java/org/microcol/gui/screen/menu/) | Hlavní menu, navigace a nastavení volné hry (`ScreenFreePlay`); skutečná volba mapy, validovaný zobrazovaný název hráče a výslovně označený náhled národa. |
| [gui.screen.setting](../microcol-game/src/main/java/org/microcol/gui/screen/setting/) | Ovládání uživatelských nastavení. |
| [gui.screen.colonizopedia](../microcol-game/src/main/java/org/microcol/gui/screen/colonizopedia/) | Herní nápověda: tlačítko a klávesa H otevírají dialog s tématy pravidel, hledáním a texty z `i18n`. Obsah je statický; dialog nečte ani nemění model. |
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

### Misionářská role a odbornost (R-0163)

`UnitFreeColonist` ukládá misionářskou roli odděleně od vojenské role a civilní
odbornosti; `UnitPo.missionary` je povinný u každého kolonisty. `Model` vlastní
dotaz na přijetí i operaci vysvěcení: evropské molo nebo vlastní kolonie s
kostelem či katedrálou, bez aktivního rozkazu a bez práce uvnitř kolonie.
`UnitFreeColonist.hasEffectiveMissionaryExpertise` je jediný dotaz odbornosti
pro budoucí misijní, sporné a podněcovací akce; konverzi a ostatní akce
nevlastní. GUI pouze čte dotaz a zobrazuje důvod. Stav ověření vede
[evidence R-0163](functional-tests.md#r-0163-missionary-ordination-2026-10-19).

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
[R-0078](requirements/done/R-0078.md).

### Průmyslové vstupy a výstupy (R-0079)

`ConstructionType` určuje poměr každého stupně. `ConstructionSlot` sdílí
celočíselný výpočet vstupního omezení a zaokrouhlení s celou budovou;
`Construction` nejprve sečte rozpočty všech pracovníků. Nedostatek se tak
zaokrouhlí jednou za budovu, nikoli zvlášť za každého pracovníka. Stávající
pořadí zdrojů v `Colony` zpřístupní vyrobené nástroje zbrojnímu řetězci
ve stejném tahu. Náhled i skutečný sklad používají stejné pravidlo;
načtení nevyžaduje nové schéma. Přejímku vede [R-0079](requirements/done/R-0079.md).

### Pobřežní výrobní předpoklady (R-0080)

`ColonyField` vlastní dotaz na chybějící dok pro vodní práci, společný pro
nabídku, náhled i změnu pracovního místa. Dok, suchý dok a loděnice poskytují
stejnou rybářskou způsobilost. `Model` kontroluje tentýž předpoklad uloženého
rybáře. `UnitType` a `ColonyBuildingQueue` vyžadují pro galeonu a fregatu
loděnici a stávající pobřežní pravidlo `Model.isOwnedCoastalPort`; fronta
ověřuje podmínku při přijetí i dokončení. UI překládá konkrétní modelový
důvod a nepřevádí pracovníka ani materiál při odmítnutí.
Rozsah a přejímku vede [R-0080](requirements/done/R-0080.md).

### Tisk a společná produkce zvonů (R-0081)

`Construction.getProduction()` počítá jeden základní zvon radnice i bez
pracovníků, přičítá příspěvky státníků a aplikuje tiskárnu (+50 %) nebo
noviny (+100 %, nahrazují tiskárnu). Zaokrouhluje dolů jednou nad celým
součtem. Stejný výsledek používá náhled i změna skladu. Dokončení stavby
probíhá po produkci, takže nový bonus platí od následující aktualizace.
Tiskové budovy nejsou dalšími producenty a nepotřebují nové schéma uložení.
Další politické a otcovské bonusy zůstávají samostatnými požadavky.
Viz [R-0081](requirements/done/R-0081.md).

### Dobrovolný odchod z opevněné kolonie (R-0082)

`Colony.canVoluntarilyLoseResident()` vlastní pravidlo minimálně tří
vnitřních obyvatel pro palisádu, pevnost a vyšší opevnění. `Unit.canLeaveColony()`
je společný dotaz pro rozhraní a validační kontroly umístění jednotky.
Přesun mezi pracemi téže kolonie nesnižuje populaci; vnější jednotky se
nezapočítávají. Dobrovolné operace ověřují pravidlo před změnou místa či
výbavy. Hlad a boj používají vlastní vynucené ztráty; nepoužívají tento zákaz.
Přeživší kolonie si ponechá opevnění i při méně než třech obyvatelích,
včetně uložení a načtení. Viz [R-0082](requirements/done/R-0082.md).

### Společná stavební práce (R-0109)

Jedinou zásobu práce vlastní sklad kolonie jako `HAMMERS`. Položky fronty
uchovávají identitu, typ a pořadí; nemají vlastní kladiva ani takové pole
v aktuálním formátu uložení. `ColonyBuildingQueue` používá stejný skladový
součet pro náhled a dokončení. Při chybějících nástrojích či předpokladech
nic neutratí; platné dokončení odečte přesnou cenu a zachová přebytek.
Za aktualizaci dokončí nejvýše první projekt. Změna, odstranění i prázdná
fronta ponechává práci ve skladu. [R-0083](requirements/done/R-0083.md) naváže
nákupem chybějících materiálů na tento společný stav.

### Zaplacené dokončení projektu (R-0083)

`ColonyBuildingQueue` vlastní neměnnou nabídku `ConstructionRushQuote`
a její potvrzení. Chybějící kladiva stojí dvě zlaté za kus; nástroje
používají společnou nákupní nabídku Evropy včetně daně. Stávající zásoby
se spotřebují pouze do ceny projektu, dokoupené materiály nejdou přes sklad.
Před změnou pokladnice, materiálů nebo fronty se znovu ověří vazba nabídky
a frontu, její stav, vlastník, zdroje, cena i předpoklady dokončení.
UI zobrazuje modelovou nabídku a vyžaduje výslovné potvrzení; samo cenu
nepočítá. Viz [R-0083](requirements/done/R-0083.md).

### Podpora nezávislosti (R-0100)

`Colony` vlastní uloženou místní svobodu a poslední koloniální příslušnost.
Skutečnou produkci zvonů připíše při aktualizaci kolonie; dokončený odchod
obyvatele upraví limit podle zbývajících pracovníků. Přesun mezi pracemi uvnitř
stejné kolonie limit nemění. Dobytí Evropanem mění příslušnost, královská okupace
ji zachovává a vyrobené zvony odečítá. `model.store.ColonyPo` vyžaduje obě hodnoty;
načítání nedoplňuje chybějící údaje ani neopravuje neplatný limit.
`RebelSupport` představuje přesný celočíselný zlomek včetně počtu obyvatel,
`Player` sčítá příspěvky příslušných kolonií. Bolivarův bonus pochází z jejich
koloniální příslušnosti. GUI pouze zobrazuje modelové hodnoty v politickém
přehledu a v informaci názvu kolonie; zaokrouhlení patří jen do zobrazení.
Produkční bonusy R-0101 a vyhlášení nezávislosti R-0105 mají tento výpočet použít.

### Kongres a příchod otce (R-0104)

`Player` vlastní uložený `Congress`: nabídku kandidátů, zvolený cíl, národní
zvony, členy a případnou čekající Jonesovu fregatu. `Colony` připisuje novou
výrobu zvonů nezávisle na místním skladu. Po aktualizaci kolonií hráč dokončí
nejvýše jeden cíl a zachová přebytek. `FounderOffer` váže potvrzení na
původního hráče a generaci nabídky. Model vytváří odměnu a uloženou zprávu
příchodu; rozhraní pouze překládá a zobrazuje. Continue neopakuje aktualizaci.
Rozsah a přejímku vede [R-0104](requirements/done/R-0104.md).

### Stavební podmínky otců (R-0084)

`ConstructionType.getRequiredFounder` určuje Smitha pro šest továren a
Stuyvesanta pro celnici. `ColonyBuildingQueue` ověřuje členství současného
vlastníka při zařazení i dokončení, včetně placeného dokončení. Chybějící
členství pozastaví rozpracovaný projekt bez spotřeby zásob; hotové budovy
se tímto pravidlem neodstraňují. GUI překládá společný důvod a váže
potvrzení také na snímek členství. Nevzniká další uložený příznak odemčení.
Rozsah a přejímku vede [R-0084](requirements/done/R-0084.md).

### Automatický vývoz celnicí (R-0085)

`Colony` vlastní výběr obchodovatelného zboží, ověření potvrzeného snímku
a automatický prodej přebytku nad 100. `ColonyPo.exportGoods` je povinný
explicitní seznam; nové kolonie začínají prázdné. Výroba může během přípravy
tahu dočasně přesáhnout sklad. Po spotřebě, chovu a stavbě následuje export
a až potom ořez kapacity s hlášením ztrát; běžné hráčské přesuny zůstávají
omezené kapacitou. Export před změnou ověří celou dávku a výslednou pokladnu,
použije sdílenou evropskou čistou cenu a za revoluce její polovinu.
`TurnEventCustomHouseExport` ukládá skutečné množství a příjem; GUI pouze
potvrzuje výběr a překládá uloženou zprávu. Bojkoty z R-0102 zatím nejsou
propojené; úplnou přejímku vede [R-0085](requirements/done/R-0085.md).

### Rozkaz opevnění jednotky (R-0106)

`model.unit.UnitAction` vlastní přechod z opevňování na účinné opevnění
v příští běžné aktualizaci vlastníka. `model.store` uchovává fázi; načtení ji
neposouvá. `Model` ověřuje vydání a zrušení rozkazu před změnou pohybu.
GUI používá tytéž dotazy pro povolení tlačítka a rozlišení obou fází.
Výběr jednotky neruší rozkaz. Zrušení nevrací spotřebovaný pohyb.
Implementace je rozpracovaná; výslednou bojovou sílu vlastní R-0093/R-0094.

### Výpočet obyčejného pozemního boje (R-0093/R-0094)

Implementace používá `model.CombatStrengths`
jako neměnný rozpis obou stran nabídky `AttackOrder`. Potvrzení znovu ověří
její vstupy před změnou diplomacie, pohybu a uloženého náhodného stavu. GUI
pouze překládá jednotlivé členy a zaokrouhluje jejich zobrazení. Metrika
`getMilitaryStrenght` pro diplomacii/statistiky se tím nepřeceňuje.

`Model` vlastní jediný uložený stav SplitMix64 pro boj; `model.store.ModelPo`
jej vyžaduje i při načtení. Počasí plaveb zůstává nezávislé. Prohra obyčejného
vojáka či dragouna mění vybavení, nikoli jeho identitu či polohu. Událost
rozlišuje poraženou a skutečně zničenou jednotku. Lokální přejímku a nezávislý stav publikace vede
[evidence R-0093/R-0094](functional-tests.md#r-0093r-0094-ordinary-land-combat-2026-10-16).

### Dělostřelectvo (R-0095)

`model.unit.UnitArtillery` vlastní poškození jedné jednotky; nemění se její
identita ani nákupní typ. `UnitPo.artilleryDamaged` je povinný pouze pro dělo.
Výroba, cenová historie, nákladní prostor a rozkaz opevnění používají stávající
modelové operace. `CombatStrengths` připojuje současné poškození a venkovní
postih ke stejnému snímku boje; GUI výsledek pouze překládá a vybírá obrázek.
Chybějící domorodá role není nahrazena kontrolou příznaku počítačového hráče.
Přejímka a její dosud chybějící návaznosti jsou vedené v [R-0095](requirements/done/R-0095.md).

### Oprava poškozené lodi (R-0098)

`PlaceShipRepair` je jediným zdrojem poškození lodi: drží původní místo,
cíl, fázi přepravy/opravy a zbývající aktualizace vlastníka. `Model` vybírá
opravnu podle průchodné vodní cesty a při ztrátě cíle znovu rozhodne;
`Player.startTurn` posouvá opravu před běžným obnovením pohybu jednotek.
`model.store` uchovává průběh bez jeho posunu při načtení.
`CargoSlot` a společné operace pasažérů brání manipulaci s nedostupným
nákladem. GUI pouze čte průběh v evropském přehledu a překládá uložené
`TurnEventShipRepair`; otevření přehledu ani zprávy neprovádí opravu.
Napojení poškození na námořní boj vlastní R-0097. Stav přejímky vede
[R-0098](requirements/done/R-0098.md).

### Námořní výsledek (R-0097)

`CombatStrengths` vlastní šest lodních základů a útočný bonus.
`AttackOrder` zmrazí `NavalCombatPreview`: náklad, tržní ceny a možné opravy.
Před dvěma uloženými bojovými losy se živý stav znovu ověří. Model přesune
jen plánovanou kořist do skutečně volných podpalubí a poté odstraní poraženou
loď nebo použije opravu R-0098. `NavalCombatOutcome` uchová výsledné ztráty,
kořist a původní polohy; GUI je nemusí dopočítávat z odstraněné jednotky.
Animace používá původní souřadnice a následné hlášení sděluje osud nákladu
včetně pasažérů. Soukromnické diplomatické výjimky vlastní R-0099.
Přejímka je vedená v [R-0097](requirements/done/R-0097.md).

### Korzárská výjimka (R-0099)

Námořní oprávnění k útoku proti korzárovi nebo korzárem patří do modelu;
nevytváří nepřátelský evropský vztah ani neotevírá útoky na pozemní jednotky.
Příkaz si pamatuje skutečný vztah zvlášť od nutnosti vyhlášení války a před
bojem jej znovu ověří. GUI jen vysvětluje výjimku a používá stejný potvrzovací
a výsledkový dialog jako ostatní útoky. Stav přejímky vede
[R-0099](requirements/done/R-0099.md).

### Vojenské povýšení (R-0111)

Civilní odbornost a veteránský vojenský stav jsou oddělené. Kolonista ukládá
veteránský příznak, zakoupený typ veterána jej má přirozeně; vybavení jej
nemaže. Sociální postup trestance a sluhy zachová registrovaný objekt a ID.
Po platném pozemním boji model vyhodnotí jediný další uložený los pro způsobilého
vítěze; Washington tento postup zaručí bez dalšího losu. Námořní a děloví
vítězové tuto větev nepoužívají; způsobilý voják porážející dělo ano. Příkaz znovu ověří vstupy hodnosti a Washingtona
před účinky. `MilitaryPromotion` zmrazí skutečný krok pro událost boje;
GUI hlásí výsledek po animaci a nevypočítává povýšení. Přesný rozsah a stav
ověření vede [R-0111](requirements/done/R-0111.md).

### Náboženská produkce (R-0110)

`ConstructionType` určuje náhradní základ kaple/kostela/katedrály a výstup
pracovního místa. `ConstructionSlot` použije existující odbornost;
`Construction.getProduction` sečte základ a pracovníky a jednou aplikuje
Pennových 150 % se zaokrouhlením dolů. Náhled kolonie i skutečný tah používají
stejný výpočet. Dokončení stavby následuje po produkci: nový náhled je okamžitý,
skutečný přírůstek nového stupně přijde v dalším tahu. Národní imigrační fond
přebírá skutečné vyprodukované kříže. [Přejímka](requirements/done/R-0110.md).

### Výuka a odborná praxe

R-0107 přidává do `model.Colony` ověřené přiřazení učitele a žáka,
neměnné přehledy kurzů a nabídky dalšího kroku. `EducationSkills` vlastní
přijatou matici škol; GUI pouze zobrazuje nabídku modelu. Učitel obsazuje
školní místo, žák zůstává na své práci. Zrušení kurzu ponechá učitele ve
škole pro dalšího žáka; výměna učitele vyžaduje uvolnění místa.
Pokrok se připisuje jednou při aktualizaci kolonie po skutečné produkci,
výuka před odbornou praxí. Výroba s omezenými vstupy rozděluje již
vyúčtovaný výstup stabilním pořadím pracovníků. Nová odbornost zachovává
identitu objektu a vojenskou hodnost. `model.store` ukládá kurzy a historii
práce; `TurnEventEducationCompleted` uchovává dokončený výsledek pro hlášení
bez opakování výuky při zobrazení či Continue.
Implementace a skutečné kontroly jsou odděleny v [evidenci](functional-tests.md#r-0107-education-work-2026-10-17).


### Dohled po příchodu De Sota (R-0069)

Společný dotaz jednotky odvozuje minimální dohled dvě pole ze skutečného
členství současného vlastníka v Kongresu. Specializovaný dotaz kolonisty
používá stejnou úpravu, takže vybavení nemůže bonus obejít. `Player` při
skutečném příchodu otce ihned odhalí okolí všech vlastních jednotek přímo
na mapě. Pasažéři, pracovníci a jednotky mimo mapu neodhalují samostatně.
Nevzniká další uložený bonus: členství a již objevený terén používají stávající
uložení. `MapManager` po přípravě lidského tahu obnoví vykreslené okraje
mlhy, aby nezůstaly na nově odkrytých polích. Výsledky přejímky vede [evidence R-0069](functional-tests.md#r-0069-de-soto-sight-integration-2026-10-17).


### Brewster a přerušená příprava přistěhovalectví

Rozpracovaná návaznost R-0075/R-0076 používá členství v Kongresu jako jediný
zdroj Brewsterova účinku. `RecruitmentPool` vlastní současné tři identity,
počty a uložený příznak čekající volby. `Player` při skutečném příchodu otce
mění pouze nevhodné kandidáty; již existující lidé se nemění. Další losování
používá přijatých 90 % svobodných kolonistů a 10 % odborníků.

`GameManager` řídí výslovnou uložitelnou hranici před výrobou kolonií.
Dosavadní příprava plaveb se při Continue neopakuje; po poslední potvrzené
volbě pokračují zbývající etapy právě jednou. Běžný `TurnStartedEvent`
následuje až po dokončení, zatímco `ImmigrationChoiceRequiredEvent` žádá
jen rozhodnutí. Ostatní herní příkazy zůstávají zablokované. Uložení smí
zachytit celou čekající volbu, nikoli mezistav účtování či probíhající přípravu.
GUI používá stejné náborové okno, odlišuje bezplatnou volbu od placené cesty
a umožňuje odložení s návratem přes Evropu. Skutečné ověření vede
[evidence](functional-tests.md#r-0075r-0076-brewster-integration-2026-10-17).

### Hlášení stavby čekající na nástroje (R-0086)

`ColonyBuildingQueue` při běžné stavební fázi připraví hlášení pro jinak
způsobilý projekt s dostatkem kladiv a nedostatkem nástrojů. Každý tah
vlastníka hlásí současný nedostatek znovu; žádný samostatný stav potlačování
se neukládá. `TurnEventConstructionWaitingForTools` zachovává kolonii,
typ projektu a tehdejší nedostatek. Načtení ani zobrazení hlášení nespouští
stavební fázi. GUI překládá snímek a před otevřením kolonie znovu ověří hru
a současné vlastnictví; otevření nemění zásoby. Stav přejímky vede
[R-0086](requirements/done/R-0086.md).


### Obrana kolonie a pracovníků (R-0096)

`Colony` vybírá ozbrojenou obranu, civilní skupinu nebo Revereho milici;
`ColonyDefense` a `AttackOrder` zmrazí skutečná pracovní místa, bojiště,
vybavení a bezpečný únik lodí. `AbstractUnit` ověří nový plán před prvním
losem a provede společné bojové účtování. Jen vyhraný boj s civilní skupinou
nebo milicí volá interní převzetí kolonie. Veřejný přímý příkaz k obsazení
byl odstraněn včetně jeho pohybových a AI volání.
Samostatný útok na pracovní pole používá stejný rozkaz, ale převádí pouze
poraženého neozbrojeného pracovníka. Dokončený odchod používá společné
účtování obyvatel, vzdělávání, limitu liberty a případné prázdné kolonie.
Revereho vítězný obránce opustí práci, pokud není posledním obyvatelem;
tato vynucená událost nemění pravidlo dobrovolného odchodu z opevnění.
`ColonyBattleOutcome` předává GUI výsledek a výslovné bojiště; animace
nevyžaduje mapovou polohu pracovníka. Přejímku vede
[R-0096](requirements/done/R-0096.md); plná regrese zůstává na hranici dávky.


### Politické výrobní bonusy (R-0101)

`PoliticalProduction` je neměnný rozpis odvozený z přesného `RebelSupport`
a obtížnosti. `Colony.startTurn` předává jediný snímek výrobním výpočtům
před i po hladomoru a účtování praxe. `ColonyField` a `ConstructionSlot`
upravují pouze obsazená místa s kladným základem, po profesní úpravě;
`Construction` následně zachová procentní bonusy a omezení vstupní surovinou.
Neobsazená výroba ani chov nejsou pracovním místem. Náhledy počítají čerstvé
hodnoty; `ColonyJobChoice` zahrne přírůstek obyvatel při příchodu pracovníka
zvenčí. GUI pouze zobrazí společný rozpis. Ukládají se vstupy podpory,
nikoli opakovaně přičítaný bonus. Stav ověření vede
[evidence R-0101](functional-tests.md#r-0101-political-production-2026-10-18).

### Nezávislost a vojenské role

R-0105 ponechává pravidla vyhlášení a plán mobilizace v `Player` a přesné
podíly v `RebelSupport`. `MilitaryRole` určuje základ síly; `UnitFreeColonist`
uchovává roli odděleně od civilní odbornosti a veteránství. `UnitPo` a
`UnitFactory` ukládají a ověřují příslušnost role a umístění jednotky.
Vstup do kolonijní práce obnoví civilní roli před oznámením přesunu.
`AttackOrder` a obrana kolonie zahrnují roli do kontroly aktuálnosti boje;
`AbstractUnit` provádí zničení a `ColonyBattleOutcome` rozlišuje zničení od
ztráty výstroje. GUI používá modelový výhled mobilizace, potvrzení a stejné
bojové výsledky. [Cílené ověření](functional-tests.md#r-0105-independence-and-military-roles-2026-10-18)
nenahrazuje dosud čekající přijetí celé druhé dávky.


## Konečné zásoby ložisek (R-0128)

`WorldMap` vlastní nezávislé zbývající zásoby stříbra a rudy pro každou konečnou
surovinu; `MineralReserve` představuje jejich neměnnou hodnotu. `WorldMapPo`
a `MineralReservePo` ukládají obě množství a při načtení odmítají chybějící,
duplicitní či neplatné údaje ložiska. Zásoby se doplní pouze při vytvoření,
nikoli načtením nebo uložením. Změna nerozšiřuje odpovědnosti GUI ani AI.

`Terrain` a `ColonyField` počítají náhled bez mutace. Příspěvek ložiska se
omezuje zbývající zásobou až po započtení profese a politických vlivů.
`Colony.startTurn()` připraví odběr podle skutečné produkce po hladomoru před
změnou dovedností a odečte jej právě jednou; celá mapa odběrů se validuje před
změnou zásob. Spotřeba vytěžené rudy v navazující výrobě není důvodem zásobu
vrátit. Vyčerpané ložisko zachová identitu i běžnou produkci podkladu.

[Průběžné ověření z 20. října 2026](functional-tests.md#r-0128-mineral-depletion-2026-10-20)
prošlo; úplná přejímka dávky zůstává samostatným krokem.

## Trvalý přesun jednotky (R-0130)

1. `Model` ověřuje zadání a zrušení rozkazu, živou identitu, vlastníka a dostupný pojmenovaný cíl. `AbstractUnit` drží cíl a stav přerušení; používá společný `PathFinder` a pravidla jednoho kroku. `GameManager` pokračuje po dokončení přípravy tahu právě aktuálního vlastníka. Načtení přes `UnitPo` pouze obnovuje a ověřuje stav; jednotkou nepohybuje.
2. GUI používá nabídku modelu a společné ovládání. Přesun z GUI běží mimo FX vlákno kvůli animacím; při dokončení přípravy tahu přímo ve FX vlákně posluchač nečeká na animaci téhož vlákna. Oznámení přerušení a zrušení používají existující turnové události. [Cílené ověření z 2026-10-20](functional-tests.md#r-0130-persistent-go-to-2026-10-20) nenahrazuje úplnou akceptaci dávky.

## Rozpuštění jednotky a čtení mapy (R-0131)

1. `Model` poskytuje důvod odmítnutí a neměnnou nabídku rozpuštění. Před změnou znovu ověří jednotku, vlastníka, fázi tahu, umístění, prázdný náklad a přesné vrácené či ztracené vybavení. Skutečnou výbavu poskytuje kolonista, kapacitu skladu `ColonyWarehouse`. GUI pouze potvrzuje nabídku a ztrátu přebytku; bojové zničení používá původní samostatnou cestu.
2. `UnitStorage` při mapových dotazech čte jedno zachycené umístění jednotky. Tím zamezuje chybě, kdy mezi ověřením mapového umístění a čtením souřadnic jednotka nastoupí do nákladu. Nejde o úplný transakční snímek světa. [Lokální regresní evidence](functional-tests.md#r-0131-disband-and-regression-repairs-2026-10-20) uvádí rozsah ověření a zbývající akceptaci dávky.

## Přistěhovalci na molu (R-0135)

1. `Player` označuje nové přistěhovalce pouze při placeném náboru a příchodu získaném kříži. Jednotka uchovává ochotu a případnou přednost, `Model` ověřuje změny a jedinou přednost na vlastníka. `EuropePier` poskytuje stejné pořadí jako společný sběr cestujících při odjezdu v `AbstractUnit`; GUI nevytváří vlastní pořadí ani další cestu změny nákladu.
2. `UnitPo` ukládá volby a načtení je pouze ověřuje. Nástup zruší čekající přistěhovalecký stav, takže navrácený cestující není automaticky novým přistěhovalcem. [Cílené testy](functional-tests.md#r-0135-immigrant-boarding-2026-10-20) nenahrazují úplnou akceptaci dávky.

## Rychlé převody nákladu (R-0137)

1. `Model` vytváří a znovu ověřuje nabídku jednoho převodu podle živé kolonie, dopravce, prostoru, množství a aktuální prodejní nabídky. Pořadí nakládky i vykládky určuje model; `CargoSlot` a `ColonyWarehouse` zůstávají vlastníky skutečné změny zásob. Nabídka nepředstavuje prodej ani nákup.
2. Ovládání kolonie ukazuje přesné podmínky a důvody odmítnutí. Rychlé příkazy mají vlastní řádek, aby neposouvaly zavírání pod název kolonie. [Lokální evidence](functional-tests.md#r-0137-quick-cargo-2026-10-20) uvádí cílené testy a zbývající akceptaci dávky.


## Vyhození nákladu přes palubu (R-0138)

1. `Model` vlastní způsobilost lodi a nabídku `JettisonOffer`, která váže konkrétní nákladový prostor, množství a polohu či stav plavby. Před odebráním přes `CargoSlot.removeCargo` znovu ověří nabídku; běžné vykládání a obchod nadále používají vlastní omezení.
2. Sdílený `JettisonFlow` propojuje mapu a atlantické ovládání s výběrem množství a potvrzením. GUI nemění náklad přímo. Dotaz na trasu používá zachycenou polohu, aby souběžné odplutí nepřerušilo obnovu ovládání. [Cílené ověření](functional-tests.md#r-0138-jettison-2026-10-20) nenahrazuje úplnou akceptaci dávky.


## Evropské obchodní účty (R-0145)

1. `Europe` ukládá neměnné `EuropeTradeAccount` podle národa a komodity. `Player` započítává skutečně vypořádaný obchod, `Colony` vývoz celnice a `ColonyBuildingQueue` pouze dovezené nástroje při dokončení stavby. Nové hodnoty se vypočítají před změnou zásob či zlata; přetečení nesmí způsobit částečný obchod.
2. `EuropeanTradePanel` čte účty v existující obrazovce statistik. Revoluční celnice je označená podmnožina prodejů, nikoli další příjem. Ukládání neprovádí obchod znovu a report neodhaluje cizí náklad. [Cílená evidence](functional-tests.md#r-0145-european-trade-accounts-2026-10-20) rozlišuje modelové kontroly, skutečnou UI cestu a kontrolu snímků.


## Produkce středu kolonie (R-0146)

1. `TownCommons` vybírá druhou surovinu ze sdíleného terénního základu a určuje její uloženou identitu. `Terrain` poskytuje základ a oprávněný bonus, `Colony` připojuje výstup k existujícímu výpočtu jídla, výroby a skladů. Střed není další pracovní místo; UI čte `getTownCommonsProduction()`.
2. Výběr suroviny se při načtení neopakuje. Náhled nečerpá ložisko; při skutečné aktualizaci se čerpá pouze konečný minerální bonus. Obnovitelné zdroje ani Prime Timber nevstupují do čerpání rezerv. [Pravidlo a tabulka](gap-analysis.md#r-0146-refinement) a [cílené ověření](functional-tests.md#r-0146-town-commons-2026-10-20) rozlišují přijaté chování od dosud čekající úplné regrese dávky.


### Domorodá sídla a znalost mapy

`NativeNation`, `NativeSettlementClass`, `NativeSettlement` a `NativeSettlements`
v balíčku `model` vlastní veřejnou identitu sídel a validaci jejich umístění.
Sídla nejsou evropské `Colony` ani evropský `Player`. `Model` brání překrytí
založenou kolonií a ukládá znalost identit po jednotlivých hráčích; získává ji
skutečným dohledem, nikoli pouhým odkrytím terénu. GUI čte pouze hráči známá
sídla. `store.NativeSettlementPo` a `NativeSettlementKnowledgePo` nesou povinná
data aktuálního formátu. Základní obranný bonus a počáteční bohatství jsou
atributy pro navazující pravidla; tento základ ještě neprovádí nativní boj,
obchod ani pravidelné doplňování pokladny. Viz [R-0150](requirements/done/R-0150.md)
a [cílené ověření](functional-tests.md#r-0150-native-settlement-identity-2026-10-20).


`NativeRelations` je společným vlastníkem kmenového hněvu a místního poplachu
vůči jednotlivým evropským národům. `Model` sbírá skutečné jednotky, obyvatele
a pevnosti; `Player` spouští aktualizaci ve své přípravě tahu a jednorázové
vynulování při příchodu Pocahontas. Ukládání samo vztahy nemění. Transakce
s půdou dodávají vstup za nezaplacené užívání podle [R-0152](requirements/done/R-0152.md);
ostatní pravidla vlastní [R-0153](requirements/done/R-0153.md).

`NativeLandRights` vlastní trvalé platby a jednorázové zabrání půdy podle evropského národa a souřadnice. `Model` poskytuje neměnnou nabídku, validuje platbu před změnou zlata, práv a pokladnice sídla a sdílí výběr prvního pole mezi náhledem a založením kolonie. Platby při zakládání se provedou až společně s potvrzením jména. Dokončené využití půdy dodává vstup do `NativeRelations`; Minuit ruší pouze budoucí nezaplacené pozemkové vlivy. GUI pouze zobrazuje známé nabídky a volá model; dialog po přesunu pracovníka čeká na konec tažení. Aktuální formát ukládá povinné `nativeLandRights`, `nativeLandTakings` a pokladnici sídla. Viz [R-0152](requirements/done/R-0152.md) a [ověření](functional-tests.md#r-0152-native-homeland-purchase-2026-10-20).

`PlayerNative` je explicitní domorodý účastník společného pořadí tahů a `UnitNativeBrave` používá společnou identitu, umístění a pohyb jednotek. Domov je uložené ID sídla téhož národa, případně explicitní stav bez domova. Nativní příprava obnovuje jednotky, ne evropskou ekonomiku; `NativeRobotPlayer` přijme vlastní oznámení tahu a vybírá legální nájezd nebo domovskou hlídku přes společné operace modelu; klidný bojovník neútočí. `GameManager` řadí domorodé národy před Evropany, zachovává společnou hranici kalendáře a uložení. Model vylučuje domorodé hráče z evropské diplomacie, obchodu a zakládání kolonií; vytvoření volné hry jim nepřidává evropské ekonomické záznamy. GUI používá znalost domovského sídla z modelu, nikoli skryté souřadnice. Viz [R-0151](requirements/done/R-0151.md) a [cílené ověření](functional-tests.md#r-0151-native-actors-and-home-linked-braves-2026-10-20).

`TradeRoutes` vlastní jednu uloženou kolekci tras se stabilními ID a názvy jedinečnými pro vlastníka. `TradeRouteRules` ověřuje navazující dosažitelné úseky podle známé mapy; `MovementPolicy` sdílí pravidla vstupu na políčko se skutečnými jednotkami. Náhled nevytváří živého dopravce. `Model` validuje vytvoření před změnou kolekce nebo čítače ID. Editor drží pouze rozpracované volby, které při každém novém otevření vynuluje. Aktuální formát vyžaduje `tradeRoutes` a `nextTradeRouteId`; automatické přesuny a náklad vlastní navazující požadavky. Viz [R-0139](requirements/done/R-0139.md) a [ověření](functional-tests.md#r-0139-named-trade-route-creation-2026-10-20).

`TradeRouteStopProcessor` provádí vykládku a nakládku v uloženém pořadí prostřednictvím existujících atomických skladových a evropských transakcí. `Model` ověřuje vlastníka, dopravce a zastávku před změnou; `TradeRouteVisit` uchovává zpracovanou návštěvu přes uložení a načtení. UI pouze konfiguruje seznamy a zobrazuje výsledky. [R-0140](requirements/done/R-0140.md), [ověření](functional-tests.md#r-0140-ordered-trade-route-cargo-2026-10-20).

`TradeRouteNavigator` pokračuje v uloženém přiřazení dopravce na hranici přípravy vlastníkova tahu. Používá sdílený `PathFinder`, běžné kroky jednotky, skutečnou plavbu přes Atlantik a existující zpracování zastávky. `TradeRouteAssignment` ukládá cíl a důvod přerušení; načtení nepřesouvá jednotku. UI čte stav a zadává modelové operace. Viz [R-0141](requirements/done/R-0141.md) a [ověření](functional-tests.md#r-0141-assigned-repeating-routes-2026-10-20).

Přiřazení trasy uchovává přijaté zastávky každého dopravce samostatně. Změna plánu nemění probíhající úsek ani odpočet plavby; navigátor přebírá nový plán na další hranici zastávky nebo při opravě přerušené trasy. Modelové zrušení a smazání odstraňuje příkazy a související odkazy bez přesunu nákladu nebo obnovení pohybu. Viz [R-0142](requirements/done/R-0142.md) a [ověření](functional-tests.md#r-0142-route-editing-and-cancellation-2026-10-21).

`RivalTradeCycle` volí omezený zásobovací cíl soupeře a používá běžné modelové přesuny, plavbu, náklad a evropské nabídky. `Player` ukládá `RivalTradeObjective` s ID kolonie, dopravce a fází; AI před akcí ověřuje aktuální vlastnictví a podmínky. Neplatné cíle vyřadí, blokované ponechá pro příští tah. GUI neřídí obchodní rozhodování. Viz [R-0091](requirements/done/R-0091.md) a [ověření](functional-tests.md#r-0091-rival-trade-and-supplies-2026-10-21).

`RivalPlayerFactory` ponechává produkční volbu `SimpleAiPlayer` a umožňuje testu lidské expedice TC101 izolovat nesouvisející náhodné pohyby evropských soupeřů. Královské a domorodé roboty tato volba nemění. `ListenerManager` doručuje události nad snímkem posluchačů, takže souběžná registrace či odebrání nenaruší probíhající oznámení. Odebraný posluchač může obdržet již zahájené oznámení. Viz [regresní ověření](functional-tests.md#second-batch-regression-repairs-2026-10-21).

R-0092 přidává `RivalExpansionCycle` pro jedinou druhou kolonii soupeře. AI vybírá známý legální cíl a volného zakladatele podle zásob a obyvatel původní kolonie; pohyb, nalodění a založení provádí sdílený model. `Player` ukládá stabilní identitu cíle, zakladatele a případné lodi; pěší cesta loď nepotřebuje. Obchodní a zakládací úkol nesdílejí loď. Vhodná loď čekající na dostatečné zásoby neopouští přístav náhodným pohybem. Viz [cílené ověření](functional-tests.md#r-0092-rival-second-colony-expansion-2026-10-21); úplná regrese dávky dosud čeká.

Rozpracované R-0065 přidává balíček `model.generation` pro validované nastavení a deterministické sestavení terénu, řek, zdrojů a domorodých osad/jednotek v současném modelovém schématu. GUI pouze sbírá volby a zobrazuje chyby; modelová vrstva připraví kandidáta před nahrazením hry. Integrace používá stávající `ReferenceExpedition` a `GameModelDao`. Cílené testy a vizuální kontrola prošly: [evidence](functional-tests.md#r-0065-generated-new-world-2026-10-21). Úplná regrese dávky čeká.

Základ R-0154 ponechává výpočet domorodých vztahů v `NativeRelations`. Balíčkový modelový vstup přijímá typované dokončené výsledky `NativeRelationOutcome`; GUI a AI nesmějí měnit hněv přímo. Ukládají se zbytky prodaných zbraní a identita započtených akcí. Skutečné modelové transakce musejí před změnou ověřit své podmínky a dodat jedinečnou uloženou identitu; jejich napojení zatím čeká. Viz [ověřený rozsah](functional-tests.md#r-0154-native-relation-outcome-foundation-2026-10-21).

R-0129 ukládá značky ztracených měst v mapě a vyhodnocuje je ve společném modelu po legálním vstupu průzkumníka. Model vlastní náhodu, odměny, ztrátu jednotky a pohřebiště přes `NativeRelationOutcome`; GUI pouze vykresluje viditelnou značku a uložené hlášení. Generátor přidává reprodukovatelné značky, editor je při uložení zachovává. Viz [cílené ověření](functional-tests.md#r-0129-lost-city-rumors-2026-10-21); další producenti domorodých událostí zatím čekají.

Rozpracované R-0155 přidává `gui.screen.nativesettlements` pro klávesnicově přístupný přehled známých osad. Dialog a stavový řádek čtou `NativeRelationView` poskytovaný modelem; pravidla postoje, změny alarmu a oprávnění k údajům zůstávají v modelu. Oznámení změny obnovuje prezentaci bez zápisu do vztahů. Cílené ověření prošlo; úplná regrese třetí dávky čeká. Viz [evidence R-0155](functional-tests.md#r-0155-native-alarm-display-2026-10-21).

R-0173 rozšiřuje společné `CombatStrengths`, `AttackOrder` a vyhodnocení boje o domorodé role, terénní léčku a obranu dělostřelectva proti skutečnému domorodému útočníkovi. Model vlastní síly, validaci před mutací, náhodu, následky porážky a jednorázový vztahový účinek. GUI pouze překládá společný rozpis. Uložená role bojovníka je povinná; AI volba nájezdu zůstává R-0156. Viz [cílené ověření](functional-tests.md#r-0173-native-land-combat-2026-10-21).

R-0156 ponechává volbu cíle a hlídky v `ai.NativeRobotPlayer`; `Model` poskytuje domov a postoj bojovníka, pravidla znalosti, pohybu a boje. Běžný nájezd respektuje dva čtverce a znalost vlastního národa. Placené cílení R-0166 ještě není zapojené. Viz [ověřený rozsah](functional-tests.md#r-0156-ordinary-native-raids-and-patrol-2026-10-21).

Základ R-0157 používá `NativeVisitRules` a neměnné možnosti modelu pro přehled návštěvy v `gui.screen.nativesettlements`. `MovementPolicy` brání běžnému evropskému průchodu přes sídlo; zrušení nabídky neprovádí akci. `NativeTeachableSkills` vybírá uloženou dovednost z přirozených místních výnosů, bez evropských profesí či staveb. Vlastníci jednotlivých návštěvních akcí zatím čekají. Viz [cílené ověření](functional-tests.md#r-0157-native-visit-chooser-foundation-2026-10-21).

R-0148 vlastní kurz v `NativeSettlement` a umístění žáka v `PlaceNativeCourse`. `Model` validuje přijetí a zrušení; evropská příprava tahu posouvá kurz a uděluje dovednost stejné osobě. Dokončený či zrušený kurz zachová žáka při blokovaném výstupu, bez dalšího udělení nebo obnovení výuky. GUI pouze nabízí potvrzení, průběh a zrušení; uložené hlášení oznamuje výsledek. Viz [cílené ověření](functional-tests.md#r-0148-native-teaching-2026-10-21).

Základ R-0159 ponechává přirozenou nabídku a poptávku v `NativeMarketRules`, stav zásob v sídle a kontakt v `NativeContacts`. Výpravy jsou oddělené podle přepravce; ukládání zapisuje pouze skutečně otevřené výpravy bez změny živého stavu. Nativní příprava tahu doplňuje zásoby a pokladnu. `Model` validuje kontakt a vstup; GUI provádí další akci až po uzavření předchozího modálního dialogu. Skutečné převody R-0160/0161 zatím čekají. Viz [ověření](functional-tests.md#r-0159-native-market-foundation-2026-10-21).

R-0160 přidává neměnné `NativeSaleQuote` a uloženou revizi obchodu. Model kontroluje aktuální podmínky i kapacitu před změnou nákladu, zlata a vztahů; smlouvání losuje až po ověření potvrzované nabídky a výsledek ukládá do výpravy. Přijaté zboží sídla je oddělené od přirozeně doplňované nabídky. GUI zobrazuje modelové množství a cenu a potvrzuje akci až po uzavření přehledu. Viz [cílené ověření](functional-tests.md#r-0160-native-sale-and-gifts-2026-10-21).

R-0161 ukládá nákupní fázi a výsledek slevy do skutečné výpravy přepravce. Model dovolí po prodeji nakoupit i právě vyprázdněnému přepravci, aniž by povolil prázdnou první obchodní návštěvu. `NativePurchaseQuote` váže cenu, kapacitu, zásoby a revizi; potvrzení nejprve ověří všechny podmínky a pak převede zboží a zlato jednou. Návrat z potvrzení zachovává výběr pouze v prezentaci, potvrzený výsledek smlouvání se ukládá. Viz [ověření](functional-tests.md#r-0161-native-purchases-2026-10-21).

R-0158 ukládá výsledek návštěvy náčelníka podle evropského národa a sídla. `Model` kontroluje podmínky před losem a převodem; `NativeChiefRules` vlastní pravděpodobnosti. Informační výsledek rozšiřuje pouze prozkoumaný terén přes `Visibility`, nikoli aktuální dohled na cizí jednotky. GUI potvrzuje riziko a zobrazuje skutečný výsledek. Viz [cílené ověření](functional-tests.md#r-0158-chief-encounter-2026-10-21).

Základ R-0164 ukládá misionáře v `PlaceNativeMission`, se stejnou identitou a vazbou na sídlo. `Model` vlastní založení, validaci a aktualizaci vztahů; evropská příprava tahu posouvá jediný uložený časovač. Účinná odbornost se čte živě z role a otců zakladatelů. Pátá nehostilní aktualizace čeká na integraci konverze R-0149. GUI pouze potvrzuje podmínky a čte stav mise. Viz [ověření](functional-tests.md#r-0164-mission-foundation-2026-10-21).

R-0149 spotřebovává pátou aktualizaci mise ve stejné přípravě tahu. Model vlastní los, legální pozemní cestu k nejbližší kolonii, původ konvertity a kumulativní nečinnost. Při návratu uvolní umístění i vazby mise/výuky; pravidla práce a výroby používají společné modelové operace. GUI čte označení a zobrazuje uložená hlášení. Viz [ověření integrace](functional-tests.md#r-0149-native-converts-2026-10-21).

R-0165 drží výpočet soupeření misionářů v `NativeContestRules`; `Model` před losem ověřuje neměnnou nabídku včetně obou identit a revize mise. Přes společné umístění odstraní pouze poraženého a při vítězství převede stejného návštěvníka do mise. GUI pouze potvrzuje riziko a zobrazuje výsledek. Viz [cílené ověření](functional-tests.md#r-0165-rival-mission-contest-2026-10-22).

### Zahraniční srovnání a znalost kontaktů (R-0168)

`Player` uchovává evropské kontakty získané skutečným dohledem nad jednotkou nebo kolonií; `PlayerPo` je ukládá. Čtení přehledu kontakt nevytváří. `Model.getForeignPowerComparisons` omezuje souhrny podle hráče, kontaktu a de Wittova členství, používá základní síly ze společného `CombatStrengths` a aktuální kolonie. `Europe` vlastní desetitahové okno čistých příjmů z evropských prodejů, uložené v `EuropePo`; okno se posouvá podle tahů příslušného národa. GUI vykresluje modelový výsledek a lokalizuje čísla, nepočítá pravidla ani neuchovává druhý zdroj ekonomických hodnot. Cílené ověření z 22. října 2026 je v [evidenci](functional-tests.md#r-0168-foreign-power-comparison-2026-10-22); úplná regrese dávky ještě čeká.

### Výkup pokladů (R-0175)

Model vytváří nabídku čistého výnosu a provádí jednorázový výkup. `UnitTreasure` ukládá potvrzený královský odvoz (identitu kolonie a částku); vlastníkova aktualizace znovu ověří polohu, vlastnictví a přístup do Evropy. Sdílený dialog v `gui.dialog` pouze zobrazuje nabídku a předává potvrzení. Obrazovky kolonie a Evropy zpřístupňují odpovídající cestu. Implementace z 22. října 2026 prošla [cíleným ověřením](functional-tests.md#r-0175-treasure-redemption-2026-10-22); úplná regrese dávky zůstává otevřená.

Obchod s cizí kolonií (R-0167) používá modelovou nabídku a atomický převod skutečného nákladu, skladu a zlata. Balíček `gui.screen.foreigncolony` zobrazuje modelové limity a potvrzení; pravidla cen a oprávnění zůstávají v modelu. [Cílené ověření](functional-tests.md#r-0167-foreign-colony-trade-2026-10-22) nenahrazuje úplnou regresi.


### Evidence královské armády (R-0185)

`model.RoyalRoster` vlastní trvalé identity a složení královské armády, stav rezervy,
odeslaných a zničených členů a zbytek vyprodukovaných zvonů. `PlayerKing` ji ukládá
přes `model.store`; produkce koloniálního hráče ji rozšiřuje pouze před vyhlášením
nezávislosti. Načtení ověřuje složení podle obtížnosti a cyklu růstu, nepřidává
náhrady za zničené členy. Kongres pouze čte a zobrazuje stav. Skutečnou přepravu
a propojení členů s bojovými jednotkami vlastní R-0186. Dílčí ověření je uvedeno
v [testové evidenci](functional-tests.md#r-0185-targeted-roster-verification-2026-10-22).


### Revoluční bojové bonusy (R-0178)

`model.CombatStrengths` určuje bombardování kolonie a přepadení v terénu podle
role, příslušné Koruny, vyhlášení nezávislosti a místa boje. Sdílený výpočet
používá náhled i rozhodnutí boje; GUI pouze zobrazuje jeho pojmenované bonusy.
`UnitFreeColonist` a `UnitPo` uchovávají původ doručeného intervenčního vojáka;
načtení odmítá příznak na nesprávné roli nebo vlastníkovi. Převzetí jednotky
a přechod do civilní práce původ ruší. Samotné doručení zůstává v R-0190.
[Dílčí ověření](functional-tests.md#r-0178-targeted-revolutionary-combat-verification-2026-10-22)
nenahrazuje úplnou akceptaci dávky.


### Nasazení REF (R-0186)

`model` validuje odeslání a opětovné naložení skutečných královských transportů;
`RoyalRoster` váže jednotky na trvalé členy a odděluje zajetí od zničení.
`ai.KingRobotPlayer` vybírá pobřežní cíl, trasu a návrat pro zbývající rezervy,
používá sdílenou plavbu, pohyb, náklad a boj. Převzatí obyvatelé kolonie nejsou
novými členy REF. Kongres zobrazuje i zajaté členy. Uložená hra zachovává vazby,
cíl a počet neúspěšných tahů; přehled výsledků je v
[testové evidenci](functional-tests.md#r-0186-targeted-ref-deployment-verification-2026-10-23).


### Podpora příčiny při útoku na kolonii (R-0188)

`CombatStrengths` čte přesný zlomek podpory z `Colony.getRebelSupport()` a politickou
příslušnost kolonie. Přidává jediný útočný bonus pro příslušného povstalce nebo
Korunu; náhled i výsledek používají stejný výpočet. Snapshot uchovává identitu
vlastníků, příslušnost i zlomek, takže změna zaokrouhleně stejné podpory také
zneplatní potvrzení. GUI pouze formátuje samostatný bonus; pravidla politické
produkce se nemění. Viz [ověření](functional-tests.md#r-0188-targeted-cause-support-verification-2026-10-23).


### Cíl zahraniční pomoci po vyhlášení nezávislosti

`model.InterventionTarget` vlastní pevný cíl, skutečně vyrobené zvony a jednorázově získaný nárok. `Player` jej zahajuje před produkcí prvního povstaleckého tahu a ukládá přes `store.InterventionPo`; načtení ani zobrazení nepřičítá produkci. Kongres a hlášení tahu pouze zobrazují modelový stav. Fyzická dodávka patří do R-0190, následné skórování do R-0196. [Cílené ověření R-0189](functional-tests.md#r-0189-targeted-intervention-target-verification-2026-10-23) nenahrazuje úplnou přejímku dávky.


`Model.deliverInterventionForces` nyní ověřuje získaný a dosud nevyužitý nárok, příslušnost hráče, přístupnost zvoleného přístavu a kapacitu ID před vytvořením osmi jednotek. Výběr nejnižšího ID používá geografickou dostupnost; blokace zachovává čekající stav místo přesunu do dalšího přístavu. Úspěch ukládá příznak doručení spolu s hlášením, načtení samo dodávku neprovádí. [Ověření R-0190](functional-tests.md#r-0190-targeted-intervention-delivery-verification-2026-10-23) zatím nezahrnuje následnou integraci přednosti porážky R-0192.

### Placené skupiny veteránů (R-0169, 2026-10-23)

`Player` uchovává intervaly, nabídku a zaplacené skupiny. `Model` vybírá kontaktovaného mírového vydavatele, ověřuje potvrzení před platbou a doručuje veterány pomocí společných pravidel přístavu a blokády používaných intervencí. `MercenaryOfferDialog` zobrazuje modelovou nabídku na aktuální obrazovce; přehled evropských vztahů umožňuje návrat k nevyřízené nabídce. Modelová událost oznamuje skutečné doručení v přehledu tahu. Počítačové národy sdílejí stav nabídky, nová nákupní strategie AI není součástí změny. [Cílené ověření](functional-tests.md#r-0169-targeted-mercenary-verification-2026-10-23); úplná akceptace dávky čeká.
