# Plán implementace MicroCol podle manuálu

Follow-up closure: [R-0256](requirements/done/R-0256.md) records the final three accepted answers without changing gameplay readiness.

Refinement 2026-10-20: [R-0255](requirements/done/R-0255.md) records the 38 answered topics through R-0219; gameplay implementation and existing readiness states remain separate.

Typografie: [R-0252 — přibalené řezy Medium a SemiBold](requirements/done/R-0252.md).

Tento plán rozděluje dokončení hry na pracovní balíky a hratelné etapy. Slouží
k zadávání práce vývojáři nebo agentovi: vybereme výsledek, doplníme konkrétní
pravidla a ověření a předáme ohraničené zadání. Součástí funkce je její ovládání,
potřebná grafika a srozumitelná odezva.

**Pracovní návrh z 11. října 2026.** Cílem zůstává rozsah pravidel z
[manuálu Sid Meier’s Colonization](Sid_Meiers_Colonization_manual.pdf), včetně
tahů, ekonomiky, soupeřů a války za nezávislost. První hratelné etapy jsou
průběžné výsledky; neznamenají omezení konečného rozsahu. Pořadí níže je návrh
pro další práci, nikoli závazný termín nebo spuštěné implementační úkoly.

**Zpřesnění postupu z 13. října 2026:** nejdřív vybíráme jednu z
[deseti pracovních oblastí](requirements/areas.md), vyjasníme její potřebnou
společnou strukturu a potom upřesňujeme jednotlivá R. Uživatel následně potvrdil
zachování současného menu A01; odstranění podmínky kampaně a mock nastavení
jsou již dodané části R-0001/R-0014.
Oblasti určují soustředění práce; balíky P zůstávají rozpadem rozsahu a etapy E
přejímacími výsledky. Číselné pořadí R ani P není fronta k provedení.

