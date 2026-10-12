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

Použít **JDK 17** a checkout s cestou bez mezer. Maven používá JVM uvedenou
v `mvn --version`; samotné `java -version` nemusí ukazovat stejnou instalaci.
Celá sada potřebuje nativní JavaFX Glass v grafické relaci; na Linuxu bez
monitoru použít Xvfb s obrazovkou alespoň 1600 × 1200 a Openbox ve stejné relaci.

Celá sada včetně závislého herního modulu, z kořene repozitáře:

```sh
mvn -B -ntp -pl microcol-test -am clean test -P ci \
  -Dtestfx.robot=glass -Dtestfx.headless=false -Dprism.order=sw \
  -DsoundEnabled=false
```

V CI běží příkaz uvnitř
`xvfb-run -a --server-args='-screen 0 1600x1200x24'`; nejprve se v téže relaci
spustí Openbox. Přesný příkaz včetně ukončení správce oken je ve
[workflow](../.github/workflows/dev-builds.yml). V místní grafické relaci
po dobu testu neovládat okno hry ručně.

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
ověřuje skutečný běh i u testů napsaných [místním Qwenem](ai/local-qwen.md).


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
