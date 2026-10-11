# Plán implementace MicroCol podle manuálu

Tento plán rozděluje dokončení hry na pracovní balíky a hratelné etapy. Slouží
k zadávání práce vývojáři nebo agentovi: vybereme výsledek, doplníme konkrétní
pravidla a ověření a předáme ohraničené zadání. Součástí funkce je její ovládání,
potřebná grafika a srozumitelná odezva.

**Pracovní návrh z 11. října 2026.** Cílem zůstává rozsah pravidel z
[manuálu Sid Meier’s Colonization](Sid_Meiers_Colonization_manual.pdf), včetně
tahů, ekonomiky, soupeřů a války za nezávislost. První hratelné etapy jsou
průběžné výsledky; neznamenají omezení konečného rozsahu. Pořadí níže je návrh
pro další práci, nikoli závazný termín nebo spuštěné implementační úkoly.

## Z čeho vycházíme

| Podklad | K čemu slouží |
| --- | --- |
| [Rozbor pravidel](gap-analysis.md) | Autoritativní katalog 228 pravidel, jejich ID, stránky manuálu, současný stav, referenční tabulky a navržené kontroly. |
| Tento plán | Balíky, závislosti, průběžné výsledky a šablona zadání. Detailní pravidla se sem nekopírují. |
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
distribucí. Balík se může objevit ve více etapách: nejprve společný základ,
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
| E7 Kandidát prvního vydání | Hru lze objevit, dohrát, uložit, aktualizovat a pochopit bez vývojáře vedle hráče. | Uzavřít P01–P32 podle rozsahu, regrese, obsah a hráčské ověření. Prověřit všechny čtyři evropské národy a pět obtížností; zbývající odchylky musí být výslovně rozhodnuté. |

Základní závislost je **tahy a uložený stav → ekonomika a doprava → okolní
svět a boj → politika → revoluce a konec hry**. UX, grafika, nápověda a
ověřování doprovázejí každou etapu. Nečekají až na poslední fázi.

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
| **P01 Pravidlová rozhodnutí a referenční data**; Q-01–Q-11 a čtyři referenční tabulky | Oddělit hodnoty výslovně uvedené v manuálu od našich doplněných vzorců. Připravit nezávislé příklady pro terén, jednotky, profese a stavby. | Před každým závislým úkolem uzavřít jeho konkrétní otázky. Test nesmí pouze číst stejné runtime konstanty jako implementace. |
| **P02 Nová hra**; SETUP-001–009 | Volba jména, čtyř národů, pěti obtížností, počáteční výpravy a data; generovaný svět, Amerika a vlastní parametry. Zapojit národní výhody do příslušných systémů. | P01/P04/P05; národní efekty také P15/P17/P18/P22. Každá volba se uloží a skutečně změní odpovídající chování; stejný seed reprodukuje mapu. |
| **P03 Tahy a kalendář**; TURN-001–002, SETUP-009 | Výslovné fáze Evropa → kolonie → akce jednotek; pořadí domorodci → Anglie → Francie → Španělsko → Nizozemsko; jasný konec hráčského tahu a kola. | P01/P02/P26, domorodé akce P18/P21. Čas se posune právě jednou za kolo; načtení nezopakuje produkci, imigraci ani jednorázové události. |
| **P04 Mapa a průzkum**; MAP-001–009, tabulka terénu | Doplnit terény, lesy, řeky, ložiska a jejich vyčerpání, výnosy, cenu pohybu, obranu a ztracená města. Zachovat funkční odkrývání; počáteční znalost světa ověřit a podle přijatého pravidla upravit. | P01/P02; návaznost na P06/P11/P17. Testovat terén s lesem i bez, zdroje a pohled jednotlivých hráčů; nápověda neprozradí neobjevené informace. |
| **P05 Jednotky a vybavení**; UNIT-001–004, tabulky jednotek a dovedností | Úplný seznam lodí, kolonistů, specialistů a bojových rolí; oddělit dovednost od výstroje. Správné kapacity, rychlosti, koně, muškety a nástroje. | P01; P07/P11/P13/P17 rozvíjejí chování rolí. Každý dostupný typ lze vytvořit a načíst; změna výstroje zachová identitu kolonisty a účtuje správné množství zboží. |