Závaznost určuje [rozdělení use cases](use-cases.md#zavazna-hra-a-samostatne-nastaveni):
**Hra** je pravidlový základ podle manuálu, **Nastavení** jsou samostatně
přejímané volby nové partie a **Regrese** chrání každou další změnu. Otevření
kolonie, detaily budov a správné herní informace patří do Hry. Podobu
výběrových obrazovek, rozmístění a vzhled lze měnit; účinky národů a obtížnosti
zůstávají součástí závazných pravidel. Balíky mohou obsahovat více kategorií,
proto do dílčího zadání vždy patří konkrétní UC a jeho kategorie.

## Aktuální zadávání práce — 2026-10-14

Všech čtyřicet původních R prošlo [migrací](requirements/migration-2026-10-14.md).
Pro další výběr používej [nový katalog](requirements/backlog.md); staré odkazy
v balících zůstávají dohledatelnou historií a vedou na nové vlastníky zbylého
rozsahu. Samotné zachycení požadavku neznamená zahájení vývoje.

| Balíky | Aktuální rozpad zbývajících výsledků |
| --- | --- |
| P02 | [Nastavení A02](requirements/backlog.md#a02) |
| P03/P26 | [Tah a obnova A04](requirements/backlog.md#a04) |
| P04–P07 | [Svět a rozkazy A05](requirements/backlog.md#a05) |
| P08/P09/P14/P15 | [Doprava a Evropa A06](requirements/backlog.md#a06) |
| P10–P13 | [Hospodářství A07](requirements/backlog.md#a07) |
| P20/P21 | [Diplomacie a soupeř A08](requirements/backlog.md#a08) |
| P17 | [Boj A09](requirements/backlog.md#a09) |
| P22–P24 | [Politika A10](requirements/backlog.md#a10) |
| P31 | [Migrace R-0050](requirements/done/R-0050.md); [R-0112: stabilita TC27/TC30](requirements/done/R-0112.md); [R-0114: adresáře podle stavu požadavků](requirements/done/R-0114.md) |
| P31/P32 | [R-0113: vynechání plného CI při změnách pouze v docs/](requirements/done/R-0113.md) |

Výběr dalšího R proběhne po dohodě. Zbytek úplného manuálu zůstává v tomto
plánu a v UC; migrace prvních čtyřiceti zadání nezavírá celý backlog hry.

## Cíl běžné nové hry

**Návrh formulace z 11. října 2026:** Hráč zvolí **Nová hra** a po případném
nastavení partie vstoupí do běžné volné hry podle pravidel manuálu. Tento vstup
je dostupný už při prvním spuštění, bez dokončení kampaně nebo výukových úkolů.
Funkce dodávané pracovními balíky se postupně propojí v této partii: průzkum,
kolonie, tahy, hospodářství, ostatní národy, politika a válka za nezávislost.
Hráč kvůli nim nemusí spouštět jednotlivé mise či zvláštní demonstrační scénáře.

Kampaň a výuka mohou zůstat jako samostatné, nepovinné možnosti. Dostupná
nápověda pomáhá během běžné hry; její absolvování neodemyká základní režim.
Pracovní výklad o samostatné výuce lze upřesnit při potvrzení cíle.

### Jak poznáme splnění cíle

1. Nový hráč bez záznamu o dokončené kampani může z hlavního menu zahájit
   běžnou partii. Nemusí nejprve vybrat či splnit misi.
2. Po potvrzení případného nastavení se otevře herní mapa volné partie.
   Zrušení nastavení ponechá hráče v menu bez spuštění partie.
3. Funkce přijaté v jednotlivých etapách jsou dostupné v této partii při
   splnění svých herních podmínek; demonstrační scénář samotný není důkazem
   jejich dokončené integrace. Hráč nemusí odemykat funkce plněním kampaně.
4. Uložení a pokračování zachová stejnou volnou partii, stav a dostupné funkce.
5. Konečný rozsah umožní souvislou partii až k řádnému výsledku podle pravidel,
   včetně nezávislosti a skóre. Podmínky výhry a prohry vycházejí z herních
   pravidel, nikoli ze seznamu výukových úkolů.

**Současný stav doložený kódem, revize
[`27a89c8e`](https://github.com/jajir/microcol/commit/27a89c8e87d732cc67227fa7dd70b43bdc193aac):**
[ButtonsPanelPresenter.refresh](../microcol-game/src/main/java/org/microcol/gui/screen/menu/ButtonsPanelPresenter.java)
zpřístupňuje volnou hru až po dokončení kampaně. Naproti tomu
[ApplicationController.startNewFreeGame](../microcol-game/src/main/java/org/microcol/gui/dialog/ApplicationController.java)
už přímo spouští režim `freePlay` a jeho
[factory](../microcol-game/src/main/java/org/microcol/model/campaign/FreePlay_0_missionFactory.java)
používá prázdné cíle mise. To neprokazuje úplnost pravidel ani dohratelnost
celé partie; ty zůstávají předmětem balíků níže. Jde o čtení zdrojů,
nikoli o nově provedený průchod běžící hrou.

Tento zápis stanovuje hráčský cíl a jeho přejímací podmínky. Implementačně
navazuje zejména na P02/P28 pro vstup do hry, P26 pro obnovu a P31 pro ověření
celé cesty; úplnost partie vyžaduje i ostatní příslušné balíky. Zapsání cíle
ani samotné odemčení tlačítka neznamená dokončení celé hry.

## Z čeho vycházíme

| Podklad | K čemu slouží |
| --- | --- |
| [Rozbor pravidel](gap-analysis.md) | Autoritativní katalog pravidel (228 původních a 2 doplněná 11. října), jejich ID, stránky manuálu, stav, referenční tabulky a navržené kontroly. |
| [Use cases a přejímka](use-cases.md) | Konkrétní hráčské kroky a výsledky, povinné varianty, oddělený stav implementace a testů a podmínky přijetí pravidel. Do zadání vybírat konkrétní UC. |
| Tento plán | Balíky, závislosti, průběžné výsledky a šablona zadání. Detailní pravidla se sem nekopírují. |
| [Malé analýzy požadavků](requirements/README.md) | Samostatné soubory R-0001.md a další: shrnutí, chování, implementační kroky a otevřené otázky; postupné upřesnění před implementací. |
| [Pracovní oblasti](requirements/areas.md) | Jediné rozdělení všech katalogových pravidel do deseti oblastí, jejich hranice, potřebná příprava a navržené pořadí soustředěné práce. |
| [Odpovědnosti Java balíčků](architecture.md) | Vlastník pravidel, stav hráče, AI, UI a persistence; hranice a současné přesahy pro přípravu každého zadání. |
| [Odhad práce](implementation-estimate.md) | Historický rozpočtový rámec, předpoklady a samostatné náklady na UX, obsah a vydání. |
| [UX](ux.md), [grafika](graphics.md), [zvuk](audio.md) | Společná pravidla ovládání, existující podklady a požadavky na jejich integraci. |
| [Vývojové distribuce](development-builds.md) | Současné sestavení, publikace a způsob předávání hratelného výsledku. |

Rozbor z 2. října uvádí **20 odpovídajících, 30 částečných, 40 odlišných,
134 chybějících a 4 nejasná pravidla**. Jde o počet různě velkých položek,
nikoli procento hotové hry. „Odpovídá“ znamená úzkou shodu doloženou kódem;
neznamená kompletně otestovaný hráčský scénář.

Kontrola k **11. říjnu 2026**, proti revizi
[`182cf7d2`](https://github.com/jajir/microcol/commit/182cf7d2), potvrdila, že
produkční model, AI a mapy se od základu rozboru
[`f0fdee25`](https://github.com/jajir/microcol/commit/f0fdee25824ee5f5dc50b05dfc9619e850c066c9)
nezměnily. V tomto rozsahu testů se změnil pouze `GoodsTradeTest`.
Základy pohybu, nákladu, kolonie, výroby, obchodu a ukládání proto **rozšiřujeme
a opravujeme**; nezadáváme jejich úplné vytvoření znovu.

Naopak sestavení, nativní distribuce a automatické aktualizace už mají novější
[ověření z 11. října](development-builds.md#verification). Starý odhad nelze
použít jako dnešní seznam nehotové infrastruktury. Novější průchod 601 herních
testů není důkazem shody se všemi pravidly manuálu.

## Etapy podle toho co hráč dokáže

Každá etapa končí předveditelným scénářem, uloženou pozicí a ověřenou vývojovou
distribucí. Přijatá funkce musí být zapojená i do [běžné nové hry](#cil-bezne-nove-hry),
ne pouze do zvlášť spouštěné ukázky. Balík se může objevit ve více etapách: nejprve společný základ,
později integrace jeho závislých pravidel. Za celý hotový se označí až po
dokončení všech svých pravidel.

| Etapa | Výsledek pro hráče | Hlavní práce a podmínka převzetí |
| --- | --- | --- |
| E0 Základ zadávání | Je jasné, jaká pravidla a verze se ověřují. | P01, základ P02/P03/P05/P26; rozhodnout jen nejasnosti potřebné pro další balík, připravit malý reprodukovatelný scénář a uchovat jeho stav. |
| E1 První kolonie a další tah | Hráč vybere jednotku, dojde na místo, založí a pojmenuje kolonii, přidělí práci a rozumí výsledku dalšího tahu. | P03/P04/P06/P10/P11, průběžně P26–P30. Přesně sedí pohyb, zásoby a spotřeba; lze zrušit rozpracovanou akci a obnovit hru. |
| E2 Fungující hospodářství a Evropa | Kolonie vyrábí, staví, vyváží, nakupuje a přiváží nové lidi. | P05/P07–P09/P11/P12/P14/P15. Vícekolový oběh zboží a peněz bez ztrát při plné lodi, skladu či zrušeném obchodu. Základ trhu předchází jeho národním a politickým efektům. |
| E3 Rozvoj a automatizace | Hráč vybavuje a vzdělává kolonisty a deleguje opakovanou přepravu. | P13/P16 a dokončení ekonomických závislostí. Výuka a trasa pokračují po načtení; přerušenou trasu lze opravit nebo zrušit. Domorodá výuka se uzavírá v E4. |
| E4 Živý svět a konflikty | Existují ostatní evropské a domorodé národy; lze obchodovat, vyjednávat a bojovat. | P02/P04/P17–P21. Soupeři hospodaří, vztahy mění povolené akce a boj vysvětlitelně používá síly a následky. Teprve nyní lze plně ověřit pořadí všech národních tahů. |
| E5 Politika a samospráva | Daně, bojkot, zvony, podpora nezávislosti a Kongres mění rozhodování hráče. | P22/P23/P27; dokončit národní výhody a dříve závislé efekty obchodu, budov, misí a vzdělávání. Každý z 25 zakladatelů má vlastní ověření. |
| E6 Celá partie | Hráč může vyhlásit nezávislost, vyhrát nebo prohrát a dostane správné skóre. | P24/P25, všechny potřebné předchozí systémy. Ověřit každou podmínku ukončení zvlášť a také celou navazující partii. |
| E7 Kandidát prvního vydání | Hru lze objevit, dohrát, uložit, aktualizovat a pochopit bez vývojáře vedle hráče. | Převzít Hru podle katalogu UC, potom uzavřít P01–P32 podle rozsahu vydání včetně samostatného Nastavení, obsahu a hráčského ověření. Pravidlové účinky všech čtyř národů a pěti obtížností jsou povinné již pro Hru; úplný výběrový průvodce má vlastní přejímku. Zbývající odchylky musí být výslovně rozhodnuté. |

Základní závislost je **tahy a uložený stav → ekonomika a doprava → okolní
svět a boj → politika → revoluce a konec hry**. UX, grafika, nápověda a
ověřování doprovázejí každou etapu v míře potřebné k použití funkcí. Hlavní
ladění vzhledu a UX následuje po [přejímce Hry](use-cases.md#kdy-muzeme-pravidla-povazovat-za-dokoncena).

## Seznam pracovních balíků

P01–P32 jsou identifikátory práce. ID typu `COL-012` odkazují do existujícího
[katalogu pravidel](gap-analysis.md#rule-catalogue). Rozsah `001–009` znamená
všechna uvedená pravidla včetně krajních. Překryvy balíků jsou integrační
závislosti, nikoli další kopie stejného požadavku nebo dvojí rozpočet.

Všechny balíky níže jsou **plánované**; některé navazují na funkční části hry.
Připravenost konkrétního dílčího zadání určuje jeho uzavřený rozsah a potřebná
rozhodnutí. Žádný velký balík se nepředává jako jediný neurčitý úkol „dodělej“.

### Pravidla a základ hry

| Balík a pravidla | Co doplnit nebo opravit | Závislost a důkaz dokončení |
| --- | --- | --- |
| **P01 Pravidlová rozhodnutí a referenční data**; přijatá pravidla a čtyři referenční tabulky | Oddělit hodnoty výslovně uvedené v manuálu od našich doplněných vzorců. Připravit nezávislé příklady pro terén, jednotky, profese a stavby. | Použít [přijatá pravidla](gap-analysis.md#accepted-game-rules), zapsané v [R-0115](requirements/done/R-0115.md). Pro konkrétní zadání převzít relevantní příklady a doplnit návrh; test nesmí pouze číst stejné runtime konstanty jako implementace. |
| **P02 Nová hra**; SETUP-001–009 | Volba jména, čtyř národů, pěti obtížností, počáteční výpravy a data; generovaný svět a vlastní geografické/klimatické parametry [R-0065](requirements/done/R-0065.md), samostatná geografická Amerika [R-0221](requirements/R-0221.md). Zapojit národní výhody do příslušných systémů, francouzský místní poplach do [R-0153](requirements/done/R-0153.md). | P01/P04/P05; národní efekty také P15/P17/P18/P22. Každá volba se uloží a skutečně změní odpovídající chování; stejný seed reprodukuje generovanou mapu. |
| **P03 Tahy a kalendář**; TURN-001–002, SETUP-009 | Výslovné fáze Evropa → kolonie → akce jednotek; pořadí domorodci → Anglie → Francie → Španělsko → Nizozemsko; jasný konec hráčského tahu a kola. | P01/P02/P26, domorodé akce P18/P21. Čas se posune právě jednou za kolo; načtení nezopakuje produkci, imigraci ani jednorázové události. |
| **P04 Mapa a průzkum**; MAP-001–009, tabulka terénu | Doplnit terény, lesy, řeky, ložiska a jejich vyčerpání, výnosy, cenu pohybu, obranu a ztracená města. Zachovat funkční odkrývání; počáteční znalost světa ověřit a podle přijatého pravidla upravit. Nové řezy: [R-0123](requirements/done/R-0123.md) druhy terénu/les, [R-0124](requirements/done/R-0124.md) základní výnos, [R-0125](requirements/done/R-0125.md) společná cena pohybu, [R-0126](requirements/done/R-0126.md) řeky, [R-0127](requirements/done/R-0127.md) zvláštní zdroje, [R-0128](requirements/done/R-0128.md) vyčerpání ložisek a [R-0129](requirements/done/R-0129.md) ztracená města. | P01/P02; návaznost na P06/P11/P17. Testovat terén s lesem i bez, zdroje a pohled jednotlivých hráčů; nápověda neprozradí neobjevené informace. |
| **P05 Jednotky a vybavení**; UNIT-001–004, tabulky jednotek a dovedností | Úplný seznam lodí, kolonistů, specialistů a bojových rolí; oddělit dovednost od výstroje. Správné kapacity, rychlosti, koně, muškety a nástroje. | P01; P07/P11/P13/P17 rozvíjejí chování rolí. Každý dostupný typ lze vytvořit a načíst; změna výstroje zachová identitu kolonisty a účtuje správné množství zboží. |

### Pohyb a doprava

| Balík a pravidla | Co doplnit nebo opravit | Závislost a důkaz dokončení |
| --- | --- | --- |
| **P06 Pohyb a rozkazy**; MOVE-001–009 | Účtovat terén, cesty a řeky; trvalý cíl, čekání, přeskočení, hlídka, opevnění, aktivace a rozpuštění jednotky. Společnou cenu pohybu, plán i dosah vlastní [R-0125](requirements/done/R-0125.md); navazují říční úseky [R-0126](requirements/done/R-0126.md) a silnice [R-0121](requirements/done/R-0121.md). Trvalý cíl a jeho zrušení vlastní [R-0130](requirements/done/R-0130.md), příkaz k rozpuštění [R-0131](requirements/done/R-0131.md). | P03–P05. Jednotka nepřekročí možnosti tahu; trvalý rozkaz přežije další tah a načtení, lze jej zrušit. UI ukáže zbývající pohyb a rozdíl mezi čekáním a hlídkou. |
| **P07 Pionýři**; PION-001–006 | Výdej a spotřeba nástrojů, orba, kácení, cesty, získané dřevo a rychlost specialisty; přerušení rozpracované práce. Nově zachycené řezy: [R-0118](requirements/done/R-0118.md) vybavení, [R-0119](requirements/done/R-0119.md) orba, [R-0120](requirements/done/R-0120.md) kácení, [R-0121](requirements/done/R-0121.md) stavba cesty, [R-0122](requirements/done/R-0122.md) výnosy cesty. | P04–P06/P11; použít přijaté hodnoty pro témata [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education)/[průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport). Úprava pole a spotřeba vzniknou právě jednou, nedostatek nástrojů má srozumitelný důvod. PION-006 zůstává průřezovým ověřením rychlosti; existující zrychlení orby se zachová. |
| **P08 Náklad a přestupy**; CARGO-001–010, CARGO-015–016 | Správná pravidla přepravy lidí, zboží, děl a pokladu; částečné množství, překládka, nalodění/vylodění, zkratky a vyhození nákladu. Nové malé řezy: [R-0132](requirements/done/R-0132.md) limit prostoru, [R-0133](requirements/done/R-0133.md) zákaz vnořených nosičů, [R-0134](requirements/R-0134.md) prostor děla/pokladu, [R-0135](requirements/done/R-0135.md) fronta přistěhovalců, [R-0136](requirements/done/R-0136.md) příjezd do kolonie, [R-0137](requirements/done/R-0137.md) rychlé přesuny, [R-0138](requirements/done/R-0138.md) odhození zboží. | P05/P06/P10. Hraniční kapacity, plný sklad, zrušení a zakázaní cestující neztratí ani nezdvojí zboží nebo jednotky. |
| **P09 Atlantik**; CARGO-011–014 | Nájezd do námořních cest, doba a směr plavby, původní návratové pole a obrat na cestě. | P03/P04/P05/P08; doby podle přijatých pravidel (viz [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport)), později efekt Magellana P23. Náklad i místo návratu přežijí plavbu a save/load, hráč vidí stav cesty. |
| **P16 Obchodní trasy**; ROUTE-001–004 | Pojmenovaná trasa nejvýše čtyř dosažitelných zastávek, pravidla zboží, výchozí zastávka a opakování; úprava, zrušení a smazání. Nové řezy: [R-0139](requirements/done/R-0139.md) podoba trasy, [R-0140](requirements/done/R-0140.md) zboží v zastávce, [R-0141](requirements/done/R-0141.md) přiřazení/opakování a [R-0142](requirements/done/R-0142.md) úprava/zrušení. | P06/P08/P09/P15. Naplněná loď, prázdný sklad nebo ztracená kolonie nezpůsobí zaseknutí tahu; přerušená trasa má opravitelný stav. |

### Kolonie a hospodářství

| Balík a pravidla | Co doplnit nebo opravit | Závislost a důkaz dokončení |
| --- | --- | --- |
| **P10 Kolonie a zásoby**; COL-001–012 | Založení na platném místě, editovatelné jméno a otevření kolonie; společná pole sousedních kolonií, automatická produkce středu [R-0146](requirements/done/R-0146.md), jídlo, růst, hlad a kapacita skladu. Bezpečné rozhodnutí při přeplnění. | P03–P06/P08/P11. Ověřit hranice potravy a skladu, posledního pracovníka a zrušené založení/přesun; zachovat již správně implementované chování. |
| **P11 Výroba a profese**; PROD-001–011 | Sedm výrobních řetězců, pořadí vstupů, skutečná spotřeba specialistů, venkovní výnosy, sluhové/trestanci, práce konvertitů [R-0149](requirements/R-0149.md), kříže a chov koní; nabídka práce se současným/nejlepším výnosem a odpovídajícím přiřazením. Jefferson/Paine/Hudson mění příslušné výstupy v [R-0180](requirements/done/R-0180.md). | P01/P03–P05/P10; konvertité také P19, politické bonusy P22/P23. Model, náhled produkce a výsledné zásoby se shodují včetně nedostatku surovin. |
| **P12 Stavby**; BUILD-001–019, tabulka budov | Populace, předchozí stavba, kladiva a nástroje; správné ceny, upgrady a účinky, změna projektu, dostavění za peníze, lodě/děla, rybolov, celnice a opevnění; La Sallův automatický stockade [R-0184](requirements/done/R-0184.md). | P05/P10/P11, odemykání zakladateli P23. Stavba nic nespotřebuje při zrušení, neobejde podmínky a oznámí chybějící nástroje; každý podporovaný typ má zobrazení. |
| **P13 Vzdělávání**; EDU-001–006 | Učitelé, žáci, trvání a stupně škol; povyšování, zrušení odbornosti [R-0147](requirements/done/R-0147.md), zkušenost prací a domorodá výuka [R-0148](requirements/done/R-0148.md). | P03/P05/P10–P12/P19; parametry P01. Výuka přežije uložení, změnu práce a odchod učitele podle zvolených pravidel; UI ukazuje způsobilost a průběh. |
| **P14 Imigrace a nákupy jednotek**; IMM-001–004, IMM-007 | Kříže místo pevného turnusu, tři obnovovaní kandidáti, zaplacení cesty, evropská univerzita a rostoucí cena lodí/děl. | P03/P05/P08/P11, národní/zakladatelské efekty P02/P23. Kandidát se odstraní a nahradí právě jednou; neúspěšný nákup nemění peníze ani přístav. ID IMM-005/006 v katalogu nejsou. |
| **P15 Evropský trh**; TRADE-001–009 | Nákupní/prodejní ceny, částečná množství, správné čisté výnosy a daň; dynamická reakce národních trhů, ekonomický přehled a obchod celnicí během revoluce. Nové řezy: [R-0143](requirements/done/R-0143.md) tržní tlak, [R-0144](requirements/done/R-0144.md) nizozemská stabilita a [R-0145](requirements/done/R-0145.md) účty/přehled; již hotová kotace R-0020 se znovu neimplementuje. | P08/P09/P11, [národní výhody](gap-analysis.md#difficulty-and-national-effects) a [přijatý výpočet daně při nákupu](gap-analysis.md#economy-production-and-education); bojkot P22, revoluce P24. Náhled ceny souhlasí s účtem; hranice zásob a peněz, odmítnutí a zákaz obchodu jsou bezpečné. |

### Ostatní národy a boj

| Balík a pravidla | Co doplnit nebo opravit | Závislost a důkaz dokončení |
| --- | --- | --- |
| **P17 Boj a následky**; COMBAT-001–016, tabulka sil | Běžný a lodní boj mají dílčí hotové řezy; nové analýzy pokrývají domorodé role/přepad [R-0173](requirements/done/R-0173.md), dobytí sídla [R-0174](requirements/R-0174.md), poklad [R-0175](requirements/R-0175.md), námořní hlídku [R-0176](requirements/R-0176.md), pobřežní palbu [R-0177](requirements/R-0177.md), královské bonusy [R-0178](requirements/R-0178.md) a Drakův korzárský bonus [R-0179](requirements/done/R-0179.md). Zbývající způsobilost, opevnění, zajetí, poškození/opravování lodí a děl, kořist a obsazení kolonie mají vlastní původní vlastníky. | P04–P06/P10/P12, [boj a námořní následky](gap-analysis.md#combat-and-naval-outcomes); vztahy P20, politické bonusy P22/P24. Oddělit test síly od řízené náhody; ověřit porážku civilisty, plné kolonie i lodi s nákladem. |
| **P18 Domorodé společnosti**; NAT-001–009 | Osm národů a sídla [R-0150](requirements/done/R-0150.md), bojovníci/domov/tahy [R-0151](requirements/done/R-0151.md), půda [R-0152](requirements/done/R-0152.md), vztahy [R-0153](requirements/done/R-0153.md), jejich události [R-0154](requirements/R-0154.md) a čitelný poplach [R-0155](requirements/done/R-0155.md); role návštěvníka a dostupné akce zůstávají dalším řezem. | P02–P05/P10, [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations). Obchod, přiblížení vojska, zabrání půdy a čas mění odpovídající stav; informace je srozumitelná i bez barev. |
| **P19 Domorodé interakce**; NAT-010–021 | Výběr akce [R-0157](requirements/R-0157.md), náčelník [R-0158](requirements/R-0158.md), trh [R-0159](requirements/done/R-0159.md), prodej/dar [R-0160](requirements/done/R-0160.md), nákup [R-0161](requirements/done/R-0161.md), výuka [R-0148](requirements/done/R-0148.md), tribut [R-0162](requirements/R-0162.md); vysvěcení [R-0163](requirements/done/R-0163.md), mise [R-0164](requirements/R-0164.md), soupeření [R-0165](requirements/R-0165.md), podněcování [R-0166](requirements/R-0166.md) a konverze [R-0149](requirements/R-0149.md). Dobytí [R-0174](requirements/R-0174.md) a poklad [R-0175](requirements/R-0175.md) mají zvláštní řezy P17. | P05/P08/P13/P17/P18; zakladatelé P23. Přijetí/odmítnutí nabídky účtuje právě jednou a ovlivní vztahy; neplatná role dostane vysvětlení. |
| **P20 Evropská diplomacie**; DIP-002–008 | Kontakt, válka/mír/aliance, odlišení korzárů, tribut, žoldnéři [R-0169](requirements/R-0169.md), obchod se soupeři [R-0167](requirements/R-0167.md), královské války a přístup k informacím [R-0168](requirements/R-0168.md). | P02/P08/P15/P17/P18, příslušní zakladatelé P23. Mírová návštěva nevyvolá automatické dobytí; smlouvy skutečně omezí útoky a přežijí načtení. |
| **P21 Chování soupeřů**; DIP-001 a chování NAT | Evropská expanze a hospodářství [R-0172](requirements/R-0172.md), přeprava, obchod, pozemní útok [R-0170](requirements/R-0170.md), námořní boj [R-0171](requirements/R-0171.md); domorodé reakce na vztahy [R-0156](requirements/R-0156.md). | P03/P10–P20 postupně. Reprodukovatelné vícekolové scénáře bez zablokování tahu a nepovolených příkazů; soupeř využívá herní pravidla, nikoli jen přesouvá jednotky. |

### Politika a dokončení partie

| Balík a pravidla | Co doplnit nebo opravit | Závislost a důkaz dokončení |
| --- | --- | --- |
| **P22 Daně a podpora nezávislosti**; GOV-001–006 | Daňové požadavky, protest, bojkot a doplacení; Fuggerovo jednorázové odpuštění [R-0181](requirements/done/R-0181.md); místní a národní podpora, zvony, loajalisté a výrobní bonusy/postihy. | P03/P10/P11/P15, [obtížnost a národní výhody](gap-analysis.md#difficulty-and-national-effects)/[podpora, produkce a Kongres](gap-analysis.md#support-production-and-congress). Hráč vidí důsledek volby; prahy a zaokrouhlení mají příklady a neaplikují bonus vícekrát. |
| **P23 Kongres a 25 zakladatelů**; GOV-007, FATHER-001–025 | Nábor a členství dodává R-0104; doménové účinky mají jednotlivé vlastníky: Sepulveda [R-0149](requirements/R-0149.md), Minuit [R-0152](requirements/done/R-0152.md), Pocahontas [R-0153](requirements/done/R-0153.md), Brebeuf [R-0163](requirements/done/R-0163.md), de Witt [R-0167](requirements/R-0167.md)/[R-0168](requirements/R-0168.md), Cortés [R-0175](requirements/R-0175.md), Drake [R-0179](requirements/done/R-0179.md), Jefferson/Paine/Hudson [R-0180](requirements/done/R-0180.md), Fugger [R-0181](requirements/done/R-0181.md), Las Casas [R-0182](requirements/R-0182.md), Coronado [R-0183](requirements/done/R-0183.md), La Salle [R-0184](requirements/done/R-0184.md); ostatní dodané účinky zachovávají své původní R. | P22 a systém ovlivněný daným zakladatelem; [účinky zakladatelů](gap-analysis.md#founder-details). Test před/při/po přijetí, načtení, kombinace efektů a jednorázové dary; žádný zakladatel nezůstane pouze portrétem. |
| **P24 Revoluce**; GOV-008–022 | Vyhlášení/mobilizace R-0105; REF soupis [R-0185](requirements/R-0185.md) a skutečné vylodění [R-0186](requirements/R-0186.md), uzavření Evropy [R-0187](requirements/R-0187.md), útok podle podpory [R-0188](requirements/R-0188.md), pomoc ciziny [R-0189](requirements/R-0189.md)/[R-0190](requirements/R-0190.md), žoldnéři [R-0169](requirements/R-0169.md), královské/koloniální bojové bonusy [R-0178](requirements/R-0178.md), vítězství [R-0191](requirements/R-0191.md) a porážky [R-0192](requirements/R-0192.md). | P03/P17/P20–P23, [podpora, produkce a Kongres](gap-analysis.md#support-production-and-congress)/[revoluce a intervence](gap-analysis.md#revolution-and-intervention)/[boj a námořní následky](gap-analysis.md#combat-and-naval-outcomes). Ověřit i všechny pobřežní kolonie, 90 % okupované populace a vojáky dosud v Evropě; pokračování po save/load. |
| **P25 Konec hry a skóre**; END-001–004, SCORE-001–009 | Hranice let [R-0193](requirements/R-0193.md), Exit/Retire [R-0194](requirements/R-0194.md), základ skóre [R-0195](requirements/R-0195.md), válečné/obtížnostní složky [R-0196](requirements/R-0196.md) a síň slávy [R-0197](requirements/R-0197.md). | P02/P03/P18/P22–P24, [konec hry a skóre](gap-analysis.md#end-dates-and-scoring). Každá větev má samostatný scénář, skóre lze rozepsat a opakované otevření výsledku je nezapíše podruhé. |

### Průřezová práce pro každou etapu

| Balík a pravidla | Co doplnit nebo opravit | Důkaz dokončení |
| --- | --- | --- |
| **P26 Ukládání a obnova**; SUPPORT-001 | Ukládat nové stavy už v příslušné funkci: fáze, rozkazy, výuka, trasy, vztahy, trh, nábor, politika, náhoda a konec hry. Použít aktuální schéma bez migrací starších her a přijatá pravidla automatického ukládání. | Načtení nezmění výsledek ani nespustí událost znovu; poškozená/nepodporovaná hra má srozumitelnou chybu a nepoškodí jiný save. Souborové ukládání a pojmenované body obnovy stanovují [pravidla obnovy](gap-analysis.md#recovery-and-presentation). |
| **P27 Informace a nápověda**; SUPPORT-002–004, DIP-007 | Přehled tahu s přechodem na problém, poradci, ekonomika, práce, flotila, vztahy, Kongres a REF; doplnit encyklopedii a aktualizovat existující výuku. Ekonomický účet/přehled sdílí [R-0145](requirements/done/R-0145.md), zahraniční srovnání [R-0168](requirements/R-0168.md). Zbývající přehledy: práce [R-0198](requirements/R-0198.md), náboženství [R-0199](requirements/R-0199.md), kolonie [R-0200](requirements/R-0200.md), domorodci [R-0201](requirements/R-0201.md), flotila [R-0202](requirements/R-0202.md), Kongres/REF [R-0203](requirements/R-0203.md), oznámení/konec [R-0204](requirements/R-0204.md). Podrobnosti objektů [R-0205](requirements/R-0205.md), encyklopedie [R-0206](requirements/done/R-0206.md), výuka [R-0207](requirements/deferred/R-0207.md). | Údaje odpovídají modelu a oprávněné znalosti hráče. Hlad, chybějící vstup nebo dokončení vedou ke konkrétnímu objektu a možné nápravě. |
| **P28 Ovládání a přístupnost** | Použít společný [UX brief](ux.md#kratke-zadani-ux-pro-novou-funkci): objevitelnost, podmínky, náhled, provedení/zrušení, návazný výběr, klávesnice a čitelnost. | Ověřená skutečná obrazovka, relevantní velikosti okna a texty, význam dostupný bez zvuku i barvy. Hráčská pozorování evidovat odděleně od automatických testů. |
| **P29 Grafika** | Mapa pokrytí pro jednotky, role, výstroj, stavby, terény, rozkazy a politické obrazovky; doplnit chybějící prvky a reprodukovatelný export. Použít [minimalistický grafický manuál](graphics.md#minimalisticky-graficky-manual), jehož zápis vede [R-0242](requirements/done/R-0242.md). Vybraný vzhled běžných tlačítek zavádí [R-0246](requirements/done/R-0246.md), s výjimkami pro dosavadní vlastní styly. | Zdroj, runtime výstup, alias a spotřebitel jsou dohledatelné; kontrola v reálné velikosti mapy a ikon, variantách a živé obrazovce. Jednotlivé assety vznikají s funkcí. |
| **P30 Zvuková odezva** | Podle [zvukových pravidel](audio.md) připravit krátké efekty, souběh s hudbou, nastavení a vybrané herní události. Rozsah další hudby určit samostatně. | Katalog určí událost, výsledek, příjemce a okamžik; zvuk se nespustí při pouhém náhledu nebo načtení reportu. Vše podstatné zůstane pochopitelné při ztlumení. |
| **P31 Celková kvalita a vyvážení** | [R-0011: automatická hranice modelu](requirements/done/R-0011.md). [R-0032: oprava JavaDoc a validačních hlášení](requirements/done/R-0032.md). [R-0033: UI testy v Dockeru](requirements/done/R-0033.md). [R-0034: lokální worker GPT-OSS 20B](requirements/done/R-0034.md). [R-0035: výstupní rozpočet a sdílené spouštění workeru](requirements/done/R-0035.md). [R-0036: GPT-OSS high a porovnání s Qwen2.5-Coder](requirements/done/R-0036.md). [R-0038: GPT-OSS high s výstupním limitem 64k](requirements/done/R-0038.md). [R-0039: úspora cloudových tokenů a medium → high/64k](requirements/done/R-0039.md). [R-0243: probuzení Codexu po dokončení workeru, Dockeru a CI](requirements/done/R-0243.md). [R-0244: Headroom pro Codex a Claude Code](requirements/done/R-0244.md). [R-0245: výchozí Codex a volba alternativ v zadání](requirements/done/R-0245.md). [R-0040: struktura a stavy požadavků](requirements/done/R-0040.md). Průběžné pravidlové a integrační kontroly, vybrané UI cesty, dlouhá partie, národy/obtížnosti, výkon a skutečné hráčské zkoušky. | Dohledatelné výsledky na dané revizi, regresní scénáře a otevřené vady. Průchod UI skriptu se nevydává za důkaz, že ovládání nový hráč pochopí. |
| **P32 Předání a vydání** | Používat existující CI, launcher a Mac/Linux balíčky; průběžně ověřovat aktualizaci a dokumentaci. Zbývající první vydání a Steam vést podle samostatného odhadu. | Úspěšné kontroly i publikace stejného commitu, odkaz na stažení a postup ověření; připravený plán zacházení s uloženými hrami. Nevytvářet již hotový distribuční systém znovu. |

P31 má ověřený lokální spouštěč [R-0257](requirements/done/R-0257.md): modelová sada jednou, dvě izolované UI skupiny a společná kontrola úplnosti výsledků. [Příkaz a předání procesu](functional-tests.md#uplne-overeni-v-izolovanych-skupinach) jsou součástí funkčního kontraktu.

P31 dělí povinnou ověřovací matici na profily [R-0208](requirements/R-0208.md), oddělené nastavení nové hry [R-0209](requirements/R-0209.md), terény [R-0210](requirements/R-0210.md), jednotky a lodě [R-0211](requirements/R-0211.md), profese [R-0212](requirements/R-0212.md), stavby [R-0213](requirements/R-0213.md), zakladatele [R-0214](requirements/R-0214.md), výrobu/obchod [R-0215](requirements/R-0215.md) a všechny konce [R-0216](requirements/R-0216.md). Souvislé průchody mají samostatné vlastníky: výprava [R-0217](requirements/done/R-0217.md), dvě kolonie [R-0218](requirements/R-0218.md), diplomacie [R-0219](requirements/R-0219.md) a nezávislost [R-0220](requirements/R-0220.md). Jde o ověřovací rozsah; jednotlivé herní změny zůstávají u příslušných funkčních R.

Zbývající společné cíle jsou rozdělené: P28 sdílená přístupnost/ovládání [R-0222](requirements/R-0222.md) a pozorování hráčů [R-0223](requirements/R-0223.md); P29 skutečné pokrytí obrázků [R-0224](requirements/R-0224.md) a reprodukovatelný export [R-0225](requirements/R-0225.md); P30 služba efektů [R-0226](requirements/R-0226.md), nastavení [R-0227](requirements/R-0227.md) a první vybraný podnět [R-0228](requirements/R-0228.md). P31 dlouhá stabilita/výkon [R-0229](requirements/R-0229.md) a vyvážení celé partie [R-0230](requirements/R-0230.md) potřebují skutečné důkazy, nikoli jen hotové matice.

P32 samostatně zachycuje místní diagnostiku [R-0231](requirements/done/R-0231.md), hráčské pokyny [R-0232](requirements/R-0232.md), produkční runtime [R-0233](requirements/R-0233.md), podepsaný Mac [R-0234](requirements/R-0234.md), Linux [R-0235](requirements/R-0235.md) a stabilní povýšení [R-0236](requirements/R-0236.md). Steam má oddělený produkt/práva [R-0237](requirements/R-0237.md), média [R-0238](requirements/R-0238.md), depoty/start [R-0239](requirements/R-0239.md), instalaci/aktualizaci [R-0240](requirements/R-0240.md) a schválení/spuštění/podporu [R-0241](requirements/R-0241.md). Stávající vývojové balíčky a launcher se nereimplementují; volitelné Steam API funkce, další hudba a nepodporované platformy nejsou součástí těchto R.

Přiřazení pokrývá všech 230 katalogových ID včetně doplnění z 11. října. Celé rodiny pravidel zůstávají
v rozsahu, včetně všech 25 zakladatelů a referenčních tabulek; priorita nezařazuje
jejich zbývající části automaticky „mimo první vydání“.

### Nové analýzy pro free play

Doplněno 13. října 2026: **dvacet návrhů R-0012–R-0031**, původně ve stavu
Draft; R-0012–R-0025 následně přešly do Refining podle rozhodnutí uživatele
a ověřování existujících UI cest. [Katalog analýz](requirements/README.md#twenty-free-play-follow-up-drafts)
a [mapa oblastí](requirements/areas.md#navazujici-navrhy-pro-free-play) určují
jejich zařazení; konkrétní rozsah a otázky vlastní každý dokument. Příprava
neznamená implementaci ani uzavření příslušného balíku nebo UC.

| Hlavní balík | Nové analýzy |
| --- | --- |
| P28 | [R-0012 menu](requirements/done/R-0012.md), [R-0013 bezpečný odchod](requirements/done/R-0013.md), [R-0017 odezva příkazů](requirements/done/R-0017.md) |
| P02 | [R-0014 profil](requirements/done/R-0014.md), [R-0015 účastníci](requirements/done/R-0015.md) |
| P27 | [R-0016 obrazovky a informace](requirements/done/R-0016.md) |
| P26 | [R-0018 obnovení hry](requirements/done/R-0018.md) |
| P04 | [R-0019 viditelnost](requirements/done/R-0019.md) |
| P05 | [R-0020 evropské vybavení](requirements/done/R-0020.md) |
| P09 | [R-0021 Atlantik](requirements/done/R-0021.md) |
| P08 | [R-0022 cestující](requirements/done/R-0022.md) |
| P14 | [R-0023 odborný kovář](requirements/done/R-0023.md) |
| P10 | [R-0024 vyložení vozu](requirements/done/R-0024.md) |
| P12 | [R-0025 podmínky staveb](requirements/done/R-0025.md) |
| P20 | [R-0026 vztahy](requirements/done/R-0026.md) |
| P21 | [R-0027 kolonie soupeře](requirements/done/R-0027.md) |
| P17 | [R-0028 útok](requirements/done/R-0028.md), [R-0029 síla](requirements/canceled/R-0029.md) |
| P22 | [R-0030 daně](requirements/canceled/R-0030.md), [R-0031 podpora nezávislosti](requirements/canceled/R-0031.md) |

## Rozdělení práce mezi lokálního workera a hlavního agenta

**Dohoda z 11. října 2026, model změněn 13. října 2026:** maximum vhodné implementační
práce předáváme lokálnímu workeru. Aktuální model GPT-OSS 20B a jeho ověřené
nastavení vlastní [provozní stránka](ai/local-qwen.md). Všech 32 balíků obsahuje
delegovatelnou část. Velký nebo složitý balík rozdělíme na menší zadání; jeho složitost sama o sobě není důvodem,
aby vše napsal hlavní agent. Tato mapa vyjadřuje plánované rozdělení práce,
nikoli tvrzení, že worker už jednotlivé oblasti úspěšně zvládl.

Hlavní agent připravuje pravidla, architekturu, rozhraní a akceptaci; s vlastníkem
hry řeší potřebná herní a UX rozhodnutí. Worker dostává implementaci, psaní testů,
opravy a navazující dokumentaci v dohodnutém rozsahu. Skutečné spuštění kontrol,
vizuální a hráčské ověření, revizi změn, integraci a publikaci drží hlavní agent.
Pro přijetí dílčí práce není nutné znovu schvalovat již dohodnutý záměr.

V posledním sloupci je příprava **hlavního agenta**, případně jím předložené
rozhodnutí vlastníka hry. Závislosti na funkcích zůstávají v katalogu balíků
výše. Předává se jen potřebné rozhraní a dokončená část závislosti, nikoli
automaticky celý předchozí balík. Výtvarnou tvorbu nesvěřujeme textovému workeru;
jeho prostor je zejména technická příprava a zapojení podkladů.

| Balík | Co zadávat workeru po malých částech | Co připravit před předáním |
| --- | --- | --- |
| P01 Pravidla | [R-0117](requirements/done/R-0117.md) opravuje zastaralé současné popisy podle dodaného kódu. Schválené datové tabulky, validátory a nezávislé testovací příklady. | Rozhodnutí příslušné Q, zdroj hodnot a očekávané výsledky. |
| P02 Nová hra | Jednotlivé volby, uložené nastavení, inicializace a testy profilů. | Rozhraní nastavení, počáteční hodnoty a návrh obrazovky. |
| P03 Tahy | Jedna fáze či přechod, ukládaný stav a testy pořadí. | Kalendář, pořadí událostí a rozhraní průchodu tahem. |
| P04 Mapa | Terénní data, výnos/cena pole, generátorový krok a testy. | Model mapy, přijatá pravidla a reprodukovatelné příklady. |
| P05 Jednotky | Jeden typ nebo změna výstroje, továrna, persistence a testy. | Rozlišení identity, odbornosti a výstroje, povolené přechody. |
| P06 Rozkazy | Jednotlivý příkaz, cena kroku, přerušení a regresní testy. | Smlouva příkazu, hraniční pohyb a způsob ovládání. |
| P07 Pionýři | Jeden druh práce, spotřeba nástrojů a testy dokončení. | Délka a přerušení práce, přesný okamžik účtování. |
| P08 Náklad | Jedna transakce, kontrola kapacity, dílčí dialog a testy. | Pravidla přenosu, zachování stavu při chybě a návrh dialogu. |
| P09 Atlantik | Přechod plavby, návratové místo a testy načtení. | Doby cesty, směry a rozhraní stavu plavby. |
| P10 Kolonie | Založení, sklad či přiřazení pracovníka jako oddělená zadání. | Přesný řez pravidel, rozhraní a požadovaný průchod UI. |
| P11 Výroba | Jeden řetězec nebo bonus, spotřeba a hraniční testy. | Pořadí výpočtu, vzorce a nezávislé účetní příklady. |
| P12 Stavby | Jedna podmínka, upgrade, transakce či zpráva a testy. | Schválená tabulka nákladů, účinky a návrh ovládání. |
| P13 Vzdělávání | Přidělení žáka, postup výuky nebo přerušení a persistence. | Stavový model, trvání a chování při odchodu učitele. |
| P14 Imigrace | Výběr kandidáta, doplnění nabídky, nákup a testy. | Práh imigrace, rozhraní nabídky a způsob potvrzení. |
| P15 Trh | Cena, daň, transakce nebo dílčí zpráva a testy. | Vzorec trhu, zaokrouhlení, pravidla daně a náhled ceny. |
| P16 Trasy | Zastávka, nakládací pravidlo, zrušení či editorový prvek. | Stav trasy, reakce na nedosažitelný cíl a návrh editoru. |
| P17 Boj | Dílčí výpočet síly, jeden následek boje a řízené testy. | Pořadí bonusů, pravidla náhody a rozhraní výsledku. |
| P18 Domorodé společnosti | Stav vztahu, jedna změna poplachu, ukládání a testy. | Význam stavů, parametry, oprávněně viditelné informace. |
| P19 Interakce | Jedna nabídka, mise, výuka nebo transakce a testy. | Pravidla návštěvy, účtování a přijatý průchod rozhodnutím. |
| P20 Diplomacie | Přechod válka/mír, nabídka nebo kontrola povolené akce. | Diplomatický model, následky voleb a rozhraní kontaktu. |
| P21 Soupeři | Jedna strategie či krok AI, scénáře a opravy konkrétních vad. | Architektura rozhodování, povolené příkazy a měřitelný cíl. |
| P22 Politika | Jeden výpočet podpory, daňové rozhodnutí nebo bojkot a testy. | Vzorce, prahy, zaokrouhlení a pořadí změn stavu. |
| P23 Kongres | Náborový krok a potom jeden efekt zakladatele s testy. | Rozhraní efektů, souběh bonusů a podmínky jednorázovosti. |
| P24 Revoluce | Jeden přechod, podmínka výsledku nebo zásah spojence. | Stavový model revoluce, přesné podmínky a příklady. |
| P25 Skóre | Jedna skórovací položka, větev konce nebo záznam výsledku. | Přijaté vzorce a rozlišení ukončení, skórování a pokračování. |
| P26 Ukládání | Jedno nové pole, schválená migrace a testy obnovy/chyb. | Verze formátu, kompatibilita a migrační strategie. |
| P27 Informace | Jeden přehled, navigace zprávy, nápovědný text a testy. | Herní pojmy, rozsah dostupných informací a návrh obrazovky. |
| P28 UX | Komponenta, klávesové ovládání, popisek či stav chyby podle briefu. | Cesta hráče, návrh interakce a plán skutečného ověření UI. |
| P29 Grafika | Inventář, alias, exportní skript, integrace assetu a validátor. | Zdrojový obrázek, cílové rozměry, varianty a vizuální očekávání. |
| P30 Zvuk | Zapojení události, nastavení, přehrávání, katalog a testy. | Vybraný zvuk, okamžik přehrání a očekávaná odezva. |
| P31 Kvalita | Testy, fixture, reprodukční scénáře, měřicí nástroje a opravy. | Co se ověřuje, nezávislý očekávaný výsledek a způsob měření. |
| P32 Vydání | Konkrétní oprava CI, balení, launcheru či dokumentace. | Kontrakt výstupů a akceptace; publikaci provede hlavní agent. |

### Připravenost prvních zadání

„Vhodné pro workera“ neznamená „lze okamžitě spustit“. Vlastníkem níže uvedené
přípravy je hlavní agent. Stav zachycuje plán k 11. říjnu 2026; žádné z těchto
tří herních zadání zatím nebylo předáno k implementaci.

| Zadání | Stav a konkrétní podmínka předání | Nejmenší práce pro workera |
| --- | --- | --- |
| Z01 Vyložení do skladu | Kontrakt přijat: pravidla a výjimky v modelu, UI používá náhled a ověří změnu před potvrzením. Samostatný PR do main. | [Implementace a konkrétní ověření Z01](functional-tests.md#z01-bezpecne-vylozeni-nakladu). |
| Z02 Pojmenování kolonie | Kontrakt přijat 12. října 2026: neměnná ID, modální pojmenování/přejmenování a aktuální JSON bez zpětné kompatibility. | [Kontrakt Z02](#z02-zalozeni-pojmenovane-kolonie) a [ověření](functional-tests.md#z02-pojmenovani-a-identita-kolonie). |
| Z03 Průchod tahem | [Kalendář a počáteční výpravy](gap-analysis.md#calendar-and-starting-expeditions) jsou pro vývoj rozhodnuté (1490 a popsaný kalendář); hlavní agent ještě stanoví rozhraní fází a persistence. Plné národní pořadí navíc čeká na příslušné části P02/P18. | Použít přijatý [kalendář a počáteční výpravy](gap-analysis.md#calendar-and-starting-expeditions); po individuálním upřesnění jedna fáze/přechod a její testy. |

Před spuštěním každého řezu hlavní agent doplní konkrétní revizi `main`, vstupy,
povolené soubory, kontrolní příkazy a ověří dostupnost workeru. U UI zadání
nesmí spoléhat na dosud nedoložený běh celé legacy TestFX sady; musí mít
proveditelný plán kontroly dotčené cesty. To je podmínka převzetí UI kódu,
nikoli důvod ponechat veškeré jeho psaní hlavnímu agentovi.

## Přijatá pravidla a další upřesnění

Manuál není úplnou technickou specifikací. Některé přesné vzorce, pravděpodobnosti
a okrajové stavy neuvádí. Dne **15. října 2026** jsme na žádost uživatele přijali
[přijatá pravidla](gap-analysis.md#accepted-game-rules),
včetně zdrojů, hodnot a příkladů. Vývoj na jejich pozdější ladění nečeká; před
implementací se ověřují konkrétní závislosti a doplňuje individuální zadání. Vývojář nesmí takovou volbu potichu vydávat za pravidlo
originálu. Pozdější ladění přijatých hodnot neblokuje vývoj.

| Kdy | Společné rozhodnutí | Co lze připravit předem |
| --- | --- | --- |
| Při zadání času nebo nové hry | Převzít pravidla témat [obtížnost a národní výhody](gap-analysis.md#difficulty-and-national-effects)/[kalendář a počáteční výpravy](gap-analysis.md#calendar-and-starting-expeditions): rok 1490, kalendář, výpravy a obtížnost. | Připravit malé modelové příklady a zkontrolovat integrační závislosti; nečekat na další schválení čísel. |
| Při změně výpočtu | Použít přijaté prozatímní pravidla pro produkci, boj, trh, vztahy a politiku. | Nezávislé očekávané výsledky a hranice; nové chybějící případy zapsat konkrétně, neobnovovat obecnou blokaci celého pravidlového tématu. |
| U první hratelné etapy a nového typu obrazovky | Zda hráč cestu najde, rozumí důsledkům a zda je návrh příjemný při hraní. | Konkrétní ovladatelný návrh se současnými prvky; rutinní ikony a kontrast nevyžadují nové schvalování stylu. |
| Před větší výrobou obsahu | Rozsah nápovědy a výuky, cílové jazyky, nové ilustrace a hudba, způsob hráčských zkoušek. | Inventář reuse/doplnit, ukázka jedné celé cesty a odhad přírůstku obsahu. |
| Před plánováním termínu vydání | Kapacita týmu, cílový rozsah vydání a platformy nad již fungující Mac/Linux kanál. | Aktualizovat zbývající práci po etapách a oddělit práci, čekání a rezervu. |

Pro společnou kontrolu je výchozí návrh **první kolonie → výroba → konec tahu**.
Pak přidáme **vývoz a návrat z Evropy**, **stavbu a vzdělání**, **kontakt a boj**
a nakonec **politiku a nezávislost**. U každé cesty ukážeme funkční stav,
následující kroky i chybový a zrušený průchod.

## První konkrétní zadání

**Zásobník návrhů z 12. října 2026, zařazený do oblastí 13. října:** prvních deset analýz najdete
v [seznamu R-0001 až R-0010](requirements/README.md#first-ten-upcoming-requirements).
Jde o připravené koncepty pro jednotlivé upřesnění, nikoli schválení nebo
spuštění deseti implementací. Výběr začíná [přípravou oblasti](requirements/areas.md#postup-po-oblastech),
u menu už je zachování současné navigace dohodnuté v R-0012. Dokončené Z01/Z02 zůstávají níže jako dosavadní
kontrakty; nejsou znovu zařazené mezi budoucí změny.

| Balík / původní zadání | Analýzy pro postupné upřesnění |
| --- | --- |
| P02/P28 | [R-0001 — vstup do volné hry bez kampaně](requirements/done/R-0001.md). |
| P01/P03/P26, Z03 | [R-0002 — fáze tahu a posun kalendáře](requirements/canceled/R-0002.md). |
| P06 | [R-0003 — čekání a vynechání tahu jednotky](requirements/canceled/R-0003.md); [R-0004 — pobřežní přístavy](requirements/canceled/R-0004.md). |
| P10/P11 | [R-0005 — sdílená pole a bezpečné přiřazení](requirements/canceled/R-0005.md); [R-0006 — výběr práce podle výnosu](requirements/canceled/R-0006.md). |
| P10/P03 | [R-0007 — spotřeba jídla a růst](requirements/canceled/R-0007.md); [R-0008 — hlad a poslední obyvatel](requirements/canceled/R-0008.md). |
| P11/P05 | [R-0009 — vstupy odborného kováře](requirements/canceled/R-0009.md). |
| P11 | [R-0010 — automatická základní produkce kříže](requirements/canceled/R-0010.md). |

Následující menší řezy lze připravit před velkými chybějícími systémy. Časový
rozpočet se doplní při zadání; nejde o tři stejně velké úkoly.

### Z01 Bezpečné vyložení do téměř plného skladu

**Balíky:** P08/P10/P28. **Pravidla:** COL-010, CARGO-001 a CARGO-009.

Hráč má loď se 100 kusy ne-potravinového zboží a sklad s 99 kusy při kapacitě
100. Při pokusu vyložit vše dostane srozumitelnou nabídku přenést jeden kus,
nebo operaci zrušit. Bez výslovně zvolené další akce se nic nezahodí. Toto je
navržené UX řešení varování z manuálu, nikoli tvrzení o původním dialogu.

- Přijetí přenese 1 kus: sklad 100, loď 99. Zrušení ponechá 99/100.
- Při plném skladu nedojde k přesunu; při dostatku místa se přenese požadavek.
- Zvolené částečné množství, prázdný zdroj a změna dostupné kapacity nesmějí
  vytvořit záporné zásoby, duplicitu ani nezachycenou chybu; před potvrzením
  se platnost znovu ověří.
- Ověřit cílené modelové hranice, skutečný dialog, opakování a save/load výsledku.
- Potraviny, zahazování přebytků výroby a přepracování všech dokovacích panelů
  jsou mimo tento řez.

Vstupní kód: [PanelColonyGoods](../microcol-game/src/main/java/org/microcol/gui/screen/colony/PanelColonyGoods.java),
[ColonyWarehouse](../microcol-game/src/main/java/org/microcol/model/ColonyWarehouse.java),
[ColonyWarehouseTest](../microcol-game/src/test/java/org/microcol/model/ColonyWarehouseTest.java)
a [CargoSlotTest](../microcol-game/src/test/java/org/microcol/model/CargoSlotTest.java).

**Stav 11. října 2026:** realizace v samostatném PR do `main`; [evidence modelu,
UI scénářů a přejímky](functional-tests.md#z01-bezpecne-vylozeni-nakladu).
UC-023 ani UC-036 se tímto dílčím řezem neuzavírá.

### Z02 Založení pojmenované kolonie

**Přijatý kontrakt — 12. října 2026:** zahrnuje pojmenování při založení i
pozdější přejmenování. Kliknutí na jméno kolonie otevře modální dialog;
stejná cesta je dostupná klávesnicí. Pole obsahuje vybrané současné/navržené
jméno, tlačítka Navrhnout jiné jméno, Zrušit a Uložit (při založení Založit
kolonii). Enter potvrzuje, Escape ruší. Do potvrzení se model nemění.

- Jméno se ořízne o krajní Unicode mezery a normalizuje do NFC. Musí mít
  1–64 Unicode code points; řídicí znaky a neplatné surrogate sekvence
  se odmítají. Vnitřní mezery, diakritika a interpunkce se zachovají.
- Duplicitní jméno v celé hře bez ohledu na velikost písmen je chyba.
  Pole zčervená, text vysvětlí chybu a potvrzení není dostupné. Vlastní
  nezměněné jméno není duplicita. Kontrolu provádí také model při potvrzení.
- Generátor projde existující seznam, neopakuje návrhy v témže dialogu ani
  obsazená jména. Po vyčerpání použije New Amsterdam 2, 3 atd. Návrhy nejsou
  rezervace a zrušení je nezapisuje do modelu.
- Přejmenovat může pouze vlastník ve svém tahu. Změna vlastníka, zánik
  kolonie nebo změna zakládající jednotky během dialogu nesmí způsobit
  částečnou změnu. Při založení se otevře právě vytvořená kolonie.
- Kolonie mají neměnný nezáporný `int id`, společnou posloupnost od nuly
  pro celou hru, oddělenou od jednotek. Přejmenování ani obsazení ID nemění.
  Zaniklá ID se nerecyklují; JSON ukládá `id` a `nextColonyId`, i když
  nezůstala žádná kolonie. Zrušený dialog ID nespotřebuje. Přetečení se odmítá.
- Obnova pracovníků používá ID kolonie. Zprávy o kolonii ukládají ID a
  tehdejší jméno jako textový snímek; přejmenování nemění jejich vazbu ani
  původní text, zpráva zůstane čitelná i po zániku kolonie.

**Obecná pravidla hry přijatá uživatelem:** textové vstupy jsou Unicode a
před validací/uložením se oříznou krajní Unicode mezery. V této vývojové fázi
se **nepožaduje zpětná kompatibilita uložených her**. Scénáře a testové
fixtures se upravují přímo na aktuální JSON; nevytvářet migrační vrstvu ani
opakovaně otevírat otázku kompatibility. Chybějící nebo chybná povinná data
se odmítnou, nikoli tiše doplní. Načtení samo nepřepisuje původní soubor.
Pravidlo textových vstupů platí i pro další úkoly; Z02 není plošný audit všech
starších formulářů. Národní seznamy jmen a atomické zapisování souborů jsou
mimo tento řez. Výsledky ověření patří do [funkčního kontraktu](functional-tests.md#z02-pojmenovani-a-identita-kolonie).

**Balíky:** P10/P28. **Pravidlo:** COL-012; zachovat platné podmínky COL-011.

Hráč na platném místě vyvolá založení, upraví nabídnuté jméno a po potvrzení
se otevře vytvořená kolonie. Zakládající kolonista je stejná jednotka se stejným vlastníkem
a přiřazením k práci. Návrh přijímá neprázdné jméno po odstranění krajních mezer;
další pravidla názvů nesmějí vzniknout potichu jako součást tohoto úkolu.

Automatické otevření je zvolený UX návrh tohoto příkladu. Lze jej později
změnit při zachování možnosti kolonii otevřít a spravovat podle UC-031;
není samostatnou podmínkou přijetí herních pravidel.

- Před potvrzením nevznikne kolonie. Zrušení nezmění jednotku, její pohyb ani mapu.
- Potvrzení vytvoří právě jednu kolonii; opakovaný vstup nezdvojí založení.
- Změněné jméno a přiřazení přežijí save/load. UI otevře právě novou kolonii.
- Neplatné místo zůstane zakázané se srozumitelným důvodem. Oprava všech
  terénních pravidel a zavedení konvertitů je samostatný řez COL-011.
- Ověřit běžný, zrušený a neplatný průchod a návrat na mapu.

Vstupní kód: [Model](../microcol-game/src/main/java/org/microcol/model/Model.java),
[ModelListenerImpl](../microcol-game/src/main/java/org/microcol/gui/event/model/ModelListenerImpl.java)
a [SelectedUnitManager](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/SelectedUnitManager.java).

### Z03 Specifikace a první řez průchodu tahem

Původní [R-0002 — fáze tahu a posun kalendáře](requirements/canceled/R-0002.md)
zůstává historickým záznamem Z03. Aktivní rozsah vlastní [R-0051](requirements/done/R-0051.md)
a [R-0052](requirements/done/R-0052.md). Používají [přijatý kalendář](gap-analysis.md#calendar-and-starting-expeditions);
upřesnit zbývá rozhraní fází a persistence. Přijetí pravidel neznamená implementaci
ani přijetí TURN-001/002.

## Šablona předání práce

Nové změny nejprve popisujeme v samostatné [analýze R-NNNN](requirements/README.md).
Nejdřív vyjasníme potřebný základ její [hlavní oblasti](requirements/areas.md).
Upřesňujeme vždy jeden vybraný požadavek v této oblasti; požadavek na upřesnění sám
nespouští implementaci. Existující Z01–Z03 a jejich odkazy zůstávají platné.
Analýza vlastní dohodnuté chování a akceptaci, následující šablona slouží
k předání konkrétní implementační práce a odkazuje na tuto analýzu.
Předání vychází ze stavu `ready for development` podle
[životního cyklu požadavku](requirements/README.md#status-and-transitions).
Technické rozdělení práce nemění přijaté chování; jeho změna vyžaduje návrat
do refinementu. Podrobná zadání a průběžné výsledky nekopírujeme do analýzy.

Balík vybírá hlavní agent spolu s vlastníkem hry. Rozpad na úkol má mít jeden
pozorovatelný výsledek. Výchozím zpracovatelem je **Codex**. Uživatel může v zadání
zvolit **Claude Code** nebo **lokální model**; volba platí pro daný požadavek a
jeho pokračování, nikoli automaticky pro další nové požadavky. Platí společná
[pravidla volby](ai/local-qwen.md#execution-choice-2026-10-18).
Připravené zadání může vypadat takto:

```text
Úkol: [R-NNNN / související Z a P – konkrétní výsledek]
Analýza: [odkaz na docs/requirements/R-NNNN.md s dohodnutým chováním a akceptací]
Status požadavku: ready for development
Oblast: [jedna hlavní A01–A10; odkaz na společný základ a konkrétní závislosti na dalších oblastech]
Výchozí revize: [commit main]
Zpracovatel: [Codex; nebo uživatelem zvolený Claude Code / lokální model]
Dílčí úkol: [jeden konkrétní výstup a hranice změny]
Příprava hlavního agenta: [potřebné rozhodnutí, rozhraní, data a návrh UX]
Připravenost: [připraveno / k dopracování kontraktu / blokováno]
Blokace: [konkrétní závislost, vlastník a podmínka odblokování; nebo žádná]
Cíl hráče: [co po dokončení dokáže]
Pravidla a zdroj: [ID z gap-analysis.md + stránky manuálu]
Přijatá rozhodnutí: [hodnoty, vzorce, příklad; odkazy na uzavřené Q]
Současný stav: [co už funguje, kde je chyba nebo mezera]
Rozsah změny: [model, UI, data, save/load, dotčené soubory]
Use cases: [konkrétní UC z use-cases.md, všechny jeho varianty a případné blokující Q]
Kategorie a profil: [Hra / Nastavení / Regrese; referenční herní parametry a co je volný návrh prezentace]
Vlastník a hranice: [balíček podle architecture.md, sdílená operace člověka/AI, relevantní současný přesah]
Mimo rozsah: [co se v tomto řezu nedělá]
Závislosti: [konkrétní hotové předpoklady]
Ovládání: [nalezení, podmínky, náhled, potvrzení, zrušení, výsledek]
Grafika a zvuk: [existující prvky / konkrétní nové podklady a spotřebitelé]
Akceptace: [počáteční stav → akce → přesný výsledek; také chyba/zrušení]
Ověření: [konkrétní ID TC scénářů, změněná očekávání, modelové testy, save/load a kontrola obrazu]
Ověření hlavním agentem: [přesné příkazy, prostředí, UI postup a očekávané výsledky]
Delegace: [jen v rámci zvolené varianty; rozsah předání a práce hlavního agenta, nebo žádná]
Časový rámec: [rozpočet práce a kontrolní bod; co předat při nedokončení]
Předání: [diff, výsledek kontrol, dokumentace, zbývající vady, build ke stažení]
```

### Připravenost a předání workeru

Před předáním musí být určené chování, rozhraní, vstupy, povolené soubory,
akceptace, plán ověření a výchozí revize. Velký balík se předává po ohraničených
úlohách, ne jako celá etapa. Nevyřešená podmínka blokuje jen závislý řez;
nezávislou připravenou práci lze zadat dál. Již dohodnutý rozsah nevyžaduje
další souhlas pro každé dílčí zadání.

Pokud uživatel zvolí variantu s workerem, worker píše vhodný kód a testy,
opravuje chyby, provádí opakované refaktoringy a mění dokumentaci či konfiguraci.
Patří sem také UI podle konkrétního návrhu a integrace grafických a zvukových podkladů. Hlavní agent
drží architekturu, nejasná pravidla, návrh UX a výtvarné řešení, skutečné
kontroly, revizi, integraci a publikaci. Stejnou implementaci souběžně nepíše.

Každý místní běh se řídí [postupem workeru](../.agents/skills/local-worker/SKILL.md)
a aktuálními [provozními limity](ai/local-qwen.md#operating-profile-bounded-assignments).
Nedostupnost nebo neúspěch se oznámí; nespouští automatickou instalaci ani
tiché nahrazení cloudovým workerem. Případné dokončení hlavním agentem má
konkrétní důvod a zaznamenané opravy. Návrh testů od workeru nenahrazuje jejich
skutečné spuštění. Podíl delegované práce a úspory lze hodnotit až z měření.

### Zadání na omezený čas

Použitelný příklad požadavku je: „Vezmi Z01 z plánu. Připrav a implementuj
nejmenší úplný řez v rozpočtu osmi pracovních hodin včetně ověření. Pokud se
rozsah nevejde, předem ho rozděl; na konci předej ověřený výsledek a zbývající
práci.“ Osm hodin je zde **příklad zvoleného rozpočtu**, nikoli odhad Z01.

Pracovní hodiny, doba běhu agenta a kalendářní termín jsou různé údaje.
Po prvních několika srovnatelných úkolech změříme zadání, implementaci, opravy,
ověření a integrační práci a podle nich zpřesníme další plán. Počet pravidel
ani rychlost generování kódu neposkytují spolehlivý termín celé hry.

Historický [odhad prvního vydání](implementation-estimate.md) pracuje přibližně
s 5 400 člověkohodinami včetně rezervy, UX a Steam práce. Není to nový příslib
ani aktuální zbývající čas. Při rekalibraci odečteme doloženě hotové části
a znovu posoudíme otevřený obsah; hotová distribuce nesnižuje chybějící herní
pravidla. Žádné budoucí běhy tímto dokumentem nejsou naplánované.

## Kdy je úkol hotový

1. Splňuje vybraná pravidla a konkrétní akceptaci; sporná volba má záznam,
   nevyřešená část zůstává výslovně otevřená.
2. Změněný stav přežije uložení a načtení. Relevantní hranice, chyba a zrušení
   nezpůsobí nežádoucí vedlejší účinek.
3. Hráč funkci najde, pozná podmínky a výsledek. Potřebné obrázky a texty
   jsou zapojené a zkontrolované v běžící hře; zvuk je doplňkový.
4. Projdou relevantní automatické kontroly podle [pravidel funkčních testů](functional-tests.md)
   a cílený hráčský průchod. Každá změna chování má scénář `TC_...`; úprava
   vzhledu zachovává jeho herní očekávání a čitelnou strukturu. Testy
   vlastností pravidel jsou oddělené od nálezů skutečného pozorování hráčů.
5. Aktualizují se dotčené řádky katalogu, dokumentace a případně grafický
   či zvukový katalog, s datem, revizí a rozsahem důkazu.
6. Změna projde běžným [dokončením a publikací](publishing.md): commit a push
   na `main`, kontrola dokumentace i vývojové distribuce a ověřený odkaz.

Stav požadavku vedeme podle [postupu analýzy a dodání](requirements/README.md#work-through-requirements-one-at-a-time)
spolu s odkazy na pravidla a revizi, aktuální závislostí, výsledkem kontrol
a dalším krokem. `done` znamená dokončenou implementaci s úspěšnou přejímkou;
publikaci ověřujeme samostatně a její výsledek připojíme odkazem. Připravená
analýza není dokončená implementace a ověření není publikace.
Po dodání se nemění celý katalog na „hotovo“ podle jednoho
úspěšného buildu.

## Rozsah této přípravy

Tento návrh ověřuje návaznost manuálu, existujícího katalogu a aktuálních
zdrojů. Znovu byly přečteny zejména pravidla tahu, ukončení a vybrané efekty
zakladatelů; stránka tahu byla zkontrolována i vizuálně. Úplné rozebrání všech
228 položek a čtyř karet zůstává doložené v původním rozboru.
Nevznikla nová implementace herních funkcí ani nové pozorování hráčů.
Rozdělení práce a akceptace výše jsou návrhy pro následující zadání.

## Implementační řez R-0012–R-0018 — 13. října 2026

1. Uživatel schválil společnou implementaci zachovaného menu A01, malého
   nastavení A02, přístupu k obrazovkám a odezvy A03 a obnovení pozice A04.
   Odstranění podmínky kampaně současně naplňuje stejný rozsah R-0001.
2. Model vlastní jméno hráče, herní obsah map a úplnou validaci vybavení.
   Obrazovky využívají jeho náhledy; nevzniká druhý systém pravidel nebo nový
   univerzální systém příkazů. Francii řídí dosavadní AI.
3. Stav implementace a dílčí důkazy jsou v [funkčním záznamu](functional-tests.md#free-play-implementation-2026-10-13).
   Závěrečná regrese prošla; publikaci potvrzuje vývojové workflow. Žádný další požadavek se tím
   automaticky neposouvá do implementace; R-0019 a vyšší zůstávají samostatné.


## Four-container regression experiment — 2026-10-20

1. [R-0253](requirements/R-0253.md) measures four isolated serial UI groups using the existing runner; production CI remains unchanged pending evidence.


## Local acceptance without CI waiting — 2026-10-20

1. [R-0254](requirements/done/R-0254.md) applies the user-selected local completion policy; CI remains configured but remote verification is requested separately.
