# Funkční testy a ochrana chování při změnách vzhledu

Funkční scénáře v `microcol-test` jsou společnou kontrolou toho, co hráč dokáže
ve hře udělat a jaký herní výsledek dostane. Každá změna chování musí mít
odpovídající pokrytí v těchto scénářích. Při úpravě vzhledu musí stejné kroky
nad stejným výchozím stavem vést ke stejnému hernímu výsledku.

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
   was introduced. [R-0076](requirements/R-0076.md) retains acceptance status.

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