### Pohyb a doprava

| Balík a pravidla | Co doplnit nebo opravit | Závislost a důkaz dokončení |
| --- | --- | --- |
| **P06 Pohyb a rozkazy**; MOVE-001–009 | Účtovat terén, cesty a řeky; trvalý cíl, čekání, přeskočení, hlídka, opevnění, aktivace a rozpuštění jednotky. | P03–P05. Jednotka nepřekročí možnosti tahu; trvalý rozkaz přežije další tah a načtení, lze jej zrušit. UI ukáže zbývající pohyb a rozdíl mezi čekáním a hlídkou. |
| **P07 Pionýři**; PION-001–006 | Výdej a spotřeba nástrojů, orba, kácení, cesty, získané dřevo a rychlost specialisty; přerušení rozpracované práce. | P04–P06/P11; uzavřít relevantní Q-10/Q-11. Úprava pole a spotřeba vzniknou právě jednou, nedostatek nástrojů má srozumitelný důvod. |
| **P08 Náklad a přestupy**; CARGO-001–010, CARGO-015–016 | Správná pravidla přepravy lidí, zboží, děl a pokladu; částečné množství, překládka, nalodění/vylodění, zkratky a vyhození nákladu. | P05/P06/P10. Hraniční kapacity, plný sklad, zrušení a zakázaní cestující neztratí ani nezdvojí zboží nebo jednotky. |
| **P09 Atlantik**; CARGO-011–014 | Nájezd do námořních cest, doba a směr plavby, původní návratové pole a obrat na cestě. | P03/P04/P05/P08; doby podle Q-11, později efekt Magellana P23. Náklad i místo návratu přežijí plavbu a save/load, hráč vidí stav cesty. |
| **P16 Obchodní trasy**; ROUTE-001–004 | Pojmenovaná trasa nejvýše čtyř dosažitelných zastávek, pravidla zboží, výchozí zastávka a opakování; úprava, zrušení a smazání. | P06/P08/P09/P15. Naplněná loď, prázdný sklad nebo ztracená kolonie nezpůsobí zaseknutí tahu; přerušená trasa má opravitelný stav. |

### Kolonie a hospodářství

| Balík a pravidla | Co doplnit nebo opravit | Závislost a důkaz dokončení |
| --- | --- | --- |
| **P10 Kolonie a zásoby**; COL-001–012 | Založení na platném místě, editovatelné jméno a otevření kolonie; společná pole sousedních kolonií, automatická produkce středu, jídlo, růst, hlad a kapacita skladu. Bezpečné rozhodnutí při přeplnění. | P03–P06/P08/P11. Ověřit hranice potravy a skladu, posledního pracovníka a zrušené založení/přesun; zachovat již správně implementované chování. |
| **P11 Výroba a profese**; PROD-001–010 | Sedm výrobních řetězců, pořadí vstupů, skutečná spotřeba specialistů, venkovní výnosy, sluhové/trestanci, konvertité, kříže a chov koní. | P01/P03–P05/P10; konvertité také P19, politické bonusy P22/P23. Model, náhled produkce a výsledné zásoby se shodují včetně nedostatku surovin. |
| **P12 Stavby**; BUILD-001–019, tabulka budov | Populace, předchozí stavba, kladiva a nástroje; správné ceny, upgrady a účinky, změna projektu, dostavění za peníze, lodě/děla, rybolov, celnice a opevnění. | P05/P10/P11, odemykání zakladateli P23. Stavba nic nespotřebuje při zrušení, neobejde podmínky a oznámí chybějící nástroje; každý podporovaný typ má zobrazení. |
| **P13 Vzdělávání**; EDU-001–006 | Učitelé, žáci, trvání a stupně škol; povyšování, zrušení odbornosti, zkušenost prací a domorodá výuka. | P03/P05/P10–P12/P19; parametry P01. Výuka přežije uložení, změnu práce a odchod učitele podle zvolených pravidel; UI ukazuje způsobilost a průběh. |
| **P14 Imigrace a nákupy jednotek**; IMM-001–004, IMM-007 | Kříže místo pevného turnusu, tři obnovovaní kandidáti, zaplacení cesty, evropská univerzita a rostoucí cena lodí/děl. | P03/P05/P08/P11, národní/zakladatelské efekty P02/P23. Kandidát se odstraní a nahradí právě jednou; neúspěšný nákup nemění peníze ani přístav. ID IMM-005/006 v katalogu nejsou. |
| **P15 Evropský trh**; TRADE-001–009 | Nákupní/prodejní ceny, částečná množství, správné čisté výnosy a daň; dynamická reakce národních trhů, ekonomický přehled a obchod celnicí během revoluce. | P08/P09/P11, Q-01 a otázka daně při nákupu; bojkot P22, revoluce P24. Náhled ceny souhlasí s účtem; hranice zásob a peněz, odmítnutí a zákaz obchodu jsou bezpečné. |

