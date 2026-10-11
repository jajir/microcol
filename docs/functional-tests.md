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
| [`org.microcol.test`](../microcol-test/src/test/java/org/microcol/test/) | Čitelný příběh hráče: příprava, kroky a konkrétní očekávání. |
| [`org.microcol.page`](../microcol-test/src/test/java/org/microcol/page/) | Vyhledání prvků, kliknutí, přetažení, klávesy a čekání na dokončení UI. |
| [`src/test/scenarios`](../microcol-test/src/test/scenarios/) | Reprodukovatelné uložené hry a testové nastavení. |
| [`AbstractMicroColTest`](../microcol-test/src/test/java/org/microcol/test/AbstractMicroColTest.java) | Společné spuštění aplikace a přístup k testovému kontextu a modelu. |

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
monitoru použít Xvfb s obrazovkou alespoň 1600 × 1200.

Celá sada včetně závislého herního modulu, z kořene repozitáře:

```sh
mvn -B -ntp -pl microcol-test -am clean test -P ci \
  -Dtestfx.robot=glass -Dtestfx.headless=false -Dprism.order=sw \
  -DsoundEnabled=false
```

V CI se před tento příkaz přidává
`xvfb-run -a --server-args='-screen 0 1600x1200x24'`. V místní grafické relaci
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
v samostatném jobu `functional` na Linuxu/JDK 17 s Xvfb při každém pushi na `main`.
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
