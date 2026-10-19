# Funkční testy a ochrana chování při změnách vzhledu

Funkční scénáře v `microcol-test` jsou společnou kontrolou toho, co hráč dokáže
ve hře udělat a jaký herní výsledek dostane. Každá změna chování musí mít
odpovídající pokrytí v těchto scénářích. Při úpravě vzhledu musí stejné kroky
nad stejným výchozím stavem vést ke stejnému hernímu výsledku.

## R-0248 — Rámečky a pravítko — 2026-10-19

1. [R-0248](requirements/done/R-0248.md) mění pouze kresbu checkboxů,
   radio buttons a dvou sliderů ve skutečném Nastavení. TC84 kontroluje
   volby a jejich preference, češtinu/angličtinu, stav pod myší a stisk,
   nedostupnost, krajní hodnoty a tažení; TC83 chrání přijatá tlačítka.
2. První běh `run-DG8Z2r` (completion `20261019T042155Z-4eefc11868af`)
   selhal při kompilaci TC84: test použil nesprávné názvy existujících
   metod pro výběr jazyka. Oprava používá `setCzechLanguage` a
   `setEnglishLanguage`; nejde o průchod funkčních kontrol.
3. Review doplnilo sledování skutečné polohy nativního jezdce i při
   animaci kliknutí na dráhu. TC84 navíc kliká na dráhu a porovnává
   vybarvený jezdec s nativní polohou. Navazující cílený běh je completion
   `20261019T042407Z-985fc6572333`. Plná regrese, vizuální kontrola
   exportovaných snímků a publikace zatím čekají.
4. `run-tQdWN7` (completion `20261019T042407Z-985fc6572333`) spustil
   8 kontrol: kontrakt page objects a TC83 prošly (6); oba scénáře TC84
   selhaly. Šipka vpravo u původního slideru rychlosti používá výchozí
   `blockIncrement=10` a z hodnoty 0 dosáhne maxima 4; test nyní očekává
   toto dosavadní chování, aplikace jej nemění. Druhý problém je neověřená
   stisknutá výplň a chyby `NGCanvas` při renderování. Kresba nyní nezačíná
   před přidělením kladných rozměrů; diagnostika kontroluje šířky, stisk,
   hover a ukládá první snímek. Tato hypotéza zatím není potvrzená opravou.
   Cílené opakování: completion `20261019T042607Z-91f1e59a078e`.

5. `run-OmgJL7` (completion `20261019T042607Z-91f1e59a078e`) prošel
   prvním scénářem TC84 včetně výplně stisku, preferencí, obou jazyků
   a nedostupnosti. Snímek českého Nastavení byl prohlédnut: rámečky,
   pravítka, popisky a Zpět jsou čitelné uvnitř okna 900 × 750.
   Druhý scénář prošel hodnotami, klávesnicí, tažením a kliknutím na dráhu,
   ale selhal až při snímku celé scény v Prism. Ochrana nulových rozměrů
   tedy renderovací problém sama nevyřešila. Izolovaná diagnostika TC84/02
   s rozměry a Prism pool statistikami je completion
   `20261019T042750Z-926a98e964d2`; přejímka stále čeká.

6. Diagnostika `20261019T042750Z-926a98e964d2` potvrdila scénu
   **100 000 × 100 000**, zatímco plátna ovládacích prvků měla běžných
   90–318 × 40 nebo 200 × 63 logických pixelů. Samostatné `Canvas.snapshot`
   v nových kontrolách nechalo JavaFX přepočítat scénu podle velkého
   preferovaného rozměru `CenteredPage`. Nové pixelové kontroly proto
   používají existující `AbstractScreen.snapshotScene`, který zachovává
   rozměry okna, a souřadnice kresby převádějí do scény. Kontrola vzhledu
   také ověřuje, že scéna nepřesahuje okno. Nemění se produkční rozložení
   ani nativní obsluha slideru. Popisky krajů pravítka nově používají Caveat.
   Cílená přejímka TC84/TC83 a kontraktu page objects běží v completion
   `20261019T043040Z-ca9ef18e6274` (supervisor 78615); výsledek zatím čeká.

7. Cílený běh `run-VMG5Zo` (completion
   `20261019T043040Z-ca9ef18e6274`) po opravě prošel: **8 kontrol,
   0 selhání, 0 chyb, 0 přeskočených** — TC84 (2), TC83 (3) a
   `PageVerificationContractTest` (3). Log neobsahuje původní výjimky Prism.
   Prohlédnuté snímky `framed-settings-czech.png`,
   `framed-settings-ruler-endpoints.png` a `framed-settings-disabled.png`
   potvrzují čitelné rámečky, pravítka a Caveat v okně 900 × 750,
   viditelný fokus i odlišenou nedostupnost. Původní anglické krajní popisky
   sliderů zůstávají i při českém jazyce; změna nepřidává lokalizační chování.
8. Úplná regrese modelu a UI v Dockeru/Xvfb/Openbox běží v completion
   `20261019T043202Z-4b64f7418ec7` (supervisor 78887, limit 3 600 s).
   Výsledek, dokončení R-0248 a publikace zatím čekají. Nativní HiDPI
   monitor nebyl součástí dosavadního ověření.

9. Úplný běh `run-9ycUac` (completion
   `20261019T043202Z-4b64f7418ec7`) dosáhl limitu 60 minut během poslední
   třídy TC73; log ukazuje pokračující hráčské kroky, nikoli zamrznutí.
   Dokončilo se **2 348 modelových/podpůrných testů ve 201 třídách** a
   **371 UI/podpůrných testů v 86 třídách**, všechny bez selhání, chyb
   a přeskočení. Po ukončení zůstaly Maven/Xvfb/Openbox logy, export XML
   není dostupný; počty pocházejí z dokončených tříd v Maven logu.
   Porovnání s 87 současnými UI testovacími třídami našlo jedinou
   nedokončenou třídu `TC_73_F65_clear_forest_test` se 6 scénáři.
   Navazující běh dokončí celou TC73 a zopakuje TC84 na nezměněných
   produkčních a testovacích zdrojích. Žádný scénář se nevynechává.
10. Doplnění regrese TC73 + TC84 běží v completion
    `20261019T053308Z-f3db4338df06` (supervisor 95522, limit 3 600 s).
    Výsledek zatím čeká. Předchozí návrhy deseti fontů a jejich tenčích
    variant jsou samostatné obrazové návrhy; v této implementaci se
    ponechává Caveat a výběr jiného fontu zůstává na uživateli.

11. Kontrolní seznam vyžaduje čerstvé Surefire XML; souhrny v logu
    ukončeného běhu proto nejsou úplnou přejímkou. Po diagnostickém
    dokončení TC73 se celá sada zopakuje ve třech částech: model + TC01–30
    a podpůrné UI třídy; TC31–60; TC61–84. Manifest
    `target/r248/full-partitions.json` obsahuje všech 288 tříd z úplného
    výběru (201 modelových a 87 UI/podpůrných), bez vynechání a duplicity.
    Výběr vychází ze skutečně spuštěných tříd posledního úplného běhu,
    včetně nedokončené TC73. Limit ani požadavek na reporty se nesnižuje.

12. Doplnění `run-0B3LYA` (completion
    `20261019T053308Z-f3db4338df06`) prošlo: čerstvé XML potvrzuje **8 testů,
    0 selhání, 0 chyb, 0 přeskočených** — TC73 (6) a TC84 (2).
    První úplná část (201 modelových tříd, TC01–30 a 3 podpůrné UI třídy)
    nyní běží v completion `20261019T053511Z-0ff68f273b7d`, supervisor 95823,
    limit 3 600 s. Zbývající části navážou bez změny testovaného kódu.

13. První úplná část `run-n8DyBO` (completion
    `20261019T053511Z-0ff68f273b7d`) prošla. Exportované XML potvrzuje
    **2 348 modelových testů ve 201 třídách a 138 UI/podpůrných kontrol
    ve 33 třídách**, 0 selhání, chyb a přeskočených. Porovnání názvů
    tříd s manifestem potvrdilo přesný výběr bez chybějících tříd.
    Druhá část TC31–60 běží v completion `20261019T055917Z-103858cd4053`,
    supervisor 98735, limit 3 600 s. Kód zůstává stejný.

14. Druhá úplná část `run-vc21j9` (completion
    `20261019T055917Z-103858cd4053`) prošla: XML potvrzuje **147 testů
    ve 30 třídách TC31–60**, 0 selhání, chyb a přeskočených. Názvy tříd
    přesně odpovídají manifestu. Poslední část TC61–84 běží v completion
    `20261019T062023Z-cb7c0d3278b2`, supervisor 2946, limit 3 600 s.

15. Poslední část `run-LQjNin` (completion
    `20261019T062023Z-cb7c0d3278b2`) prošla: **92 testů ve 24 třídách
    TC61–84**, 0 selhání, chyb a přeskočených. Souhrn tří čerstvých
    exportů Surefire XML obsahuje **2 348 modelových a 377 UI/podpůrných
    kontrol**, celkem 288 tříd bez chybějících či duplicitních tříd.
    Tím je dokončen celý výchozí výběr, včetně TC21/TC38/TC83/TC84.
16. Závěrečné review zkontrolovalo rozsah CSS pouze pro Nastavení,
    průhlednost kresby vůči myši, převzetí skutečných oblastí nativních
    skinů, obnovu kresby při změně hodnoty/stavu/jazyka a zachování
    nativních vstupů i preferencí. Testy zachovávají herní očekávání;
    pixelové kontroly používají stávající bezpečné snímkování scény.
    Nebyl zjištěn zbývající blokující nález. Sdílený formatter prošel
    všech 11 změněných Java souborů s kontrolou tokenů a idempotence;
    dokumentace prošla 9 kontrolami a přísným MkDocs buildem.
    R-0248 je `done`; zveřejnění přesného commitu a sestavení ještě čeká.
    Nativní HiDPI monitor nebyl testován, Docker používá Linux/JDK 17,
    Xvfb/Openbox a Glass.

17. Publikace commitu
    [`3894bdc3`](https://github.com/jajir/microcol/commit/3894bdc354dfca94e30932a9d5bad820d7523b8a)
    je ověřena 19. října 2026. Vývojový
    [běh 39211069004](https://github.com/jajir/microcol/actions/runs/39211069004)
    dokončil funkční sadu, build-tool kontroly, všechny tři nativní balíčky
    a publikaci. Stažené Surefire XML z `tests-functional` potvrzuje
    **2 348 modelových a 377 UI/podpůrných testů**, bez selhání, chyb
    a přeskočených. Samostatná
    [kompatibilita 39211069052](https://github.com/jajir/microcol/actions/runs/39211069052)
    má v každé ze čtyř kombinací Linux/Windows × Java 11/17 stejných
    2 348 úspěšných modelových testů. Dokumentace prošla buildem a deployem
    [39211069037](https://github.com/jajir/microcol/actions/runs/39211069037);
    živá stránka R-0248 obsahuje stav `done`.
18. Stabilní manifest `dev` ukazuje na
    [dev-184-3894bdc354df](https://github.com/jajir/microcol/releases/tag/dev-184-3894bdc354df)
    a přesný zdrojový commit `3894bdc354dfca94e30932a9d5bad820d7523b8a`.
    Ověřeny jsou všechny tři platformy v manifestu a 9 kontrolních součtů
    release assetů proti SHA-256 digestům GitHubu. Stažení a ZIP integrita
    [macOS launcheru](https://github.com/jajir/microcol/releases/download/dev/MicroCol-Dev-macos.zip)
    byly ověřeny; launcher při příštím spuštění nabídne aktuální build.
    Tato přejímka nepředstírá ruční spuštění hry na všech třech systémech
    ani kontrolu nativního HiDPI. Nové varianty fontů zůstávají návrhy.

## R-0246 — Dřevěná tlačítka — 2026-10-18

1. [R-0246](requirements/done/R-0246.md) mění vzhled standardních tlačítek;
   individuálně upravené ovládání zachovává. TC83 ověřuje menu, návrat
   z nastavení, původní ikonové ovládání a lodě, různé délky českých popisků,
   tři stavy, fokus, Space/Enter/Escape, nedostupnost a přepínač v modálním
   vzorku s produkčním dialogovým stylem. TC21 a celá sada chrání dosavadní
   hráčské výsledky; samotný vzorek nenahrazuje reálné cesty dialogy.
2. Diagnostické běhy `run-7IOQJA` a `run-pZ0rX8` spustily po 15 kontrolách:
   14 prošlo a modální vzorek TC83 selhal na zkráceném `OK`. Následující
   cílené běhy `run-UnCFZ0`, `run-WtfLhz`, `run-HiXF0S` a `run-XTvCHd`
   upřesnily příčinu: dekorované testovací okno změnilo po zobrazení šířku
   na 200 px. Původní hypotéza o pozdních CSS metrikách nebyla potvrzena.
   Vzorek nyní používá stejný `UNDECORATED` Stage jako produkční dialogy.
   `run-HiXF0S` navíc selhal při návratu z Kongresu; cesta nyní používá
   skutečné zavírací tlačítko. Žádný z těchto běhů není přejímkou.
3. Review doplnilo obnovu kresby při změně textu/graphic ve standardním
   skinu a TC83 ověřuje změnu popisku. Neplatné CSS `inherit` u fontu
   výjimek bylo nahrazeno původní rodinou a relativní velikostí; následné
   běhy už původní konverzní varování nehlásily.
4. `run-y0UNru` prošel dvěma scénáři ze tří; třetí ověřil rozměry, obrys,
   výplně, kliknutí a Space, ale selhal na Enter. Test omylem očekával
   výchozí akci při fokusu jiného tlačítka. Oprava před Enter přesune fokus
   na netlačítkový obsah dialogu; standardní handlery hry se nemění.
   Opravený cílený běh `run-hw4Gw3` (completion
   `20261018T213003Z-e9840a5d8913`) prošel všemi 3 scénáři TC83:
   0 selhání, 0 chyb, 0 přeskočených.
5. Snímky menu, hry, Kongresu a modálního vzorku byly vizuálně zkontrolovány:
   popisky jsou čitelné, ikonové ovládání zůstalo původní. Myš, klávesnice
   i výběr prošly cíleným během. Nativní HiDPI monitor nebyl součástí
   těchto kontrol.
6. Úplný běh `run-enzXJ1` (completion `20261018T213052Z-259b33b9d1ce`)
   19. října v místním čase dosáhl limitu 45 minut při pokračujícím TC34;
   nešlo o zamrznutí. Log dokládá 2 348 úspěšných modelových/podpůrných
   kontrol a 275 dokončených UI/podpůrných kontrol v 60 třídách
   (274 prošlo, 1 selhala, 0 chyb a přeskočených). Úplná UI přejímka
   chybí a export XML po ukončení není dostupný.
   TC71 odhalil překrytí příchozího a odchozího panelu plavby kvůli vyšším
   tlačítkům. Odchozí panel se v existujícím rozložení posouvá o 20 px dolů;
   TC71 zachovává kontrolu nepřekrývání a skutečný návrat lodi a ukládá snímek.
   Cílená oprava `run-hw7nu5` (completion
   `20261018T221716Z-65bbb7d8dff2`) prošla: TC71 (1) a TC83 (3),
   0 selhání/chyb/přeskočených. Snímek potvrzuje volný prostor mezi
   příchozím tlačítkem Reverse course a odchozím panelem.
   Celá sada musí následně projít po částech se shodným úplným výběrem;
   časový limit není důvodem k vynechání scénáře. Rozdělení zachovává
   všechny modelové testy, 83 TC tříd a 3 podpůrné UI třídy:
   model + TC01–30 + podpůrné; TC31–60; TC61–83.
7. První část `run-MMqOoO` (completion `20261018T221835Z-725cf5d05b47`)
   prošla. Exportované XML dokládá **2 349 testů v 202 třídách microcol-game**
   a **138 UI/podpůrných testů v 33 třídách**, vždy 0 selhání/chyb/přeskočených.
   Explicitní výběr zahrnul navíc úspěšný integrační `ConnectorTest`, který
   původní běh nevybíral.
8. Druhá část `run-J4UAZF` (completion `20261018T224314Z-63d79df7e0f7`)
   prošla: exportované XML potvrzuje **147 testů v 30 třídách TC31–60**,
   0 selhání/chyb/přeskočených.
9. Třetí část `run-hbl3f2` (completion `20261018T230444Z-23dede859e0f`)
   prošla: **90 testů v 23 třídách TC61–83**, 0 selhání/chyb/přeskočených.
   Kontrola seznamů tříd proti rozdělení nepotvrdila žádné chybějící ani
   opakované výběry mezi částmi. Úplná místní přejímka tak obsahuje
   **2 349 modelových/integračních a 375 UI/podpůrných testů**, bez selhání,
   chyb či přeskočení. Jde o pracovní změny nad `6cf0d0dd`, nikoli ještě
   o důkaz publikované distribuce.
10. Závěrečná kontrola změn ověřila zachování nativních handlerů a vlastních
    obrazových skinů, obnovu kresby při změně textu a konstantní logické
    tloušťky tahů. Sdílený formatter ověřil 8 změněných Java souborů
    (shodné kódové tokeny, idempotence). Prošlo 19 kontrol completion runneru,
    21 kontrol launcheru workeru bez inference, 9 kontrol dokumentace,
    přísný MkDocs build a kontrola whitespace vlastních zdrojů. Převzatá
    licence Caveat OFL zůstává byte-for-byte včetně původního koncového whitespace.
11. Commit vzhledu `f6e414cc2829bedef8f4abd4a9a8bee47a5909f6` byl odeslán
    do main. Jeho [vývojový běh](https://github.com/jajir/microcol/actions/runs/39176195646)
    zrušil starý 45minutový limit po 2 711 dokončených kontrolách bez chyby;
    tato neúplná evidence sama není přejímkou. Následná oprava limitu
    `07bb1e0203442bd46ff88f7868fbc7e5489639c8` mění pouze CI a dokumentaci,
    zachovává tentýž herní kód a obsahuje tuto změnu tlačítek.
12. [Vývojový běh 39176254760](https://github.com/jajir/microcol/actions/runs/39176254760)
    19. října 2026 úspěšně dokončil všechny distribuční, funkční a publikační
    kroky. Stažené XML potvrzuje **2 348 modelových testů v 201 třídách
    a 375 UI/podpůrných testů v 86 třídách**, 0 selhání/chyb/přeskočených.
    Rozdíl proti místnímu výběru je jediný dodatečný `ConnectorTest`.
    [Kompatibilita](https://github.com/jajir/microcol/actions/runs/39176255209)
    prošla pro Linux/Windows a Java 11/17; [Pages](https://github.com/jajir/microcol/actions/runs/39176254769)
    úspěšně nasadily dokumentaci.
13. Kanál `dev` i [vydání dev-181-07bb1e020344](https://github.com/jajir/microcol/releases/tag/dev-181-07bb1e020344)
    obsahují přesný commit `07bb1e0203442bd46ff88f7868fbc7e5489639c8`.
    Všechny tři archivy macOS ARM64/x86-64 a Linux x86-64 existují a jejich
    GitHub SHA-256 digest souhlasí se staženým manifestem. Ověřené zdroje
    jsou pod `target/r246/ci-final-functional` a `target/r246/published`;
    completion `20261019T000902Z-5d2333283b08` není jediným podkladem závěru.

Pracovní pravidla platí od **11. října 2026**. Vzorem čitelné struktury je
[TC_18_F01_plow_field_test](../microcol-test/src/test/java/org/microcol/test/TC_18_F01_plow_field_test.java).
Testy chrání konkrétní ověřené cesty; nejsou důkazem úplnosti pravidel podle
manuálu ani zárukou, že ve hře nemůže být jiná chyba. Chybějící pravidla nadále
eviduje [analýza rozdílů](gap-analysis.md).

[Katalog use cases](use-cases.md) propojuje pravidla s hráčskými kroky a vede
jejich přejímku. Před změnou vybrat dotčené UC a pravidla, přiřadit konkrétní
TC/metody a po ověření aktualizovat jejich evidenci. UC nenahrazuje ID TC
ani zde předepsanou strukturu testů; jediný dílčí scénář neuzavírá všechny
varianty celého UC.

V zadání rozlišovat [Hru, Nastavení a Regresi](use-cases.md#zavazna-hra-a-samostatne-nastaveni).
Herní výsledky mají závazné očekávání z manuálu a přijatých rozhodnutí.
Změna průvodce nastavením nebo vzhledu mění kroky či page objects, nikoli
očekávané účinky pravidel. Národ, obtížnost a jejich uložené parametry ověřovat
i tehdy, když jejich výběrové UI ještě není hotové.

U herní akce ověřit obě vrstvy podle [kontraktu modelu](architecture.md#platnost-herni-akce-a-vyjimky):
model nepovolený vstup odmítne výjimkou před změnou stavu; UI hráči nabídne
jen dovolenou akci nebo vysvětlení. Testovat také zrušení a změnu stavu mezi
náhledem a potvrzením. Výjimka v modelovém testu nenahrazuje test dostupného
ovládání a bezpečného výsledku v UI.

## Co vyžaduje každá změna


| Druh změny | Povinné pokrytí |
| --- | --- |
| Nová funkce nebo změna pravidla | Doplnit nebo upravit pojmenovaný scénář `TC_...`, který provede akci přes UI a ověří herní výsledek. Výpočty a hraniční případy doplnit do modelových testů v `microcol-game`. |
| Oprava chyby | Přidat reprodukční kroky a očekávaný správný výsledek; ověřit, že test zachytí původní chybu a po opravě projde. |
| Obrázky, CSS, rozměry, rozmístění nebo popisky | Zachovat očekávané herní výsledky a provést dotčené funkční scénáře. Ověřit také skutečný obraz, klikatelnost, výběr a klávesové ovládání podle [UX pravidel](ux.md). |
| Sdílené tlačítko, dialog, ovládání mapy nebo page object | Najít všechna použití a ověřit související scénáře; před dokončením spustit celou funkční sadu. |

Před úpravou uvést dotčená pravidla a konkrétní ID scénářů. Pokud stávající
scénář už přesně ověřuje zachovávané chování, stačí doložit jeho úspěšný běh;
není třeba mechanicky měnit testový soubor. Nové nebo změněné chování musí
mít nové či změněné očekávání ve stejném commitu jako implementace.

U změny pravidel popsat starý a nový očekávaný výsledek a důvod změny.
U pouhé změny vzhledu se očekávané zásoby, tahy, pohyb, příkazy a stav světa
nemění. Selhání se řeší opravou aplikace nebo skutečně zastaralého způsobu
vyhledání ovládacího prvku, nikdy odstraněním aserce, `@Disabled`, potlačením
výjimky, přidáním retry nebo výběrem tagu, který scénář vynechá.

## Stálá a čitelná struktura scénářů

Zachovat současný JUnit 5 a TestFX a členění následujících souborů:

| Místo | Odpovědnost |
| --- | --- |
| [`org.microcol.test`](../microcol-test/src/test/java/org/microcol/test/) | Čitelný příběh hráče: příprava, kroky a explicitní volání `verify…` s očekávanými výsledky. |
| [`org.microcol.page`](../microcol-test/src/test/java/org/microcol/page/) | Vyhledání prvků, akce a čekání; oddělené pojmenované metody `verify…` pro ověření výsledků. |
| [`src/test/scenarios`](../microcol-test/src/test/scenarios/) | Reprodukovatelné uložené hry a testové nastavení. |
| [`AbstractMicroColTest`](../microcol-test/src/test/java/org/microcol/test/AbstractMicroColTest.java) | Společné spuštění aplikace a přístup k testovému kontextu a modelu. |

**Aktuální pravidlo od 12. října 2026:** aserce v balíčku `page` patří pouze
do metod začínajících `verify`, dále pojmenovaných podle ověřované věci.
Nejde o prefix názvu třídy. Očekávané počty, hodnoty či stavy přijímají jako
parametry, například `verifyNumberOfShipsInPort(expectedCount)` nebo
`verifyUnitInPortSelected(unitIndex, expectedSelected)`. Aserce nesmí být
přímo v akčních metodách, getterech ani konstruktorech.

Třídy `TC_...` nevolají JUnit/TestFX aserce přímo, ani je neschovávají do
vlastních pomocných metod. Scénář udává očekávání a volá příslušnou page
verifikaci; nevznikají obecné obálky typu `verifyEquals(expected, actual)`.
Například `PageComponentPanelDock` má samostatné gettery, `selectUnitFromPort`
s podmíněným kliknutím a FX čekáním a dvě samostatné metody `verify…`.
TC19 tak čitelně provede `colony.selectUnitFromPort(0)` a následně
`colony.verifyUnitInPortSelected(0, true)`. TC01/TC06 volají
`verifyNumberOfShipsInPort(1)`; tělo aserce zůstává v komponentě doku.

Ověření nepřesouvá jednotky, nekliká a nemění ověřovaný stav. Akce zachovávají
technické preconditions a časově omezená čekání: chybějící prvek nebo
nedokončené nativní gesto musí selhat, ne být tiše přeskočeno. Dotazy TestFX
`FxAssert.assertContext()` pouze zpřístupňují vyhledávání, nejsou asercí.
Kontroly identity obrazovek zůstávají v pojmenovaných `verify…` metodách;
stávající navigace je může volat při přechodu. Přímá herní očekávání scénáře
zůstávají explicitní, například výběr lodi a stav zásob.

[PageVerificationContractTest](../microcol-test/src/test/java/org/microcol/test/PageVerificationContractTest.java)
při běžném Maven testu parsuje zdroje pomocí JDK compiler API: odmítne aserce
v `TC_...` a aserce mimo `verify…` v `page`. Zahrnuje i Java `assert`, lambdy
a kvalifikovaná volání; vlastní negativní příklad chrání funkčnost této
kontroly. Třetí test ověřuje, že verifikace doku odmítne chybný počet, chybný
výběr i neexistující loď, nikoli že kontrolu přeskočí. Tyto kontroly doplňují,
nenahrazují hráčské scénáře.

Každý scénář dodržuje stejné pořadí:

1. Krátký JavaDoc vysvětlí ověřované chování.
2. Pojmenované konstanty popíší důležitá místa a soubor výchozí hry.
3. `@Start` zavolá společné `initialize(...)` s tímto souborem.
4. Metoda `@Test` s používaným tagem načte hru přes `WelcomePage`.
5. Následují přímočaré kroky hráče a bezprostřední kontroly výsledků, oddělené
   krátkými anglickými komentáři. Závěr ověří skutečný herní důsledek.

Zachovat ID třídy `TC_nn`, vazbu `Fnn` na scénář a ID metody `TC_nn`.
Nové třídy pojmenovat `TC_nn_Fnn_popis_test`; popis metody má odpovídat tomu,
co test skutečně kontroluje. Stávající scénáře nepřečíslovávat a nepřepisovat
plošně do parametrizované tabulky, DSL, hluboké dědičnosti nebo obecných
callbacků. Kód, komentáře i názvy pomocných metod zůstávají anglicky.

Používat existující page objects, například `pressP()` a
`nextTurnAndCloseDialogs()`. Detaily vzhledu, CSS selektory a převod souřadnic
patří tam, nikoli do příběhu každého testu. Například výběr množství zboží
používá Ctrl na macOS a Ctrl+Shift na Linuxu; rozdíl řeší společný page object.
Zachovávat stabilní ID ovládacích
prvků i při výměně ikony. Nové čekání má sledovat skutečnou podmínku s časovým
limitem; náhodně prodlužované `sleep` neschovává chybu synchronizace.
U akcí více jednotek čekat na celý očekávaný výsledek: například TC01 čeká
na obě vyloděné jednotky. Tlačítko dalšího tahu se může krátce odblokovat
mezi jejich animacemi, a samo tedy dokončení celé akce nepotvrzuje.

Model lze číst pro ověření výsledku. Herní akci ale test provádí přes UI:
`unit.setActionType(...)` by v testu klávesy P obešlo právě chování, které
potřebujeme chránit. Před i po akci ověřovat tutéž jednotku. Očekávané hodnoty
odvozovat z pravidel a výchozího scénáře, nikoli z runtime konstanty testované
implementace. Fixture má měnit jen úkol, který potřebuje jiný výchozí stav;
u sdílené fixture zkontrolovat všechny její spotřebitele.

## Příklad ochrany orby

TC18 používá skutečný výběr kolonisty, přesun na sousední pole, konec tahu
a klávesu P. Před příkazem kontroluje dostupnou akci, jeden bod pohybu a stav
`noAction`. Po příkazu kontroluje `plowField`, nula bodů pohybu, zachovanou
polohu téže jednotky a skrytí již probíhající akce.

Samotné zmizení ikony by nestačilo: chyba by mohla ikonu skrýt, aniž by
jednotka začala pracovat. [TC15](../microcol-test/src/test/java/org/microcol/test/TC_15_F05_plow_field_test.java)
navíc ověřuje zobrazení nulového pohybu v pravém panelu. Tyto scénáře samy
neověřují délku orby, spotřebu nástrojů ani konečné výnosy pole; změna těchto
pravidel musí doplnit příslušné kroky a modelové testy.

## Spouštění a ochrana publikace

Od **13. října 2026** je výchozím prostředím lokálních UI testů **Docker
s Xvfb a Openbox**, podle dohody v [R-0033](requirements/done/R-0033.md). Zamčený
Mac není překážkou těchto testů. Nativní macOS okno používat jen pro výslovnou
platformní nebo ruční vizuální kontrolu; běžná regrese nevyžaduje odemčení.

### Docker UI tests

Při práci agenta spouštět dlouhý běh přes
[completion runner](ai/completion-callbacks.md#docker-tests) s UUID původního
tasku. Po callbacku ověřit konkrétní reporty; watchdog ani oznámení nenahrazují
přejímku. Níže uvedený foreground příkaz, výběr testů a CI zůstávají platné.

Z kořene repozitáře spustit:

```sh
docker compose -f compose.ui-tests.yml run --build --rm ui-tests
```

[Compose](../compose.ui-tests.yml) sestaví [testovací obraz](../scripts/ui-tests/Dockerfile)
s Maven 3.9.12, JDK 17, Linuxem amd64, knihovnami JavaFX, Xvfb a Openbox.
Apple Silicon používá emulaci amd64, aby lokální prostředí odpovídalo CI.
Kontejner má limit 4 GiB; testovací JVM používá explicitní heap 2 GiB
a Maven 768 MiB. Výchozí JVM heap 1 GiB při tomto limitu nestačil pro TC19.
Obraz je testovací nástroj, nikoli distribuovaná hra. Neotevírá porty ani
nepřipojuje displej hostitele. Docker Engine musí běžet; u již nainstalovaného
Docker Desktop jej lze spustit příkazem `docker desktop start` a ověřit
`docker info`. Nedostupný runtime diagnostikovat nebo použít Linux CI,
nikoli přejít na klikání do zamčené macOS plochy.

[Entrypoint](../scripts/ui-tests/run.sh) čte checkout přes read-only mount,
zkopíruje aktuální zdroje včetně necommitnutých změn bez `.git` a adresářů
`target` do dočasného `/workspace` a tam spustí čistý Maven build. Tím se
nepřenášejí staré výsledky ani nativní binárky hostitele a JavaFX má cestu bez
mezer. Osobní save soubory ani hostitelský Maven profil se nepřipojují.
Maven závislosti se ukládají v samostatném Docker volume.

Každý běh ponechá vlastní adresář `target/docker-ui/run-XXXXXX/` s `maven.log`,
`xvfb.log`, `openbox.log`, `exit-code.txt` a dostupnými reporty v
`microcol-game/surefire-reports/`, `microcol-test/surefire-reports/` a screenshoty
v `microcol-test/warehouse-qa/`. Při selhání zůstávají dostupné výsledky zachované
a příkaz vrací nenulový stav. Kontrolovat výsledky konkrétního nového běhu,
ne starší sousední adresář. Na Linuxu jsou exportované soubory vlastněné rootem
kontejneru; obsah je čitelný pro nahrání CI artifactu.

Cílenou diagnostiku lze vybrat běžnými Maven argumenty, například:

```sh
docker compose -f compose.ui-tests.yml run --build --rm ui-tests \
  -Dtest=TC_06_F06_buy_and_sell_goods_test,TC_23_F16_atlantic_voyage_audit_test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

Název konkrétní třídy ověřit ve zdrojích; cílený běh nenahrazuje celou sadu.
`failIfNoSpecifiedTests=false` zde dovoluje závislý herní modul bez zvoleného
TC, nikoli přijetí nulového počtu testů. Výchozí plný příkaz tento přepínač
nepoužívá. Všechny běhy používají skutečný Linux Glass a
`testfx.headless=false` ve virtuálním X11 displeji 1600 × 1200; Openbox spravuje
zaměření oken. Hostitelský monitor se nepoužívá.

Samotný runner lze ověřit příkazem `bash scripts/ui-tests/test-runner.sh`.
Kontrola používá skutečný Xvfb/Openbox s malou náhradou Maven: ověří zachování
návratového kódu 7, chybu exportu, oddělené reporty, skryté zdroje, vynechání
starých `target` adresářů a argument s mezerou. Výsledky nechá v
`target/docker-ui-runner-checks/`. Na vlastním Linux filesystemu kontejneru také
ověří čtení reportů neprivilegovaným uživatelem; macOS bind mount tuto kontrolu
oprávnění nenahrazuje. Nenahrazuje skutečné herní testy.

Povinný CI job `functional` spouští stejný Compose příkaz. Nejprve ověří také
runner. Artifact
`tests-functional` obsahuje adresář nového kontejnerového běhu i při selhání;
`publish` zůstává závislý na úspěšné úplné sadě. [Workflow](../.github/workflows/dev-builds.yml)
je zdrojem přesné konfigurace a časového limitu.

Ověření **13. října 2026** na Apple Silicon v tomto amd64 kontejneru:
výchozí příkaz prošel s **683 herními a 64 funkčními kontrolami**, bez selhání,
chyb či vynechání, včetně TC05/TC06 a TC23. Běh `run-6BVsG8` trval 10:27;
všech 22 zdrojových fixtures zůstalo beze změny. Runner ověřil i nenulové
návratové kódy a export reportů při selhání. První neúspěšný běh, opravu limitu
heapu a konkrétní důkazy zachovává [R-0033](requirements/done/R-0033.md).

### Omezená alternativa Monocle

Rychlá cílená kontrola obou scénářů orby při vývoji:

```sh
mvn -B -ntp -pl microcol-test -am test \
  -Dtest=TC_18_F01_plow_field_test,TC_15_F05_plow_field_test \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dtestfx.robot=glass -Dtestfx.headless=true -Dprism.order=sw \
  -Dheadless.geometry=1600x1200-32 -Dprism.allowhidpi=false \
  -DsoundEnabled=false
```

Příznak `surefire.failIfNoSpecifiedTests=false` slouží jen pro závislé moduly,
které cílenou třídu neobsahují. V `microcol-test/target/surefire-reports/` musí
být čerstvé výsledky obou vybraných scénářů; cílený příkaz nenahrazuje celou sadu.
Tento cílený běh používá [Monocle 17.0.10](https://github.com/TestFX/Monocle#gradle)
kompatibilní s JavaFX 17. Monocle však nepodporuje přenosový režim `LINK`,
na kterém závisí dialog výběru množství zboží v TC05 a TC06. Úplnou sadu tedy
nespouštět s `testfx.headless=true`. Automatický test nehodnotí výtvarnou
kvalitu výsledných obrázků.

Profily `local` a `ci` vybírají tagy `ci, local`. Nové scénáře mají
`@Tag("local")`, stejně jako TC18. Surefire zahrnuje všechny třídy `TC_...`
včetně historického názvu TC12 bez přípony `_test` a při prázdném výběru
modulu selže. Testové nastavení se pro každý test kopíruje do
`microcol-test/target/test-homes/`; původní uložené hry a nastavení se nemění.

[Vývojový workflow](../.github/workflows/dev-builds.yml) spouští celou sadu
v samostatném jobu `functional` na Linuxu/JDK 17 s Xvfb a správcem oken Openbox při každém pushi na `main`.
Job `publish` závisí i na tomto výsledku. Selhání tedy zabrání vydání nové
vývojové distribuce; dřívější vydání zůstane dostupné. Reporty se ukládají jako
artifact `tests-functional` i při selhání. Publikace dokumentace má vlastní
workflow a sama o sobě neprokazuje průchod hry.

### Známé omezení prostředí

Dne 11. října 2026 start testu v pracovní kopii s cestou `Application Support`
selhal ještě před scénářem: JavaFX při načítání fontu z rozbalených resources
použilo doslovné `%20` v souborové cestě. Ověřování proto používá běžný checkout
bez mezer. Tato úprava testů nemění produkční `FontService`; případná oprava
načítání fontů musí mít vlastní regresi. Původní Monocle `jdk-11+26` navíc
selhávalo na rozhraní časovače JavaFX 17 a bylo nahrazeno kompatibilní verzí.

Na Linuxu nastavuje `MicroCol.configureJavaFx()` před startem JavaFX
`glass.disableGrab=true`. Stejný produkční vstup volá `@BeforeAll` funkčních
testů. Jde o dočasné obejití chyby GTK/Glass, kdy po drag/drop zůstává myš
zachycená u původního okna a nově otevřený modální dialog nedostává kliknutí.
Vypíná se explicitní zachytávání ukazatele v Glass, nikoli nativní drag/drop
nebo modalita dialogu; na ostatních platformách se toto nastavení nepoužívá.
Přepínač zpracovává
[GtkApplication](https://github.com/openjdk/jfx17u/blob/master/modules/javafx.graphics/src/main/java/com/sun/glass/ui/gtk/GtkApplication.java)
a [nativní grab_pointer](https://github.com/openjdk/jfx17u/blob/master/modules/javafx.graphics/src/main/native-glass/gtk/glass_general.cpp).
Při změně JavaFX toto obejití znovu posoudit, včetně přetažení mimo okno,
modálních dialogů a obvyklé sady UI scénářů. TC19/TC_17 chrání zrušený drag/drop
mimo okno a následující funkční přesun. Není dovoleno přidat přepínač pouze
do CI a ponechat hráčům jiné chování.

Xvfb dodává displej, nikoli správu zaměření oken. Funkční job proto spouští
Openbox: bez správce oken po opakovaném otevření modálního dialogu klávesnice
mířila na zakázané hlavní okno, přestože JavaFX hlásilo dialog jako zaměřený.
Page object po OK, Zrušit i Escape čeká na skutečné zavření původního okna.
Tato kontrola doplňuje aserce zásob; jejich nezměněná hodnota sama o sobě
nedokazuje, že zrušení proběhlo.

### Ověření při zavedení pravidel — 11. října 2026

Na macOS/JDK 17 prošlo 603 herních testů a cíleně TC18/TC15. První úplný
běh skutečně vybral 27 testů; odhalil neplatnou DTD textů misí, závod při
vyhledání tlačítka po konci tahu a omezení Monocle pro TC05/TC06. Texty misí
i čekání byly opraveny a TC03 poté prošel. Oba jazyky textů mise mají nový
modelový test ověřený před opravou i po ní.

Na Linuxu/JDK 17 s Xvfb následně prošlo **27/27 funkčních testů** (21 metod
v 18 třídách TC a šest základních testů) a **603/603 herních testů**, bez
selhání, chyb i vynechání. Ověřeny stažené Surefire XML v artifactu
`tests-functional` z [běhu 38156401646](https://github.com/jajir/microcol/actions/runs/38156401646),
revize `951e82ae824f55084f7af65edafbdc1948293997`. Pro další změny vždy
zkontrolovat čerstvý běh, nikoli tento historický výsledek.

Místní nativní běh na macOS v této relaci nedokázal načíst hru kliknutím;
nepovažuje se za úspěšný výsledek. Pro úplnou sadu slouží Linux/Xvfb a jeho
uložené reporty. [Záznam delegace](ai/local-qwen.md#functional-plowing-regression-2026-10-11)
rozlišuje práci Qwenu, opravy hlavního agenta a místní důkazy.

## Dokončení a záznam výsledku

### Přesun asercí přístavu — 12. října 2026

**Historický výsledek větve main, nahrazený pravidlem pojmenovaných verifikací
integrovaným z PR #44.** Následující kontroly popisují tehdejší revizi.

`PageComponentPanelDock` nyní pouze vrací počet lodí; `ColonyScreen`
a `EuropePortScreen` jej zpřístupňují bez očekávané hodnoty. Původní tři
kontroly jedné lodi jsou přímo v `TC_01_embark_disembark`, `TC_06_moving_goods`
a `TC_06_moving_goods_buy_cargo_and_move_it_to_occupied_cargo_slot`.
Selektory, akce, fixtures, ID i očekávání zůstaly stejné; ostatní historické
verifikační metody nebyly v tomto úkolu převáděny.

Kontrola zdrojů potvrdila odstranění všech použití `verifyNumberOfShipsInPort`,
absenci asercí/verifikace v komponentě a zachování tří očekávání v testech.
Sdílený formatter prošel všech pět změněných Java souborů se zachováním tokenů
a idempotencí. Po aplikaci opraveného výstupu Qwen prošel na macOS/JDK 17
s Monocle cílený **TC01: 1 test, 0 selhání, 0 chyb, 0 přeskočených**.
Použit byl výše uvedený cílený příkaz s výběrem
`-Dtest=TC_01_F01_embark_disembark_test`; log je `target/dock-qwen-tc01.log`.
TC06 potřebuje nativní režim LINK; oba jeho scénáře a celou sadu ověřuje
povinný Linux/Xvfb job publikované revize. Lokální TC01 jej nenahrazuje.
[Záznam Qwen](ai/local-qwen.md#dock-assertion-move-2026-10-12) rozlišuje neúspěšné
pokusy, opravu workerem a aplikaci/formátování hlavním agentem.

### Kontrolní seznam

Před přijetím změny reviewer ověří:

- Každé změněné chování má dohledatelné ID scénáře a konkrétní očekávání;
  relevantní zamítnutí, zrušení, hraniční stav a save/load nejsou opomenuty.
- Test zůstal přímočarý a čitelný, používá společné page objects a neobchází UI.
- Změna vzhledu nezměnila herní očekávání a relevantní scénáře skutečně běžely.
- Čerstvé Surefire XML obsahuje zamýšlené třídy a metody, skutečné počty,
  nula selhání a chyb a žádné nezdůvodněné přeskočení. Zelený Maven s nulou
  vybraných scénářů není úspěšné ověření.

Do výsledku úkolu stačí uvést revizi, změněné chování, ID scénářů, příkaz,
JDK a prostředí, skutečné počty a cestu k reportům. Neprovedené kontroly nebo
nefunkční testovací prostředí uvést jako otevřenou překážku. Hlavní agent
ověřuje skutečný běh i u testů napsaných [místním workerem](ai/local-qwen.md).


## R-0163 missionary ordination — 2026-10-19

1. Přijato v izolované dávkové větvi, zatím nepublikováno. Vysvěcení je zdarma
   a bez spotřeby pohybu na evropském molu nebo před vlastní kolonií s kostelem
   či katedrálou. Zachovává identitu, specializaci a vybavení včetně sluhů a
   trestanců. Přidělený pracovník musí nejprve opustit práci. Kaple nestačí.
2. `Model.getOrdinationProblem` a `ordainMissionary` sdílejí ověření vlastníka,
   živé hry, fáze, umístění a rozkazu před změnou. `UnitPo.missionary` ukládá
   roli; scénáře a tvůrci dat byly aktualizovány bez migrace starých uložených her.
   `hasEffectiveMissionaryExpertise` čte jezuitu nebo Brébeufa živého vlastníka.
   Nativní mise, konverze, denunciace a podněcování zůstávají dalšími požadavky.
3. Haiku připravilo a implementovalo model/UI, Sonnet doplnil chybějící TC96
   a opravil neplatné použití EnumSource pro UnitType. Po třech pokusech uživatel
   výslovně povolil dokončení Codexem: model nyní přijímá i katedrálu (kontrola
   budovy porovnává přesný typ) a český UI test hledá správné „Vysvětit“.
   Původní selhání zůstávají evidována v completion záznamech
   `20261019T120849Z-54afc1dac06a`, `20261019T122151Z-c37ff019b993`
   a `20261019T122354Z-fb3ea7d1e455`.
4. Finální Docker/Xvfb běh `20261019T123100Z-deb767e1dafa`:
   **2 551 modelových testů a 14 UI/provozních kontrol**, nula chyb, selhání
   a přeskočení. Výběr `*Test,!RivalTradeCycleTest,TC_96*` zahrnuje 35 běhů
   MissionaryOrdinationTest, fázi přípravy tahu a všech pět TC96 scénářů.
   R-0091 RivalTradeCycleTest zůstává samostatným otevřeným blokátorem.
5. Předchozí diagnostický běh ověřil dalších **30 souvisejících UI scénářů**
   TC25/27/29/47/56/67 a tři kontroly zdrojového kontraktu. Finální oprava
   změnila pouze přijetí katedrály a český očekávaný text TC96; související
   vybavení, přeprava, imigrace, Kongres a náboženská produkce se nezměnily.
6. `TC_96_F87_missionary_ordination_test` ověřuje skutečná menu a dialogy:
   molo (zrušení, Escape, potvrzení, opakování, Continue), odmítnutí kaple,
   kostel (změna přidělené práce před potvrzením), katedrálu a Brébeufa
   před/po, český dialog a Enter. Modelové kontroly pokrývají sociální třídy,
   vybavení a přesný pohyb, přidělenou práci, náklad, moře, rozkaz, změnu
   vlastníka a persistence. Samostatná větev cizí kolonie pod vlastní jednotkou
   nemá přímý scénář; vlastnictví kontroluje sdílená operace.
7. Sdílený formatter: 48 Java souborů, shoda tokenů a idempotence PASS.
   Devět dokumentačních testů a strict MkDocs prošly. Celá UI regrese a
   publikace jsou odloženy na integraci deseti přijatých požadavků dávky.

## Z01 — bezpečné vyložení nákladu

**Přijaté ověření — 11. října 2026:**
[CI běh 19](https://github.com/jajir/microcol/actions/runs/38176961016)
na revizi `c2e24ba45d8e5530664a89e784c4572694e95283`, JDK 17,
Linux/Xvfb/Openbox, nativní Glass. Čerstvé Surefire XML v artefaktu
[tests-functional](https://github.com/jajir/microcol/actions/runs/38176961016/artifacts/11716435850)
potvrzuje:

| Sada | Vybráno / prošlo | Selhání / chyby / vynecháno |
| --- | --- | --- |
| Herní modul včetně modelových hranic | 663 / 663 | 0 / 0 / 0 |
| Všechny funkční scénáře | 44 / 44 | 0 / 0 / 0 |
| Z toho nová TC19 | 17 / 17 | 0 / 0 / 0 |

Prošly také kontroly launcheru a balíčky macOS ARM64, macOS Intel a Linux x86-64.
Testovací build je `dev-19-c2e24ba45d8e`; například
[macOS ARM64 ke stažení](https://github.com/jajir/microcol/actions/runs/38176961016/artifacts/11715904023).
Větev PR správně vynechala publikaci release. Následné doplnění této evidence
mění jen dokumentaci; ověřený herní a testovací kód zůstává shodný s uvedenou revizí.
Celé UC-023/036 tím není přijato: další směry, vozy, výroba a potraviny mají
v katalogu vlastní zbývající kritéria.

Implementace ve větvi `codex/rules-warehouse-unloading` (11. října 2026) pokrývá
část UC-023/UC-036: vlastní loď → vlastní sklad, kontrolu zdroje, vlastníka,
polohy, druhu, kladného množství a kapacity. `ColonyWarehouse.getUnloadableGoods`
je neměnný náhled; `moveToWarehouse` ověřuje celý požadavek znovu a odmítne jej
výjimkou před změnou inventářů. UI používá stejný náhled při přetažení i po
návratu z dialogu. Automaticky se nic nezahazuje ani nezmenšuje již potvrzený
požadavek.

[TC19 / F14](../microcol-test/src/test/java/org/microcol/test/TC_19_F14_warehouse_unloading_test.java)
má vlastní [fixture](../microcol-test/src/test/scenarios/F14-warehouse-unloading.microcol);
F05 sdílená s TC15 se nemění. Metody jsou samostatné lineární příběhy podle
TC18, s `@Start`, komentovanými anglickými kroky, page objects a explicitními
inventárními asercemi.

| Metoda v TC19 | Kontrolovaný výsledek |
| --- | --- |
| TC_01 | Návrh 1 kusu z nákladu 100 do skladu 99/100; potvrzení 100/99, opakované odmítnutí, save/load stejných zásob. |
| TC_02 / TC_03 | Viditelné Zrušit i Escape zachovají 99/100. |
| TC_04 / TC_05 | Plný sklad vypíše vysvětlení; běžné vyložení 20 do 50 vytvoří 70 a prázdný zdroj. |
| TC_06 | Skutečná klávesnice nemůže potvrdit nulu ani překročit volnou kapacitu. |
| TC_07 | Přesné částečné množství 17 z 20; opakované otevření téhož dialogu s vysvětlením a bez něj. |
| TC_08 / TC_09 | Změna kapacity nebo vyprázdnění nákladu při otevřeném dialogu bezpečně odmítne potvrzení. |
| TC_10 / TC_11 / TC_12 / TC_13 | Změna polohy, vlastníka, druhu nebo snížení zásoby při otevřeném dialogu se odmítne bez dalšího přesunu. |
| TC_14 / TC_15 / TC_16 | Zánik kolonie nebo lodi a odplutí mimo mapu během dialogu nesmějí připsat ani odebrat náklad. |
| TC_17 | Přetažení mimo okno zachová zásoby; následující platné přetažení stále funguje. |

U scénářů změny během dialogu test výslovně simuluje **konkurenční událost**
platnou modelovou operací na FX vlákně. Testované vyložení nadále provádí
hráč přetažením a potvrzením v UI; simulace neobchází tuto akci. Syntakticky
nemožné vstupy z UI, například `null` a záporný objekt `Goods`, chrání modelové
testy. Prázdný či jednotkou obsazený zdroj nemůže být přetažen jako zboží.

[ColonyWarehouseUnloadTest](../microcol-game/src/test/java/org/microcol/model/ColonyWarehouseUnloadTest.java)
ověřuje všechny nepotravinové druhy při kapacitách 100/200/300, neměnný náhled,
atomické odmítnutí a zachování nákladu. Nové Zrušit používá také sdílený dialog
v Evropě: rozšíření TC06 kontroluje, že zrušený přesun mezi prostory nevytvoří
náklad s nulovým množstvím. Obchodní ceny ani evropská pravidla se nemění.

**Historie oprav a ověřování:** macOS/JDK 17: 661 herních testů, 0 selhání,
0 chyb, 0 vynechání. Reprodukční test cizího nákladu na původní revizi
`89354999954f8a1bfa4c9a6ac0030751056f63e2` selhal očekávaně: chyběla výjimka.
První místní Monocle běh vybral osm nových scénářů (1 selhání, 7 chyb):
pomocník pro výběr znovu klikl na automaticky vybranou loď a odznačil ji.
Page object nyní kliká jen při potřebě výběru a vybraný stav výslovně ověří.
Cílený nativní macOS běh nedokončil vstup do hry.
Po opravě výběru Monocle otevřel dialog a vytvořil screenshot, ale narazil
na chyby vnořené smyčky drag/drop (`dndGesture.dragboard`, `wrong EventLoop`):
11 vybraných, 1 prošlý, 1 selhání, 9 chyb, 0 vynechání.
První [Linux CI](https://github.com/jajir/microcol/actions/runs/38172274558)
na revizi `09887b8c` ověřilo všech 27 původních testů, ale z 13 nových prošly
jen dva (celkem 40 vybraných, 29 prošlých, 11 selhání, 0 chyb/vynechání).
Po otevření modálního okna během nativního drag/drop zůstával vstup zablokovaný.
Vyložení nyní čeká na `DRAG_DONE` zdroje a dialog otevře přes `Platform.runLater`.
Po této opravě prošlo na macOS/Monocle **14/14** scénářů TC19 bez selhání,
chyb a vynechání; TC_07/TC_13 vyžadují nativní režim LINK, proto v tomto
cíleném výběru nejsou.

Druhý [Linux CI](https://github.com/jajir/microcol/actions/runs/38173009939)
na `7c48ba8a` znovu ověřil 661 herních a 27 původních UI testů, ale TC19
selhal: 43 vybraných, 15 selhání, 1 chyba, 0 vynechání. První dialog nepřijal
kliknutí a zůstal otevřený; ostatní selhání v této třídě byla důsledkem
zablokovaného vstupu. Místní Linux/Xvfb reprodukce potvrdila chybu nativního
zachytávání myši. Před samotným kliknutím byl dialog viditelný, zaměřený a
tlačítko povolené. Čekání ani změna robota Glass → AWT problém neodstranily.
Nastavení `glass.disableGrab=true` reprodukční TC_13 opravilo; nyní se používá
ve skutečné hře i testech podle výše popsaného kontraktu. Tento cílený výsledek
ještě nenahrazoval přejímku celé sady se správcem oken.

Nativní TC05 navíc odhalil souběžné čtení nastavení při překreslení a zápis
na vlákně tahu. `SettingService` nyní synchronizuje čtení, zápis i celou
operaci read–modify–write. Oba nové testy v `SettingServiceConcurrencyTest`
před opravou selhaly; po opravě prošlo všech devět cílených testů nastavení.
Nejde o potlačení chyby v čekání TestFX.

Místní nativní běh s Openbox vybral 22 scénářů: TC05 3/3, TC06 2/2,
TC19 16/17 (jedno selhání, žádná chyba ani vynechání). TC19/TC_07 odhalil,
že page object kliká na střed celého slideru, který u popsané stupnice neleží
na ovladači; šipky pak ovládaly tlačítka. Pomocník nyní kliká na skutečný
úchyt slideru a ověřuje zaměření i zvolenou hodnotu před potvrzením.
Cílené opakování TC19/TC_06 a TC_07 pak prošlo 2/2, bez selhání, chyb a
vynechání. Ani tento cílený výběr ještě nenahrazoval úplné CI.

Třetí [CI běh](https://github.com/jajir/microcol/actions/runs/38176111274)
na `9fdb0813` ověřil **663 herních testů** a všechny tři nativní balíčky.
Z **44 UI scénářů prošlo 43**, jeden selhal, žádný neměl chybu ani nebyl vynechán.
TC19/TC_17 kontroloval následující přesun bez potvrzeného dokončení nativních
gest. Page object nyní u zrušeného i platného přetažení čeká na skutečnou
událost `DRAG_DONE` původního zdroje a následnou FX frontu. Samotné puštění
tlačítka a čekání na FX frontu tuto nativní fázi nemusí ukončit. Report sám
nerozlišuje, které z obou gest ještě probíhalo.
Očekávání zásob 50/20 po zrušení a 70/0 po platném přesunu zůstává zachováno.
Po čekání na zrušené gesto prošla místně celá TC19: 17/17. Po doplnění
stejné kontroly platného přetažení prošlo šest cílených scénářů (TC19/TC_01,
TC_13, TC_17 a všechny tři TC05), bez selhání, chyb a vynechání.
Následující úplný CI běh 19 prošel podle přejímací tabulky výše.

Nezávislá kontrola doplnila existenci kolonie a lodi v živém modelu.
Oba reprodukční modelové testy před opravou selhaly (chybějící výjimka),
po opravě prošly a UI scénáře TC_14/TC_15 ověřily odmítnutí přes skutečný dialog.
TC_16 chrání také zdroj mimo mapu. Místně byly prohlédnuty screenshoty
omezeného množství, plného skladu a změny stavu: text i tlačítka jsou viditelné.

Screenshoty dialogů se ukládají do ignorovaného
`microcol-test/target/warehouse-qa/` a CI je přiloží k `tests-functional`.
Slouží k vizuální kontrole textu a ovládání, nenahrazují herní aserce.
Samostatný [PR #44](https://github.com/jajir/microcol/pull/44) míří do `main`;
do sloučení nepublikuje nový vývojový release
ani web. Potraviny, produkční zahazování a kolonijní UI vozů zůstávají mimo Z01.

## PR #44 — přesun asercí doku

**Historická změna 12. října 2026, nahrazená novým pravidlem níže:**
Na revizi `e229682e` `PageComponentPanelDock` neobsahoval aserce,
verifikační metody ani JUnit importy. Počet lodí vrací přes
`getNumberOfShipsInPort()`; TC01 obsahuje jednu a TC06 dvě přímé aserce počtu.
`isUnitInPortSelected(...)` pouze čte stav; všech 18 volání výběru lodi
v 17 scénářích TC19 bezprostředně následuje explicitní aserce, včetně načtení
uložené hry. Podmíněné kliknutí a čekání na FX frontu zůstávají beze změny.
Scénáře, fixtures, komentované kroky a očekávané herní výsledky se nemění.

Jde o ochranu UC-023, UC-036 a UC-119, nikoli nové herní chování. Kontrola
zdrojů a společný formatter prošly pro všech šest změněných Java souborů.
Lokálně na macOS/JDK 17 prošel TC01 s Monocle: 1 test, 0 selhání, chyb
a vynechání; Maven přitom zkompiloval všechny testové třídy. Příkaz byl
`mvn -B -ntp -pl microcol-test -am test -Dtest=TC_01_F01_embark_disembark_test -Dsurefire.failIfNoSpecifiedTests=false -Dtestfx.robot=glass -Dtestfx.headless=true -Dprism.order=sw -Dheadless.geometry=1600x1200-32 -Dprism.allowhidpi=false -DsoundEnabled=false`.
Prošlo také všech 9 dokumentačních kontrol a přísné sestavení MkDocs.

**Ověřeno 12. října 2026:** [CI běh 24](https://github.com/jajir/microcol/actions/runs/38242771215)
na revizi `e229682ecd0ef5f492534bd6192e804bd8bfc13b`, JDK 17,
Linux/Xvfb/Openbox a nativní Glass, úplným příkazem z oddílu
[Spouštění a ochrana publikace](#spousteni-a-ochrana-publikace).
Stažené čerstvé Surefire XML v artefaktu
[tests-functional](https://github.com/jajir/microcol/actions/runs/38242771215/artifacts/11744026846)
potvrzuje 663 herních testů a 44 funkčních scénářů; ve všech sadách je
0 selhání, 0 chyb a 0 vynechání. Dotčené TC01 prošlo 1/1, TC06 2/2
a TC19 17/17. Místní kopie reportů je v `target/pr44-ci-evidence/`.

Prošly také kontroly launcherů a všechny tři nativní balíčky.
Testovací build `dev-24-e229682ecd0e` je dostupný například pro
[macOS ARM64](https://github.com/jajir/microcol/actions/runs/38242771215/artifacts/11744106186).
[Dokumentační CI](https://github.com/jajir/microcol/actions/runs/38242774635) prošlo.
Publikace release a webu byla na větvi PR správně vynechána. Toto následné
doplnění evidence mění pouze dokumentaci, nikoli ověřený Java kód.
PR zůstává otevřený; existující konflikty s `main` tento přesun neřeší.

## PR #44 — pojmenované verifikace

Nové zadání z 12. října 2026 nahrazuje předchozí směr přesunu asercí.
Všech 19 tříd TC používá pojmenované verifikační metody místo přímých asercí;
počty, zásoby, typy zboží, prázdný náklad, uložený soubor, zachované reference
na kolonistu i zničený zdroj zůstávají ověřené. TC19 stále ověřuje výběr lodi
po všech 18 výběrech včetně opětovného načtení. ID scénářů ani fixtures se nemění.
Byly odděleny také kontroly slideru, existence lodi a připravenosti dialogu;
kontrola nativního `DRAG_DONE` stále selže po pěti sekundách.

Místně na macOS/JDK 17 prošly TC01 a TC18 (každý 1/1) s Monocle a všechny
tři kontroly `PageVerificationContractTest`, bez selhání, chyb a vynechání.
Maven zkompiloval všechny testové třídy. Použit byl stejný cílený příkaz
výše s výběrem `-Dtest=PageVerificationContractTest,TC_01_F01_embark_disembark_test,TC_18_F01_plow_field_test`;
po doplnění třetí kontroly byl samostatně znovu spuštěn
`-Dtest=PageVerificationContractTest`. Společný formatter ověřil 32 změněných
Java souborů se zachovanými tokeny, komentáři a idempotencí. Prošlo všech
9 dokumentačních kontrol, přísný MkDocs a validátor local-worker skillu.

**Úplná regrese ověřena 12. října 2026:**
[CI běh 25](https://github.com/jajir/microcol/actions/runs/38247066928)
na revizi `450a7fdc6b7e38b734c750fe204547f9956804cb`, JDK 17,
Linux/Xvfb/Openbox, nativní Glass a úplný Maven příkaz uvedený výše.
Stažené Surefire XML z
[tests-functional](https://github.com/jajir/microcol/actions/runs/38247066928/artifacts/11745963820)
potvrzuje **663 herních testů, všech 44 UI scénářů a 3 kontroly
PageVerificationContractTest**, vždy 0 selhání, 0 chyb a 0 vynechání.
TC19 prošlo všech 17 scénářů. Modul `microcol-test` tedy nově vykazuje
47 testů, nikoli 47 hráčských scénářů. Místní kopie XML je v
`target/pr44-verifier-ci-evidence/`.

Prošly také všechny tři nativní balíčky, kontroly launcherů a
[dokumentační CI](https://github.com/jajir/microcol/actions/runs/38247070083).
Testovací build `dev-25-450a7fdc6b7e` lze stáhnout například pro
[macOS ARM64](https://github.com/jajir/microcol/actions/runs/38247066928/artifacts/11745613581).
Publikace release/webu je na větvi PR vynechána. Následné doplnění tohoto
záznamu mění jen dokumentaci, ne ověřený Java kód. PR není sloučený;
existující konflikty s `main` zůstávají mimo tento refaktor.

## PR #44 — sjednocení s main

Dne **12. října 2026** byly vyřešeny konflikty při začlenění `main`
(`92a22efa`) do větve PR (`972e1007`). Aktuální pojmenované page verifikace
zůstaly zachované; starší přesun asercí přímo do TC je označený jako historie.
Záznamy obou větví o Qwen zůstávají dostupné společně s novými časovými limity
a postupem cílených oprav. Pět dotčených Java souborů je obsahově shodných
s hlavou PR před sloučením; žádná herní očekávání ani fixtures se nemění.

Místně na macOS ARM64/JDK 17 prošlo **663 herních testů**, **TC01 1/1**
a **PageVerificationContractTest 3/3**, bez selhání, chyb a vynechání.
Příkazy odpovídají výše uvedeným běhům; cílený výběr byl
`-Dtest=PageVerificationContractTest,TC_01_F01_embark_disembark_test`.
Logy jsou `target/merge44-game.log` a `target/merge44-tests.log`, čerstvé XML
v příslušných `target/surefire-reports/`. Všech pět Java souborů prošlo
sdíleným Eclipse formatterem bez změny obsahu. Prošlo také devět kontrol
dokumentace a přísný MkDocs build. Tato místní kontrola nenahrazuje úplnou
Linux/Xvfb regresi ani ověření distribucí v CI výsledné revize na main.

## Z02 — pojmenování a identita kolonie

Kontrakt a obecná rozhodnutí jsou v [plánu Z02](implementation-plan.md#z02-zalozeni-pojmenovane-kolonie).
Dotčené UC-031, UC-114 a UC-119 pokrývá původní TC02 a nová
`TC_20_F02_colony_names_test` se stejnou F02 fixture. JSON fixtures dostávají
jen explicitní ID a další volné ID; herní stav, vlastníci, zásoby, jednotky
a původní ID scénářů zůstávají zachované.

| Scénář | Očekávaný výsledek |
| --- | --- |
| TC02/TC_02 | Potvrzení navrženého jména otevře novou kolonii; původní kontroly pracovníků a produkce zůstávají. |
| TC20/TC_01 | Opakované návrhy, změna konceptu, Zrušit a Escape nezaloží kolonii ani nespotřebují ID/pohyb. |
| TC20/TC_02 | Unicode jméno, potvrzení Enter, správná nová kolonie a stejný zakladatel/ID po save/load. |
| TC20/TC_03 | Duplicitní jméno má červené pole, vysvětlení a neaktivní Uložit; přejmenování vyžaduje potvrzení. |
| TC20/TC_04 | Prázdné a 65znakové jméno se odmítne; 64 znaků se přijme; obsazené pole nelze znovu založit. |
| TC20/TC_05 | Simulovaný souběžný přesun zakladatele bezpečně odmítne staré potvrzení. |

Modelové testy `ColonyNamesTest` a `ColonyIdentityTest` chrání Unicode/NFC,
NBSP, délku v code points, neplatné surrogate sekvence, globální duplicity,
generování, oprávnění, pracovníky, JSON round-trip, nerecyklování zaniklých
ID a zprávy s historickým jménem. Chybějící či chybná identita se odmítá;
kompatibilita se starým save formátem není požadována.

**Lokálně ověřeno 12. října 2026:** JDK 17/macOS, `mvn -B -ntp -pl
microcol-game -am test`: **673 testů**, bez selhání, chyb a vynechání.
TC20 prošlo **5/5**, TC02 **1/1** a kontrakt page verifikací **3/3** s
Monocle (`-Dtest=TC_20_F02_colony_names_test,TC_02_F02_founding_colony_test,PageVerificationContractTest`
a headless parametry popsanými výše). Logy: `target/z02-game.log`,
`target/z02-targeted-final.log`; čerstvé XML jsou v `target/surefire-reports`
příslušných modulů. Tyto cílené běhy nenahrazují úplnou Linux/Xvfb regresi.

První UI běh odhalil, že původní široký panel tlačítek překrýval jméno;
jméno je nyní nad ním, s omezenou šířkou a tooltipem pro dlouhé názvy,
aby nepřekrývalo ostatní ovládání. Testovací clipboardové zkratky v místním
headless prostředí neměnily text; page helper nyní používá klávesy a textový
vstup TestFX. Neúspěšné kroky byly znovu spuštěny se stejnými očekáváními.
Screenshot `microcol-test/target/warehouse-qa/duplicate-name.png` byl
prohlédnut: červené pole, úplné vysvětlení, Zrušit a neaktivní Uložit jsou
čitelné. Jde o kontrolu implementátorem, nikoli studii s hráči.

**První úplné CI na `d598be1f`:** [běh 27](https://github.com/jajir/microcol/actions/runs/38262477833)
ověřil 673 modelových testů a všechny tři distribuce. Z 52 testů modulu
`microcol-test` prošlo 49; TC20/TC_01 četlo po kliknutí na Generovat ještě
původní návrh a otevřený dialog zablokoval následující dva scénáře. Žádný
test nebyl vynechán. Všechny původní UI scénáře prošly.

Lokální Linux/Xvfb/Openbox diagnostika ukázala skutečné události tlačítka a
správné návrhy; s diagnostikou prošel izolovaný TC20/TC_01 i všech pět TC20.
Z původního CI samotného nelze rozlišit opožděné a nedoručené kliknutí.
Page helper proto po jednom kliknutí čeká nejvýše pět sekund na skutečnou
změnu textu, nikoli pouze na vyprázdnění FX fronty. Nedojde-li ke změně,
test selže; nekliká znovu a očekávaná jména se nemění. Diagnostický kód není
součástí aplikace ani PR. Logy jsou v `target/z02-linux/`.

Po této změně a bez diagnostických úprav prošlo lokálně na Linuxu/JDK 17,
Xvfb/Openbox a nativním Glass **TC20 5/5, TC02 1/1 a page kontrakt 3/3**, bez
selhání, chyb a vynechání (`target/z02-linux/verified.log`). Použit byl stejný
cílený výběr, ale `-Dtestfx.headless=false`. Sdílený formatter znovu ověřil
všech 32 dotčených Java souborů včetně zachování tokenů a idempotence.

**Úplné CI po opravě, 12. října 2026:** [běh 28](https://github.com/jajir/microcol/actions/runs/38264716437)
na revizi `d494f18de25f310f5d07b3f8ed9591425b6428a3` prošel. Stažené
Surefire XML z [tests-functional](https://github.com/jajir/microcol/actions/runs/38264716437/artifacts/11753973451)
potvrzuje **673 herních testů a 52 testů modulu microcol-test**, vždy
0 selhání, 0 chyb a 0 vynechání. Druhý součet zahrnuje 43 pojmenovaných
TC scénářů (včetně všech pěti TC20), pět `SimpleOperationsTest`, jeden
`EnvironmentTest` a tři kontroly `PageVerificationContractTest`; nejde o
52 hráčských scénářů. Původní selhávající scénáře byly skutečně znovu ověřeny.
Místní kopie důkazů je v `target/z02-ci-verified/`. Screenshot duplicitního
jména z tohoto nativního Linux běhu byl prohlédnut: červené pole,
vysvětlení, Zrušit a neaktivní Uložit jsou čitelné.

Prošly také kontroly launcherů, všechny tři nativní distribuce a
[dokumentační CI](https://github.com/jajir/microcol/actions/runs/38264722933).
Testovací build `dev-28-d494f18de25f` je dostupný například pro
[macOS ARM64](https://github.com/jajir/microcol/actions/runs/38264716437/artifacts/11753909068).
Publikace release a webu je na větvi PR záměrně vynechána; PR #45 není
sloučený. Následné doplnění tohoto záznamu mění pouze dokumentaci,
nikoli ověřený kód nebo fixtures.

## R-0011 — hranice modelu

1. Dne 13. října 2026 prošlo na macOS/JDK 17.0.18 všech **683 testů**
   `microcol-game` příkazem `mvn -B -ntp -pl microcol-game -am test`.
   ArchUnit kontroluje produkční model včetně podbalíčků bez výjimek;
   konkrétní kontrakt vlastní [R-0011](requirements/done/R-0011.md).
2. Nové regrese chrání identifikátory a oba překlady zpráv o konci hry
   a nezávislosti, zahájení a jednorázové objevení kontinentu v tutoriálu,
   pořadí stejně hodnocených cílů AI a limit tří kontinentů.
3. Cílený běh `mvn -B -ntp -pl microcol-test -am test
   -Dtest=TC_03_F03_declare_independence_test,TC_12_F11_start_free_game,TC_20_F02_colony_names_test,PageVerificationContractTest
   -Dsurefire.failIfNoSpecifiedTests=false` s dokumentovanými přepínači Monocle
   prošel: **10 kontrol, 0 selhání, 0 chyb, 0 vynechaných**. To zahrnuje
   sedm hráčských scénářů a tři kontroly zdrojového kontraktu.
4. Jde o regrese stávajících cest UC-080/082, UC-113, UC-115 a UC-119,
   bez změny pravidel, vzhledu či textů. Příslušné UC tím nejsou celé přijaté;
   zprávy v jednotkovém testu samy neověřují kompletní konec hry UC-111.
5. Následný [CI běh revize dcc1cfa1](https://github.com/jajir/microcol/actions/runs/38350505498)
   prošel na první pokus na Linuxu/JDK 17 s Xvfb/Openbox: **683 herních testů
   a 52 funkčních kontrol**, bez selhání, chyb a vynechání. Prošly i kontroly
   sestavení a všechny tři nativní platformy; publikaci a přesný build
   zaznamenává [R-0011](requirements/done/R-0011.md).

<a id="free-play-audit-2026-10-13"></a>
## Audit existujících cest volné hry — 13. října 2026

1. Zadání zpřesňuje R-0012–R-0025 a ověřuje současné UI. Nemění produkční
   pravidla ani nerealizuje celé budoucí návrhy. Testy výslovně označené jako
   pozorování mezery neslouží k přijetí požadovaného cílového chování.
2. Nové scénáře používají F15/F16 a pojmenované page verifikace. TC21 ověřuje
   obrazovky a čistý profil, TC22 dostupnost evropských a kolonijních operací,
   TC23 plavbu a obnovu existujícího autosave. Starý ignorovaný autosave v
   místním adresáři fixture nesmí určovat výchozí stav: TC21 jej odstraňuje
   pouze ze své izolované testovací kopie, TC23 tam připraví známou F16.
3. Podrobnosti hráčských výsledků a otevřených otázek vlastní jednotlivá R.
   Výjimka obrázkového provideru v TC22/TC_04 je diagnostikou nedostupného
   vozu, nikoli úspěšným UI vyložením. Skrytá stavební fronta se neobchází
   přímou změnou modelu. TC21/TC_05 a TC23/TC_02 dokumentují rozdíl mezi
   uložením na začátku tahu a očekávaným uložením při vstupu do menu.

| Cesta | Scénář | Rozsah |
| --- | --- | --- |
| Evropa, report tahu, statistiky, kolonie | TC21/TC_01–TC_04 | Kliknutí a existující klávesy; klávesa pro otevření kolonie chybí. |
| Menu a Continue | TC21/TC_05, TC23/TC_02 | Samostatně čistý profil a existující autosave; požadované uložení při odchodu dosud chybí. |
| Výbava a nákup v Evropě | TC22/TC_01–TC_02 | Molo nemá vybavovací nabídku; úplný nákupní seznam nemá odborného kováře. |
| Cestující | TC22/TC_03, TC01 | Nalodění/vylodění na molu a mezi kolonií, lodí a pobřežím. |
| Vůz a stavba | TC22/TC_04–TC_05 | Chybí napojení obrázku vozu, stavební ovládání je skryté. |
| Atlantik a uložená plavba | TC23/TC_01 | Aktuální okamžitý odjezd při vstupu na HIGH_SEA, zachování nákladu a jiný návratový bod. |

Lokální ověření na macOS/JDK 17:

1. Cílený Monocle běh vybral TC21, TC22, TC23, TC01, TC12 a
   PageVerificationContractTest: **17 kontrol, 16 úspěšných, 1 chyba,
   0 vynechaných**. TC23/TC_01 narazil na výjimku Monocle
   `ClipboardAssistance` při drag-and-drop. Ostatní nové cesty včetně obou
   stavů autosave prošly. Následná kontrola fixture opravila cestujícího na
   legální výbavu 80 nástrojů bez mušket; konečný stav ověřuje nativní brána.
2. Úplný příkaz `mvn -B -ntp -pl microcol-test -am clean test -P ci
   -Dtestfx.robot=glass -Dtestfx.headless=false -Dprism.order=sw
   -DsoundEnabled=false` v samostatném checkoutu bez mezer: **683 herních
   testů prošlo**; funkční sada vybrala 64 kontrol, měla 56 selhání a 2 chyby,
   bez skipů. Diagnostika plochy potvrdila zamčený Mac; běžné načtení hry
   nepřijímalo kliknutí ani v nezměněných TC. Tento běh není přejímkou UI.
3. Sdílený formatter prošel všech pět nových Java souborů, zachoval tokeny
   a při opakování nic nezměnil. Prošlo 9 dokumentačních kontrol a strict
   MkDocs; číslované kapitoly byly zkontrolovány ve webovém náhledu.
4. Konečnou UI přejímku a publikaci musí potvrdit Linux/JDK 17/Xvfb/Openbox
   v povinném [vývojovém workflow](../.github/workflows/dev-builds.yml).
   Lokální překážky se neskrývají pomocí skipů, retry ani slabších asercí.
   Logy jsou v `~/Library/Logs/Microcol/freeplay-audit-2026-10-13/`.

### Výsledná nativní přejímka a publikace

1. Revize [64927cde](https://github.com/jajir/microcol/commit/64927cde2144b64ac23797e146e919d94fccf01d)
   prošla 13. října 2026 [Linux CI](https://github.com/jajir/microcol/actions/runs/38362888217):
   **683 herních testů a všech 64 funkčních/kontrolních testů**, bez selhání,
   chyb nebo vynechání. To zahrnuje **12 nových scénářů TC21–TC23**, původní
   TC01/TC06/TC12/TC19 a tři source-contract kontroly. Oba TC23 prošly včetně
   plavby do Evropy, pokračování z uloženého průběhu a návratu s nákladem.
2. Nativní běh vyřešil místní překážku Monocle a zamčené plochy, nikoli
   popsané herní mezery. Continue v čistém profilu, uložení při vstupu do menu,
   výbava na evropském molu, odborník, vůz, skrytá fronta a návrat na původní
   místo zůstávají následnými opravami. Cílová pravidla nebyla oslabena.
3. Prošly všechny tři nativní balíčky a job `publish`. Vydání
   [dev-39-64927cde2144](https://github.com/jajir/microcol/releases/tag/dev-39-64927cde2144)
   obsahuje 10 souborů včetně manifestu a kontrolních součtů. Dokumentaci
   nasadil [Pages workflow](https://github.com/jajir/microcol/actions/runs/38362888204);
   publikované R-0014 a tato stránka byly ověřeny včetně odkazů na správný commit.
4. Tento záznam uzavírá audit a testovou implementaci. Neoznačuje R-0012–R-0025
   ani celé dotčené UC za implementované. Nové mapy, Francie, opravy menu,
   fog-of-war renderovací testy a chybějící pravidlové varianty se přijímají
   zvlášť podle jejich analýz.

<a id="free-play-implementation-2026-10-13"></a>

## Implementace R-0012–R-0018 — 2026-10-13

1. **Ověřeno 13. října 2026:** implementace R-0012–R-0018 prošla úplnou
   lokální regresí v Dockeru/JDK 17/Xvfb/Openbox. **705 herních testů
   a 78 funkčních/kontrolních testů**, bez selhání, chyb nebo vynechání.
   Zahrnutý ArchUnit nadále hlídá nezávislost modelu. Nativní macOS UI se
   pro tuto přejímku nepoužilo.
2. TC12 zachovává start po kampani. Sedm TC21 ověřuje nově také čistý profil,
   aktuální pozici po návratu do menu, zrušení načítání, Settings Back bez modelu
   a nabídku vlastních kolonií myší i přes O/šipky/Enter. Prohlížení nemění tah
   ani zlato. TC23/TC_02 obnovuje poslední odjezd lodi, náklad a zbývající plavbu.
3. Šest TC24/F15 pokrývá skutečné spuštění obou map, francouzské vlastnictví,
   další tah a Continue, Back/Escape beze změny uložené hry, jméno o délce
   0/64/65 znaků, české nastavení a vyloučení cizích měst z nabídky kolonií.
   `PlayerDisplayNameTest` má šest kontrol Unicode/persistence;
   `FreePlayMapsTest` dvě kontroly načtení, persistence a topologie ostrovů.
4. Čtrnáct `ColonistEquipmentTest` a pět TC25/F17 ověřují náklady a vracení
   vybavení, nedostatek zásob, plný sklad, zrušení, změnu nákladů, nedostupný
   subjekt, opakovanou operaci a češtinu/klávesnici. TC19 má 18 scénářů:
   TC_13 po změně 20→19 zruší aktualizovanou volbu, TC_18 ji výslovně potvrdí;
   TC_14 po zániku kolonie vrací mapu. Žádná zamítnutá změna se neprovede částečně.
5. První společný běh odhalil heap OOM: fixture po zavření okna neukončila
   mapovou animaci. `MicroColApplication.stop()` nyní ukončí aktivní obrazovku
   přes presenter a `AfterEach` volá stejný úklid. Stejný cílený příkaz poté
   prošel všemi **32 kontrolami** (TC19/24/25 a page contract), bez skipů.
   Limit paměti ani herní očekávání nebyly oslabeny. První úplný běh odhalil
   také zastaralou přípravu `ColonyIdentityTest`: scénář nyní výslovně odstraní
   přibalené soupeře/kolonie před ověřením prvního ID 0; původní aserce zůstaly.
6. Úplná UI sada odhalila nepotvrzené dialogy v TC05/TC06/TC25. Přímý záznam
   nativních událostí a fyzický snímek prokázaly chybu souřadnic: znovu otevřený
   dialog ležel na (300, 317), ale JavaFX hlásilo (0, 0), takže klik mířil mimo něj.
   `AbstractMessageWindow` nyní centruje až po zobrazení nativního okna a nepoužívá
   pomocné otevření mimo obrazovku. Spekulativní změny testovacích kliků i dočasné
   diagnostické výpisy byly odstraněny. Původní kliknutí a herní aserce zůstaly;
   následná cílená i úplná regrese prošly.
7. JavaFX snapshot u hlavního menu přepočítával obrovskou preferovanou plochu.
   Testovací helper po dobu snímku použije skutečné rozměry scény a původní
   preference i rozměry obnoví v `finally`. Český/anglický setup a dialogy
   vybavení byly vizuálně zkontrolovány: text, náklady a tlačítka jsou celé vidět.
   Po snímku pokračují skutečné hráčské akce. Produkční rozložení starého menu
   se kvůli snímkům neměnilo.
8. Úplný příkaz: `docker compose -f compose.ui-tests.yml run --build --rm ui-tests`.
   Log: `target/r12-r18/docker-full.log`; XML a snímky:
   `target/docker-ui/run-AMGZOF/`. Prošel sdílený Java formatter včetně kontroly
   zachování tokenů a idempotence, devět dokumentačních kontrol a strict MkDocs.
   Dílčí logy, odmítnuté lokální výstupy a opravy jsou v
   `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-13-r*-*/`.
9. Toto je přijetí uvedeného rozsahu, nikoli celé matice národů/obtížností,
   kompletní ekonomické AI nebo všech UC. Výběr národa zůstává výslovně náhledem.
   Zveřejnění této revize musí potvrdit její [vývojový workflow](https://github.com/jajir/microcol/actions/workflows/dev-builds.yml)
   a manifest [kanálu dev](https://github.com/jajir/microcol/releases/tag/dev);
   samotný lokální výsledek publikaci nedokládá.


<a id="fog-implementation-2026-10-13"></a>

## Mlha nad neprozkoumanou mapou R-0019 — 2026-10-13

1. Cílený Docker/JDK 17/Xvfb/Openbox běh prošel **7 kontrolami**, bez selhání,
   chyb nebo vynechání: `VisibilityTest` (1), `TC_26_F18_fog_of_war_test` (3)
   a `PageVerificationContractTest` (3). Příkaz doplnil běžný Docker runner o
   `-Dtest=VisibilityTest,TC_26_F18_fog_of_war_test,PageVerificationContractTest`
   a `-Dsurefire.failIfNoSpecifiedTests=false`. Log:
   `target/r19-r31/fog-final-targeted.log`; výstupy: `target/docker-ui/run-QUn0sq/`.
2. TC26/TC_01 kontroluje skrytého nepřítele i kolonii, stavový řádek, pravý
   panel, skutečné pixely mlhy a náhled známého/skrytého cíle. TC_02 odkrývá
   hranici skutečným pohybem, vrací loď a ověřuje průzkum i po Continue.
   TC_03 kreslí diagnostickou animaci skutečným mapovým kompozitorem: známý
   pixel zůstane viditelný, skrytý zakryje mlha a dokončovací callback proběhne.
   Nejde o tvrzení, že byly přehrány všechny konkrétní nepřátelské animace.
3. Na původním produkčním kódu test s fregatou prokázal cestu útoku k dosud
   neprozkoumanému nepříteli (`fog-final-baseline.log`); samostatný předchozí
   běh prokázal překreslení mlhy animací (`fog-confirmed-baseline.log`).
   Po opravě stejná očekávání prošla. Dřívější chyby testovacího gesta,
   souřadnic a snapshotu nejsou důkazem chyby herních pravidel; jejich opravy
   a lokální worker jsou popsány v [provozním záznamu](ai/local-qwen.md#r-0019-fog-overlays-2026-10-13).
4. Snímky `fog-unknown.png` a `fog-discovered-continued.png` z konečného
   cíleného běhu byly vizuálně zkontrolovány. Skrytá kolonie ani loď před
   průzkumem nejsou vykreslené, po průzkumu a Continue zůstávají známé.
5. Úplný příkaz `docker compose -f compose.ui-tests.yml run --build --rm ui-tests`
   prošel: **706 herních testů a 81 funkčních/kontrolních testů**, nula selhání,
   chyb a vynechání. Ověřena čerstvá XML a návratový kód 0 v
   `target/docker-ui/run-qniEHK/`; log `target/r19-r31/fog-full.log`, doba Maven
   12:50. Zahrnuty jsou všechny tři TC26 i původní hráčské cesty. Prošlo osm
   změněných Java souborů sdíleným formatterem, devět kontrol dokumentace a
   strict MkDocs; číslovaná evidence byla zkontrolována ve webovém náhledu.
6. Přesné dosahy rolí Q-11, druhá vrstva živé viditelnosti a nativní macOS UI
   nejsou tímto řezem ověřeny. Celé UC-006 proto zůstává otevřené.
7. [CI revize f383ebfb](https://github.com/jajir/microcol/actions/runs/38411903978)
   zopakovalo celých **706/81 kontrol**, bez selhání, chyb nebo vynechání;
   ověřena stažená XML v `target/r19-r31/ci-r19/`. Všechny tři platformní
   balíčky i publikace prošly. Manifest kanálu `dev` potvrzuje vydání
   [dev-47-f383ebfbe87f](https://github.com/jajir/microcol/releases/tag/dev-47-f383ebfbe87f)
   a plnou revizi `f383ebfbe87f350970af36868cfb4d6b2d242b03`.
   [Pages](https://github.com/jajir/microcol/actions/runs/38411903434) nasadily
   dokumentaci; zkontrolován zveřejněný text R-0019 a odkazy architektury na
   správný commit. Toto následné doplnění evidence nemění ověřený herní kód.


<a id="europe-equipment-implementation-2026-10-13"></a>

## Evropská výbava R-0020 — rozpracováno 2026-10-13

1. Zatím je ověřený cenový základ, nikoli celé vybavování na molu. Cílený
   Docker příkaz s výběrem
   `-Dtest=EuropeTradeQuoteTest,PlayerTest,TC_05_F05_moving_goods_test,TC_06_F06_buy_and_sell_goods_test,PageVerificationContractTest`
   a `-Dsurefire.failIfNoSpecifiedTests=false` prošel: **15 modelových a 8
   funkčních/kontrolních testů**, bez selhání, chyb a vynechání. Čerstvá XML
   jsou v `target/docker-ui/run-G3aGhn/`, log `target/r19-r31/pricing-targeted.log`.
2. Čtyři testy nabídky a jedenáct testů hráče kontrolují bid/ask, celá čísla,
   zlomkovou daň, přesnou částku, nedostatek jednoho zlatého, přetečení,
   nulovou cenu a rozlišení změněné nabídky i při shodném zaokrouhleném součtu.
   Politiku čísel, odlišenou od výslovného textu manuálu, vlastní [R-0020](requirements/done/R-0020.md).
3. TC06 nově kontroluje viditelnou jednotkovou cenu i celkovou částku před
   skutečným nákupem/prodejem. Sdílený page helper mění množství kliknutím na
   úchyt a klávesami Home/šipka, ne přímým zápisem do slideru; klávesový krok
   trhu je jeden kus. Prošly i všechny tři TC05 používající tento helper.
   Samostatný TC06/TC_06_moving_goods s původními cenovými metodami prodeje
   selhal přesně na `expected 2 gold, actual 4 gold`; po vrácení opravy stejný
   příkaz prošel. Logy `market-original-regression.log` a
   `market-fixed-regression.log` jsou v `target/r19-r31/`.
4. Navazující modelová kontrola s výběrem
   `-Dtest=EuropeEquipmentTest,PlayerTest,EuropeTradeQuoteTest,ColonistEquipmentTest,PageVerificationContractTest`
   prošla: **52 modelových a tři strukturální kontroly**, nula selhání, chyb
   a vynechání. Nových 22 případů ověřuje nákup/prodej všech tří druhů,
   souběh koní a mušket, zbylých 37 nástrojů, přesné peníze, opakování,
   zastaralou nabídku, polohu/vlastnictví/identitu, uzavřený obchod a přetečení.
   Stejná kontrola nejprve odhalila chybný název getteru v novém testu; po
   opravě na `getActionPoints()` prošla. Logy `equipment-model-targeted.log`
   a `equipment-model-fixed.log` jsou v `target/r19-r31/`; XML v izolovaném
   worktree úkolu `2026-10-13-r20-pier-equipment-model`, `target/docker-ui/`.
5. První celý běh TC27 prošel: **devět hráčských scénářů a tři strukturální
   kontroly** spolu s 52 modelovými testy, bez selhání, chyb a vynechání.
   Log `equipment-ui-fixed.log`, izolovaná XML `run-jRkncS`. Předchozí kompilace
   narazila na neexistující `JsonParser.parseString` v instalované verzi Gson;
   příprava testovacího save používá existující `Gson.fromJson`.
   Scénáře kontrolují všechny nákupy/prodeje, rušení, nekompatibilní výbavu,
   přesné peníze, změnu nabídky, přesunutý subjekt, klávesnici, češtinu,
   uzavřený obchod a nástroje/identitu při nalodění, plavbě a Continue.
6. Anglické i české snímky nabídky a zprávy po změně ceny byly zkontrolovány.
   Následně se názvy zboží změnily na existující množné překladové klíče.
   Integrační běh již ověřil všech devět TC27 včetně doplněného odmítnutí
   přesunů nákladu po nezávislosti, ale odhalil dvě chyby TC06: stará fixture
   měla `declaredIndependence=true` a přesto očekávala povolený obchod.
   F06 nyní nastavuje správnou obchodní podmínku před nezávislostí; žádná
   očekávaná cena, zásoba ani ID testu se neoslabila. Stejná integrační
   kontrola po opravě prošla: **53 modelových/architektonických a 27
   funkčních/strukturálních testů**, bez selhání, chyb a vynechání. Čerstvá
   XML `target/docker-ui/run-5AoDQ4/`, log `equipment-integrated-fixed.log`.
   Zahrnuty jsou TC05, TC06, TC22, TC25, TC27 a modelová hranice ArchUnit.
7. Úplná lokální Docker regrese dokončena 13. října 2026: **737 herních a
   90 funkčních/strukturálních testů**, nula selhání, chyb a vynechání.
   Čerstvá XML jsou v `target/docker-ui/run-lQgf1l/`, log v
   `target/r19-r31/equipment-full.log`; příkaz
   `docker compose -f compose.ui-tests.yml run --build --rm ui-tests` skončil
   s kódem 0. Vyložení v cílové kolonii a
   odbornost budoucího specialisty se ověří v návaznosti na R-0022/R-0023.
   Tento dílčí záznam nepovažuje celé R-0020 ani nadřazený cíl za dokončený.
8. Tento ověřený řez je publikovaný v commitu
   [`b989413bb606`](https://github.com/jajir/microcol/commit/b989413bb606b65a49c82c33076a2e82c4385768).
   [CI 38424361493](https://github.com/jajir/microcol/actions/runs/38424361493)
   prošla včetně všech tří nativních platforem a publikace; stažená funkční
   XML potvrzují stejných **737 + 90 testů**, bez selhání, chyb a vynechání.
   [Pages 38424361284](https://github.com/jajir/microcol/actions/runs/38424361284)
   také prošly. Stažený manifest kanálu `dev` potvrzuje úplné SHA a build
   [dev-48-b989413bb606](https://github.com/jajir/microcol/releases/tag/dev-48-b989413bb606).
   Požadavek zůstává rozpracovaný kvůli výše uvedeným navazujícím cestám.

## Atlantic voyage implementation — 2026-10-13

1. Publikované [R-0021](requirements/done/R-0021.md) mění odjezd až na samostatný
   pohyb ven z námořního pruhu a ukládá jeho výchozí pole i pobřeží. Návrat
   používá přesné pole; obsazený cíl nechává loď čekat na nule. Časování
   3/4/3 tahy zůstává výslovně současnou politikou, nikoli rekonstruovaným
   výpočtem počasí z manuálu.
2. Izolované geometrické kontroly prošly: 19 `HighSeaPolicyTest`, dva
   `HighSeaTest`, jeden ArchUnit a tři strukturální testy. Čerstvá XML
   `run-diqWLY` v worktree `2026-10-13-r21-sea-lane-policy`; log
   `target/r19-r31/sea-lane-policy-fixed.log` v hlavním checkoutu. První
   příkaz opomněl vybrat test z funkčního modulu; opravený výběr přidal
   strukturální kontrolu a zachoval selhání při prázdném výběru.
3. Cílená integrace v Docker/JDK 17/Xvfb/Openbox prošla: **45 modelových a
   34 funkčních/strukturálních testů**, nula selhání, chyb a vynechání.
   Výběr: `AtlanticVoyageTest,HighSeaPolicyTest,HighSeaTest,UnitFrigateTest,UnitColonistOnMapTest,ModelArchitectureTest,TC_23_F16_atlantic_voyage_audit_test,TC_27_F19_europe_equipment_test,TC_28_F20_western_voyage_test,TC_19_F14_warehouse_unloading_test,PageVerificationContractTest`.
   Příkaz používá běžný `docker compose -f compose.ui-tests.yml run --build --rm ui-tests`
   s tímto `-Dtest` a `-Dsurefire.failIfNoSpecifiedTests=false`.
   XML jsou v izolovaném `2026-10-13-r21-voyage-arrival/target/docker-ui/run-hPfVaK/`,
   log `target/r19-r31/voyage-integrated-json-fixed.log` v hlavním checkoutu.
4. První integrační běh odhalil nesprávnou přípravu nového testu s prázdnou
   sadou vyhodnocení konce hry. Oprava používá existující evaluátory mise.
   Druhý běh odhalil pět chyb ukládání: volitelný směr odjezdu vyžaduje
   `nullSafe()` registraci existujícího Gson převodníku. Následné stejné
   kontroly prošly. Validace modelu nadále odmítá neúplný či chybný původ
   rozpluté lodi; nejde o domýšlení dat nebo migraci starých save souborů.
5. TC23 zachovává obě původní ID, nově očekává vstup do pruhu následovaný
   dalším krokem ven a přesný návrat. TC28/F20 přidává západní vstup, pohyb
   podél pruhu a zpět dovnitř, zrušení přetažení, čtyřtahový odjezd,
   obsazený návrat s Continue a následné připlutí po uvolnění pole. Druhý
   scénář skutečným přetažením ověřuje srozumitelné odmítnutí mapy bez cíle.
   Náklad 7 rudy, cestující i 80 nástrojů zůstávají zachované. TC19/TC_16
   stále ověřuje loď mimo mapu během otevřené nabídky vykládání; příprava
   souběžné události nyní odplouvá z platného námořního pruhu.
6. Kontrola exportovaných snímků odhalila zkrácenou zprávu čekání. Minimální
   výška zalamovaného popisku je opravená; TC28 nově kontroluje skutečně
   vykreslený text, nikoli pouze hodnotu `Label.getText()`. Stejná západní
   cesta, odmítnutí mapy a 11 modelových případů včetně dodatečného obsazeného
   kroku ven prošly: **11 modelových a pět funkčních/strukturálních testů**,
   nula selhání, chyb a vynechání. Čerstvá XML hlavního checkoutu
   `target/docker-ui/run-ZBBq6H/`, log `target/r19-r31/voyage-visible-status.log`.
   Výsledný snímek `western-arrival-waiting.png` ukazuje celý důvod na dvou
   řádcích.
7. Úplná regrese hlavního checkoutu dokončena 2026-10-13 v 16:28 UTC:
   `docker compose -f compose.ui-tests.yml run --build --rm ui-tests`, návratový
   kód 0, **767 herních a 92 funkčních/strukturálních testů**, žádné selhání,
   chyba ani vynechání. Čerstvá XML jsou v `target/docker-ui/run-DcYHSR/`,
   log `target/r19-r31/voyage-full.log`. Zahrnuje obě TC23 a obě TC28,
   devět TC27 a všech 18 TC19.
8. Publikace ověřena 2026-10-13: commit `88573a85ac0336307724b7da8bf06bc4bb159990`,
   [build dev-49-88573a85ac03](https://github.com/jajir/microcol/releases/tag/dev-49-88573a85ac03).
   [CI 38430949548](https://github.com/jajir/microcol/actions/runs/38430949548)
   včetně funkčních testů, tří nativních balíků a `publish` uspěla; uspěly také
   [Pages 38430949275](https://github.com/jajir/microcol/actions/runs/38430949275).
   Stažený `release-r21/manifest.tsv` potvrzuje stejný celý SHA a build;
   odpovídající Mac ARM ZIP je v releasu dostupný (61 102 179 bajtů).
   Čerstvá CI XML v `target/r19-r31/ci-r21/` potvrzují **767 + 92 testů**,
   nula selhání, chyb a vynechání. Tím není přijata zbývající práce R-0022 a dále.

## Individual passenger transfers — 2026-10-13

1. Lokálně ověřené [R-0022](requirements/done/R-0022.md) mění hromadné vylodění na
   výběr jednoho cestujícího. TC01 zachovává původní dvě osoby a kontroly
   nákladu, ale potvrzuje dvě samostatné volby. Nové TC29/F21 ověřuje zrušení,
   Escape, reset volby, jednotlivé vylodění, vybavení, Continue a odmítnutí
   pobřeží obsazeného během otevřené nabídky. Úplná regrese prošla
   781 modelovými a 98 funkčními/podpůrnými testy; publikace zatím čeká.
2. `PassengerTransferTest` používá skutečný model a JSON save/load. První
   cílený běh zkompiloval změny, ale devět případů skončilo na chybně připravené
   mapě: starý námořní původ lodi odkazoval na pole nově změněné na oceán.
   Druhý běh po opravě prošel sedmi případy a ArchUnit; dva zbývající případy
   odhalily další chyby přípravy: `Model.stop()` neznamená neodstartovanou hru
   a nepřátelská loď nesmí být nově vytvořená na obsazeném poli. Následná
   oprava používá explicitní příznak zahájení a sousední volné vodní pole.
3. Další běh odhalil, že celou neodstartovanou hru nelze uložit pro porovnání;
   kontrola odmítnutí nyní porovnává uložitelné stavy pasažéra a lodi, všechny
   ostatní sloty a příznak běhu. Po této opravě **devět modelových případů a
   ArchUnit prošlo** bez selhání či vynechání. UI část pak odhalila chybný
   les na novém vodním poli F21; opravená příprava nemění validaci hry.
4. Opravený cílený výběr prošel: **10 modelových/architektonických a šest
   funkčních/strukturálních testů**, žádné selhání, chyba ani vynechání.
   Výběr: `PassengerTransferTest,ModelArchitectureTest,TC_01_F01_embark_disembark_test,TC_29_F21_passenger_transfer_test,PageVerificationContractTest`,
   běžný Docker příkaz s `-Dtest` a `-Dsurefire.failIfNoSpecifiedTests=false`.
   Log `target/r19-r31/passenger-integrated-map-fixed.log`, izolovaný
   checkout `2026-10-13-r22-passenger-dialog`, čerstvá XML `run-MZv8FT`.
   Původní chyby zůstávají v `passenger-model-first.log`,
   `passenger-integrated.log`, `passenger-integrated-fixture-fixed.log` a
   `passenger-integrated-state-fixed.log`. Snímek `passenger-selection.png`
   byl zkontrolován: celé souřadnice, obě volby, vybavení a tlačítka jsou viditelné.
5. V této fázi před přejímkou zbývalo rozšířit hraniční a hráčské kontroly plné/cizí lodi,
   přístavního převodu bez bodů a pobřežního predikátu; aktualizovat starší
   izolované mock testy podle skutečného kontraktu; provést úplnou regresi,
   kontrolu snímků a publikaci. Žádný neprovedený scénář není přijatý.
6. Následná kontrola všech testů herního modulu v hlavním checkoutu našla
   **777 případů, 17 selhání a osm chyb**, nula vynechání (`run-7ZqsCH`,
   `target/r19-r31/passenger-model-regression.log`). Funkční modul se po tomto
   selhání nespustil. Nejde o úspěšnou regresi. Zasažené třídy:
   `CargoSlotTest`, `ColonyWarehouseUnloadTest`, `UnitColonistEmbarkTest`,
   `UnitFrigateEmbarkTest` a `UnitTest`.
7. Dvě přípravy testů nákladu používaly mock pasažéra bez účinku při umístění,
   přestože skutečný pasažér nyní zapisuje obě strany vazby před událostí.
   `CargoSlotTest` napodobuje tento účinek v mocku; test vykládání skladu
   připravuje obsazený slot přes nízkoúrovňový konstruktor místa. Původní
   tvrzení o obsahu a odmítnutí převodu jsou zachovaná. Cílená oprava prošla:
   **105 modelových a tři strukturální testy**, nula selhání, chyb a vynechání,
   čerstvá XML `target/docker-ui/run-jSDIvr/`, návratový kód 0; log
   `passenger-cargo-fixtures-fixed.log`. Zbývající tři starší testovací třídy
   přepravy se ještě musely přizpůsobit živým modelovým předpokladům a novému účtování.
8. Rozšíření přípravy z 13. října zachovává všech 16 původních případů
   `UnitColonistEmbarkTest`, ale ověřuje je nad skutečným modelem a oběma
   vazbami nákladu. Odmítnutí a zrušení porovnává uložený stav; úspěch kontroluje
   jednu spotřebovanou akci a nezměněnou loď. Dvě izolované přípravy `UnitTest`
   nyní mají živé vlastníky a skutečnou identitu místa, s nulovou místní cenou
   a cenou jedna při sousedním nalodění. Fregata předává i příznak aktuálního
   tahu sdílenému predikátu pasažéra. ID původních testů zůstala zachovaná.
9. Nový reprodukční test `rejectsSlotFilledDuringMovementConfirmationBeforeAnimation`
   skutečně selhal: po zaplnění místa během potvrzovací události model ještě
   vyslal začátek kroku (`run-YIOwMX`, `passenger-boundaries-compile-fixed.log`).
   `embarkFromLocation` nyní opakuje kontrolu před touto událostí, obdobně jako
   vylodění. Stejný test po opravě prošel; nešlo pouze o změnu očekávání.
10. Rozšířená pobřežní příprava zpočátku nechala na novém moři pole a tabákovou
    práci, které loader správně odmítl. Příprava nyní odstraňuje pole/stromy
    z vodního políčka a používá u pracovníků kukuřici, platnou na obou terénech.
    První oprava nesprávně použila neexistující `GoodsType.FOOD`; překlad
    ji odmítl a následovala oprava na skutečné `CORN`. Produkční validace
    nebyla uvolněna. Přidány jsou všech osm směrů vody, cizí vlastník přístavu,
    falešná identita pasažéra a neplatné odkazy na odstraněného člověka či loď.
11. Opravený cílený běh v izolovaném checkoutu `2026-10-13-r22-frigate-tests`
    prošel **44 modelovými/architektonickými případy**, nula selhání, chyb
    a vynechání (`run-myTaDs`, `passenger-boundaries-fixture-fixed.log`).
    Výběr: `UnitColonistEmbarkTest,UnitFrigateEmbarkTest,UnitTest,PassengerTransferTest,ModelArchitectureTest`.
    Funkční část stejného běhu (`TC01,TC27,TC29,PageVerificationContractTest`)
    dokončila **18 případů s jedním selháním**, nula chyb a vynechání.
    Všech pět TC29, TC01 a tři strukturální kontroly prošly. TC29 zahrnuje
    také jedinou zbývající volbu, nulový pohyb v kolonii a Evropě, obsazený
    náklad a odmítnutí cizí lodi přes UI. Čerstvá XML byla zkontrolována.
12. Zbývající selhání TC27/TC_07 odhalilo chybný předpoklad autora testu:
    nová loď se vrací na `[49,15]`, nikoli na `[26,16]`. Rozšíření původní
    evropské cesty nyní zachovává skutečný návrat a provádí další plavbu
    a tahy přes UI až ke kolonii. Ovládání posunu mapy používá hráčské
    klávesy A/D, nikoli přemístění lodi v modelu. Výsledek následného
    ověření je v bodech 13–15.

13. Úplný výběr herního modulu v hlavním checkoutu prošel **782 testy**,
    nula selhání, chyb a vynechání (`run-rOOFNJ`,
    `passenger-model-and-return.log`). Přesný výběr je uložen v
    `target/r19-r31/passenger-model-and-return-command.txt`. Čerstvá XML
    byla zkontrolována. Ve funkční části prošly tři strukturální kontroly,
    ale TC27/TC_07 čekal na nedosažitelný odstup od pravého okraje mapy.
    Pomocná metoda posunu nyní používá plnou viditelnost skutečného políčka
    o 45 pixelech, nikoli dodatečný pravý okraj 128 pixelů. Opakovaný test
    následoval společně s TC29 včetně nového scénáře zcela plné lodi.

14. Následný hlavní běh **prošel: 34 modelových a deset funkčních/strukturálních
    případů**, nula selhání, chyb a vynechání, návratový kód 0
    (`run-WcEg9m`, `passenger-return-scroll-fixed.log`). Výběr:
    `UnitColonistEmbarkTest,UnitFrigateEmbarkTest,PassengerTransferTest,TC_27_F19_europe_equipment_test#TC_07_equipment_and_identity_survive_embarkation_voyage_and_continue,TC_29_F21_passenger_transfer_test,PageVerificationContractTest`.
    Všech šest TC29, návrat TC27 a tři strukturální testy prošly. TC27
    provádí 25 polí plavby a běžné tahy od skutečného návratu `[49,15]`
    k přístavu `[24,16]`, vyloží člověka mimo pracovní místa a ověří zachování
    100 nástrojů i stejné identity po Continue. TC29/TC_06 prokazuje odmítnutí
    úplně plné lodi se zachováním všech zásob a druhého pasažéra. Čerstvá
    XML byla zkontrolována; exportovaný výběrový dialog je celý čitelný.
15. Úplná hlavní regrese **prošla: 781 modelových a 98 funkčních/podpůrných
    testů**, nula selhání, chyb a vynechání; návratový kód 0, čas 17:15.
    Příkaz `docker compose -f compose.ui-tests.yml run --build --rm ui-tests`,
    log `target/r19-r31/passenger-full.log`, čerstvá XML
    `target/docker-ui/run-oIvH5v/` (96 herních a 32 funkčních tříd).
    Ověřeny jsou i původní TC01/TC22/TC23, všech devět TC27 a všech šest TC29.
    Sdíleným formatterem prošlo 27 změněných Java souborů; následně se již
    Java kód neměnil. Dokumentace prošla devíti kontrolami a striktním
    sestavením, náhled byl zkontrolován. CI a publikace balíčků zatím čekají.
16. Rozdíl **782** ve výslovném výběru a **781** v běžném herním běhu
    je ověřen porovnáním XML: explicitní `-Dtest` navíc zahrnul jeden
    `org.microcol.integration.ConnectorTest`, který standardní Maven
    konfigurace historicky vylučuje. Tento dodatečný test také prošel.
    Žádná jiná třída ani počet případů se mezi oběma herními výběry neliší;
    nejde o odstraněný či vynechaný test nové přepravy.

17. Commit `a2a4ac22b9dbf1341fb1be81f3cadb84adb5176a` byl odeslán na `main`.
    Pages [38442144492](https://github.com/jajir/microcol/actions/runs/38442144492)
    prošly. Vývojový běh
    [38442144257](https://github.com/jajir/microcol/actions/runs/38442144257)
    ověřil všechny tři nativní balíčky, ale funkční job dokončil **98 případů
    s jednou chybou**, nula selhání a vynechání. TC29/TC_04 čekal na nalodění
    do volného místa po předchozím odmítnutém přetažení na obsazený slot.
    Publikační job se nespustil; nový vývojový build proto není dodaný.
    Stažená XML `target/r19-r31/ci-r22/docker-ui/run-AcYIkY/` a log
    `target/r19-r31/r22-ci-failed.log` uchovávají původní výsledek.
    Diagnostika běží nad čistým checkoutem tohoto commitu, odděleně od R-0023.

18. Cílená lokální diagnostika původního R-0022 commitu prošla všemi šesti
    TC29 a třemi strukturálními kontrolami; log
    `target/r19-r31/passenger-ci-diagnostic.log`. Dočasné filtry událostí myši
    v izolovaném checkoutu nebyly převzaty do zdrojů. Příčina původního CI
    timeoutu zůstává neprokázaná. Neúspěšný CI job byl jednou zopakován
    beze změny kódu (`38442144257`, pokus 2); opět selhal na TC29/TC_04.
19. Pomůcka `PassengerTransferPage` nyní po přetažení čeká na nativní
    `DRAG_DONE`, včetně odmítnutého cíle, místo pouhého vyprázdnění FX fronty.
    Používá existující vzor z `ColonyScreen`, pětisekundový limit a odstranění
    posluchače v `finally`. Scénáře a všechna očekávání zůstala stejná.
    Izolovaná revize R22 prošla výběrem **13 modelových a devíti funkčních/
    podpůrných testů**, nula selhání, chyb a vynechání (`run-lw3bzb`, exit 0;
    `target/r19-r31/passenger-drag-completion.log`). Přesná sekvence událostí
    původního vzdáleného timeoutu nebyla zachycena; úplná regrese a nové CI
    měly ověřit vyřešení publikační překážky.
20. Nové CI `38448815206` pro samostatnou opravu `b3ba0efc` prošlo celé:
    **781 modelových a 98 funkčních/podpůrných případů**, bez selhání,
    chyb a vynechání. Čerstvá XML jsou v
    `target/r19-r31/ci-r22-fixed/docker-ui/run-cYDLux`, návratový kód 0.
    Prošly všechny tři nativní balíčky, ostatní kontroly i publikace;
    Pages `38448815200` také uspěly. Manifest buildu
    `dev-51-b3ba0efcf67e` obsahuje úplné SHA
    `b3ba0efcf67e400fa80a83fef20d07107e8db784` a všechny tři archivy.
    Původní dvě selhání zůstávají v evidenci, nyní prošel stejný TC29/TC_04
    v celé CI sadě. Stažený archiv macOS ARM64 má 61 107 194 bajtů a
    SHA-256 `3c3905f09109de51eb27d2a67ff33355546dcf070a856fde11b85b953292bf67`,
    shodné s manifestem. Rozpracované R-0023 v tomto commitu není.

## Expert purchase implementation — 2026-10-13

1. Rozpracované [R-0023](requirements/done/R-0023.md) přidává skutečný typ
   `MASTER_BLACKSMITH` přes stávajícího kolonistu, továrnu a ukládání.
   `Player` ověřuje dostupný typ, živého aktuálního hráče, Evropu a peníze
   před vytvořením jednotky a účtováním. První regresní test nad původním
   nákupem skutečně selhal: odmítnutý horník odečetl 600 zlata
   (`run-lsw2rs`, `expert-unsupported-before.log`). Po opravě stejná kontrola
   nezměněného stavu prošla; další úspěšný nákup navíc ověřuje zachování
   následujícího ID.
2. Izolovaný výběr `ExpertPurchaseTest,EuropeEquipmentTest,UnitTypeTest,ModelArchitectureTest,PageVerificationContractTest`
   nejprve našel chybu přípravy: uložená mapa skladu nebyla měnitelná.
   Kopie mapy opravila přípravu bez změny modelové validace. Následně prošlo
   **36 modelových a tři strukturální případy**, nula selhání, chyb a
   vynechání (`run-AfJ4fv`, `expert-model-fixture-fixed.log`).
   Dvanáct nových modelových případů zahrnuje nákup, odmítnutí, vybavení,
   save/load, přepravu a skutečné pracovní místo kováře.
3. Nové TC30/F22 používá skutečnou evropskou nabídku. První hlavní výběr
   prošel modelem a scénářem zrušení/duplicitního potvrzení/opětovného
   nákupu/Continue, ale TC30/TC_03 hledal obecné `buttonOk` ve dvou oknech.
   Po zavření nesprávného okna zmizela nabídka; následující TC30/TC_02 také
   selhal na viditelnosti startovní obrazovky. Selektor nyní hledá tlačítko
   uvnitř okna konkrétního varování. Očekávání nákupu ani zásob se neměnila.
4. Opakovaný hlavní výběr **prošel: 36 modelových a 13 funkčních/podpůrných
   testů**, nula selhání, chyb a vynechání; návratový kód 0, čas 1:50.
   Příkaz: `docker compose -f compose.ui-tests.yml run --build --rm ui-tests -Dtest=ExpertPurchaseTest,EuropeEquipmentTest,UnitTypeTest,ModelArchitectureTest,TC_30_F22_expert_purchase_test,TC_22_F15_free_play_capability_audit_test,TC_06_F06_buy_and_sell_goods_test,PageVerificationContractTest -Dsurefire.failIfNoSpecifiedTests=false`.
   Čerstvá XML `target/docker-ui/run-CiqOHJ/`, log
   `target/r19-r31/expert-ui-warning-fixed.log`. Původní neúspěch zůstává
   v `expert-ui-first.log`. Všechna tři TC30, pět TC22, dvě TC06 a tři
   strukturální kontroly prošly. Snímek `expert-hiring.png` byl zkontrolován:
   čtyři nabídky, název odborníka, přesná cena a důvod nedostatku zlata
   jsou čitelné. Java změny byly naformátované sdíleným profilem.
5. Navazující výběr prošel: **13 modelových a sedm funkčních/podpůrných
   testů**, nula selhání, chyb a vynechání (`run-JpZkqv`, návratový kód 0,
   `target/r19-r31/expert-journey-first.log`). Nové TC30/TC_04 najme kováře,
   koupí mu muškety, uloží/načte na molu a během plavby, přes skutečné
   ovládání dopluje do kolonie a vylodí ho mimo pracovní místa. Po dalším
   Continue ho přetáhne do kovárny: muškety se vrátí do skladu, odbornost
   zůstane a po tahu přibude šest nástrojů za tři kusy rudy. Identita,
   vybavení a přiřazení jsou kontrolované také po načtení.
6. Rozšířený běh nejprve selhal pouze v TC30/TC_06: testovací
   `EuropePortScreen` očekával anglické „Europe“ i po přepnutí do češtiny.
   Pomůcka nyní přijímá očekávaný nadpis; anglické scénáře ponechávají
   původní kontrolu, český požaduje „Evropa“. Původní neúspěch je v
   `target/r19-r31/expert-journey-complete.log`. Následující běh prošel:
   **13 modelových a 14 funkčních/podpůrných testů**, nula selhání, chyb
   a vynechání (`run-9jWePO`, exit 0, čas 2:32;
   `target/r19-r31/expert-journey-language-fixed.log`). Všech šest TC30,
   pět TC22 a tři strukturální kontroly zahrnují také odmítnutí odebraného
   hráče a českou nabídku potvrzenou přes Tab/Space. Snímek
   `expert-hiring-czech.png` byl zkontrolován jako čitelný.
7. Lokální worker doplnil pojmenování pracovníků ve stávajícím stavovém
   řádku budovy. První izolovaný výběr selhal v TC30/TC_04 na názvu prázdné
   kovárny: test očekával „Blacksmith’s House“ z obecných prostředků,
   obrazovka ale správně používá „Blacksmith house“ z vlastního
   `screen/colony/ConstructionTypeName.xml`. Opravené je pouze přesné
   očekávání původního názvu; jméno odborníka, jeho odchod a výroba mají
   původní kontroly. Log `target/r19-r31/expert-workplace-first.log` zachovává
   neúspěch. Další běh už ověřil název pracovníka a jeho zachování po Continue,
   ale gesto odchodu skončilo timeoutem. Diagnostika skutečného
   `MOUSE_PRESSED` ukázala, že viditelného kováře na souřadnicích (291, 695)
   překrývá prázdný přesah `id_tobacco` z předchozího řádku; jeho hranice
   zvětšoval posunutý panel výroby. Důkaz je v
   `expert-workplace-pick-diagnostic.log`, původní timeout v
   `expert-workplace-label-fixed.log`. Hlavní agent omezil zachytávání myši
   na skutečný obsah panelu a vypnul je pro dekorativní výrobní údaje.
   Stejná cesta, souřadnice a všechna herní očekávání poté prošly:
   **13 modelových a 14 funkčních/podpůrných případů**, nula selhání, chyb
   a vynechání (`run-CqcmQQ`, exit 0, čas 2:37;
   `target/r19-r31/expert-workplace-picking-fixed.log`). Dočasná diagnostika
   byla odstraněna. Názvy prázdné a obsazené kovárny, Continue, odchod
   pracovníka a výroba se ověřují skutečným ovládáním. Přijatý workerův
   kód i dvouřádková oprava hlavního agenta byly začleněné. Samostatné
   nové TC30/TC_07 pro nahrazení celé hry pak také prošlo spolu se třemi
   strukturálními kontrolami (**čtyři případy**, `run-BnErBn`, exit 0;
   `target/r19-r31/expert-replaced-game.log`). Obě hry si zachovají zlato
   a nevznikne odborník. Úplná regrese v hlavním checkoutu
   (`target/r19-r31/expert-full.log`) skončila chybou popsanou níže; publikace R-0023 čeká. Celé UC-055 tím není přijaté.

8. Konečný výsledek první úplné regrese: **793 modelových testů prošlo** bez
   selhání, chyb a vynechání. Funkční část zaznamenala chybu v
   TC03/TC_03 při vyhlášení nezávislosti: královská AI měnila seznam
   jednotek současně s vykreslováním, které streamovalo živý `ArrayList`
   v `UnitStorage.getUnitsAt` (řádek 109). Výsledkem byla
   `ConcurrentModificationException`. Funkční část dokončila **105 případů,
   jedna chyba, žádné selhání aserce ani vynechání**; všech sedm TC30 prošlo.
   Log `target/r19-r31/expert-full.log`, běh `run-VHuz8A`, exit 1.
   Tento běh neumožnil commit ani publikaci. Následná oprava je ověřená
   cíleně podle bodu 9; úplnou sadu je nutné ověřit znovu.

9. Oprava registru zachovává pořadí a používá `CopyOnWriteArrayList` pro
   stabilní členství při čtení. Nový
   `UnitStorageTest.mapQueryKeepsMembershipSnapshotWhileUnitsAreReplaced`
   řízeně pozastaví čtení, odebere původní jednotku, přidá jinou a ověří
   původní i následující výsledek. Žádné spánky, náhodné opakování ani
   potlačení výjimky. Na původním kódu test skutečně skončil
   `ExecutionException` způsobenou `ConcurrentModificationException`
   (`run-dgJqvu`, jeden modelový případ, jedna chyba, exit 1; UI fáze se
   kvůli očekávanému selhání nespustila). Po opravě prošlo **18 modelových
   a 11 funkčních/kontrolních případů**, žádná chyba, selhání ani vynechání
   (`run-PdZoxo`, exit 0). Výběr obsahoval všech pět `UnitStorageTest`,
   12 `ExpertPurchaseTest`, jeden `ModelArchitectureTest`, původní TC03,
   všech sedm TC30 a tři strukturální kontroly. Příkaz:
   `docker compose -f compose.ui-tests.yml run --build --rm ui-tests -Dtest=UnitStorageTest,ExpertPurchaseTest,ModelArchitectureTest,TC_03_F03_declare_independence_test,TC_30_F22_expert_purchase_test,PageVerificationContractTest -Dsurefire.failIfNoSpecifiedTests=false`.
   Dvouřádková produkční změna pochází z návrhu lokálního workera; test
   dokončil hlavní agent po vyčerpání obou oprav a společného limitu.
   Změna je začleněná a naformátovaná v hlavním checkoutu. Úplná regrese
   a publikace R-0023 ještě nejsou doložené.

10. Následná úplná regrese hlavního checkoutu prošla: **794 modelových testů
    v 97 třídách a 105 funkčních/kontrolních případů ve 33 třídách**, žádné
    selhání, chyby ani vynechání. Příkaz
    `docker compose -f compose.ui-tests.yml run --build --rm ui-tests`,
    exit 0, doba 18:50, běh `run-QGuMTf`, log
    `target/r19-r31/expert-full-fixed.log`. Čerstvé XML reporty potvrzují
    původní TC03 i všech sedm TC30. Sdílený formatter ověřil 23 změněných
    Java souborů včetně shody tokenů a idempotence. Lokální testování tím
    prošlo; CI, Pages a zveřejněný distribuční manifest ještě čekají.

11. Publikace R-0023 ověřena 13. října 2026: commit
    `2a464170c54d95713ab9781ece0cc0f24c17e0a2`,
    [CI 38459737614](https://github.com/jajir/microcol/actions/runs/38459737614)
    úspěšně dokončilo všechny tři nativní balíčky, kontroly, úplné funkční
    scénáře a publikaci. Čerstvý artifact `tests-functional` obsahuje
    **794 modelových a 105 funkčních/kontrolních případů**, nula selhání,
    chyb a vynechání. [Pages 38459737655](https://github.com/jajir/microcol/actions/runs/38459737655)
    také prošlo; veřejná stránka R-0023 obsahuje závěrečnou lokální evidenci.
    Manifest kanálu odkazuje na stejný úplný commit a
    [build dev-52-2a464170c54d](https://github.com/jajir/microcol/releases/tag/dev-52-2a464170c54d).
    Skutečně stažený macOS ARM64 ZIP má 61 109 033 bytů a SHA-256
    `870eae3ecaca643e9485cfe432283fc4909ef59fdaae9fa5cd0d105f6159fbb9`,
    shodný s manifestem. Reporty a distribuční důkazy jsou uložené v
    `target/r19-r31/ci-r23/` a `target/r19-r31/release-r23/`.


## Wagon unloading implementation — 2026-10-13

1. [R-0024](requirements/done/R-0024.md) doplňuje vůz do stejného seznamu
   dopravců jako lodě. `Colony.getUnitsInPort` vrací vlastní místní
   `UnitWithCargo`; existující obrázek a české/anglické názvy podporují mapu
   i přístav. Vykládání nadále používá jedinou modelovou transakci skladu.
   Nezavádí se přeprava lidí vozem ani automatické trasy.
2. Nové `WagonWarehouseTest` obsahuje sedm případů se skutečným vozem
   vytvořeným továrnou: limity 100/200/300, zastaralá nabídka, vzdálený/cizí/
   odstraněný vůz, zákaz pasažérů a uložení identity i obou zásob.
   `TC_31_F23_wagon_unloading_test` má osm čitelných hráčských scénářů.
   TC01 přesune vůz po mapě, vybere jej vedle lodi, zruší nabídku 10 z 30
   rud při zásobě 90, potvrdí 100/20, ověří plný sklad a Continue.
   TC02 ověřuje rozšíření skladu a Escape; TC03–06 změněné zásoby,
   náklad, místo a vlastníka během potvrzení. TC07 odmítne pasažéra
   a neplatné tažení ikony; TC08 ověří skutečný český mapový popis.
   Stávající TC22/TC_04 si ponechává ID, ale požaduje podporovaný obrázek.
3. První běh `run-SsY6YV`, exit 1, prošel **66 modelovými případy**.
   Funkční část měla **34 případů, šest selhání, žádnou chybu ani vynechání**.
   TC31/03 zobrazil správnou novou nabídku 5 a model držel 95 rud, ale
   panel skladu stále ukazoval 90. Původní `PanelColonyGoods` překresloval
   pouze po ukončení celé smyčky dialogu. Selhání při otevřeném modálu
   předcházelo pěti dalším selháním načtení hry; nevznikl jejich log načtení
   fixture. Všech 18 TC19, pět TC22, tři strukturální kontroly a TC31/07–08
   prošlo. Log: `target/r19-r31/wagon-first.log`.
4. Hlavní agent doplnil existující `RepaintColonyEvent` po úspěšném ověření
   obnovené nabídky a před novým výběrem množství. Pravidla převodu i všechna
   očekávání zůstala zachovaná. Stejný příkaz poté prošel **66 modelovými
   a 34 funkčními/kontrolními případy**, nula selhání, chyb a vynechání
   (`run-XUJtg1`, exit 0; `target/r19-r31/wagon-refresh-fixed.log`):
   `docker compose -f compose.ui-tests.yml run --build --rm ui-tests -Dtest=WagonWarehouseTest,ColonyWarehouseUnloadTest,ModelArchitectureTest,TC_31_F23_wagon_unloading_test,TC_22_F15_free_play_capability_audit_test,TC_19_F14_warehouse_unloading_test,PageVerificationContractTest -Dsurefire.failIfNoSpecifiedTests=false`.
   Čerstvé XML potvrzují všech osm TC31. Snímek `wagon-colony.png` byl
   prohlédnut: vůz se odlišuje od lodi a zobrazuje své dva prostory.
   Název kolonie je ve snímku vlevo oříznutý; nejde o přijetí celého layoutu.
5. Přijatých 14 zdrojových/testovacích/fixture souborů bylo začleněno
   z izolovaného checkoutu po ověření původního commitu `2a464170`.
   Jedenáct Java souborů prošlo společným formatterem, shodou tokenů
   a idempotencí. Úplná regrese hlavního checkoutu příkazem
   `docker compose -f compose.ui-tests.yml run --build --rm ui-tests`
   prošla **801 modelovými a 113 funkčními/kontrolními případy**, bez selhání,
   chyb a vynechání (`run-vD4k94`, exit 0; `target/r19-r31/wagon-full.log`).
   Počty potvrzují čerstvé XML 98 modelových a 34 funkčních/kontrolních tříd.
   Publikace zatím čeká. Tento řez nepředstavuje přijetí všech směrů UC-023 ani
   výrobního přetečení a potravin v UC-036.

6. Následné CI `38466289267` pro `e43e32fc` prošlo 801 modelovými případy,
   třemi nativními balíčky a kontrolami publikace. Funkční sada měla 113 případů,
   jedno selhání TC27/07 a žádné chyby či vynechání. Všech osm TC31 prošlo.
   Herní publikace byla přeskočena; Pages `38466289130` uspěly. Artefakt
   `tests-functional` potvrzuje selhání prázdného stavového řádku při návratu
   na pole 31,16 po dalším tahu (`target/r19-r31/ci-r24/`).
7. Nový TC21/08 reprodukoval problém přes skutečný nativní přesun kurzoru:
   popis kolonie → popis tlačítka Další tah → návrat na totéž pole ukázal prázdný
   text. První příprava s TestFX `Motion.DIRECT` nezachovala stejné poslední
   pole, protože i tento režim interpoluje mezilehlé pohyby; nativní
   `MouseRobot.move` pak chybu doložil (čtyři funkční/kontrolní případy,
   jedno selhání, žádná chyba ani vynechání; `r24-status-native-before.log`).
   `MouseOverTileManager` si ponechával stejné pole, zatímco opuštění tlačítka
   vymazalo jeho popis. Malá oprava invaliduje cache při vstupu na mapu,
   bez opakovaného přepočítávání cesty při každém pohybu uvnitř stejného pole.
   Cílená regrese poté prošla **6 modelovými a 35 funkčními/kontrolními
   případy**, bez selhání, chyb a vynechání (`run-zWp3q1`, exit 0;
   `target/r19-r31/r24-status-fixed.log`). Výběr zahrnul
   `MouseOverTileManagerTest,ModelArchitectureTest,TC_21_F15_free_play_screen_navigation_test,TC_27_F19_europe_equipment_test,TC_30_F22_expert_purchase_test,TC_31_F23_wagon_unloading_test,PageVerificationContractTest`
   se stejným Docker příkazem a `-Dsurefire.failIfNoSpecifiedTests=false`.
   Všech osm TC21 včetně nové regrese a všech devět TC27 včetně původního
   selhání prošlo. Pět Java souborů prošlo společným formatterem se shodou
   tokenů a idempotencí. Úplné CI a nová publikace zatím čekají.

8. Následné úplné CI `38470444093` pro opravu `13001e6a` prošlo
   **802 modelovými a 114 funkčními/kontrolními případy**, bez selhání,
   chyb nebo vynechání; počty potvrzují čerstvé XML artefaktu
   `tests-functional` v `target/r19-r31/ci-r24-fixed/`.
   Prošly všechny tři nativní balíčky, kontroly i publikace; Pages
   `38470444072` uspěly a veřejná stránka obsahuje opravenou evidenci.
   Manifest vývojového kanálu identifikuje `dev-54-13001e6ac6b8`, úplný commit
   `13001e6ac6b89ede1fa6da06945eebd0c4f72354` a všechny tři platformy.
   Skutečně stažený [macOS ARM64 ZIP](https://github.com/jajir/microcol/releases/download/dev-54-13001e6ac6b8/MicroCol-macos-arm64-dev-54-13001e6ac6b8.zip)
   má 61 109 572 bajtů a SHA-256
   `9212a2b8409dc32820da113ebdb7dc1fb9c6e27e8beca5d006e8e4ef238e1418`,
   shodný s manifestem i `SHA256SUMS`. Ověřeno 13. října 2026;
   záznam `target/r19-r31/release-r24/verification.json`. Nejde o nové ruční
   spuštění této distribuce na všech platformách.

## Construction model implementation — 2026-10-13

1. Rozpracované [R-0025](requirements/done/R-0025.md) opravilo metadata existujících
   staveb, jejich společnou dostupnost, integritu fronty a její načítání.
   První přijatý řez metadat prošel **52 modelovými a třemi strukturálními
   případy** (`run-h4Sn6S`). Původní frontové testy ukázaly 18 skutečných
   selhání z 23 případů; jeden další chybný předpoklad testu o historickém
   předchůdci byl opraven na kontrolu skutečně existující budovy.
2. Validace uložených položek prošla **24 modelovými a třemi strukturálními
   případy** (`run-Ol9vPt`): 21 neplatných front, dvě platná načtení a
   architektura. Platné načtení nepřepisuje soubor a nespouští dokončení.
   Příprava testu JSON null měla dvě caller-owned chyby, nejprve převod
   do `JsonArray`, potom vynechání null při serializaci. Oprava na
   `JsonElement` a explicitní ukládání null prošla stejným výběrem;
   log `target/r19-r31/construction-save-null-fixture-fixed.log`.
3. Sdílená dostupnost prošla **125 modelovými a třemi strukturálními
   případy** (`run-k4ldgf`), včetně 60 nových případů hranic všech
   podporovaných projektů. Společný integrační běh následně provedl
   **141 modelových případů, jedno selhání, žádnou chybu ani vynechání**.
   Při chybějícím předchůdci staré dokončení spotřebovalo nástroje a vyhodilo
   výjimku; UI modul se po modelovém selhání nespustil.
4. Samostatná kontrola dokončení nyní před změnou fronty ověřuje živou
   kolonii, již existující cíl a aktuálního předchůdce. Neopakuje populační
   podmínku zahájení. Opravený výběr prošel **144 modelovými a třemi
   strukturálními případy**, bez selhání, chyb či vynechání (`run-soSK6J`,
   exit 0; `target/r19-r31/construction-completion-first.log`). Zahrnuje
   původní selhání, dokončení po poklesu populace, již postavený cíl,
   zánik kolonie a jednotku bez potřebné pily. Příkaz:
   `docker compose -f compose.ui-tests.yml run --build --rm ui-tests -Dtest=ConstructionAdmissionTest,ConstructionMetadataTest,ConstructionQueueSaveTest,ConstructionQueueIntegrityTest,ColonyBuildingQueueTest_queue,BuildingStatusTest,ModelArchitectureTest,PageVerificationContractTest -Dsurefire.failIfNoSpecifiedTests=false`.
   Historický název třídy fronty v tomto provedeném příkazu je správný pro
   daný běh; následné přejmenování níže mění budoucí výběry.
5. Širší výběr `-Dtest=*,!TC_*` provedl **946 modelových případů s jedním
   selháním a jednou chybou**, bez vynechání; UI modul se nespustil.
   Navíc oproti výchozímu výběru zahrnul dva případy
   `UnitColonistTestInCargo`, dva `ColonyBuildingQueueTest_queue` a historicky
   vyloučený `integration.ConnectorTest`. Obě závady byly ve staré přípravě
   vylodění: neúplné mocky už nesplňovaly přijatý živý vztah pasažér–loď
   a očekávaný text výjimky byl z doby před R-0022. Hlavní agent nahradil tuto
   malou přípravu skutečnou uloženou pozicí, zachoval oba případy a přidal
   ověření prázdného slotu, identity, pohybu a nezměněného stavu při odmítnutí.
   Názvy `UnitColonistInCargoTest` a `ColonyBuildingQueueTest` nyní odpovídají
   běžnému Maven discovery. Opravený širší běh prošel **946 modelovými a devíti dalšími případy**,
   bez selhání, chyb či vynechání (Docker exit 0; `run-paz8wv`,
   `target/r19-r31/construction-all-model-fixed.log`). Devět případů tvoří
   pět `SimpleOperationsTest`, jeden `EnvironmentTest` a tři strukturální
   kontroly. Změněné Java soubory prošly společným formatterem se shodou
   tokenů a idempotencí; pomocný seznam vstupů musel při přejmenování
   přestat zahrnovat již neexistující původní soubory.
6. Tento modelový výběr záměrně neprovádí hráčské třídy `TC_...`;
   jejich nedokončená aktualizace pro stavění a úplná regrese jsou stále
   nutné před publikací R-0025. Průběh lokálních zadání a oprav vlastní
   [evidence workeru](ai/local-qwen.md#r-0025-construction-admission-2026-10-13).

## Construction UI implementation — 2026-10-14

1. [R-0025](requirements/done/R-0025.md) now exposes the carpenter's construction
   control and a localized building/unit picker. Selection is a draft until
   explicit confirmation. A changed colony/model or reviewed population,
   predecessor, queue or resource state requires a new selection. Existing
   queue order/removal remains available through visible buttons. The model
   retains admission, progress, completion and persistence authority.
2. `TC_32_F24_construction_eligibility_test` uses its own F24 fixture. TC_01
   checks three/four residents, the missing immediate predecessor, Cancel,
   Escape and duplicate confirmation. TC_02 assigns two carpenters through the
   workplace UI, actually produces six hammers, preserves 64 assigned hammers
   while one tool short, unloads the final tool, completes exactly once and
   checks Continue before and after completion. The final acceptance also
   keeps a second warehouse entry and its ID/progress through completion; the
   model counterpart preserves 17 hammers on that following entry. TC_03 first builds the mill
   through UI, then exercises a mixed wagon/warehouse queue, both order controls,
   removal and Continue. TC_04 injects an external workforce change only to
   exercise stale confirmation; TC_06 similarly replaces the current game.
   TC_05 uses Czech settings and Tab/Space to open/confirm. TC_07 restores a
   structurally valid project without its predecessor and verifies that a real
   turn keeps its progress/tools and visible explanation.
3. TC22/TC_05 keeps its historical method ID but now expects a visible,
   keyboard-reachable control and model-derived population rejection. Six
   `QueueControllerTest` cases cover model/presentation ordering, removed items,
   rejected admission, a changed callback, a replaced model and a stopped game.
   Assertions remain in the named page verification methods for player scenarios.
4. The first isolated compilation (`run-P3kK5o`) failed because caller UI code
   used the package-private resident query. The existing immutable resident
   snapshot is now public; a caller controller-test setup was also changed to
   use an already supported wagon instead of a package-private unit factory.
   No behavior assertion was relaxed. The next run (`run-M1bOpA`) passed **74
   model cases, six controller cases and three source-contract cases**, but six
   of the twelve player scenarios failed. The focused list consumed Escape,
   leaving the modal open and preventing subsequent navigation. The dialog now
   handles Escape before the list; scenarios explicitly verify that the actual
   modal window closes, as well as checking unchanged game state.
5. The repair run **`run-EyvYNv` passed six controller cases and 15 functional/
   control cases** (seven TC32, five TC22 and three source-contract checks),
   zero failures, errors or skips, Docker exit 0. Main sources/resources/fixtures
   were compared byte-for-byte with the isolated tested versions before broader
   verification. Shared formatting passed on all 28 changed Java files with
   exact code-token preservation and idempotence.
6. The actual English/Czech picker screenshots and the colony's waiting state
   were inspected. Costs, unavailable reasons, confirmation and the visible
   `64 / 64` hammer and `19 / 20` tool state are readable. Screenshots are
   diagnostic evidence under the run's `microcol-test/warehouse-qa/`; they are
   not a substitute for the passing player assertions. This is automated
   interaction and visual inspection, not a player study.
7. Commands used in the isolated worker tree, after unloading GPT-OSS:

   ```sh
   docker compose -f compose.ui-tests.yml run --build --rm ui-tests '-Dtest=ConstructionAdmissionTest,ConstructionQueueIntegrityTest,QueueControllerTest,PageVerificationContractTest,TC_32_F24_construction_eligibility_test,TC_22_F15_free_play_capability_audit_test' -Dsurefire.failIfNoSpecifiedTests=false
   docker compose -f compose.ui-tests.yml run --build --rm ui-tests '-Dtest=QueueControllerTest,PageVerificationContractTest,TC_32_F24_construction_eligibility_test,TC_22_F15_free_play_capability_audit_test' -Dsurefire.failIfNoSpecifiedTests=false
   ```

8. Full main regression **`run-5NRq2p` passed 951 game-module cases in 105
   classes and 121 functional/control cases in 35 classes**, zero failures,
   errors or skips, Docker exit 0 (21 min 36 s). Fresh XML totals are recorded in
   `target/r19-r31/r25-full-main-evidence.json`. Production sources remained
   unchanged while this suite ran. Two completion tests were strengthened after
   its source snapshot to check preservation of a following queue entry; their
   separate rerun **`run-cpJfEl` passed 14 queue-integrity cases and ten
   functional/control cases** (seven TC32 plus three page-contract checks),
   zero failures, errors or skips, Docker exit 0 (1 min 46 s). Fresh XML totals
   are in `target/r19-r31/r25-completion-target-evidence.json`. Thus both the
   existing full suite and the final strengthened acceptance passed.
9. Main checkout acceptance commands:

   ```sh
   docker compose -f compose.ui-tests.yml run --build --rm ui-tests
   docker compose -f compose.ui-tests.yml run --build --rm ui-tests '-Dtest=ConstructionQueueIntegrityTest,TC_32_F24_construction_eligibility_test,PageVerificationContractTest' -Dsurefire.failIfNoSpecifiedTests=false
   ```

10. Publication **`dev-55-61789f816924`** is verified for commit
    `61789f8169248f7ec1987d48be4b750311dcc81f`. [CI](https://github.com/jajir/microcol/actions/runs/38480041688)
    passed all three native builds, build-tool checks, functional scenarios and
    publication; [Pages](https://github.com/jajir/microcol/actions/runs/38480041684)
    passed build/deployment. Fresh CI XML (`run-OcrDcI`) passed **951 game-module
    and 121 functional/control cases**, zero failures, errors or skips. The
    public analysis and source links were checked against the intended commit.
    The development manifest and per-build assets agree on the full SHA and all
    three platforms; the downloaded macOS ARM archive is **61,111,759 bytes** with
    SHA-256 `2747cbd808e0782e39e3017892f05e2873dacc13f2224fabca454c3a6c369d15`,
    matching both the manifest and `SHA256SUMS`. Publication evidence is in
    `target/r19-r31/release-r25/verification.json`; CI totals are in
    `target/r19-r31/r25-ci-evidence.json`.
11. The local source logs are `target/r19-r31/construction-ui-*.log`, with report
   totals in `target/r19-r31/r25-ui-target-evidence.json`. Worker attempts and
   caller repairs are recorded separately in the [worker evidence](ai/local-qwen.md#r-0025-queue-controller-2026-10-14).


## European relation model integration — 2026-10-14

1. Rozpracované [R-0026](requirements/done/R-0026.md), UC-069 / DIP-002/003,
   má zapojené ukládání úplných dvojic a modelové přechody mír/válka. Interní
   identita je nezávislá na upravitelném názvu hráče. Referenční mapy začínají
   v míru; všech 31 přibalených modelových pozic má explicitní seznam vztahů.
2. `EuropeanRelationModelTest` má 17 případů: symetrie a izolace třetích stran,
   nesprávný tah, cizí/odebrané identity, králové, neplatné opakované přechody,
   ukončená či nezahájená hra, přijatý mír, přejmenování a změna pořadí dat
   při skutečném diskovém uložení/načtení. Porovnání snapshotů chrání jednotky,
   pohyb, zásoby, vlastnictví i peníze před vedlejšími účinky.
3. `EuropeanRelationSaveTest` má 17 diskových případů včetně chybějícího seznamu,
   neúplných řádků, neplatných identit/stavu a duplicitních či obrácených dvojic.
   Každé odmítnutí zachovává původní bajty souboru. Osm `EuropeanRelationsTest`
   chrání samostatný datový kontrakt; dvě `FreePlayMapsTest` ověřují referenční
   světy včetně počátečního míru a uložených dvojic.
4. Reprodukční `run-3zhffH` vypnul pouze nové příznaky stop/game-over: oba
   vybrané lifecycle testy selhaly na stále běžící hře. Po obnově opravy prošel
   `run-PAfjpb`: **44 modelových + 3 source-contract případy**, nula selhání,
   chyb a vynechání. `GameManager` invaliduje zastavenou instanci před událostí
   a ukládá dosažený herní konec, aniž by odstranil čitelný aktuální tah.
5. Širší regrese na main `run-HEgCQZ`, Docker exit **0**, příkaz
   `docker compose -f compose.ui-tests.yml run --build --rm ui-tests
   '-Dtest=*,!TC_*' -Dsurefire.failIfNoSpecifiedTests=false`, prošla
   **994 modelovými případy ve 109 třídách a 9 podpůrnými případy ve 3 třídách**,
   nula selhání/chyb/vynechání. Explicitní wildcard zahrnuje i `ExtraConnectorTest`,
   který výchozí Maven výběr nespouští. Reporty jsou v
   `target/docker-ui/run-HEgCQZ/`; souhrn v
   `target/r26-draft/all-model-evidence.json`.
6. Nejnovější formátování zkontrolovalo 13 Java souborů, bez dalších změn,
   se zachováním tokenů a idempotencí. Lokální worker dodal pouze provider
   neplatných vstupů; [evidence](ai/local-qwen.md#r-0026-save-validation-cases-2026-10-14)
   odděluje jeho dva pokusy od implementace a skutečných kontrol hlavního agenta.
7. Tento běh ověřoval modelový základ, nikoli dokončení R-0026 nebo celé
   UC-069. Všechny `TC_...` byly výslovně mimo výběr. Další práce a skutečné
   výsledky jsou uvedené níže.

### European hostility and political report — 2026-10-14

1. Cílený `run-n3Tff9` prošel **68 modelovými/AI případy a 3 source-contract
   kontrolami**, nula selhání/chyb/vynechání. Následný širší `run-Sz9Rjx`
   (`'-Dtest=*,!TC_*'`) prošel **1014 modelovými + 9 podpůrnými případy**,
   Docker exit 0. Tyto běhy předcházely pozdější opravě úniku lodí a novému UI.
2. Hráčský `run-D95R6O` ověřil TC03 a tři source-contract kontroly, ale **TC04
   selhal**: po porážce obránců blokovala loď v přístavu běžný vstup dobyvatele.
   Očekávání ztráty kolonie a úniku lodi zůstala zachovaná. Hlavní agent doplnil
   společné plánování úniku před pohybem a použití dotazu také v AI.
3. Při tvorbě dalších regresí dva běhy (`run-lR21XW`, `run-ixUZ5r`) skončily
   na chybách kompilace testu: chybějící import a nesprávně zvolená metoda nákladu.
   `run-OZhjBu` spustil 75 modelových/AI případů a měl tři chyby přípravy
   nestandardní smíšené skupiny lodí; `run-3cCd4o` měl dvě. Registrační kontrola
   odhalila cizí vlastníky na jednom poli ještě před testovanou akcí. Příprava
   nyní nejprve registruje jednotky odděleně, potom vytvoří záměrný hraniční stav.
   Tyto neúspěšné běhy neověřují opravu ani hráčské scénáře.
4. Nové `TC_33_F25_european_relations_test` / `EuropeanRelationsPage` a fixture
   F25 jsou připravené pro politický přehled tří hráčů, zrušení, potvrzení právě
   jednou, Continue, mírové odmítnutí vstupu a válečné zabrání s vybavenou
   jednotkou, změněný vztah/model/jméno, zastavenou hru a českou klávesnici.
   V této fázi sedm hráčských scénářů ještě čekalo na ověření. Po opravě přípravy lodí prošlo
   **75 modelových/AI případů** v `run-eYTrYy`, bez selhání/chyb/vynechání.
   Celý reactor měl exit 1 kvůli kompilaci nového page objectu (přístup k neveřejné
   metodě); UI tímto během ještě nebylo ověřeno.
5. R-0026 zůstává rozpracované a nepublikované. Další kontrola musí zahrnout
   uvedené scénáře, TC03/TC04, širší modelové testy, vizuální kontrolu a úplnou
   hráčskou regresi. Lokální reporty vlastní `target/r26-draft/` a
   `target/docker-ui/`; nejsou náhradou následné CI/publikační evidence.

6. `run-PGxGlm` pak skutečně spustil TC03, TC04, sedm TC33 a tři
   source-contract kontroly: **12 případů, jedna chyba, nula vynechání**.
   Oba původní Crown scénáře prošly včetně úniku lodi z dobytého přístavu.
   Česká cesta chybně zkoušela vstoupit Tabem do nevybrané záložky; page object
   nyní používá Tab pro vstup do skupiny a šipky pro výběr záložky.
7. Běh zároveň zaznamenal `Not on FX application thread` při oznámení lidského
   dobytí kolonie. Dosavadní kontrola vlastnictví to neodhalila. Nová kontrola
   skutečně zobrazené zprávy selhala v `run-AB5iU1` (1 UI chyba + 3 úspěšné
   source-contract kontroly). Oprava předává zprávu přes `Platform.runLater`
   a ověří původní instanci hry. Hlavní agent vlastní tuto integrační opravu;
   nejde o chybu lokálně generovaného potvrzení války.
8. Snímky `run-PGxGlm` ukázaly čitelné tři dvojice a zalamování dlouhého jména.
   Pro Unicode emoji v kontejneru chyběl glyph. Zkoušený
   `fonts-noto-color-emoji` však ve snímku vykreslil prázdné místo; finální
   testovací image proto používá monochromatický `fonts-symbola` (ověření níže).

9. Po opravě `run-sFAN9W` prošlo **7 TC33 + 3 source-contract kontroly**,
   nula selhání/chyb/vynechání, Docker exit 0. Zpráva o zabrání kolonie je nyní
   výslovně ověřena jako zobrazené okno. České ovládání používá Tab/šipky/Space
   a Escape. Zkontrolované snímky potvrzují rozložení a zalomení dlouhých jmen;
   uvedený glyphový problém prostředí se tím neuzavírá. Chybně zadané jméno
   TC21 v tomto příkazu žádný TC21 nespustilo; skutečné navigační scénáře
   zahrnuje až následující úplný výběr `'-Dtest=*'`.
10. Úplná regrese `run-YyZwF5` dokončila **1021 modelových případů** bez
    selhání/chyb/vynechání a **128 hráčských/podpůrných případů s jednou chybou**,
    nula vynechání, Docker exit 1. TC33 všech sedm, TC03/TC04 a osm TC21 prošly.
    Selhal `TC23/02_menu_preserves_latest_departure`: starší snímek seznamu
    jednotek v `GamePaintService` předal do `PaintService.paintUnit` loď,
    kterou model mezitím přesunul na širé moře. Živý `getPlaceLocation()`
    pak vyhodil výjimku na JavaFX vlákně. Kontrola plavby nebyla oslabena;
    cílená reprodukce a oprava sdíleného vykreslování jsou rozpracované.
    Tento běh používal obraz s barevným fontem; samostatné ověření Symbola
    zaznamenává finální cílený běh níže.

11. Kontrola zdrojů Eclipse compilerem (`--release 11`, zapnutá validace Javadoc)
    nalezla 11 nových neúplných popisů parametrů/návratových hodnot v R-0026.
    Doplnění komentářů a sdílený formatter je odstranily bez změny vykonávaného
    kódu. Zbývá 10 upozornění z dřívějších změn pro závěrečný audit R-0032;
    tento běh není tvrzením o celé sadě bez upozornění. Evidence je v
    `target/r26-draft/source-validation-evidence.json`; formatter ověřil
    27 Java souborů, zachování tokenů a idempotenci.

12. Cílený `run-cEAatQ` reprodukoval chybu deterministicky: nová kontrola
    `TC23/02` nejprve ověřila vykreslení lodi na mapě, po skutečném odplutí pak
    předala tutéž loď do sdíleného painteru. Tento druhý krok selhal původním
    `getPlaceLocation()` (1 UI chyba + 3 úspěšné source-contract kontroly).
    Oprava čte `Unit.getPlace()` právě jednou a kreslí jen zachycené
    `PlaceLocation`; dodatečná kontrola typu následovaná novým živým čtením by
    ponechala souběhové okno. Explicitní orientace animací zůstává zachovaná.
    Drobnou integrační opravu provedl hlavní agent: předání této jedné metody
    a následná revize by převýšily vlastní změnu. Ověření opravy je v následujícím bodě.

13. Finální cílený `run-ZOs4ll` prošel **2 TC23 + 7 TC33 + 3 source-contract
    kontrolami**, nula selhání/chyb/vynechání, Docker exit 0. Pozitivní kontrola
    kreslí loď na mapě; po odplutí stejný painter bezpečně nechá podklad beze změny.
    Obě plavby zachovaly původní kontroly nákladu, Continue a návratu. V logu
    nebyla nalezena další výjimka vykreslování ani chyba JavaFX vlákna.
    Snímky `relations-initial.png`, `declaration-czech.png` a
    `declaration-long-name.png` potvrzují čitelný přehled, ovládání a zalamování;
    znak vlny je nyní viditelný. `fc-match ':charset=1f30a'` vybral Symbola.
    Jde o ověření linuxového testovacího obrazu, nikoli balení nového fontu do hry.
    Finální formatter zkontroloval 30 Java souborů, tokeny a idempotenci.
    Úplnou regresi po této opravě musí potvrdit povinné CI před publikací;
    předchozí lokální úplný běh zůstává zaznamenaný jako neúspěšný.

14. Publikace byla ověřena 14. října 2026: [CI 38491697779](https://github.com/jajir/microcol/actions/runs/38491697779)
    a [Pages 38491697769](https://github.com/jajir/microcol/actions/runs/38491697769)
    skončily úspěšně na `432e96b0f691cbbd80242597e98a53d998986259`.
    Čerstvý CI `run-WEFxnK` ověřil **1020 testů herního modulu a 128 hráčských/podpůrných
    případů**, nula selhání/chyb/vynechání. TC03, TC04, osm TC21, oba TC23 a všech
    sedm TC33 jsou v XML; log nemá zachycenou chybu asynchronního vykreslování.
    Lokální explicitní `-Dtest=*` navíc spouštěl existující `integration.ConnectorTest`,
    který běžný Maven podle dosavadního POM vylučuje; proto měl 1021 případů.
    Žádné vyloučení testu se touto změnou neměnilo.
15. Nativní reporty všech tří platforem mají po **1020 herních, 31 build-tool
    a 2 integračních testech**, vše bez chyb/vynechání. Samostatný build-tool job
    má 31 + 1. Vydání a vývojový manifest uvádějí `dev-56-432e96b0f691` a stejnou
    úplnou revizi; ověřeny jsou tři platformní záznamy a jejich assety/SHA256SUMS.
    Skutečně stažený [Mac ARM archiv](https://github.com/jajir/microcol/releases/download/dev-56-432e96b0f691/MicroCol-macos-arm64-dev-56-432e96b0f691.zip)
    má 61 123 089 bajtů a SHA-256
    `7761fb5009e8171e1362dfc2b440d2fc65cbd746605ba701ce80f4caa76167c6`.
    Publikovaný text R-0026 i zdrojové odkazy stránky architektury odpovídají
    této revizi. Lokální evidence vlastní `target/r26-draft/ci-functional-evidence.json`,
    `ci-native-tools-evidence.json` a `release/verification.json`.

## Rival first-colony preparation — 2026-10-14

1. Rozpracované [R-0027](requirements/done/R-0027.md) patří k zakládací části
   UC-068/DIP-001. První krok opravuje nekonečné hledání směru obklíčené jednotky.
   Reprodukční `run-Ujxgay` spustil tři `BoundedAiMovementTest`: jedna kontrola
   nedokončila pohyb do dvou sekund a selhala, dvě prošly. Po omezení hledání na
   konečný průchod sousedy prošel `run-sAeMOC`: **3 nové pohybové případy,
   8 EuropeanRelationAiTest a 3 page-contract kontroly**, bez chyb a vynechání.
2. Nový přibalený svět `free-play-rival-expedition.microcol` má francouzskou loď
   s kolonistou a žádné francouzské kolonie. Původní a dvouostrovní svět nadále
   splňují R-0015. `run-mZrKO0` prošel **4 modelovými případy**, včetně skutečného
   diskového round tripu, a **11 hráčskými/podpůrnými případy**: šest stávajících
   TC24, dva nové TC34 a tři kontroly struktury testů. Nula failures/errors/skips.
3. Aktuální `TC_34_F26_rival_first_colony_test` ověřuje běžnou volbu třetího světa,
   Start/Continue před prvním tahem a zachování zvolené mapy po přepnutí do
   češtiny. Kontroluje vlastníka, ID, nákladní vztah lodě/kolonisty, mír a absenci
   kolonií. Anglický a český snímek nastavení byl vizuálně zkontrolován: nové
   názvy i ovládací prvky jsou čitelné. Jde o automatické ověření, nikoli studii
   s hráči nebo důkaz kolonizační strategie.
4. Oba kladné běhy použily `docker compose -f compose.ui-tests.yml run --build
   --rm ui-tests` s `-Dsurefire.failIfNoSpecifiedTests=false`; první výběr byl
   `BoundedAiMovementTest,EuropeanRelationAiTest,PageVerificationContractTest`,
   druhý `FreePlayMapsTest,RivalExpeditionWorldTest,TC_24_F15_free_play_setup_test,TC_34_F26_rival_first_colony_test,PageVerificationContractTest`.
   Skutečné XML reporty a snímky zůstávají v `target/docker-ui/` příslušných
   izolovaných worktrees, odkazy na běhy a integraci v
   `target/r27-draft/navigation-evidence.json` a `map-setup-evidence.json`.
5. První dva běhy výše ověřovaly přípravu světa, nikoli zakládací AI.
   Následující kroky doplňují strategii a její skutečný průchod. Změny zatím
   zůstávají v pracovní kopii `main`; úplná regrese a publikace jsou otevřené.
   Dílčí zakládací cíl neuzavírá celé UC-068 o ekonomice a strategii soupeřů.

6. Sdílené zakládání nejprve odhalilo chyby v `run-91JTPK`: ze sedmi
   reprodukčních případů čtyři selhaly a dva skončily výjimkou (moře/hory,
   zastavená hra, částečné vrácení vybavení, okraj mapy a neúrodné okolí).
   Po opravě prošel `run-x0oL0L`: **51 modelových a 15 hráčských/kontrolních
   případů**, nula failures/errors/skips. Nových 16 `FoundingPreconditionsTest`
   ověřuje i identitu, pohyb, perspektivní dotaz pro pasažéra, šest okrajových
   souřadnic, obnovení a nepřevzetí již obhospodařovaného pole.
7. Při rozšíření ochrany okraje přibyl sedmnáctý modelový případ. `run-sNwYeq`
   prošel **52 modelovými případy**; z 15 UI/kontrolních případů selhal pouze
   nový platný přesun pracovníka po předchozím odmítnutí pole mimo mapu.
   Robotovo uvolnění tlačítka a vyprázdnění FX fronty předběhlo nativní
   dokončení tažení. Page object nyní používá existující čekání na
   `DRAG_DONE` také pro pole. Stejný TC34 byl znovu spuštěn v `run-GEqhIN`:
   **všech pět TC34 a tři page-contract kontroly prošly**, bez chyb/vynechání.
   Očekávaný přesun na (2, 15) ani výnos 4 nebyly změněny.
8. Nové TC34/03–05 skutečně založí kolonii na okraji, odmítnou hory přes
   tlačítko i klávesu a založí neúrodné stanoviště s obyvatelem v radnici.
   Snímky ukazují skutečná okrajová pole a vnitřního pracovníka na Arktidě;
   nevydáváme je za úplnou přejímku rozložení celé obrazovky (v zachyceném
   900px okně je část názvu vlevo oříznutá). První izolovaný pokus
   `run-6PeruE` navíc vyžadoval opravu testovacího mocku platné souřadnice
   v `ColonistEquipmentTest`; původní očekávaná výjimka a kontrola nezměněných
   zásob zůstaly zachované a následně prošly.
9. Zakládací výběr použil stejný Docker příkaz s
   `-Dtest=FoundingPreconditionsTest,ColonyIdentityTest,ColonyTest,ColonyFieldTest,ColonistEquipmentTest,ModelArchitectureTest,TC_02_F02_founding_colony_test,TC_14_F01_founding_colony_close_to_other_test,TC_20_F02_colony_names_test,TC_34_F26_rival_first_colony_test,PageVerificationContractTest`;
   poslední cílené opakování použilo `TC_34_F26_rival_first_colony_test,PageVerificationContractTest`.
   Reporty jsou v izolovaném `2026-10-14-r27-founding-query/target/docker-ui/`,
   souhrn v `target/r27-draft/founding-evidence.json`. Nejnovější výsledek
   každé vybrané třídy dokládá 52 modelových a 15 hráčských/kontrolních
   případů; nejde o jeden společný běh ani úplnou regresi.

10. Počáteční znalost mapy byla obnovována pouze člověku. `run-iHqAJl`
    spustil dva `RivalExpeditionWorldTest` a oba selhaly: soupeř neznal ani
    pole vlastní lodě. Model nyní odhaluje skutečné okolí jednotek všem
    vlastníkům. `run-tYgNmS` prošel **44 modelovými a 3 kontrolními případy**,
    včetně obou původně selhávajících kontrol, `VisibilityTest`, dvanácti
    nových čtecích plánovacích případů a předchozí zakládací/pohybové regrese.
11. Po zapojení do tahů proběhl `run-y9AeVC`: **46 modelových a 12
    hráčských/kontrolních případů**, bez failures/errors/skips. Dva skutečné
    modelové průchody stanovují tři tahy: loď (35,10) → (29,12) → (27,14),
    vylodění kolonisty 4 na (26,15) za jeho pohybový bod, založení ve třetím
    tahu. TC34/06 tentýž průchod provádí přes běžné nastavení, další tah a
    Continue během plavby; po třech dalších tazích zůstává jediná kolonie,
    stejný obyvatel a nezměněná lidská flotila i zlato. Snímek
    `rival-first-colony-map.png` byl prohlédnut: jméno, francouzská vlajka a
    stavový řádek jsou viditelné; člověk zůstává na mapě. Prošly i všechny tři
    `TC_26_F18_fog_of_war_test`, včetně ochrany neznámých polí.
12. `run-g1B6ja` prošel **20 modelovými a 3 kontrolními případy**. Obsahuje
    12 `FirstColonyPlannerTest`, 4 `FirstColonyJourneyTest` a 4
    `RobotLifecycleTest`. Skutečné tahy obklíčené flotily vracejí řízení
    člověku; zabrání-li člověk plánovanému břehu během plavby, soupeř přistane
    a založí jinde bez změny lidských jednotek. Zachycený skutečný callback AI
    po `stop` nic neprovede, zastavení během první akce nepohne druhou jednotkou
    ani neukončí tah. Živý kontrolní případ nadále provede obě akce a jediný
    `endTurn`. Čtecí plánovač pokrývá i neznámý terén, nedostatek pevninského
    jídla, pořadí volby, zákaz průzkumu přes širé moře a stejné rozhodnutí po
    obnovení. Úplná regrese a zveřejnění stále zbývají.
13. `run-y9AeVC` použil Docker příkaz s výběrem
    `FirstColonyJourneyTest,FirstColonyPlannerTest,RivalExpeditionWorldTest,VisibilityTest,FoundingPreconditionsTest,BoundedAiMovementTest,EuropeanRelationAiTest,ModelArchitectureTest,PageVerificationContractTest,TC_34_F26_rival_first_colony_test,TC_26_F18_fog_of_war_test`;
    `run-g1B6ja` vybral `FirstColonyJourneyTest,RobotLifecycleTest,FirstColonyPlannerTest,PageVerificationContractTest`.
    V obou případech platí `-Dsurefire.failIfNoSpecifiedTests=false` a skutečné
    výsledky pocházejí z XML v `target/docker-ui/`, nikoli z návrhu workeru.

14. TC34/07 provádí dvě skutečná kliknutí na další tah s francouzskou lodí
    uzavřenou horami: oba tahy skončí u člověka, kolonista zůstává na palubě,
    kolonie nevznikne a lidské jednotky/zlato zůstanou stejné. První
    `run-sWOoW2` zachytil chybu přípravy: uložený lidský tah začínal s lodí
    vyčerpanou na 0 bodech a běžný další tah jí doplnil 6. Porovnání všech
    polí potvrdilo, že jde o jediný rozdíl. Fixture nyní začíná s plným pohybem
    jako ostatní jednotky, očekávání celého snapshotu zůstalo zachované.
    Opakování `run-w3JjEJ` prošlo **všemi 7 TC34 a 3 source-contract kontrolami**,
    nula failures/errors/skips. Použitý výběr byl
    `TC_34_F26_rival_first_colony_test,PageVerificationContractTest` se stejným
    Docker příkazem a přepínačem jako výše. Podklady vlastní
    `target/r27-draft/blocked-ui-diagnosis.json` a `journey-evidence.json`.
15. Úplná regrese `run-34q6U7` prošla: **1063 modelových/herních a 135
    hráčských/podpůrných případů**, nula failures/errors/skips, Docker exit 0,
    doba 23:39. Příkaz byl `docker compose -f compose.ui-tests.yml run --build
    --rm ui-tests '-Dtest=*' -Dsurefire.failIfNoSpecifiedTests=false`.
    Čerstvé XML reporty obsahují 117 modelových a 37 funkčních testovacích tříd;
    všech sedm TC34 prošlo spolu s celou starší sadou. Log neobsahuje
    nezachycenou výjimku vlákna. Všech 40 souborů kontrolovaného checkpointu
    zůstalo během běhu beze změny; sdílený formatter předtím ověřil 25 Java
    souborů. Záznam vlastní `target/r27-draft/full-regression.log` a
    `full-regression-evidence.json`; XML jsou v `target/docker-ui/run-34q6U7/`.
    Výslovné `-Dtest=*` zahrnuje také běžně vynechaný `integration.ConnectorTest`;
    výchozí CI má tedy o jeden herní případ méně, bez změny výjimek. Lokální
    přejímka je dokončená; vzdálené CI, Pages a publikace ještě nejsou ověřené.

16. Vzdálené [CI 38503447587](https://github.com/jajir/microcol/actions/runs/38503447587)
    pro `a43507d5` prošlo 1062 modelovými/herními případy, všemi nativními
    balíčky a publikačními nástroji. Z 135 hráčských/podpůrných případů měl
    TC34/03 jednu chybu: druhé, platné přetažení pracovníka nedoručilo čekaný
    `DRAG_DONE`. První přetažení na neplatné pole skončilo a zachovalo obyvatele.
    Záznam s úrovní INFO neurčuje přesný důvod nedokončeného nativního gesta;
    není to selhání modelové aserce. Publikace byla správně vynechána, Pages
    `38503447615` prošly. Čerstvé XML a log vlastní
    `target/r27-draft/ci-functional/` a `ci-drag-diagnosis.json`.

17. Izolovaná oprava nad stejnou revizí mění pouze gesto v
    `ColonyScreen.dragFieldWorkerTo`: po stisku a krátkém pohybu uvnitř zdrojového
    pole čeká na skutečné `DRAG_DETECTED` a návrat jeho obsluhy; teprve pak
    přesune kurzor na cíl, uvolní tlačítko a čeká na `DRAG_DONE`. Náhodné čekání,
    opakování ani slabší očekávání se nepřidaly. Běh `run-X7yDOw` prošel všemi
    **13 případy**, nula failures/errors/skips, exit 0, doba 2:18. Výběr:
    `TC_34_F26_rival_first_colony_test,TC_01_F01_embark_disembark_test,TC_07_F07_producing_gooods_test,TC_17_F13_new_unit_in_colony_test,PageVerificationContractTest`
    s obvyklým Docker příkazem a `-Dsurefire.failIfNoSpecifiedTests=false`.
    Sdílený formatter ověřil jediný změněný Java soubor. Log je
    `target/r27-draft/ci-drag-check.log`; výsledky a původní selhání vlastní
    `ci-drag-diagnosis.json`. Celou sadu musí znovu potvrdit CI opravného commitu;
    změny rozpracovaného R-0028 nejsou součástí této opravy.

18. Opakované [CI 38505754614](https://github.com/jajir/microcol/actions/runs/38505754614)
    pro `afcdb67d` znovu prošlo všemi 1062 herními testy a nativními balíčky,
    ale z 135 hráčských/podpůrných případů selhal TC34/03. Nová diagnostika
    upřesnila fázi: druhé gesto nevyvolalo `DRAG_DETECTED`, takže nešlo pouze
    o chybějící oznámení dokončení. Přesné nativní načasování z INFO logu
    odvodit nelze; publikace byla znovu vynechána. Pages `38505754829` prošly.
19. Gesto nyní nejprve ověří běžný pohyb ukazatele ve zdrojovém poli a skutečný
    stisk; až potom zahájí přetažení pohybem 15 px uvnitř 45px pole. Zachovává
    kontrolu zahájení i dokončení, stejný časový limit, úklid tlačítek a všechny
    herní aserce. Izolovaný `run-Ddq9Mm` prošel **13 případy**, nula
    failures/errors/skips, exit 0, doba 2:23; výběr odpovídá bodu 17.
    Sdílený formatter znovu ověřil jediný Java soubor. Čerstvá evidence:
    `target/r27-draft/ci-native-rearm-check.log` a `native-rearm-evidence.json`.
    Plná vzdálená regrese a zveřejnění této opravy nadále zbývají.


20. [CI 38507855238](https://github.com/jajir/microcol/actions/runs/38507855238)
    pro `6db8473d` znovu selhalo pouze na TC34/03: po odmítnutém přetažení
    nedorazil běžný pohyb myši ve zdrojovém poli. Všech **1062 herních případů**
    a dalších **134 ze 135 hráčských/podpůrných případů** prošlo; nativní balíčky
    a build kontroly také. Publikace byla vynechána, Pages `38507854874` prošly.
    Příčina v nativním vstupu zůstává neprokázaná. Diagnostický `c2c96e60`
    zaznamenává skutečné události scény, souřadnice, bounds plátna a zaměření;
    nemění gesta ani očekávání. Před odesláním prošel TC34/03 se třemi
    strukturálními kontrolami (**4 případy**, `run-amABsR`, nula
    failures/errors/skips). Nové CI `38510205972` má dodat vzdálenou stopu;
    tato instrumentace sama o sobě není prokázanou opravou.

21. Diagnostické [CI 38510205972](https://github.com/jajir/microcol/actions/runs/38510205972)
    zachytilo konkrétní rozdíl: druhé gesto opustilo plátno při (846, 361)
    a vrátilo se na požadovaný zdroj (872, 340) událostí `MOUSE_ENTERED` bez
    stisknutého tlačítka; další `MOUSE_MOVED` už nepřišlo. Bounds plátna byly
    stejné a okno mělo fokus. To prokazuje chybné očekávání jediného typu
    události, nikoli vnitřní příčinu chování GTK. Helper nyní přijímá oba typy
    návratu na zdroj, zachovává kontrolu stisku, zahájení i dokončení drag/drop
    a stejných pět sekund. Dočasné trasování je odstraněné. CI navíc selhalo
    na TC30/04 kvůli prázdnému stavovému řádku; příčina tohoto samostatného
    selhání není prokázaná. Publikace byla vynechána.
22. Oprava vstupu prošla v `run-qLhRQy`: **14 hráčských/podpůrných případů**,
    nula failures/errors/skips, exit 0, doba 3:40. Výběr tvoří všech sedm TC34,
    TC01, TC07, TC17, TC30/04 a tři strukturální kontroly, se standardním Docker
    runnerem a `-Dsurefire.failIfNoSpecifiedTests=false`. TC30/04 zde prošel
    bez cílené změny svého chování; nejde o důkaz odstranění jeho příčiny.
    Čerstvá XML vlastní `target/docker-ui/run-qLhRQy/`, log a souhrn
    `target/r27-draft/native-entry-check.log` a `native-entry-evidence.json`.
    Sdílený formatter ověřil jeden změněný Java soubor. Konečnou společnou
    revizi R-0028 musí přijmout úplné CI; další požadavky se podle zadání
    z 2026-10-14 nezahajují.

## Attack initiation integration — 2026-10-14

1. Rozpracované [R-0028](requirements/done/R-0028.md) pokrývá UC-072 a
   COMBAT-001/002/003 s návazností na DIP-002. `TC_35_F27_attack_eligibility_test`
   používá `F27-attack-eligibility.microcol`: ozbrojeného kolonistu, neozbrojeného
   pracovníka, ozbrojeného jezdce s profesí mistra kováře, vůz a francouzského
   obránce. Hlavní hráčská cesta používá skutečné gesto na mapě, potvrzení,
   běžné uložení a Continue. Úpravy modelu během dialogu výslovně simulují
   změněný stav; řízený generátor volí obě již existující větve výsledku boje.
2. TC35/01 ověřuje čtecí náhled mírového útoku. /02 ověřuje povinné potvrzení
   při skrytém rádci, skutečná jména/obrázky účastníků a Cancel/Escape/zavření.
   /03 pokrývá neozbrojeného pracovníka a vůz. /04–06 odmítají změnu vybavení,
   polohy cíle a celé hry bez dalších účinků. /07 potvrzuje útok specialisty,
   vyčerpání pohybu, Continue a reset předchozího souhlasu. /08 ověřuje češtinu.
   /09 provádí už nepřátelský útok bez skrytého volitelného rádce a kontroluje
   přeživšího obránce i po Continue.
3. `run-53UF9h` prošel **90 modelovými/AI a 19 hráčskými/kontrolními případy**,
   nula failures/errors/skips, Docker exit 0, doba 2:10. Výběr byl
   `TC_35_F27_attack_eligibility_test,TC_33_F25_european_relations_test,PageVerificationContractTest,AttackBoundaryTest,AttackOrderTest,AttackRoleTest,EuropeanHostilityTest,EuropeanRelationAiTest,FirstColonyPlannerTest,ModelArchitectureTest`
   s příkazem `docker compose -f compose.ui-tests.yml run --build --rm ui-tests`
   a `-Dsurefire.failIfNoSpecifiedTests=false`. Všech devět TC35 a všech sedm
   dosavadních TC33 prošlo. XML jsou v `target/docker-ui/run-53UF9h/`, log je
   `target/r28-draft/integration-check.log`. Další kontrola zastavení AI mezi
   jednotlivými akcemi a úplná regrese dosud zbývají; R-0028 není publikovaný.
4. Modelové hranice zahrnují deset kombinací rolí/domén, dvanáct nepřípustných
   stavů, lodní pohyb, dvanáct změn potvrzeného návrhu, cizí model, smíšené
   obsazení a oba výsledky boje. Obnovená hra postupuje skutečnými tahy do
   dalšího tahu vlastníka přeživší jednotky. Anglické a české snímky dialogu
   byly prohlédnuty v předchozím úspěšném `run-ituNEe`; obě strany zobrazují
   správné a odlišné vybavení, varování ani tlačítka se neřežou.
5. Počáteční `run-8wW4qZ` reprodukoval chybějící náhled útoku. Při přípravě
   testů caller opravil chybný import, nesprávné klonování typovaného fixture
   objektu a šest bodů pohybu přidělených čtyřbodové fregatě. Herní snapshot
   záměrně vynechává pouze `focusedField`: výběr jednotky mění polohu pohledu.
   Jednotky, pohyb, diplomacie, kolonie i ostatní uložený stav zůstávají
   porovnávané. Starší neúspěšné běhy nejsou počítané jako přejímka.
6. Výpočet síly a šancí zůstává v R-0029. Chybějící role, korzárské výjimky
   a obecné vyjednávání nejsou touto přejímkou uzavřené. Dvě lokální AI/role
   delegace a jejich skutečné výsledky vlastní [provozní evidence](ai/local-qwen.md).

7. Dodatečný případ zastavení po skutečném modelovém kroku nejprve selhal v
   `run-M8uv3z`: robot ještě zaútočil po `stop`. Dvě cílené opravy v rámci stejné
   lokální delegace doplnily stávající `isActiveTurn` před každou další akcí
   a zachovaly kontrolu vlastníka jednotky. Izolovaný `run-7VaET0` prošel **94
   modelovými/AI a 3 kontrolními případy**, nula failures/errors/skips, exit 0.
   Výběr: `EuropeanRelationAiTest,RobotLifecycleTest,FirstColonyPlannerTest,AttackBoundaryTest,AttackOrderTest,AttackRoleTest,EuropeanHostilityTest,PageVerificationContractTest`
   se stejným Docker příkazem jako výše. Původní živá větev nadále bojuje;
   zastavená větev provede právě jeden již dokončený krok a další boj ani
   nalodění nespustí. Log vlastní `target/r28-draft/ai-stop-check.log`.

8. Úplný lokální `run-jcIFvV` (`-Dtest=*`) dokončil **1114 modelových a 144
   hráčských/podpůrných případů**. Modelové případy a 143 hráčských/podpůrných
   případů prošly, včetně všech devíti TC35. Jediné selhání TC04 postrádalo
   očekávanou ztrátu kolonie: původní královští útočníci ve fixture nebyli
   ozbrojení. Nula errors/skips, žádná nezachycená výjimka vlákna, exit 1.
   Původních 30 souborů checkpointu zůstalo beze změn. Důkazy:
   `target/r28-draft/full-regression-evidence.json`, `full-regression.log`
   a čerstvá XML `target/docker-ui/run-jcIFvV/`. Tento běh není zelená přejímka.
9. Stejně neozbrojené vojáky vytvářela produkční královská továrna. Nový
   `RoyalExpeditionRoleTest` to reprodukoval (`run-ErblLA`, jeden neúspěšný
   modelový případ). Inicializace nyní přidává muškety; F04 výslovně vybavuje
   svých 17 útočníků, ostatní data a původní herní aserce TC04 zůstaly.
   TC03 nově počítá ozbrojené královské pozemní jednotky podle role hráče,
   minimálně tři. Caller opravil i vlastní přípravu nového testu: před uložením
   musí normálně zahájit hru. Dřívější `run-PToN2S` s touto chybou se nepočítá
   jako ověření opravy. `run-XRWRk0` poté prošel **23 modelovými a 5
   hráčskými/podpůrnými případy**, nula failures/errors/skips, exit 0, doba 2:24.
   Výběr: `RoyalExpeditionRoleTest,AttackRoleTest,AtlanticVoyageTest,ModelArchitectureTest,TC_03_F03_declare_independence_test,TC_04_F04_loosing_battle_test,PageVerificationContractTest`
   se standardním Docker runnerem a `-Dsurefire.failIfNoSpecifiedTests=false`.
   Oprava zachovává výbavu v nákladu i po uložení/načtení; další úplná regrese
   musí potvrdit společnou výslednou revizi. Evidence: `royal-role-evidence.json`
   a `royal-role-fixed-check.log` v `target/r28-draft/`.


## Uzavření publikačního ověření R-0027/R-0028 — 2026-10-14

Při migraci požadavků byl zkontrolován úspěšný [CI běh 38584259123](https://github.com/jajir/microcol/actions/runs/38584259123)
na `062f8ce9ec5e1d04f85bc2f182346417a2274f6b` a stažen artefakt
`tests-functional`. XML dokládají **1 115 modelových a 144 hráčských/kontrolních
případů**, nula failures/errors/skips; zahrnují všech sedm TC34 a devět TC35.
Nejde o nový lokální běh při úpravě dokumentace. Kopie zpráv je v
`target/requirements-migration/ci-functional/`.

Tento následný úplný běh uzavírá výše zaznamenané čekání na regresi a publikaci;
staré neúspěšné běhy zůstávají historií. Distribuce
[dev-70-062f8ce9ec5e](https://github.com/jajir/microcol/releases/tag/dev-70-062f8ce9ec5e)
a její manifest potvrzují stejnou revizi a tři platformy. Samostatná
[kompatibilita 38584258925](https://github.com/jajir/microcol/actions/runs/38584258925)
prošla na Linuxu/Windows a Java 11/17, včetně opravy úklidu testu R-0037.

[R-0050](requirements/done/R-0050.md) a [mapa migrace](requirements/migration-2026-10-14.md)
odlišují tuto přijatou část od nových požadavků. Úspěšný TC30 například stále
obsahuje staré očekávání spotřeby rudy; oprava výroby R-0061 tím není ověřena.

## TC27/TC30 stability analysis — 2026-10-15

1. [R-0112](requirements/done/R-0112.md) zachycuje požadavek na diagnostiku a
   stabilizaci TC27/07 a TC30/04. Následuje kontrola existujících výsledků
   z 14. října, nikoli nová testovací kampaň nebo ověřená oprava.
2. Plné CI pro [a35620ae](https://github.com/jajir/microcol/actions/runs/38552266613)
   a [2d701dda](https://github.com/jajir/microcol/actions/runs/38568564709) prošlo.
   [0825596c, pokus 1](https://github.com/jajir/microcol/actions/runs/38578635563/attempts/1)
   dokončil 144 funkčních/kontrolních případů s jediným selháním: TC27/07
   očekával ve stavovém řádku text `26`, ale řádek byl prázdný. Git objekty
   `microcol-game`, `microcol-test`, `compose.ui-tests.yml`, `scripts/ui-tests`,
   kořenového `pom.xml` a vývojového workflow jsou mezi těmito třemi revizemi
   shodné. Shoda zdrojů neprokazuje shodné časování ani obsah sestavených obrazů.
3. Dodatečná kontrola **15. října 2026** potvrdila, že i
   [pokus 2 stejné revize](https://github.com/jajir/microcol/actions/runs/38578635563/attempts/2)
   selhal v TC27/07: tentokrát očekával text `37`, opět dostal prázdný řádek.
   Znovu šlo o 144 případů, jedno selhání, žádné chyby ani vynechání.
   [CI pro f96b5710](https://github.com/jajir/microcol/actions/runs/38551980402)
   mělo stejný počet případů a jediné selhání TC30/04 na textu `24`.
4. Oba scénáře používají společné mapové pomůcky. Zdroj
   [GamePage](../microcol-test/src/test/java/org/microcol/page/GamePage.java)
   po přesunu kurzoru ihned kontroluje text bez explicitního čekání na jeho
   aktualizaci; to je podezřelé místo, nikoli doložená příčina. Starší
   [oprava návratu na stejné pole](#wagon-unloading-implementation-2026-10-13)
   měla reprodukovanou chybu aplikace. Samotný podobný příznak neprokazuje,
   že nová selhání mají tutéž příčinu, ani že jsou všechna falešným poplachem.
5. Pozdější [CI 062f8ce9](https://github.com/jajir/microcol/actions/runs/38584259123)
   a [9af7d9f9](https://github.com/jajir/microcol/actions/runs/38597668566) prošla.
   Tyto zelené běhy neuzavírají příčinu proměnlivých výsledků. Četnost náhodných
   selhání nebyla změřena řízeným opakováním stejného výběru a prostředí;
   smíšené historické běhy nelze použít jako takovou statistiku.


## R-0112 diagnostic campaign — 2026-10-15

1. Selected requirement: [R-0112](requirements/done/R-0112.md). Baseline
   `71074fc67df25e4b44290f86e6d87ddc210a8bd4`, clean isolated checkout.
   Local Docker uses Linux amd64 emulation on Apple Silicon, Xvfb/Openbox and
   unchanged Compose limits. Two fresh combined selections of TC27/07 and
   TC30/04 passed **2/2 each**, no failures/errors/skips, Maven durations
   **2:37 and 2:33**. A third diagnostic run with input/status logging also
   passed **2/2**, **2:34**. These passes do not invalidate the earlier CI failures
   or establish their cause. Raw local records: `target/r112/` and the attached
   worktree's `target/docker-ui/`.
2. Temporary test-side tracing records map input, coordinates, bounds, focus,
   movement state, hover cache, status changes and action boundaries. It does
   not change the gesture or add waits to obtain a pass. Application code,
   fixtures and original assertions remain unchanged in the published diagnostic
   revision. This is investigation, not an accepted repair.
3. The manual `TC27 and TC30 stability campaign` workflow runs the exact two
   suspect methods, each in its own JVM, in three or ten fresh Docker runs.
   It records the source/image/fixture identity, stops on the first failed run
   and uploads every result under `tc27-tc30-stability`. Three is the diagnostic
   budget; ten is acceptance after repair. The existing complete suite remains
   the development publication gate. No retries or skipped failures count as
   acceptance.

4. Native Linux diagnostic campaign [38690874108](https://github.com/jajir/microcol/actions/runs/38690874108),
   source `99e6c14b9a0a935bed838f313e21cb56f65be129`: all **three runs passed
   2/2**, zero failures/errors/skips. TC27 durations **47.922–48.238 s**, TC30
   **58.201–58.562 s**. Runtime: amd64, Temurin **17.0.18+8**, same Glass robot,
   Xvfb/Openbox and software rendering as the earlier failure. Image:
   `sha256:d927242a3a87425bf51b0445748c518c6f3fc2f01630e9dca56350ea0696c3c9`.
   The observed map moves were delivered with input enabled and animation idle.
   No cause is established from successful traces. Raw downloaded artifact:
   `target/r112/native-diagnostic-3/`.
5. Since isolated journeys did not reproduce the failure, the manual campaign
   also offers `scope=classes` to preserve surrounding TC27 and TC30 scenarios
   within each class JVM. This is a distinct diagnostic selection, not a retry
   erasing a failure. Keep the source, first result and actual selection for
   every campaign. The complete suite still provides the wider acceptance.

6. Native full-class campaign [38692014520](https://github.com/jajir/microcol/actions/runs/38692014520),
   source `cc9d5cbf949c8b60332d25439850633e3521a258`, reproduced the original
   **TC27/07 empty status for tile 37,16** on repetition three. Runs
   `run-GIiNJK` and `run-TFIhF8` passed **16/16**; `run-ccY1A9` ran **16 with
   1 failure, 0 errors/skips** (TC27: 9/1; TC30: 7/0). Durations **3:06, 2:55,
   2:36**. Every result is retained; this is not a calibrated random-failure rate.
   Image `sha256:c5a019422810c2b1c43e08381d627b3dc96341f4a120714b3210bb64ae923b64`,
   unchanged F19/F22 fixture hashes, same recorded native runtime. Raw artifact:
   `target/r112/native-classes-3/`.
7. The failed action delivered **187 `MOUSE_MOVED` events**, including the exact
   target screen position **395,485** / canvas position **295,385**. All reported
   **focus=true, moving=false, enabled=false**, empty hover cache/status. The
   human `TurnStartedEvent` was emitted at **06:30:16.865 UTC**; movement began
   at **06:30:18.143** and the empty assertion failed at **06:30:18.467**. No
   subsequent turn change appears between them. Missing/delayed mouse delivery,
   off-target positioning and an unfinished unit animation do not explain this
   captured failure.
8. Source inspection identifies a stale ownership cache: `ListenerManager`
   dispatches each turn notification/listener on a separate executor, while
   `GamePanelController.onTurnStarted` overwrites `humanIsOnTurn` from the event's
   historical player. A delayed computer notification can therefore overwrite
   the human notification after the model has already returned control. The
   selected repair reads the existing live model ownership instead of keeping
   that duplicate state; deterministic old-notification regression and acceptance
   remain pending. No global event-dispatch redesign is selected.
9. The separate full diagnostic suite [38690873812](https://github.com/jajir/microcol/actions/runs/38690873812)
   passed **1115 game + 144 functional checks**, zero failures/errors/skips.
   Its release job failed with **HTTP 403, Cannot create draft release**; that
   diagnostic revision is not claimed as a published distribution. This does
   not change the failed full-class measurement above.


## R-0112 map input repair — 2026-10-15

1. [R-0112](requirements/done/R-0112.md) removes `GamePanelController`'s cached turn
   owner. Input now uses the current player in the existing live model;
   unloaded/stopped games and computer turns remain disabled. The model's current
   player and the controller's movement flag are `volatile` across model/FX
   threads. Existing animation behavior and player gestures are preserved.
   Temporary verbose input tracing is removed; a failed status assertion retains
   expected/actual text plus input-enabled and movement state.
2. Negative control kept the old cached predicate, with constructor dependency
   wiring only, and delivered the prior computer notification after the human
   turn. `TC21/09` verified the model still belonged to the human, then reproduced
   **empty status instead of tile 24** before native colony selection. Its Docker
   run had **1 failing UI case + 3 passing source-contract cases**, no errors/skips.
   The separate unit run had **3 expected ownership failures**, **1 test setup
   error** (the caller incorrectly specified mocking a final event in this module)
   and **5 passing MouseOverTileManager cases**. That error was corrected using
   real events; it is not evidence of an application failure.
3. After the repair, Docker selection `GamePanelControllerTest`,
   `MouseOverTileManagerTest`, `PageVerificationContractTest`, TC21/08–09,
   TC27/07 and TC30/04 passed **9 game + 7 functional checks**, zero
   failures/errors/skips. The UI reproducer uses a controlled stale UI notification
   and actual native mouse hover/colony opening; it does not change model ownership,
   goods or time to obtain a pass. Evidence: `target/r112/negative-control*.log`,
   `target/r112/fix-targeted.log` and the attached worktree's exported Docker results.
4. Acceptance source is the clean commit
   `e273f7f07d74a9de7b534a48f22c37c228f46b43`. The completed checks below have
   **zero failures, errors or skips**. Local Docker uses amd64 emulation on Apple
   Silicon; CI uses native amd64 Linux. These are separate measurements.

   | Environment and selection | Result | Maven duration | Evidence |
   | --- | --- | --- | --- |
   | Local Docker, complete regression | 1119 game + 145 functional checks passed | 24:27 | `target/r112/local-acceptance/full.json`, `run-gkGeED` |
   | Native CI, complete regression | 1119 game + 145 functional checks passed | 17:46 | [38697135103](https://github.com/jajir/microcol/actions/runs/38697135103), `run-hoG1Xd` |
   | Local Docker, complete TC27 + TC30 classes | 10/10 runs, 16 cases each; 160 passed | 4:05–4:07 per run | `target/r112/local-acceptance/summary.json`, all ten exported result directories |
   | Native CI, complete TC27 + TC30 classes | 10/10 runs, 16 cases each; 160 passed | 2:54–3:06 per run | [38697134714](https://github.com/jajir/microcol/actions/runs/38697134714), `target/r112/native-acceptance-10/` |

   Both campaigns completed within their 45-minute budgets; the ten local runs
   took approximately **41:35** wall time in total. Each campaign stops at its
   first failure and preserves all first results. No automatic retry, timeout
   increase or weakened assertion is part of this change. A finite successful
   sample does not prove that all intermittent UI failures are impossible.
5. Campaign metadata records the exact selection, clean source and unchanged
   F19/F22 fixture hashes. Local image:
   `sha256:fa136bbcf55270c61eff5b096f1a04505f9aa2f597fc450b729b02d4d1216a07`;
   native campaign image:
   `sha256:7a9c26c5ce0f691067c92e2f8d28e301a13f5892be9407a68f8be8ad364e813a`.
   Raw records are under `target/r112/local-acceptance/` and
   `target/r112/native-acceptance-10/`; exported local reports remain in the
   attached worktree's `target/docker-ui/`. Every repetition uses a fresh
   container and each class its own JVM.
6. Candidate publication is independently verified: development workflow
   [38697135103](https://github.com/jajir/microcol/actions/runs/38697135103)
   published [dev-76-e273f7f07d74](https://github.com/jajir/microcol/releases/tag/dev-76-e273f7f07d74).
   The release and downloaded channel manifest identify the same full commit
   and all three platforms. Documentation
   [38697135139](https://github.com/jajir/microcol/actions/runs/38697135139) and
   compatibility [38697135095](https://github.com/jajir/microcol/actions/runs/38697135095)
   also passed. The final acceptance record changes documentation only;
   application, test and runner sources remain those accepted above.


## R-0051 calendar and Continue — 2026-10-15

1. The shared calendar now converts both the current date and historical round
   indexes with the accepted annual/seasonal schedule. Explicit later starts keep
   their own seasonal base. The bundled `thrive` mission now begins in 1490;
   existing independence eligibility remains owned by R-0105. Continue no longer
   triggers a fresh-turn autosave; explicit menu-position saves remain available.
2. `TC_37_F29_calendar_boundaries_test` uses F29 fixtures for 1599 and a later
   1630 winter. Its first three linear scenarios inspect the actual displayed date
   across 1600 seasons and 1601, preserve winter through Continue, and check both
   bytes and modification time of the opened recovery save. TC21's nine existing
   navigation/Continue scenarios retain their original assertions. Calendar
   acceptance is only part of UC-004; nationality, difficulty and expedition
   profile acceptance remain open.
3. The caller applied the shared Eclipse formatter to all seven final changed Java
   files, checking exact code-token preservation and idempotence. The first two
   targeted builds exposed caller-written test compatibility errors: parameterized
   JUnit was absent in `microcol-test`, then its Mockito version lacked
   `verifyNoInteractions`. Ordinary JUnit tests and the existing Mockito API fixed
   those source errors without adding dependencies. Review then identified unsupported final-class mocking in those four
   listener checks. The caller replaced them with TC37 scenarios observing real
   recovery files for all fresh/resumed and human/computer combinations. The
   installed mock library has no inline support; no dependency was added.
4. The corrected Docker/Xvfb/Openbox targeted run passed **59 calendar cases,
   12 player scenarios and 3 page-contract checks**, with zero failures, errors or
   skips. Command selection: `CalendarTest,CalendarSeasonTest,CalendarSeasonsTest,
   CalendarStoreTest,CalendarInvalidCreationTest,TurnStartedListenerTest,
   PageVerificationContractTest,TC_37_F29_calendar_boundaries_test,
   TC_21_F15_free_play_screen_navigation_test`, with
   `-Dsurefire.failIfNoSpecifiedTests=false`. Fresh XML is in
   `target/docker-ui/run-PoUuN7/`, log `target/r51/targeted-compatible.log`.
   The listener test's tag was corrected after this snapshot; its four cases were
   not executed in this targeted run and require the full run below.
5. The full local Docker run (`target/docker-ui/run-Q9KKay/`,
   `target/r51/full.log`, 24:47) passed **1153 game tests** and **148 UI-module
   cases**. Four additional caller-written listener tests errored before their
   assertions because Mockito could not mock final `GameController`. No other
   failure/error/skip occurred. This is a failed run, not full-suite acceptance.
   TC37 now owns equivalent real-file checks without final-class mocks; targeted
   repair and the complete Linux CI run must pass before acceptance. Overall
   UC-004 profile acceptance remains open.

6. The repaired targeted Docker run passed all **7 TC37 scenarios and 3
   page-contract checks**, with zero failures/errors/skips; fresh XML is in
   `target/docker-ui/run-n0eDex/`, log `target/r51/autosave-fixed.log`.
   It selected `TC_37_F29_calendar_boundaries_test,PageVerificationContractTest`
   with `-Dsurefire.failIfNoSpecifiedTests=false`. The complete CI run remains the
   final acceptance gate for the combined source before closing R-0051.

7. The complete [Linux CI run](https://github.com/jajir/microcol/actions/runs/38747926082)
   for `48d6cfadae3445fe03d98dd1e82c749b0133987d` passed **1153 game tests and
   152 UI-module cases**, with zero failures/errors/skips. The latter includes
   all seven TC37 scenarios; no mocked listener test remains. Downloaded XML is
   under `target/r51/ci-functional/docker-ui/run-sxCcLk/`. This is the clean full
   regression acceptance following the local test repair. All four Java 11/17
   Windows/Linux compatibility jobs also passed in
   [run 38747926164](https://github.com/jajir/microcol/actions/runs/38747926164).

## R-0052 partial turn-order delivery — 2026-10-15

1. The partial [R-0052](requirements/done/R-0052.md) delivery orders registered actors
   by stable nation identity and their associated Crowns. Each existing actor's
   preparation advances voyages and scheduled immigration, processes colonies,
   then refreshes movement and ongoing unit work. This covers part of UC-005;
   native actors, absent political phases and preparation-time action/save
   exclusion are not claimed as implemented.
2. `NationalTurnOrderTest` has eleven cases for shuffled storage, absent nations,
   human selection/display names, Crown association, fresh rosters, identical
   registered actors and actual save/Continue. `NationalTurnPreparationTest`
   checks real arrivals, immigration before birth, colony food, movement and
   repeat Continue, plus plowing's effect only on the following colony update.
   `TC_36_F28_national_turn_order_test` observes all eight actors across two
   rounds and preserves the complete saved position across two Continue actions.
3. Caller test corrections preserved the acceptance: use the existing misspelled
   `onUnitArraviedToColonies` listener API, use a valid 200-corn producing colony
   for birth rather than an over-capacity fixture, and set the existing game-over
   regression to Dutch (now the last present nation) instead of Spain. The first
   targeted build failed compilation; the second executed 37 model cases with
   one failure and one fixture error. Neither is acceptance evidence.
4. Remaining R-0052 work is unchanged: reject reentrant/concurrent partial saves
   and actions throughout preparation, preserve the prior valid save on failure,
   and verify failed-preparation recovery. The requirement stays `ready for
   development`; this publication does not mark it done.
5. The corrected targeted Docker/Xvfb/Openbox run passed **37 model cases and
   4 UI/support cases**, with zero failures/errors/skips. Selection:
   `NationalTurnOrderTest,NationalTurnPreparationTest,EuropeanRelationModelTest,CalendarTest,TC_36_F28_national_turn_order_test,PageVerificationContractTest`,
   with `-Dsurefire.failIfNoSpecifiedTests=false`. Fresh evidence is in
   `target/docker-ui/run-4V9AeR/`, log `target/r52/publish-targeted-fixtures.log`.
   Shared Eclipse formatting preserved exact code tokens and was idempotent.
   Documentation checks passed 21 worker-launcher tests, nine documentation
   tests and strict MkDocs.
6. The first broader model run executed **1167 cases**, with **25 failures and
   26 errors**, no skips; UI did not run. Combat fixtures assumed Dutch was the
   first fresh-game actor, and the rival journey assumed France first acted in
   round 1. Fixtures now advance to the intended actor; the unchanged rival route
   starts in round 0, lands in round 1 and founds in round 2. Combat outcomes,
   voyage positions, visibility, ownership, movement and colony assertions remain.
   TC34 follows the same corrected calendar and still checks Continue during
   travel. Initial statistics are counted before notifying the first actor so
   an asynchronously started AI cannot race ahead of that bookkeeping.
7. The revised broader Docker selection passed **1167 model cases** and **44 of
   46 UI/support cases**; two TC34 visibility assertions failed, with no errors
   or skips (`target/r52/publish-regression-fixed.log`). It selected `*Test`
   plus TC07/08/09/10/13/15/16/17/18/21/23/28/34/36/37. France's newly earlier
   first turn reveals (26,15) before Dutch control. The page verification now
   takes the expected shore visibility, and those two new-game scenarios expect
   it to be revealed. No visibility check was removed. This failed broader run
   is retained separately from the repair and full CI acceptance.
8. The repaired Docker run passed **17 model cases and 10 UI/support cases**,
   with zero failures/errors/skips: `FirstColonyJourneyTest,NationalTurnOrderTest,NationalTurnPreparationTest,TC_34_F26_rival_first_colony_test,PageVerificationContractTest`
   and `-Dsurefire.failIfNoSpecifiedTests=false`. All seven TC34 scenarios ran.
   Evidence is in `target/docker-ui/run-GpxHoM/`, log
   `target/r52/publish-rival-repair.log`. This also executes the final initial
   statistics ordering and the restored exact initial rival-position assertion.
   The full GitHub regression remains the publication gate.
9. For source commit `36d104eb45c2c8d100162198aad948570c3a6d8d`, downloaded
   [Linux native CI evidence](https://github.com/jajir/microcol/actions/runs/38758974439)
   passed **1166 game cases and 33 build/package checks**; separate launcher
   evidence passed **32 checks**, all without failures/errors/skips. The local
   explicit `*Test` selection above includes one extra case outside the normal
   CI game selection. All four Java 11/17 × Windows/Linux compatibility jobs
   passed in [run 38758974476](https://github.com/jajir/microcol/actions/runs/38758974476).
   [Pages run 38758974398](https://github.com/jajir/microcol/actions/runs/38758974398)
   deployed; the live evidence page contains this section and source links pinned
   to `36d104eb`. Native and launcher evidence is retained in
   `target/r52/ci-linux/` and `target/r52/ci-build-tools/`.
10. A separate Docker check of the remaining new-game entry paths passed **10
    UI/support cases** (TC24's six scenarios, TC12 and three page-contract cases),
    with zero failures/errors/skips. Selection:
    `TC_12_F11_start_free_game,TC_24_F15_free_play_setup_test,PageVerificationContractTest`
    with `-Dsurefire.failIfNoSpecifiedTests=false`; log
    `target/r52/publish-new-game-check.log`. No additional source change was needed.
11. The earlier [full CI run for 5beea7b5](https://github.com/jajir/microcol/actions/runs/38758246219)
    finished with **1166 passing game cases** and **153 UI-module cases, two
    failures**, no errors/skips. Downloaded XML confirms that its only failures
    were the two TC34 shore-visibility expectations corrected in `36d104eb`;
    no other gameplay failure appeared. Publication was skipped for that failed
    run. Evidence is retained under `target/r52/ci-first-functional/`.
12. The corrected [full CI run for 36d104eb](https://github.com/jajir/microcol/actions/runs/38758974439)
    passed **1166 game tests and 153 UI-module cases**, with zero failures,
    errors or skips. Downloaded XML is in `target/r52/ci-final-functional/`.
    All three native packages, launcher checks and publication passed. The
    [published build dev-84-36d104eb45c2](https://github.com/jajir/microcol/releases/tag/dev-84-36d104eb45c2)
    is a public prerelease containing all three platform archives and installers.
    The downloaded development-channel manifest identifies that exact build and
    source commit; local manifest evidence is in `target/r52/published/`.
    This completes acceptance and publication of this partial delivery, not the
    outstanding preparation-time save/action boundary in R-0052. No manual
    playtest is claimed.

## R-0052 preparation boundary — 2026-10-15

1. [R-0052](requirements/done/R-0052.md) doplňuje hranici existujících národních
   tahů: příprava blokuje hráčské příkazy a reentrantní snímky; souběžný
   snímek čeká. Selhání nepovolí uložení částečného stavu. Původní soubor
   zůstává použitelný přes menu a Pokračovat. Nové tržní/domorodé fáze
   nejsou součástí tohoto převzetí.
2. Izolovaný opravený životní cyklus prošel **49 modelovými případy**
   (`NationalTurnOrderTest`, `NationalTurnPreparationTest`,
   `TurnPreparationBoundaryTest`, `EuropeanRelationModelTest`, `CalendarTest`)
   a **3 případy PageVerificationContractTest**, bez chyb nebo přeskočení.
   Boundary test má 12 případů včetně skutečného DAO, nezměněných bajtů
   souboru, souběžného snímku, chyby při příjezdu/zrození/práci, setupu kola,
   rekurze, zrušení a konečného herního výsledku.
3. Integrovaný první běh měl **74 modelových případů, 1 selhání, 0 chyb a
   přeskočení**: pozitivní fixture pohybu neměla body. Caller nastavil běžný
   počet bodů a tentýž výběr prošel **74/74**. Nový
   `TurnPreparationActionTest` ověřuje 21 jinak platných příkazů a nezměněný
   stav při jejich zamítnutí během přípravy i po selhání. První příkaz
   obsahoval nesprávný název TC21; opravený výběr používá skutečnou třídu
   `TC_21_F15_free_play_screen_navigation_test`.
4. TC36 rozšiřují scénáře `TC_02_preparation_blocks_map_commands_and_duplicate_end_turn`
   a `TC_03_failed_preparation_keeps_recovery_save_and_menu_continue_works`.
   Zachovávají lineární kroky hráče a ověření v page objektech. Cílený běh
   zahrnuje také TC07/08/10/15/18/21/23 a zdrojový kontrakt. Tento běh měl
   **22 UI-modulových případů, 1 selhání, 0 chyb a přeskočení**. TC36/02
   použil helper očekávající aktualizaci status baru, přestože zkoušel
   zablokovanou mapu. Caller použil samostatný surový klik v page objektu;
   ověření neměnného stavu jednotky a vypnutého režimu pohybu zůstalo.
   První pokus o tento helper neprošel kompilací kvůli chybějícímu importu
   `TILE_CENTER`; po doplnění prošlo všech **6 případů TC36 a zdrojového
   kontraktu**, bez chyb nebo přeskočení. Ostatních 19 případů původního
   UI výběru již prošlo. Úplná regrese poběží v povinném Linux CI před
   zveřejněním hry; do jejího ověření požadavek zůstává `ready for development`.
5. Sdílený formatter kontroluje shodu tokenů a idempotenci všech změněných
   Java souborů. Logy jsou v `target/r52/boundary-*.log`; neúspěšné worker
   pokusy a caller opravy popisuje [provozní evidence](ai/local-qwen.md#turn-preparation-boundary-worker-2026-10-15).
   Prošlo také **21 testů worker nástroje**, **9 testů dokumentace** a přísné
   sestavení MkDocs; vykreslená sekce byla zkontrolována.
   UI běží v Docker/Xvfb/Openbox, bez ovládání hostitelské plochy. Manuální
   hraní nebylo provedeno. Publikace se ověřuje samostatně po pushi.

6. První publikovaný kandidát `aa6677f3` prošel Pages, ale
   [CI 38768159824](https://github.com/jajir/microcol/actions/runs/38768159824)
   zastavilo vydání: **1199 modelových případů, 2 selhání, 0 chyb/přeskočení**.
   Dva existující `CargoSlotTest` očekávaly odmítnutí rozdílné komodity dříve
   než přístup k vlastníkovi. Caller zachoval tuto neměnící validaci před
   novou přípravnou kontrolou; žádná mutace se před kontrolu nepřesunula
   a původní testy zůstaly beze změny.
7. Opravený místní Docker běh s `-Dtest=*Test` prošel **1200 případy herního
   modulu a 9 případy UI modulu**, bez chyb či přeskočení, exit 0. Obsahuje
   celý zvolený modelový výběr, 47 cargo testů a nové boundary/action testy;
   nenahrazuje úplnou sadu číslovaných UI scénářů. Log je
   `target/r52/boundary-all-model.log`. Následující CI ověřila plnou sadu
   před zveřejněním vývojové hry.

8. Oprava `d71e71b0136a1840b5231313be2c56bf266ef77e` prošla
   [celou vývojovou CI](https://github.com/jajir/microcol/actions/runs/38768750788).
   Stažené XML funkčního běhu potvrzují **1199 případů herního modulu a
   155 případů UI modulu**, bez selhání, chyb či přeskočení. TC36 má všechny
   tři scénáře; ověřena je také celá dosavadní sada číslovaných cest hráče.
9. Všechny tři nativní balíčky a publish job uspěly. Stažené reporty
   macOS obou architektur a Linuxu mají každý **1199 herních a 33 balíčkových kontrol**,
   bez chyb/přeskočení; launcher reporty mají **32/32**.
   [Kompatibilita](https://github.com/jajir/microcol/actions/runs/38768750650)
   uspěla ve všech čtyřech kombinacích Windows/Linux × Java 11/17.
   [Pages](https://github.com/jajir/microcol/actions/runs/38768750791)
   úspěšně sestavily a nasadily dokumentaci.
10. Vývojový manifest potvrzuje [dev-87-d71e71b0136a](https://github.com/jajir/microcol/releases/tag/dev-87-d71e71b0136a)
    ze stejného commitu a archivy všech tří platforem. Tím se uzavírá
    [R-0052](requirements/done/R-0052.md) v přijatém rozsahu existujících fází.
    XML a manifest jsou v `target/r52/boundary-ci-*` a
    `target/r52/boundary-published/`. Následná dokumentační revize nemění
    tuto publikovanou hru; její Pages a úmyslně přeskočené game jobs se
    ověřují samostatně.


## R-0053 unit turn orders — 2026-10-15

1. [R-0053](requirements/done/R-0053.md) doplňuje viditelné Čekat (Tab) a
   Vynechat tah (Mezerník). Model kontroluje aktuální registrované identity,
   připravený tah, umístění, pohyb a pokračující práci. Vynechání mění pouze
   dosavadní pohyb; save/Continue a obnovení tahu využívají existující stav.
2. Po opravě caller fixture prošlo **40 modelových případů**:
   `UnitTurnOrdersTest` **12**, `UnitStorageTest` **5**,
   `TurnPreparationActionTest` **21**, `NationalTurnPreparationTest` **2**,
   bez selhání, chyb či přeskočení. Předchozí běh měl jednu chybu testu,
   protože se pokoušel uložit ještě nezahájenou hru. Model ji správně odmítl;
   oprava kontroluje stav jednotky a hranici nezahájené hry přímo.
3. Kombinovaný Docker/Xvfb/Openbox běh zahrnul TC07/08/10/11/15/18/21/23/36
   a zdrojový kontrakt. Z **26 UI-modulových případů** mělo **25 úspěch**,
   jeden chybu a žádný nebyl přeskočen. Nový TC11/04 po správně dokončeném
   tahu hledal toolbar na stále otevřeném hlášení tahu. Caller doplnil
   skutečný krok hráče zavírající hlášení a opakuje TC11 se zdrojovým kontraktem;
   původní ověření zablokovaných vstupů a nezměněné jednotky zůstávají.
4. TC11/01 zachovává původní ID i cyklus. Nové scénáře
   `TC_02_wait_skip_save_resume_and_restore_movement`,
   `TC_03_cancel_preview_and_preserve_cycle_position` a
   `TC_04_preparation_rejects_wait_and_skip_input` pokrývají tlačítka, klávesy,
   prostřední jednotku v pořadí, jednu/žádnou jednotku, dvakrát Pokračovat,
   následující tah, zrušení náhledu a přípravnou hranici. Skutečný Space při
   zaměřeném tlačítku působí jednou; ruční výběr vyčerpané jednotky dovoluje
   prohlídku a odmítnuté vynechání zobrazí vysvětlení ve status baru.
5. Snímek `target/r53/unit-turn-orders.png` byl vizuálně zkontrolován:
   anglické popisky a zkratky se vejdou, toolbar i ukončení tahu jsou dostupné.
   Zachycený obsah má 900 × 750 px. Český text je dodán, samostatné manuální
   hraní ani česká vizuální kontrola nebyly provedeny. Sdílený formatter
   zachoval tokeny a idempotenci; prošlo **21 testů worker nástroje** a
   **9 testů dokumentace**. Logy a opravy jsou v `target/r53/` a
   [provozní evidenci](ai/local-qwen.md#unit-turn-orders-worker-2026-10-15).
   Úplná regrese, nativní distribuce a Pages se ověřují při publikaci;
   do jejich přijetí požadavek zůstává `ready for development`.

6. Opravený TC11 a `PageVerificationContractTest` prošly **7/7**, bez
   selhání, chyb či přeskočení, exit 0. Ostatních **22** případů původního
   kombinovaného UI výběru již prošlo; změněn byl pouze krok uzavírající hlášení
   v TC11/04. Model i GUI byly převzaty z kontrolovaného izolovaného checkoutu;
   hlavní checkout znovu používá sdílený formatter před commitem.

7. První plná [CI revize b842b112](https://github.com/jajir/microcol/actions/runs/38780304686)
   prošla **1211 herními případy**. Z **158 UI-modulových případů** měl jeden
   chybu, žádný selhání ani přeskočení. TC11 prošel celý. TC03 při pátém
   rychlém přechodu klikl na tlačítko odpojené od scény během otevření hlášení.
   `GamePage.nextTurn` čekal jen na aktivní toolbar, který se může aktivovat
   mezi pohyby královských jednotek před dokončením kola. Caller opravil
   sdílenou synchronizaci testu: čeká na nové dokončené lidské kolo a při
   existujících událostech na skutečně zobrazené hlášení. Nemění počet tahů,
   očekávané ozbrojené vojáky ani ostatní herní assertions. Jde o malou opravu
   callerem vlastněného ověření bez dalšího worker zadání.
8. Všechny tři nativní balíčky té revize prošly **1211 herními a 33 balíčkovými
   kontrolami**, launcher **32/32**, kompatibilita i Pages uspěly. Publikace
   hry byla kvůli UI chybě správně přeskočena; tyto dílčí úspěchy nejsou
   dokladem zveřejnění nové hry. Stažená evidence je v `target/r53/ci-*`.

9. Opravený helper prošel místním Docker během **20/20 UI-modulových případů**
   (TC03/04/10/11/36/37 a zdrojový kontrakt), bez selhání, chyb či přeskočení,
   exit 0. TC03 dokončí všech sedm kol a zachovává ověření nejméně tří
   ozbrojených královských vojáků; TC04 nadále kontroluje ztrátu kolonie a
   únik lodi. Log je `target/r53/turn-ui-recheck.log`. Prošel sdílený formatter
   a přísné MkDocs. Následující plná CI ověřuje všechny cesty před publikací.

10. Implementace [`79784bd7`](https://github.com/jajir/microcol/commit/79784bd7515778e22650c695412e2795dece5cf2)
    prošla [úplnou vývojovou CI](https://github.com/jajir/microcol/actions/runs/38783983178).
    Stažené XML potvrzují **1211 herních a 158 UI-modulových případů**, bez
    selhání, chyb nebo přeskočení; TC11 má všechny čtyři scénáře. Tím je
    přijato celé UC-014 v rozsahu [jednotkových příkazů](gap-analysis.md#unit-turn-orders).
11. Všechny tři nativní platformy mají každá **1211 herních a 33 balíčkových
    kontrol**, launcher má **32/32**, bez chyb či přeskočení. Všechny čtyři
    Windows/Linux × Java 11/17 [kombinace](https://github.com/jajir/microcol/actions/runs/38783983369)
    uspěly; [Pages](https://github.com/jajir/microcol/actions/runs/38783983476)
    úspěšně nasadily dokumentaci a živá stránka pravidel odkazuje na stejnou revizi.
12. Manifest vývojového kanálu potvrzuje [dev-90-79784bd75157](https://github.com/jajir/microcol/releases/tag/dev-90-79784bd75157)
    ze stejného commitu a archivy všech tří platforem. [R-0053](requirements/done/R-0053.md)
    je dokončeno. Stažené reporty a manifest jsou v `target/r53/ci-*` a
    `target/r53/published/`. Následný dokumentační commit uzavírá stav a odkazy;
    ověřují se jeho Pages, úmyslně přeskočené game jobs a nezměněný manifest.

## R-0054 coastal port movement — 2026-10-15

1. Selected [R-0054](requirements/done/R-0054.md) retains the existing shared coastal
   predicate and separate passenger landing. Source baseline is `77dd824a`.
   F30 deliberately places an inland colony beside an own coastal port with
   one isolated water square; ordinary founding spacing is unchanged. TC38
   covers loaded entry/cancellation/save/exit, inland rejection, empty-ship
   ordinary-land rejection and diagonal lake access. The cancellation defect
   below requires a small game-screen keyboard-dispatch fix.
2. The two local-worker model-test candidates failed compilation before any
   test ran; see [worker evidence](ai/local-qwen.md#coastal-movement-acceptance-worker-2026-10-15).
   They are not integrated into main. The caller ran the separate prepared UI
   work from main in Docker `run-JWBcUF`: **25 model checks passed**
   (`PassengerTransferTest` 12, `UnitColonistOnMapTest` 10,
   `BoundedAiMovementTest` 3); **16 UI-module checks ran with 3 failures,
   no errors/skips**. Existing TC01/13/16/29 and the three page-contract checks
   passed. TC38's diagonal journey passed; its other three cases failed.
3. Diagnosis: two whole-state comparisons differed only at `focusedField.y`
   (15 → 16), because F30 inherited the old focus after moving the ship. The
   third test released a canceled preview back onto the colony, opening its
   screen before the next map gesture. The caller aligns fixture focus with
   the ship and uses Escape before releasing the mouse. Whole-state, movement
   and cargo assertions remain intact. A targeted TC38/source-contract rerun
   narrowed the failures but did not establish acceptance: Docker `run-AMJLRB`
   ran seven UI-module checks with two failures, no errors/skips.
4. The focused diagnostic `run-ewrAEK` passed three source-contract checks and
   failed TC38/TC_01. It captured the Colonies `MenuButton` as the focused
   control while Escape left the preview active after Continue. This is a
   reproduced input defect, distinct from the earlier fixture/click issues.
   `ScreenGame` now routes Escape through a filter and the presenter consumes
   it only when canceling a preview. TC38 verifies cancellation before mouse
   release, deliberately retains menu focus, and also verifies ordinary menu
   Escape dismissal. TC38, TC21 navigation, TC11 unit orders and the source
   contract passed in Docker `run-Z5hHps`: **20 tests, zero failures/errors/skips**.
   The full regression result follows below.
5. Shared formatting passes token-preservation and idempotence. The nine
   documentation tests, strict MkDocs build and 21 worker-tooling checks pass.
   Full local Docker `run-qDtP0c` passed **1211 game tests and 162 UI-module
   checks**, with zero failures/errors/skips. This includes all four TC38 cases.
   The user requested publication of all current changes; the separate final
   model acceptance still keeps R-0054 open.
6. Published source `22bf30cbe160cb44f5784d11dfdf471050644d03` passed
   [development run 38804463986](https://github.com/jajir/microcol/actions/runs/38804463986):
   all three native packages, launcher checks, functional tests and publication.
   Downloaded functional reports confirm **1211 game tests and 162 UI-module
   checks**, with zero failures/errors/skips. The downloaded channel manifest
   identifies [dev-92-22bf30cbe160](https://github.com/jajir/microcol/releases/tag/dev-92-22bf30cbe160)
   from that exact commit. [Compatibility](https://github.com/jajir/microcol/actions/runs/38804463984)
   and [Pages](https://github.com/jajir/microcol/actions/runs/38804463969) also passed;
   the live documentation contains the new evidence and source links pinned to
   the published revision. Local logs, downloaded reports and manifest are in
   `target/r54/`. The following documentation-only evidence update retains the
   same game build; verify its Pages deployment and intentionally skipped game jobs.

7. On **2026-10-16**, the remaining model/AI acceptance passed in isolated
   Docker `run-Zqjuc6`: **39 model checks and 16 UI-module checks**, zero
   failures/errors/skips, exit 0. `CoastalPortMovementTest` adds 14 invocations:
   eight port directions with unchanged cargo/passenger identity, inland and
   ordinary-land rejection, paired map edges, all eight foot-movement directions
   with exhaustion, real file reload, actual AI port entry and a blocked AI
   route. TC01/13/16/29/38 and the source contract passed together.
8. The final worker candidate and caller corrections are separated in
   [worker evidence](ai/local-qwen.md#coastal-movement-acceptance-worker-2026-10-15).
   The caller corrected a package-private fixture API, a reversed assertion,
   an impossible two-point foot-colonist setup and the expected exhaustion
   exception. Gameplay code and all required acceptance boundaries are unchanged.
   The reviewed test file is integrated into main; full CI regression and
   publication of this final model-test addition remain pending.

9. Initial [development run 38817793009](https://github.com/jajir/microcol/actions/runs/38817793009)
   for `df25024d` failed its functional job before compiling or running any
   game or UI test: Maven could not reach Maven Central to resolve `maven-clean-plugin`.
   The downloaded artifact confirms a network-unreachable dependency lookup,
   not an assertion failure. Required functional acceptance and publication
   remain pending a rerun of the failed work.

10. Final acceptance on **2026-10-16**: [attempt 2 of the same run](https://github.com/jajir/microcol/actions/runs/38817793009/attempts/2)
    passed the full functional suite and published successfully. Downloaded
    reports contain **1225 game tests and 162 UI-module checks**, zero
    failures/errors/skips. All three native packages and launcher checks passed;
    downloaded Apple Silicon reports also confirm 1225 game tests and 33
    packaging checks. [Java compatibility](https://github.com/jajir/microcol/actions/runs/38817792981)
    and [Pages](https://github.com/jajir/microcol/actions/runs/38817792949) passed.
11. The downloaded channel manifest identifies
    [dev-94-df25024d4b3e](https://github.com/jajir/microcol/releases/tag/dev-94-df25024d4b3e)
    from exact source `df25024d4b3e12ca4584586a6da0d047d59c5087`. Evidence is
    retained under `target/r54/ci-model-final-retry`, `ci-model-native-arm` and
    `published-final`. R-0054 and UC-011 are accepted for their complete scoped
    variants; terrain costs and persistent orders remain separate requirements.
    The following documentation-only closure must deploy Pages, intentionally
    skip game jobs and preserve this game manifest.

## R-0055 shared fields — 2026-10-16

1. The user requested publication of the current checkpoint while [R-0055](requirements/done/R-0055.md) was in progress. Its status remains `ready for development`; COL-003 and UC-032 are only partially covered. The shared rules and affected active requirements record the agreed answers without keeping resolved questions open.
2. The checkpoint implements the live model assignment query/guard, global shared-square admission, equipment preflight and expected-worker crop changes. The colony screen uses the query for native drag admission and the existing localized status bar for rejection. Load and Continue switch screens only after the real loader succeeds; invalid data is explained before replacing the current game.
3. `SharedColonyFieldAssignmentTest` checks rejection snapshots and release/reassignment with last-resident preservation and stale crop identity. `GameLoadRejectionTest` uses the real loader with invalid duplicate colony identity, checks unchanged file bytes/no replacement, successful loading and propagation of unrelated failures. This does not test the pending saved-field validator. The first check failed compilation on an unavailable Mockito API; the next ran **64 model/controller cases, with four setup errors** from mocking final classes. Correcting the setup to real campaign/loader objects produced **64 passing cases**, zero failures/errors/skips.
4. `TC_39_F31_shared_colony_fields_test` uses `F31-shared-colony-fields.microcol`: neighboring colonies, known workers and stocks. The player path rejects the occupied shared square with visible feedback, moves the neighbor's last resident, then successfully assigns the freed square without stock changes. Two further cases exercise actual Load/Continue with existing invalid-identity validation, checking the message, unchanged menu/game reference and source bytes. Selected Docker UI results and screenshots are recorded below after execution.
5. Saved-field validation was not integrated: the final local-worker generation was deliberately stopped for this publication request, not accepted or timed out. See [worker evidence](ai/local-qwen.md#shared-field-persistence-worker-2026-10-16). Remaining R-0055 acceptance includes contradictory saved assignments, the full role/identity/owner/direction/turn/equipment matrix, drag cancellation, stale UI actions, crop production across turns and complete round-trip acceptance. No completion of that requirement or the broader UC-032 is claimed.
6. The shared Eclipse formatter passed token-preservation and idempotence checks. Local documentation validation passed **21 worker-launcher tests, nine documentation tests and strict MkDocs**. Final UI results and publication evidence are recorded below.

7. Docker `run-wzfQKV` passed **64 model/controller cases and 18 UI-module cases**, zero failures/errors/skips: the six selected model classes, TC_02 founding (1), TC_21 navigation (9), TC_38 coastal movement (4), the initial TC_39 shared-square scenario (1), and the page source contract (3). After adding the two invalid-load player cases, `run-9Hqsds` again passed all 64 model/controller checks but failed UI compilation because that module has no JUnit parameterized-test dependency. The caller changed the two cases to ordinary named `@Test` methods, preserving both paths and adding no dependency; final UI verification follows.

8. Final Docker `run-sEw8Id` passed **six UI-module cases**: all three TC_39 player paths plus the three page source-contract checks, with zero failures/errors/skips. Across the selected runs, the final checkpoint has 64 model/controller and 20 distinct UI-module checks covered. Exported screenshots show the shared-square status text and the Continue rejection message; the Load dialog snapshot was blank despite its passing rendered-label assertion, so it is not visual acceptance of that dialog. The colony screenshot also retains existing clipping in the colony layout; this checkpoint does not claim a layout redesign. Logs and screenshots are retained under `target/r55/` and `target/docker-ui/`.

9. Checkpoint source [`f13295d38bc2263d01a37e73dbadc4e9d6176b81`](https://github.com/jajir/microcol/commit/f13295d38bc2263d01a37e73dbadc4e9d6176b81) passed [development CI 38833711352](https://github.com/jajir/microcol/actions/runs/38833711352). Downloaded functional XML reports contain **1231 game tests and 165 UI-module cases**, zero failures/errors/skips. Each Linux, Apple Silicon and Intel Mac package passed **1231 game tests and 33 packaging checks**; the separate launcher evidence has **32 passing checks**. [Windows/Linux Java 11/17 compatibility](https://github.com/jajir/microcol/actions/runs/38833711307) and [Pages deployment](https://github.com/jajir/microcol/actions/runs/38833711305) also passed.
10. The release manifest downloaded on 2026-10-16 identifies [dev-96-f13295d38bc2](https://github.com/jajir/microcol/releases/tag/dev-96-f13295d38bc2) and that exact source commit, with all three platform assets. The live R-0055 page includes the checkpoint and unfinished acceptance statements. This verifies publication of the checkpoint, not completion of R-0055. The following documentation-only evidence update must deploy Pages, intentionally skip game jobs and preserve this game manifest. Downloaded reports and manifests are under `target/r55/published-*`.


## R-0055 completion work — 2026-10-16

1. Following the published checkpoint, the caller drafted read-only saved-field preflight in `Model.make`, before live colony/unit creation. `SharedColonyFieldPersistenceTest` covers valid exact-byte round-trip, same/different-owner shared squares, duplicate jobs/directions, missing/wrong-owner/wrong-role workers, conflicting/orphan places, crop/location validity, partial field records, empty colonies and restoration outside a live owner turn. These are authored checks, not executed results. A static reference scan found no conflicts in 40 readable source fixtures; the deliberately malformed JSON fixture was excluded.
2. The caller expanded TC_39 from three to eight player scenarios, retaining stable IDs: conflicting-field Load/Continue rejection, active-game preservation, native drag cancellation and stale drop rejection, crop cancellation/change/round-trip/one-round output, stale crop confirmation and equipment-return overflow. Existing controller invalid-identity coverage remains. `SharedColonyFieldAssignmentTest` additionally covers foreign occupation, replacement-worker crop identity, removed objects, invalid crop/local occupation, unregistered field identity and last-resident indoor/outdoor transitions. The separate local worker is authoring six live-boundary tests; all new formatting, compilation and Docker acceptance wait for model unload. No change in R-0055 readiness/completion is implied.

3. Caller Docker `run-2wd7Ep` passed **96 model/controller checks**: 27 persistence, seven live assignments, four load-controller checks and the retained identity/equipment/founding/turn guards. Its **12 UI-module cases had one error**: TC_39/06 timed out because `ContinentTool` omitted the seed of a one-square island, so the king’s unit could not find its continent and finish the turn. The other seven TC_39 scenarios, founding scenario and three page-contract cases passed; no cases were skipped. This is a real shared-map defect, not a missing crop rule or a worker-generated failure.
4. The caller added the starting square before continent expansion, with a regression covering a singleton island, a connected pair and unit-to-continent lookup. F31 remains unchanged: its king was already on valid grassland. The unchanged production/turn expectations and selected model/player regressions are being rerun after shared formatting. This small root-cause repair remains caller-owned; local-worker repair is not dispatched while Docker is running.

5. Final caller Docker `run-6rqPts` passed **108 model/controller checks and 12 UI-module cases**, zero failures/errors/skips. This includes all 27 saved-field cases, seven live-assignment cases, both continent test classes, free-play maps, retained boundary/controller checks, all eight TC_39 player scenarios, TC_02 founding and three page-contract checks. The shared formatter passed token-preservation and idempotence checks. The singleton-island repair restores the unchanged full-round production scenario.
6. The user requested publication of all current changes before the independent live-boundary repair was dispatched. This is a second verified checkpoint, not closure of R-0055 or UC-032. The six-test worker assignment remains pending in its existing job/worktree; no unreviewed worker source is included. Full CI and release publication are checked for the resulting commit.

7. Full CI for checkpoint `3e998eed` exposed **eight setup errors among 1264 game checks**, with no assertion failures/skips: seven coastal-movement invocations moved a colony onto another worked square, and one disembarkation case changed a colony owner without its residents. These dynamic fixtures had escaped the static source-fixture scan. The caller corrected their job/owner setup while preserving every movement/payload/rejection assertion and the saved-data validation. This bounded fixture repair stays caller-owned; no new worker job or budget reset is involved. Full regression and publication are rerun after correction.

8. The corrected fixtures passed all **1264 game tests locally**, zero failures/errors/skips, in the full Docker regression. The UI phase and cross-platform publication checks remain in progress at this correction commit. Strict MkDocs and shared formatting passed.

9. Corrected source [`07e927dffaafa5a3cfcee625d7c710d7fbd0ca34`](https://github.com/jajir/microcol/commit/07e927dffaafa5a3cfcee625d7c710d7fbd0ca34) passed [full development CI](https://github.com/jajir/microcol/actions/runs/38838785696): downloaded XML reports contain **1264 game tests and 170 UI-module checks**, zero failures/errors/skips. All three native packages passed; downloaded Apple Silicon reports confirm 1264 game and 33 packaging checks, and launcher evidence confirms 32 checks. [Windows/Linux Java 11/17 compatibility](https://github.com/jajir/microcol/actions/runs/38838785697) and [Pages deployment](https://github.com/jajir/microcol/actions/runs/38838785673) also passed.
10. The downloaded channel manifest identifies [dev-99-07e927dffaaf](https://github.com/jajir/microcol/releases/tag/dev-99-07e927dffaaf), that exact commit and all three platform archives. The live requirement page contains the updated checkpoint and unfinished boundary matrix. This verifies publication, not completion of R-0055. Reports and manifests are retained under `target/r55/second-ci-*` and `target/r55/second-published/`. The following documentation-only evidence commit must deploy Pages, intentionally skip game jobs and preserve this manifest.

11. The additional full local Docker run finished successfully after publication: **1264 game tests and 170 UI-module checks**, zero failures/errors/skips. Its log is `target/r55/second-full-regression.log`. Documentation-only commit `bfebf3cb` passed [Pages](https://github.com/jajir/microcol/actions/runs/38840634520); its [development](https://github.com/jajir/microcol/actions/runs/38840634516) and [compatibility](https://github.com/jajir/microcol/actions/runs/38840634555) classifiers passed with game jobs intentionally skipped. The before/after channel manifests are byte-identical and retain dev-99; the live evidence page contains the final CI totals.

12. Screenshot review of the full local run (`run-G5jsD1`) confirms the Load rejection message is readable. The Continue screenshot remains blank despite the passing explicit label/window assertions and successful preservation checks; it is not accepted as visual evidence of that dialog. No layout redesign is claimed. The pending live-boundary worker adds model coverage and does not resolve this capture limitation.

13. Cross-checking the published CI artifacts (`second-ci-functional/docker-ui/run-hAmoZH`) shows a readable **Continue** rejection dialog, while that run’s Load capture is blank. Together with the readable local Load capture, both player paths have readable dialog evidence in separate successful runs. The blank captures alternate between paths; their cause is not established, and no blank image is treated as visual acceptance. The passing player assertions and unchanged game/file checks remain the functional evidence.

14. Final boundary integration in Docker `run-gAixgo` passed **106 model/controller checks and 12 UI-module checks**, zero failures/errors/skips, confirmed from the exported XML. Selection includes all six `SharedColonyFieldBoundaryTest` cases, seven existing live-assignment cases, 27 saved-field cases, founding, equipment, turn-preparation, both continent classes and the load controller; UI selection includes all eight TC_39 paths, TC_02 and the three page source-contract checks. The new tests exercise all eight directions, independent wrong-turn/stopped states, stale unit identity, role/owner/source restrictions and exact equipment-capacity boundaries with unchanged-world snapshots. The worker's compiler and fixture failures and caller corrections remain in the [operating log](ai/local-qwen.md#shared-field-persistence-worker-2026-10-16). Shared formatting, 21 launcher tests, nine documentation tests and strict MkDocs passed. This is selected acceptance; full CI and final publication remain pending.


15. Final source [`3c521c5dcb9ac968481de92aec62bdf569779b65`](https://github.com/jajir/microcol/commit/3c521c5dcb9ac968481de92aec62bdf569779b65) passed [full development CI](https://github.com/jajir/microcol/actions/runs/38855265884). Downloaded functional XML confirms **1270 game tests and 170 UI-module checks**, zero failures/errors/skips, including six new boundary cases, seven shared-assignment cases, 27 saved-field cases and all eight TC_39 paths. Each of the three downloaded native reports confirms **1270 game and 33 packaging checks**; launcher reports confirm 32 passing checks. [Windows/Linux Java 11/17 compatibility](https://github.com/jajir/microcol/actions/runs/38855265910) and [Pages build/deployment](https://github.com/jajir/microcol/actions/runs/38855265747) also passed.
16. The published channel manifest identifies [dev-105-3c521c5dcb9a](https://github.com/jajir/microcol/releases/tag/dev-105-3c521c5dcb9a), that exact source commit and all three platform archives. Evidence is under `target/r55/final-ci-evidence/` and `target/r55/final-published/`. Together with the requirement-by-requirement acceptance audit and the earlier readable Load/Continue captures, this closes [R-0055](requirements/done/R-0055.md) and COL-003. UC-032 remains partial for its separate workshop-capacity scope. The following documentation-only closure must verify Pages deployment, intentional game-job skips and an unchanged game manifest.

## R-0056 fixture preparation — 2026-10-16

1. A temporary audit worktree at `3c521c5d` loaded the proposed F32 fixture through the real `Model.make`, checked exact save/load round-trip and unchanged snapshots after read-only queries. The current lumber field yields 1; free alternatives yield 2 and 3; the better shared field yields 6 but rejects assignment. The master blacksmith's existing production rule yields 6 tools for 3 ore, or 2 tools for the fixture's available 1 ore. This establishes test-data expectations for [R-0056](requirements/done/R-0056.md), not implementation of its picker or preview API.
2. Docker `run-oZMALr` passed **one fixture-preparation test and three page source-contract checks**, zero failures/errors/skips, confirmed from XML. Shared formatting passed token preservation/idempotence. Local probe source and artifacts remain under `target/r56/runtime-audit/`; the candidate scenario remains under `target/r56/` until formal integration. No gameplay code or production formula changed, and no new player journey is claimed as verified.


### R-0056 indoor assignment boundary — 2026-10-16

1. Shared colony admission now checks registered worker/owner/source identity,
   running owner turn and equipment-return capacity for indoor placement as well
   as outdoor placement. Restoration setters remain separate. Eight new
   `IndoorColonyJobAssignmentTest` cases cover read-only queries, last-resident
   transfers, stale/wrong-role/wrong-owner/remote workers, invalid targets and
   atomic gun/horse/tool returns at warehouse capacity.
2. Docker focused run `run-5jiemY` passed 186 model and 3 page-contract tests.
   Broader `-Dtest=*Test` run `run-Zy6A7s` passed 1279 game and 9 UI/control tests,
   with zero failures/errors/skips and exit 0 in both runs. Evidence is retained
   under the `2026-10-16-r56-indoor-boundaries` local-worker tree and main's
   `target/r56/indoor-{boundary-acceptance,full-model-regression}.log`.
3. These checks verify the shared admission foundation, including founding and
   persistence regressions. They do not run `TC_...` player scenarios or verify
   the pending job picker; [R-0056](requirements/done/R-0056.md) remains in progress.


### R-0056 job choice implementation — 2026-10-16

1. `ColonyJobChoiceTest` now verifies 19 real-model cases: read-only immutable
   offers, current-field crop preservation, explicit best relocation, center and
   indoor sources, stable ties, unavailable destinations, stale source/target/
   output/ownership, exact destinations, expert output, advisory input stock,
   current-job no-ops and atomic equipment rejection. The existing turn-preparation
   suite adds job confirmation to its guarded commands (22 cases).
2. The first focused run found five old `ColonyFieldTest` mocks without a worker
   after production began sharing the worker-aware preview helper. Supplying an
   ordinary worker and valid map preserved their original expected yields; a
   subsequent compile caught an ambiguous Mockito `any()` overload, corrected
   to `any(Location.class)`. The failed runs remain in `target/r56`; neither is
   counted as passing evidence.
3. Successful broader model/control regression recorded in
   `target/r56/choice-full-model-regression-retry.log` passed **1297 game and 9
   UI/control tests**, zero failures/errors/skips. It predates the added
   turn-preparation choice case and the final keyboard fixes, so final full
   acceptance remains required.
4. Initial TC40 player runs `run-MEx62g` and `run-qMjGMf` exposed ListView consuming
   Escape and Enter. A dialog-local key filter now closes on Escape and confirms
   the selected available action on Enter when the list has focus. Failures in
   subsequent game entry were downstream of the open modal; game-entry assertions
   were preserved. Prairie corn expectation was corrected to the existing yield 4.
5. Docker run `run-nhbBmh` passed **40 model tests and 10 UI tests** (seven
   `TC_40_F32_colony_job_choices_test` player scenarios plus three page-contract
   checks), zero failures/errors/skips, exit 0. It covers all three entry points,
   field comparisons, explicit movement, ordinary/expert input shortage labels,
   cancel/Escape/Enter, stale destination/source crop rejection and explicit-slot
   dragging. English/Czech screenshots in the run's `warehouse-qa` directory were
   inspected: names and numeric details fit and shortage feedback is visible.
6. The local scenario worker added TC40/08–11 (no available outdoor field,
   full indoor building, current-job no-op and building-body manual drop).
   Isolated Docker run `run-PuzAgH` passed 40 model and 14 UI checks, including
   all eleven player scenarios and three page-contract checks. The four cases
   were reviewed and integrated; complete regression/publication remain pending.

7. Subsequent source review found that resolving the indoor destination again at
   release could silently choose another slot after the hovered slot became busy.
   The handler now retains the slot admitted during hover and rechecks that exact
   slot on release. Caller-owned TC40/12–15 cover capacity changes and slot
   occupation during explicit-slot and building-body drops. The first main run passed all 49 selected model cases and the first eleven
   player cases but failed the four rejection-message assertions. Diagnostic
   run `run-1AaYr8` confirmed the native drag completion replaced the rejection
   with the building's hover name. An initial hover-message retention fix passed
   the stock cases but `run-QNyJTj` still failed TC40/14–15 because slot assignment
   repaints and replaces the building itself. The final approach reuses the job
   picker after native drag completion: it shows the localized rejection and
   refreshed choices, requires explicit confirmation, and supports cancellation.
   The temporary hover-state change was removed. All four scenarios retain their
   atomic world-state/equipment assertions and additionally cancel the refreshed
   picker and verify no mutation. Rerun evidence follows below.

8. Main Docker run `run-jdlQYs` passed **49 focused model checks** (19 job-choice,
   22 turn-preparation and eight indoor-boundary cases) plus **18 UI checks**
   (all 15 TC40 player scenarios and three page-contract cases), with zero
   failures/errors/skips and exit 0. This includes the direct center-to-field
   path, all four stale-drop variants, refreshed-picker cancellation and exact
   world-state preservation. Full regression and publication follow separately.

9. Final local Docker run `run-DQiA2M` completed with exit 0: **1298 game tests
   and 185 UI-module checks**, zero failures/errors/skips. Its XML includes all
   15 TC40 scenarios and all eight TC39 shared-field regressions. The English,
   Czech and expert preview captures were inspected, as was `job-stale-drop.png`:
   the rejection remains readable after repaint, with explicit Assign/Cancel.
   The shared Java formatter passed exact-token and idempotence checks for all
   18 changed Java files. The 21 launcher tests, nine documentation tests and
   strict MkDocs build also passed.
10. Source [`75341b36bcc2d1e760fe6b6c0e7405007fdb3c67`](https://github.com/jajir/microcol/commit/75341b36bcc2d1e760fe6b6c0e7405007fdb3c67) passed
    [full development CI](https://github.com/jajir/microcol/actions/runs/38890512391).
    Downloaded functional XML independently confirms **1298 game and 185 UI
    checks**, zero failures/errors/skips. Each of the three native reports has
    **1298 game and 33 packaging checks**; launcher evidence has **32 checks**,
    all passing. All four Windows/Linux × Java 11/17 jobs passed in
    [compatibility CI](https://github.com/jajir/microcol/actions/runs/38890512440).
    [Pages](https://github.com/jajir/microcol/actions/runs/38890512406) built and
    deployed successfully; the live rule page pins its code links to this source.
11. The downloaded channel manifest identifies
    [dev-107-75341b36bcc2](https://github.com/jajir/microcol/releases/tag/dev-107-75341b36bcc2), that exact
    source commit and all three platform archives. Their release-asset SHA-256
    digests match the manifest. Evidence is under `target/r56/ci-evidence/` and
    `target/r56/published/`. Acceptance covers each R-0056 behavior: all three
    entry points, eligible/tied/shared fields, crop preservation, exact moves,
    ordinary/expert potential versus actual production, advisory shortages,
    unavailable/full jobs, read-only cancellation, stale sources/destinations,
    equipment atomicity and preserved manual dragging. This closes R-0056,
    PROD-011 and UC-033. The documentation-only closure must separately verify
    Pages deployment, intentional game-job skips and an unchanged game manifest.


## R-0057–R-0061 cloud Sol batch — 2026-10-16

1. The user selected these five requirements and explicitly replaced the local worker with economical cloud Sol assignments for this batch. Main-agent reasoning is Astra high; one Sol low worker operates in an isolated tree, with targeted repairs and main-agent review. This is an experiment, not a measured subscription-saving claim.

2. R-0057 adds the 199/200/201 effective-food matrix to `NationalTurnPreparationTest`: three residents consume six food, two outside visitors survive, newborn identity/owner/outside placement and one report agree, and loading the completed boundary does not repeat accounting. TC08 reopens the colony without mutation and opens/cancels newborn 27's job chooser. No game code needed repair. Shared Java formatting preserved code tokens and was idempotent.

3. Docker `run-WMZvXr` passed 5 model checks, TC17 and 3 page-contract checks but failed TC08 because the worker compared a snapshot held by another page-object instance. Targeted repair fixed that error; `run-KPAz0p` retained those passes but exposed an unsuitable focus assertion. The second repair used the actual job-choice interaction; Docker `run-bScZIQ` passed TC08 (1 test, no failures/errors/skips). Main reviewed final source and parsed the XML. Logs remain under the isolated tree `~/Library/Application Support/Microcol/cloud-worker/worktrees/2026-10-16-r57/target/`. These focused results do not yet establish full batch regression or publication.

4. R-0058 removes the food-only 200 ceiling, retains checked representable amounts and nonfood limits, and creates one birth/report per complete 200 after consumption. Ship and wagon food unloading share an explicit amount/timing confirmation. A changed source requires a fresh offer; comparison uses that offer's snapshot, not the obsolete initial drag. Focused model XML records 131 passes (national preparation 8, warehouse 35, unload 59, wagon 8, goods 20, overflow 1); Docker TC05 `run-sAEeS1` passed 3 scenarios and TC41 `run-VZxx96` passed 2, all zero failures/errors/skips. The main agent reviewed changes and verified XML, then formatted all 15 changed/new Java files with token preservation and idempotence.

5. R-0058 test repairs: the initial F33 expectation incorrectly assumed another fixture's net food output. Explicit accounting establishes 16 produced and 16 consumed: 190 + 20 ship + 20 wagon − 200 birth = 30 retained. A subsequent explicit accounting assertion initially used 6 rather than 16 and failed in `run-DtVVp4`; the corrected assertion passes in `run-VZxx96`. TC41 also verifies cancellation, no immediate birth, stale cargo rejection without transfer, and refreshed acceptance of 19. These are fixture/test corrections, not weakened production expectations. Full integrated regression remains pending.

6. Main-agent R-0058 integration regression (`mvn -q -pl microcol-game -am test`) passed all **1308 model/controller tests**, zero failures/errors/skips, verified from 135 XML reports. The page-object source contract passed and strict MkDocs build succeeded. Logs are in `target/r57-r61/`; the full UI acceptance remains pending for the completed batch.

7. R-0059 commits survivor food after one random internal loss, keeps zero safe and clamps remaining shortage without another death. The immutable famine report stores victim identity/type and actual final stocks; destroyed colonies report no remaining stocks. UI uses localized names and a visible, accessible uncertainty warning. `ColonyFamineTest` passed eight checks: food and indoor/outdoor-input victims, exactly two cigars from two surviving tobacco, nonempty carried-passenger/visitor preservation, last-resident loss, zero and completed-boundary continuation. Main review strengthened originally non-exact output and potentially vacuous passenger assertions before acceptance. Docker `run-DKrKjr` passed TC09, TC10 and all three page-contract checks (5 total, zero failures/errors/skips). Main inspected final XML and source, and formatted 13 Java files with token preservation/idempotence. Full integrated regression remains pending.

8. R-0060 makes colony destruction idempotent, preserves outside troops, relocates owned docked ships to deterministic legal water without spending movement, and reports persisted ship/cargo losses when blocked. Removed selections and colony views refresh. Four destruction checks plus eight indoor-job and seven shared-field checks passed. They cover friendly sharing, enemy blockade, map-edge water, nonempty passengers/goods, absent queues, save/load and continuation. The first UI setup incorrectly assumed repeated famine must eliminate eight residents; surviving food workers can stabilize production, so the fixture now deterministically has one town-hall resident. Docker `run-ii2QHg` passed TC09 and the page contract but exposed an incorrect automatic-selection assumption in TC10; the actual Tab action fixes it. Final `run-oZwA8c` passed TC10 and the three page-contract checks, with no failures/errors/skips. Main verified XML and formatted 11 Java files with token preservation/idempotence. Repairing-ship state does not exist yet; R-0098 retains that dependency. Full integrated regression remains pending.

9. R-0061 uses a text-only cloud `gpt-6-sol`/low assignment instead of an interactive worker context. The first response correctly reported that the caller's append-only test anchor prevented updating contradictory old expert assertions. After that anchor was corrected, the next response requested missing warehouse/statistics and fixture context. Neither response changed code. With the supplied context, the third response produced all eight validated replacements. The caller applied them atomically and verified 38 focused tests (17 `ConstructionSlotTest`, 21 `ColonyJobChoiceTest`), zero failures/errors/skips. They cover ordinary/expert zero, partial and full input, incorrect goods types, two workers sharing stock, exact preview versus committed stock and saved completed-boundary continuation. Review then requested removal of duplicate potential-input arithmetic from the unavailable-slot menu path. Sol low supplied a static shared helper and preserved existing test callers; main applied and reviewed the repair, and formatted six Java files with exact token preservation and idempotence. The complete model regression passed 1325 tests; TC30 passed seven scenarios and TC40 passed fifteen. The complete integrated results follow below. Evidence is in `target/r57-r61/text-r61/`; these are cloud calls, with no local inference.

10. Main-agent complete Docker acceptance `run-59RlQi` ran `docker compose -f compose.ui-tests.yml run --build --rm ui-tests` with no test selector and no zero-test allowance. Exit code was 0. Independently parsed XML records **1325 game/model/controller checks in 137 reports and 187 UI checks in 44 reports**, zero failures/errors/skips. This includes TC08/17, TC05/41, TC09/10, TC30/40, the page contract and all prior player scenarios. Nine documentation checks and strict MkDocs also passed. The shared formatter was applied to every changed Java file before integration. All five requirements have local integrated acceptance; GitHub platform acceptance and publication remain pending at this checkpoint.

11. Code commit [0b0cb059](https://github.com/jajir/microcol/commit/0b0cb0593df4326754f1111f60ad63f1fde64f09) was pushed to `main`. [Development CI](https://github.com/jajir/microcol/actions/runs/38904142512) passed all three native packages; downloaded XML independently confirms **1325 game and 33 packaging checks per platform**, and **32 launcher/publication checks**, all zero failures/errors/skips. All four Windows/Linux × Java 11/17 jobs passed in [compatibility CI](https://github.com/jajir/microcol/actions/runs/38904142589). [Pages](https://github.com/jajir/microcol/actions/runs/38904142420) built and deployed successfully. Functional CI and channel promotion subsequently completed; final release evidence follows below. Native reports are retained under `target/r57-r61/ci-evidence/`.

12. Final downloaded functional CI XML independently confirms **1325 game and 187 UI checks**, zero failures/errors/skips. Publication succeeded and the downloaded development manifest identifies [dev-109-0b0cb0593df4](https://github.com/jajir/microcol/releases/tag/dev-109-0b0cb0593df4), source commit `0b0cb0593df4326754f1111f60ad63f1fde64f09`, and all three platform archives. GitHub release-asset SHA-256 digests match every manifest entry. Evidence is in `target/r57-r61/published/` and `target/r57-r61/ci-evidence/`. R-0057–R-0061 are complete and moved to `requirements/done/`; UC-034/035 are accepted, while UC-037/038 retain only the verified smith-house slice. No local model was invoked, and every worker remained Sol low under Astra high review. The documentation-only closure must separately verify Pages deployment, intended game-job skips and the unchanged game manifest.


## R-0062 automatic cross production — 2026-10-16

1. `Construction.getProduction()` now includes the declared unattended cross
   contribution once, shared by forecast and actual colony accounting. It retains
   worker output and existing bell behavior. Bundled buildArmy/thrive colonies
   missing any religious building now explicitly contain an unstaffed chapel;
   no user save is migrated or changed on opening. Tier increases remain R-0110.
2. Sol low supplied the model change and tests; caller review strengthened exact
   staffed/bell expectations, upgrade accounting and the national-round boundary.
   The caller added fixture and UI coverage and applied the shared formatter to
   six Java files, with code-token preservation and idempotence passing.
3. Local macOS model regression passed **1329 tests, zero failures/errors/skips**
   (`mvn -q -pl microcol-game -am test`, `target/r62/model-full.log`). The final
   review assertions passed a separate four-case `ColonyCrossProductionTest`
   rerun (`target/r62/model-review.log`). Earlier focused selection passed 111.
4. Docker/Xvfb/Openbox run `target/docker-ui/run-Ums7nw` passed **8 model and
   6 UI-module checks**, zero failures/errors/skips: `Construction_CHURCH_Test`,
   `ColonyCrossProductionTest`, `TC_02_F02_founding_colony_test`, both scenarios
   of `TC_07_F07_producing_gooods_test` and `PageVerificationContractTest`.
   TC02 verifies the new colony's visible +1; TC07 verifies visible unstaffed
   output, repeated opening, two colonies, Continue and exact stocks over two
   rounds. Existing corn/tobacco expectations remain unchanged.
5. Full UI regression and CI/publication are pending. These selected passes do
   not close all of UC-040, religious upgrades or national immigration.


## R-0064 difficulty profile — 2026-10-16

1. The selected difficulty is an immutable model value and required save field.
   Setup defaults new choices to Conquistador; all five names are localized in
   setup and the game panel. Start assembles the selected profile before replacing
   the game. Missing/unknown saved identities are rejected without migration.
   Forty-two tracked bundled/test scenarios explicitly declare Conquistador.
2. Sol low supplied the model/start path, production UI and profile tests. Caller
   review moved model reads outside a queued UI callback and added bundled
   default coverage. The caller supplied fixtures and TC24/page-object coverage.
   Shared formatting checked 14 changed Java files, code tokens and idempotence.
3. The local model regression passed **1332 tests**, zero failures/errors/skips
   (`target/r64/model-full.log`). The Docker model selection also passed all
   three `DifficultyProfileTest` cases (five-value disk round trip, invalid
   identity rejection and bundled defaults), plus two `FreePlayMapsTest` cases.
4. Initial Docker run `run-5ekXkv` failed UI compilation because the caller's
   new helper used unavailable `JsonParser.parseString` in the installed Gson.
   Replacing it with the supported instance parser fixed that compilation error.
   Retry `run-bRNwur` passed nine TC21 navigation scenarios, three page-contract
   checks and seven of eight TC24 scenarios, but TC24's five-level selection
   failed: the inherited Home/Down helper selected Governor when Conquistador
   was expected. The caller changed difficulty selection to click its named
   popup item; acceptance values remain unchanged. These failures were in
   caller-authored test code, not worker implementation.
5. Final Docker run `run-qMWN4a` passed **3 model and 12 UI-module checks**,
   zero failures/errors/skips: all nine TC24 scenarios and three page-contract
   checks, including every difficulty through Start/Continue, unchanged recovery
   files after missing/unknown profile rejection, and language-change identity.
   The Czech setup screenshot from `run-bRNwur` was inspected: the complete
   form, difficulty choice and Start/Back controls fit the test window. The
   final Czech game screenshot also shows the selected Governor label fully
   visible below the unit panel, above the gold/calendar bar.
   Full CI and publication remain pending; no complete R-0064 acceptance yet.


## R-0108 six-ship roster — 2026-10-16

1. Implementation uses the shared ship/cargo/voyage paths and existing transport/warship artwork. [R-0108](requirements/done/R-0108.md) owns the acceptance boundary; naval combat outcomes, the privateer diplomatic exception and complete royal-force lifecycle remain separate requirements.
2. Initial full model run executed 1332 tests, with one failure: the frigate visibility test still expected the old four-move radius. The existing visibility rule uses speed plus one; six moves cover 225 squares rather than 121. The exact expectation was updated without changing visibility code.
3. The worker's added seven-case roster suite initially had six passes and one failure: its terrain setup modified a returned map without applying `setTerrainType`. Targeted worker repair applied the terrain and used `Model.moveUnit`; the caller then reran all 1339 model tests successfully (zero failures, errors or skips). The full-hold player scenario now fills and verifies the new sixth galleon hold, preserving the original rejection assertions.
4. First selected Docker attempt failed during UI test compilation because the caller used JUnit parameter annotations without that module's parameter dependency. No UI tests executed. The caller reused ordinary scenario loops instead of introducing a dependency. A local compile then found an ambiguous TestFX lookup lambda; direct child-node selection fixed that helper. Local UI compilation passed; the Docker rerun is pending. New TC30 cases cover all six rendered carriers, visible hold counts, passenger boarding, Atlantic departure and Continue, plus new ship purchases and Czech offer text. Existing TC23, TC28, TC29 and TC30 journeys remain selected.
5. Full UI acceptance, CI and publication are pending. This is implementation evidence, not a completed requirement.


### R-0062 full CI follow-up — 2026-10-16

1. Run [38910438258](https://github.com/jajir/microcol/actions/runs/38910438258) failed full UI acceptance: 188 UI/support tests, five failures and one error, zero skips. All six were in TC40 colony-job choices: TC01/05/09/10/11 could not observe the status bar and TC14 failed during its stale explicit-slot drag. The artifact `tests-functional`, Docker run `run-82omTk`, was downloaded and inspected. Targeted R-0062 cases passed, but this complete acceptance failure blocks publication/closure.
2. A bounded Sol low diagnosis in an isolated `db946d43` worktree is underway. No retry or repair is claimed successful yet. The earlier targeted results remain valid within their stated scope.


### R-0108 selected UI follow-up — 2026-10-16

1. Docker `run-TJfnwQ` executed 23 UI/support tests: 21 passed and two new TC30 cases failed at the caller's hold-count observation. The dock intentionally retains six crate nodes and depicts unavailable holds as closed; counting nodes did not measure available capacity. `EuropePortScreen.verifyAvailableHoldCount` now checks each open/closed crate image against the expected capacity. Existing player assertions were retained. A targeted rerun of TC30 TC08/09 and the TC40 CI failure group is underway.
2. TC23's two Atlantic scenarios, TC28's two western-voyage scenarios, all six TC29 passenger scenarios, eight other TC30 cases and all three page-contract tests passed. The Czech purchase capture `ship-roster-czech.png` was visually inspected: all seven named offers, prices, insufficient-funds explanation and close control fit within the 378-by-649 dialog. New ship types reuse existing transport/warship artwork.
3. CI TC40 diagnosis found TC14's missing native `DRAG_DONE` before the five later status-bar failures. Synchronous repaint during its stale-slot mutation is a hypothesis, not a proven production cause; the previous full CI had passed the unchanged scenario. No speculative code change was applied. The selected rerun retains its stale-state and status-bar assertions.

4. The local reproduction `run-6Fmt4y` confirmed TC40's first native-drag timeout (TC15), followed by setup failures: 15 TC40 cases, 12 failures and two errors. The worker's targeted production repair queues `ColonyPanel`'s construction-move repaint with `Platform.runLater`, retaining the exact stale-drop tests. Its selected verification is pending.
5. The same run passed TC30 TC09 for all three new purchases. TC08 then exposed a second caller assumption: existing Atlantic travel begins at three turns, not two. The scenario now verifies three, advances one turn before saving, verifies two after Continue, and completes the remaining voyage. Docker `run-S9fkry` passed that scenario for all six types plus three page-contract tests (four tests, no failures/errors/skips).
6. R-0064 full CI [38911836474](https://github.com/jajir/microcol/actions/runs/38911836474) also failed only the same six TC40 cases: 191 UI/support tests, five failures and one error, zero skips. Publication remains blocked until the repair and complete acceptance pass.

7. Docker `run-uo1009` verified the deferred repaint repair: all 15 unchanged TC40 scenarios passed, including both stale slot/body drops; TC30/04's hire/equip/voyage/smithy/Continue path and three page-contract checks also passed (19 UI/support tests, zero failures/errors/skips). The attempted TC07 selector used a misspelled class name and selected no TC07 case; it is not counted as fresh evidence. Full CI will include the correctly named existing scenario.
8. Final local model evidence is 1339 passing cases; all selected ship scenarios passed across `run-TJfnwQ`, `run-6Fmt4y` and `run-S9fkry` after the documented caller corrections. Shared formatter checked 17 changed Java files with exact-token preservation and idempotence. Complete CI/publication is still required. The live development manifest inspected on 2026-10-16 still identifies `dev-109-0b0cb0593df4`, so no new game build is claimed.

<a id="r-0066-reference-expeditions-2026-10-16"></a>
## R-0066 reference expeditions — 2026-10-16

1. The new reference profile creates four colonial nations and four Crowns,
   peaceful pairs, legal distinct eastern starts and two equipped colonists per
   carrier. The model matrix covers all 20 nation/difficulty combinations,
   deterministic placement, source isolation, invalid maps and save/load.
   The visible profile currently uses Dutch; R-0063 owns nation selection.
2. Initial model checks exposed test setup mistakes: saving before starting the
   game, then expecting the human to take the first turn instead of England.
   Both were returned to Sol low for targeted test repairs; ordinary turn order
   was preserved. The second full run had 1341 tests with one assertion failure,
   no errors/skips. Final full retry passed **1341 tests**, zero failures/errors/skips.
3. Main-agent UI compilation initially used equipment methods on generic `Unit`;
   the helper now checks the known `UnitFreeColonist` type. Docker `run-mCzRV7`
   passed all 11 TC24 scenarios and three page-contract checks: **14 passed**,
   zero failures/errors/skips. This includes reference Start, Continue, canceled
   setup/save preservation and Czech labels. The command misspelled TC34's class,
   so that run provides no TC34 evidence. The correctly named Docker retry
   `run-lWIEIy` passed all seven TC34 scenarios and three page-contract checks:
   **10 passed**, zero failures/errors/skips.
4. Shared formatter checked all ten changed Java files, with token-preservation
   and idempotence passing. Nine documentation checks and strict MkDocs passed.
   The Czech setup screenshot was visually checked: the reference label and
   existing nation-preview notice fit, with Start/Back visible.
5. Full CI and publication remain pending. This is local implementation evidence,
   not completed acceptance.

<a id="r-0063-nation-selection-2026-10-16"></a>
## R-0063 actual nation selection — 2026-10-16

1. The reference setup passes the selected `EuropeanNation` through the normal
   application/game controllers into the fresh expedition factory. Authored
   scenarios retain their existing Dutch identity; the disabled selector and
   English/Czech note explain that restriction. The DAO rejects incompatible
   authored requests and null setup inputs before reading a scenario.
2. TC24 retains the original authored-world paths and extends reference
   Start/Continue to all four nations, independent player name, exact human
   equipment, ownership and saved unit identities. Map switching resets the
   fixed scenario to Dutch; canceling a nation draft preserves the saved game.
   The R-0066 model matrix remains the all-20-profile initial-state check.
3. The first model/Docker attempts failed to compile the new boundary test:
   `verifyNoInteractions` is unavailable in the repository's Mockito version.
   This worker test error was returned for targeted repair without changing
   dependencies. No tests from these failed attempts count as passed.
4. The retry exposed an unsupported mock of final `GameModelPoDao`; the worker
   replaced it with the real DAO and observable boundary assertions. Final full
   model verification passed **1342 tests**, zero failures/errors/skips. Docker
   `run-unKRlD` passed all 11 TC24 scenarios plus three page-contract checks:
   **14 passed**, zero failures/errors/skips; TC24/10 cycles all four nations.
   The selected UI check does not freeze AI ship positions after their turns;
   the human expedition is exact, all ownership remains checked, and the model
   matrix verifies every initial expedition before turns.
5. The shared formatter checked eight Java files with exact-token preservation
   and idempotence passing. Nine documentation tests and strict MkDocs passed.
   The Czech reference setup screenshot shows the real nation label/note and
   unobscured Start/Back controls. Full CI and publication remain pending.

### Full regression follow-up after R-0108 — 2026-10-16

1. CI [38915559041](https://github.com/jajir/microcol/actions/runs/38915559041)
   at `57d0db52` passed **1339 model tests**, all build checks and three native
   packages. The 194 UI/support tests had **8 failures**, no errors/skips;
   publication was skipped. The downloaded XML confirms failures only in TC22
   (five) and TC26 (three). All 15 TC40 cases passed, verifying the deferred
   native-drop repaint fix in the full suite.
2. TC22's purchase roster still expected only the original ship offers. The
   explicit expected list now includes Caravel, Merchantman and Privateer;
   the failed dialog assertion also left later TC22 setups without a visible
   status bar. TC26's original hidden frontier is inside the faster frigate's
   existing speed-derived sight radius. A bounded Sol low task adjusts the
   scenario geometry while retaining hidden/reveal/Continue/pixel assertions.
   Repairs require fresh targeted and full validation before acceptance.

3. Targeted Docker `run-ot9pGv` passed all five TC22 and three TC26 scenarios
   plus three page-contract checks: **11 passed**, zero failures/errors/skips.
   F18 moves the enemy/colony to x=33: distance eight from the initial frigate,
   seven after one eastward step, preserving the unchanged hidden/reveal/return/
   Continue assertions under its sight radius of seven. Shared formatter checked
   all three changed Java files, with token preservation and idempotence passing.
   Full CI and publication still require the corrected revision.

<a id="r-0067-load-recovery-2026-10-16"></a>
## R-0067 failed-load recovery — 2026-10-16

1. File loading rejects read/parse/required-shape/data failures before replacing
   the active model, reusing the existing localized dialog and false return.
   Null caller arguments and errors while starting an already constructed game
   remain outside the data-rejection catch. Empty decoded JSON roots are rejected
   explicitly. Existing authored resources remain current-schema data.
2. Sol low supplied the bounded implementation and recovery tests; main review
   added empty-root and nested-location cases. The first full model run had
   1347 tests with one fixture error: its chosen campaign had no on-map unit.
   The targeted repair now creates a malformed on-map placement explicitly.
   Main also corrected the test journey: exiting to menu stops the game, so
   active-state preservation must be checked before exit. The picker-cancel
   check is a separate menu action.
3. Full CI's functional-job budget is raised from 25 to 45 minutes. The previous
   complete UI run alone took 22 minutes 21 seconds; the new nation/recovery
   scenarios need headroom in addition to build/setup. No tests or assertions
   are removed and existing local command limits remain unchanged.
4. The first corrected full model run passed **1347 tests**, zero failures/errors/
   skips. Additional malformed-data probes then exposed two real gaps: a null
   ship cargo slot escaped as `NullPointerException`, and a truncated terrain
   row array loaded successfully by treating missing terrain as ocean. The
   focused 11-test run had one failure and one error. These are open defects
   until explicit cargo/grid validation and fresh checks pass.
5. Explicit cargo-slot validation and exact serialized terrain grid validation
   close both observed gaps. Null/short rows are also covered. The first full
   grid-validation run exposed 20 constructor errors from one legacy test fixture:
   the tenth row of `test-map-ocean-10x10.json` was mislabeled `row-0000`.
   Correcting its terrain/tree labels to `row-0010` preserves the same geometry.
   Final full model run passed **1351 tests**, zero failures/errors/skips.
   Thirteen CI-script tests, nine documentation tests and strict MkDocs passed.
6. Final Docker `run-12Niyj` passed **24 UI/support checks**: TC24 (11), TC39
   (8), TC42 (2) and page contract (3), zero failures/errors/skips. TC42 verifies
   malformed Continue without an active game, malformed Load while a game is
   active, preserved instance/world/source bytes, picker cancel and valid retry.
   The earlier 24-pass run preceded the corrected active-game assertion and is
   not used as evidence for that assertion. Shared formatter checked eight Java
   files with exact-token preservation and idempotence passing.
7. Full CI and publication remain pending.

### Native drag follow-up from the next full run — 2026-10-16

1. R-0066 CI [38917618848](https://github.com/jajir/microcol/actions/runs/38917618848)
   at `d407a393` passed 1341 model tests, but its 196 UI/support checks had
   21 failures and two errors, zero skips. Besides the already corrected old
   TC22/TC26 expectations, TC40 first failed TC12's native-drag completion guard,
   then subsequent scenarios cascaded. Downloaded XML/logs confirm this result.
2. The earlier full run's passing TC40 result therefore does not establish
   stable completion. A bounded Sol low follow-up investigates the remaining
   drag lifecycle with all original stale-state assertions retained. Publication
   and affected requirement acceptance remain open until a corrected full run.

3. TC12's warehouse-stock callback emits no repaint; the exact missing drag
   stage was not instrumented in the failed run. The page helper now uses the
   explicit native gesture already used by field-drag tests: press/move through
   hysteresis, verify drag detection, move/release and verify completion, with
   mouse release on setup failure. The same dropped-event mutation and outcome
   assertions remain. This is a test-gesture reliability repair, not evidence
   of another production repaint defect.
4. Docker `run-z0p0ke` passed all 15 TC40 scenarios plus three page-contract
   checks: **18 passed**, zero failures/errors/skips. Shared formatter checked
   the one Java file with exact tokens and idempotence passing. Complete CI on
   the repaired revision is still required to establish stable acceptance.

<a id="r-0068-recovery-checkpoints-2026-10-16"></a>
## R-0068 separate recovery checkpoints — 2026-10-16

1. The first bounded stage implements atomic sibling-temp replacement with no
   destructive fallback, typed Latest position/Latest turn/Previous decade
   paths and fresh-human-turn checkpoint policy. Focused execution passed
   **18 checks**: checkpoint policy (3), atomic persistence (2) and retained
   failed-load rejection (13), zero failures/errors/skips. The injected move
   failure preserves prior destination bytes and removes the temporary file.
2. Boundary coverage includes initial 1490, 1499→1500, later decades, seasonal
   1609→1610 and later seasons, nonfresh resume, nonhuman turns and autosave off.
   Main formatter checked six core Java files with token preservation and
   idempotence passing. Menu/settings/listener integration remains under work;
   this core result does not establish player-facing completion or publication.
3. Integrated menu/settings/listener implementation passed the full model suite:
   **1357 passed**, zero failures/errors/skips. The main review corrected the
   menu-exit failure path to retain the active game, reused the shared dialog
   controls, and required exact saved-state comparisons for both recovery slots.
   Docker `run-bYWf42` executed 34 UI/support checks: **33 passed, one failed**,
   zero errors/skips. All three new TC43 scenarios passed (both complete saved
   states, no rewrite on resume/cancel, autosave off, corrupt retry and same
   active game after failed menu save). TC24/39/42 and the source contract also
   passed. TC37/04 still expected the historical shared autosave destination;
   the fresh-turn contract now targets Latest turn. A targeted correction and
   rerun were performed as recorded below.
4. Docker `run-58IaxN` passed the corrected TC37 (7), TC43 including Czech
   controls (4), and page contract (3): **14 passed**, zero failures/errors/skips.
   Fresh turns write Latest turn; resumed/nonhuman notifications leave all
   three slots untouched. Both saved-state comparisons, failed-write retention
   and corrupt-load retry pass. Czech settings and recovery-chooser screenshots
   were inspected: labels and controls are visible without overlap or clipping.
   The main formatter checked 27 Java files with exact tokens and idempotence;
   nine documentation checks and strict MkDocs passed. Full CI/publication remain
   pending; whole UC-113 coverage is not claimed.

### R-0063 Czech note regression follow-up — 2026-10-16

1. Full run [38918709719](https://github.com/jajir/microcol/actions/runs/38918709719)
   on `695d767e` completed with 196 UI/support checks: nine failures, no errors
   or skips. Eight were the already corrected TC22/TC26 cases; TC34/02 still
   expected the former preview-only Czech note. The assertion now checks the
   intended fixed-scenario note, matching TC24 and the shipped translation.
   Gameplay assertions remain unchanged. Docker `run-C5pjVe` passed all seven
   TC34 scenarios and three page-contract checks: **10 passed**, zero
   failures/errors/skips. Shared formatter checked the changed Java file with
   exact-token preservation and idempotence. Full CI remains required.
2. The subsequent full run [38919368189](https://github.com/jajir/microcol/actions/runs/38919368189)
   on `241f3584` completed all 196 UI/support checks with only that same TC34
   note failure (one failure, zero errors/skips). TC22, TC26 and all TC40
   scenarios passed. The corrected note is included in `3b518aac`; its own full
   CI remains the publication gate.

<a id="r-0069-explicit-sight-2026-10-16"></a>
## R-0069 explicit unit sight — 2026-10-16

1. The bounded implementation replaces speed-derived exploration with clipped
   Chebyshev sight: current ships/scout roles two squares, ordinary land roles
   one. Equipment gains reveal immediately on-map; nested dragoon disarming
   must not expose a transient scout radius. Missing seasoned-scout/De Soto
   owners remain explicit integration dependencies in the requirement.
2. Initial full model execution ran **1382 tests: five failures, one error**,
   zero skips. Two old expedition assertions expected an enemy five/six squares
   away to be discovered; three AI journey assertions assumed the previous
   wider exploration route. A new transient-role fixture incorrectly returned
   equipment into a full warehouse and was correctly rejected. Repairs retain
   validation and real colony viability/conservation checks.
3. An observational probe using the existing real-turn test runner confirmed
   the revised rival journey: human boundaries 0/1 at ship (29,4)/(23,4),
   landing at (21,2) on boundary 2, colony there on boundary 3. This explains
   the changed route; it does not substitute for the required corrected tests.
4. Initial Docker UI execution stopped at test compilation: a changed fog
   diagnostic signature missed AttackPage/TC38 callers. The worker retained
   the shared no-argument empty-path check and made only the fog diagnostic
   target-specific. No UI test passed in that attempt. Acceptance is pending.

5. After targeted repairs the full model suite passed **1382 tests**, zero
   failures/errors/skips. Journey assertions now retain the original founder,
   legal coastal site, nonnegative food, unchanged human fleet, disk Continue
   and an occupied-site alternative using the actual narrower-sight route.
   Docker `run-T1yA85` passed TC25 (5), TC26 (3) and page contract (3):
   **11 passed**, zero failures/errors/skips. The discovered/continued fog
   screenshot was inspected; the hidden/revealed overlay checks remain intact.
6. Docker `run-Gs8OWx` executed TC34/TC38/page-contract: **14 checks, one
   failure**, zero errors/skips. TC38 and six TC34 scenarios passed. TC34/06
   tried to select the new northern colony outside the current viewport; its
   model founding/viability checks had already passed. The targeted repair
   pans the map before checking that this undiscovered colony stays hidden;
   no map knowledge or unit position may be changed to satisfy that assertion.
   Full acceptance and CI remain pending.
7. Docker `run-7rp2IC` ran TC34/page-contract: **10 checks, one failure**,
   zero errors/skips. Panning brought the tile into the viewport, but mouse
   movement left the status bar empty. Replacing the helper's stage-offset
   conversion with canvas `localToScreen` did not resolve it: focused Docker
   `run-X1dkND` again failed TC34/06 (**4 checks, one failure**, no errors/skips).
   The preselection screenshot was inspected; further input diagnostics are
   required. Neither attempted repair is recorded as a verified fix.

8. Focused diagnostic Docker `run-yLOju0` reproduced the failure (**4 checks,
   one failure**, no errors/skips): target (21,2) was at canvas (27,67), while
   the observed mouse tile remained (32,8). The shared test helper now uses
   actual toolbar bounds to select an uncovered point within the same tile.
   It retains real mouse input and all status assertions; temporary diagnostics
   were removed. Docker `run-wbwye3` passed TC34 (7), TC38 (4), TC26 (3), and
   page contract (3): **17 passed**, zero failures/errors/skips. The resulting
   screenshot was inspected: selected (21,2) remains Unknown with the hidden
   status, while the human fleet remains unchanged. The shared formatter
   checked 15 Java files with exact-token preservation and idempotence.
   Full CI and specialist/founder integration remain separate gates.

### Full-suite follow-up on R-0068 — 2026-10-16

1. Full run [38923947897](https://github.com/jajir/microcol/actions/runs/38923947897)
   on `3b518aac` ran **202 UI/support checks: seven failures, three errors**,
   zero skips. TC21/05 still read Latest position after a fresh-turn save,
   which now writes Latest turn. TC40/13 reached its stale-drop stock mutation
   but timed out waiting for native drag completion; subsequent TC40 failures
   encountered the wrong screen. Modal rejection timing is a diagnostic
   hypothesis, not a verified cause. Packaging, compatibility and Pages passed;
   game publication was skipped and remains unverified.
2. The building-drop rejection now follows the existing goods-drop pattern:
   wait for source `DRAG_DONE`, then queue the modal dialog. TC21 establishes
   Latest position through menu save/Continue before testing the subsequent
   changed save. Docker `run-zj1syb` passed TC21 (9), TC40 (15), and the page
   contract (3): **27 passed**, zero failures/errors/skips. Existing stale
   destination, equipment/stock and native-drag checks remain unchanged.
   The shared formatter checked both changed Java files with exact-token
   preservation and idempotence. A new full CI run remains required.

<a id="r-0071-voyage-duration-2026-10-16"></a>
## R-0071 sampled Atlantic duration — 2026-10-16

1. The selected implementation samples weather once after valid departure,
   calculates ordinary duration from the agreed direction/edge/base-speed
   factors, and persists weather, original duration and remaining turns.
   Loading validates those factors without rerolling. Royal carrier creation
   uses the same calculation and validates its arrival lane before creation.
2. Initial full model execution stopped at test compilation: a new diagnostic
   called nonexistent `UnitType.getName()`. No tests executed in that attempt.
   The same Sol low worker corrected the call, supplied independent explicit
   expected-duration tables, and added a deterministic partly completed voyage
   roundtrip plus royal no-lane rejection. Main review and execution continue;
   these changes are not yet acceptance evidence.
3. The first completed full model run executed **1386 tests: zero failures,
   one error**, no skips. A ship-roster fixture substituted a slow ship while
   retaining a fast ship's weather/duration combination. The targeted repair
   keeps valid voyage factors for both the full-hold and over-capacity rejection
   fixtures, so those tests still exercise cargo rules rather than failing
   prematurely on voyage validation. The full rerun passed **1386 tests**,
   zero failures/errors/skips, including all-weather explicit tables,
   partly completed voyage roundtrip and royal no-lane rejection. Main formatter
   checked 17 Java files with exact-token preservation and idempotence.
   Player-journey acceptance and full CI remain pending.
4. Docker `run-xTTfIF` passed TC23 (2), TC27 (9), TC28 (2), TC30 (10), and
   page contract (3): **26 passed**, zero failures/errors/skips. The journeys
   retain the sampled duration/weather through menu save/Continue, reach the
   destination after exactly the sampled number of owner updates, and retain
   cargo/equipment identities. The western blocked-arrival path waits at zero
   through Continue and retries successfully after its obstruction leaves.
   Nine documentation checks and strict MkDocs passed before final evidence
   recording; full CI and Magellan integration remain separate gates.

<a id="r-0072-voyage-reversal-2026-10-16"></a>
## R-0072 Atlantic course reversal — 2026-10-16

1. The model keeps original source/direction, sampled weather/duration and
   current route position/destination. Reversal offers bind live ownership,
   transit state and revision; an obsolete offer stays invalid after two
   reversals restore the same position and destination. Endpoint arrival is
   shared with ordinary turn advancement, including zero-distance return and
   blocked coast waiting. Missing route metadata and inconsistent derived
   remaining time are rejected when loading.
2. The initial full model suite passed **1391 tests**, zero failures/errors/
   skips. Main review then added explicit single-reversal disk roundtrips in
   both original directions, immediate return to both sources, and stale
   confirmation after real owner progress. All **7 VoyageReversalTest cases**
   passed in the focused rerun, including the strengthened comparison of serialized
   cargo, passenger identities and pioneer tools before/after disk reload and
   return to the source. Full CI for R-0072 remains pending.
3. Docker run `run-VHA3jL` ran 11 checks: ten passed and the Czech TC44 case
   failed because its navigation helper expected the English port title. The
   correction supplies the actual Czech title without changing gameplay checks;
   rerun `run-ctlkVN` passed that case and all three page-contract checks
   (**4 passed**, zero failures/errors/skips). The initial run also passed TC23,
   TC28 and the three English TC44 cases: cancellation, keyboard/Escape after a
   prior confirmation, reversal/Continue/source arrival, stale confirmation and
   blocked-source retry.
4. Main inspected the Czech confirmation and reversed-ship screenshots from
   those runs: destination/time and both buttons were readable, and the new
   action remained visible next to the remaining time. This is visual inspection,
   not a player study.
5. Docker regression run `run-is3XJV` passed **22 checks**: TC27 equipment
   (9), TC30 expert purchase (10), and page contract (3), zero failures/errors/
   skips. The shared formatter passed for all 18 changed Java files; the nine
   documentation tests and strict MkDocs build passed. Full CI and publication
   for this change remain pending.

### Integrated acceptance through R-0069 — 2026-10-16

1. Full run [38928383156](https://github.com/jajir/microcol/actions/runs/38928383156)
   on `ba651749` passed **1382 model and 202 UI/support tests**, zero failures,
   errors or skips. All three native packages and publication checks passed;
   compatibility run 38928383106 and Pages run 38928383189 also passed. The
   live R-0069 page was fetched and its current acceptance note verified.
2. Published immutable build
   [dev-120-ba6517498c2d](https://github.com/jajir/microcol/releases/tag/dev-120-ba6517498c2d)
   contains the expected platform archives/installers, launchers, manifest and
   checksums. Publication retained the development channel because main already
   contained the newer R-0071 revision. The fetched `dev` manifest still names
   `dev-109-0b0cb0593df4`; this is not evidence that the launcher has updated to
   build 120. The complete run verifies the integrated R-0062/63/64/66/67/68,
   current-type R-0069 and R-0108 changes; individual requirement closure still
   requires checking each requirement's remaining scope and dependencies.

## R-0073 sentry and automatic boarding — 2026-10-16

1. Preparation: the model assignment owns persistent sentry state, explicit
   activation, visible adjacent foreign-movement wake-up and departure boarding
   through existing passenger rules. Main owns map/pier controls, localized
   labels and the player scenarios in `TC_45_F36_sentry_boarding_test`.
2. Planned acceptance: stable-ID capacity selection on the European pier,
   cancellation and activation, Continue without boarding, adjacent pioneer
   boarding with its movement cost and tools preserved, and Czech controls.
   Existing TC29 passenger and TC27/30 equipment/expert paths remain regression
   gates. These are authored checks, not passing results; execution is pending.
3. Initial full model execution ran **1400 tests**, with two failures and no
   errors/skips. Both failures were new `SentryOrderTest` expectations that a
   map passenger had boarded; all other 1398 checks passed. The same worker is
   diagnosing fixture/capacity/eligibility and repairing the actual cause.
   Main review also requested explicit rejection of Europe departure by a
   sentry ship. These issues remain open until rerun evidence is recorded.

### R-0071 full publication acceptance — 2026-10-16

1. Full run [38930310720](https://github.com/jajir/microcol/actions/runs/38930310720)
   on `b1695549` passed **1386 model and 202 UI/support tests**, zero failures,
   errors or skips. All three native packages and publication gates passed;
   compatibility 38930310741 and Pages 38930310767 had already passed.
2. Verified release
   [dev-121-b1695549d14d](https://github.com/jajir/microcol/releases/tag/dev-121-b1695549d14d)
   contains all three platform archives/installers, launchers, manifest and
   checksums. The publication log explicitly left the automatic development
   channel unchanged because a newer main revision was present; this immutable
   build does not establish that the launcher channel advanced.

### R-0073 repair and model acceptance — 2026-10-16

1. The first two model failures came from passengers with zero remaining moves.
   After correcting the intended eligible fixture, seven of eight focused tests
   passed. The remaining capacity test still allowed both passengers aboard:
   the old five-slot scenario was expanded to the current six-slot Galleon.
   Its fixture now fills the sixth hold and asserts exactly one free hold
   before departure. The zero-move rejection case remains explicit.
2. Full model rerun passed **1401 tests**, zero failures/errors/skips, including
   the new disk roundtrip, tools conservation and departure rejection checks.
3. Main's initial UI test compilation failed because the project JUnit version
   lacks `assertInstanceOf`. The page verification now uses the existing
   `assertTrue(instanceof)` style with the same assertion meaning. Docker
   player execution is running; no UI pass is claimed yet.
4. First player execution ran six checks: five passed, while map boarding
   failed because main's new fixture put the ship on a grassland tile. Europe
   stable-ID/capacity/cancellation/Continue and Czech keyboard controls passed.
   Changing that tile to ocean initially left its old trees, so the following
   run could not load the scenario (three player failures; page contract passed).
   Direct `GameModelPoDao`/`Model.make` diagnosis reported trees on ocean at
   (25,15). Removing that tree made the same direct loader pass. The next
   Docker run uses this validated fixture; no acceptance is inferred from
   the fixture fix alone.
5. Main inspected the first run's Czech map and Europe screenshots. The map
   Activate control and S flag were readable. The pier marker needed explicit
   minimum size and contrasting background; its corrected rendering still
   requires fresh inspection. A map drag guard now directs a sentry user to
   Activate without silently clearing the order, with a player assertion.
6. Validated-fixture run `run-gb4quw` passed five of six checks. The remaining
   map test reached the correct sentry-drag rejection but its generic drag
   helper expected coordinate feedback instead of the intentional activation
   message. A separate explicit-feedback drag action preserves the old helper's
   assertions and verifies the actual expected message. The test still requires
   unchanged location/order/moves and subsequent legal boarding. Main inspected
   the updated pier screenshot: both S markers are visible on contrasting
   backgrounds. The combined TC45/29/27/30 and page-contract run is in progress.
7. Main review strengthened automatic-selection acceptance: the scenario now
   has exactly one eligible map unit, proves it is first before Sentry, proves
   no automatic candidate remains during Sentry and after disk restore, then
   proves Activate restores that same candidate. All **8 SentryOrderTest cases**
   passed after this test-only improvement. Production model code is unchanged
   from the passing 1401-test full run.
8. Combined Docker run `run-qnXGmJ` passed **31 checks**, zero failures/errors/
   skips: TC27 equipment (9), TC30 expert purchase (10), TC29 passenger transfer
   (6), TC45 sentry (3), and page contract (3). Main inspected the final Czech
   map screenshot: Activate and the S flag remain readable, and the corrected
   water fixture renders consistently. The pier marker was separately inspected
   after its contrast fix. Shared formatting passed for all 23 changed Java
   files; nine documentation tests and strict MkDocs passed before publication.
   Full CI/publication for R-0073 remain pending.

### R-0072 full publication acceptance — 2026-10-16

1. Full run [38932825583](https://github.com/jajir/microcol/actions/runs/38932825583)
   on `79248aa4` passed **1393 model and 206 UI/support tests**, zero failures,
   errors or skips. All three native package jobs and publication passed;
   compatibility 38932825522 and Pages 38932825663 also passed.
2. Verified release
   [dev-122-79248aa40c1d](https://github.com/jajir/microcol/releases/tag/dev-122-79248aa40c1d)
   contains all three platform archives/installers, launchers, manifest and
   checksums. Publication explicitly kept the automatic development channel
   unchanged because a newer main revision was present.

<a id="r-0074-specialist-roster-2026-10-16"></a>
## R-0074 European specialist roster — 2026-10-16

1. One Sol low worker implemented the bounded model roster, production,
   persistence and tests in an isolated worktree based on `62b419d7`. Main
   implemented localized names/images, the scrollable purchase list and TC30
   additions, reviewed shared callers and integrated the model stage.
   No local worker was used. No token-saving measurement is claimed.
2. First full model run executed **1464 tests, zero failures, five errors**.
   All errors came from the existing `ColonyFieldTest` map mock returning no
   terrain type. Farmer/fisherman production now requires that context.
   The same worker supplied real grassland in the fixture without removing
   assertions or weakening production validation. The full rerun passed **1464 tests**, zero failures/errors/skips. Main formatter checked all 20 changed Java files with exact-token preservation and idempotence.
3. TC30/11 and TC30/12 inspect all 19 entries, independent expected prices and
   English/Czech names, scroll visibility, excluded planters, keyboard purchase
   of the final entry and disk continuation. Existing TC30 cases remain.
   Docker `run-xivrXs` passed all **12 TC30 scenarios**, zero failures/errors/skips.
   The page verification source contract passed **three tests** separately.
   English and Czech screenshots were inspected: names/prices are readable,
   the last entry is visible after scrolling and the close button remains
   outside the scroll area. Nine documentation tests and strict MkDocs passed.
   This is roster/shared purchase-flow acceptance; new-specialty transport-to-job
   journeys and the remaining role effects still need final integration.
4. Full R-0074 remains incomplete: veteran rank/strength, Jesuit missionary
   outcomes, scout native/rumor effects and remaining improvement actions need
   their real integrations. Current scout radius and hardy plow timing do not
   substitute for those outcomes. Full CI/publication is also pending.


### R-0073 full CI finding — 2026-10-16

1. Run [38935767290](https://github.com/jajir/microcol/actions/runs/38935767290)
   completed its UI/support suite with **209 tests, one failure**, no errors or
   skips. TC22/01 expected the old three-entry pier equipment menu; the actual
   menu correctly added `Sentry`. This was outside the earlier selected suite.
   All three native packages, compatibility and Pages passed, but no complete
   game publication is claimed for this revision.
2. Main updated both primary/context-click expectations to the exact four-entry
   menu, preserving equipment and unchanged-unit checks. The final complete
   TC22 rerun passed five scenarios; see the baseline repair evidence below.

<a id="r-0075-persistent-recruitment-2026-10-16"></a>
## R-0075 persistent paid recruitment — 2026-10-16

1. A single Sol low model/test assignment in an isolated worktree based on
   `7717124e` implements persistent candidate tokens, weighted replacement,
   checked passage prices, guarded confirmation, explicit save schema and
   servant/criminal production. Main owns the Europe action/dialog, EN/CZ
   labels, fixture migration and TC46/F37. No token-saving percentage is measured.
2. First full model run executed **1473 tests, zero failures, 48 errors**.
   Older player-copy tests used plain Gson; the new candidate type needed its
   explicit field adapter. One display-name roundtrip fixture constructed a
   player without the newly required pool. The worker supplied the field
   adapter and an explicit pool/difficulty in that fixture, preserving validation.
   The full rerun passed **1473 tests**, zero failures/errors/skips.
3. Main requested additional identity-exhaustion, monetary/threshold-overflow
   and plain-Gson regression cases; the focused eight-case run passed.
   Review then found that the next paid-passage price could overflow even when
   the current price was valid, leaving an unloadable counter after charging.
   The worker added prospective-price preflight and a regression at p=107374172.
   The shared treasury check now compares gold with price directly, avoiding
   subtraction overflow for Integer.MIN_VALUE. A final complete rerun passed
   **1478 tests**, zero failures/errors/skips, including both new boundaries.
   Main formatter checked 35 Java files with exact-token preservation and
   idempotence.
   Docker `run-2QwwJl` passed TC46 (6), TC30 (12) and the page contract (3):
   **21 passed**, zero failures/errors/skips. EN/CZ recruitment screenshots were
   inspected: all three names/prices and confirmation controls are readable.
   After the arithmetic repairs, the next selection again passed all six TC46
   cases and the contract, but TC22 exposed two failures described below.
4. The current stage does not claim cross-earned arrivals (R-0076) or Brewster's
   real founder integration (R-0104). Saved progress already affects the quote;
   a reached earned threshold blocks paid offers until that subsystem settles
   it. Full CI, publication and final dependent acceptance remain open.


### TC22 baseline repair after R-0074/R-0075 — 2026-10-16

1. The follow-up selection executed **14 tests, two failures**, no errors or
   skips. TC22/02 still expected only the pre-R-0074 purchase list. Its assertion
   left the modal offer open; the following TC22/01 did not log a fixture load
   and instead logged a 10000→5000 gold change in the previous game before
   failing to find the game status bar. This evidence identifies a cascade
   from the unclosed failed offer, not a reason to weaken gameplay assertions.
2. Main updated the complete expected roster to all six purchasable ordinary/
   ship entries plus 19 specialists, keeping the exact-list comparison and
   cancellation/treasury checks. Both Sentry menu expectations remain explicit.
   The complete TC22 plus source-contract rerun passed **eight tests**, zero
   failures/errors/skips. No test was skipped, no retry was added and no
   assertion was removed. Nine documentation tests and strict MkDocs passed;
   complete CI/publication remain separate gates.

### R-0076 national cross immigration — 2026-10-16

1. `RecruitmentPool` consumes increasing thresholds and carries surplus;
   `Player` creates the selected saved candidate on the pier before colony
   production. Each colony adds its actual new crosses once to the national
   pool. Existing local warehouse accounting is preserved. The fixed-turn gift
   is removed; human and computer colonial players share the rule, subject to
   Europe access. Existing persistence fields suffice.
2. The first complete model run executed **1485 tests, one failure**, no errors
   or skips: `NationalTurnPreparationTest` still expected the old turn-11 gift.
   The same Sol low worker seeded the actual threshold in that test, preserving
   arrival/birth/movement/Continue assertions, and added identity, next-target
   overflow and computer/closed-Europe boundaries. The final complete model
   run passed **1488 tests**, zero failures/errors/skips.
3. The first Docker attempt stopped at test compilation: main used
   `assertInstanceOf`, unavailable in this project's JUnit version. Main changed
   it to the existing `assertTrue(... instanceof ...)` style without weakening
   the place assertion. Docker `run-47Uj09` then passed **11 tests**: TC47 (2),
   TC46 (6) and `PageVerificationContractTest` (3), zero failures/errors/skips.
   TC47 runs real colony production to 100 crosses, checks the pending passage
   explanation, saves/Continues before arrival, advances the national turn,
   verifies the notification, chosen candidate type and one pier unit, and
   saves/Continues again without duplication. Czech progress and English
   pending screenshots were inspected: text and controls remain readable.
4. Shared formatter checked **13 Java files**, exact-token preservation and
   idempotence passed. `mvn -q -pl microcol-game -am -DskipTests package`
   passed, including Javadoc; this packaging command does not repeat tests.
   Documentation checks and strict MkDocs are run before commit. Complete CI,
   platform compatibility and publication remain separate pending gates.
   Actual Brewster selection still requires R-0104; no placeholder founder flag
   was introduced. [R-0076](requirements/done/R-0076.md) retains acceptance status.

### R-0074/R-0075 CI follow-up — 2026-10-16

1. R-0074 development run `38937895895` failed with **211 UI/support tests,
   two failures**, no errors/skips: TC22's obsolete seven-entry purchase roster
   and the following modal-state cascade. Both were repaired in `11c53f15`;
   the local full TC22 rerun is recorded above. This failed run did not publish
   a game build.
2. R-0075 compatibility run `38940319283` failed Javadoc on Java 11/17 and
   Linux/Windows: `EuropeButtonsPanel.updateLanguage` inherited an invalid
   `@return` tag although it returns void. Main corrected that comment in the
   R-0076 change; local packaging including Javadoc passed. R-0075 Pages run
   `38940319281` succeeded. Full development/publication and the new platform
   matrix still require verification; Pages success does not establish game
   publication.

### R-0077 repeated ship purchase prices — 2026-10-16

1. `Player` owns the checked per-type quote and persisted purchase counts.
   The dialog passes the displayed price; stale confirmation rejects before
   payment, count changes or unit creation. Future-price and identity limits
   are validated before a successful purchase. Existing specialist and paid
   recruitment prices remain independent. Artillery requires its real R-0095
   type and remains an explicit acceptance dependency.
2. Complete model suite: **1495 passed**, zero failures/errors/skips. The new
   seven model tests cover successive purchases, independent types/players,
   fixed specialists, quote-only reads, stale and insufficient funds, disk
   Continue, malformed counters, current/next price overflow and exhausted
   unit identities. The main-owned `TC_48_F39_ship_purchase_prices_test`
   covers cancel, duplicate confirmation, two purchases, unaffordable third,
   Continue, stale quote and an expense after the offer opens.
3. Docker `run-u37N7u` passed **24 tests**, zero failures/errors/skips:
   TC48 (3), TC30 (12), TC46 (6) and the page contract (3). The player paths
   verified prices 1000→1100→1200, exact treasury changes, independent types,
   two real ships and persisted history, and unchanged state after rejected
   stale/insufficient confirmation. Formatter checked nine Java files with
   exact-token preservation and idempotence; packaging including Javadoc
   passed. Nine documentation tests and strict MkDocs passed. Complete CI
   and publication remain separate gates.

### R-0075 complete CI outcome — 2026-10-16

1. Run [38940319431](https://github.com/jajir/microcol/actions/runs/38940319431)
   at `11c53f15` completed: **1478 model and 217 UI/support tests passed**, zero
   failures/errors/skips. This includes all three TC45 Sentry scenarios and
   the repaired TC22 roster assertions, so the earlier UI regression is resolved.
2. All three native package jobs failed solely on the invalid Javadoc `@return`
   in `EuropeButtonsPanel.updateLanguage`; publication was skipped. The repair
   is in `2a5dbac6`. Its run `38941685067` has already passed all three native
   packages, while its complete functional job remains in progress. Do not
   infer a new downloadable game build from these intermediate results.

### R-0078 horse breeding — 2026-10-16

1. `Colony` freezes the shared breeding calculation before mutation, applies
   it once after resident food and colonist births, and before construction
   completion. Preview includes actual horse output and all growth/breeding
   food use; raw production remains separate for execution and famine. Existing
   saves already hold all required goods and building state.
2. Main review caught a transposed expected horse/food value for 201 effective
   food; the same worker corrected it before execution. First complete run:
   **1522 tests, one failure**, no errors/skips. The final-resident famine test
   accidentally retained a food-producing worker, so its own negative-food
   precondition failed. The worker repaired that fixture using the existing
   nonfood-worker pattern, preserving famine/destruction assertions. The full
   rerun passed **1522 tests**, zero failures/errors/skips, including all 27
   breeding cases and the existing food/turn-preparation checks.
3. First Docker attempt stopped during test compilation because main's setup
   called package-private `ColonyWarehouse.setGoods`. Main changed setup to
   the existing public add/remove operations; no production API was expanded.
   The repaired Docker run `run-lBqiU0` passed **17 tests**, zero failures,
   errors or skips: TC49 (3), TC41 food import (2), TC32 construction (7),
   TC09/10 famine (1 each) and page contract (3). The new journey builds real
   Stables, verifies two horses in that update and four in the following one,
   checks the visible horse preview, saved stocks, capacity-one/full limits
   and a birth followed by exactly one horse from 201 effective food.
4. Seven Java files passed shared formatting, exact-token preservation and
   idempotence. Packaging including Javadoc passed; nine documentation tests
   and strict MkDocs are checked after final evidence updates. Full CI and
   publication remain pending. Normal final-stock forecasts include births
   and breeding; famine keeps its pre-loss deficit warning and produces no
   horses, preserving the existing survivor recalculation contract.

### R-0079 industrial production — 2026-10-16

1. Astra main assigned the production model and rule tests to the existing
   Sol low worker in the isolated R79 worktree. Main reviewed the shared
   arithmetic, integrated it and authored F41/TC50 plus its page object.
   No local model or duplicate implementation was used; token savings were
   not measured.
2. The first full model run had 1543 cases with five errors: Mockito slots
   defaulted to occupied without production budgets, and a modified hill
   retained a plowed-field marker. The worker repaired those test fixtures.
   A second run exposed one remaining invalid empty field with production;
   the worker cleared both parts of the vacated assignment. The final full
   run passed **1543 tests**, zero failures/errors/skips. The 20-tier table
   covers ordinary/expert input budgets and scarce/zero input, with separate
   aggregate rounding and same-turn source-chain checks. Production code
   needed no repair in these retries.
3. Main formatted eight Java files using the shared profile with exact-token
   preservation and idempotence checks. Packaging including Javadoc passed.
   The player results follow below; no local model pass alone establishes
   complete CI or publication.

4. Initial Docker run `run-KNrb2t` exposed a real missing image alias for
   Arsenal: all three TC50 cases failed when opening the colony. The existing
   specialist (12), construction (7) and page-contract (3) cases passed.
   Main registered the three military production tiers using existing
   metalworking artwork and documented that visual limitation. The assertions
   and player paths remain intact; TC50 is rerun after the fix.

5. The alias repair run `run-SxmD7M` passed the factory and ironworks/arsenal
   journeys plus the page contract. The lumber-mill test alone failed because
   main expected excess project hammers to remain in stock, whereas the
   existing queue moves all available hammers into the project. Main changed
   the fixture to 49 initial hammers plus three produced, exactly the 52 cost,
   and explicitly checks three versus six hammer production and the subsequent
   six stored hammers. This keeps the upgrade check independent of overflow
   policy. The captured colony image confirms visible military building and
   worker panels, musket output and warehouse forecasts; the reused industrial
   artwork remains a documented visual limitation.

6. Final Docker run `run-FY5oFR` passed **six tests**: all three TC50 journeys
   and all three page-contract checks, zero failures/errors/skips. Together
   with the unchanged 12 specialist and seven construction cases from the
   earlier run, the selected player acceptance passed. Final packaging,
   formatter (eight Java files), nine documentation tests and strict MkDocs
   passed. Full CI and R79 publication remain separate pending gates.


### R-0076 complete CI and Sentry closure — 2026-10-16

1. [Run 38941685067](https://github.com/jajir/microcol/actions/runs/38941685067)
   passed at `2a5dbac6bb124d369cc79a8ae6b27f9f58ab644a`: **1488 model and
   219 UI/support tests**, zero failures/errors/skips, all three native
   packages and publication. Compatibility and Pages passed separately.
2. [Release dev-126-2a5dbac6bb12](https://github.com/jajir/microcol/releases/tag/dev-126-2a5dbac6bb12)
   contains Linux/macOS archives, installers, launchers, manifest and checksums.
   The publisher explicitly retained the current automatic development channel;
   this immutable release does not prove that channel advanced.
3. This verifies the repaired TC22 roster/Sentry expectations and native Javadoc
   packaging together. R-0073 is now done. R-0074/0075/0076 retain their explicit
   remaining gameplay dependencies; passing CI does not remove those scopes.

### R-0077 complete CI — 2026-10-16

1. [Run 38942912137](https://github.com/jajir/microcol/actions/runs/38942912137)
   passed at `d9e9615de799fd0438d3a86e2e1a5ff5eb930f05`: **1495 model and
   222 UI/support tests**, zero failures/errors/skips. Native packages,
   compatibility and Pages passed.
2. [Release dev-127-d9e9615de799](https://github.com/jajir/microcol/releases/tag/dev-127-d9e9615de799)
   has all platform archives/installers, launchers, manifest and checksums.
   The publisher explicitly left the automatic development channel unchanged.
   R-0077 still awaits its explicitly linked artillery integration in R-0095.

### R-0080 coastal facilities — 2026-10-16

1. Astra main assigned the bounded model/test scope to the existing Sol low
   worker in the isolated R80 worktree. Main owns the requirement, localized
   field/project feedback, F42/TC51 and actual verification. No local model
   or duplicate implementation was used; cloud-token savings were not measured.
2. Initial full model run had 1548 cases, three failures and eight errors.
   Existing terrain-editing and specialist fixtures placed workers on water
   without a dock; the old colony mock similarly assumed fishing everywhere.
   The worker added explicit completed docks to preserve those tests' original
   assertions, restored the existing missing-predecessor queue reason, and
   strengthened new rejection checks to compare the complete saved state.
   The final full model run passed **1550 tests**, zero failures/errors/skips.
   Tests include equipped-worker rejection, all three facility tiers, enclosed
   water, shared-square exclusion, inland projects, completion and saved work.
3. Main added a dock to the existing F04/F06/F07/F08/F13 colonies that already
   contain fishermen. This is current-schema fixture maintenance, not a runtime
   save migration. Player scenarios, packaging and complete CI/publication
   remain pending and are recorded below once observed.
4. Additional model acceptance queues a valid coastal ship and dock, reloads
   each after the adjacent water becomes land, and verifies the full saved
   state remains unchanged when completion is refused. The full 1550-case run
   includes this check.
5. Initial Docker run `run-ckCkdK` passed TC51 (2), TC40 (15), TC32 (7) and
   the page contract (3). TC39 had one failed food assertion: its old expected
   98/102 food omitted the two food consumed by R-0078 horse breeding in each
   colony. The log confirms both horse stocks changed 50→52. Main corrected
   expected food to 96/100 and added explicit horse-stock assertions; crop,
   worker and turn assertions are preserved. The affected scenarios and the
   five dock-maintained fixtures are rerun below.
6. During Docker run `run-Wt0uh3` (25 cases), main briefly inserted the new 52-horse
   assertion into TC39/01 before its first turn as well as TC39/06. The source
   was corrected, but that run had already compiled the intermediate version:
   TC39/01 correctly reported 50 rather than 52 horses. TC39/06 passed its
   repaired full food/horse/crop balance. A fresh TC39 run is required; the
   incorrect extra assertion was not an existing gameplay expectation. TC04 (1),
   TC06 (2), TC07 (2), TC08 (1), TC17 (1), TC32 (7) and the page contract (3)
   all passed in this run.

7. Fresh Docker run `run-0Ivuau` passed **11 tests**: all eight TC39 cases
   and all three page-contract checks, zero failures/errors/skips. Across the
   recorded runs, every selected new and regression player scenario passed.
   The shared formatter checked 16 changed Java files with exact-token
   preservation and idempotence. Packaging including Javadoc, nine documentation
   tests and strict MkDocs passed. Full CI/publication remain pending.


### R-0078 complete CI regression — 2026-10-16

1. [Run 38944235418](https://github.com/jajir/microcol/actions/runs/38944235418)
   at `1ce545c1` failed its full UI gate: **225 UI/support cases, one failure,
   zero errors/skips**. TC39/06 expected 98 food but received 96, omitting
   the two food consumed by the newly implemented horse breeding. Publication
   was blocked. Compatibility and Pages passed separately.
2. The same failure was reproduced during R-0080's targeted regression.
   Its repair preserves crop and field assertions, updates both colonies'
   expected food, and checks 52 horses explicitly. Local TC39 rerun passed all eight cases in `run-0Ivuau`; subsequent full CI
   remains the acceptance gate. No failed build is a published result.


### R-0081 printing production — 2026-10-16

1. Scope: BUILD-013 / UC-047 printing only. A bounded Sol low worker
   implemented model calculations/tests in an isolated worktree; main owns
   the job-picker text, F43/TC52 player paths, review and actual verification.
   No local model or duplicate implementation was used.
2. Initial full model run: **1567 tests, one failure, zero errors/skips**.
   All new printing cases passed; the older cross-production test still
   expected zero bells in its unattended second town hall. The accepted
   economy rule requires one. This expectation was returned to the same
   worker for targeted repair; cross-production checks must be preserved.
3. Repaired full model rerun passed **1567 tests**, zero failures/errors/skips.
   The shared formatter checked eight Java files with exact-token preservation
   and idempotence; packaging including Javadoc passed.
4. Initial Docker `run-wyfqeK`: the unattended TC52 scenario passed. The
   staffed scenario stopped at main's incorrect English goods-name expectation
   (“Bells” instead of the existing “Bell of liberty”). Main corrected only
   that expectation; the full staffed journey still requires a fresh rerun.
5. Initial Docker regression completed: all 15 TC40 job cases, seven TC32
   construction cases and three page-contract checks passed. Across the run
   there were 27 cases, one failure described above, zero errors/skips.
   A fresh TC52/page-contract run checks the corrected expectation.
6. Fresh Docker `run-oW0RxV` passed **five tests**: both TC52 player
   scenarios and three page-contract checks, zero failures/errors/skips.
   Both real upgrades, unchanged workers, odd-total rounding, unattended
   output and Continue passed. Main inspected the captured town-hall output
   showing +14 with the two original statesmen. All selected player paths
   have now passed; no failed run is counted as acceptance.
7. Nine documentation tests and strict MkDocs passed. Full CI and publication
   remain pending; the requirement stays ready until those gates complete.


8. Full CI on `d8bff42a1962` passed **1567 model and 232 UI/support tests**, zero failures/errors/skips; native packaging, compatibility and Pages succeeded. Release [dev-131-d8bff42a1962](https://github.com/jajir/microcol/releases/tag/dev-131-d8bff42a1962) has all ten expected assets. The publisher explicitly kept the automatic channel unchanged. R-0081 is done.

### R-0082 fortified population — 2026-10-16

1. Scope: BUILD-015 / UC-047 voluntary departures and forced famine.
   A bounded Sol low worker implemented model guards/tests in an isolated
   worktree; main owns UI feedback, F44/TC53, review and actual checks.
   No local model or duplicate model implementation was used.
2. Initial full model suite passed **1573 tests**, zero failures/errors/skips.
   The final expanded suite passed **1575 tests**, zero failures/errors/skips,
   including departure at four residents for all three tiers and forced
   capture/reload of a two-resident fortified colony.
3. Initial Docker attempt stopped before test execution: main's new page
   helper passed an untyped lookup to overloaded `dropTo`. Explicitly selecting
   `Node.class` repairs the compile error without changing gameplay. The full
   selected Docker run was repeated.
4. Docker `run-ngvE1j`: 22 cases, zero assertion failures, two errors in
   TC53 because `ImageLoaderBuilding` had no STOCKADE image mapping; opening
   the actual colony threw before either scenario could proceed. TC40 (15),
   TC01 (1), TC09 (1) and the page contract (3) passed. Main registered all
   three defensive tiers with existing art, documented the visual reuse and
   reruns both new player scenarios. No gameplay assertion was weakened.
5. Docker `run-T1WQ71`: TC53 famine/Continue and page-contract checks passed.
   TC53 departure passed rejection, feedback, internal reassignment and the
   fourth resident leaving, but main then dragged the passenger into F44's
   occupied cargo slot 0 (one tool). The existing boarding rule correctly
   rejected it. Main changed the destination to empty slot 1, preserving all
   assertions and cargo rules; both scenarios are rerun.
6. Docker `run-5vnQgP` passed both TC53 scenarios and three page-contract
   checks. Main's screenshot review then found the fort panel overlapping
   the tobacco workshop: no `id_fort` position existed in CSS. Main assigned
   an unused middle-row position in both CSS and SCSS and added a visible
   non-overlap assertion against neighboring panels.
7. Final Docker `run-Xdymlw` passed **five tests**: both TC53 scenarios and
   three page-contract checks, zero failures/errors/skips. Main inspected the
   fresh screenshot: the fort occupies its own space and neighbors remain
   accessible. Across the recorded runs every selected new/regression player
   path passed. Nine changed Java files passed shared formatting with token
   preservation/idempotence; packaging/Javadoc, nine documentation tests and
   strict MkDocs passed. Full CI/publication remain pending.

### R-0079 full CI regression — 2026-10-16

1. [Run 38946246170](https://github.com/jajir/microcol/actions/runs/38946246170)
   completed with **228 UI/support cases, one failure, zero errors/skips**.
   TC39/06 retained the old 98-food expectation instead of 96 after horse
   breeding. This is the same failure recorded for R-0078; the repair was
   already committed in `165e83a8` and passed the local TC39 rerun.
2. Native packages, compatibility and Pages passed; game publication was
   blocked. The subsequent R-0080 full CI gate remains independently pending.


### R-0078–R-0080 complete acceptance — 2026-10-16

1. [Full run 38948579264](https://github.com/jajir/microcol/actions/runs/38948579264)
   on `165e83a8` passed **1550 model tests and 230 UI/support tests**, zero
   failures/errors/skips. This includes repaired TC39 food expectations and
   the horse-breeding, industrial and coastal-facility player journeys.
2. All three native packages, compatibility and Pages passed. Release
   [dev-130-165e83a83a79](https://github.com/jajir/microcol/releases/tag/dev-130-165e83a83a79)
   was verified with Linux and both macOS archives/installers, launchers,
   manifest and checksums. The publisher explicitly kept the automatic
   development channel unchanged. R-0078, R-0079 and R-0080 are now done.


9. Full R-0082 CI on `3ac0bc75` failed with **234 UI/support cases, one error**, zero assertion failures/skips: TC53/01 attempted `#chooseWork` after the rejected native drag before the popup was found. Main separated context-click and popup lookup with an explicit bounded wait; TC53, TC40 and TC52 are selected for a fresh regression. R-0082 publication remains blocked until fresh acceptance.

10. Fresh Docker `run-uJVC0Q` passed both TC53 cases, all 15 TC40 cases and both TC52 cases within a 36-case run, zero failures/errors/skips. The shared popup wait preserves all departure/reassignment assertions. Full CI of the repair is still required; the earlier failed publication is not counted as success.

### R-0109 shared hammers — 2026-10-16

1. Selected before R-0083 because rush purchase requires the accepted shared
   hammer balance. A bounded Sol low worker changed model/serialization/tests;
   main updated the construction picker, TC32, F45/TC54 and actual verification.
   No local model or duplicate implementation was used.
2. Full model suite passed **1576 tests**, zero failures/errors/skips.
   Per-entry progress assertions were replaced with shared warehouse-balance
   checks, preserving queue identity/order and completion boundary assertions.
   Current-schema entries no longer contain personal hammer progress; bundled
   static fixtures contained no such fields. No migration was added.
3. Initial Docker `run-L6ynrt` passed TC52 (2), TC50 (3), TC51 (2),
   TC49 (3) and the page contract (3). TC54 passed queue edits, saved
   continuation and shared balances through wagon completion, then main's
   helper incorrectly counted wagons via `getUnitsOutSideColony`, a list
   intentionally excluding cargo carriers. Main corrected the helper to count
   units at the actual colony location; assertions and gameplay are unchanged.
   All seven TC32 cases also passed. The initial run totaled 21 cases, one
   failed helper assertion, zero errors/skips. The fresh TC54 rerun remains
   pending. Screenshot inspection confirmed the pool and per-project costs;
   main shortened the shared-pool label to avoid ellipsis in the narrow panel.
4. Fresh Docker `run-AFXN3N` passed all **four tests**: TC54 and three
   page-contract checks, zero failures/errors/skips. The wagon count repair
   is verified and the entire shared-pool/Continue journey passed. A final
   run verifies the shortened visible label.
5. Final Docker `run-r7ZZ6W` passed all **four tests** with the final label.
   Main inspected the screenshot: the shared 131-hammer balance and each
   project's costs are fully readable. All selected player scenarios passed
   across the recorded runs; the 15 changed Java files passed exact-token
   preservation and formatter idempotence.
6. Packaging/Javadoc, shared formatting and nine documentation tests plus
   strict MkDocs passed. Full CI/publication remain separate gates.


### R-0083 rush completion — 2026-10-16

1. Scope: BUILD-004 / UC-044. A bounded Sol low worker owns model quote,
   transaction and boundary tests in an isolated worktree. Main owns the
   confirmation dialog, F46/TC55, review and actual verification. No local
   model or duplicate model implementation was used.
2. Review requested an explicit turn-preparation guard at confirmation and
   a real preparation-callback regression. The quote already hides unavailable
   games through `isGameRunning`; confirmation now also uses the shared guard.
3. All **1586 model tests passed**, zero failures/errors/skips. The shared
   formatter checked eight Java files with exact-token preservation and
   idempotence; packaging including Javadoc, nine documentation tests and
   strict MkDocs passed.
4. Docker `run-uYYmGH` passed **13 tests**, zero failures/errors/skips:
   TC55 (two rush journeys), TC32 (seven construction regressions), TC54
   (shared-pool journey) and three page-contract checks. Actual taxed price
   76, cancellation, insufficient gold, changed-stock rejection, one completion
   and Continue all passed. Main inspected the captured quote: full material,
   total and remaining-treasury text and both buttons are readable.
5. Full CI and publication remain pending; R-0083 stays ready until those
   gates finish. No failed or skipped check is counted as acceptance.

### R-0104 Congress recruitment — 2026-10-16

1. Scope: GOV-007 / UC-079 acquisition and FATHER-003 / UC-088 Jones's
   exactly-once frigate. A bounded Sol low worker owns model state, commands,
   production integration and model tests in an isolated worktree. Main owns
   the Congress dialog, current-schema fixtures, F47/TC56, review and actual
   verification. No local model or duplicate model implementation is used.
2. Initial model run: **1602 tests, one failure and three errors**, zero skips.
   The new round-trip assertion compared unit-list ordering although loading
   groups ships before other units; the full state must instead be compared by
   stable unit ID. Two new fixtures assumed a nonexistent England player or
   removed required HIGH_SEA voyage origins. An existing Mockito test constructed
   a player inside unfinished stubbing, now exposed by the constructor's
   difficulty read. These four issues were sent to the same worker for targeted
   repair; no failing run is counted as acceptance.
3. Targeted repairs preserve every saved field while normalizing only the unit
   list by stable ID; fixture ownership and the isolated voyage origin are now
   valid, and Mockito setup is completed before constructing the foreign player.
   The full rerun passed **1602 tests**, zero failures/errors/skips. Shared
   formatting checked 32 Java files with exact-token preservation and idempotence.
4. Main's first documentation check rejected the UC evidence-cell format; the
   required implementation/verification prefix was restored before rerunning.
   Player acceptance and final delivery remain pending.
5. Docker `run-uJVC0Q` passed **36 tests**, zero failures/errors/skips:
   TC56 (two Congress journeys), TC53 (two fortified-population cases), TC40
   (15 job cases), TC52 (two printing cases), TC30 (12 Europe purchase/transport
   cases) and three page-contract checks. Jones arrived through ordinary
   production, created one real frigate without payment, and retained membership
   and the ship through Continue; local bells were not consumed. Cancel and
   stale confirmation preserved the saved world. Main inspected both captured
   Congress screens: all five candidates, progress, members and reward are readable.
6. Packaging including Javadoc, nine documentation tests and strict MkDocs
   passed. Full CI and publication remain pending; R-0104 stays ready until
   delivery gates finish. This does not certify the other 24 founder effects.

### R-0084 founder construction — 2026-10-16

1. Scope: BUILD-007/014 and FATHER-011/014; UC-042/045/048/096/099.
   The existing Sol low worker owns founder metadata, shared queue validation
   and boundary tests in an isolated worktree. Main owns translated reasons,
   confirmation snapshots, F48/TC57, review and actual verification.
   No local model or duplicate implementation is used.
2. Review requested real ownership-transfer coverage in addition to the
   worker's saved-membership scenario, plus stale rush rejection, unrelated
   founders and preservation of the other prerequisites. The same worker
   supplies these focused additions. The full model suite passed **1613 tests**,
   zero failures/errors/skips, including capture/recapture and preserved buildings.
3. Initial Docker run `run-4NEZkM` rejected the new F48 fixture before any
   player scenario: 200 tools exceeded its 100-unit warehouse capacity
   (food also exceeded capacity). Main corrected both initial stocks to 100
   and verified direct loading through the model; no production code changed.
   The failed TC57 cases are rerun separately; initial failure is not acceptance.
4. In the first Docker run, TC32 (seven), TC50 (three), TC56 (two) and
   the three page-contract checks passed; all three TC57 cases failed at load.
   Shared formatting checked eight Java files with exact-token preservation
   and idempotence. Packaging including Javadoc passed.
5. Corrected Docker `run-OZLW9Q` passed **six tests**, zero failures/errors/skips:
   all three TC57 journeys and the three page-contract checks. Smith recruitment
   unlocks all six otherwise eligible factories; Stuyvesant unlocks only the
   custom house. Actual normal completion and Continue passed for ironworks
   and custom house; the retained founder-less head preserves tools/work and
   cannot be rushed. Together with the prior passing regressions, all 18
   distinct selected tests passed. Main inspected the captured construction
   dialog: founder name, costs, stock and disabled confirmation are readable.
6. Nine documentation tests and strict MkDocs passed. Full CI and publication
   remain separate gates; R-0084 stays ready while they are pending.

### R-0085 custom-house export — 2026-10-16

1. Scope: BUILD-014 / TRADE-009, UC-048/081. The existing bounded Sol low
   worker owns model export selection, staged production, export accounting,
   saved events and boundary tests. Main owns UI, current-schema JSON fixtures,
   F49/TC58, review and actual checks. No local model or duplicate implementation.
2. Initial build failed because the worker used EnumSet with GoodsType, which
   is a class rather than an enum. The worker repaired it to HashSet.
   The next full run had **1620 tests, three failures**, zero errors/skips:
   new corn/tobacco tests assumed productive workers not present in the chosen
   fixture. The same worker receives the failures for targeted setup repair.
   The actual France-to-Dutch capture/treasury test passed in that run.
3. Review keeps R-0102's boycott lifecycle as an explicit unfinished dependency;
   ordinary and revolution exports alone do not complete R-0085. UI and final
   model verification are running; publication is not claimed.
4. Older R-0083 CI `38954893144` failed with **237 UI/support tests, two errors**:
   the known TC53 job-popup issue (repaired in R-0104) and TC10 JavaFX
   PulseReceiver NullPointerException during animation. The latter is included
   in current targeted regression; this observation does not establish a root
   cause or claim it is fixed. Log: `target/r85/r83-ci-failed.log`.
5. The worker repaired the fixture setup to use productive lumber work and
   controlled food surplus, and asserted explicit 7-price/10%-tax net income
   6 (3 during revolution). Review also removed a spurious treasury write
   when no goods are sold. The final full model suite passed **1620 tests**,
   zero failures/errors/skips. Eighteen Java files passed shared formatting
   with exact-token preservation and idempotence; packaging/Javadoc passed.
6. Docker `run-UmGBkj` passed **16 tests**, zero failures/errors/skips: TC58
   (four custom-house journeys), TC57 (three founder gates), TC50 (three
   production cases), TC54, TC10, TC14 and three page-contract checks.
   TC10's earlier animation error did not reproduce; this does not prove its
   underlying cause fixed. TC14 is colony-spacing coverage, not warehouse
   unloading; main corrected the regression selection to TC19 for the final run.
   Main inspected the export dialog: all 16 commodities, reserve rule and
   confirm/cancel controls are readable.
7. The first Docker snapshot preceded the no-sale treasury-write removal;
   final-source TC58/TC19/page-contract verification is running separately.
   Nine documentation tests and strict MkDocs passed before this final
   evidence update. Boycott and full publication acceptance remain open.
8. Final-source Docker `run-csqhIb` passed **25 tests**, zero failures/errors/skips:
   TC19 (18 warehouse transfers), TC58 (four export journeys) and three
   page-contract checks. Across both runs, all **34 distinct selected tests**
   passed. Exported fresh production was 103 tobacco → 231 gold after 25% tax,
   plus 30 ore → 90; unselected silver remained. Revolution delivered 115+45.
   Continue retained balances and choices without repeating the sale.
9. Main retained the trivial final persisted-event validation: reject
   non-tradable goods in a saved export report, with a focused malformed-event
   assertion. Its focused rerun passed all **seven model tests**, zero failures/errors/skips,
   separately from the earlier full suite. This does not change the valid player journey.

### R-0087 peace negotiation — 2026-10-16

1. Scope: UC-069 / DIP-003/004. The existing Sol low worker supplies the model,
   optional per-pair pending-offer persistence and boundary tests. Main owns
   the existing report UI, F50/TC59 journeys, review and actual verification.
   No local model or parallel duplicate implementation is used.
2. Offers bind the actor, rival, visitor, price, armed land strengths, both
   treasuries and Franklin membership. Explicit acceptance validates the live
   contact and unchanged terms before one transfer and symmetric peace.
   Cancel leaves war; postponement and Continue retain the pending proposal.
3. First full model run: **1628 tests, one failure**, zero errors/skips.
   The worker expected 200 gold in an insufficient-funds fixture containing
   existing French armed strength 1.5; the correct price was 300. After that
   correction, the same test exposed a second fixture mistake: setting guns
   to true did not change an already armed unit. These failed runs are not
   acceptance; the same worker handles targeted correction.
4. Previous R-0104 CI `38957291472` failed with **239 UI/support tests, one
   failure and one error**: TC53's indoor work popup still timed out after
   the bounded-wait change, and TC11's button-width assertion failed. The
   earlier local TC53 pass did not establish a durable fix. Both are included
   in the current regression selection and remain publication blockers.
5. Corrected final model run passed **1628 tests**, zero failures/errors/skips.
   The stale-strength setup now changes the unarmed Dutch visitor rather than
   rearming the already armed French unit; all unchanged-world assertions remain.
   Packaging including Javadoc and nine documentation tests plus strict MkDocs
   passed before the later toolbar repair.
6. Docker `run-puTaEk` ran **20 UI/support tests: one failure**, zero errors/skips.
   All four new TC59 journeys, seven TC33 regressions, two TC53 cases and the
   three page-contract checks passed; TC11 reproduced its label-fit failure.
   Main inspected the peace-offer screenshot: rival, price, treasury, contact
   and all three actions are readable. Final-source keyboard acceptance/cancel
   verification is running separately.
7. Follow-up repair replaces the shared toolbar's HBox with FlowPane: controls
   wrap at their preferred widths while retaining the right-side reservation.
   No labels or assertions are shortened. The protected-drag test also waits
   for its source's actual `DRAG_DONE` before another mouse gesture; the previous
   FX-queue wait alone did not guarantee native drag completion. TC53 passed
   locally before this change, so its CI race remains a hypothesis pending
   verification. Final targeted and full UI acceptance remain open.
8. The added hidden-contact case initially failed because `Model.make` correctly
   restores current unit sight (R-0069); clearing saved visibility while loading
   an adjacent visitor cannot hide the contact. The worker moved the visitor
   adjacent only after loading, before explicitly revealing its sight. The
   other ten focused cases passed, including ordinary AI attack eligibility
   becoming false after accepted peace; the corrected focused rerun passed all **11 cases**, zero failures/errors/skips.
9. R-0084 CI `38958675524` also failed on the same two checks: **242
   UI/support tests, one failure (TC11) and one error (TC53)**, zero skips.
   Native packages, compatibility and Pages succeeded, but game publication
   was skipped. This corroborates the open shared UI blockers; it does not
   invalidate the already passing model and focused gameplay results.
10. Final targeted Docker `run-aEKtDp` passed **30 tests**, zero failures,
    errors or skips: TC59 (four, including Enter acceptance and Escape cancel),
    TC11 (four), TC40 (15), TC52 (two), TC53 (two) and three page-contract
    checks. Main inspected the toolbar screenshot: labels retain full width,
    overflow wraps below and the right-side information remains unobscured.
    Full model and UI verification is now running in Docker with no selection
    filter; its result and publication remain required.

11. Franklin automatic proposals and saved report notices were added in the
    isolated R-0088 worktree. Review found a reverse-direction pending-offer
    collision; the worker added an unordered-pair guard and a fresh-turn AI
    regression. The first focused run had **26 tests, one failure**: the test
    incorrectly expected the previous-turn notice to remain after a new turn.
    `GameManager` clears that report as designed; main corrected the expectation
    without changing gameplay. The subsequent full model run passed **1646 tests**,
    zero failures/errors/skips, including six Franklin cases. Game/player-test
    compilation, shared formatting (31 Java files, exact tokens and idempotence),
    nine documentation checks and strict MkDocs passed. The new TC59 automatic
    offer journey and tribute UI remain unverified; the older Docker snapshot
    cannot establish acceptance for these later changes.

### R-0088 European tribute — 2026-10-16

1. Scope: UC-069 / DIP-004. The same bounded Sol low worker supplies model,
   persistence and rule tests in an isolated worktree based on main 92c5b806
   plus the reviewed R-0087 model dependency. Main supplies the report action,
   localized proposal dialog, F51/TC60 journeys, review and actual verification.
2. Confirmed demands transfer at most 200 gold only when the actor's armed
   land strength is at least twice the rival's. Paid, refused and empty-treasury
   outcomes start the pair's ten-round wait without changing its relation.
   Cancel/preview/stale confirmation do not. Pending and resolved state persist.
3. Initial focused check passed **19 tests**, zero failures/errors/skips:
   eight tribute cases and eleven peace regressions. Main reviewed the missing
   operational cooldown boundary and low nonzero treasury case; the worker
   added actual rejection at round nine, confirmation at round ten for the
   remaining 117 gold, treasury conservation and stale strength/role rejection.
   The final focused rerun passed **20 tests**, zero failures/errors/skips.
   The later combined full model run passed **1646 tests**, including nine
   tribute cases; no UI acceptance or publication is claimed.
4. Main compiled all game and player-test sources successfully. F51/TC60 covers
   keyboard cancel, postponed Continue, one-time confirmed payment, unchanged
   war/peace, visible refusal, stale treasury rejection and a zero-gold rival.
   These player scenarios have not yet run. No local model was used.

### R-0106 Fortification order timing — 2026-10-16

1. In an isolated worktree based on `92c5b806`, Sol low implemented the shared
   order/persistence and initial rule tests; main implemented toolbar/status,
   F52/TC61 and the AI order-preservation guard. No local model was used.
2. Initial full model run passed **1624 tests**, zero failures/errors/skips,
   including three `FortifyOrderTest` cases and one `FortifiedUnitAiTest`.
   Main compiled all game/player-test sources. The shared formatter checked
   twenty Java files with exact-token and idempotence checks passing.
3. Review requested additional unchanged-state boundary tests for stopped,
   wrong-owner/stale units, invalid placement and other orders, effective-stage
   cancellation and intervening owner turns. The expanded focused rerun passed **seven tests**, zero failures/errors/skips.
   Main added an otherwise legal attack before and after explicit cancellation;
   its first two runs failed because the fixture was not started and then had
   zero movement. Correcting those fixture conditions produced the passing
   result. Production code was unchanged by these test corrections.
4. F52/TC61 contains issue/inspect/cancel and Continue before/after activation,
   keyboard confirmation, visible stage, unchanged rejected drag and restored
   movement after effective-stage cancellation. These UI scenarios have not run.
   Shared toolbar, sentry and plowing regressions and publication remain open.


### Combined peace, tribute and fortification verification — 2026-10-16

1. The reviewed R-0106 source changes were applied to the isolated R-0088
   worktree, which already contains R-0087 and the Franklin follow-up. Main
   remains unchanged by this integration; the older full Docker run uses its
   earlier R-0087 snapshot.
2. The combined full model suite passed **1653 tests**, zero failures/errors/skips.
   All player-test sources compiled, packaging/Javadoc passed, and the shared
   formatter checked **49 Java files** with exact tokens and idempotence intact.
   Nine documentation tests and strict MkDocs also passed before this evidence
   paragraph was added.
3. A targeted Docker run is queued behind successful completion of the existing
   full baseline: TC59/60/61, TC33, TC45, TC11, TC15/18, TC36, TC56/57 and the
   page-verification contract. Neither the queued selection nor the older
   snapshot proves acceptance of the new player journeys. Publication remains
   pending.

4. The older full Docker snapshot subsequently exposed TC34/06 failing with
   `The tile remains covered by the game controls`: its northern hidden tile
   lay entirely beneath the wrapped toolbar. The captured screenshot confirms
   two rows over the map; this is a layout defect, not a relaxed gameplay check.
   Main bound the game content's top padding to the toolbar's actual height,
   so the map canvas begins below all rows, and added a geometry assertion to
   TC11's existing layout verification. The repair is in the combined isolated
   tree only. The pending exit-zero-gated Docker command will not start after
   this failed baseline; its eventual terminal state must be observed before
   launching the repaired selection, including all TC34 scenarios.

5. The older full Docker `run-GmgnNe` finished with **1631 model tests passing**
   and **250 UI/support tests, zero failures, one error, zero skips**. The only
   error was TC34/06's covered northern tile. TC11 and TC53 passed in this full
   snapshot, alongside the existing manual peace journeys. No game publication
   follows from this failed run.
6. The original gated follow-up correctly ended without running. Repaired
   combined Docker `run-jSb6yX` now runs the above selection plus all TC34 cases.
   The layout repair compiled and its documentation checks passed before this
   result note. New peace/tribute/fortification UI acceptance remains pending.

7. Interim combined-run evidence: all five TC59 peace cases (including the
   Franklin automatic offer), all five TC60 tribute cases and all four TC11
   toolbar cases passed. TC61/02 reached the restored effective order but its
   hover helper expected a new hover event while the cursor was already on the
   selected tile. The test now moves to an adjacent tile first, then returns
   before the real rejected drag; location, order and movement assertions remain
   unchanged. That correction is outside the running snapshot and requires a
   rerun. Main visually inspected `r106-fortified-order.png`: both toolbar rows
   remain above the map and the full cancellation label is visible. These interim
   observations do not close combined acceptance or publication.

8. Final local acceptance: `run-jSb6yX` completed 46 UI/support tests with
   one failure (the TC61 hover setup above), zero errors and zero skips. All
   seven TC34 northern-map/regression cases passed. After the test-only
   correction, Docker `run-A5oBhI` passed both TC61 journeys and all three
   `PageVerificationContractTest` checks: five tests, zero failures/errors/skips.
   The combined production model had already passed all 1653 tests; subsequent
   production changes were limited to the verified toolbar/map layout.
   The shared formatter checked 50 Java files with exact code-token preservation
   and idempotence; nine documentation tests and strict MkDocs also passed.
   Main integrated the reviewed sources on 2026-10-16 after checking original
   file hashes. Full CI and publication remain pending.

### R-0093/R-0094 ordinary land combat — 2026-10-16

1. Preparation uses isolated branch `codex/r93-combat`, based on `1f56c85b`.
   The bounded Sol low worker owns model calculation, quote validation,
   persisted combat RNG, equipment-loss outcomes and model tests. Main owns
   the localized dialog, status feedback, TC35/player helpers and acceptance.
2. TC35 retains the existing legal-selection, cancellation, stale-command and
   Continue journeys. Its former destruction expectations change to the agreed
   surviving loser, exact equipment, both movement allowances and original
   locations; this is a gameplay rule change, not a weakened assertion.
   Additional cases cover a mounted defender changing after the quote, a
   defeated dragoon retaining muskets through Continue, and the exact forest
   plus effective-fortification example (3 against 4, displayed 42.86%).
3. Production code and new tests are in progress. No new model/UI pass or
   publication is claimed; main `1f56c85b` CI is a separate earlier snapshot.

4. First completed model acceptance: `mvn -pl microcol-game -am test -q`
   passed **1681 tests, zero failures/errors/skips** across 171 report files.
   Main independently totaled the XML reports. An earlier run exposed three
   obsolete destruction expectations, updated to assert the agreed survivor
   identity, equipment and movement. Main review found a multiplier-floor bug;
   the worker repaired it and covered the boundary before this full pass.
   Additional real fortified-loss boundary tests are being added separately.
5. Main applied the shared formatter to all 23 changed Java files: exact code
   tokens and idempotence passed. Nine docs tests and strict MkDocs passed
   before this evidence note. Docker now runs TC35, TC33, TC61, TC42/43, TC59/60
   and the page verification contract; this selection is not yet accepted.

6. Docker `run-5mxmy1` finished **40 tests: zero failures, three errors, zero
   skips**. TC35/07, /09 and /11 exposed the new battle-status notification
   updating a JavaFX label from the model's combat thread. The main-owned
   listener now captures the resolved message and posts it through
   `Platform.runLater`. All other selected scenarios passed, including both
   localized strength previews and the 3-vs-4 forest/fortification case.
   A fresh TC35 plus page-contract run is checking the repair.
7. Main visually inspected English, Czech and forest/fortification dialog
   screenshots: strengths, percentages, equipment-loss explanation, war warning
   and both buttons are visible without clipping. A later focused model run
   passed all five `CombatOutcomeTest` cases, including both fortified-loss
   continuations; it added two tests after the 1681-test full run.

8. Repair acceptance: Docker `run-J2f73p` passed all **12 TC35 cases and
   three page-contract checks**, zero failures/errors/skips. This verifies
   the FX-thread repair through actual accepted attacks, including the status
   message and save/Continue. All remaining cases from the earlier 40-test
   selection had passed without source changes. Full CI/publication remain
   independent gates; the earlier 1681-test model run predates only the two
   additional model tests and GUI notification scheduling correction.

9. Packaging/Javadoc (`mvn -pl microcol-test -am package -DskipTests`)
   passed after the repair. Main integrated 85 reviewed source/fixture files
   with unchanged-original hash checks; unrelated R-0070 remained untouched.
   The final integrated documentation checks and publication are tracked below.


## R-0095 artillery lifecycle — 2026-10-17

1. Isolated implementation based on `0d027e62` adds artillery construction,
   European price history, one-hold transport, fortify eligibility and persistent
   full/damaged states. `ArtilleryTest` has ten passing cases; the complete
   game suite has **1,693 tests, zero failures/errors/skips**, independently
   counted from 172 XML reports. Logs are in `target/r95/model-full.log` and
   `model-focused.log` in the R-0095 worktree. The native-raid bonus has no
   runtime role to exercise and remains pending; ordinary civilian combat
   strength/capture still belongs to R-0096.
2. Main review corrected an omitted +50% artillery attack bonus. The first
   Docker run `run-wgScNc` passed all twelve TC35 cases; TC62 failed its first
   strength assertion, leaving the modal open and causing two later failures.
   Retry `run-KoWgYd` passed purchase/one-hold/Continue and construction paths,
   but failed destruction feedback. An explicit wait also timed out, ruling out
   a simple delayed FX notification. Outcome feedback now follows completion
   of the fight animation. Combined `run-sj4d02` passed all four TC62 cases,
   all nine TC21 cases and three source-contract cases; eleven of twelve TC35
   cases passed. TC35/11 checked its message before the new animation-completion
   notification. Its existing verification now awaits the actual message,
   keeping every equipment/location/Continue assertion. Rerun `run-ldP8ag`
   passed all twelve TC35 cases and three source-contract checks, zero failures,
   errors or skips. Thus all selected player paths passed; the combined first
   run is retained as a failure rather than relabeled successful.
3. `TC_62_F53_artillery_test` covers English/Czech strength previews and cancel,
   two defeats separated by Continue, European 500→550 pricing, shipment in one
   hold, armory admission and construction. Page assertions remain in named
   verification methods. The source contract passed three checks. The English
   battle screenshot was visually checked: strengths, penalty, consequences and
   both buttons are readable. The Czech screenshot from `run-sj4d02` was also
   visually checked: the long outdoor penalty wraps without clipping.
4. Prior full CI `38969438804` at `1f56c85b` completed with 258 UI/support tests,
   one failure and no errors/skips: TC21/08 targeted tile `[24,13]` instead of
   `[24,16]`. `FreePlayAuditPage.hoverTileDirectly` incorrectly added stage
   coordinates to a canvas-local point. It now uses `Canvas.localToScreen`,
   preserving both original same-tile description assertions. Verification of
   this correction passed all nine TC21 cases in the combined Docker run.
5. Packaging/Javadoc, nine documentation checks and strict MkDocs passed after
   the implementation and test corrections. Main applied the shared formatter
   to 24 changed Java files with exact-token and idempotence checks. Integration
   and publication remain pending; these local results do not identify a new
   published game build.

## R-0098 ship repair — 2026-10-17

1. Scope: COMBAT-014, BUILD-010/011, UC-076/046. A saved damaged ship
   follows transfer and repair owner updates while its cargo remains aboard
   and inaccessible. The battle trigger and loot remain R-0097.
2. Implementation in progress: `ShipRepairTest` covers model selection,
   progression and rejection; `TC_63_F54_ship_repair_test` covers actual turns,
   Europe/drydock, EN/CZ progress, Continue and restored passenger access.
   No checks or publication are claimed by this preparation record.
3. Selected regression: TC01/04/23/28/29 and
   `PageVerificationContractTest`. Full CI remains independently required.

4. Main model acceptance: the first full run had 1705 cases, zero failures
   and two errors from the new carrier cast in existing `UnitTest`. Sol repaired
   the cast with a type-safe damage check; the two original tests and all 12
   repair cases passed. The final full run passed **1705 cases**, zero failures,
   errors or skips (173 XML suites independently counted). Package/Javadoc
   and the shared formatter for 25 changed Java files also passed.
5. Docker `run-IwzCEt`: five cases, one failure; the English repair journey and
   three source-contract cases passed, but the Czech test called the English
   screen-name overload. Main selected the existing Czech-name overload.
   Docker `run-YM6sqX`: 19 cases, one failure, zero errors/skips. TC01/04/23/28/29,
   the contract and three TC63 journeys passed. TC63/03 could not open the game
   because the prior Czech test left its modal open. Explicit final closes
   repaired the test lifecycle without weakening any assertion. Final checks
   use the completed repair schema, including saved original port/sea kind.

6. Final Docker `run-bYLOKN` passed **16 cases**, zero failures/errors/skips:
   all four TC63 journeys, both TC53 population scenarios, seven additional
   TC37 calendar scenarios and three source-contract checks. Main visually
   inspected both EN/CZ repair screenshots: full explanations and progress
   fit without clipping, and the close control remains visible. The separate TC40 run passed all **15 job-choice scenarios**, zero failures/errors/skips,
   verifying the shared job-menu helper.
7. Integrated and pushed as `55b4da3f`. Compatibility and Pages passed;
   complete game acceptance/publication run 38976061239 remains in progress.
   R-0098's saved repair journey does not by itself accept R-0097's battle trigger.

### Earlier R-0093/R-0094 CI follow-up — 2026-10-17

1. Full run [38971531376](https://github.com/jajir/microcol/actions/runs/38971531376)
   on `0d027e62` failed: 261 UI/support cases, one assertion failure, one error,
   zero skips. TC21/08 was the already diagnosed tile-coordinate issue repaired
   in `fbfd421b`. TC53/01 timed out opening an indoor job menu after a rejected
   departure; its model state and preceding population checks were unchanged.
2. TC53's current diagnostic run passed both cases without reproducing that
   timeout. Failure-only bounds/target/screenshot capture was added to
   `ColonyJobPage`; there is no retry, longer wait or relaxed assertion. This
   is diagnostic evidence, not a root-cause fix or successful earlier CI.
3. Native packages, compatibility and documentation succeeded for `0d027e62`,
   but no game release was published from that failed run. Publication of
   `fbfd421b` remains separately pending.

## R-0097 naval combat — 2026-10-17

1. Scope: COMBAT-014 / UC-076. Six naval strengths, two saved combat draws,
   capacity-limited market-value loot, explicit passenger fate, sinking or the
   R-0098 repair path. Privateer diplomacy and blockade remain separate scope.
2. Model snapshot: `NavalCombatTest` 9/9 and the full model suite 1714 tests,
   zero failures/errors/skips (`/tmp/r97-model-full.log`). Main reviewed the
   preflight checks, deterministic goods transfers and immutable result event;
   package/Javadoc passed (`/tmp/r97-package.log`). The two additional stale-market and repair-duration checks passed
   in the focused 11/11 suite (`/tmp/r97-focused-boundaries.log`); production
   model code did not change after the full suite.
3. Docker player acceptance is running for `TC_64_F55_naval_combat_test`,
   TC63 repair, TC35 shared confirmation and `PageVerificationContractTest`.
   TC64 covers cancel, English sinking/prizes, Czech damage/repair overview,
   Continue and cargo changes during review. Results and screenshot inspection
   remain pending; source existence is not acceptance.
4. First Docker run: TC35 12/12 and source contract 3/3 passed. TC64 failed
   at loading its new fixture: main authored an invalid `FURS` goods identity
   instead of `FUR`. Corrected the fixture and independently loaded the model
   successfully. Also corrected the expected stale-offer text to the existing
   localized rejection and the Czech ship name. Rerun remains pending.
5. The second TC64 run reached the real fight review and exposed an unescaped
   percent sign in the new English/Czech naval warning. `I18n` formats messages
   with `String.format`; changed the literal to `50%%`. This was a GUI resource
   defect, not a worker/model failure. The next run includes TC64, the source
   contract and TC22; all results remain pending.
6. Prior R95 CI run 38973979651 completed with 265 UI/support cases, two
   failures and one error, zero skips. TC22's exhaustive purchase roster omitted
   the newly implemented Artillery; corrected its expected list. Its following
   status-bar failure followed the unclosed failed purchase dialog. TC53's
   indoor context-menu timeout recurred; R98 diagnostics are pending and no
   root-cause fix is claimed. All native packages, compatibility and Pages passed;
   game publication was skipped.
7. Third run: TC22 5/5 and source contract passed; TC64 cargo-staleness
   passed. Its sinking assertion incorrectly assumed silver was the most valuable
   good. Actual current sale prices are Fur/Ore 4 and Silver 3; the correct
   capacity-limited prize is 60 Fur and 7 Ore, leaving 150 Silver to sink.
   Updated UI assertions to those existing prices; no production rules changed.
   The failed English modal prevented the following Czech setup. Final TC64
   rerun is pending.
8. Final Docker `run-qQ2zCB`: **6/6 passed**, zero failures/errors/skips:
   all three TC64 journeys and three source-contract checks. Main inspected
   both English/Czech screenshots: complete loss/prize/passenger/destination
   text and acknowledgement control are visible without clipping. TC22's
   preceding corrected run passed 5/5; TC35 12/12 and TC63 4/4 passed on the
   same production Java snapshot (before the naval-only message escape fix).
9. Integrated and pushed as `695a54a7`; full CI/publication run 38977696381
   remains in progress. Model boundaries, chosen player journeys and package/docs
   checks do not replace full acceptance.

## R-0099 privateer hostilities — 2026-10-17

1. Scope: DIP-002, COMBAT-002/003, UC-069/072. A legal privateer attack or
   counterattack against a privateer preserves the European relation. Other
   attacks retain ordinary treaty confirmation and validation.
2. Focused model acceptance passed 33/33 (PrivateerExceptionTest 6,
   NavalCombatTest 11, AttackOrderTest 16). Main full model suite then passed
   **1722 tests**, zero failures/errors/skips (`/tmp/r99-model.log`).
   Coverage includes both privateer directions, peace/war preservation, stale
   relation rejection with a whole saved-state comparison, ordinary declaration,
   excluded merchant attacks and existing Crown hostility.
3. `TC_65_F56_privateer_test` and TC35/TC64 regression are running in Docker.
   Acceptance covers English/Czech mandatory explanation with hidden advisor,
   cancel, both combat directions, Continue and changed relation rejection.
   Source implementation, model checks and passing strict docs do not yet claim
   complete UI acceptance or CI/publication.

4. First TC65 run reached and verified the Czech counterattack outcome, then
   TestFX's selector-based close encountered `PrismTextLayout.lines == null`
   while computing visibility off the FX thread. The modal remained open and
   following TC65 setup failed. `NavalBattlePage.closeOutcome` now resolves
   the button's screen center on the FX thread before an actual robot click;
   no retry, longer timeout or weakened assertion was introduced. Rerun pending.

5. The first combined run completed 21 cases with two failures and one error,
   all in the interrupted TC65 class; TC35 12/12, TC64 3/3 and source contract
   3/3 passed. The corrected helper is being checked with TC65, TC64 and the
   source contract. Package/Javadoc passed, as did 9 documentation tests and
   the strict MkDocs build; no production model change followed the full suite.
6. The second run passed the Czech TC65 case but subsequent TC65 setup still
   failed (9 total cases, two failures, zero errors/skips; TC64 3/3 and source
   contract 3/3 passed). Coordinate-based closing removed the JavaFX exception
   but did not establish reliable modal teardown. Replaced that approach with
   the existing Escape action and an explicit wait for the captured dialog's
   window to stop showing. This also avoids querying text layout from TestFX's
   background visibility filter. The final affected run is pending.
7. Final Docker `run-zYoFo4` passed **9/9**, zero failures/errors/skips:
   TC65 3, TC64 3 and source contract 3. Main inspected both English/Czech
   review screenshots: strengths, chance, naval consequences, explicit unchanged
   relations and both controls fit visibly. Escape plus observed window closure
   passed in both classes; no production change followed model acceptance.
8. Integration and full CI/publication remain pending. Last verified published
   game remains `dev-131-d8bff42a1962`; later pushes are not publication evidence.

## R-0111 military promotion — 2026-10-17

1. Scope: COMBAT-008 / UC-074 veteran acquisition. Ordinary eligible armed
   winners have one 25% promotion draw; Washington guarantees one step.
   Specialty, identity and rank persist; veteran bases are 3/4.5 and are not
   multiplied twice. Continental/revolutionary promotions remain separate scope.
2. Model implementation and TC66 player paths are in progress. Planned checks
   cover cancel, ordinary promotion/nonpromotion, Washington/expert retention,
   persistent information and the next actual strength review after Continue.
   Test results, integration and publication are not yet claimed.

3. Initial model snapshot passed 1729 full-suite tests with zero failures,
   errors or skips. Main review then corrected an over-restriction in its own
   refinement: an eligible soldier defeating artillery can promote; only an
   artillery winner is ineligible. A targeted worker repair and regression
   test are in progress; the prior full-suite result does not certify that
   changed production snapshot. Initial Docker player acceptance is still running.
4. Targeted correction passed 23 focused cases (artillery 11, promotion 7,
   ordinary outcome 5), including promotion against full/damaged artillery,
   unchanged defeated/destroyed event facts and saved draw count. First Docker
   attempt stopped at test compilation: main called `setActionPoints` through
   the Unit interface instead of its colonist implementation. Corrected the
   test-only fixture helper; no UI case ran in that attempt. Full model and
   Docker acceptance are rerunning on the corrected production snapshot.

5. Final model snapshot passed **1730 tests**, zero failures/errors/skips
   (`/tmp/r111-model-final.log`); package/Javadoc passed. Main reviewed promotion
   timing, unchanged unit identity, persisted rank, artillery event facts and
   frozen eligibility/founder inputs. Shared formatter passed for 24 changed
   Java files. Docker player acceptance remains in progress.

### R-0098 full CI follow-up — 2026-10-17

1. Run 38976061239 on `55b4da3f` failed with 269 UI/support cases, two failures,
   one error and zero skips. TC22's expected purchase roster still lacked
   Artillery in that older snapshot; this and its following setup cascade were
   addressed in `695a54a7`, with all five TC22 cases passing locally.
2. TC53/01 again timed out opening the indoor job menu. Failure diagnostics show
   the click inside the canvas/slot and no matching menu. Screenshot and source
   review did not establish occlusion or invalid geometry. Pointer-event delivery
   remains unproven; additional failure-only event/pick diagnostics are being
   added, with no retry or increased timeout. No root-cause correction is claimed.
3. Native packages, compatibility and Pages passed, but publication was skipped.
   Last verified game remains `dev-131-d8bff42a1962`.

### R-0111 player verification corrections — 2026-10-17

1. The next Docker run executed 22 cases: TC35 (12), TC62 (4) and the
   source contract (3) passed; TC66 had one pass and two lookup errors. Both
   promotion cases had already verified the model rank and feedback text,
   but the selected tile's unit panel retained its pre-combat contents.
   Clicking the same selected tile does not emit a new selection event.
2. Refresh the existing right panel when combat resolves. Screenshot inspection
   also exposed the second feedback line outside the scene: the status label
   was in a non-layout Pane. A wrapping VBox now lays out the complete message;
   TC66 checks its preferred height and scene bounds as well as both languages.
   The corrected TC66 and shared TC40/TC53 paths are being rerun in Docker.
3. An additional model boundary case independently seeds the saved generator
   to produce just below 0.25 and exactly 0.25 after a winning combat draw.
   Only the first promotes; both consume exactly two draws. The focused
   promotion suite passed 8/8; no production model change followed the
   1730-case full-suite result.

4. The corrected run executed 23 cases: TC40 15, TC53 2 and source-contract
   3 passed; TC66 passed its nonpromotion case but both promotion cases failed
   the new text-height assertion. Screenshots confirmed the stale panel was
   refreshed, while the surrounding VBox still shrank the status bar and the
   narrow unit label truncated its rank. Preserve preferred text height in the
   status bar and wrap the veteran label; rerun TC66 with ordinary combat TC35.
   The visibility failures remain recorded, not replaced by text-only checks.

5. Docker `run-JSaUOj` passed **18 cases**, zero failures/errors/skips: TC66
   3, ordinary combat TC35 12 and source-contract 3. Main viewed English/Czech
   screenshots: both complete outcome lines and the wrapped veteran rank fit.
   Continue preserves the expert and next combat bases 3/4.5. The earlier
   TC40 15 and TC53 2 passing cases also verify the shared menu diagnostics.
   A final Czech wording correction is checked separately before integration.

6. Final packaging/Javadoc and documentation (9 tests and strict MkDocs) passed.
   Shared Java formatting checked 27 files with unchanged code tokens and
   idempotent output. Complete CI/publication is still a separate gate;
   R-0111 remains ready for development until that acceptance is established.

7. Final Czech wording verification `run-Ef7V2M` passed 4/4 (TC66 Czech
   Washington/expert/Continue plus three source-contract checks); the final
   screenshot was viewed and both feedback lines remain readable.

## R-0110 religious production — 2026-10-17

1. Scope: PROD-009 / FATHER-019 / UC-040. Church/cathedral unattended and
   staffed output follows the agreed totals; Penn floors 150% of aggregate
   crosses once. No new production formula in GUI or save migration.
2. Same Sol low worker implemented only model/tests. Initial focused failures
   came from a null owner in the old mocked church and an invalid four-category
   Congress offer after adding Penn. Repairing those fixtures produced 42
   passing focused tests. Review strengthened the second-colony Penn assertion
   and added expert cathedral 15→22; the promotion-independent religious suite
   then passed 17/17. Logs are retained in the R110 worktree's target directory.
3. Main full model suite passed **1739 tests**, zero failures/errors/skips,
   on base `4436fe19` with the R110 changes (`/tmp/r110-model-full.log`).
   This snapshot does not yet include the separately staged R111 changes.
   The shared formatter passed for all seven changed Java files.
4. TC67/F58 player journeys are prepared for actual upgrades, unattended and
   ordinary/expert output, Penn rounding, next-turn national cross credit and
   Continue in English/Czech. Docker acceptance, integration and publication
   remain pending; no player success is claimed yet.

5. Initial Docker acceptance was interrupted after TC67 exposed two distinct
   issues. The English journey reached the cathedral but could not open its
   worker menu: screenshot inspection showed the building absent because the
   legacy `#id_church` CSS explicitly hid every religious tier. Remove that
   rule and require every building ancestor to be visible in the page check.
   The Czech helper then supplied two military Congress candidates and no
   religious candidate after inserting Penn; the save was rejected by the
   existing validation dialog. Replace Penn's offer with Sepulveda. These were
   main-agent GUI/fixture corrections; no completed test count or success is
   claimed for the interrupted run (`/tmp/r110-ui.log`).

6. Retry executed 9 cases: Czech TC67, both TC47 immigration cases, both TC52
   printing cases and three source-contract checks passed. English TC67 still
   timed out opening the visible cathedral worker menu after completing the
   upgrade; additional event-target diagnostics now investigate this shared
   interaction. The error remains open. Main started a root Maven clean while
   this Docker run was exporting, deleting its mounted result directory;
   `/tmp/r110-ui-retry.log` retains the counts, but that run's screenshots/XML
   were not retained. A fresh Docker run preserves its output separately.
7. Combining R110 with `f9ad9fb9` initially hit 11 missing-resource errors:
   Maven retained an older compiled Loc.xml without the new veteran label.
   Source resources were correct; a clean build passed **1748 model tests**,
   zero failures/errors/skips (`/tmp/r110-model-clean.log`). No production
   model correction was needed. Avoid cleaning a worktree while Docker uses
   its result directory.

8. Combined Docker `run-aS2oM7` executed five cases: Czech TC67 and three
   source-contract checks passed; English TC67 again timed out. The new trace
   proved both the press and context-menu request picked the empty
   `panel-dock` VBox at scene (321,303), and never reached the cathedral.
   The dock is drawn after the buildings; its empty bounding rectangle blocked
   the intended worker. Disable bounds picking on that shared VBox, leaving
   ship/crate children interactive, and verify colony and Europe dock paths.
   This does not establish the cause of the separate TC53 CI failure.


9. After the shared dock picking correction, Docker `run-E1JUBf` passed
   **9 cases**, zero failures/errors/skips: TC67 English/Czech, TC19/01
   confirmed unloading, TC16 embark/disembark, both TC53 cases and the three
   source-contract checks. Main viewed both religious production screenshots:
   the staffed cathedral preview reads +15 and Penn's ordinary preview +13.
   Actual credit and Continue pass. The requested TC06 method filter matched
   no method (its stable names begin TC_06), so Europe trading is explicitly
   rerun using the whole TC06 class rather than counted as covered here.
10. Packaging/Javadoc and documentation (9 tests and strict MkDocs) passed
    after the dock fix; shared formatting checked all eight changed Java files.
    Complete CI/publication remains pending.

11. Europe trading regression passed 5/5 (both TC06 buy/sell/cargo-transfer
    cases and three source-contract checks), with zero failures/errors/skips.
    The final shared dock implementation now has colony and Europe interaction
    acceptance (`/tmp/r110-ui-europe.log`).

### R-0097 complete CI follow-up — 2026-10-17

1. Development run 38977696381 on `695a54a7` failed with 272 UI/support cases,
   zero failures, one error and zero skips: TC53/01 again could not open the
   indoor worker menu after rejected departure. The older trace lacks the new
   event-target instrumentation. TC22's corrected artillery roster no longer
   fails. Compatibility and Pages passed; no new game publication is claimed.
2. The complete log is retained at `/tmp/r97-ci-failed.log`. New event-target
   diagnostics are published in `f9ad9fb9`; use that evidence before attributing
   TC53 to the now-diagnosed religious-building dock overlap.

### R-0107 education work — 2026-10-17

1. In progress on `codex/r107-school-education`, based on `8c1c1bb6`.
   Astra medium owns GUI, fixtures, review and acceptance; the reused Sol low
   worker owns bounded model changes and model tests. No local model is used.
   School assignments and productive-work learning implement UC-050/051.
   TC_68/F59 covers visible assignment, preview-only close, self-teaching
   rejection, explicit cancel, mid-course Continue, four-update graduation
   and the twentieth productive update, including English/Czech feedback.
   Further cases cover paused teaching, same-skill replacement and stale
   confirmation without moving the teacher.
   These are planned checks until a passing result is recorded below.
2. Current-schema shipped/test scenarios gain explicit `educationCourses: []`
   and colonist `workUpdates: 0`. Sixty-six existing fixture files were updated
   with additive edits; semantic comparison against the intended JSON passed.
   No player's save is modified, and no backward-compatibility migration is added.
3. Initial main GUI compilation missed the `UnitFreeColonist` import; corrected
   before the worker's focused retry. First Docker attempt failed test compilation
   because the main page object called package-private `getSlotAt`; use the
   public construction-slot list. Retry captured an unfinished worker test
   calling nonexistent `Unit.isAtPlaceMap`. No player tests ran in either
   attempt (`/tmp/r107-ui-first.log`, `/tmp/r107-ui-retry.log`). Wait for the
   stable model handoff before taking the next Docker source snapshot.
4. Independent main review requested cross-role participant validation, integer
   value comparison for saved self-teaching IDs and reconciliation after an
   externally earned specialty/social step. Worker repairs and boundary checks
   remain in progress. Full acceptance/publication is not yet claimed.

5. The frozen model handoff passed 36 focused EducationTest cases; main started
   the full suite and Docker against that snapshot. Full model result: 1784
   tests, zero failures, 28 errors, zero skips. The errors are newly strict
   work-history loading in older Java fixture mutations/new UnitPo objects
   and initial expedition assembly. The worker is correcting those callers
   while retaining strict current-schema validation. No full-model success
   is claimed (`/tmp/r107-model-full.log`).

6. The worker repaired eight Java test builders and the production
   `ReferenceExpedition` colonist constructor; the nine affected suites passed
   104/104. Main reran the full model suite: **1784/1784 passed**, no
   failures/errors/skips (`/tmp/r107-model-final.log`). Packaging/Javadoc also
   passed (`/tmp/r107-package.log`). The 36 new education tests include the
   skill matrix, staffing capacities, malformed saves and same-colony transfers.
7. Docker `run-2LHVDp` executed 15 UI/support cases: six naval/privateer cases
   and three contract checks passed; four TC68 and two TC53 cases failed. The
   first failure in each colony class was the close-colony action, with later
   cases cascading from a remaining modal. Diagnostic `run-KZYPxA` retained a
   screenshot; a further single-case trace proves the close click was picked
   by `Label[id=colonyName]`, opening rename instead of the map. The invisible
   label bounds overlapped the newly centered button row. Main aligns only
   colony buttons to the right, preserving rename and the real close action.
   Temporary event tracing was removed after capturing
   `/tmp/r107-ui-click-trace.log`; normal screenshots remain. A new TC68/53/40
   run verifies the fix. Czech education dialog was visually inspected: complete
   instructions, participants, school requirement and progress are readable.

8. Header repair acceptance passed **24/24** UI/support cases in Docker
   `run-7Mw5il`: TC68 four, TC40 fifteen, TC53 two and source contract three,
   zero failures/errors/skips (`/tmp/r107-ui-header-fix.log`). School progress,
   mid-course Continue, graduation feedback, productive-work feedback, explicit
   cancel, same-skill replacement and stale confirmation all pass. English
   and Czech education dialogs were visually inspected; labels, course progress
   and all controls are readable. Final wording uses player-facing “turns”
   rather than “colony updates”; TC68 English/Czech and TC58 export controls
   are being rerun against that final wording and header. Shared formatter
   verified **43 changed Java files**, exact code tokens/idempotence pass.

9. Final text/header/export selection passed **9/9** cases in Docker
   `run-ZUpP7o` (TC68 two, TC58 four, contract three), zero failures/errors/skips
   (`/tmp/r107-ui-final.log`). The final colony screenshot confirms separate
   close/export/education controls outside the rename label. English/Czech
   course dialogs are readable; a final Czech grammar correction makes the
   duration label independent of singular/plural. Complete CI/publication is
   still required before marking this requirement done.
10. Czech grammar follow-up passed **4/4** (TC68 Czech plus three source
    contract checks) in `run-R1R4dy`, zero failures/errors/skips
    (`/tmp/r107-ui-czech-final.log`). Documentation validation passed nine
    tests and strict MkDocs; rendered architecture, UX and evidence text was
    inspected. Sources are ready for integration, with complete CI still pending.

### R-0099 complete CI follow-up — 2026-10-17

1. Development run 38978586721 failed with 275 UI/support cases, two failures,
   two errors, zero skips. TC53 repeats the older uninstrumented indoor-menu
   timeout. TC65/02 sees the naval outcome window but Escape does not close
   it; subsequent TC65/01 and /03 fail setup with that modal still open.
   The log does not prove which window received Escape.
2. The reused Sol low quality worker traced the first failure read-only. Main
   changes `NavalBattlePage.closeOutcome` to click that dialog's own OK control
   and retains the same window-closed wait and gameplay assertions. TC64/65
   regression is required; this is not yet a verified fix.
   Compatibility/Pages passed, but this failed game build was not published.


### R-0069 De Soto sight integration — 2026-10-17

1. Selected after inspection at `b2403f3d`: Congress membership existed, but
   neither the base nor colonist sight query applied De Soto and his arrival
   did not reveal the second ring. This is the remaining founder integration
   explicitly required by R-0069, not completion of lost-city or live-visibility
   rules.
2. Planned player acceptance: TC69/F60 chooses De Soto, preserves hidden terrain
   through pre-arrival Continue, produces the last required bell through the
   ordinary turn, and checks unchanged observer position, actual revealed map
   pixels, hidden third ring and post-arrival Continue. TC26 and TC56 retain
   existing exploration, hidden-information and Congress checks. Actual results
   will be recorded after execution; no passing result is claimed here.
3. Sol low changed only the base/colonist sight query, the actual Congress
   arrival boundary and `UnitSightTest`. All **48 focused sight checks passed**.
   Main reviewed the complete diff and independently ran the full model suite:
   **1,807 tests, zero failures/errors/skips** (`/tmp/r69-desoto-model-full.log`).
   Shared formatting passed for model and main-authored test files. Docker
   TC69/TC26/TC56 and the page contract are still running.
4. Initial Docker run `run-LIQG4n` passed **9 tests** (TC69 one,
   TC26 three, TC56 two, page contract three), but main's screenshot review
   found obsolete black fog borders after the stationary reveal. The existing
   `MapManager` rebuilt the border cache on movement, arrival and game load,
   but not after Congress changed explored terrain during turn preparation.
   Added a check for the newly interior tile's absent fog border; the initial
   green run alone is not accepted as completed visual behavior.
5. The stronger TC69 check reproduced the border defect in `run-pLcKXW`:
   **4 tests, one failure, zero errors/skips**, specifically the stale cached
   border at the now-interior tile (30,16). `MapManager` now rebuilds hidden
   coast images when a human turn starts, after preparation can reveal terrain
   through founder arrival. Model sight and stored exploration remain the sole
   authority. Final packaging/Javadoc passed after this GUI correction.
6. Older R-0110 CI `38981740046` completed with **280 UI/support tests,
   one failure and one error**. The first error is TC65/01 in
   `NavalBattlePage.closeOutcome` (Escape timeout), followed by TC65/03's
   blocked status-bar setup. This revision predates the actual OK-button repair
   in `b2403f3d`; that newer complete CI remains pending. Log:
   `/tmp/r110-ci-failed.log`. This is not evidence of a new De Soto failure.
7. Final corrected Docker run `run-L6HLc6` passed **all 9 tests**, zero
   failures/errors/skips (TC69 one, TC26 three, TC56 two, contract three).
   Main checked exported XML counts and viewed the arrival screenshot: the
   obsolete inner black border is gone, the second ring is revealed and the
   third ring remains hidden. The scenario checks the same boundary after
   Continue. Model suite remains **1,807 passing tests**; model code did not
   change after that run. Complete CI/publication remain pending.


### R-0075/R-0076 Brewster integration — 2026-10-17

1. Inspection at `176aedf4` found real Congress membership but no Brewster
   recruitment filtering or earned-immigrant choice. This is an explicit
   integration dependency in R-0075/R-0076. The selected implementation retains
   the accepted Europe-arrival phase before colony production; it does not
   defer accounting until ordinary gameplay has already started.
2. Main authored TC70/F61/F62: actual founder arrival filters candidate tokens
   without changing an existing servant; two earned choices from 230 crosses
   retain the unchanged colony through deferred save/Continue before and between
   choices; the final choice resumes production once. A Czech keyboard path
   exercises the same choices. TC46, TC47, TC56 and preparation/recovery tests
   remain regressions. These are planned checks, not passing results.
3. Main migrated 68 existing JSON fixtures with explicit
   `pendingEarnedChoice: false` and added the two scenario fixtures. No existing
   saved game is rewritten by opening it. The shared formatter covers GUI and
   test changes. Sol low owns only model/event/store code and model tests;
   main reviews, verifies and integrates independently.

4. Independent full model verification passed **1,815 tests, zero failures,
   errors or skips**. The initial Docker run stopped during test compilation:
   the new page object called package-private `Colony.save`. Main corrected the
   test to snapshot colonies through public `Model.save`; no model visibility
   was widened. The selected Docker rerun remains pending. Packaging including
   Javadoc passed; nine documentation tests and strict MkDocs passed before
   this evidence update.

5. The selected Docker rerun reached all new player paths. TC70/01 passed;
   TC70/02 and TC70/03 failed only on the final cross assertion (expected 7,
   actual 6). F62 has an unstaffed **chapel**, whose accepted output is one,
   not a church's two. Main corrected the test's expected total to
   `230 - 100 - 125 + 1 = 6`; pending-state colony equality, candidate IDs,
   Continue checks, keyboard choices and arrival assertions remain intact.
   This is a test-fixture expectation correction, not a changed production rule.

6. The next TC70 rerun passed accounting and identity checks, then exposed a
   second page-object mistake: it checked `isAtEuropePort` (ship berths) for
   immigrants. `Model.addUnitInEurope` correctly places people on
   `PlaceEuropePier`. Main corrected both checks to `isAtEuropePier` and added
   explicit reopen-after-deferral coverage; no gameplay assertion was removed.

7. Final verification: **1,817 model tests** in 179 exported suites passed,
   including AI Brewster and first-start pending/Continue coverage added by
   Sol low after main review. Docker `run-fPMBjH` passed TC46 (6), TC47 (2),
   TC56 (2) and the page contract (3); its two TC70 test-authoring failures are
   explained above. Corrected Docker `run-75XPG6` passed **all three TC70 player
   scenarios plus three contract checks**, with zero failures, errors or skips.
   Main checked the exported XML and viewed English/Czech screenshots; text,
   choices and deferral instructions are legible. The shared formatter passed
   25 changed Java files, including exact code-token preservation and
   idempotence. Packaging/Javadoc, nine documentation tests and strict MkDocs
   passed. Full CI/publication remain independently pending.

### R-0104 Magellan integration — 2026-10-17

1. Inspection at `3fe0c884` found Congress membership and ordinary bounded
   Atlantic durations, but no runtime Magellan bonus. The refined R-0104
   integration applies the accepted +1 ship movement and west-edge-to-Europe
   rounding-up half duration; it preserves R-0071's archived ordinary evidence.
2. Main authored TC71/F63 for actual Congress arrival, higher movement,
   save/Continue with spent movement, an unchanged existing four-turn voyage,
   actual western departure and visible remaining duration, and an ordinary
   return crossing with cargo. TC23/TC28 and TC44 remain regressions. These
   scenarios are planned acceptance, not passing results yet. Sol low owns
   model code/tests; main independently verifies model, GUI paths and packaging.

3. The first independent full model run executed **1,823 tests with 33 errors,
   zero assertion failures and zero skips**. The new global movement-limit
   load guard rejected existing synthetic land-unit fixtures (31 cases) and
   two ship-roster fixtures that changed type without resetting movement.
   Main returned this to Sol low: keep the new validation scoped to ships,
   preserve existing land loading behavior, and give the two roster fixtures
   valid per-type movement while retaining all cargo/departure checks. A full
   rerun and final Docker acceptance remain required.

4. Sol low corrected the scope of the load guard and the two roster fixtures.
   Its 50 targeted regressions and full model suite passed; independent main
   `package` verification also passed **1,823 model tests in 180 suites** and
   Javadoc/packaging. Initial Docker `run-gEx4Wx` passed TC44 (4), TC28 (2),
   TC23 (2) and the page contract (3). TC71 passed founder/movement/saved-voyage
   and western-duration checks, then timed out dragging the ship home.
   Inspection found `AtlanticAuditPage` selected `TitledPanel`'s first empty
   `HBox`, rather than the sibling containing the actual drop handler. Main
   corrected the shared selector to that drop target; existing journeys and
   their cargo/timing assertions remain unchanged. A final regression rerun is
   required before integration.

5. Diagnostic rerun `run-I3Y0rL` reproduced the return-drag timeout. Main viewed
   the exported scene: the existing inbound voyage's Reverse course control
   also overlapped the outbound area's title. The outbound region moves down
   40 pixels within the existing Europe layout; TC71 now checks that the two
   regions do not intersect before using the corrected real drop target.
   Temporary diagnostic captures were removed from the final scenario.

6. During this integration, R-0107's complete CI (`38984101141`) finished with
   284 UI/support tests and four TC29 failures. Sol low's read-only diagnosis
   found the first failed load in TC29/05: its fixture converts a colonist to
   a galleon but retained colonist-only `workUpdates`. The load rejection
   caused the following three hidden-status-bar failures. Main removes
   `workUpdates` and `workSkill` only from that converted ship; all boarding,
   ownership and equipment checks remain. TC29's six scenarios join the final
   Docker selection to verify both the root correction and the apparent cascade.

7. Final Docker `run-7wH6Lo` passed **18 tests, zero failures/errors/skips**:
   TC71 (1), TC23 (2), TC28 (2), TC29 (6), TC44 (4) and the page contract (3).
   Main checked exported XML and viewed the Magellan scene: the outbound title
   is visible below incoming voyage controls. The final run proves the actual
   drag, non-overlapping regions and all passenger regressions. Model/package
   verification passed 1,823 tests in 180 suites; final resource packaging,
   Javadoc, shared formatting of 13 changed Java files, nine documentation tests
   and strict MkDocs passed. Complete CI/publication remain pending; the earlier
   R-0107 failed build was not published.

### R-0069–R-0111 combined acceptance — 2026-10-17

1. The complete [development run 38987733946](https://github.com/jajir/microcol/actions/runs/38987733946) for source `57e16d9110297edf2fec33150e5f1ef84875d658` passed: **1,823 model tests in 180 suites and 289 player/support tests in 74 suites**, with zero failures, errors or skips. Counts were checked against the downloaded `tests-functional` XML reports, not inferred from a green summary. All three native packaging jobs and build-tool checks passed; compatibility run `38987733942` and Pages run `38987733978` also succeeded.
2. This combined run includes all six TC29 passenger scenarios, three TC70 Brewster scenarios and TC71 Magellan. It verifies the stale colonist-to-ship fixture repair after the earlier R-0107, De Soto and Brewster runs failed only on TC29. The earlier failures remain historical evidence, not successful releases. The source, model and targeted player acceptance for each bounded outcome remain in their existing dated sections; this record supplies the final common regression gate.
3. The following requirements now have their whole selected outcome accepted. The listed TC groups are present and passing in the full run; model boundaries and earlier localized/visual checks remain documented with each requirement. This does not imply that an entire UC or every founder effect is complete.

| Requirement | Passing player groups (scenario counts) | Accepted outcome |
| --- | --- | --- |
| [R-0069](requirements/done/R-0069.md) | TC26 (3), TC69 (1) | Explicit sight and actual De Soto integration |
| [R-0075](requirements/done/R-0075.md) | TC46 (6), TC70 (3) | Persistent paid recruitment and Brewster choices |
| [R-0076](requirements/done/R-0076.md) | TC47 (2), TC70 (3) | Cross-based earned immigration and resumable choices |
| [R-0077](requirements/done/R-0077.md) | TC48 (3), TC62 (4) | Ship and artillery purchase escalation |
| [R-0082](requirements/done/R-0082.md) | TC53 (2) | Fortified population minimum for voluntary departure |
| [R-0083](requirements/done/R-0083.md) | TC55 (2) | Quoted construction completion purchase |
| [R-0084](requirements/done/R-0084.md) | TC57 (3) | Actual founder gates for factories/custom house |
| [R-0087](requirements/done/R-0087.md) | TC59 (5) | Peace negotiation and Franklin timing |
| [R-0088](requirements/done/R-0088.md) | TC60 (5) | European tribute choices and cooldown |
| [R-0093](requirements/done/R-0093.md) | TC35 (12) | Shared ordinary land strength explanation |
| [R-0094](requirements/done/R-0094.md) | TC35 (12) | Strength-based ordinary land resolution |
| [R-0097](requirements/done/R-0097.md) | TC64 (3) | Naval outcomes and cargo/passenger fate |
| [R-0098](requirements/done/R-0098.md) | TC63 (4) | Damage destinations, repair timing and continuation |
| [R-0099](requirements/done/R-0099.md) | TC65 (3) | Privateer hostility exception |
| [R-0104](requirements/done/R-0104.md) | TC56 (2), TC71 (1) | Congress acquisition, Jones and selected Magellan integration |
| [R-0106](requirements/done/R-0106.md) | TC61 (2) | Next-turn effective fortification |
| [R-0107](requirements/done/R-0107.md) | TC68 (4) | School teaching and agreed experience progression |
| [R-0109](requirements/done/R-0109.md) | TC54 (1) | Single retained hammer balance across queue changes |
| [R-0110](requirements/done/R-0110.md) | TC67 (2) | Religious building production |
| [R-0111](requirements/done/R-0111.md) | TC66 (3) | Veteran promotion and demotion preserving specialty |

4. Publication is verified as [dev-149-57e16d911029](https://github.com/jajir/microcol/releases/tag/dev-149-57e16d911029): the public immutable prerelease has all ten expected assets. The downloaded three-platform `manifest.tsv` names the exact source above; `SHA256SUMS`, the downloaded manifest and GitHub asset digests agree for every asset. Evidence is retained locally under `target/r149-publication/`; the full CI log is `/tmp/r104-magellan-ci-complete.log`.
5. The automatic `dev` channel is **unchanged at dev-109-0b0cb0593df4**, confirmed from its downloaded manifest. Publication explicitly retained the channel because main had advanced. The new build is available through its immutable release link; do not claim that the launcher automatically receives it. Documentation-only commits and their successful Pages deployments are separate from this game build.
6. The complete R-0062–R-0111 objective remains unfinished: 34 requirements are now done and 16 remain active. R-0074/R-0095 still require actual native integration; R-0085 still requires boycott integration. R-0065, R-0070, R-0086, R-0089–R-0092, R-0096 and R-0100–R-0103/R-0105 retain their remaining decisions or implementation. A passing common regression does not remove these requirements.


### R-0086 construction waiting notice — 2026-10-17

1. Selected [R-0086](requirements/done/R-0086.md), BUILD-019 and UC-043 on
   baseline `3942b5de`. The final user decision is one reminder **each owner
   turn**, as defined by the shared
   [rule](gap-analysis.md#recovery-and-presentation). The queue emits an immutable
   colony/project/shortage snapshot during construction processing with enough
   hammers and insufficient tools. Viewing, dismissal and Continue do not execute
   that phase or append another reminder; the next turn reports the current shortage.
2. Final acceptance requires model coverage for hammer/tool boundaries, consecutive
   updates, current shortage, exact spending/completion, unit projects, immutable
   event roundtrip and malformed events. TC32/TC02 follows the report by keyboard,
   retains the 64 hammers/19 tools wait, saves/continues, then advances to 70 hammers
   with a new reminder and delivers the last tool. Completion leaves 12 hammers,
   zero tools and the following warehouse project; Continue cannot complete or
   debit it twice.
3. TC32/TC08 covers Czech text and mouse navigation; TC09 clicks a retained action
   after capture and checks no colony screen or resource mutation; TC10 fires a
   retained action after game replacement and checks a usable map and unchanged
   games. Page assertions belong in named `verify...` methods. Screenshot and
   geometry checks caught and repaired a clipped/out-of-page action.
4. Earlier targeted passes and the canceled full regression used the superseded
   once-per-episode policy and are not final acceptance. Their failures, repairs
   and actual counts remain in [worker evidence](ai/local-qwen.md#claude-code-subscription-trial-2026-10-17).
   Final-policy acceptance follows; publication is verified separately.

5. Final-policy targeted Docker `run-6Psd2R` passed **87 cases**, zero failures,
   errors or skips: 26 waiting-report, 18 queue-integrity, 20 queue-save, ten
   rush, ten TC32 and three source-contract cases. The caller inspected fresh
   Surefire results and the Czech screenshot at
   `target/docker-ui/run-6Psd2R/microcol-test/warehouse-qa/turn-report-waiting-tools-czech.png`:
   the full action remains inside the paper, with readable text and no overlap.
   Shared formatting passed for 15 changed Java files with no changes, exact
   code tokens and idempotence. This is automated application/visual evidence,
   not a player study or a native macOS input check.

6. Caller full Docker `run-lOI2O1` exited **0**. Fresh XML reports contain
   **1,849 model tests in 181 suites and 292 player/support tests in 74 suites**,
   with zero failures, errors or skips. All ten TC32 scenarios and the three
   source-contract checks are present. The integrated source/resource/test files
   were compared by SHA-256 with the tested worktree and are byte-identical;
   intervening main commits changed documentation only. Reports, logs and
   screenshots from all trial runs are preserved in the primary checkout's
   `target/docker-ui/`, and invocation logs remain under
   `~/Library/Logs/Microcol/claude/2026-10-17-r0086/`.
7. The integration commit is `3c0342c4260b5141ba38be1dfc5143c1692bcf67`.
   Local documentation acceptance passed 21 worker-launcher checks, nine
   documentation checks and strict MkDocs. The rendered requirement shows the
   final per-turn rule. CI/publication evidence follows below.

8. Independent [development CI 39033123358](https://github.com/jajir/microcol/actions/runs/39033123358)
   passed for that exact source: downloaded fresh `tests-functional` XML reports
   confirm **1,849 model tests / 181 suites and 292 player/support tests / 74
   suites**, zero failures/errors/skips. All three native packaging jobs and
   build-tool checks passed. [Java compatibility](https://github.com/jajir/microcol/actions/runs/39033123365)
   and [Pages deployment](https://github.com/jajir/microcol/actions/runs/39033123329)
   succeeded. Live Pages showed the final per-turn rule and revision-pinned
   architecture source links. CI evidence is retained under `target/r86-ci/`.
9. Published [dev-158-3c0342c4260b](https://github.com/jajir/microcol/releases/tag/dev-158-3c0342c4260b)
   is a public immutable prerelease with all ten expected assets. The three-platform
   manifest names the exact source above. `SHA256SUMS` agrees with GitHub asset
   digests, the downloaded manifest and the downloaded Mac ARM application ZIP.
   Release metadata, the publish log and verified downloads are retained under
   `target/r86-publication/`.
10. The publisher intentionally retained the automatic `dev` channel because
    `main` had advanced through concurrent documentation-only commits. Its
    downloaded manifest still identifies **dev-109-0b0cb0593df4**; the launcher
    does not automatically receive build 158. Use the immutable release's
    [Mac ARM download](https://github.com/jajir/microcol/releases/download/dev-158-3c0342c4260b/MicroCol-macos-arm64-dev-158-3c0342c4260b.zip).
    R-0086's status/link closure is a separate documentation-only publication,
    not another game build. The Claude worktree was archived after all run
    evidence was copied to the primary checkout.


## Claude batch 20 targeted acceptance — 2026-10-17

1. The user authorized two ten-requirement batches. Targeted checks are recorded
   below; full functional acceptance and CI remain pending until each batch boundary.
   Model operating evidence is in the [batch record](ai/local-qwen.md#claude-model-selection-batch-2026-10-17).
2. R-0118: shared formatter passed for four Java files (exact tokens and
   idempotence). Docker `run-8Gp9HE` exited 0: `ColonistEquipmentTest` 21,
   `EuropeEquipmentTest` 22, `TC_25_F17_colony_equipment_test` 8 and
   `PageVerificationContractTest` 3; all **54 passed**, no failures/errors/skips.
   New TC25 cases 06–08 cover 19-tool rejection/cancel, exact 20-tool equipment
   retained by Continue, and stock dropping below 20 with the choice open.
   Existing TC01–05 and European equipment outcomes remain intact.
   Reports/screenshots are in the batch worktree's `target/docker-ui/run-8Gp9HE/`.
3. R-0133: after one bounded Haiku repair, all six changed Java files passed
   the shared formatter's exact-token/idempotence checks. Docker `run-LjAQb2`
   exited 0: PassengerTransfer 21, CargoSlot 47, ColonyWarehouseUnload 59,
   WagonWarehouse 8, ShipRoster 7, UnitColonistEmbark 17, SentryOrder 8,
   TC29 6, TC31 8 and PageVerificationContract 3: **184 passed**, no failures,
   errors or skips. The first attempt's non-enum parameter-source compilation
   failure is fixed and the actual failed compilation plus all selected checks
   now pass. TC31_07 additionally rejects dragging a wagon into a ship and
   verifies unchanged wagon cargo, colony stock and Continue. Model checks
   cover four nested-carrier combinations, direct/embark paths, invalid
   restoration, valid passenger identity/movement restoration and slot relation.
   Reports/screenshots: `target/docker-ui/run-LjAQb2/` in the batch worktree.
   Full batch acceptance and publication remain pending.
4. R-0132: first targeted Docker `run-cO7Ogt` passed 104 model checks but
   had two UI errors (absent `#labelGold` during a market choice), with 10
   UI/support checks total and no skips. After the final Sonnet-high repair,
   shared formatting passed on nine Java files and Docker `run-GSzhm6` exited
   0: CargoSlot 56, CargoCapacity 34, NavalCombat 11, EuropeTradeQuote 4,
   TC05 3, TC06 4 and PageVerificationContract 3: **115 passed**, no failures,
   errors or skips. TC06 retains its existing IDs and adds named
   `TC_06_moving_goods_stale_purchase_reduced_capacity` and
   `TC_06_moving_goods_stale_purchase_incompatible_commodity`: invalidated
   purchases show a rejection without charge, renewed offers use current space,
   exact fixture gold/cargo survives Continue. Model checks cover 99/100/101
   requests across three transfer routes, conservation/tax, passenger/mixed
   rejection and valid/invalid disk restoration. Naval loot remains verified.
   Reports/screenshots: `target/docker-ui/run-GSzhm6/` in the batch worktree.
   Full batch regression and publication remain pending.
5. R-0179: after one Haiku authoring repair, the caller applied shared
   formatting to six Java files (exact-token/idempotence passed) and corrected
   the Czech name inflection in the new label. Docker `run-N5F3WJ` exited 0:
   NavalCombat 18, CombatStrength 25, AttackOrder 16, PrivateerException 6,
   TC64 4, TC65 3 and PageVerificationContract 3: **75 passed**, no failures,
   errors or skips. Model checks verify Drake attack 16/defense 12, ordinary
   privateer 12/8, owner isolation, other ships, controlled actual resolution,
   disk roundtrip without stacking and stale order rejection before any draw.
   TC64_04 verifies the named bonus, 61.54% odds against a galleon, cancel and
   Continue; original TC64/65 outcomes remain intact. Reports/screenshots:
   `target/docker-ui/run-N5F3WJ/`. Full batch regression/publication pending.
6. R-0184: Docker `run-WFtFHk` initially passed 50 model checks but had two
   errors among nine UI/support checks: TC72 dragged empty carpenter slot 0
   after `assignCarpenter` placed worker 4 into slot 1. The targeted worker
   repair changed only those two indices, retaining every assertion/timeout.
   Shared formatting passed on six Java files. Rerun `run-VoE0Pb` exited 0:
   LaSalleStockade 15, Congress 16, FounderConstruction 11, FortifiedPopulation 8,
   new TC72/F64 2, existing TC53 2, TC56 2 and PageVerificationContract 3:
   **59 passed**, no failures/errors/skips. TC72 verifies cancel, joining below
   threshold, later third-resident grant, existing eligible colony grant, visible
   stockade, unchanged resources, blocked departure at three, no duplicate and
   Continue. Model coverage includes fields/buildings, capture/new owner, higher
   defenses, forced resident loss, queue/resource preservation and disk/load
   behavior. Reports/screenshots: `target/docker-ui/run-VoE0Pb/`.
   Full batch regression/publication remain pending.
7. R-0147: `run-lpElKF` stopped after 108 model checks with three errors:
   loading supported sugar/cotton/tobacco planters as field workers failed.
   Review also closed the new public specialty mutation's validation bypass.
   `run-GSs2a1` then had 112 model checks with one error in the new foreign-owner
   fixture's raw Gson clone. The second repair reused the configured loader.
   Final `run-aZpHYU` exits 0: ClearSpecialty 55, Education 36, ColonyJobChoice 21,
   TC68 8 and PageVerificationContract 3 — **123 passed**, no failures/errors/skips.
   New TC68_05–08 cover cancel/Escape, confirmed clear, retained job, veteran/tools,
   teaching/course rejection and recovery, stale confirmation and Continue.
   Model checks cover all 21 supported civilian roles, atomic direct-entry and
   foreign-owner rejection, school/work relearning and real disk roundtrips.
   Shared Java formatting passed; reports/screenshots remain under
   `target/docker-ui/run-aZpHYU/`. Full batch regression/publication are pending.
8. R-0119: `run-YFvZ6F` had 181 model checks, two failures and 30 errors.
   Diagnosis/repairs are recorded in the [Claude batch operating evidence](ai/local-qwen.md#claude-model-selection-batch-2026-10-17).
   `run-vGuYyP` passed 279 model checks, TC18's seven scenarios and three
   source-contract checks, but TC15_06 confused UI selection with model order.
   Final `run-D7bgbV` exits 0: **280 model + nine UI + three source-contract = 292
   passed**, no failures/errors/skips. Selected model suites: PlowField,
   AttackBoundary, EuropeanSpecialist, NationalTurnPreparation,
   TurnPreparationAction, TurnPreparationBoundary, UnitTurnOrders, FortifyOrder,
   Terrain, TerrainType, SpecialistProduction, NationalTurnOrder,
   PassengerTransfer, CargoSlot and UnitColonistEmbark. TC15_06 checks two
   pioneers sharing one field and only one 20-tool charge; TC18_02–07 cover
   missing/19 tools, cancellation, four/two-update completion, unfinished and
   completed Continue, exact 20-tool exhaustion and visible rejection reason.
   Model coverage includes old/canceled/foreign callbacks, saved countdown and
   following-colony-update output. Earlier R-0133 specialist cargo reload was
   fixed through the shared colonist storable capability, retaining carrier
   rejection. Shared formatter passed; reports/screenshots remain under
   `target/docker-ui/run-D7bgbV/`. Full batch regression/publication are pending.
9. R-0120 partial verification after the Pro quota interruption: Docker
   `run-mIaLGe` exits 0, **122 passed** (ClearForest 43, PlowField 22, Terrain 5,
   SpecialistProduction 21, NationalTurnPreparation 8, NationalTurnOrder 11,
   existing TC18 7, TC15 2, PageVerificationContract 3), no failures/errors/skips.

## R-0180 founder production (2026-10-19)

1. Přijato v izolované dávce, publikace dosud neproběhla. Jefferson přidává
   50 procentních bodů pouze k obsazené radnici; Paine aktuální daň k obsazené
   i neobsazené části. Tisk se sčítá, noviny nahrazují lis, součet se zaokrouhlí
   jednou dolů. Hudson zdvojuje kožešiny po dosavadních úpravách, nikoli kabáty.
   Sdílené výpočty v Construction a ColonyField používají současného vlastníka.
2. Docker `20261019T124401Z-030c9bb6ea98` ověřil 2 561 modelových testů
   a **18 UI/provozních kontrol**, bez selhání, chyb a přeskočení. TC97
   ověřuje radnici, lis, noviny, výběr kožešinového pole, skutečný tah a Continue;
   doprovázejí jej TC52/77/67/56 a zdrojový kontrakt.
3. Haiku následně doplnilo modelové kontroly obou směrů změny vlastníka při
   skutečném dobytí a výroby kabátů se vstupem 6/2/0. První běh
   `20261019T125225Z-11bee4a3d0d0` měl 2 564/2 566 úspěchů: dvě testovací
   mapy neměly kladný výnos kožešin. Uživatel povolil dokončení Codexem;
   fixture dostala travnaté pole s bonusem lovu kožešin, aserce zůstaly zachované.
4. Finální Docker `20261019T125745Z-e26736a9967a`:
   **2 566 modelových testů a 9 provozních/kontraktních kontrol PASS**, nula
   selhání/chyb/přeskočení. Výběr `*Test,!RivalTradeCycleTest` ponechává
   rozpracované R-0091 samostatně otevřené. Dřívější UI výsledek se znovu
   používá, protože následně se změnily pouze modelové testy a dokumentace.
5. Sdílený formatter: 17 Java souborů, tokeny/idempotence PASS. Dokumentační
   testy a strict MkDocs ověřeny při uzavření. Plná regrese a publikace čekají
   na dokončení desetipoložkové dávky. Tax-demand UI není novým scénářem;
   změnu aktuální daně a následný výnos pokrývá modelový test.


## Claude batch 1 integrated acceptance — 2026-10-17

1. Integrated revision `d2955b57` includes the first ten Claude requirements and
   current main documentation. Full Docker `run-hSFnT1` ran **2,072 model tests
   (all passed) and 320 UI/contract checks (seven failures, two errors, zero
   skips)**. Nine old scenario expectations still used doubled corn from plow
   or manual passenger unloading on owned-port arrival. New feature scenarios
   passed. This full run itself was not green.
2. Caller updated only affected legacy tests/fixtures; production code remained
   identical to the full-run snapshot. TC01/13/21/27/30 now check automatic
   arrival while retaining ordinary-shore selection, cargo, equipment and
   Continue. TC07/08/17/41 use the accepted +1 plow yield, retaining exact food
   accounting and the unchanged 200-food birth threshold. Detailed diagnoses,
   caller-owned corrections and fixture values are in the
   [operating record](ai/local-qwen.md#claude-model-selection-batch-2026-10-17).
3. Corrected-scenario Docker `run-Km1yji` ran 99 checks: 58 model and 41
   UI/contract. All passed except the caller's newly added TC13 helper mismatch
   (it incorrectly required ore from another fixture). Final `run-jfk9pd`
   passes TC13 and all three source-contract checks, zero failures/errors/skips.
   Thus every scenario that failed the full run has a passing rerun; unchanged
   scenarios retain the full-run evidence. No gameplay assertion was weakened
   and no failing/skipped scenario is treated as accepted.
4. Shared Java formatter, 21 local-worker harness tests, nine documentation
   tests and strict MkDocs build pass; final archival/link checks are repeated
   with the completion documentation. Local acceptance covers R-0118, R-0133,
   R-0132, R-0179, R-0184, R-0147, R-0119, R-0120, R-0136 and R-0090.
   CI subsequently passed all 2,072 model and 320 functional/contract checks
   for `333de9b6bf8f` and published `dev-171-333de9b6bf8f`; see the
   [independent publication evidence](publishing.md#claude-batch-1-publication-2026-10-17).

## R-0070 current sight — 2026-10-17

1. Docker `run-aegEQ7` compiled and passed 130 selected model checks, six of
   seven TC26 scenarios and three source-contract checks. TC26_07 timed out
   after an unbalanced synthetic movement event; it was not accepted. Review
   also required colony-only terrain exploration and half-visible fight guards.
2. Sonnet medium repaired those boundaries and the diagnostic event lifecycle.
   Docker `run-9WkN2C` passes **142 checks: 132 model, seven TC26 and three
   source-contract**, zero failures/errors/skips. Selection includes current
   sight, legacy visibility, attack boundaries/orders/roles, peace, tribute,
   reference expedition, first-colony planning/journey, load rejection and
   status-bar disclosure. TC26_04–07 cover departure/re-entry, stale colony
   owner/date and Continue, colony-only sight and capture, hidden movement and
   partial/fully hidden foreign fights. Shared formatter passes.
3. This is targeted requirement-11 acceptance. Full second-batch regression and
   publication remain pending. Worker attempts and caller review are recorded
   in the [operating evidence](ai/local-qwen.md#claude-model-selection-batch-2026-10-17).

## R-0089 European alliances — 2026-10-18

1. Docker `run-6JdvBs` passes **287 checks: 253 model and 34 functional/contract**,
   zero failures, errors or skips, exit 0. Selection: `Alliance*Test`,
   `European*Test`, `PeaceNegotiationTest`, `FranklinPeaceOfferTest`,
   `TributeDemandTest`, `Attack*Test`, `CoastalPortMovementTest`,
   `CurrentSightTest`, TC33 (7), TC59 (5), TC60 (5), TC74 (14) and
   `PageVerificationContractTest` (3). Shared Java formatter, nine documentation
   tests and strict MkDocs build pass.
2. TC74 verifies proposal, cancel/refusal, saved pending offer, stale terms,
   once-only payment, explicit termination/wait, allied passage without access,
   no allied attack, mutual defense, Crown rounds 19/20/21 and Continue,
   Czech labels and retained overlap across Continue before/after termination.
   Model tests additionally cover Crown/defense-driven overlap, departure and
   re-entry restrictions, adjacent combat after separation, malformed overlap
   evidence and saved event randomness. Existing ownership and privateer
   regression assertions remain intact.
3. Earlier `run-4xcJ62` (one model error), `run-M5HEYR` (four UI failures and
   one error) and `run-aH0VVY` (one UI failure) were diagnostics, not acceptance.
   The final selection reruns their affected tests. Worker attempts, fixture
   diagnosis and the main-agent test-call adjustment are recorded in the
   [operating evidence](ai/local-qwen.md#claude-model-selection-batch-2026-10-17).
4. This is targeted requirement-12 acceptance. Full second-batch regression,
   CI and publication remain pending; this does not close all of UC-069.

## R-0100 rebel support — 2026-10-18

1. Docker `run-kPlLaY` passes **2,153 checks: all 2,141 model tests and 12
   functional/contract checks**, zero failures/errors/skips, exit 0. Selection
   `*Test,TC_75_*` includes TC75 (3), `SimpleOperationsTest` (5),
   `PageVerificationContractTest` (3) and `EnvironmentTest` (1).
   Production code was unchanged since `run-Qx6F1u`, which additionally passed
   TC36 (3), TC39 (8) and TC56 (2); its complete selection had 18 passing checks.
   `run-l7vcIM` also verified all three final TC75 journeys before the last
   model-test fixture repair. Full second-batch visual regression/CI is pending.
2. TC75 verifies the read-only national/local report, completed resident-exit
   cap, actual bell credit and Continue, and the 2/8-resident transfer through
   real exit, map movement and a new job. The 1/9 result keeps liberty 200/400,
   displays local 100%/22% and national 30%, including after Continue. The local
   colony tooltip is checked. Czech strings are provided, but TC75 runs in
   English; no Czech player-path acceptance is claimed.
3. Model tests cover exact weighting/thresholds, zero residents, transient
   same-colony reassignment versus completed exit, required saved fields,
   Bolivar, European conquest and subsequent Crown occupation with differing
   founder membership, liberty preservation and roundtrip affiliation.
   Existing famine, birth, Congress and turn regressions pass. Actual Revere
   battle placement remains R-0096, using the shared resident-exit accounting.
4. Diagnostic `run-ZIKsKS` had three model failures; `run-KRtFY7` had two
   failures and one error. Final acceptance reran all model tests after repair.
   The [operating evidence](ai/local-qwen.md#claude-model-selection-batch-2026-10-17)
   separates worker attempts, medium-to-high escalation, main fixture-schema
   edits and inherited movement-mock fixes. Shared formatter, nine documentation
   tests and strict MkDocs build pass. This is targeted requirement-13 acceptance.


## R-0096 colony defense (2026-10-18)

1. Targeted acceptance of [R-0096](requirements/done/R-0096.md), UC-075:
   armed defenders are selected before the civilian group; residents keep their
   actual field/building jobs until the result. Civilian victory transfers the
   whole colony, including all eight fields, indoor workers, identity, liberty
   and cargo-preserving ship escape. Independent field combat captures only
   the worker. Peaceful colonies require declaration review; cancellation and
   stale inputs leave the world and saved random state unchanged.
2. `ColonyDefenseTest` covers frozen places, strongest/stable-ID selection,
   armed-to-civilian transitions, blocked escape, full ownership transfer,
   worker capture/last-resident removal, Revere's actual musket cost, forced
   3→2 fortified exit versus voluntary refusal, last-resident retention,
   military promotion and Save/Continue validation. Removed direct-capture
   callers now use real battles in model/UI fixture helpers.
3. Docker `run-F6cEP6` passes all **2,175 model tests**, zero failures/errors/
   skips. Its selected UI run passes TC04 (1), TC26 (7), TC32 (10), TC35 (12),
   TC76 (10), basic/environment checks (6) and source contract (3). TC33 had
   one obsolete dialog assumption followed by four blocked scenarios; the
   main-owned correction and its intermediate error are recorded in the
   [worker evidence](ai/local-qwen.md), not treated as passing tests.
4. Final focused Docker `run-QeRl9J` passes TC33 (7) and source contract (3).
   Together the unchanged production code has **2,231 distinct passing
   model/selected functional checks**, no remaining failures or skips.
   TC76_01–TC76_10 cover civilian loss/win, armed-first defense, Revere win,
   last-resident behavior and defeat, peaceful confirm/cancel, stale review,
   independent field capture and fortified forced exit. Continuation checks
   preserve workers, equipment, ownership and liberty. TC33 retains Czech
   diplomacy and keyboard coverage; new TC76 journeys ran in English.
5. Main applied the shared Java formatter (37 files; token/idempotence checks
   pass), reviewed the changes and validated documentation. This is targeted
   acceptance only: full Docker visual regression and publication/CI remain
   at the second batch boundary, requirement 20. Status remains ready for
   development until that required batch acceptance; no publication is claimed.


## R-0101 political production (2026-10-18)

1. Targeted acceptance of [R-0101](requirements/done/R-0101.md), UC-078: exact
   support gives staffed positive-base jobs +1 at 50% and +2 at 100%; the
   difficulty threshold gives a single −1 Tory penalty. One snapshot covers
   food, famine, post-famine production and productive experience; newly
   credited bells affect subsequent previews/updates. Prospective job choices
   account for joining residents. The colony tooltip separates bonus, Tory
   count/threshold and penalty, with English and Czech text.
2. `PoliticalProductionTest` checks exact boundaries, all five thresholds,
   combined/clamped output and prospective population. Integration checks
   exercise staffed fields, zero-yield exclusion, unattended bells/crosses,
   horse breeding, specialist then political then press/newspaper/Penn
   percentages, limited and stocked manufacturing input, actual famine and
   Bolivar affiliation through European capture/Crown occupation. The famine
   test observes the actual victim and checks every survivor; it does not
   claim exhaustive random-victim coverage.
3. Final Docker `run-97pdNe` passes **2,210 model tests** and nine basic/
   environment/source-contract checks, zero failures/errors/skips, exit 0.
   Production code remained unchanged through the test repairs. UI diagnostic
   `run-SAOYhq` passed TC50 (3), TC52 (2), TC68 (8), TC75 (3) and TC77_01;
   its two TC67 failures were diagnosed as a Tory-induced construction delay.
   F58 now explicitly uses Discoverer to isolate its religious rule; existing
   expected outcomes were preserved. Final `run-2ymlLP` passes TC67 (2),
   TC77 (2) and source contract (3), zero failures/errors/skips, exit 0.
   Together these runs establish **2,239 distinct passing checks**.
4. TC77_01 shows an exact 50% bonus, input-limited tools, population dilution,
   actual output and Continue. TC77_02 shows Viceroy's six-Tory penalty,
   removal below the threshold, changed tool production and Continue. TC75
   retains support accounting; TC67 retains Czech Penn and upgrade paths.
   New TC77 paths ran in English. Main's formatter checked 24 Java files
   with code-token/idempotence checks, and documentation validation passed.
5. [Worker evidence](ai/local-qwen.md) records the medium/medium/high attempts
   and failed fixture assumptions. This is targeted acceptance, not full-batch
   regression or publication. Status stays ready for development until the
   agreed second-batch boundary checks; no additional CI run is claimed.
6. Full development CI [#174](https://github.com/jajir/microcol/actions/runs/39156232241)
   on `d829993d293fa6cbab138f38a271c7f4b7cd847d` ran all **2,299 model checks**
   successfully and **367 UI/contract checks**, of which 362 passed and five
   failed, with no errors or skips. Failures: TC07 food 165 instead of 170;
   TC08 food 188 instead of 193; TC41 food production 8 instead of 10;
   TC57 retained hammers 264 instead of 266 and 502 instead of 503.
   All four tested colonies have eight residents and zero liberty on
   Conquistador, so R-0101 correctly subtracts one per staffed job. These older
   fixtures implicitly assumed neutral politics. Only their starting liberty
   is changed to 200 (one rebel, seven Tories, 12.5% support); populations,
   inventories, difficulty, production code and every expected result remain
   unchanged. This follows the same neutral-state repair already used by F42.
7. Codex performs this repair directly at the user's request. The diagnostic
   selection includes TC07/TC08/TC41/TC57, TC77, `PoliticalProductionTest`,
   `PoliticalProductionIntegrationTest` and `PageVerificationContractTest`.
   Docker `run-LdfWrZ` passes all **45 checks**: 32 political model checks and
   13 UI/source-contract checks, with zero failures/errors/skips, exit 0.
   All five previously failing checks pass with their original assertions.
   TC77 and the model boundary tests still verify the actual Tory penalty and
   support bonuses. Callback `20261018T193907Z-fc0296fd8642` resumes the correct
   desktop task after 165.903 seconds. Review confirms only four liberty values
   changed in the fixtures; no Java formatting is needed. Full CI/publication
   must still pass on the repaired commit.
8. Repaired source `cffcd9a39f443f55cfe8f653cdb4cca7bf96160c` passes full
   [development #175](https://github.com/jajir/microcol/actions/runs/39160063302):
   **2,299 model and 367 UI/contract checks**, zero failures/errors/skips.
   The downloaded XML reports also verify 32 build-tool checks and 2,332 checks
   on each of macOS ARM64, macOS Intel and Linux. Java compatibility and Pages
   pass on the same commit. The automatic channel manifest matches immutable
   [dev-175-cffcd9a39f44](https://github.com/jajir/microcol/releases/tag/dev-175-cffcd9a39f44);
   the downloaded macOS launcher matches its published SHA-256. This closes the
   fixture-repair publication blocker without changing the earlier recorded
   second-batch requirement boundaries.

## R-0102 tax demand and boycott — 2026-10-18

1. R-0102 introduces national tax cadence, saved human decisions and commodity
   boycotts. Model acceptance covers all difficulty thresholds, initial zero,
   cap, stock-value/tie selection, bounded destruction, liberty without Congress
   credit, arrears, stale choices, save/load, immigration ordering, AI acceptance
   and Fugger's one-time clearing. Large full-stock values use long arithmetic;
   an unrepresentable winning arrears quote offers no protest instead of choosing
   a different commodity. Ordinary prices retain the agreed outcome.
2. Docker `run-czrcJC` passes 2,248 model and 43 selected UI/basic/contract
   checks. Final `run-oDcbjl` passes **2,250 model and 18 UI/basic/contract
   checks**, zero failures/errors/skips. Final TC58 (6) and TC78 (3) plus
   unchanged TC27 (10), TC55 (3), TC68 (8), TC70 (4), SimpleOperations (5),
   Environment (1) and source contract (3) establish **2,293 distinct passing
   checks**. Full second-batch regression and publication remain pending.
3. TC78 verifies exact demand consequences, defer/Escape, reopening, blocked
   actions, Continue, acceptance, protest and stale-stock rejection. TC27
   exercises a real tools protest and equipment restrictions. TC58's real
   protest leaves 200 ore above the 100 export threshold while tobacco sells
   for the exact 231 gold; Continue repeats neither export nor destruction.
   Its revolution case retains half-net export despite a seeded boycott.
   TC55 tests missing-tools import versus stocked tools; TC70 joins earned
   immigration to the tax pause before production. TC68's education journeys
   remain passing. The English demand capture is readable; no separate Czech
   visual capture or full visual regression was run.
4. Main updated 72 current-schema fixture files, preserving nine controlled
   positive rates, and applied the shared formatter with code-token/idempotence
   checks. Initial diagnostic failures, caller path error and three worker
   attempts are recorded in [operating evidence](ai/local-qwen.md). Payment
   remains R-0103; R-0102 stays ready until the agreed batch acceptance boundary.

## R-0103 back-tax payment — 2026-10-18

1. R-0103 adds a quote-bound payment for one commodity's frozen arrears.
   Player validates active actor, European access, amount, demand generation
   and treasury before removing that debt and emitting the gold change.
   A synchronous listener sees the completed result and cannot pay it twice.
2. Docker `run-IdGG0D` passes **2,261 model checks** and 24 of 25 selected
   UI/basic/contract checks; the remaining TC79_03 stale port mark was fixed
   in the dialog's rejection refresh. Final `run-J1dNdH` passes TC79 (3) and
   source contract (3), zero failures/errors/skips. Model code did not change
   after its passing run. TC27 (10), TC78 (3), SimpleOperations (5),
   Environment (1), contract (3) and TC79 (3) together establish **2,286
   distinct passing checks**. Full second-batch acceptance remains pending.
3. TC79 creates a real musket protest, cancels payment without charge, pays
   exactly 400 gold, buys and sells two muskets through the existing cargo
   dialogs and preserves the remaining silk boycott and accounting across
   Continue. Separate paths verify one-gold-short rejection and a stale open
   payment after another command settles the debt. Model tests include
   duplicate/reentrant confirmation, wrong actor, pause, independence, saved
   state, changed prices and exact buy/sell accounting. UI paths ran in English;
   Czech strings were reviewed in source, without a separate visual capture.
4. Haiku medium/medium/high completed the work with diagnosed UI repairs;
   [operating evidence](ai/local-qwen.md) records failures and measured time.
   Main formatted the 12 changed Java files, reviewed the diff and verified
   docs. No main implementation takeover or Sonnet/Opus escalation occurred.

## R-0085 real-boycott completion — 2026-10-18

1. TC58_06 now creates the boycott through the actual tax protest: 300 ore
   loses 100, the ordinary custom house preserves the remaining 200, and
   tobacco export leaves exactly 1,231 gold. The test saves that game through
   the normal action and changes only the independence flag in a controlled
   copy. This isolates the export rule; it does not verify R-0105's declaration
   or muster. No boycott or stock is invented after the protest.
2. On the next update, 100 ore earns half its 300 ordinary net (150 gold),
   and three tobacco earn half their six ordinary net (three gold). Exact
   report text, stocks, 1,384 gold and unchanged 800 ore arrears are checked.
   Continue repeats neither export nor payment. Unselected silver stays 150.
3. Docker `run-kp4OXZ` passes **2,261 model and 15 UI/basic/contract checks**,
   zero failures/errors/skips. TC58 contains six scenarios; basic/environment/
   contract contribute nine. Haiku medium completed the test-only change in
   one attempt; main formatter checked two Java files with token/idempotence
   pass. Original [R-0085 evidence](#r-0085-custom-house-export-2026-10-16) and
   [R-0102 evidence](#r-0102-tax-demand-and-boycott-2026-10-18) retain earlier
   acceptance. Full second-batch regression/publication remains pending.

## R-0123 terrain identities and lakes — 2026-10-18

1. Plains, marsh and lake have distinct serialized identities; tundra permits
   forest. Shared terrain defense covers the new bare/forested cells. All
   fourteen types have English/Czech names and non-null existing artwork;
   lake artwork uses the ocean tile without becoming an ocean identity or
   Europe sea lane. Dedicated new terrain artwork is not part of this change.
2. Model tests exercise strict unknown-code/illegal-forest loading, round trips,
   occupied lake movement, land rejection, dock-gated fishing and actual stored
   food after production. The same saved combat draw loses against forest and
   wins after clearing plains/marsh/tundra, using the shared preview/resolver.
   Docker `run-PinZB3` passes all **2,300 model checks**, zero failures/errors/skips.
3. TC80/F72 uses a scoped F42 copy to verify visible names and forest state,
   Save/Continue, actual lake ship movement, land rejection and fishing with
   a completed dock. Final `run-KPhjW4` passes all three TC80 scenarios and three source-contract checks, zero failures/errors/skips.
   Existing TC35 (12), TC38 (5), TC73 (6), basic/environment (6) and contract
   (3) pass in `run-PinZB3`. TC51 (2) passes in `run-3A9YzG` after F42 starts
   with 200 liberty: one rebel among eight residents avoids the R-0101 Tory
   penalty without awarding a production bonus. Expected construction and
   fishing output is unchanged. Together the selected runs establish **2,337
   distinct passing checks** on the final implementation.
4. Initial fixture and UI failures, all three Sonnet medium attempts, and
   explicit main-owned test repairs are recorded in the
   [operating evidence](ai/local-qwen.md). Full second-batch regression and
   publication remain pending; R-0124 production and R-0125 movement matrices
   are separate requirements. No full visual suite was run.

## R-0243 completion runner — 2026-10-18

1. Host-side completion delivery changes no gameplay, fixture, selector or test
   assertion. The existing five Docker runner checks pass. Real Docker run
   `run-P5uABi` through the completion runner passes **10 checks**: 3
   `PageVerificationContractTest` and 7 `TC_18_F01_plow_field_test`, with zero
   failures, errors or skips. Maven exit is 0; the exact desktop probe task
   acknowledges the success callback.
2. Intentional timeout probes verify cleanup and notification, not gameplay
   acceptance. Their evidence and the diagnosed automatic-removal race are in
   [the operating record](ai/local-qwen.md#completion-callbacks-2026-10-18).
   The full Docker suite, native packaging and publication gates remain required
   in CI; the targeted local run does not replace them.

## R-0105 independence and military roles — 2026-10-18

1. `Player` validates exact national support of at least 50%, the pre-1800
   deadline and an owned coastal colony, including revalidation after veto
   callbacks. Mobilization uses exact local support and stable unit IDs once.
   Continental/royal roles retain identity, expertise and equipment, persist
   across Continue, use the agreed 4/5 and 5/6 bases, and are destroyed on defeat.
   Full REF deployment, intervention and R-0178 situational bonuses remain open.
2. Docker `run-0D4uNt` passes **2,349 model tests and 52 checks in microcol-test**,
   zero failures, errors or skips. The latter are 43 named player scenarios,
   six basic/environment checks and three source-contract checks. Selection:
   `*Test,TC_81*,TC_82*,TC_03*,TC_33*,TC_35*,TC_76*,TC_77*,TC_58*`.
   TC81/F73 covers 49/50%, the date boundary, stale confirmation, cancel/Escape,
   unequal-colony muster and Continue. TC03 preserves actual Crown landings.
   TC82/F57 verifies role strength, the loss warning, actual destruction and
   persistence. Capture-affiliation tests exercise one owned coastal colony,
   the conqueror's Bolivar bonus, eligibility and exact muster.
3. The first repaired model run, `run-1xHpkM`, had one error among 2,337 tests:
   its full warehouse correctly rejected returning a soldier's muskets. The
   final fixture provides room; separate field/building tests preserve full
   warehouse rejection without mutation and verify loadable state in callbacks.
   Earlier UI diagnostic `run-HCNtv9` passed 45 checks; it did not establish
   model acceptance. The final run above actually reruns the failed paths.
4. Shared formatting checked 36 Java files, changed six, with identical code
   tokens and an idempotent result. Main then clarified the English/Czech
   battle warning for an ordinary loser opposing a special-role unit. That
   text change is included in the pending full second-batch regression;
   the targeted result predates that wording correction. Batch acceptance,
   integration and publication are still pending. Worker attempts and budget
   evidence are recorded in [operations](ai/local-qwen.md).

5. Full batch run `run-u9GsTq` reaches the 2,700-second watchdog; cleanup
   succeeds, exit 130. Its Maven log records 272 completed microcol-test checks
   with three failures and no errors/skips. Forced termination did not export
   Surefire XML, so this is partial log evidence, not a successful full run.
   TC07, TC08 and TC41 expose old fixtures with eight Delft residents and zero
   liberty: CONQUISTADOR's eight-Tory threshold now reduces each eligible job.
   Main sets Delft liberty to 200 in F07/F08/F33: one rebel leaves seven Tories,
   below the penalty threshold and below the 50% bonus. Production, food, birth
   and import assertions are unchanged. The other 29 incomplete/failed suites
   are scheduled in full with the source contract; acceptance is still pending.

## Claude second-batch acceptance — 2026-10-18

1. R-0070, R-0089, R-0096, R-0100–0103, R-0085, R-0105 and R-0123
   complete the second ten selected requirements. Their earlier targeted
   evidence remains above. Acceptance combines completed suites from
   `run-u9GsTq`, the 108-check remainder `run-bakaPf`, and `run-QhdhWp`:
   **2,348 default model tests and 372 distinct microcol-test checks pass**,
   zero remaining failures, errors or skips. This is a reconciled regression,
   not a claim that the initial timed-out invocation succeeded. The targeted
   run also passed `ConnectorTest`, normally excluded by the default Maven
   integration-test filter, explaining its 2,349-model-test count.
2. The remainder identifies two TC57 failures (266/264 and 503/502 hammers).
   Main reuses the exact F48 political fixture correction already committed
   in `cffcd9a3`; TC57 and the source contract rerun in `run-QhdhWp` pass all
   six checks. F07/F08/F33 repairs pass in `run-bakaPf`. All 85 runnable UI
   suite classes have completed evidence; repeated classes count only once.
   Assertions and intended production/birth/construction outcomes are preserved.
3. R-0105 implementation is committed as `36073d96`. Integration of current
   main brings documentation and the identical four fixture corrections only;
   no tested gameplay source changes. Requirement status now records local
   acceptance. GitHub Actions and development publication of this final batch
   are pending and will be verified against the pushed commit.

## R-0124 terrain production — 2026-10-19

1. [R-0124](requirements/done/R-0124.md) implements all 22 legal bare/forested
   terrain variants and 176 literal commodity cells from aid card 4. The main
   agent visually rechecked the reference and corrected the old audit's omitted
   swamp-fur zero. Terrain owns the current row; field choices, previews and
   saved-job validation use the same model operation. Water retains CORN=3,
   including lakes; plowing and political modifiers remain separate.
2. `TerrainProductionMatrixTest` verifies every cell, forbidden forest variants,
   zero-output choices, forest removal and actual field production. TC86 verifies
   the player preview/cancel/assignment, the next turn's exact warehouse change,
   and Save/Continue without extra stock. Its grassland-forest baseline is 6
   lumber; Delft's existing political penalty makes the displayed and stored
   output 5. TC40 preserves unavailable-current-crop rejection, exact field
   choice and stale-state protection. A separate model case proves that a
   strictly better neighbor-worked field is excluded (6 versus eligible 4).
3. Haiku medium produced the table but left dependent fixtures incomplete.
   Sonnet medium repaired them in two bounded attempts. Failed diagnostics
   `run-D7DZwh` (3 failures/23 errors), `run-yho4et` (15 failures/1 error), and
   `run-l1Cr0G` (5 UI failures/errors) are superseded only for the rerun checks.
   Original player outcomes and rejection assertions remain; bare lumber jobs
   now have legal forests, and expected effective yields account for modifiers.
4. Final Docker/Xvfb/Openbox `run-u3rX4Y` passes **2,382 model tests and 34
   microcol-test checks**, zero failures/errors/skips: TC40 (15), TC86 (1),
   TC34 (9), SimpleOperationsTest (5), EnvironmentTest (1) and the page source
   contract (3). Unchanged TC18 (7), TC73 (6), TC80 (3), TC50 (3), and TC07 (2)
   passed in `run-l1Cr0G`. Together these prove 55 distinct selected UI checks.
   The known unfinished `RivalTradeCycleTest` for R-0091 was explicitly excluded;
   this is targeted R-0124 acceptance, **not full batch regression**.
5. Main-agent formatting checked 29 changed Java files (including unchanged
   pending R-0091 files): one changed, exact code tokens and idempotence passed.
   Static audit of 71 bundled/test scenario files found all 318 occupied field
   jobs valid under the new baseline. That audit does not establish every
   runtime-generated fixture or all gameplay. Full batch regression and GitHub
   publication remain pending; no new published build is claimed.


## R-0125 terrain movement — 2026-10-19

1. Base terrain movement is accepted on the isolated batch branch. Integer thirds are authoritative in the model and saved unit state; execution, weighted route/range and UI affordability share costs. Ships retain one point for water/port steps. The full-allowance expensive step ends at zero. Road/river connectivity is still owned by R-0121/R-0126.
2. Docker completion `20261019T073410Z-ef358fc97804` passed **2,397 model tests** with zero failures/errors/skips. The unfinished R-0091 `RivalTradeCycleTest` was explicitly excluded. Selected TC01/11/18/23/28/29/38/50/87 and default supporting checks ran: 41 of 42 passed; TC87 first failed on a detached preview canvas. This is not full batch regression.
3. Main-agent repair, explicitly selected by the user, cancels the preview with Escape before releasing the mouse. Completion `20261019T074343Z-32a398bf4ade` ran TC26/28/35/74/76/87 plus the page contract: 50 of 51 passed, with only TC87 reading its stale pre-move panel. Its later diagnostic runs exposed incorrect status/dialog expectations for zero movement. The final test reacquires the current panel, verifies zero movement cannot enter move mode, retains rejection-dialog checks for a positive insufficient allowance, and verifies unchanged state and Save/Continue.
4. Final completion `20261019T075420Z-b1448cf4f8da` passes **all three TC87 scenarios and all three page-contract checks**, zero failures/errors/skips. Production code is unchanged since the passing model run; other selected player paths are unchanged since their passing checks. Tests cover the full-allowance mountain step, weighted reach/preview, zero/partial rejection and fresh-model fractional/zero persistence through Continue.
5. Two Haiku medium attempts, a quota-interrupted Sonnet medium attempt and one user-authorized Haiku continuation produced the change. Initial schema-fixture and test-lifecycle failures were repaired and rerun. Main inspected and applied the worker's one-time conversion of 72 tracked files / 834 allowances; one deliberately malformed load-rejection fixture was preserved. Shared formatting checks all 53 changed Java files with exact code-token and idempotence checks passing. No full UI suite or publication is claimed here; ten-requirement batch integration remains pending.


## R-0121 road construction and travel — 2026-10-19

1. [R-0121](requirements/done/R-0121.md) is accepted on the isolated batch branch. Roads persist on non-sea squares; ordinary/hardy pioneers finish after two/one owner updates and spend 20 tools once. Cancel and stale work have no road/tool effect. UI offers build/cancel, localized remaining work and visible roads. Connected land-road endpoints cost one third in execution, path, range and preview; fractional movement remains selectable. Road production bonuses remain R-0122.
2. Docker completion `20261019T091935Z-f6e579f233ea` passed **2,440 model checks**, zero failures/errors/skips, with unrelated unfinished R-0091 `RivalTradeCycleTest` excluded. Its 36 UI/environment/source-contract checks passed 35; TC88's final rendering setup moved the pioneer into a ship at the colony and then incorrectly expected a map location.
3. The user-authorized main-agent repair changes only that TC88 destination to empty grassland `(24,12)`, preserving the location and rendered-road assertions. Completion `20261019T092652Z-967e9ee22106` passed **8 checks**: four TC88 construction scenarios, one TC89 road-travel scenario and three page-contract checks, zero failures/errors/skips. Together with unchanged passing TC11/15/18/73/87 and environment checks in the earlier run, all **36 distinct selected checks** pass. This is targeted acceptance, not a full-suite result.
4. TC88 covers ordinary/hardy construction, visible countdown, cancellation, insufficient tools, completion, tools charged once, Continue and rendered road. TC89 covers both road directions, exact remaining thirds, final-third movement availability and Continue/rendering. BuildRoadTest and RoadMovementTest cover terrain/tool boundaries, stale/concurrent/repeated completion, persistence and connected/disconnected costs with route/range behavior. UC-021 road-output and UC-012 river scope remain open under their own requirements.
5. Worker history: Haiku medium preparation, implementation and repair, followed by one user-authorized Sonnet medium repair. Compilation, lifecycle-test and UI-fixture errors were diagnosed from saved evidence; no failed run is reclassified as passing. Sonnet also fixed stale progress feedback after cancel. Main reviewed changes and applied the shared formatter: 46 Java files checked, exact-code-token and idempotence checks pass. No new package or layer dependency was introduced.
6. Batch progress is three accepted requirements (R-0124, R-0125, R-0121). Integration, full batch regression and publication remain pending the agreed ten-requirement checkpoint. These attempts do not establish a measured token-cost optimum; Haiku required escalation for this combined model/UI task.


## R-0126 rivers — 2026-10-19

1. [R-0126](requirements/done/R-0126.md) is accepted on the isolated batch branch for authored maps. River size and reciprocal adjacent segments persist with validation. Connected land travel costs one third, without stacking a road discount; disconnected crossings keep terrain cost. Small/large rivers add +1/+2 only to positive output before job modifiers. Rendering distinguishes size and hides courses into unexplored neighbors. Generation and editor controls are not part of this acceptance.
2. Completion `20261019T094445Z-25795a89db5b` passed **2,457 model checks**, zero failures/errors/skips, excluding unrelated unfinished R-0091 RivalTradeCycleTest. River tests cover positive/zero yields, plow composition, malformed courses, persistence, both travel directions, road overlap, exact thirds and route/range. Its UI run passed 18/20 checks; TC90 exposed a large river stroke cap extending into a small neighbor and an incorrect English commodity-label expectation.
3. Haiku medium repaired the stroke to end at the shared midpoint and restored canvas stroke/fill/width/cap so later grid rendering retains its own state. The Sugar cane expectation preserves the numerical result. A third TC90 scenario checks that the known tile does not draw a course into an unknown neighbor. Completion `20261019T095309Z-4776c9cc0383` passed **15 checks**: TC86/87/88/89/90 and three page-contract checks, with zero failures/errors/skips. Model code is unchanged from the passing model run. The earlier environment checks remain separate; no full-suite claim is made.
4. TC90 verifies small/large rendered courses, connected travel and exact remaining movement through Continue, field preview versus actual next-turn sugar and persistence, and fog suppression. Two Haiku medium attempts completed this slice; no Sonnet escalation or main-agent implementation repair was needed. Main reviewed the shared model/rendering boundaries and applied the Java formatter (28 files, exact-token and idempotence checks pass). No new package responsibility or layer dependency was introduced.
5. Batch progress is four accepted requirements (R-0124, R-0125, R-0121, R-0126). Integration, full batch regression and publication remain pending the ten-requirement checkpoint. Whole UC-008 still includes the separate resource-overlay/depletion requirements. No token-cost optimum is claimed from these two attempts.


## R-0127 resource overlays — 2026-10-19

1. [R-0127](requirements/done/R-0127.md) is accepted on the isolated batch branch: twelve explicit resource kinds, +2 to each matching output (both ore and silver for mixed minerals), persisted identity, known-tile marker and English/Czech inspection names. Land kinds accept any land terrain; fishery accepts ocean/lake. Matching overlays can introduce a zero-baseline output before flat plow/river and later job modifiers; nonmatching output receives no bonus.
2. Completion `20261019T101217Z-9b2389ea8b3d` passed **2,476 model/localization checks and 21 UI/environment/source-contract checks**, zero failures/errors/skips. Selection was `*Test,!RivalTradeCycleTest,TC_91*,TC_90*,TC_86*,TC_88*,TC_89*`; unrelated R-0091 trade tests remain excluded. TC91 checks resource marker/inspection and fog, prime-sugar preview against actual next-turn stock, and Continue without duplicate output. Model tests cover all resource identities, zero/nonmatching boundaries, modifier composition, invalid storage/terrain and persistence.
3. Main reviewed the shared production boundary, load validation and known-only UI integration; applied the shared formatter (28 Java files, token preservation and idempotence pass). One Haiku medium preparation and one implementation attempt completed this slice; no repair was needed after the first actual test run. The preparation inspected local aid-card pages 132–135 and resource descriptions 24–25 without finding an explicit numerical overlay override; base terrain chart cells were not treated as overlay bonuses.
4. Batch progress is five accepted requirements (R-0124, R-0125, R-0121, R-0126, R-0127). Full batch regression, main integration and publication remain pending. Depletion is R-0128, natural generated placement R-0065 and road-yield eligibility R-0122; this acceptance claims none of those. Measured token-cost optimum remains unestablished.


## R-0143 national markets — 2026-10-19

1. [R-0143](requirements/done/R-0143.md) is accepted on the isolated batch branch. Europe owns four national markets with exact pressure, bid/spread and affected-since-update flags. Confirmed sales/purchases settle their quote before applying full own and quarter cross-market pressure; equipment, rush tools and custom-house exports use that engine. Quiet recovery runs only at the national update boundary. R-0144's Dutch half-factor remains separate.
2. Completion `20261019T111007Z-f048a4b027ae` passed **2,508 model checks and 39 UI/environment/source-contract checks**, zero failures/errors/skips. Selection: `*Test,!RivalTradeCycleTest,TC_94*,TC_06*,TC_27*,TC_55*,TC_58*,TC_78*,TC_79*`. Unfinished R-0091 tests remain excluded. Coverage includes fractions/thresholds/bounds, malformed saved pressure including integer extremes, actual rush/export/equipment pressure, cross-market quiet flags, ModelPo round trip and no replay on load, confirmed prices, UI stale offers and related tax/equipment/export journeys.
3. Haiku prepared and implemented the slice; a bounded Sonnet repair corrected scripted non-national actor recovery, integer-bound validation/pressure arithmetic, partial-buy quoting and missing integration coverage. Existing equipment/custom-house expectations were recalculated from the specified trade thresholds rather than copied from failing output. Earlier runs (2,485/2,489 model and 28/33 UI) remain failed evidence. Final selected checks all pass.
4. Main reviewed quote/settlement callers, saved-state validation and isolated Player market hunks from the unfinished rival-trade objective. Shared formatting checked 45 Java files with token-preservation and idempotence passing. Current-schema market state was added directly to bundled maps and scenario fixtures; deliberately corrupt input remains invalid. Full batch regression and publication are not established by this targeted run.
5. Six requirements are accepted in the batch: R-0124, R-0125, R-0121, R-0126, R-0127 and R-0143. Main integration remains the ten-requirement checkpoint. This larger cross-cutting task required Sonnet repair; token-cost optimum is not yet measured.


## R-0144 Dutch market pressure — 2026-10-19

1. [R-0144](requirements/done/R-0144.md) is accepted on the isolated batch branch. Europe's common pressure engine applies the receiver's factor: Dutch own/cross trades contribute 4/1 eighths, other markets 8/2. Thresholds, bounds, spread, confirmed settlement and recovery remain shared. No second market store or new option was added.
2. Completion `20261019T112955Z-563717256c54` passed **34 selected UI/source-contract checks**, zero failures/errors/skips: TC06/27/55/58/78/79/94/95 plus page verification. TC95 exercises actual Dutch purchases, the 100-versus-200-unit threshold, cross-market comparison and Continue. Model tests cover both signs, repeated single units, Dutch 200/800 versus ordinary 100/400 thresholds and saved fractions.
3. The first model run passed 2,510/2,515; five old Dutch expectations in equipment and national-update tests still used the previous factor. Haiku's repair changed only EuropeEquipmentTest and NationalMarketUpdateTest, retaining shifted-price recovery coverage with 200-unit setup and independently calculated remainders. Completion `20261019T113746Z-541fe06c0e99` passed **2,515 model checks and nine environment/source-contract checks**, zero failures/errors/skips; unrelated R-0091 RivalTradeCycleTest remains excluded. Production/UI were unchanged, so the passing UI evidence above remains applicable.
4. Main inspected the receiver-based calculation, repair file scope and exact threshold arithmetic, and applied the shared formatter (25 Java files, token-preservation/idempotence pass). Two Haiku medium attempts completed this slice. Seven requirements are accepted in the batch; full regression, main integration and publication remain pending the ten-requirement checkpoint. This is not a measured token-cost optimum.