### Ostatní národy a boj

| Balík a pravidla | Co doplnit nebo opravit | Závislost a důkaz dokončení |
| --- | --- | --- |
| **P17 Boj a následky**; COMBAT-001–016, tabulka sil | Nahradit pevnou 60% šanci výpočtem z jednotek, výstroje, terénu a bonusů; způsobilost útoku, opevnění, veteráni, zajetí, poškození/opravování lodí a děl, kořist a obsazení kolonie. | P04–P06/P10/P12, Q-08; vztahy P20, politické bonusy P22/P24. Oddělit test síly od řízené náhody; ověřit porážku civilisty, plné kolonie i lodi s nákladem. |
| **P18 Domorodé společnosti**; NAT-001–009 | Osm národů, sídla a hlavní sídla, půda, vztahy ke každému Evropanovi, kmenový hněv a místní poplach; role návštěvníka a dostupné akce. | P02–P05/P10, Q-09. Obchod, přiblížení vojska, zabrání půdy a čas mění odpovídající stav; informace je srozumitelná i bez barev. |
| **P19 Domorodé interakce**; NAT-010–021 | Náčelník, obchod a smlouvání, dary, výuka, tribut a poklad; misionáři, mise, soupeření, podněcování útoku a konverze. | P05/P08/P13/P17/P18; zakladatelé P23. Přijetí/odmítnutí nabídky účtuje právě jednou a ovlivní vztahy; neplatná role dostane vysvětlení. |
| **P20 Evropská diplomacie**; DIP-002–008 | Kontakt, válka/mír/aliance, odlišení korzárů, tribut, žoldnéři, obchod se soupeři, královské války a přístup k informacím. | P02/P08/P15/P17/P18, příslušní zakladatelé P23. Mírová návštěva nevyvolá automatické dobytí; smlouvy skutečně omezí útoky a přežijí načtení. |
| **P21 Chování soupeřů**; DIP-001 a chování NAT | Evropská expanze a hospodářství, přeprava, obchod, obrana a útok; domorodé reakce na vztahy. | P03/P10–P20 postupně. Reprodukovatelné vícekolové scénáře bez zablokování tahu a nepovolených příkazů; soupeř využívá herní pravidla, nikoli jen přesouvá jednotky. |

### Politika a dokončení partie

