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
s Xvfb a Openbox**, podle dohody v [R-0033](requirements/R-0033.md). Zamčený
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
heapu a konkrétní důkazy zachovává [R-0033](requirements/R-0033.md).

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
   konkrétní kontrakt vlastní [R-0011](requirements/R-0011.md).
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
   zaznamenává [R-0011](requirements/R-0011.md).

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
   Politiku čísel, odlišenou od výslovného textu manuálu, vlastní [R-0020](requirements/R-0020.md).
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

1. Publikované [R-0021](requirements/R-0021.md) mění odjezd až na samostatný
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

1. Lokálně ověřené [R-0022](requirements/R-0022.md) mění hromadné vylodění na
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

1. Rozpracované [R-0023](requirements/R-0023.md) přidává skutečný typ
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
