# Odpovědnosti Java balíčků

Tento přehled určuje, kam v MicroCol patří nová změna a které části mají
spolupracovat. Před zadáním práce vybrat vlastníka pravidla a dotčené balíčky
z této mapy; není potřeba pokaždé znovu odvozovat rozdělení celé hry ze zdrojů.
Konkrétní metody a jejich volající je před úpravou stále nutné přečíst.

**Stav a pravidla údržby k 11. říjnu 2026.** Mapa vychází ze zdrojů revize
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

```text
gui: volba člověka ───┐
                     ├──> model: ověření a provedení akce
ai: volba počítače ───┘
```

Existujícím příkladem je pohyb: [GameModelController](../microcol-game/src/main/java/org/microcol/gui/event/model/GameModelController.java)
i [SimpleAiPlayer](../microcol-game/src/main/java/org/microcol/ai/SimpleAiPlayer.java)
volají `Model.moveUnit`. Diagram vyjadřuje hranici pro herní operace;
neznamená, že všechny současné akce už používají totožnou cestu.

## Model a rozhodování počítače

V následujících tabulkách jsou názvy relativní k `org.microcol` v
`microcol-game/src/main/java`. Odkaz otevírá příslušný adresář.

| Balíček | Odpovědnost a orientační třídy | Hranice pro další změny |
| --- | --- | --- |
| [kořen org.microcol](../microcol-game/src/main/java/org/microcol/) | Vstup do aplikace, JavaFX životní cyklus a sestavení závislostí: `MicroCol`, `MicroColApplication`, `ExternalModule`, výměnné vazby pro testy. | Pravidla a konkrétní obrazovky patří do svých balíčků. |
| [model](../microcol-game/src/main/java/org/microcol/model/) | Svět, hráči, jednotky, kolonie, ekonomika, cesty a tahy: `Model`, `Player`, `AbstractUnit`, `Colony`, `GoodsTrade`, `PathFinder`, `GameManager`, `Calendar`. | Autoritativní pravidla a změny stavu; nepřidávat závislosti na obrazovkách, JavaFX ani strategiích AI. |
| [model.unit](../microcol-game/src/main/java/org/microcol/model/unit/) | Konkrétní druhy jednotek, továrna a jednotkové akce, například `UnitFactory`, `Ship`, `UnitActionPlowField`. Sdílené `Unit`, `AbstractUnit` a `UnitType` jsou nadále v `model`. | Schopnosti a provedení akcí; rozhodování soupeře a animace patří jinam. |
| [model.event](../microcol-game/src/main/java/org/microcol/model/event/) | Události změn a přechodů modelu, včetně zrušitelných událostí před akcí; odběratelé používají `ModelListener`. | Popis herního děje bez UI ovládacích prvků. Nejde o uloženou historii zpráv pro hráče. |
| [model.turnevent](../microcol-game/src/main/java/org/microcol/model/turnevent/) | Zprávy aktuálního přehledu tahu o příjezdu lodí, hladu, ztrátě kolonie a dalších výsledcích: `TurnEvent`, `TurnEventStore`. | Záznamy se ukládají s hrou, protože je nelze spolehlivě obnovit z aktuálního světa. Při ukončení tahu se zprávy hráče čistí; nejde o trvalý archiv. Zobrazení vlastní `gui.screen.turnreport`. |
| [model.store](../microcol-game/src/main/java/org/microcol/model/store/) | Datové objekty uloženého světa a Gson převody: `ModelPo`, `UnitPo`, `CargoPo`, adaptéry akcí a typů. | Ukládaná reprezentace; výpočty herních pravidel zůstávají v modelových objektech. Tento balíček sám nezajišťuje celý souborový save/load. |
| [model.campaign](../microcol-game/src/main/java/org/microcol/model/campaign/) | Kampaně, mise, cíle a jejich posluchače; `GameModel` spojuje misi a `Model`, `GameModelDao` sestavuje a obnovuje hru. Obsahuje i volnou hru `FreePlay`. | Specifické podmínky mise; obecná ekonomika, boj a pohyb patří do společného modelu. |
| [model.campaign.po](../microcol-game/src/main/java/org/microcol/model/campaign/po/) | Obálka celé uložené hry: svět, jméno kampaně/mise, data cílů a souborové čtení/zápis přes `GameModelPoDao`. | Formát a převod dat; nespouštět zde herní akce ani dialogy. |
| [model.campaign.store](../microcol-game/src/main/java/org/microcol/model/campaign/store/) | Uložení postupu kampaně, například dokončených misí, přes `CampaignsDao`. | Odlišovat postup kampaně od rozehrané pozice a od uživatelských nastavení. |
| [ai](../microcol-game/src/main/java/org/microcol/ai/) | Rozhodování evropských a královských soupeřů: `SimpleAiPlayer`, `KingRobotPlayer`, chování jednotek a analýza kontinentů. | Volit akce nad společnými pravidly; neobcházet modelové operace a nepřidávat nové závislosti na GUI. Současné vazby na animace jsou výjimkou níže. |
| [i18n](../microcol-game/src/main/java/org/microcol/i18n/) | Překladové klíče, XML resource bundles, jejich načítání, cache a kontrola úplnosti. | Překlad významu; neurčuje herní pravidla, rozložení obrazovek ani okamžik otevření dialogu. |