| Balík a pravidla | Co doplnit nebo opravit | Závislost a důkaz dokončení |
| --- | --- | --- |
| **P22 Daně a podpora nezávislosti**; GOV-001–006 | Daňové požadavky, protest, bojkot a doplacení; místní a národní podpora, zvony, loajalisté a výrobní bonusy/postihy. | P03/P10/P11/P15, Q-01/Q-03. Hráč vidí důsledek volby; prahy a zaokrouhlení mají příklady a neaplikují bonus vícekrát. |
| **P23 Kongres a 25 zakladatelů**; GOV-007, FATHER-001–025 | Výběr, postup náboru, členství a společné zapojení modifikátorů. Potom samostatná dílčí zadání pro jednotlivé efekty. | P22 a systém ovlivněný daným zakladatelem; Q-05. Test před/při/po přijetí, načtení, kombinace efektů a jednorázové dary; žádný zakladatel nezůstane pouze portrétem. |
| **P24 Revoluce**; GOV-008–022 | Podmínky a nevratnost vyhlášení, uzavření Evropy, mobilizace, královská armáda včetně rezerv, okupace, zahraniční pomoc, žoldnéři a všechny podmínky vítězství/prohry. | P03/P17/P20–P23, Q-03/Q-04/Q-08. Ověřit i všechny pobřežní kolonie, 90 % okupované populace a vojáky dosud v Evropě; pokračování po save/load. |
| **P25 Konec hry a skóre**; END-001–004, SCORE-001–009 | Rozlišit opuštění, odchod se skóre, konec v 1800/1850, ztrátu poslední kolonie od 1600 a pokračování bez dalšího skórování. Výpočet položek a síň slávy. | P02/P03/P18/P22–P24, Q-06. Každá větev má samostatný scénář, skóre lze rozepsat a opakované otevření výsledku je nezapíše podruhé. |

### Průřezová práce pro každou etapu

