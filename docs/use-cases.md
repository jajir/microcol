# Use cases a přejímka pravidel hry

**Migrace požadavků 2026-10-14:** aktuální zbytek R-0001–R-0040 a novější
důkazy vlastní [mapa migrace](requirements/migration-2026-10-14.md), další práci
vybíráme z [nové řady](requirements/backlog.md). Starší auditní pozorování níže
mají platnost své uvedené revize; nejsou všechna tvrzením o aktuální chybě.
Migrace nemění ID ani nepovyšuje dílčí přejímku na hotový celý UC.

Toto je pracovní seznam toho, co musí hráč dokázat v běžné partii MicroCol,
abychom mohli přijmout implementaci pravidel a přesunout hlavní pozornost na
grafiku a ladění UX. Závazná herní pravidla jsou oddělena od nastavení nové
partie a volného návrhu prezentace. Každé **UC** má stabilní ID, výchozí
situaci, kroky, očekávané výsledky a vlastní záznam implementace a ověření.

**Založeno 11. října 2026 podle [manuálu](Sid_Meiers_Colonization_manual.pdf).**
Pravidla, jejich přesné hodnoty, stránky a rozbor zdrojového kódu zůstávají v
[analýze rozdílů](gap-analysis.md). Tato stránka je autoritativní pro hráčské
scénáře a jejich přejímku; [implementační plán](implementation-plan.md) určuje
pracovní balíky a etapy. [Deset pracovních oblastí](requirements/areas.md)
určuje soustředění a přípravu další práce. Oblasti nenahrazují níže uvedené
kategorie Hra / Nastavení / Regrese. UC není nová třída ani nový Java balíček.

## Závazná hra a samostatné nastavení

**Celkem 119 UC** zůstává v jednom katalogu a odkazuje na všech 230 pravidel
rozboru. Rozdělení platí od 11. října 2026; nemění dosavadní stav implementace.
Kategorie je uvedena u každého UC, aby byla součástí předávaného zadání.

| Kategorie | Rozsah | Podmínka dokončení |
| --- | --- | --- |
| **Hra** | 116 UC, UC-003–118 | Závazné chování podle manuálu a výslovně přijatých rozhodnutí. Všechna tato UC a jejich herní varianty musí projít před přesunem hlavní práce na grafiku a UX. |
| **Nastavení** | 2 UC, UC-001–002 | Samostatně přejímané volby nové partie: nabídka map, generátor, průvodce, volba národa, jména a obtížnosti. Popsaný rozsah je výchozí návrh; lze jej později změnit výslovnou úpravou zadání a testů. Neoznačuje se automaticky za hotový při přijetí hry. |
| **Regrese** | 1 UC, UC-119 | Trvalá ochrana přijatého chování. Evidence dokládá poslední konkrétní změnu a běh; budoucí změny vyžadují nové ověření. |

