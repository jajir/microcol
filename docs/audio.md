# Zvuky a zvuková odezva v MicroCol

Zvuk má hráči pomáhat poznat, co se právě stalo: potvrdit důležitý úspěch, upozornit na problém nebo doplnit atmosféru hry. MicroCol zatím přehrává **jednu hudební skladbu na pozadí**. Od R-0228 je v kódu zapojen **jediný efekt**, krátký [zvuk vítězství v misi](#mission-success-01-zvuk-vitezstvi-v-misi). Obecná správa více zvuků a další efekty implementované nejsou.

Tato stránka je společným místem pro současnou implementaci, základní pravidla a budoucí katalog zvuků. Grafická část má vlastní [pravidla a technický popis](graphics.md). Stav k **11. říjnu 2026**, větev `develop`, revize `aa1161ea7b3a7ea604a8031065c579557b3e8f92`. Popis současného chování vychází z kódu a metadat souboru; neznamená ověřený poslech na všech platformách.

## Základní pravidla pro další práci

Toto je výchozí pracovní zadání pro nové zvuky. Podrobný hudební styl, nástroje a historická věrnost zatím nejsou stanovené a není potřeba je řešit před každou funkční změnou.

- **Zvuk musí mít srozumitelný účel.** U každého efektu pojmenovat událost a informaci, kterou sděluje. Úspěch, varování a neúspěch mají být rozlišitelné. Není potřeba ozvučit každé kliknutí nebo každý krok jednotky.
- **Důležitá informace musí být dostupná i bez zvuku.** Efekt doplňuje text, stav obrazovky, ikonu či animaci. Například při získání odměny má hráč vidět, co a kolik získal; krátký příjemný zvuk její přijetí podpoří.
- **Výsledek má působit příjemně a přiměřeně.** Běžné potvrzení má být krátké a nenápadné, významný úspěch může mít výraznější motiv. Zvuky nemají rušit při dlouhé hře ani přehlušovat důležitější upozornění. Konkrétní estetické řešení lze zvolit vlastním úsudkem v rámci tohoto zadání.
- **Přehrávat skutečný výsledek ve správný okamžik.** Úspěch oznamovat až po provedení akce. Samotné stisknutí tlačítka ani obecný konec mise neprokazují úspěch. U animovaných dějů určit, zda efekt patří k zahájení, zásahu nebo dokončení.
- **Hlídání opakování je součást návrhu.** Jedna událost nemá znít několikrát kvůli překreslení obrazovky či otevření stejného reportu. U častých událostí určit slučování, minimální odstup a případné varianty; u jednorázového úspěchu obvykle stačí jeden zvuk.
- **Zvuk respektuje hráčův pohled a nastavení.** Neprozrazovat dění, které hráč nevidí nebo o něm zatím nemá vědět. Nové efekty musejí respektovat vypnutí zvuku; oddělená hlasitost hudby a efektů je cíl budoucího rozšíření, nikoli současná funkce.
- **Chyba zvuku nesmí zablokovat herní akci.** Při rozšíření zajistit dokončení herní akce i bez dostupného audiozařízení či konkrétního efektu. Současný přehrávač toto ještě plně nezajišťuje.

## Co je nyní uloženo v projektu

V prohlédnutých zdrojích byl 11. října 2026 jediný audio soubor: [AnnounceMyName_1.wav](../microcol-game/src/main/resources/music/AnnounceMyName_1.wav). Není zde zdrojový projekt pro střih této skladby. Efekt R-0228 má vlastní editovatelný zdroj a export popsaný [v katalogu](#mission-success-01-zvuk-vitezstvi-v-misi).

| Vlastnost | Současná hodnota |
| --- | --- |
| Umístění ve zdrojích | `microcol-game/src/main/resources/music/AnnounceMyName_1.wav` |
| Název pro načtení za běhu | `music/AnnounceMyName_1.wav` |
| Úloha | Hudba na pozadí od zobrazení hlavního okna aplikace. |
| Kontejner a kódování | WAV, RIFF little endian, nekomprimované PCM. |
| Vzorkovací frekvence | 44 100 Hz. |
| Hloubka a kanály | 16 bitů, stereo, 2 kanály. |
| Počet vzorkovacích rámců | 7 276 520; každý rámec obsahuje vzorek pro oba kanály. |
| Délka | Přibližně 165 sekund, tedy 2 minuty 45 sekund. |
| Velikost souboru | 29 106 124 bajtů. |
| Opakování a varianty | Jedno přehrání do konce; žádná smyčka, playlist ani varianty. |
| Autor a zdroj | U souboru není samostatně evidovaný původ ani licence skladby; doplnit do katalogu při dohledání. |

Tyto hodnoty jsou naměřené z existujícího souboru, nejsou specifikací všech budoucích zvuků. Současný kód deleguje rozpoznání dat na Java Sound; neobsahuje vlastní dekodér ani konverzi formátů. **Podporu MP3, OGG či jiného komprimovaného formátu proto nelze odvozovat od názvu metody `playSound`.** Takové rozšíření vyžaduje samostatné ověření přehrávače a distribuce.

### Cesta ze souboru do hry

**WAV v resources → Maven jej přibalí → `StreamReader` otevře resource → `AudioSystem` načte formát → `SourceDataLine` přehraje data.**

[Konfigurace resources](../microcol-game/pom.xml) kopíruje obsah `src/main/resources` bez textového filtrování; zvlášť filtruje pouze `version.properties`. Pro audio není konverzní ani exportní krok. Do hry se dostává již hotový WAV. Na rozdíl od obrázků se nevyřezávají části atlasu a neexistují aliasy jednotlivých zvuků.

[StreamReader](../microcol-game/src/main/java/org/microcol/gui/util/StreamReader.java) používá `Module.getResourceAsStream`. Cesta je relativní ke kořeni resources, bez `src/main/resources` a bez absolutní cesty na disku. Přejmenování současné skladby proto vyžaduje změnit i řetězec v `MusicController`.

Zvuk používá **Java Sound z `javax.sound.sampled`**, nikoli JavaFX `MediaPlayer` nebo `AudioClip`. Modulové deskriptory v [microcol-module](../microcol-module/src/main/java/module-info.java) a [microcol-dist](../microcol-dist/src/main/java/module-info.java) deklarují `java.desktop`. Přibalení souboru podle konfigurace samo o sobě neověřuje funkční zvuk ve výsledném instalačním balíčku.

## Jak nyní funguje přehrávání

### Spuštění a přehrání skladby

1. [MicroColApplication](../microcol-game/src/main/java/org/microcol/MicroColApplication.java) nastaví `primaryStage.setOnShowing` na volání `ApplicationController.startMusic()` a zobrazí hlavní okno. Hudba tedy začíná při otevření aplikace, už v nabídce, nikoli až se začátkem konkrétní partie.
2. [ApplicationController](../microcol-game/src/main/java/org/microcol/gui/dialog/ApplicationController.java) vezme uloženou hlasitost z `GamePreferences` a předá ji do `MusicController.start()`.
3. [MusicController](../microcol-game/src/main/java/org/microcol/gui/MusicController.java) ověří `isSoundEnabled()` a spustí nové vlákno, které zavolá `MusicPlayer.playSound("music/AnnounceMyName_1.wav", defaultVolume)`. Výběr skladby je pevně zapsaný v kódu.
4. [MusicPlayer](../microcol-game/src/main/java/org/microcol/gui/MusicPlayer.java) otevře `AudioInputStream`, zjistí jeho formát, získá a otevře odpovídající `SourceDataLine`, nastaví hlasitost a spustí ji. Data čte po blocích s bufferem **128 000 bajtů** a zapisuje je do zvukového výstupu. Nenačítá předem celou skladbu do paměťové cache.
5. Po konci souboru zavolá `drain()` a `close()` na výstupní lince. Další skladba ani opakování nenásledují. Změna obrazovky, konec tahu nebo výsledek mise nejsou v této cestě zvukové spouštěče.

`MusicPlayer` a `MusicController` jsou sdílené instance vytvořené přes Guice. [MicroColModule](../microcol-game/src/main/java/org/microcol/gui/MicroColModule.java) registruje instance s anotací `@Listener` do společného Guava `EventBus`; na události reagují metody označené `@Subscribe`. Současný `MusicController` takto poslouchá pouze změnu hlasitosti.

### Hlasitost a vypnutí zvuku

| Nastavení | Současná implementace |
| --- | --- |
| Ovládací prvek | Jeden posuvník v nastavení; [SettingVolumeView](../microcol-game/src/main/java/org/microcol/gui/screen/setting/SettingVolumeView.java). |
| Rozsah | 0 až 100; výchozí hodnota 75. |
| Změna za běhu | [SettingVolumePresenter](../microcol-game/src/main/java/org/microcol/gui/screen/setting/SettingVolumePresenter.java) propojuje posuvník s `PreferencesProperties.volumeProperty`. |
| Uložení | [PreferencesProperties](../microcol-game/src/main/java/org/microcol/gui/preferences/PreferencesProperties.java) při změně uloží hodnotu přes `GamePreferences` a vyšle `VolumeChangeEvent`. |
| Soubor nastavení | Pole `volume` v `${user.home}/.microcol/.setting.json`; [SettingService](../microcol-game/src/main/java/org/microcol/gui/preferences/SettingService.java), [SettingDao](../microcol-game/src/main/java/org/microcol/gui/preferences/SettingDao.java) a [PersistingTool](../microcol-game/src/main/java/org/microcol/model/campaign/po/PersistingTool.java). |
| Zpracování události | `MusicController` předá hodnotu do `MusicPlayer.setVolume()`, pokud je zvuk povolený. |
| Vypnutí při spuštění | JVM vlastnost `soundEnabled=false`, například argument `-DsoundEnabled=false` předaný přímo JVM hry. |
| Výchozí povolení | `GamePreferences.isSoundEnabled()` používá výchozí hodnotu **`true`**. Komentář o výchozím `false` je zastaralý. |

Ukládání probíhá přes JSON, ačkoli starší komentář v [GamePreferences](../microcol-game/src/main/java/org/microcol/gui/preferences/GamePreferences.java) ještě odkazuje na `java.util.prefs`. Při přidání dalších nastavení navázat na skutečnou cestu přes `Setting`, `SettingService` a `SettingDao`.

Hodnota 0–100 se lineárně mapuje mezi minimum a maximum ovladače `MASTER_GAIN` konkrétního výstupu. **Není to přímé procento amplitudy.** Nula pouze nastavuje minimální gain; není implementovaná zvláštní funkce mute. Neexistují samostatné posuvníky hudby a efektů ani uložený přepínač jejich vypnutí.

`soundEnabled` je systémová vlastnost JVM, ne pole v uživatelském JSON. Při spuštění přes nástroj vytvářející další JVM je nutné ověřit předání vlastnosti právě procesu hry; samotný parametr Maven příkazu to nemusí zajistit.

### Ukončení a omezení důležitá pro rozšíření

[QuitGameListener](../microcol-game/src/main/java/org/microcol/gui/event/QuitGameListener.java) při `QuitGameEvent` zavolá `MusicController.stop()` a poté `System.exit(0)`. `MusicPlayer.stop()` pouze nastaví příznak `run=false`; nečeká na konec vlákna. V běžné přehrávací smyčce by následovalo dohrání bufferu přes `drain()` a zavření linky.

Před použitím pro efekty je potřeba počítat s těmito vlastnostmi současného kódu:

- Přehrávač má jedinou sdílenou `sourceLine` a příznak `run`. Nemá řízení souběžných hlasů, frontu, priority ani ochranu proti opakovanému `start()`. Spustit pro každý efekt další `MusicController.start()` by znovu spouštělo tutéž skladbu a sdílelo stav.
- Po `stop()` se `run` nevrací na `true`; příznak také nemá výslovnou synchronizaci mezi vlákny. Restart přehrávače tedy nelze považovat za hotovou funkci.
- `setVolume()` předpokládá existující použitelnou linku a podporu `MASTER_GAIN`. Kód neřeší změnu před otevřením či po zavření linky ani zařízení bez tohoto ovladače.
- Chybějící resource, nepodporovaný soubor nebo chyba otevření výstupu vedou k `MicroColException`. Chyba čtení dat se jen vypíše; chybí řízený přechod do tichého režimu. Vstupní audio stream není explicitně uzavřen a úklid není zajištěn blokem `finally` pro všechny chyby.

To jsou omezení odvozená ze zdrojového kódu, nikoli závěr, že se všechna projevila při hraní. Tato dokumentace přehrávač nemění.

## Kde hra nyní oznamuje události

Hra už má vizuální odezvu a modelové události, na které může další zvuková práce navázat. **Kromě vítězného konce mise žádná z následujících herních situací nepřehrává vlastní efekt.**

| Situace | Současná vizuální odezva a místo v kódu | Co rozlišit při budoucím ozvučení |
| --- | --- | --- |
| Zpráva scénáře | [MissionEventListener](../microcol-game/src/main/java/org/microcol/gui/event/model/MissionEventListener.java) otevírá lokalizovaný `DialogMessage` pro `EventShowMessages` a `DialogWithMan` pro `EventShowDialogWithMan`. | Typ dialogu sám neříká, zda jde o úspěch, varování nebo jinou zprávu. |
| Konec mise | Tentýž listener při `EventFinishMission` zobrazí zprávy a přepne na obrazovku `CAMPAIGN`. Vítězný konec doprovodí [zvuk vítězství](#mission-success-01-zvuk-vitezstvi-v-misi). | Proherní či neutrální konec zvuk nemá; viz níže. |
| Splněný dílčí cíl | Například [Default_1_mission](../microcol-game/src/main/java/org/microcol/model/campaign/Default_1_mission.java) mění stav cíle a posílá zprávu. [GoalsPanel](../microcol-game/src/main/java/org/microcol/gui/screen/goals/GoalsPanel.java) při otevření ukazuje různé pečetě podle splnění. | [MissionGoal.setFinished](../microcol-game/src/main/java/org/microcol/model/campaign/MissionGoal.java) pouze změní boolean; neexistuje zde univerzální událost dokončení cíle. Překreslení pečeti není nové splnění. |
| Začátek tahu a jeho přehled | [ShowTurnEvensOnTurnStartedEvent](../microcol-game/src/main/java/org/microcol/gui/screen/turnreport/ShowTurnEvensOnTurnStartedEvent.java) otevře report pouze pro čerstvý začátek tahu člověka s neprázdným seznamem zpráv. [TeService](../microcol-game/src/main/java/org/microcol/gui/screen/turnreport/TeService.java) zpracuje novou jednotku, příjezd lodě, hladomor, ztrátu či zničení kolonie a vyhozené zboží. | Oddělit zvuk začátku tahu od významu jednotlivých zpráv. Více zpráv nemá současně přehrát několik stejně důležitých efektů. |
| Boj | [UnitAttackedEventListener](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/UnitAttackedEventListener.java) spustí animaci zkřížených mečů. [UnitAttackedEvent](../microcol-game/src/main/java/org/microcol/model/event/UnitAttackedEvent.java) nese útočníka, obránce a zničenou jednotku. | Výběr cíle, otevření bojového poradce, skutečný útok a výsledek jsou různé okamžiky; výhru vztáhnout ke správnému hráči. |
| Pohyb a dobytí kolonie | [UnitMovedListener](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/UnitMovedListener.java) animuje krok pohybu. [GamePanelPresenter](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/GamePanelPresenter.java) při `ColonyWasCapturedEvent` otevírá dialog dobytí kolonie. | Každý krok nemusí mít zvuk; zvlášť řešit významnou událost dobytí a její příjemce. |
| Narození jednotky | [SelectedUnitManager](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/SelectedUnitManager.java) může po `NewUnitWasBornEvent` vybrat novou jednotku na vybraném políčku. Zpráva se objevuje také v přehledu tahu. | Určit jeden okamžik oznámení, aby narození a pozdější zobrazení reportu nehrály dvojmo. Výcvik, koupě a narození nejsou automaticky stejná událost. |
| Nedostatek peněz nebo nemožný pohyb | [BuyUnitPanel](../microcol-game/src/main/java/org/microcol/gui/screen/europe/BuyUnitPanel.java) a [MarketBuyPanel](../microcol-game/src/main/java/org/microcol/gui/screen/market/MarketBuyPanel.java) zobrazují `DialogNotEnoughGold`; `GamePanelPresenter` pro některé nepovolené pohyby zobrazí `DialogUnitCantMoveHere`. | Chyba vstupu není herní prohra. Úspěšný nákup sám nevysílá univerzální událost „success“ a ne každá neplatná akce otevírá dialog. |

**`EventFinishMission` nese od R-0228 výslovný výsledek.** Každý producent jej vytváří buď jako `EventFinishMission.victory(...)`, nebo `EventFinishMission.ended(...)`; GUI čte `isVictory()` a lokalizované zprávy nezkoumá. Vítězstvím jsou jen splnění všech cílů `Default_0_mission`, `Default_1_mission` a `Default_2_mission` a vyhrané povstání (`REBELLION_WON_PROCESSOR` v [GameOverProcessors](../microcol-game/src/main/java/org/microcol/model/campaign/GameOverProcessors.java)). Vypršení času, ztráta kolonií, prohrané povstání, kalendářní konec volné hry i odchod do důchodu jsou `ended`. Samostatný proherní motiv zůstává mimo rozsah.

[ModelListenerImpl](../microcol-game/src/main/java/org/microcol/gui/event/model/ModelListenerImpl.java) převádí modelové události na GUI `EventBus`, ale některé filtruje: například kroky pohybu podle viditelnosti a změnu zlata podle lidského hráče. Událost prodeje zboží v Evropě nepředává, boj naopak v této vrstvě nemá stejný filtr viditelnosti jako pohyb. Při napojení zvuku tedy ověřit celý tok od vzniku události po její zpracování; existence třídy události sama nestačí.

## Technický návrh pro budoucí efekty

Následující struktura je výchozí návrh pro efekty. R-0228 z ní zavedl jen adresáře `src/audio/` a `src/main/resources/sounds/` a přehrávač jediného efektu; obecná služba pro více efektů neexistuje.

### Soubory a formáty

- Stávající hudbu ponechat v `microcol-game/src/main/resources/music/`.
- Krátké efekty ukládat do `microcol-game/src/main/resources/sounds/`, s jednoduchými názvy podle účelu, například `mission-success-01.wav`. To je příklad budoucího souboru, nikoli dostupný resource.
- Pro první efekty používat jako výchozí export **WAV, nekomprimované PCM, 16 bitů, 44 100 Hz**. Navazuje na parametry existující skladby; přehrání nových souborů se přesto musí ověřit. Jednoduchý neprostorový efekt může být mono, hudba stereo. Jiné formáty či parametry nejprve ověřit v cílovém přehrávači.
- Má-li zvuk editovatelný zdroj nebo vzniká úpravou nahrávky, evidovat jej odděleně od runtime exportu; navržené místo je `microcol-game/src/audio/`. Ukládat postup exportu a původ, aby změna nebyla závislá na jednom hotovém WAV.
- Exporty mají mít čistý začátek a konec bez lupnutí, zbytečného ticha a slyšitelného zkreslení. Vnímanou hlasitost sladit s ostatními efekty a hudbou. Číselný cíl hlasitosti ani pevný počet variant zatím nejsou stanovené; smyčku připravovat jen tam, kde je požadována.

### Zapojení do kódu

Budoucí cesta má být **potvrzená událost → rozhodnutí, zda ji hráč má slyšet → identifikátor efektu → audio služba → resource**. Modelová pravidla nemají otevírat WAV ani znát jeho umístění. GUI vrstva může využít existující `EventBus`, pokud příslušná událost obsahuje správný výsledek a vzniká ve vhodný okamžik.

Pro efekty zavést samostatnou službu nebo vědomě rozšířit audio vrstvu o souběh s hudbou. Nezaměňovat dlouhý stream skladby s přehráním krátkého potvrzení. Načítání a přehrávání nesmí blokovat JavaFX vlákno; krátké opakované efekty mohou využít cache, dlouhá hudba streaming. Konkrétní API a implementace zůstávají rozhodnutím první funkční změny.

První implementace musí bezpečně obsloužit vlastní prostředky, respektovat současné vypnutí zvuku a zvládnout chybu bez dopadu na herní akci. Určit, co se stane při souběhu zvuků. Oddělené ovládání hudby a efektů doplnit při rozšíření nastavení; není podmínkou každého jednotlivého efektu. Pravidla pro prioritu, odstup nebo potlačení opakování ukládat k jednotlivým efektům a skutečně je zapojit do kódu; samotný zápis v dokumentaci nic nepřehrává ani neomezuje.

## První efekt schválený 1. listopadu 2026

1. Uživatel pro [R-0228](requirements/R-0228.md) vybral krátké potvrzení vítězství v misi a pouze podporu přehrávání nezbytnou pro tento efekt. [R-0226](requirements/canceled/R-0226.md) zůstává zrušen; jeho obecný rozsah se tím neobnovuje.
2. Efekt doplní první zobrazení potvrzeného úspěšného dokončení lidské mise (splněné cíle nebo potvrzené vítězství nezávislosti). Prohra, časový limit, odchod do důchodu, běžné kalendářní ukončení, znovuotevření zprávy a načtení dokončené hry jej nespustí. Stávající text a výsledek nesou úplnou informaci i bez zvuku.
3. Jedna dokončená partie má nejvýše jeden zvuk; nevzniká fronta ani překryv. Nově rozehraná partie může mít vlastní vítězství. Do R-0227 platí současná hlasitost a nulová hodnota znamená ticho; `soundEnabled=false` má vždy přednost. Hudba běží nezávisle. Chyba zařízení nebo souboru nesmí ovlivnit hru ani zdržet UI.
4. Implementace je popsána v [katalogu](#mission-success-01-zvuk-vitezstvi-v-misi), včetně zdroje, exportu, práv a stavu ověření. Uživatel schválil poslech náhledu 1. listopadu 2026; mix na všech platformách není ověřen. [R-0227](requirements/R-0227.md) následně přidá samostatné ovládání kategorií.

## Katalog a šablona nového zvuku

Současný katalog tvoří [jedna skladba popsaná výše](#co-je-nyni-ulozeno-v-projektu) a [zvuk vítězství v misi](#mission-success-01-zvuk-vitezstvi-v-misi). Nové efekty přidávat sem s vlastní stručnou podkapitolou podle následující šablony. Katalog rozšiřovat podle skutečných funkčních změn; počty zvuků v [odhadu pracnosti](implementation-estimate.md#provisional-additional-work) jsou plánovací scénáře, nikoli schválený seznam povinných assetů.

| Položka | Co vyplnit |
| --- | --- |
| Identifikátor a stav | Stabilní název; návrh / soubor připraven / zapojeno / ověřeno. |
| Význam | Co má hráč pochopit; potvrzení, úspěch, upozornění, neúspěch či atmosféra. |
| Spouštěč | Konkrétní událost a metoda, podmínky výsledku, hráč, kterého se týká. |
| Okamžik | Při provedení akce, zásahu, dokončení animace nebo jiném přesně určeném bodu. |
| Vizuální doprovod | Text, ikona, animace, report nebo změna stavu; případně lokalizační klíče. |
| Soubory | Resource cesta a odkaz na editovatelný zdroj, autor, původ, licence a potřebný kredit. |
| Parametry | Formát, frekvence, bitová hloubka, kanály, délka, počet variant a případná smyčka. |
| Hlasitost a priorita | Kategorie hudba/efekt, poměr k ostatním zvukům, chování při překryvu. |
| Opakování | Nejmenší odstup, slučování více událostí, potlačení při znovuotevření UI nebo načtení hry. |
| Vypnutí a chyba | Chování při mute, vypnutém zvuku, chybějícím souboru nebo nedostupném zařízení. |
| Ověření | Datum, scénář, platforma a výsledek; oddělit kontrolu kódu od skutečného poslechu. |

### mission-success-01 — zvuk vítězství v misi

| Položka | Hodnota |
| --- | --- |
| Identifikátor a stav | `mission-success-01`; **zapojeno v kódu** ([R-0228](requirements/R-0228.md), 1. listopadu 2026). Export, automatické ověření a schválení poslechu náhledu jsou dokončeny; podrobnosti níže. |
| Význam | Úspěch: hráč dokončil misi nebo vyhrál povstání. |
| Spouštěč | `EventFinishMission` s `isVictory()`; [MissionEventListener](../microcol-game/src/main/java/org/microcol/gui/event/model/MissionEventListener.java) předá výsledek a identitu partie (`GameModel.getPlaythroughId()`) do [MissionVictoryCue](../microcol-game/src/main/java/org/microcol/gui/event/model/MissionVictoryCue.java). Konec mise vzniká jen z mise lidského hráče; roboti ani skryté dění jej nevyvolají. |
| Okamžik | Na JavaFX vlákně těsně před zobrazením první zprávy o vítězství. |
| Vizuální doprovod | Stávající zpráva o vítězství (např. `Missions.gameOver_rebellionVictory`, `default_m0_gameOver`, `default_m1_done`, `default_m2_done1`), okno výsledku a přechod na obrazovku kampaně. Bez zvuku se nic nemění. |
| Soubory | Runtime `sounds/mission-success-01.wav` v `microcol-game/src/main/resources/`; konstanta `JavaSoundSuccessCuePlayer.RESOURCE`. Editovatelný zdroj [mission-success-01.py](../microcol-game/src/audio/mission-success-01.py). Autor: projekt MicroCol, listopad 2026; původní dílo vypočtené jen z čísel ve zdroji, bez převzaté nahrávky či melodie. Šíří se stejně jako ostatní původní zdroje projektu; cizí kredit není potřeba. |
| Export | Z kořene repozitáře `python3 -I microcol-game/src/audio/mission-success-01.py`. Bez náhody; stejný Python na stejné platformě zapíše stejný soubor. |
| Parametry | Zadané ve zdroji: WAV, nekomprimované PCM, 44 100 Hz, 16 bitů, mono, 1,4 s, jedna varianta, bez smyčky. Vzestupný rozložený akord C dur (C5 E5 G5 C6) se zvonivým tónem, náběh 8 ms, doznění 0,25 s do přesného ticha, špička −6 dBFS. Export ověřen: 61 740 rámců, 1,400 s, 44 100 Hz, 16bitové mono PCM, špička 16 384; opakovaný export je bitově totožný. |
| Hlasitost a priorita | Kategorie efekt, vlastní linka vedle hudby; hudba běží nezávisle. Do R-0227 platí uložená hlasitost 0–100: přehrávač lineárně násobí vzorky exportu `hlasitost / 100` (50 je asi −6 dB, 100 beze změny), takže nezávisí na `MASTER_GAIN` zařízení a nikdy nezesiluje ani neořezává. Ovladač gain linky nepoužívá. Hudba si ponechává vlastní mapování na `MASTER_GAIN`. Během přehrávání se další požadavek zahodí; fronta ani překryv nevznikají. |
| Opakování | Nejvýše jednou za partii a běh aplikace. Opakované oznámení, zprávy, Continue, načtení či vítězství znovu dosažené ze starší pozice téže partie jej nespustí; nová partie má vlastní. |
| Vypnutí a chyba | `soundEnabled=false` a hlasitost 0 zvuk potlačí. Chybějící či nečitelný soubor, soubor v jiném formátu než 44 100 Hz, 16 bitů, mono, signed PCM little endian (odmítne se před otevřením linky), nedostupné zařízení, selhání zápisu nebo vlákna jen zvuk vynechají a zapíší varování; stream i linka se vždy zavřou. Ukončení hry (`QuitGameListener`) i aplikace (`MicroColApplication.stop`) přehrávání zastaví. |
| Ověření | 1. listopadu 2026: 4 840 modelových testů, pouze schválené známé selhání FRIGATE, žádné chyby/přeskočení; 19 cílených Docker kontrol prošlo. Uživatel přehrál náhled a schválil „Sounds good—use it“. Jde o schválení náhledu, nikoli ověření mixu hudby a efektu na všech platformách. Přesné běhy jsou ve [funkční evidenci](functional-tests.md#r-0228-mission-victory-cue-2026-11-01). |


### Postup při ozvučení herní události

Tento postup platí pro situace vybrané k ozvučení. Pokud stačí vizuální odezva, nová funkce nepotřebuje audio soubor ani změnu přehrávače.

1. Rozhodnout, co hráč potřebuje vidět a zda mu zvuk pomůže. Zapsat význam a spouštěč do katalogu ještě před zapojováním.
2. Ověřit zdroj události a její výsledek. Rozlišit úspěch a neúspěch, aktuálního hráče, skutečné nové dění a opětovné zobrazení staré zprávy.
3. Připravit potřebný soubor nebo varianty a vizuální doprovod. Pro novou jednotku není automaticky potřeba vlastní hlas či celá sada efektů; podle situace lze sdílet například potvrzení dokončeného výcviku.
4. Zapojit efekt do audio služby, nastavení a odpovídajícího listeneru. Pokud potřebná infrastruktura ještě chybí, zavést pouze část potřebnou pro daný efekt a popsat skutečně zavedené chování zde.
5. Ověřit správný okamžik, počet přehrání, hlasitost, souběh, vypnutí zvuku a chování při chybě. Poslechem zkontrolovat i několik opakování v běžném tempu hry.
6. Aktualizovat stav katalogu, odkazy na kód a datum ověření. Při změně tohoto návrhu zachovat jediný aktuální popis zde.

## Ověření a další otevřené části

Kontrola z **11. října 2026** zahrnuje prohledání audio resources a jejich volání, průchod implementací přehrávání a nastavení a přečtení WAV přes `file` a Python `wave`. [SettingDaoTest](../microcol-game/src/test/java/org/microcol/gui/preferences/SettingDaoTest.java) ověřuje ukládání celého nastavení včetně hodnoty hlasitosti; [test upozornění na hladomor](../microcol-test/src/test/java/org/microcol/test/TC_10_F10_famine_will_plague_colony_test.java) kontroluje vizuální report. Tyto testy nedokládají slyšitelné přehrání. Přímé testy životního cyklu `MusicPlayer` ani spouštění efektů ve zdrojích nejsou. Při této dokumentační změně nebyla hra spouštěna ani proveden poslech či test distribučního balíčku.

Dokumentace prošla téhož dne sestavením `target/docs-venv/bin/python -m mkdocs build --strict`, kontrolou odkazů a vizuální kontrolou místního náhledu.

Pro budoucí rozšíření ještě zůstává rozhodnout konkrétní zvukový charakter a obsazení, výběr efektů pro první herní situace, případné nové skladby a jejich opakování. Technicky zbývá zavést obsluhu efektů, oddělená nastavení a ověření bez audiozařízení i na podporovaných platformách. Žádná z těchto položek neznamená, že musí vzniknout předem celá rozsáhlá zvuková sada; doplňovat je spolu s konkrétními funkcemi.