## Uživatelské rozhraní a podpůrné služby

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
| [gui.screen](../microcol-game/src/main/java/org/microcol/gui/screen/) | Volba obrazovky, životní cyklus a hlavní kontejner. |
| [gui.screen.game](../microcol-game/src/main/java/org/microcol/gui/screen/game/) | Sestavení herní obrazovky a její klávesové vstupy. |
| [gui.screen.game.components](../microcol-game/src/main/java/org/microcol/gui/screen/game/components/) | Pravý panel, tlačítka, stavový řádek a údaje o poli a jednotkách. |
| [gui.screen.game.gamepanel](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/) | Canvas mapy, viditelná oblast, výběr, kurzor, posun, zvýraznění cesty a animace. |
| [gui.screen.colony](../microcol-game/src/main/java/org/microcol/gui/screen/colony/) | Pracovníci, pole, sklad, budovy a přístav na obrazovce kolonie. |
| [gui.screen.colony.buildingqueue](../microcol-game/src/main/java/org/microcol/gui/screen/colony/buildingqueue/) | Dialog a editace stavební fronty; zpracování fronty a ceny zůstávají v modelu. |
| [gui.screen.europe](../microcol-game/src/main/java/org/microcol/gui/screen/europe/) | Evropský přístav, nákup a nábor jednotek, zboží a přehled plavby. |
| [gui.screen.market](../microcol-game/src/main/java/org/microcol/gui/screen/market/) | Obrazovky nákupu a prodeje a prezentace cen. |
| [gui.screen.campaign](../microcol-game/src/main/java/org/microcol/gui/screen/campaign/) | Výběr kampaně a mise. |
| [gui.screen.goals](../microcol-game/src/main/java/org/microcol/gui/screen/goals/) | Zobrazení cílů mise a jejich postupu. |
| [gui.screen.statistics](../microcol-game/src/main/java/org/microcol/gui/screen/statistics/) | Zobrazení herních statistik a grafů. |
| [gui.screen.turnreport](../microcol-game/src/main/java/org/microcol/gui/screen/turnreport/) | Prezentace uložených zpráv `model.turnevent`. |
| [gui.screen.menu](../microcol-game/src/main/java/org/microcol/gui/screen/menu/) | Hlavní menu a navigace. |
| [gui.screen.setting](../microcol-game/src/main/java/org/microcol/gui/screen/setting/) | Ovládání uživatelských nastavení. |
| [gui.screen.colonizopedia](../microcol-game/src/main/java/org/microcol/gui/screen/colonizopedia/) | Herní nápověda. |
| [gui.screen.editor](../microcol-game/src/main/java/org/microcol/gui/screen/editor/) | Nástroje pro editaci mapového podkladu a jeho PO dat. Editor záměrně mění mapu mimo běžné herní tahy; tato cesta není vzorem provádění akcí hráče či AI. |

## Testy a ostatní adresáře