**Otevřít vlastní kolonii, prohlédnout její podrobnosti a budovy, zjistit
zásoby a výrobu a vydat povolený příkaz patří do Hry.** UC-031/033/042/114
ověřují tyto schopnosti a správné informace. Rozmístění panelů, ilustrace,
ikony, klávesy a automatické otevírání oken můžeme měnit; nesmějí odstranit
potřebné informace, zpřístupnit skryté údaje nebo změnit herní důsledek.
Požadavek z manuálu tedy určuje **co hráč může a co se stane**, nikoli přesnou
kopii původních obrazovek. Prezentační rozhodnutí [obnova a prezentace](gap-analysis.md#recovery-and-presentation) neblokují přejímku Hry,
pokud zůstává funkční přístup k informacím, příkazům a obnovení pozice.

**Volba obtížnosti není totéž co její účinek.** Podoba nabídky patří do
Nastavení; národní výhody, parametry obtížnosti, jejich použití ve výpočtech
a uchování v uložené hře patří do Hry (UC-003/004/078/112/115). Všechny čtyři
národy a pět obtížností zůstávají v pravidlových testech. Dokud není hotový
výběrový průvodce, použít předem připravené pozice s výslovně určeným profilem;
za běhu test nesmí obcházet pravidla změnou modelu.

Před přejímkou určit referenční profil běžné nové hry: mapu/seed, národ,
obtížnost, rok, výpravu a tabulku přijatých parametrů s odkazy na [obtížnost a národní výhody](gap-analysis.md#difficulty-and-national-effects)/[kalendář a počáteční výpravy](gap-analysis.md#calendar-and-starting-expeditions)
a případná další rozhodnutí. **Tento profil zatím není uzavřený.** Musí být
hratelný z menu bez kampaně a bez vývojářských zásahů (UC-115). Více mapových
voleb a celý průvodce mohou následovat samostatně; chybějící herní pravidlo
nelze skrýt volbou jednoduššího profilu. Pozdější vlastní parametry mohou
vytvořit další režim, ale nesmějí potichu přepsat referenční pravidla a testy.

## Jak seznam používat

1. V rámci zvolené oblasti a jejího připraveného základu vybrat jedno UC
   nebo malou navazující skupinu; projít odkazovaná pravidla
   a otázky Q v rozboru. Do zadání převzít kategorii a **všechny varianty**,
   ne pouze název. U Hry určit referenční profil; prezentační návrh vést zvlášť.
2. Určit vlastníka podle [mapy balíčků](architecture.md): pravidlo v modelu,
   volba robota v AI, ovládání v GUI, uložený stav v persistenci. Funkce musí
   být dostupná v běžné volné hře, nikoli jen v demonstrační misi.
3. Připojit konkrétní existující nebo nový scénář TC, fixture a modelové testy.
   Jeden UC může potřebovat více TC; jeden TC může dokládat části více UC.
   ID **UC**, pravidel a **TC** se vzájemně nenahrazují ani nepřečíslovávají.
4. Implementovat, zkontrolovat a spustit testy podle
   [funkčního kontraktu](functional-tests.md). Zachovat lineární anglické
   kroky, @Start, page objects a výslovné aserce současných TC.
5. Ve stejném commitu upravit sloupec **Evidence** a dotčené položky rozboru.
   Po publikaci připojit skutečný běh CI a distribuční build. Neúspěšný či
   přeskočený test ponechat otevřený.

### Stav a evidence

V každém řádku jsou dvě nezávislé hodnoty. Zápis **? / N** je výchozí stav,
nikoli tvrzení, že daná funkce neexistuje.

| Pole | Hodnoty |
| --- | --- |
| I — implementace celého UC | **?** aktuálně neposouzeno; **0** chybí; **R** rozpracováno nebo část neodpovídá; **H** implementováno včetně všech variant jeho kategorie. I=H vyžaduje odkaz na implementační commit a kontrolu všech souvisejících požadavků. |
| V — ověření celého UC | **N** neběželo v úplném rozsahu; **B** část kritérií blokuje nerozhodnutá otázka; **F** poslední ověření selhalo; **P** všechny varianty prošly na uvedené revizi. |
| TC / důkaz | Konkrétní testy a jejich metody, fixture/seed, datum, commit, příkaz nebo CI report, počet vybraných/prošlých/chybných/přeskočených testů; případný zbylý blokátor. Pro ruční krok reprodukovatelný postup a pozorovaný výsledek. |

Příklad formátu budoucího záznamu: **R / F; TC: odkaz na třídu#metodu;
revize: SHA; běh: URL; vybráno/prošlo/chyby/přeskočeno: 3/2/1/0;
zbývá: odmítnutí akce bez nástrojů**. Jde o vzor, nikoli provedený test.
Částečný úspěch lze takto zapsat, ale celé UC se neoznačí jako hotové.
U UC-119 znamená úspěšná evidence ochranu na uvedené revizi, nikoli trvalé
uzavření regresního ověřování. Odkaz na stejné pravidlo z různých kategorií
neznamená, že přijetí jednoho UC přijalo i ostatní.

**Výchozí stav:** žádné UC zde není přijato jako kompletně ověřené.
Rozbor z 2. října posoudil původních 228 pravidel: 20 statických shod,
30 částečných, 40 odlišných, 134 chybějících a 4 nejasná. To je orientace pro
implementaci, ne výsledek nových scénářů. Doplnění rozboru z 11. října jsou
označena zvlášť. Existující testy dále chrání své dosavadní aserce.

Dne **15. října 2026** uživatel přijal [prozatímní herní pravidla](gap-analysis.md#accepted-game-rules). Scénáře používají jejich hodnoty a okrajové podmínky; pozdější ladění a ověření shody s originálem nejsou blokující. Stav **N** znamená, že úplné ověření neběželo. Implementační stavy a starší důkazy se nemění. Detailní návrh a technické závislosti zůstávají předmětem jednotlivých R. [R-0115](requirements/done/R-0115.md) eviduje přijetí pravidel; [R-0116](requirements/done/R-0116.md) jejich začlenění bez zodpovězených otázek.

### Společné podmínky všech scénářů

Aktérem je lidský hráč; u soupeřových akcí je výslovně uvedena AI. Test si
připraví vlastní reprodukovatelnou pozici, vlastníky, datum, obtížnost, zásoby
a jednotky. Akce hráče prochází skutečným UI, výsledek ověřuje i stav modelu.
Samotná přítomnost ikony či otevření okna nestačí.

- Pro každou volbu s potvrzením prověřit přijetí, zrušení a nesplněnou
  podmínku. Zrušená či zamítnutá transakce nic neodebere. Výslovná herní
  volba s následky, například protest proti dani, provede právě své popsané
  následky. Automatický tahový děj nemá umělou možnost zrušení.
- U množství ověřit nulu, hranici −1 / hranici / hranici +1 a vyčerpání zdroje,
  kde mají herní smysl. Kontrolovat obě strany převodu a provedení právě jednou.
- Náhodné výsledky vyvolat řízeným seedem nebo testovým zdrojem náhody;
  ověřit možné větve i vstupy výpočtu. Jedno vítězství nedokazuje pravděpodobnost.
- Stav, který trvá více tahů, uložit uprostřed děje, načíst a dokončit.
  Nesmí se ztratit postup ani zopakovat odměna. Kontrolovat také další tah.
- Člověk a AI podléhají stejným pravidlům; zvláštní výjimky národů a královské
  armády jsou výslovné. Očekávání se nečte z konstant testované implementace.
- Informace potřebná pro rozhodnutí a vysvětlení výsledku musí být čitelně
  dostupná už nyní. Výtvarné provedení, animace a podrobný UX polish následují
  po funkční přejímce; nesmí měnit očekávané herní výsledky.

## Začátek hry a tahy

Zdroj: tištěné strany 2, 7–10 / PDF 8, 13–16;
[pravidla nastavení a tahů](gap-analysis.md#setup-and-turn-sequence).

| UC a pravidla | Příprava a kroky | Očekávání a povinné varianty | Evidence I / V; TC |
| --- | --- | --- | --- |
| UC-001 — Nastavit mapu nové partie<br>SETUP-001<br>**Nastavení** | Z čistého profilu otevřít Novou hru; zvolit náhodný Nový svět, Ameriku a vlastní parametry mapy. Jednou nastavení zrušit. | Výchozí návrh Nastavení: každá volba vytvoří hratelnou volnou partii; vlastní velikost pevnin, vlhkost a klima ovlivní mapu. Stejný testový seed a nastavení reprodukují svět. Zrušení vrátí menu bez rozehrané partie. Samotný dostupný vstup do běžné hry bez kampaně přejímá Hra v UC-115. | ? / N; — |
| UC-002 — Zvolit národ a obtížnost<br>SETUP-002, SETUP-003<br>**Nastavení** | Přes nastavení zahájit hru pro Anglii, Francii, Španělsko a Nizozemsko na každé z pěti obtížností; zadat vlastní jméno. | Výchozí návrh Nastavení: matice 4 × 5 startů předá vybraný národ, obtížnost a samostatné jméno do nové partie. Zrušení nic nespustí; rozmístění a způsob volby nejsou předepsané. Uchování a skutečné účinky parametrů mají vlastní povinnou přejímku Hry v UC-003/004/078/112/115. | ? / N; — |
| UC-003 — Uplatnit národní výhodu<br>SETUP-004, SETUP-005, SETUP-006, SETUP-007<br>**Hra** | V jinak stejných pozicích vyvolat imigraci, kontakt s domorodci, útok na jejich sídlo a stejný objem obchodu. | Anglie získává více přistěhovalců; Francie vyvolává menší nepřátelství; Španělsko má +50 % k útoku na domorodé sídlo, nikoli libovolný cíl; nizozemské ceny kolísají méně. Zkontrolovat každou výhodu i nepříslušný národ. | ? / N; pravidla [obtížnost a národní výhody](gap-analysis.md#difficulty-and-national-effects); — |
| UC-004 — Zachovat herní profil a postupovat kalendářem<br>SETUP-002, SETUP-003, SETUP-008, SETUP-009<br>**Hra** | V referenčních pozicích všech 4 národů × 5 obtížností prohlédnout příslušnost, soupeře, nezávislé jméno hráče, parametry a počáteční expedici; dokončit několik celých kol včetně načtení uprostřed. | Národ, obtížnost, přijaté parametry a jméno se nezamění a přežijí uložení; parametry podle přijatých pravidel (viz [obtížnost a národní výhody](gap-analysis.md#difficulty-and-national-effects)) řídí příslušná pravidla, nejsou jen popisek. Výchozí rok je 1490 (výslovná odchylka od manuálu); nizozemská obchodní loď a ostatní sestavy odpovídají rozhodnutí [kalendář a počáteční výpravy](gap-analysis.md#calendar-and-starting-expeditions). Datum postupuje jednou za celé kolo podle přijatého kalendáře, ne po každém hráči. Nezávisí na podobě výběrového UI z UC-002. | ? / N; pravidla [obtížnost a národní výhody](gap-analysis.md#difficulty-and-national-effects), [kalendář a počáteční výpravy](gap-analysis.md#calendar-and-starting-expeditions); kalendářní část ověřena 2026-10-15: TC37 a modelové testy, [evidence](functional-tests.md#r-0051-calendar-and-continue-2026-10-15); R-0066 lokálně ověřuje startovní sestavy 4 × 5; R-0063 přidává skutečný výběr čtyř národů a TC24 Start/Continue ([evidence R-0063](functional-tests.md#r-0063-nation-selection-2026-10-16)) ([evidence](functional-tests.md#r-0066-reference-expeditions-2026-10-16)); úplné účinky profilů zůstávají N |
| UC-005 — Dokončit tah ve správném pořadí<br>TURN-001, TURN-002<br>**Hra** | Připravit v jednom kole evropskou událost, výrobu, hlad a aktivní jednotky; postupně skončit tahy včetně tahu bez pohyblivé jednotky. | Domorodci → Anglie → Francie → Španělsko → Nizozemsko, nezávisle na pořadí v uloženém souboru. U národa nejprve Evropa, potom kolonie, pak pohyb/boj a správa. Každý děj právě jednou; správu lze provádět i před potvrzením konce tahu. Královské tahy a bezpečnou hranici uložení určuje [pořadí národních tahů](gap-analysis.md#national-turn-processing). | ? / N; pravidla [pořadí národních tahů](gap-analysis.md#national-turn-processing); částečná [evidence TC36 a modelu](functional-tests.md#r-0052-partial-turn-order-delivery-2026-10-15); [hranice přípravy a obnova](functional-tests.md#r-0052-preparation-boundary-2026-10-15); chybějící fáze trhu a domorodců zůstávají mimo toto dílčí převzetí |

## Mapa a průzkum

Zdroj: strany 14–22 / PDF 20–28, karta PDF 135;
[mapa a referenční hodnoty](gap-analysis.md#map-terrain-and-exploration).

| UC a pravidla | Příprava a kroky | Očekávání a povinné varianty | Evidence I / V; TC |
| --- | --- | --- | --- |
| UC-006 — Odkrýt neznámý svět<br>MAP-007, MAP-008<br>**Hra** | Začít s expedicí, pohybovat se, opustit objevenou oblast a načíst uloženou hru. | Na začátku známe pouze okolí expedice, nikoli skryté soupeře. Objevený terén zůstává známý. Oddělit znalost terénu od aktuální znalosti cizích jednotek; ověřit přijatý dosah dohledu. | ? / N; pravidla [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport); dílčí [R-0019 / TC26](functional-tests.md#fog-implementation-2026-10-13): neprozkoumané buňky, překryvy a Continue; R-0069 zavádí dohodnutý dohled současných rolí a ověřuje TC25/TC26 ([evidence](functional-tests.md#r-0069-explicit-sight-2026-10-16)); úplné CI a samostatná živá viditelnost R-0070 zůstávají otevřené. |
| UC-007 — Použít všechny druhy terénu<br>MAP-001, MAP-002, MAP-003<br>**Hra** | Na referenční mapě bez bonusů projít a obhospodařit každý terén a dovolenou lesní variantu; uložit a načíst. | Každá buňka terén × les × komodita z karty má správnou základní produkci, cenu vstupu a obranu. Zahrnout tundru s lesem, jezera, oceán a námořní cesty. Sporné buňky vyhodnotit podle přijatých pravidel, viz [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education), nikoli přepsat podle aktuálních konstant. | ? / N; pravidla [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education); — |
| UC-008 — Využít řeky a zvláštní zdroje<br>MAP-004, MAP-005<br>**Hra** | Porovnat shodná pole bez řeky, s malou/velkou řekou a postupně se všemi typy zvláštního zdroje; pohybovat se podél řeky. | Řeka zlepší příslušnou produkci a cestování po jejím toku; velká má větší efekt. Každý zdroj zvýhodní správné komodity, včetně zvěře, oázy, loviště a ryb. Směr spojení, velikost řeky a zdroj se uloží. | ? / N; pravidla [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education), [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport); — |
| UC-009 — Vyčerpat ložisko<br>MAP-006<br>**Hra** | Opakovaně těžit stříbrné, rudné a smíšené ložisko; kontrolní jednotka na obdobném poli pěstuje plodinu. | Konečná zásoba se těžbou vyčerpá a její bonus zmizí, základní terén zůstane. Jiná činnost ložisko nespotřebuje. Po načtení se zásoba neobnoví; velikost zásoby a rychlost čerpání jsou rozhodnutí [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education). | ? / N; pravidla [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education); — |
| UC-010 — Prozkoumat zvěst o ztraceném městě<br>MAP-009<br>**Hra** | Vstoupit do značky obyčejným a zkušeným průzkumníkem; řízeně vyvolat přínos, prázdný nález a nebezpečí. | Zpráva a změna světa odpovídají výsledku, značka se spotřebuje jednou. Odbornost ovlivní průzkum; načtení nevydá druhou odměnu. Konkrétní odměny, rizika a pohřebiště navazují na [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations)/[průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport). | ? / N; pravidla [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations), [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport); — |

## Pohyb a rozkazy

Zdroj: strany 25–27 / PDF 31–33;
[pohyb](gap-analysis.md#movement-and-persistent-orders).

| UC a pravidla | Příprava a kroky | Očekávání a povinné varianty | Evidence I / V; TC |
| --- | --- | --- | --- |
| UC-011 — Přesunout jednotku o pole<br>MOVE-001, MOVE-002<br>**Hra** | Vyzkoušet osm směrů s pěší jednotkou i lodí, okraje mapy a přístav. | Pěší zůstává na souši bez přepravy; loď na vodě nebo v pobřežním sídle. Nelze překročit sever/jih ani proplout pobřežní kolonií do vnitrozemské. Neplatný krok nezmění místo ani pohyb. | H / P; [implementace df25024d](https://github.com/jajir/microcol/commit/df25024d4b3e12ca4584586a6da0d047d59c5087), TC38/01–04 a 14 modelových případů, úplná regrese a publikace prošly 2026-10-16; [evidence R-0054](functional-tests.md#r-0054-coastal-port-movement-2026-10-15) |
| UC-012 — Zaplatit pohyb podle terénu<br>MOVE-003, MAP-002<br>**Hra** | Projít trasu přes rovinu, les, kopce, hory, řeku a silnici s různým zbytkem pohybu. | Provedený krok, plán cesty i zvýrazněný dosah používají stejné ceny. Vyčerpaná jednotka nepokračuje. Varianta ceny vyšší než celý tahový příděl se řídí přijatými pravidly, viz [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport). | ? / N; R-0072: modelové testy a TC44 ověřují obrácení, zrušení, zastaralé potvrzení, Continue a návrat k blokovanému pobřeží; [evidence](functional-tests.md#r-0072-voyage-reversal-2026-10-16). Úplné CI čeká. |
| UC-013 — Dojít na vzdálené místo<br>MOVE-004<br>**Hra** | Zadat bod vzdálený alespoň tři tahy a pojmenovaný přátelský cíl; v polovině uložit, načíst, jednou rozkaz zrušit. | Jednotka pokračuje do cíle, nepřekročí pohyb a nenabízí nedosažitelné přátelské cíle. Zrušení vrátí ruční řízení. Obsazení nebo zablokování trasy nesmí způsobit nelegální krok či nekonečný tah. | ? / N; pravidla [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport); — |
| UC-014 — Počkat nebo vynechat pohyb<br>MOVE-005<br>**Hra** | Se třemi aktivními jednotkami použít Čekat (Tab) a Vynechat tah (Mezerník), opakovat s jednou/žádnou jednotkou, uložit a dvakrát pokračovat. | Čekající se znovu aktivuje po ostatních; vynechaná až v dalším tahu. Ani jedna volba nevytvoří trvalý rozkaz; opakované čekání nesmí ztratit jednotku z pořadí. | H / P; [implementace 79784bd7](https://github.com/jajir/microcol/commit/79784bd7515778e22650c695412e2795dece5cf2), TC11/01–04 a 12 modelových případů, úplná regrese a publikace prošly 2026-10-15; [evidence R-0053](functional-tests.md#r-0053-unit-turn-orders-2026-10-15) |
| UC-015 — Držet hlídku a opevnit jednotku<br>MOVE-006, MOVE-007, MOVE-008<br>**Hra** | Vydat hlídku a opevnění, ukončit tah, přiblížit cizí jednotku, potom jednotky ručně aktivovat. | Hlídka běžně nepřekáží aktivací, cizí soused ji probudí. Opevnění dává +50 % až příští tah. Aktivace zruší rozkaz a dovolí další platnou akci; u skupiny se změní jen vybraná jednotka. Automatické nalodění viz UC-025. | ? / N; R-0073 přidává hlídku a její aktivaci; modelové testy i vybraná hráčská regrese prošly, úplné CI čeká. [Evidence](functional-tests.md#r-0073-sentry-and-automatic-boarding-2026-10-16). Opevnění zůstává samostatné. |
| UC-016 — Zrušit práci nebo rozpustit jednotku<br>MOVE-008, MOVE-009<br>**Hra** | Aktivovat jednotku během orby, cesty a obchodní trasy; samostatně rozpustit vlastní jednotku, včetně nosiče s nákladem. | Rozkaz opravdu skončí, zachovají se jen dokončené účinky podle přijatých pravidel (viz [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport)). Rozpuštěná jednotka zmizí ze světa, výběru i uložené hry. Dopad na cestující a náklad odpovídá přijatým pravidlům rozpuštění; cizí jednotku rozpustit nelze. | ? / N; pravidla [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport); — |

## Vybavení a úpravy krajiny

Zdroj: strany 26–27, 46, 65 / PDF 32–33, 52, 71; karta PDF 133;
[vybavení a pionýři](gap-analysis.md#equipment-and-pioneers).

| UC a pravidla | Příprava a kroky | Očekávání a povinné varianty | Evidence I / V; TC |
| --- | --- | --- | --- |
| UC-017 — Vybavit kolonistu v kolonii<br>UNIT-001, PION-001<br>**Hra** | Vlastnímu kolonistovi dát koně, muškety a nástroje ze skladu; vyzkoušet i vojáka → dragouna a průzkumníka → dragouna. | Koně/muškety po 50, mez 49/50; nástroje: 0/1/19 odmítnout, 20/99/100/101 vybaví 20/99/100/100. Sklad klesne přesně, druhé vybavení se neztratí. Identita a dovednost kolonisty zůstávají. | ? / N; — |
| UC-018 — Změnit vybavení na evropském molu<br>UNIT-002<br>**Hra** | Na molu koupit/prodat koně a muškety, koupit nástroje a prodat jejich zbytek; opakovat bez dostatku zlata. | Koně/muškety po 50, nákup nástrojů 100, prodej skutečného zbytku. Souhlasí nabídka ceny, pokladna a výbava; neúspěch nic neodebere. Daň/zaokrouhlení nákupu se řídí stejným rozhodnutím jako UC-057. | ? / N; pravidla [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education); — |
| UC-019 — Dokončit orbu<br>PION-002<br>**Hra** | Pionýr s 20/40/100 nástroji začne orat vhodné holé pole; kontrolně nevybavený kolonista, les, nevhodný a už zoraný terén. | Platný rozkaz přetrvá potřebné tahy, dokončí pole a spotřebuje 20 nástrojů právě jednou. Neplatné varianty odmítne. Po práci a načtení se změní správné výnosy. Délka a přerušení jsou [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport); současné TC18/TC15 dokazují jen zahájení, ne celý UC. | ? / N; pravidla [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport); částečné testy viz níže |
| UC-020 — Vykácet les<br>PION-003<br>**Hra** | Pionýr vykácí les, dokončí práci, poté na témže poli vyrábět a případně orat. | Spotřebuje 20 nástrojů, přidělí dřevo podle přijatého množství/cíle, les trvale odstraní. Pole již neposkytuje dřevo/kožešiny z lesa; zemědělské výnosy se upraví. Opakované kácení ani načtení nedá další dřevo. | ? / N; pravidla [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education), [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport); — |
| UC-021 — Postavit silnici a využít zkušeného pionýra<br>PION-004, PION-005, PION-006<br>**Hra** | Postavit silnici; porovnat pohyb a výnos rudy, kožešin, dřeva a horského stříbra. Všechny tři práce opakovat běžným a zkušeným pionýrem. | Silnice stojí 20 nástrojů jednou, uloží se a zrychlí cestu. Stříbro získá silniční bonus jen s ložiskem. Zkušený pionýr dokončí každou práci dříve při stejné spotřebě nástrojů. Přesné časy/bonusy [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education)/[průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport). | ? / N; pravidla [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education), [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport); — |

## Lodě, náklad a obchodní trasy

Zdroj: strany 26, 28–29, 42–43, 59–65 / PDF 32, 34–35, 48–49, 65–71,
doplněk PDF 3, karta PDF 132;
[přeprava](gap-analysis.md#transport-ships-and-atlantic-travel) a
[trasy](gap-analysis.md#automated-trade-routes).

| UC a pravidla | Příprava a kroky | Očekávání a povinné varianty | Evidence I / V; TC |
| --- | --- | --- | --- |
| UC-022 — Provozovat všech šest lodí<br>UNIT-003, UNIT-004<br>**Hra** | Získat každou loď dovoleným nákupem, stavbou nebo událostí, pohybovat se a zaplnit náklad. | Pohyb/prostory: karavela 4/2, obchodní loď 5/4, galeona 6/6, korzár 8/2, fregata 6/4, man-o-war 6/6. Platí i po načtení; královské/intervenční lodě se neobjeví jako obyčejný předrevoluční nákup. Sílu ověřuje UC-073. | ? / N; [R-0108](requirements/done/R-0108.md): modelová matice šesti typů, TC30/08–10 a TC29/06 cíleně ověřeny; úplná regrese/publikace a související zvláštní bojová pravidla zůstávají otevřené. [Evidence](functional-tests.md#r-0108-six-ship-roster-2026-10-16). |
| UC-023 — Přesunout celé i částečné zboží<br>CARGO-001, CARGO-009<br>**Hra** | Přesouvat sklad ↔ loď, sklad ↔ vůz a nosič ↔ nosič; z 60 kusů zvolit 17. Prověřit prázdný i částečně plný prostor a evropský nákup. | Prostor obsahuje nejvýše 100 kusů jediného druhu. Přesun zachovává součet, respektuje zdroj i volno, nedovolí směs ani přetečení na modelovém vstupu. Meze 99/100/101; obsloužit vůz také přes kolonijní UI. | R / N; Z01 loď → sklad: [TC19 a modelové hranice](functional-tests.md#z01-bezpecne-vylozeni-nakladu); Z01 ověřeno 17 UI scénáři na c2e24ba4. [R-0024: cílené vykládání vozu](functional-tests.md#wagon-unloading-implementation-2026-10-13) i úplná regrese prošly; publikace čeká. Ostatní směry nejsou tímto řezem přijaty. |
| UC-024 — Naložit jednotky a poklad<br>CARGO-002, CARGO-003, CARGO-004, CARGO-005<br>**Hra** | Ze sousední souše nalodit obyčejného/vybaveného kolonistu, dělostřelectvo a poklad; zkusit plnou/cizí/vzdálenou loď a vůz. | Kolonista s výbavou i dělo potřebují jeden prostor; poklad šest volných prostorů. Vůz veze pouze zboží; loď ani vůz není běžný cestující. Odmítnutí nic neodebere, vyložení či zničení uvolní celou rezervaci. | ? / N; — |
| UC-025 — Automaticky nalodit a selektivně vylodit<br>CARGO-006, CARGO-007, CARGO-008<br>**Hra** | Vyplout s čekajícími lidmi v Evropě i hlídkami v kolonii; změnit pořadí a odmítnout nalodění. Připlout do pobřežního sídla a zvlášť na volné pobřeží. | Nalodí se jen oprávnění lidé podle pořadí a volných prostorů; lze posunout emigranta dopředu i vyřadit. V sídle cestující automaticky vystoupí; při landfall lze vybrat jednotlivé lidi a jiné nechat na palubě. Zrušení vylodění zachová náklad. | ? / N; R-0073 přidává nalodění výslovně zadaných hlídek podle ID a kapacity; [evidence](functional-tests.md#r-0073-sentry-and-automatic-boarding-2026-10-16) rozlišuje dokončené a čekající kontroly. Pořadí emigrantů a ostatní části UC tím nejsou uzavřené. |
| UC-026 — Rychle naložit, vyložit a odhodit náklad<br>CARGO-010, CARGO-016<br>**Hra** | S více druhy zásob použít rychlé naložení, opakované vyložení a odhození vybraného zboží na moři. | Nakládání vybere nejhodnotnější dostupné zboží kromě koní, nástrojů a zbraní. Vykládání zpracuje prostor za prostorem; odhození nepřidá zlato ani nezmění jiné prostory. Pořadí částečných zásob a rozsah odhození ověřit podle přijatých pravidel, viz [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport). | ? / N; pravidla [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport); — |
| UC-027 — Přeplout Atlantik a vrátit se<br>CARGO-011, CARGO-012, CARGO-013, CARGO-015<br>**Hra** | Na obou stranách mapy vstoupit do námořní cesty, pohybovat se podél ní, potom ven; s cestujícími a zbožím absolvovat zpáteční plavbu. | Samotný vstup do cesty nespustí přejezd, až pohyb ven. Plavba trvá 1–4 tahy podle přijatých faktorů; návrat vede na zaznamenané výchozí pole. Náklad i výbava zůstávají. Uložení uprostřed a obsazený návratový bod mají definovaný výsledek. | ? / N; pravidla [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport); — |
| UC-028 — Obrátit loď během plavby<br>CARGO-014<br>**Hra** | Obrátit oba směry cesty před příjezdem a po načtení pokračovat. | Změní se cíl a správně zbývající čas podle přijatých pravidel (viz [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport)), nikoli náklad nebo vlastník. Loď nepřijede dvakrát a nevyskytuje se současně na mapě i v Evropě. | ? / N; pravidla [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport); — |
| UC-029 — Zadat a projet obchodní trasu<br>ROUTE-001, ROUTE-002, ROUTE-003<br>**Hra** | Vytvořit pojmenovanou pozemní/námořní trasu, až čtyři zastávky a šest nakládaných i šest vykládaných druhů na každé. Přiřadit loď/vůz a první zastávku. | Nabídka cílů respektuje dosažitelnost a typ dopravy, povolená námořní trasa zahrne Evropu. Vyloží a pak naloží podle zásob/kapacity; evropská transakce účtuje peníze. Proběhnou dva okruhy s načtením uprostřed; pěší jednotku přiřadit nelze. | ? / N; pravidla [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport); — |
| UC-030 — Změnit a zrušit trasu<br>ROUTE-004, MOVE-008<br>**Hra** | Za běhu upravit zastávky/náklad, zrušit rozkaz jednoho nosiče a smazat přiřazenou trasu. | Změny se promítnou v určeném okamžiku, ostatní nosiče nerozbije zrušení jednoho. Po smazání nezůstanou visící odkazy ani nekonečný tah; hráč dostane zpět řízení. Nedostupná/ztracená zastávka má rozhodnutou obsluhu. | ? / N; pravidla [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport); — |

## Kolonie, obyvatelé a výroba

Zdroj: strany 33–58 / PDF 39–64, karty PDF 133–135;
[kolonie](gap-analysis.md#colony-workforce-food-and-storage) a
[produkce](gap-analysis.md#production-and-professions).

| UC a pravidla | Příprava a kroky | Očekávání a povinné varianty | Evidence I / V; TC |
| --- | --- | --- | --- |
| UC-031 — Založit, otevřít a prohlédnout kolonii<br>COL-001, COL-011, COL-012, BUILD-016<br>**Hra** | Založit kolonii běžným kolonistou na různých pozemních terénech; změnit navržené jméno. Otevřít její detail, vrátit se na mapu a znovu otevřít; totéž po načtení. Zkusit hory, moře a domorodého konvertitu. | Platný zakladatel pracuje uvnitř své kolonie. Detail zpřístupní její jméno, obyvatele, jednotlivé existující budovy, zásoby a produkci shodné s modelem; pouhé prohlížení nic nespotřebuje. Automatické počáteční budovy odpovídají tučným řádkům karty PDF 134. Střed bez dalšího pracovníka vyrábí jídlo a druhou místní komoditu, respektuje zdroje kromě Prime Timber. Neplatné založení je odmítnuto; rozestupy ověřit podle přijatých pravidel, viz [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education). Automatické otevření detailu po založení a jeho rozložení jsou volba prezentace. | ? / N; pravidla [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education); [Z02: pojmenování, identita a save/load](functional-tests.md#z02-pojmenovani-a-identita-kolonie). Dílčí kontrola pevniny/hor, okraje a neúrodného stanoviště je doložená v [R-0027](functional-tests.md#rival-first-colony-preparation-2026-10-14). Přijetí celého UC včetně všech zakladatelů, středové výroby a budov zůstává otevřené. |
| UC-032 — Rozdělit pracovníky na pole a do dílen<br>COL-002, COL-003, COL-004<br>**Hra** | Obsazovat osm okolních polí a dílnu, měnit komoditu a přesouvat lidi; přidat sousední kolonii se sdíleným polem. | Na jednom poli jeden člověk a jedna komodita, i mezi koloniemi. V běžné výrobní budově nejvýše tři lidé; čtvrtý odmítnut. Přesun nezdvojí pracovníka ani výnos. Samostatná školní omezení platí podle UC-050. | R / N; Dokončené R-0055: osm scénářů TC_39/F31 ověřuje sdílené pole, zrušení a zastaralé akce, produkci po obnově a odmítnutí konfliktního uložení; [aktuální evidence](functional-tests.md#r-0055-completion-work-2026-10-16). Doplňující hraniční matice i úplná CI prošly; publikováno v `dev-105-3c521c5dcb9a`. Samostatné pokrytí kapacity dílen zůstává otevřené, proto celý UC není uzavřen. |
| UC-033 — Vybrat práci podle výnosu<br>PROD-011<br>**Hra** | Pro stejného pracovníka porovnat současné pole, dvě volná s rozdílným výnosem a ještě lepší obsazené/sdílené pole. Zvolit práci i z pracovníka uvnitř, projít nabídku venkovních i vnitřních zaměstnání. | Nabídka ukáže současný/nejlepší dostupný výnos; u pracovníka uvnitř nejlepší. Vlastní odbornost je označena. Volba venkovní práce u pracovníka uvnitř jej umístí na nejproduktivnější dostupné pole; pouhá změna plodiny již obhospodařovaného pole jej automaticky nestěhuje. Zohlední jeho schopnosti, nedovolí obsazené pole; zrušení ponechá práci. Shodu více nejlepších polí lze rozhodnout bez změny výnosu. | H / P; [R-0056](requirements/done/R-0056.md), [implementace `75341b36`](https://github.com/jajir/microcol/commit/75341b36bcc2d1e760fe6b6c0e7405007fdb3c67), [TC40](../microcol-test/src/test/java/org/microcol/test/TC_40_F32_colony_job_choices_test.java): všech 15 scénářů i úplná regrese (1298 herních a 185 UI kontrol) prošly 16. 10. 2026. Publikováno jako `dev-107-75341b36bcc2`; [evidence](functional-tests.md#r-0056-job-choice-implementation-2026-10-16). |
| UC-034 — Nakrmit kolonii a rozšířit populaci<br>COL-005, COL-006, COL-009<br>**Hra** | Produkovat jídlo s lidmi uvnitř a posádkou venku; po spotřebě připravit 199/200/201 jídla, zvlášť dovézt zásobu přes hranici. | Každý pracovník uvnitř spotřebuje 2 jídla za tah; vnější posádka se takto neúčtuje. Za 200 přebytku vznikne svobodný kolonista a 200 se odečte. Potraviny se nesmějí oříznout na 199 před vyhodnocením narození. Více porodů a okamžik po dovozu ověřit podle přijatých pravidel, viz [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education). | H / P; 2026-10-16, [0b0cb059](https://github.com/jajir/microcol/commit/0b0cb0593df4326754f1111f60ad63f1fde64f09): spotřeba, hranice 199/200/201/399/400/401, dovoz lodí/vozem, více narození, identita a pokračování; TC08/17/41, [přejímka R-0057–R-0061](functional-tests.md#r-0057r-0061-cloud-sol-batch-2026-10-16). Úplná lokální regrese 1325 modelových + 187 UI testů prošla. |
| UC-035 — Přežít nedostatek jídla<br>COL-007<br>**Hra** | Nedostatek výroby nejprve pokrýt skladem; pak vyvolat přesnou nulu a zápornou bilanci, opakovat do posledního obyvatele. | Pokrytý deficit ani nula nikoho nezabije. Při nepokrytém nedostatku zemře jeden kolonista za tah, přijde zpráva a zůstanou platná pracovní místa/vlastnictví. Výběr oběti, pořadí dějů a poslední obyvatel mají přijatý výklad [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education). | H / P; 2026-10-16, [0b0cb059](https://github.com/jajir/microcol/commit/0b0cb0593df4326754f1111f60ad63f1fde64f09): nula, jedna náhodná ztráta, přepočet přeživších, konečné zásoby ve zprávě, poslední obyvatel, lodě a pokračování; TC09/10, [přejímka R-0057–R-0061](functional-tests.md#r-0057r-0061-cloud-sol-batch-2026-10-16). Budoucí opravy lodí vlastní R-0098. Úplná lokální regrese prošla. |
| UC-036 — Uložit zásoby a řešit přetečení<br>COL-008, COL-010<br>**Hra** | Pro každý nepotravinový druh ověřit kapacitu bez skladu, se skladem a rozšířením; do 99/100 zásob vyložit 100 kusů. | Kapacity jsou 100/200/300. Při nadměrném vyložení hráč rozhodne; zrušení zachová loď i sklad, potvrzení provede jen schválené množství. Oddělit přetečení výroby, transfer a zvláštní pravidlo jídla; ukázat příslušné zprávy. | R / N; pravidla [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education); [Z01: kapacita a bezpečné vyložení](functional-tests.md#z01-bezpecne-vylozeni-nakladu); Z01 model i UI ověřeny na c2e24ba4; [R-0024](functional-tests.md#wagon-unloading-implementation-2026-10-13) doplňuje cíleně ověřený vůz. Výroba a jídlo zůstávají mimo řez. |
| UC-037 — Vyrobit zboží ze surovin<br>PROD-001, PROD-002, PROD-003, PROD-006<br>**Hra** | Provést všech sedm řetězců; s volným kolonistou dát dost materiálu, potom nedostatek. Zvlášť expert kovář s 3 novými a 3 skladovanými rudami. | Dřevo→kladiva, ruda→nástroje, tabák→doutníky, bavlna→látka, cukr→rum, kožešiny→kabáty, nástroje→muškety. Základní svobodný pracovník dává 3, expert z příkladu 6 nástrojů za 6 rud; s pouhými 3 rudami nejvýše 3. Nejprve se využije aktuální těžba, pak sklad, nic nevznikne bez vstupů. | R / N; řez kovářského experta přijat na [0b0cb059](https://github.com/jajir/microcol/commit/0b0cb0593df4326754f1111f60ad63f1fde64f09): 6/3/0 rud → 6/3/0 nástrojů, společná zásoba, náhled a pokračování; TC30/40 a modelové testy, [přejímka R-0057–R-0061](functional-tests.md#r-0057r-0061-cloud-sol-batch-2026-10-16). Ostatní výrobní řetězce a profese tím nejsou přijaty. |
| UC-038 — Zaměstnat všechny druhy pracovníků<br>PROD-004, PROD-005<br>**Hra** | Každou profesi z karty PDF 133 přidělit na specializovanou i jinou práci; porovnat zločince, sluhu a svobodného kolonistu. | Obyčejná dílna s dostatkem vstupů dává 1/2/3; venkovní práci nepenalizovat odvozením z této řady. Každý expert má svůj účinek, mimo odbornost pracuje jako svobodný. Profese i práce přežijí uložení; číselné bonusy, které manuál neuvádí, řeší [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education). | R / N; řez kovářského experta přijat na [0b0cb059](https://github.com/jajir/microcol/commit/0b0cb0593df4326754f1111f60ad63f1fde64f09): 6/3/0 rud → 6/3/0 nástrojů, společná zásoba, náhled a pokračování; TC30/40 a modelové testy, [přejímka R-0057–R-0061](functional-tests.md#r-0057r-0061-cloud-sol-batch-2026-10-16). Ostatní výrobní řetězce a profese tím nejsou přijaty. |
| UC-039 — Zaměstnat domorodého konvertitu<br>PROD-007, NAT-021<br>**Hra** | Nového konvertitu poslat na pole a do výroby; druhého nechat nezařazeného, dalšího zaměstnat před vypršením lhůty. | Na poli pracuje, výrobní práci odmítne; nezaměstnaný do osmi tahů odejde ke kmeni podle rozhodnutého okamžiku kontroly. Zaměstnání včas jej zachová. Načtení neresetuje věk konverze; kolonii založit nesmí. | ? / N; pravidla [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations), [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education); — |
| UC-040 — Produkovat kříže<br>PROD-008, PROD-009<br>**Hra** | Porovnat kolonii bez kazatele, po kostelu a katedrále, pak s běžným a odborným kazatelem. | Každá kolonie automaticky dává jeden kříž za tah. Budovy i kazatelé zvyšují výstup, efekt experta je rozlišený. Zobrazený výstup souhlasí s národním přírůstkem pro imigraci; nepřičítá se dvakrát. Přesné násobky [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education). | ? / N; pravidla [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education); — |
| UC-041 — Chovat koně<br>PROD-010<br>**Hra** | Nechat ve skladu počáteční koně a jídlo, sledovat několik tahů se stájí i bez ní, nulovou zásobu a plný sklad. | Koně se množí podle přijatého pravidla, stáj urychlí chov; respektují se zdroje a kapacita. Počet, prahy a spotřeba jídla musí být stanoveny v [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education), nikoli převzaty z náhodného chování současného kódu. | ? / N; pravidla [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education); — |

## Stavba a účinky budov

Zdroj: strany 48–58 / PDF 54–64 a úplná karta PDF 134;
[budovy včetně číselné matice](gap-analysis.md#construction-upgrades-and-building-effects).

| UC a pravidla | Příprava a kroky | Očekávání a povinné varianty | Evidence I / V; TC |
| --- | --- | --- | --- |
| UC-042 — Zpřístupnit správný stavební projekt<br>BUILD-001, BUILD-016<br>**Hra** | Pro každý řádek stavební karty připravit populaci těsně pod minimem a na minimu, s předchůdcem i bez něj; vybrat projekt také modelovým vstupem. | Správná cena kladiv/nástrojů, populace a předchůdce; počáteční budovy se neplatí znovu. Zvlášť kostel 52/0/3 a pevnost Fort 120/100/4. Nestačí jen správná konstanta: provedení musí podmínky vynutit. | R / N; [R-0025: modelové hranice a TC32/01–07](functional-tests.md#construction-ui-implementation-2026-10-14) cíleně prošly pro reprezentované typy. Celá karta včetně chybějících budov a zakladatelů není přijata. [R-0084 / TC57](functional-tests.md#r-0084-founder-construction-2026-10-16) lokálně ověřil podmínku členství, skutečný nábor, dokončení a Continue; úplné CI/publikace čekají. |
| UC-043 — Dokončit projekt a změnit rozestavěný<br>BUILD-002, BUILD-003, BUILD-019<br>**Hra** | Nasbírat kladiva, změnit projekt, dokončit kladiva s chybějícím posledním nástrojem, dovézt jej a ukončit tah. | Změna zachová dosavadní kladiva; bez nástrojů se nestaví a hráč dostane vysvětlení. Po dodání se jednou spotřebují zdroje a dokončí právě aktuální projekt. Načtení nerozmnoží budovu ani nevrátí zaplacené nástroje. | ? / N; — |
| UC-044 — Zaplatit urychlenou stavbu<br>BUILD-004<br>**Hra** | Otevřít nabídku dostavby rozpracovaného projektu; zrušit, přijmout s přesnou částkou, zkusit o jedno zlato méně. | Nabídka a skutečná cena souhlasí; pouze přijatá dostupná koupě dostaví aktuální projekt jednou. Změna projektu během nabídky nesmí koupit něco jiného. Cenový vzorec vyžaduje rozhodnutí [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education). | ? / N; pravidla [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education); — |
| UC-045 — Vylepšit výrobní řetězec<br>BUILD-005, BUILD-006, BUILD-007, BUILD-008, BUILD-009, BUILD-017, BUILD-018<br>**Hra** | Se stejnými pracovníky postupně postavit pilu, kovárnu/železárnu, všechny čtyři luxusní řetězce a zbrojní řetězec. Porovnat dostatek a nedostatek vstupů. | Pila zdvojnásobí kladiva i potřebu dřeva; kožešinový dům→stanice→továrna jde správným směrem. Magazine zdvojnásobí muškety, Arsenal spotřebuje poloviční nástroje na stejný výstup. Šest továren vyžaduje Smitha. Každý upgrade má skutečný účinek a neobejde suroviny. Neudané výkony [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education). | ? / N; pravidla [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education); — [R-0084 / TC57](functional-tests.md#r-0084-founder-construction-2026-10-16) lokálně ověřil podmínku členství, skutečný nábor, dokončení a Continue; úplné CI/publikace čekají. |
| UC-046 — Lovit ryby, opravovat a stavět lodě/děla<br>BUILD-010, BUILD-011, BUILD-012<br>**Hra** | Před/po doku přiřadit rybáře na moře i jezero; s drydock/shipyard opravovat a stavět lodě; se zbrojnicí stavět dělo. | Dok zpřístupní rybolov, drydock opravy, shipyard konstrukci oprávněných lodí v pobřežní kolonii. Dok ani drydock sám stavbu lodí neodemkne. Dělo vyžaduje armory nebo vyšší stupeň. Časy opravy viz [boj a námořní následky](gap-analysis.md#combat-and-naval-outcomes). | ? / N; pravidla [boj a námořní následky](gap-analysis.md#combat-and-naval-outcomes); — |
| UC-047 — Posílit tisk a obranu kolonie<br>BUILD-013, BUILD-015<br>**Hra** | Porovnat produkci zvonů po tiskárně/novinách; z kolonie s palisádou a třemi obyvateli odebrat pracovníka, potom jen přerozdělit práci. | Tiskárna násobí existující zvony o +50 %, noviny o +100 %, netvoří náhradního producenta. Obranná stavba nedovolí dobrovolně snížit populaci pod tři; přesun mezi pracemi ano. Zaokrouhlení a nucené ztráty obyvatel ověřit podle přijatých pravidel, viz [podpora, produkce a Kongres](gap-analysis.md#support-production-and-congress)/[ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education). | ? / N; pravidla [podpora, produkce a Kongres](gap-analysis.md#support-production-and-congress), [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education); — |
| UC-048 — Automaticky vyvážet přes celnici<br>BUILD-014, TRADE-009<br>**Hra** | Se Stuyvesantem postavit celnici, vybrat druhy exportu a dodat/vyrobit zboží; jeden druh ponechat. Opakovat po načtení i za revoluce. | Před otcem stavba není dostupná. Každý tah se prodají oprávněné vybrané zásoby za správný výnos, ostatní zůstanou; uloží se výběr. Běžný přístav při revoluci zůstává uzavřen; příjem za revoluce testuje UC-081. | R / N; pravidla [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education); — [R-0084 / TC57](functional-tests.md#r-0084-founder-construction-2026-10-16) lokálně ověřil podmínku členství, skutečný nábor, dokončení a Continue; úplné CI/publikace čekají. [R-0085 / TC58](functional-tests.md#r-0085-custom-house-export-2026-10-16) implementuje výběr a export nad rezervu včetně polovičního čistého příjmu za revoluce; cílené modelové a hráčské ověření prošlo, úplné CI/publikace a propojení bojkotu z R-0102 zůstávají otevřené. |
| UC-049 — Ověřit celou číselnou konfiguraci budov<br>BUILD-016, BUILD-001<br>**Hra** | Spustit nezávislou parametrizovanou kontrolu všech řádků karty PDF 134 a praktické hraniční stavby z UC-042. | Zahrnout i nulové náklady, populace, automatické budovy, oba sklady, stáje, školy, tisk, celnici a všechny předchůdce. Report jmenuje každý řádek; několik vybraných budov ani shoda dvou stejných konstant není úplná kontrola. | R / N; [R-0025](requirements/done/R-0025.md) ověřuje 41 explicitních položek metadat, přijetí projektů a TC32. Chybějící budovy, účinky a zakladatelé zůstávají mimo tento řez; nejde o úplnou přejímku karty. |

## Vzdělávání

Zdroj: strany 33–34, 56 / PDF 39–40, 62 a karta PDF 133;
[vzdělávání a matice profesí](gap-analysis.md#education-and-skill-acquisition).

| UC a pravidla | Příprava a kroky | Očekávání a povinné varianty | Evidence I / V; TC |
| --- | --- | --- | --- |
| UC-050 — Vyučovat odpovídající odbornost<br>EDU-001, EDU-002<br>**Hra** | Do školy/koleje/univerzity dát způsobilé učitele a žáky; vyzkoušet každou profesi, přeplnění a příliš nízký stupeň školy. | Limity učitelů 1/2/3 a minimální stupeň podle každého řádku karty. Po přijaté délce získá žák odbornost bez spotřeby materiálu. Učitel nezmizí, pokrok se uloží, upgrade neztratí platné obsazení. | ? / N; pravidla [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education); — |
| UC-051 — Postoupit od zločince k odborníkovi<br>EDU-003<br>**Hra** | Učit postupně zločince, sluhu a svobodného kolonistu, kontrolovat stav po každém dokončeném úseku. | Zločinec→sluha→svobodný→odborník, bez přeskočení stupně. Identita/vlastník přetrvají; nová produktivita a skórovací třída odpovídají novému stavu. | ? / N; pravidla [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education); — |
| UC-052 — Zrušit odbornost a naučit jinou<br>EDU-004, EDU-005<br>**Hra** | Specialistovi zrušit odbornost, učit jinou; samostatný svobodný pracovník dlouhodobě vykonává stejnou práci. | Zrušení vytvoří svobodného kolonistu při zachování identity a platné práce. Nové učení funguje; zkušenost z práce se připisuje správné profesi. Délka/pravděpodobnost a změna zaměstnání mají výklad [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education). | ? / N; pravidla [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education); — |
| UC-053 — Rozlišit evropské a domorodé dovednosti<br>EDU-006, NAT-015<br>**Hra** | Sestavit všechny profese označené hvězdičkou na kartě a schválený seznam dovedností nedostupných v Evropě; zkusit oba způsoby získání. | Hvězdička znamená možnost domorodé výuky, sama neznamená zákaz evropské. Zločinec se u domorodců neučí, sluha/svobodný ano; již odborný návštěvník nepřepíše schopnost. Souhlas/odmítnutí viz UC-063. | ? / N; pravidla [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations), [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education); — |

## Imigrace a evropský obchod

Zdroj: strany 35–36, 59–66 / PDF 41–42, 65–72;
[imigrace](gap-analysis.md#immigration-and-european-hiring) a
[evropský trh](gap-analysis.md#trade-and-the-european-market).

| UC a pravidla | Příprava a kroky | Očekávání a povinné varianty | Evidence I / V; TC |
| --- | --- | --- | --- |
| UC-054 — Přilákat přistěhovalce kříži<br>IMM-001, IMM-002<br>**Hra** | Ve dvou koloniích vyrábět kříže a dosáhnout imigračního prahu; sledovat tři kandidáty a náboženský přehled. | Přírůstky tvoří jeden národní součet, po prahu přijde kandidát na molo a jeho místo v nabídce se ihned doplní. Po načtení zůstane součet i nabídka, událost se neopakuje. Více křížů má odpovídající účinek; růst prahu [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education). | ? / N; pravidla [obtížnost a národní výhody](gap-analysis.md#difficulty-and-national-effects), [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education); — |
| UC-055 — Zaplatit nábor, výcvik a nákup jednotky<br>IMM-003, IMM-004, IMM-007<br>**Hra** | Vybrat jednoho ze tří kandidátů, samostatně vyškolit odborníka a opakovaně koupit loď/dělo; zkusit nedostatek peněz a zrušení. | Nábor doplní pool, výcvik jej nevyčerpá; dorazí správný člověk/jednotka. Evropsky nedostupné profese nejsou v nabídce. Každá zaplacená transakce účtuje zobrazenou cenu jednou; poptávka zdražuje další loď/dělo a cena se uloží. | ? / N; pravidla [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education); — |
| UC-056 — Koupit a prodat přesné množství<br>TRADE-001, TRADE-002<br>**Hra** | S odlišnou nákupní/prodejní cenou koupit/prodat 1, 37, 99 a 100 kusů; opakovat s částečně obsazeným prostorem. | Evropa má dost nabídky/poptávky, ale platí kapacita a rozpočet hráče. Nákup používá ask, prodej bid; množství, náklad a peníze přesně souhlasí. Zrušený dialog nemá transakci. | ? / N; — |
| UC-057 — Vidět správnou cenu a daň<br>TRADE-003, TRADE-004, TRADE-005<br>**Hra** | Prodat při nulové i nenulové dani včetně zlomkového výsledku; nakoupit s přesně zobrazeným dostupným zlatem. | Čistý výnos v nabídce = přírůstek pokladny; nákup odpovídá kotované ask ceně a přijatému výkladu zdanění nákupu. Jeden zaokrouhlovací postup pro UI/model; částka o jedno zlato nižší nákup odmítne bez změny. | ? / N; pravidla [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education); — |
| UC-058 — Pohnout cenami a zkontrolovat účetnictví<br>TRADE-006, TRADE-007, TRADE-008<br>**Hra** | Prodávat stejnou komoditu opakovaně, potom neobchodovat; přidat prodej jiného národa. Otevřít ekonomický přehled. | Výprodej cenu snižuje, útlum ji může zvýšit podle přijatého modelu. Trhy jsou národní a ovlivňují se i cizím obchodem. Report pro druh/národ správně oddělí nakoupené/prodané množství a výdaje/příjmy; historie přežije načtení. | ? / N; pravidla [obtížnost a národní výhody](gap-analysis.md#difficulty-and-national-effects), [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education); — |
| UC-059 — Přijmout daň nebo protestovat<br>GOV-001, GOV-002, GOV-003<br>**Hra** | Od výchozí nulové daně vyvolat královské zvýšení; přijmout nebo odmítnout. Potom zkusit obchod bojkotovaným druhem a splatit dluh. | Přijetí uloží novou sazbu a použije ji v dalších obchodech bez protestních škod. Protest zničí vybrané zboží, zvýší odpor a zablokuje nákup i prodej daného druhu. Dostatečná platba obnoví právě jeho obchod, nedostatečná nic nezmění. Fugger ruší existující bojkoty podle UC-097; další protest může vytvořit nový. | ? / N; pravidla [podpora, produkce a Kongres](gap-analysis.md#support-production-and-congress), [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education); — |

## Domorodé národy

Zdroj: strany 19, 33–34, 67–77 / PDF 25, 39–40, 73–83;
[domorodé národy a vztahy](gap-analysis.md#native-nations-settlements-and-relations).

| UC a pravidla | Příprava a kroky | Očekávání a povinné varianty | Evidence I / V; TC |
| --- | --- | --- | --- |
| UC-060 — Rozpoznat národ a získat půdu<br>NAT-001, NAT-002, NAT-003<br>**Hra** | Navštívit všech osm národů, hlavní i obyčejné sídlo; založit/pracovat/upravovat pole na hranici domorodé půdy před a po odkoupení. | Arawakové/Cherokee/Irokézové mají vesnice, Tupi/Sioux/Apache tábory, Aztékové/Inkové města. Hlavní sídlo je rozlišeno, silnější a bohatší; bojovníci mají domov. Nárok sahá jedno pole kolem táborů/vesnic, dvě kolem měst. Neodkoupená půda je viditelná a její užívání zhoršuje vztahy; zakoupení se uloží. | ? / N; pravidla [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations); — |
| UC-061 — Vyvolat a uklidnit napětí<br>NAT-004, NAT-005, NAT-006, NAT-007, NAT-008<br>**Hra** | Samostatně měnit populaci, zástavbu, blízkost, zbraně, vojáky a cizí mise; zaútočit/vymáhat tribut, potom obchodovat, darovat a nechat klidné tahy. | Lokální alarm a hněv celého kmene jsou odlišné a vedené vůči každému Evropanovi. Útok na hlavní sídlo a pohřebiště má odpovídající dopad; dar uklidňuje více než obchod, hlavní sídlo zesiluje účinek. Zobrazení odděluje náladu, rychlost změny a vliv jiného národa. Domorodá AI při nepřátelství útočí a při uklidnění reaguje. | ? / N; pravidla [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations); — |
| UC-062 — Navštívit náčelníka<br>NAT-009, NAT-010<br>**Hra** | Vstoupit průzkumníkem do přátelského a nepřátelského sídla; porovnat zkušeného a obyčejného, vyvolat řízené větve. | Dostupné akce odpovídají roli. Návštěva odhalí poptávané zboží a vyučovanou dovednost; podle výsledku informaci, dar nebo smrt. Nálada/odbornost ovlivní výsledek; zrušení návštěvy není útok ani obchod. | ? / N; pravidla [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations); — |
| UC-063 — Naučit se od domorodců<br>NAT-015, EDU-006<br>**Hra** | Do přátelského sídla poslat svobodného, sluhu, zločince, vybaveného pionýra a již odborného kolonistu; nabízené učení přijmout i odmítnout. | Svobodný/sluha získá nabízenou dovednost za přijatých podmínek, zločinec není přijat a odborník nezíská další specializaci. Nabídka a výsledek souhlasí, odmítnutí nezmění schopnost. Uloží se případný průběh i výsledek. | ? / N; pravidla [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations); — |
| UC-064 — Obchodovat se sídlem<br>NAT-011, NAT-012, NAT-013, NAT-014<br>**Hra** | Před/po pozemním kontaktu přivézt plný vůz i loď. Prodat, smlouvat, odmítnout nebo darovat; pak koupit jedno ze tří nabízených zboží. | Bez předchozího pozemního představení odmítnout první obchod lodí i vozem. Prázdný či nepoptávaný náklad nestačí; poptávka odpovídá místním zdrojům. Každá dohoda zachová zboží a zlato obou stran, kapacitu a rozpočet; dar nepřidá zlato. Nepřátelské sídlo odmítá, neklidné odmítá lodě; stejný druh po sobě nekupuje kromě mušket. Lodní obchod je méně výnosný než vozový. | ? / N; pravidla [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations); — |
| UC-065 — Vyžádat tribut a dobýt sídlo<br>NAT-016, NAT-007<br>**Hra** | Vojákem, dragounem a průzkumníkem žádat zlato; těmito rolemi a dělem zaútočit. Zkusit nezpůsobilou roli a řízené výsledky. | Platí oprávnění, změna zlata a hněvu; útok spustí skutečný boj. Dobytí může dát poklad s transportem podle UC-024, s Cortesem platí UC-086. Poklad ani odstranění sídla se po načtení neopakuje. | ? / N; pravidla [boj a námořní následky](gap-analysis.md#combat-and-naval-outcomes), [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations); — |
| UC-066 — Vyslat misionáře a založit misi<br>NAT-017, NAT-018, NAT-021<br>**Hra** | Vysvětit kolonistu v Evropě a kolonii s/bez kostela, získat experta; založit misi a vyvolat konverzní událost. | Bez kostela/katedrály kolonie nevysvěcuje. Mise má vlastníka a odborný stav, snižuje místní alarm vůči sobě a může jej zvyšovat vůči rivalům. Expert je účinnější. Konverze dodržuje rozhodnutý spouštěč/pravděpodobnost, ne automaticky domyšlený každotáhový příchod. | ? / N; pravidla [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations); — |
| UC-067 — Vypudit cizí misi nebo podnítit válku<br>NAT-019, NAT-020<br>**Hra** | Napadnout pověst cizí mise a vyvolat obě rozhodnutí domorodců; samostatně objednat útok na vybraného Evropana. | Při úspěšné denunciaci zemře cizí misionář a jeho mise zanikne; při neúspěchu zemře příchozí a cizí mise zůstane. Postoje ke stranám ovlivní šanci. Cena války reaguje na postoje a počet misí. Platba jen při dohodě, domorodá AI skutečně sleduje zvolený cíl; odmítnutí nic neodebere. | ? / N; pravidla [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations); — |

## Evropští soupeři a diplomacie

Zdroj: strany 78–82 / PDF 84–88;
[evropská diplomacie](gap-analysis.md#european-diplomacy-and-rivalry).

| UC a pravidla | Příprava a kroky | Očekávání a povinné varianty | Evidence I / V; TC |
| --- | --- | --- | --- |
| UC-068 — Hrát proti fungujícímu soupeři<br>DIP-001<br>**Hra** | Ze startovní flotily nechat každého evropského soupeře rozvíjet partii s řízeným seedem; sledovat zásoby a právoplatnost akcí. | AI přistane, založí kolonii, uživí/vyrábí, přepravuje a reaguje na soupeře, dovede smysluplně bojovat. Nezískává nezdokumentované suroviny ani tahy. Před testem stanovit měřitelné cíle a limit tahů podle obtížnosti; nepožadovat shodné tahy s původní AI. | ? / N; pravidla [obtížnost a národní výhody](gap-analysis.md#difficulty-and-national-effects); [R-0027: dílčí první kolonie](functional-tests.md#rival-first-colony-preparation-2026-10-14), tříkolový referenční průchod a Continue; obecná ekonomika/expanze a celé UC zůstávají otevřené. |
| UC-069 — Vyhlásit válku, vyjednat mír a alianci<br>DIP-002, DIP-003, DIP-004, DIP-008<br>**Hra** | Z míru napadnout jednotku/kolonii/loď; zvlášť korzárem. Vyvolat sousedský kontakt a návštěvu starosty průzkumníkem; vyjednat tribut, mír a alianci. | Běžný útok mění bilaterální vztah na válku, korzár má zvláštní režim. Mír přeruší nepřátelství AI, porušení smlouvy je obnoví; aliance může cílit Evropana i domorodý národ. Vliv evropské královské války před/po Franklinovi má zaznamenaný výklad. | R / N; pravidla [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations); R-0026: TC33 (7 případů), model/AI mír–válka; [evidence](functional-tests.md#european-hostility-and-political-report-2026-10-14). Celá cesta útoku/příměří/aliance zůstává otevřená (R-0028, [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations)) |
| UC-070 — Přijmout žoldnéře<br>DIP-005, GOV-018<br>**Hra** | Vyvolat nabídku před revolucí i v ní; přijmout celý oddíl, odmítnout a zkusit nedostatek zlata. | Souhlas zaplatí celou nabídnutou skupinu veteránů a dodá ji jednou; odmítnutí nevylučuje budoucí nabídku. Manuál říká přibližně 5 000 zlata, nikoli přesný univerzální spouštěč. Prahy, ceny a pravděpodobnosti stanovit v [revoluce a intervence](gap-analysis.md#revolution-and-intervention). | ? / N; pravidla [revoluce a intervence](gap-analysis.md#revolution-and-intervention); — |
| UC-071 — Obchodovat s rivaly a porovnat mocnosti<br>DIP-006, DIP-007, GOV-012<br>**Hra** | Přivézt loď/vůz do cizí kolonie bez/s de Wittem a za míru/války; nabídnout vlastní náklad k výměně nebo prodeji rivalovi a otevřít poradce. | Obchod se zpřístupní pouze s otcem a mírem; dohoda změní obě zásoby/peníze bez dobytí. Přehledy respektují znalost světa; de Witt zpřístupní populaci, počet/průměrnou velikost kolonií, vojenskou/námořní/obchodní sílu. Ochotný intervenční spojenec nabízí výhodnější ceny a zboží podle rozhodnutých parametrů. | ? / N; pravidla [revoluce a intervence](gap-analysis.md#revolution-and-intervention), [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations); — |

## Boj a dobývání

Zdroj: strany 30–32, 79–81, 87–89 / PDF 36–38, 85–87, 93–95,
karty PDF 132 a 135;
[boj a úplná matice sil](gap-analysis.md#land-combat-naval-combat-and-capture).

| UC a pravidla | Příprava a kroky | Očekávání a povinné varianty | Evidence I / V; TC |
| --- | --- | --- | --- |
| UC-072 — Zahájit povolený útok<br>COMBAT-001, COMBAT-002, COMBAT-003<br>**Hra** | Přetáhnout vybavenou jednotku na známý sousední cíl; mírový útok potvrdit, zrušit nebo změnit stav během náhledu. Zkusit neozbrojenou roli, vzdálenost, terén a pokračování po uložení. | Podporované ozbrojené role zahájí boj; neozbrojený běžný kolonista, vůz a galeona neútočí. Oba účastníci vyčerpají pohyb a přeživší jej obnoví při příštím běžném tahu svého vlastníka. Mírový útok vyžaduje potvrzení i při skrytém rádci; starý návrh nesmí pronásledovat cíl ani změnit jinou hru. | R / N; [R-0028](requirements/done/R-0028.md), [evidence TC35/01–09](functional-tests.md#attack-initiation-integration-2026-10-14). Cíleně prošlo 90 modelových/AI a 19 hráčských/kontrolních případů; úplná regrese/publikace zbývají. Celý katalog rolí včetně pokladu a ostříleného zvěda zatím není implementovaný. |
| UC-073 — Spočítat skutečnou bojovou sílu<br>COMBAT-004, COMBAT-005, COMBAT-006, COMBAT-007, COMBAT-009, COMBAT-010<br>**Hra** | Pro každou pozemní, domorodou a námořní roli z PDF 132 spočítat základ; postupně přidat terén, útok, opevnění, přepad a bombardování. | Základní útok má +50 %; opevnění +50 %, palisáda/fort/fortress +100/+150/+200 %. Platí celá terénní matice a výjimka proti domorodcům; jejich přepad a rebelský přepad proti královským mimo kolonii. Bombardování +50 % jen pro oprávněné jednotky/cíl. Síly veteránů 3 a 4,5 již obsahují +50 %, nesčítat podruhé. Výsledný výpočet používá i resolver, nejen dialog. | ? / N; pravidla [boj a námořní následky](gap-analysis.md#combat-and-naval-outcomes); — |
| UC-074 — Povýšit vojáka a poškodit dělo<br>COMBAT-008, COMBAT-011<br>**Hra** | Vyhrát/prohrát běžným vojákem, získat veterána také výukou/nákupem; prohrát plným dělem a bránit proti domorodému nájezdu. | Řízená výhra může dát veteránství, síla a uložený stav souhlasí. Dělo 7/5 se po porážce změní na poškozené 5/3. Obrana opevněné kolonie proti nájezdu má +75 % pro dělo; otevřený terén má rozhodnutý postih. | ? / N; pravidla [boj a námořní následky](gap-analysis.md#combat-and-naval-outcomes); — |
| UC-075 — Dobýt celou kolonii<br>COMBAT-012, COMBAT-013<br>**Hra** | Napadnout kolonii s armádou, pak jen s civilisty; obsadit i kolonii s osmi pracujícími poli a loděmi. | Vojenská obrana musí padnout, bez ní stále proběhne civilní obrana. Až úspěšný vstup změní kolonii i všechny obyvatele na vítěze, práce zůstanou platné. Žádný pracovník neustrne u starého vlastníka; porážka převzetí neprovede. Další tah a načtení zachovají konzistenci. | ? / N; pravidla [boj a námořní následky](gap-analysis.md#combat-and-naval-outcomes); — |
| UC-076 — Vybojovat námořní střet a blokádu<br>COMBAT-014, COMBAT-015<br>**Hra** | Bojovat loděmi s nákladem, řízeně vyvolat potopení, poškození a ukořistění; proplouvat kolem hlídky a přístavní blokády. | Potopená loď a náklad mají přijatý osud, poškozená míří do nejbližší vhodné opravny včetně Evropy. Ukořistění respektuje kapacitu; oprava vede k návratu do služby. Hlídka může neodhalit, zpomalit či zastavit, nevytvoří trvalý neplatný stav. | ? / N; pravidla [boj a námořní následky](gap-analysis.md#combat-and-naval-outcomes); — |
| UC-077 — Bránit pobřeží pevností<br>COMBAT-016<br>**Hra** | Proplout kolem palisády, fortu a fortress, s dělostřelectvem i bez, potom mimo sousedství. | Sousední moře ostřelují jen oprávněné pevnosti; vyšší stupeň a děla zvyšují sílu. Palisáda nebo nesousední pole nepřebírá tento účinek. Zohlední se vztahy, výsledek a pokračování plavby podle přijatých pravidel (viz [boj a námořní následky](gap-analysis.md#combat-and-naval-outcomes)). | ? / N; pravidla [boj a námořní následky](gap-analysis.md#combat-and-naval-outcomes); — |

## Politika a válka za nezávislost

Zdroj: strany 83–89 / PDF 89–95;
[vláda a revoluce](gap-analysis.md#government-taxation-and-independence).

| UC a pravidla | Příprava a kroky | Očekávání a povinné varianty | Evidence I / V; TC |
| --- | --- | --- | --- |
| UC-078 — Zvýšit podporu a změnit produktivitu<br>GOV-004, GOV-005, GOV-006<br>**Hra** | Vyrábět zvony, přidávat/přesouvat obyvatele; připravit 49/50/99/100 % místní podpory a Tory počty kolem prahu obtížnosti. | Místní a národní podpora se počítají správně a zobrazí se odděleně. Při 50 % produkce +1, při 100 % další +1. Tory postih −1 začne při dosažení 10 na Discoverer a 6 na Viceroy; mezilehlé úrovně, kombinace a zaokrouhlení ověřit podle přijatých pravidel, viz [obtížnost a národní výhody](gap-analysis.md#difficulty-and-national-effects)/[podpora, produkce a Kongres](gap-analysis.md#support-production-and-congress). Přesná barva ukazatele při 50 % je prezentace [obnova a prezentace](gap-analysis.md#recovery-and-presentation); číselná hodnota musí být správná. | ? / N; pravidla [obtížnost a národní výhody](gap-analysis.md#difficulty-and-national-effects), [podpora, produkce a Kongres](gap-analysis.md#support-production-and-congress); — |
| UC-079 — Získat otce zakladatele<br>GOV-007, SUPPORT-002<br>**Hra** | Po první kolonii vyrábět zvony, projít postupem výběru/příchodu otců všech pěti kategorií a otevřít Kongres. | Členství, aktuální postup a účinky přetrvají. Jednorázová odměna se nevydá znovu po načtení; cizí národ ji nedostane. Přesný výběr kandidátů a prahy jsou [podpora, produkce a Kongres](gap-analysis.md#support-production-and-congress); všech 25 účinků má samostatné UC-086–110. | R / N; [R-0104 / TC56](functional-tests.md#r-0104-congress-recruitment-2026-10-16) lokálně ověřil výběr, produkci, Jonesův příchod a Continue; ostatní účinky zůstávají samostatné. Pravidla [podpora, produkce a Kongres](gap-analysis.md#support-production-and-congress); — |
| UC-080 — Vyhlásit nezávislost a mobilizovat<br>GOV-008, GOV-009<br>**Hra** | S národní podporou 49/50 %, před rokem 1800, vyhlásit nezávislost; vyzkoušet i rok před 1700 a méně než tři kolonie. Připravit různé místní podpory. | Pod 50 % zamítnuto, od 50 % dovoleno; potvrzený čin je nevratný a neopakuje se. Veteráni se mobilizují podle místní podpory, pod 50 % žádní; přesné počty odpovídají přijatým pravidlům mobilizace. Hranici roku 1800 svázat s ukončením partie, ne obejít jiným UI. | ? / N; pravidla [podpora, produkce a Kongres](gap-analysis.md#support-production-and-congress); — |
| UC-081 — Pokračovat v hospodářství bez domovského přístavu<br>GOV-011, GOV-012, TRADE-009<br>**Hra** | Po vyhlášení zkusit Evropu, nábor, nákup i plavbu již rozjeté lodi; provozovat celnici a zahraniční obchod. | Domovský přístav a běžné evropské příkazy jsou uzavřené i v modelu. Celnice dál prodává za 50 % normálního příjmu, de Wittův obchod zůstává pod svými podmínkami. Stav lodi na Atlantiku, daňové pořadí a zaokrouhlení mají rozhodnutí; nelze obejít zákaz načtením. | R / N; pravidla [účinky zakladatelů](gap-analysis.md#founder-details), [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education), [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport); — [R-0085 / TC58](functional-tests.md#r-0085-custom-house-export-2026-10-16) implementuje výběr a export nad rezervu včetně polovičního čistého příjmu za revoluce; cílené modelové a hráčské ověření prošlo, úplné CI/publikace a propojení bojkotu z R-0102 zůstávají otevřené. |
| UC-082 — Čelit královské armádě<br>GOV-014, GOV-015, COMBAT-007, COMBAT-009, SUPPORT-002<br>**Hra** | Před revolucí sledovat růst REF, po ní nechat přepravit pěchotu, jízdu, děla a válečné lodě a zaútočit na kolonii i v krajině. | Kongres ukazuje skutečné síly včetně rezerv. Král používá přijaté složení/růst a legální přepravu; bombardování +50 % v útoku na kolonii, v terénu možnost rebelského přepadu. Zničené síly se svévolně neobnoví po načtení. | ? / N; pravidla [revoluce a intervence](gap-analysis.md#revolution-and-intervention), [boj a námořní následky](gap-analysis.md#combat-and-naval-outcomes); — |
| UC-083 — Získat zahraniční intervenci<br>GOV-016, GOV-017<br>**Hra** | Po deklaraci ukončit tah, zjistit cílové zvony, vyrobit těsně pod/na/nad cílem a přijmout spojeneckou flotilu. | Cíl je oznámen jednou následující tah. Dosažení přivede lodě man-o-war a vojsko pod hráčovu kontrolu do vlastního přístavu; bombardování +50 % proti Tory koloniím. Událost i stav se uloží, dodávka se neduplikuje; cíl, složení a případ bez přístavu odpovídají přijatým pravidlům intervence. | ? / N; pravidla [revoluce a intervence](gap-analysis.md#revolution-and-intervention), [boj a námořní následky](gap-analysis.md#combat-and-naval-outcomes); — |
| UC-084 — Změnit politiku okupované kolonie<br>GOV-010, GOV-013<br>**Hra** | Za revoluce bojovat o kolonii s 60% rebely, nechat ji obsadit králem, produkovat zvony a znovu dobýt. | Rebelské straně při útoku odpovídá +60 %, královské +40 %; nejde o trvalý bonus všude na mapě. Zvony pod korunou posilují Tory, po osvobození znovu správnou stranu. Stav populace a podpory se neztratí. | ? / N; pravidla [podpora, produkce a Kongres](gap-analysis.md#support-production-and-congress), [boj a námořní následky](gap-analysis.md#combat-and-naval-outcomes); — |
| UC-085 — Vyhodnotit vítězství a všechny porážky revoluce<br>GOV-019, GOV-020, GOV-021, GOV-022<br>**Hra** | Nezávisle připravit vlastnictví všech kolonií, posledního pobřežního města a 89/90/91 % obsazené populace; měnit zbytek REF v Evropě i Americe. | Výhra vyžaduje vlastní kontrolu všech kolonií a společný zbytek REF pod přijatou mezí „velmi málo“, ne nutně nulovou evropskou rezervu. Prohra nastane při všech králem držených koloniích, všech pobřežních nebo nejméně 90 % populace. Jmenovatel a souběh výsledků musí být rozhodnuté; konec nastane jednou. | ? / N; pravidla [revoluce a intervence](gap-analysis.md#revolution-and-intervention); — |

## Všech 25 otců zakladatelů

Zdroj: strany 90–94 / PDF 96–100;
[účinky otců](gap-analysis.md#founding-father-effects).
Pro **každý** řádek připravit stav bez otce, získat jej cestou UC-079,
porovnat správného i cizího vlastníka, uložit/načíst a zkontrolovat další tah.
Jednorázový účinek se nesmí opakovat. Trvalý účinek ověřit na existujících
i později získaných příslušných jednotkách/koloniích, pokud pravidlo neříká jinak.

| UC a pravidlo | Akce / změněná situace | Očekávání a povinné varianty | Evidence I / V; TC |
| --- | --- | --- | --- |
| UC-086 — Hernan Cortes<br>FATHER-001<br>**Hra** | Dobýt běžné i hlavní domorodé sídlo. | Poklad vznikne vždy a je větší než bez otce při jinak stejných podmínkách; přesné zvýšení [účinky zakladatelů](gap-analysis.md#founder-details). | ? / N; pravidla [účinky zakladatelů](gap-analysis.md#founder-details); — |
| UC-087 — Francis Drake<br>FATHER-002<br>**Hra** | Zaútočit a bránit se korzárem, kontrolně jinou lodí. | Korzáři mají +50 % bojové síly v obou rolích; jiná loď tento bonus nemá. | ? / N; pravidla [boj a námořní následky](gap-analysis.md#combat-and-naval-outcomes); — |
| UC-088 — John Paul Jones<br>FATHER-003<br>**Hra** | Přijmout otce a opakovaně načíst. | Hráč získá právě jednu bezplatnou fregatu se správnými parametry; pokladna neklesne. | R / N; [R-0104 / TC56](functional-tests.md#r-0104-congress-recruitment-2026-10-16) lokálně ověřil skutečnou fregatu, pokladnu a Continue; úplné CI a publikace čekají. |
| UC-089 — Paul Revere<br>FATHER-004<br>**Hra** | Napadnout kolonii s/bez stálé obrany a s/bez dostatku mušket. | Nechráněná kolonie automaticky vyzbrojí pracovníka z vlastního skladu, pouze jsou-li zbraně. Spotřebuje správné vybavení; prohra stále umožní dobytí. | ? / N; pravidla [boj a námořní následky](gap-analysis.md#combat-and-naval-outcomes); — |
| UC-090 — George Washington<br>FATHER-005<br>**Hra** | Vyhrát/prohrát neveteránským vojákem i dragounem ze zločince/sluhy/svobodného/odborníka, zvlášť veteránem. | Každý způsobilý vítěz postoupí ve statusu; konkrétní cílové stupně jsou [účinky zakladatelů](gap-analysis.md#founder-details). Prohra ani již veterán tímto účinkem další stupeň nezíská. | ? / N; pravidla [účinky zakladatelů](gap-analysis.md#founder-details); — |
| UC-091 — Simon Bolivar<br>FATHER-006<br>**Hra** | Porovnat všechny kolonie s nízkou/vysokou podporou a později založenou kolonii. | Uplatní se zvýšení o 20 podle rozhodnutí procenta/procentní body a rozsahu, nikdy nad maximum. Přesný výklad a nové kolonie [účinky zakladatelů](gap-analysis.md#founder-details). | ? / N; pravidla [účinky zakladatelů](gap-analysis.md#founder-details); — |
| UC-092 — Benjamin Franklin<br>FATHER-007<br>**Hra** | Vyvolat evropskou válku a jednat s každým rivalem. | Královská válka nevynutí koloniální vztah, vyjednávání je levnější a soupeři nabídnou mír za cenu; nejde o automatický bezplatný mír. | ? / N; pravidla [účinky zakladatelů](gap-analysis.md#founder-details), [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations); — |
| UC-093 — Thomas Jefferson<br>FATHER-008<br>**Hra** | Zaměstnat obyčejného i odborného státníka s/bez tisku. | Státníci produkují o 50 % více zvonů; kombinace a zaokrouhlení odpovídají rozhodnutí. | ? / N; pravidla [účinky zakladatelů](gap-analysis.md#founder-details); — |
| UC-094 — Thomas Paine<br>FATHER-009<br>**Hra** | Po získání otce změnit daňovou sazbu. | Procentní bonus zvonů všech kolonií sleduje aktuální sazbu daně, ne sazbu při příchodu otce. | ? / N; pravidla [účinky zakladatelů](gap-analysis.md#founder-details); — |
| UC-095 — Pocahontas<br>FATHER-010<br>**Hra** | Získat při vysokém napětí a zopakovat stejnou provokaci. | Napětí se jednorázově uklidní na content; následný přírůstek místního alarmu je poloviční. Neodvozovat automaticky půlení každého přímého hněvu kmene. | ? / N; pravidla [účinky zakladatelů](gap-analysis.md#founder-details), [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations); — |
| UC-096 — Adam Smith<br>FATHER-011<br>**Hra** | Vybrat každou ze šesti továren před/po příchodu. | Odemkne příslušné projekty, zachová předchůdce/populaci/zdroje a žádnou továrnu sám nepostaví. | R / N; [R-0084 / TC57](functional-tests.md#r-0084-founder-construction-2026-10-16) lokálně ověřil podmínku členství, skutečný nábor, dokončení a Continue; úplné CI/publikace čekají. |
| UC-097 — Jakob Fugger<br>FATHER-012<br>**Hra** | Získat při více bojkotovaných druzích a později znovu protestovat. | Existující bojkoty/dluhy zmizí bez platby; pozdější nový bojkot může vzniknout. | ? / N; — |
| UC-098 — Peter Minuit<br>FATHER-013<br>**Hra** | Založit/pracovat na dříve placené domorodé půdě. | Platba za půdu není nutná, peníze zůstanou. Neznamená to obecnou imunitu proti domorodému hněvu z jiných příčin. | ? / N; — |
| UC-099 — Peter Stuyvesant<br>FATHER-014<br>**Hra** | Vybrat stavbu celnice před/po příchodu. | Odemkne celnici, ostatní podmínky a zaplacení stavby trvají; export má vlastní kontrolu UC-048. | R / N; [R-0084 / TC57](functional-tests.md#r-0084-founder-construction-2026-10-16) lokálně ověřil podmínku členství, skutečný nábor, dokončení a Continue; úplné CI/publikace čekají. |
| UC-100 — Jan de Witt<br>FATHER-015<br>**Hra** | Otevřít zahraniční přehled a obchodovat s mírovou/válečnou kolonií. | Zpřístupní podrobnější srovnání i obchod jako dva samostatné účinky. Válečný stav obchod nadále zakazuje. | ? / N; pravidla [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations); — |
| UC-101 — Jean de Brebeuf<br>FATHER-016<br>**Hra** | Použít starého i nového neodborného misionáře pro misi, denunciaci a podnícení války. | Všechny příslušné schopnosti mají odbornou účinnost; noví misionáři nezůstanou bez efektu. Parametry a kombinace [účinky zakladatelů](gap-analysis.md#founder-details)/[domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations). | ? / N; pravidla [účinky zakladatelů](gap-analysis.md#founder-details), [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations); — |
| UC-102 — William Brewster<br>FATHER-017<br>**Hra** | Dosáhnout náboženské imigrace a opakovaně doplňovat kandidáty. | Hráč zvolí ze tří; budoucí imigrace nepřivádí zločince ani sluhy. Dříve přistálí lidé se tím nemažou. | ? / N; pravidla [účinky zakladatelů](gap-analysis.md#founder-details); — |
| UC-103 — Bartolome de Las Casas<br>FATHER-018<br>**Hra** | Získat otce s existujícími konvertity a potom získat nového konvertitu. | Právě existující se jednorázově stanou svobodnými kolonisty. Pozdější konvertita se tímto minulým účinkem automaticky nepřemění. | ? / N; — |
| UC-104 — William Penn<br>FATHER-019<br>**Hra** | Vyrábět kříže v různě vybavených koloniích. | Celková produkce křížů vzroste o 50 % ve všech koloniích; vyřešit kombinace a zaokrouhlení. | ? / N; pravidla [účinky zakladatelů](gap-analysis.md#founder-details); — |
| UC-105 — Juan de Sepulveda<br>FATHER-020<br>**Hra** | Opakovat příslušnou konverzní událost s kontrolovanou náhodou. | Pravděpodobnost příchodu konvertity se zvýší právě u oprávněného spouštěče; spouštěč a faktor odpovídají přijatým pravidlům konverze. | ? / N; pravidla [účinky zakladatelů](gap-analysis.md#founder-details), [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations); — |
| UC-106 — Francisco de Coronado<br>FATHER-021<br>**Hra** | Získat otce při skrytých existujících koloniích, potom založit další cizí kolonii. | Jednou odhalí tehdejší kolonie a jejich bezprostřední okolí; nezavede trvalé odhalování budoucích kolonií ani bez podkladu všech domorodých sídel. | ? / N; — |
| UC-107 — Henry Hudson<br>FATHER-022<br>**Hra** | Porovnat obyčejného/odborného lovce kožešin a výrobce kabátů. | Výnos lovců kožešin se zdvojnásobí; nejde o samostatné zdvojnásobení výroby kabátů. Kombinace ostatních bonusů [účinky zakladatelů](gap-analysis.md#founder-details). | ? / N; pravidla [účinky zakladatelů](gap-analysis.md#founder-details); — |
| UC-108 — Sieur de La Salle<br>FATHER-023<br>**Hra** | Založit kolonii a zvýšit populaci 2→3; prověřit starší kolonii a lepší opevnění. | Nová kolonie dostane při třech obyvatelích palisádu jednou; lepší obrana se nesníží. Palisádu získají podle přijatých pravidel i způsobilé kolonie existující před příchodem otce. | ? / N; pravidla [účinky zakladatelů](gap-analysis.md#founder-details); — |
| UC-109 — Ferdinand Magellan<br>FATHER-024<br>**Hra** | Pohybovat se každým typem lodi a plout do Evropy z obou okrajů mapy. | Všechny vlastní lodě získají +1 pohyb; západní cesta do Evropy je výrazně kratší podle přijatého výkladu. Dva různé účinky se ověří samostatně. | ? / N; pravidla [účinky zakladatelů](gap-analysis.md#founder-details), [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport); — |
| UC-110 — Hernando de Soto<br>FATHER-025<br>**Hra** | Projít všemi typy jednotek střed a okraj mapy. | Dosah vidění je dvě pole; jednotka, která už viděla dvě, nedostane automaticky třetí. Odhalí se správná oblast bez překročení mapy. | ? / N; — |

## Konec hry, uložení a informace

Zdroj: strany 11–12, 16–22, 34–35, 41–42, 65, 68, 79–80, 85–89 /
PDF 17–18, 22–28, 40–41, 47–48, 71, 74, 85–86, 91–95;
[konec a skóre](gap-analysis.md#end-conditions-and-scoring),
[persistence a informace](gap-analysis.md#supporting-persistence-and-information).

| UC a pravidla | Příprava a kroky | Očekávání a povinné varianty | Evidence I / V; TC |
| --- | --- | --- | --- |
| UC-111 — Ukončit nebo opustit partii<br>END-001, END-002, END-003, END-004<br>**Hra** | Vybrat Exit/Retire a obě volby jednou zrušit; překročit 1800 s/bez války, 1850 ve válce; ztratit poslední kolonii před/od 1600. | Exit nepočítá skóre, Retire ano, zrušení zachová partii. Bez revoluce rok 1800 uzavře skóre, další hraní jej nemění. Platí revoluční limit a ztráta poslední kolonie od 1600. Limit 1850 a pokračování po 1800 ověřit podle přijatých pravidel, viz [konec hry a skóre](gap-analysis.md#end-dates-and-scoring). Konec ani zápis výsledku se neopakuje. | ? / N; pravidla [konec hry a skóre](gap-analysis.md#end-dates-and-scoring); — |
| UC-112 — Spočítat skóre a zapsat Hall of Fame<br>SCORE-001, SCORE-002, SCORE-003, SCORE-004, SCORE-005, SCORE-006, SCORE-007, SCORE-008, SCORE-009<br>**Hra** | Ukončit známou pozici se všemi bodovanými složkami; nezávisle vyzkoušet pořadí nezávislosti, data kolem 1780 a obtížnosti. Odehrát více výsledků a restartovat aplikaci. | Populace: zločinec/sluha 1, svobodný 2, odborník 4; otec 5; každých celých 1 000 zlata 1; procento podpory 1; zničené sídlo −(obtížnost+1). Bonusy 100/50/25 %, zvon po intervenci 1, časný bonus a obtížnost podle přijatých pravidel (viz [konec hry a skóre](gap-analysis.md#end-dates-and-scoring)). Konvertity, čtvrté místo a pořadí bonusů vyhodnotit podle stejných přijatých pravidel. Přehled ukáže rozpad, Hall of Fame se zobrazí i bez kvalifikace a uchová správné pořadí po restartu. | ? / N; pravidla [konec hry a skóre](gap-analysis.md#end-dates-and-scoring); — |
| UC-113 — Uložit a obnovit rozpracovanou partii<br>SUPPORT-001<br>**Hra** | Ručně uložit, odejít a pokračovat; zapnout/vypnout autosave a překročit tah i desetiletí. Opakovat během cesty, práce, učení, náboru, trasy a revoluce. | Obnoví se svět, aktuální hráč/fáze, zásoby, vztahy, události, jednorázové efekty a průběžný pokrok. Automatická obnova nabízí poslední tah a desetiletí, nikoli jen dvě libovolné generace. Počet ručních slotů, pořadí a rozvržení nabídky patří do prezentace [obnova a prezentace](gap-analysis.md#recovery-and-presentation) a neblokují ověření obnoveného stavu. Chyba čtení/zápisu nesmí potichu přepsat platnou pozici. | ? / N; dílčí R-0067 bezpečné odmítnutí načtení a R-0068 oddělené body obnovy, autosave nastavení a zachování hry při chybě zápisu: [průběžná evidence](functional-tests.md#r-0068-recovery-checkpoints-2026-10-16). R-0072 navíc ověřuje pokračování obrácené plavby se zachováním nákladu: [evidence](functional-tests.md#r-0072-voyage-reversal-2026-10-16). Úplné scénáře učení, tras a revoluce zůstávají neověřené. |
| UC-114 — Zjistit stav a dostupné příkazy<br>SUPPORT-002, SUPPORT-003, SUPPORT-004<br>**Hra** | Prohlédnout mapu, vybranou jednotku, vlastní kolonii včetně budov a jejich detailů, Evropu, poradce a nápovědu před/po změně stavu. | Datum, zlato, pohyb, vlastník, příkaz, terén, zásoby, výroba, stav budov a dostupné akce souhlasí s modelem. Prohlížení/přiblížení/přesun pohledu nemění svět. Nápověda odpovídá pravidlům. Cizí viditelná loď ukáže obsazené prostory bez odhalení přesného skrytého nákladu. Přehledy zpřístupní všechna pole níže a respektují znalost světa. Automatická okna po příjezdu, způsob hlášení, rozložení a přepínače jsou prezentace [obnova a prezentace](gap-analysis.md#recovery-and-presentation); funkční přístup k důležitým výsledkům a podkladům pro rozhodnutí je povinný. | ? / N; dílčí popis mapy po návratu z tlačítka ověřuje [TC21/08 a oprava R-0024](functional-tests.md#wagon-unloading-implementation-2026-10-13), cílená regrese prošla. [R-0112 / TC21/09](functional-tests.md#r-0112-map-input-repair-2026-10-15) cíleně ověřuje, že opožděná zpráva o tahu AI nezablokuje mapu aktuálního člověka; úplná lokální/CI regrese i deset opakování TC27/TC30 v každém prostředí prošly. Nejde o přijetí všech přehledů. |

## Souvislé průchody běžnou hrou

Tyto UC prověřují propojení systémů a jsou součástí přejímky. Neopisují nová
pravidla: odkazují na již uvedená UC a relevantní průřezová pravidla.
Pro celý průchod určit výchozí seed, data mezikontrol a očekávané výsledky
před spuštěním. U dlouhé partie lze kombinovat uložené kontrolní body s jedním
nepřerušeným průchodem; pouhé přepnutí závěrečného příznaku není dohrání.

| UC a pravidla | Příprava a kroky | Očekávání a povinné varianty | Evidence I / V; TC |
| --- | --- | --- | --- |
| UC-115 — Expedice se uživí a začne obchodovat<br>SETUP-001, SETUP-002, SETUP-003, TURN-002, SUPPORT-001<br>**Hra** | Nový profil → Nová hra s určeným referenčním profilem → průzkum → vylodění → kolonie → práce/jídlo → výrobek → přeprava → prodej → nábor → návrat. | Kroky UC-003–058 jsou propojené v běžné hře bez kampaně a ručního doplňování surovin. Kontrolovat bilance, vlastnictví, čas a zprávy, uložit a pokračovat. Jedna úplná cesta pro každý národ; kromě vstupu přes Novou hru lze ostatní profily načíst z připravených počátečních pozic. Herní profily 4 × 5 a jejich účinky ověří UC-004 a související pravidla. Vstup přes všechny ovládací volby a mapové generátory se přejímá samostatně v UC-001/002. | ? / N; pravidla [obtížnost a národní výhody](gap-analysis.md#difficulty-and-national-effects), [kalendář a počáteční výpravy](gap-analysis.md#calendar-and-starting-expeditions), [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education), [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport); — |
| UC-116 — Dvě kolonie vytvoří výrobní síť<br>ROUTE-003, PROD-002, BUILD-002, EDU-001, SUPPORT-001<br>**Hra** | Z prvního města zásobovat druhé vozem/lodí, vzdělat pracovníka, vylepšit výrobu a automatizovat obchod. Vyvolat nedostatek a obnovit dodávku. | UC-029–055 fungují společně: zdroje nevznikají zdarma, sklad/práce/budovy/škola se neobejdou; nedostatek pozdrží závislý děj a další dodávka jej obnoví. Dva okruhy před/po načtení dají dohledatelnou bilanci. | ? / N; pravidla [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education), [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport); — |
| UC-117 — Diplomacie má důsledky v ekonomice a válce<br>NAT-008, DIP-003, DIP-006, COMBAT-013, SUPPORT-001<br>**Hra** | Navázat kontakt, koupit půdu, učit se a obchodovat; vyvolat konflikt, dobýt kolonii, vyjednat mír a obnovit povolený obchod. | UC-060–077 propojují vztahy s AI, zásobami a vlastnictvím. Ztracená kolonie nezůstane v původní trase/výběru jako vlastní; mír skutečně ovlivní chování. Načtení nezruší smlouvu ani nepřepíše vlastníky. | ? / N; pravidla [boj a námořní následky](gap-analysis.md#combat-and-naval-outcomes), [domorodé a evropské vztahy](gap-analysis.md#native-and-european-relations), [průzkum, práce a přeprava](gap-analysis.md#exploration-work-and-transport); — |
| UC-118 — Získat nezávislost a konečné skóre<br>GOV-008, GOV-019, SCORE-009, SUPPORT-001<br>**Hra** | Běžnou partií dosáhnout podpory, vyhlásit nezávislost, přijmout intervenci, porazit REF a otevřít výsledky; pokračovat z uloženého mezibodu. | UC-078–114 tvoří dosažitelný řetězec bez úprav modelu testem za běhu. Evropa se uzavře, náhradní obchod funguje, intervence i vítězství nastanou jednou, konečný výsledek odpovídá odehrané historii. Krátké řízené scénáře zvlášť pokryjí všechny prohry a časové konce. | ? / N; pravidla [podpora, produkce a Kongres](gap-analysis.md#support-production-and-congress), [revoluce a intervence](gap-analysis.md#revolution-and-intervention), [konec hry a skóre](gap-analysis.md#end-dates-and-scoring), [boj a námořní následky](gap-analysis.md#combat-and-naval-outcomes); — |
| UC-119 — Zachovat pravidla při změně vzhledu<br>TURN-002, SUPPORT-001<br>**Regrese** | Po každé změně ovládání/grafiky provést stejné dotčené TC na stejných fixtures; při sdíleném ovládání celou funkční sadu. Zahrnout změny jazyka/nastavení během správy. | Stejné kroky mají stejné herní výsledky, zásoby a tahy. Bez zvuku jsou výsledky srozumitelné. Změna nastavení neporuší běžící hru ani načtení; selektory se opravují v page objects, aserce se neoslabují. Výtvarnou kvalitu hodnotit samostatně. | R / N; [Z01 regrese na c2e24ba4](functional-tests.md#z01-bezpecne-vylozeni-nakladu): 44/44 UI včetně TC08/TC09. Změny jazyka a všechny varianty Nastavení tím nejsou převzaty. |

## Povinné matice a obsah přehledů

Jedno zaškrtnuté UC se vztahuje na všechny jeho varianty. Následující přehled
brání tomu, aby například jedna otestovaná loď zastoupila celý vozový park.
Číselná očekávání čerpat z uvedených referenčních matic a zaznamenaných
rozhodnutí, nikoli z hodnot načtených z testované aplikace.

| Matice | Požadovaný rozsah / vazba |
| --- | --- |
| Herní profily | 4 národy × 5 obtížností v referenčních pozicích: příslušnost, výprava, parametry a save/load, UC-003/004/115. Každý popsaný účinek obtížnosti také u příslušného pravidla, zejména UC-078/112. |
| Nastavení nové partie | Samostatná přejímka UC-001/002: 4 × 5 startů přes výběrové ovládání, náhodná, americká a upravená mapa. Není podmínkou přijetí Hry; základní spuštění běžné partie ověřuje UC-115. |
| Terén | Každý řádek a lesní/holá varianta, všechny produkční buňky, pohyb i obrana karty PDF 135; zvlášť řeky, zdroje a úpravy, UC-007–009/012/021/073. |
| Jednotky | Všechny bojové role, šest lodí, příslušné pohyby/prostory/síly/oprávnění z PDF 132; poškozené dělo a role během revoluce, UC-022/024/072–077/082–083. |
| Profese | Všech 22 dovedností z PDF 133: účinek, práce mimo specializaci, stupeň školy, domorodá výuka, dostupnost v Evropě; UC-038/050–055 a role průzkumníka/pionýra/misionáře/vojáka v jejich UC. |
| Budovy | Každý řádek PDF 134: kladiva, nástroje, populace, předchůdce, zakladatel, skutečný účinek; UC-031/042–050. Populace novin je 4 podle přijaté karty; [ekonomika, produkce a výuka](gap-analysis.md#economy-production-and-education) zachovává rozhodnutí R-0025. |
| Zakladatelé | Všech 25 účinků UC-086–110; před/po získání, vlastní/cizí hráč, uložení, jednorázovost a kombinace. |
| Výroba a obchod | Sedm řetězců, 0–3 výrobní pracovníci, nedostatek/přesná zásoba/přebytek; porod 199/200/201, hlad kolem nuly, kapacity 100/200/300; bid/ask, daň, částečné množství a zrušení. |
| Ukončení | Každá samostatná výhra/prohra, 1600/1780/1800/1850 a obě strany hranic; všechny složky skóre, pořadí a obtížnost podle rozhodnutí; UC-085/111–112. |

UC-114 navíc kontroluje tyto údaje, jejich soulad s modelem a oprávnění k zobrazení:

| Přehled | Povinné informace |
| --- | --- |
| Práce | Počty všech typů kolonistů a po výběru jejich umístění. |
| Náboženství | Produkce a nasbírané kříže, postup k dalšímu přistěhovalci. |
| Kolonie | Populace, produkce/spotřeba a zásoby, rozpracovaná stavba, očekávaný přírůstek či nedostatek. |
| Ekonomika | Nakoupená/prodaná množství a výdaje/příjmy podle komodity a národa; neslévat nákup s prodejem. |
| Domorodci | Známé národy, jejich vztahy a dostupné informace o osadách; neodhalovat neobjevené údaje. |
| Zahraniční vztahy | Válka/mír a příslušné vztahy; s de Wittem navíc srovnání populace, kolonií a vojenské, námořní a obchodní síly. |
| Námořnictvo | Vlastní lodě, náklad, poloha a cíl cesty; u cizí lodě pouze dovolená informace o obsazenosti. |
| Kongres | Národní podpora, přítomní otcové, postup dalšího získání a skutečná REF včetně rezerv. |
| Zprávy a konec | Hlad/narození, chybějící vstupy a nástroje, dokončení projektu, připlutí, diplomacie a revoluční události; konečný výsledek a skóre. Přesné přepínače automatických oken řeší [obnova a prezentace](gap-analysis.md#recovery-and-presentation). |

## Vazba na současné testy

Při založení katalogu nebyly přepsány ani přečíslovány testy. Existující
[TC18](../microcol-test/src/test/java/org/microcol/test/TC_18_F01_plow_field_test.java)
a [TC15](../microcol-test/src/test/java/org/microcol/test/TC_15_F05_plow_field_test.java)
jsou **část UC-019**: ověřují zahájení orby a pohyb/zobrazení, nikoli spotřebu
nástrojů, konec práce a všechny zákazy. Při implementaci manuálu je nutné
upravit i fixture a doplnit očekávání; ponechaný zelený test se starým pravidlem
nemůže potvrdit nové UC.

Podobně [CargoSlotTest](../microcol-game/src/test/java/org/microcol/model/CargoSlotTest.java)
pomáhá UC-023/024, ale sám neprovede přepravu přes obrazovky celé hry.
U ostatních UC se konkrétní TC doplní po přečtení jejich skutečných asercí.
Prázdná vazba není důkaz neexistence implementace, nýbrž dosud nepřiřazeného
úplného přejímacího důkazu.

Doplnění z 13. října 2026: [audit existujících cest volné hry](functional-tests.md#free-play-audit-2026-10-13)
přiřazuje TC21 k navigaci UC-114/115 a ukládání UC-113; TC22 k UC-018,
UC-024/025, UC-055, UC-023/036 a UC-042; TC23 k UC-027 a uložení plavby.
Jde o omezené fungující cesty a explicitní pozorování mezer. Nedostupná výbava,
odborník, vůz nebo stavební fronta neznamenají úspěšnou přejímku těchto UC;
stavy celých UC se tímto auditem neuzavírají. Původní cílová očekávání v tabulkách
zůstávají zachována, jednotlivé opravy vlastní R-0018 a R-0020–R-0025.

Navazující [implementace R-0012–R-0018](functional-tests.md#free-play-implementation-2026-10-13)
rozšiřuje důkazy: TC24 pokrývá dvě konkrétní mapy, jméno hráče, francouzské
vlastnictví a návraty v UC-001/002/004/115; TC25 pokrývá kolonijní vybavení,
vrácení zásob a odezvu příkazů v UC-114/119. TC19/TC_13/TC_18 ověřují novou
volbu po změně množství. TC21/TC23 nyní vyžadují poslední uloženou pozici,
nikoli původně pozorovanou ztrátu pohybu. Úplná regrese této implementace prošla;
tyto omezené cesty neuzavírají celé UC ani matici národů a obtížností.

Historicky dne 12. října byly kontroly počtu lodí přesunuty z page component do samotných
TC01 a obou scénářů TC06. TC01 souvisí s UC-024/025 (přeprava jednotek), TC06
s UC-055/056 (nákup lodě a obchod). Tento přesun zachovává původní očekávání
jedné lodi, neověřuje všechny varianty těchto UC a nemění jejich stav přejímky.
Lokálně po opravě výstupu Qwen prošel TC01 (1/1); úplnou sadu včetně TC06
nadále vyžaduje UC-119. Příkazy a rozsah jsou ve
[funkčním kontraktu](functional-tests.md), průběh delegace v
[záznamu Qwen](ai/local-qwen.md#dock-assertion-move-2026-10-12).
Tento směr byl ještě téhož dne nahrazen pojmenovanými page verifikacemi
v PR #44; aktuální pravidlo určuje funkční kontrakt.

Historický běh na revizi
[fb3a9734](https://github.com/jajir/microcol/commit/fb3a9734149a8e95c8b940cfd6a479e2c04a2fe7)
z 11. října měl 603 úspěšných herních testů a ve funkční sadě 27 vybraných,
25 úspěšných, 2 chyby, 0 přeskočených. TC08 a TC09 skončily chybou načtení
nastavení; [běh CI](https://github.com/jajir/microcol/actions/runs/38164271083)
nepublikoval novou distribuci. Při práci na Z01 později nativní TC05 odhalil
souběh čtení a zápisu nastavení; dva reprodukční testy před synchronizací
`SettingService` selhaly a po opravě prošly. Na `c2e24ba4` prošlo všech 44 UI
scénářů včetně TC08/TC09, bez chyb a vynechání; viz
[skutečná evidence Z01](functional-tests.md#z01-bezpecne-vylozeni-nakladu).
Historický neúspěch zůstává zaznamenaný, ale není posledním výsledkem regrese.
Všechny varianty změn jazyka a Nastavení vyžadují vlastní přejímku UC-119.

Nové pravidlo z 12. října 2026 nahrazuje původní přesun asercí doku do TC.
Scénáře teď předávají očekávání pojmenovaným metodám `verify…` v page objektech,
nikoli přímým asercím. Refaktor všech tříd TC zachovává jejich ID a herní
výsledky; aktuální ověření UC-119 a dotčených scénářů je v
[evidenci pojmenovaných verifikací](functional-tests.md#pr-44-pojmenovane-verifikace).

### Kontrola samotného katalogu — 11. října 2026

Lokálně prošlo všech 8 dokumentačních kontrol a přísné sestavení MkDocs.
Kontrola vazeb našla 119 jedinečných UC a pokrytí všech 230 katalogových
pravidel; tabulky byly zkontrolovány ve webovém náhledu. Jde o ověření
dokumentace. Herní scénáře popsané touto stránkou tím nebyly provedeny.
Výklad manuálu a akceptační kritéria připravil hlavní agent s nezávislou
kontrolou ekonomických a bojových/politických částí; krátká kontrola vazeb
využívá stávající dokumentační unittest běh.

Po rozdělení na Hru, Nastavení a Regresi téhož dne prošlo **9 dokumentačních
kontrol** a přísné sestavení MkDocs. Kontroly navíc hlídají kategorii každého
UC a počet 116 v podmínce přijetí Hry. Všech 119 ID a pokrytí všech 230 pravidel
zůstalo zachováno; členění bylo zkontrolováno i ve webovém náhledu.

## Kdy můžeme pravidla považovat za dokončená

Přejímka vyžaduje současně následující podmínky:

1. **Všech 116 UC kategorie Hra má I=H a V=P** včetně všech herních variant
   a matic, s dohledatelnými testy a aktuálními výsledky. UC-001/002 mají
   samostatnou přejímku Nastavení; jejich otevřený stav se nezamlčí ani
   nebrání přijetí Hry. Nově nalezené pravidlo přidá UC nebo variantu;
   stabilní ID zůstávají a počet není neměnným cílem.
2. Každý herní požadavek katalogu je ověřen, včetně účinků národů a obtížností
   a uložených parametrů. Je určen referenční profil a žádná otevřená
   pravidlová Q nebrání posouzení výsledku. Všechny schválené interpretace
   a odchylky jsou výslovně zaznamenané. Nerozhodnutý vzhled či pořadí oken
   v [obnova a prezentace](gap-analysis.md#recovery-and-presentation) tuto podmínku neblokuje. Přesná shoda s nedokumentovanou vnitřní
   implementací původní hry se netvrdí.
3. Modelové kontroly ověří výpočty a meze, TC skutečné hráčské akce. Souvislé
   průchody UC-115–118 proběhnou v běžné nové hře, včetně uložení a obnovení.
4. Celá povinná funkční sada i relevantní modelové testy projdou bez skipů
   a bez známé otevřené regrese; UC-119 dokládá kontrolu této revize.
   Přehled evidence uvádí datum, commit, počty
   a odkazy; historický zelený report nenahrazuje kontrolu změněného chování.
5. Ověřená distribuce je skutečně publikovaná a lze na ní zopakovat přijaté
   hráčské cesty. Zelené sestavení dokumentace samo nestačí.

Potom můžeme označit **pravidla podle manuálu a přijatých rozhodnutí za
funkčně přijatá** a věnovat hlavní práci grafice a UX. Není to potvrzení
dokončení všech konfiguračních voleb originálu ani záruka absence budoucích
chyb. Stejné scénáře zůstanou regresní ochranou při výměně obrázků,
rozmístění či ovládání; výsledky testů Hry se kvůli vzhledu neupravují.

Mimo tuto přejímku jsou původní DOS instalace/hardware, shoda jednotlivých
kláves, ilustrací, rozložení oken a textu epitafu, historická příloha
PDF 101–131 a dokončení volitelné kampaně. Dostupnost herních informací,
příkazů a srozumitelného výsledku do přejímky naopak patří.

Doplnění R-0020 z 13. října 2026: TC27 ověřuje nákupy/prodeje výbavy v
UC-018, přesné citované ceny v návaznosti na UC-057 a identitu/nástroje při
návratové plavbě s uložením v UC-027/113. TC22/TC_01 už očekává nabídku
výbavy po obou druzích kliknutí, při zrušení stále původní stav. Jde o
[rozpracovanou implementaci](functional-tests.md#europe-equipment-implementation-2026-10-13);
vyložení v kolonii a nové odbornosti se tímto dílčím během nepovažují za
ověřené. Úplná regrese tohoto řezu následně prošla lokálně i v publikované
CI commitu `b989413b` (737 herních a 90 funkčních/strukturálních testů).

Publikované R-0021 pokrývá UC-027 a UC-113: TC23 nyní vyžaduje samostatný
krok ven z pruhu a přesný návrat, nové TC28/F20 ověřuje západní plavbu,
rušení, čekání na obsazený cíl, Continue a odmítnutí mapy bez návratového
pole. Tyto cílené cesty prošly v Dockeru; [evidence](functional-tests.md#atlantic-voyage-implementation-2026-10-13)
zachycuje i opravu zkráceného viditelného textu. Úplná lokální regrese
prošla: 767 herních a 92 funkčních/strukturálních testů bez chyb a vynechání.
Stejné počty prošly v CI a build `dev-49-88573a85ac03` byl publikován.

Lokálně ověřené R-0022 doplňuje UC-024/UC-025: TC01 zachovává nalodění a vylodění
obou osob pomocí dvou samostatných voleb. Šest cílených TC29/F21 prošlo:
výběr i jediného pasažéra, zrušení, Continue, nulový pohyb v místním přístavu,
plná/cizí loď a změněný cíl. Model znovu kontroluje oprávnění před změnou
místa. TC27/TC_07 navíc prošel skutečnou plavbou z východního okraje do
kolonie, vyložením vybaveného člověka mimo pracovníky a dalším Continue.
Cestu specialisty doplňuje níže ověřené R-0023. Úplná regrese R-0022 prošla 781 modelovými
a 98 funkčními/podpůrnými testy bez selhání, chyb a vynechání lokálně i v CI;
publikovaný build je `dev-51-b3ba0efcf67e`.
Aktuální stav a neuzavřené kontroly jsou v
[záznamu ověření přepravy](functional-tests.md#individual-passenger-transfers-2026-10-13).

Publikované R-0023 doplňuje omezenou odbornickou část UC-055: TC30/F22
ověřuje cenu 2 200 zlata, zrušení, dvojí potvrzení téže nabídky, další samostatný
nákup, nedostatek peněz a Continue na molu. Tři scénáře prošly v cíleném
výběru 36 modelových a 13 funkčních/podpůrných testů; viz
[evidence](functional-tests.md#expert-purchase-implementation-2026-10-13).
Následný TC30/TC_04 prošel skutečnou plavbou, vyloděním mimo pracovníky,
zařazením do kovárny a výrobou šesti nástrojů ze tří rud. Continue na molu,
na lodi, před zaměstnáním a v zaměstnání zachovává odbornost a identitu.
Rozšířených šest TC30 včetně ztráty oprávnění a českého klávesového nákupu
prošlo ve výběru 13 modelových a 14 funkčních/podpůrných testů. Viditelné
pojmenování pracovníka u kovárny včetně jeho odchodu prošlo v dalším
stejném výběru; samostatně prošlo i odmítnutí staré nabídky po nahrazení
hry. Úplná regrese lokálně i v CI prošla se 794 modelovými a 105
funkčními/kontrolními případy; build `dev-52-2a464170c54d` je publikovaný.
Výše uvedený poměr rudy a nástrojů je současná implementace; jeho opravu
vlastní samostatné [R-0009](requirements/canceled/R-0009.md). Tím není
přijat celý UC-055 s imigračním poolem a dalšími profesemi.

Rozpracované R-0074 rozšiřuje odbornickou část UC-055 na 19 evropských
profesí. Modelový běh 1464 testů prošel; TC30/11–12 přidávají kontrolu celého
seznamu v češtině i angličtině a klávesový nákup poslední položky.
Všech 12 TC30 scénářů prošlo v Dockeru; tři kontroly page objectů také prošly.
Podrobné ověření a nehotové neprodukční schopnosti sleduje
[evidence R-0074](functional-tests.md#r-0074-specialist-roster-2026-10-16);
celý UC-055 zůstává nepřijatý.

R-0071 rozšiřuje UC-027/113 o jednorázově vylosované počasí a uloženou původní
délku plavby. Modelová sada (1386) a vybrané UI scénáře TC23/27/28/30
s kontraktem (26) prošly 2026-10-16; [evidence](functional-tests.md#r-0071-voyage-duration-2026-10-16)
odlišuje ověřené cesty od čekajícího úplného CI a účinku Magellana.


Rozpracované R-0075 doplňuje UC-054/055 o přesně tři uložené kandidáty a
samostatnou cenu placené cesty. TC46/F37 ověřuje zrušení, jedinou výměnu,
Continue, nedostatek peněz, zastaralou cenu, český klávesový nábor s přepravou
a nezávislost nákupu odborníka. Skutečné výsledky a otevřené návaznosti
vede [evidence R-0075](functional-tests.md#r-0075-persistent-recruitment-2026-10-16).

### UC-054: národní přistěhovalectví R-0076

R-0076 doplňuje národní postup a příchody z uložených kandidátů. Kříže
z právě dokončené výroby se projeví při následující evropské fázi národního
tahu; obrazovka Evropy ukazuje čekání a blokuje placenou cestu do vyřízení
příchodu. `TC_47_F38_immigration_test` ověřuje skutečnou výrobu, příchozího
na molu a Pokračovat před i po příchodu; stav provedení je vedený v
[R-0076](requirements/R-0076.md), nikoli potvrzen touto mapou scénáře.

### UC-055: opakované nákupy R-0077

`TC_48_F39_ship_purchase_prices_test` mapuje zrušení nabídky, dvě potvrzené
karavely za 1000 a 1100, nedostupnou třetí za 1200, Pokračovat a odmítnutí
změněné nabídky nebo nedostatku peněz po otevření. Růst cen skutečného
dělostřelectva zůstává integrační závislostí R-0095. Provedení a přejímku
vede [R-0077](requirements/R-0077.md); samotná mapa není důkazem průchodu.

### UC-041: chov koní R-0078

`TC_49_F40_horse_breeding_test` mapuje skutečné postavení stájí, obyčejný
a zdvojený přírůstek, zobrazený náhled skladu, kapacitní limit a přednost
narození kolonisty při efektivních 201 jednotkách jídla. Po uložení se
zásoby ani stavba neopakují. Modelové a hráčské výsledky jsou vedené v
[R-0078](requirements/done/R-0078.md); tato mapa sama nepotvrzuje průchod.

### UC-045: průmyslová výroba R-0079

`TC_50_F41_industrial_production_test` propojuje skutečné dokončení pily
se zdvojnásobením produkce stejného tesaře, společné zaokrouhlení nedostatku
pro dva pracovníky továrny a přidělení pracovníků železárně a zbrojnici.
Kontroluje zobrazené změny zásob, skutečný další tah a Pokračovat.
Modelová tabulka pokrývá všech 20 průmyslových stupňů. Přijetí továren
podle otce zůstává v R-0084; tyto scénáře neuzavírají celé UC-045.
Výsledky vede [evidence R-0079](functional-tests.md#r-0079-industrial-production-2026-10-16).

### UC-046: doky, rybolov a loděnice R-0080

`TC_51_F42_coastal_facilities_test` provádí skutečné dokončení doku,
suchého doku a loděnice, odmítá rybolov bez zařízení a zachovává rybáře
přes obě vylepšení. Galeonu a fregatu dokončuje až s loděnicí; zrušení
nabídky a Pokračovat chrání pracovníky, frontu a skutečné lodě.
Testy modelu doplňují vnitrozemské a uložené neplatné stavy.
Výsledky zaznamenává [R-0080](requirements/done/R-0080.md); tato mapa sama
nepotvrzuje průchod scénářů ani úplné přijetí UC.

### UC-047: tiskové bonusy R-0081

`TC_52_F43_printing_production_test` objedná a dokončí tiskárnu a noviny,
se stejnými dvěma státníky ověří náhled 7 → 10 → 14 zvonů a skutečné
přírůstky skladu. Druhý scénář přesune státníky do jiných profesí a ověří
základ bez obsluhy 1 → 1 → 2. Oba projdou Continue bez opakování produkce;
první kontroluje i zrušení objednávky a popisek příspěvku pracovníka.
Výsledky vede [evidence R-0081](functional-tests.md#r-0081-printing-production-2026-10-16).
Obranná část UC-047 zůstává samostatným rozsahem.

### UC-047: minimum obyvatel opevněné kolonie R-0082

`TC_53_F44_fortified_population_test` táhne třetího obyvatele ven a
kontroluje odmítnutí, vysvětlení a nezměněný svět. Přidělení jiné vnitřní práce
zůstává povolené; čtvrtý obyvatel může odejít a nastoupit na loď. Druhý
scénář projde skutečným hladověním na dva obyvatele a zachováním palisády.
Oba ověřují Continue. Výsledky vede [R-0082](requirements/R-0082.md).

### UC-043: zachování kladiv R-0109

`TC_54_F45_shared_hammers_test` mění A→B→A, odstraní oba projekty,
zkontroluje zrušení výběru a poté dokončí pilu, sklad a vůz přes skutečné
tahy. Kontroluje přebytky, jen jedno dokončení za tah, prázdnou frontu
a Continue. TC32 zachovává kontrolu chybějících nástrojů a předchůdce
s kladivy ve společném skladu. Výsledky vede [R-0109](requirements/R-0109.md).

### UC-044: zaplacení dokončení R-0083

`TC_55_F46_rush_construction_test` otevře cenu aktuální kovárny, zruší
nákup beze změny, poté zaplatí 76 zlatých a ověří hotovou stavbu,
zbytek fronty a Continue bez další platby. Druhý scénář ověří nedostatek
zlata, odmítnutí zastaralé nabídky po změně materiálů a novou cenu.
Model doplňuje nulovou cenu, přebytky, jednotky a neplatné předpoklady.
Výsledky vede [R-0083](requirements/R-0083.md).