| Balík a pravidla | Co doplnit nebo opravit | Důkaz dokončení |
| --- | --- | --- |
| **P26 Ukládání a obnova**; SUPPORT-001 | Ukládat nové stavy už v příslušné funkci: fáze, rozkazy, výuka, trasy, vztahy, trh, nábor, politika, náhoda a konec hry. Stanovit zacházení se staršími uloženými hrami a autosave. | Načtení nezmění výsledek ani nespustí událost znovu; poškozená/nepodporovaná hra má srozumitelnou chybu a nepoškodí jiný save. Přesný počet slotů řeší Q-07. |
| **P27 Informace a nápověda**; SUPPORT-002–003, DIP-007 | Přehled tahu s přechodem na problém, poradci, ekonomika, práce, flotila, vztahy, Kongres a REF; doplnit encyklopedii a aktualizovat existující výuku. | Údaje odpovídají modelu a oprávněné znalosti hráče. Hlad, chybějící vstup nebo dokončení vedou ke konkrétnímu objektu a možné nápravě. |
| **P28 Ovládání a přístupnost** | Použít společný [UX brief](ux.md#kratke-zadani-ux-pro-novou-funkci): objevitelnost, podmínky, náhled, provedení/zrušení, návazný výběr, klávesnice a čitelnost. | Ověřená skutečná obrazovka, relevantní velikosti okna a texty, význam dostupný bez zvuku i barvy. Hráčská pozorování evidovat odděleně od automatických testů. |
| **P29 Grafika** | Mapa pokrytí pro jednotky, role, výstroj, stavby, terény, rozkazy a politické obrazovky; doplnit chybějící prvky a reprodukovatelný export. Zachovat [současný vzhled](graphics.md#soucasne-zadani-vzhledu). | Zdroj, runtime výstup, alias a spotřebitel jsou dohledatelné; kontrola v reálné velikosti mapy a ikon, variantách a živé obrazovce. Jednotlivé assety vznikají s funkcí. |
| **P30 Zvuková odezva** | Podle [zvukových pravidel](audio.md) připravit krátké efekty, souběh s hudbou, nastavení a vybrané herní události. Rozsah další hudby určit samostatně. | Katalog určí událost, výsledek, příjemce a okamžik; zvuk se nespustí při pouhém náhledu nebo načtení reportu. Vše podstatné zůstane pochopitelné při ztlumení. |
| **P31 Celková kvalita a vyvážení** | Průběžné pravidlové a integrační kontroly, vybrané UI cesty, dlouhá partie, národy/obtížnosti, výkon a skutečné hráčské zkoušky. | Dohledatelné výsledky na dané revizi, regresní scénáře a otevřené vady. Průchod UI skriptu se nevydává za důkaz, že ovládání nový hráč pochopí. |
| **P32 Předání a vydání** | Používat existující CI, launcher a Mac/Linux balíčky; průběžně ověřovat aktualizaci a dokumentaci. Zbývající první vydání a Steam vést podle samostatného odhadu. | Úspěšné kontroly i publikace stejného commitu, odkaz na stažení a postup ověření; připravený plán zacházení s uloženými hrami. Nevytvářet již hotový distribuční systém znovu. |

Přiřazení pokrývá všech 228 katalogových ID. Celé rodiny pravidel zůstávají
v rozsahu, včetně všech 25 zakladatelů a referenčních tabulek; priorita nezařazuje
jejich zbývající části automaticky „mimo první vydání“.

## Co potřebujeme rozhodnout společně

Manuál není úplnou technickou specifikací. Některé přesné vzorce, pravděpodobnosti
a okrajové stavy neuvádí. Před implementací příslušného chování zapíšeme konkrétní
volbu do [existujících otázek Q-01 až Q-11](gap-analysis.md#open-questions-and-decisions),
včetně důvodu a příkladu. Vývojář nesmí takovou volbu potichu vydávat za pravidlo
originálu. Nejasnost pozdní revoluce ale nebrání opravě překládání nákladu.

| Kdy | Společné rozhodnutí | Co lze připravit předem |
| --- | --- | --- |
| Před prvním zadáním měnícím čas nebo novou hru | Počáteční rok, průběh kalendáře, výpravy a potřebné parametry obtížnosti; Q-01/Q-02. | Přehled doložených hodnot, varianty chybějících parametrů a malé modelové příklady. |
| Před změnou sporného výpočtu | Produkce a zaokrouhlení, bojová náhoda, trh, vztahy, politika a revoluce podle příslušných Q. | Nezávislý výpočet síly nebo zásob a testy nesporných částí. |
| U první hratelné etapy a nového typu obrazovky | Zda hráč cestu najde, rozumí důsledkům a zda je návrh příjemný při hraní. | Konkrétní ovladatelný návrh se současnými prvky; rutinní ikony a kontrast nevyžadují nové schvalování stylu. |
| Před větší výrobou obsahu | Rozsah nápovědy a výuky, cílové jazyky, nové ilustrace a hudba, způsob hráčských zkoušek. | Inventář reuse/doplnit, ukázka jedné celé cesty a odhad přírůstku obsahu. |
| Před plánováním termínu vydání | Kapacita týmu, cílový rozsah vydání a platformy nad již fungující Mac/Linux kanál. | Aktualizovat zbývající práci po etapách a oddělit práci, čekání a rezervu. |

Pro společnou kontrolu je výchozí návrh **první kolonie → výroba → konec tahu**.
Pak přidáme **vývoz a návrat z Evropy**, **stavbu a vzdělání**, **kontakt a boj**
a nakonec **politiku a nezávislost**. U každé cesty ukážeme funkční stav,
následující kroky i chybový a zrušený průchod.

## První konkrétní zadání

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

### Z02 Založení pojmenované kolonie

**Balíky:** P10/P28. **Pravidlo:** COL-012; zachovat platné podmínky COL-011.

Hráč na platném místě vyvolá založení, upraví nabídnuté jméno a po potvrzení
se otevře vytvořená kolonie. Zakládající kolonista je stejná jednotka se stejným vlastníkem
a přiřazením k práci. Návrh přijímá neprázdné jméno po odstranění krajních mezer;
další pravidla názvů nesmějí vzniknout potichu jako součást tohoto úkolu.

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

**Balíky:** P01/P03/P26. **Pravidla:** TURN-001/002, SETUP-009 a SUPPORT-001.

Nejprve uzavřít Q-02 pro kalendář a místo posunu data. Manuál, tištěná strana
10 / PDF 16, stanoví národní pořadí a fáze. Nestanoví každý detail vnitřního
výpočtu kolonie; ty zůstávají v Q-10. Potom oddělit fáze tahu a vytvořit
testovatelnou posloupnost na malém scénáři bez nutnosti spouštět grafiku.

- Záznam ukáže Evropu, kolonii a aktivaci jednotek v dohodnutém pořadí.
- Celé kolo posune datum právě jednou; načtení uprostřed hráčských akcí
  nezpůsobí další produkci, příchod kolonisty ani posun času.
- Test pokryje přechod na dalšího hráče i z posledního hráče do nového kola.
- První řez používá dostupné hráče. Úplné národní pořadí vyžaduje P02/P18
  a před jeho ověřením se TURN-001 nesmí označit za dokončené.
- Ceny trhu, nativní AI, vzorce zvonů a celé přepracování save formátu
  nejsou součástí tohoto prvního řezu.

Vstupní kód: [GameManager](../microcol-game/src/main/java/org/microcol/model/GameManager.java),
[Player](../microcol-game/src/main/java/org/microcol/model/Player.java)
a [Calendar](../microcol-game/src/main/java/org/microcol/model/Calendar.java).

## Šablona předání práce

Balík vybírá hlavní agent spolu s vlastníkem hry. Rozpad na úkol má mít jeden
pozorovatelný výsledek. Připravené zadání může vypadat takto:

```text
Úkol: [Z01 / P08 – konkrétní výsledek]
Výchozí revize: [commit main]
Cíl hráče: [co po dokončení dokáže]
Pravidla a zdroj: [ID z gap-analysis.md + stránky manuálu]
Přijatá rozhodnutí: [hodnoty, vzorce, příklad; odkazy na uzavřené Q]
Současný stav: [co už funguje, kde je chyba nebo mezera]
Rozsah změny: [model, UI, data, save/load, dotčené soubory]
Mimo rozsah: [co se v tomto řezu nedělá]
Závislosti: [konkrétní hotové předpoklady]
Ovládání: [nalezení, podmínky, náhled, potvrzení, zrušení, výsledek]
Grafika a zvuk: [existující prvky / konkrétní nové podklady a spotřebitelé]
Akceptace: [počáteční stav → akce → přesný výsledek; také chyba/zrušení]
Ověření: [cílené testy, save/load, UI scénář a potřebná kontrola obrazu]
Časový rámec: [rozpočet práce a kontrolní bod; co předat při nedokončení]
Předání: [diff, výsledek kontrol, dokumentace, zbývající vady, build ke stažení]
```

Návrhová rozhodnutí, integraci a přijetí drží hlavní agent. Ohraničené jednoznačné
implementační úkoly standardně předává dostupnému [lokálnímu workeru](ai/local-qwen.md) podle jeho
[pracovního postupu](../.agents/skills/local-worker/SKILL.md); velká pravidlová
nejasnost nebo celá herní etapa pro něj není vhodné zadání. Jeho návrh testů
ani vlastní tvrzení o úspěchu nenahrazují skutečně spuštěné kontroly.

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
4. Projdou relevantní automatické kontroly a cílený hráčský průchod. Testy
   vlastností pravidel jsou oddělené od nálezů skutečného pozorování hráčů.
5. Aktualizují se dotčené řádky katalogu, dokumentace a případně grafický
   či zvukový katalog, s datem, revizí a rozsahem důkazu.
6. Změna projde běžným [dokončením a publikací](publishing.md): commit a push
   na `main`, kontrola dokumentace i vývojové distribuce a ověřený odkaz.

Pro evidenci úkolu stačí stav **navrženo → připraveno → rozpracováno → ověřeno
→ publikováno**, odkazy na pravidla a revizi, aktuální závislost, výsledek
kontrol a další krok. Po dodání se nemění celý katalog na „hotovo“ podle
jednoho úspěšného buildu.

## Rozsah této přípravy

Tento návrh ověřuje návaznost manuálu, existujícího katalogu a aktuálních
zdrojů. Znovu byly přečteny zejména pravidla tahu, ukončení a vybrané efekty
zakladatelů; stránka tahu byla zkontrolována i vizuálně. Úplné rozebrání všech
228 položek a čtyř karet zůstává doložené v původním rozboru.
Nevznikla nová implementace herních funkcí ani nové pozorování hráčů.
Rozdělení práce a akceptace výše jsou návrhy pro následující zadání.