| Místo | Odpovědnost |
| --- | --- |
| [`microcol-game/src/test/java`](../microcol-game/src/test/java/) | Jednotkové a modelové testy u odpovídajících balíčků; nezávislé očekávané výsledky pravidel. |
| [`microcol-test/src/test/java/org/microcol/test`](../microcol-test/src/test/java/org/microcol/test/) | Čitelné hráčské scénáře `TC_...`, jejich příprava a výslovné kontroly výsledků. |
| [`microcol-test/src/test/java/org/microcol/page`](../microcol-test/src/test/java/org/microcol/page/) | Page objects: selektory, kliknutí, souřadnice a čekání na dokončení UI. |
| [`microcol-test/src/main/java/org/microcol/mock`](../microcol-test/src/main/java/org/microcol/mock/) | Náhrady UI služeb potřebné pro testovací prostředí. |
| [`microcol-test/src/test/scenarios`](../microcol-test/src/test/scenarios/) | Výchozí uložené pozice a nastavení scénářů; zachovávat podle [testového kontraktu](functional-tests.md). |
| [`microcol-build-tests/src/test/java/org/microcol/build`](../microcol-build-tests/src/test/java/org/microcol/build/) | Kontroly launcheru, publikace a nativních distribucí. |
| [`microcol-game/src/main/resources`](../microcol-game/src/main/resources/) | Herní a UI prostředky, texty, mapy a obrazové podklady; použití popisují příslušné katalogy. |
| [`docs`](index.md) | Autoritativní projektová dokumentace a zdroj veřejného webu. |

Odpovědnosti Maven modulů, distribucí, launcheru a zachovaného `microcol-site`
udržuje samostatný [přehled modulů](modules.md). Balíky P01–P32 v
[implementačním plánu](implementation-plan.md) jsou členění práce; jedna funkce
může zasáhnout několik Java balíčků. Nevytvářet balíček jen podle čísla úkolu.

## Současné přesahy a jejich údržba

Následující vazby jsou **pozorované ve zdrojích k uvedené revizi**, nikoli
doklad čistého oddělení nebo seznam oprav nutných před každou další funkcí.

| Současný přesah | Jak postupovat při relevantní změně |
| --- | --- |
| [`AbstractRobotPlayer`](../microcol-game/src/main/java/org/microcol/ai/AbstractRobotPlayer.java) a `SimpleAiPlayer` používají `AnimationLock`/`AnimationManager` z GUI. | Při práci na AI nebo řízení tahu oddělit rozhodování a herní dokončení od čekání na obraz; zachovat pořadí akcí a ověřit oba režimy. |
| [`GameOverProcessors`](../microcol-game/src/main/java/org/microcol/model/campaign/GameOverProcessors.java) a mise používají GUI enum `Dialog`. | Význam výsledku má určit model/mise, výběr textu a dialogu UI. Nevytvářet další přímé závislosti misí na obrazovkách. |
| Model používá `gui.MicroColException`, persistence kampaně `gui.util.PersistingTool` a `i18n` používá `gui.util.StreamReader`. | Jde o historické umístění společných pomůcek. Přesun řešit s jejich skutečnými volajícími, nepovažovat tyto importy za vzor pro nové vazby. |
| [`Default_0_mission`](../microcol-game/src/main/java/org/microcol/model/campaign/Default_0_mission.java) používá analýzu kontinentů z `ai`. | Společná analýza mapy nemá vyžadovat strategii robota. Při jejím rozšíření určit společného vlastníka podle použití. |
| Založení/spuštění/uložení aplikace je rozdělené mezi `gui.event.model`, `gui.dialog.ApplicationController` a `PersistingService`. | Při změně projít celý tok; název `dialog` dnes nezaručuje pouze zobrazování oken. |
| Podmínky orání doplňuje také [`ButtonsGamePanelController`](../microcol-game/src/main/java/org/microcol/gui/screen/game/components/ButtonsGamePanelController.java); AI při nalodění používá jiný vstup než člověk v `GameModelController`. | Při změně těchto akcí sjednotit autoritativní kontrolu a provedení v modelu; ověřit tlačítko, klávesu a AI podle dotčeného rozsahu. Zápis není nově provedená runtime reprodukce. |

Nový kód má respektovat směr `gui → model` a `ai → model`. Stávající přesahy
neopravovat plošně v nesouvisejícím úkolu ani je nerozšiřovat. Hranice nyní
kontroluje review; samostatný automatický test architektonických závislostí
zatím není zavedený.

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
