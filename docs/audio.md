# Zvuky a zvuková odezva v Akropia

Zvuk má hráči pomáhat poznat, co se právě stalo: potvrdit důležitý úspěch, upozornit na problém nebo doplnit atmosféru hry. Akropia zatím přehrává **jednu hudební skladbu na pozadí**. Od R-0228 je v kódu zapojen **jediný efekt**, krátký [zvuk vítězství v misi](#mission-success-01-zvuk-vitezstvi-v-misi). Obecná správa více zvuků a další efekty implementované nejsou.

Tato stránka je společným místem pro současnou implementaci, základní pravidla a budoucí katalog zvuků. Grafická část má vlastní [pravidla a technický popis](graphics.md). Stav k **11. říjnu 2026**, větev `develop`, revize `aa1161ea7b3a7ea604a8031065c579557b3e8f92`. Popis současného chování vychází z kódu a metadat souboru; neznamená ověřený poslech na všech platformách.

## Základní pravidla pro další práci

Toto je výchozí pracovní zadání pro nové zvuky. Podrobný hudební styl, nástroje a historická věrnost zatím nejsou stanovené a není potřeba je řešit před každou funkční změnou.

- **Zvuk musí mít srozumitelný účel.** U každého efektu pojmenovat událost a informaci, kterou sděluje. Úspěch, varování a neúspěch mají být rozlišitelné. Není potřeba ozvučit každé kliknutí nebo každý krok jednotky.
- **Důležitá informace musí být dostupná i bez zvuku.** Efekt doplňuje text, stav obrazovky, ikonu či animaci. Například při získání odměny má hráč vidět, co a kolik získal; krátký příjemný zvuk její přijetí podpoří.
- **Výsledek má působit příjemně a přiměřeně.** Běžné potvrzení má být krátké a nenápadné, významný úspěch může mít výraznější motiv. Zvuky nemají rušit při dlouhé hře ani přehlušovat důležitější upozornění. Konkrétní estetické řešení lze zvolit vlastním úsudkem v rámci tohoto zadání.
- **Přehrávat skutečný výsledek ve správný okamžik.** Úspěch oznamovat až po provedení akce. Samotné stisknutí tlačítka ani obecný konec mise neprokazují úspěch. U animovaných dějů určit, zda efekt patří k zahájení, zásahu nebo dokončení.
- **Hlídání opakování je součást návrhu.** Jedna událost nemá znít několikrát kvůli překreslení obrazovky či otevření stejného reportu. U častých událostí určit slučování, minimální odstup a případné varianty; u jednorázového úspěchu obvykle stačí jeden zvuk.
- **Zvuk respektuje hráčův pohled a nastavení.** Neprozrazovat dění, které hráč nevidí nebo o něm zatím nemá vědět. Nové efekty musejí respektovat vypnutí zvuku a od [R-0227](requirements/done/R-0227.md) samostatnou [hlasitost efektů](#hlasitost-a-vypnuti-zvuku); hudba má vlastní hlasitost.
- **Chyba zvuku nesmí zablokovat herní akci.** Při rozšíření zajistit dokončení herní akce i bez dostupného audiozařízení či konkrétního efektu. Hudba i zvuk vítězství při chybě jen ztichnou a uvolní prostředky (R-0227, R-0228).

## Co je nyní uloženo v projektu

V prohlédnutých zdrojích byl 11. října 2026 jediný audio soubor: [AnnounceMyName_1.wav](../akropia-game/src/main/resources/music/AnnounceMyName_1.wav). Není zde zdrojový projekt pro střih této skladby. Efekt R-0228 má vlastní editovatelný zdroj a export popsaný [v katalogu](#mission-success-01-zvuk-vitezstvi-v-misi).

| Vlastnost | Současná hodnota |
| --- | --- |
| Umístění ve zdrojích | `akropia-game/src/main/resources/music/AnnounceMyName_1.wav` |
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

[Konfigurace resources](../akropia-game/pom.xml) kopíruje obsah `src/main/resources` bez textového filtrování; zvlášť filtruje pouze `version.properties`. Pro audio není konverzní ani exportní krok. Do hry se dostává již hotový WAV. Na rozdíl od obrázků se nevyřezávají části atlasu a neexistují aliasy jednotlivých zvuků.

[StreamReader](../akropia-game/src/main/java/org/akropia/gui/util/StreamReader.java) používá `Module.getResourceAsStream`. Cesta je relativní ke kořeni resources, bez `src/main/resources` a bez absolutní cesty na disku. Přejmenování současné skladby proto vyžaduje změnit i řetězec v `MusicController`.

Zvuk používá **Java Sound z `javax.sound.sampled`**, nikoli JavaFX `MediaPlayer` nebo `AudioClip`. Modulové deskriptory v [akropia-module](../akropia-module/src/main/java/module-info.java) a [akropia-dist](../akropia-dist/src/main/java/module-info.java) deklarují `java.desktop`. Přibalení souboru podle konfigurace samo o sobě neověřuje funkční zvuk ve výsledném instalačním balíčku.

## Jak nyní funguje přehrávání

### Spuštění a přehrání skladby

1. [AkropiaApplication](../akropia-game/src/main/java/org/akropia/AkropiaApplication.java) nastaví `primaryStage.setOnShowing` na volání `ApplicationController.startMusic()` a zobrazí hlavní okno. Hudba tedy začíná při otevření aplikace, už v nabídce, nikoli až se začátkem konkrétní partie.
2. [ApplicationController](../akropia-game/src/main/java/org/akropia/gui/dialog/ApplicationController.java) vezme uloženou hlasitost hudby (`GamePreferences.getMusicVolume()`) a předá ji do `MusicController.start()`.
3. [MusicController](../akropia-game/src/main/java/org/akropia/gui/MusicController.java) ověří `isSoundEnabled()`, nastaví hlasitost přehrávače a spustí nové vlákno, které zavolá `MusicPlayer.playSound("music/AnnounceMyName_1.wav")`. Výběr skladby je pevně zapsaný v kódu.
4. [MusicPlayer](../akropia-game/src/main/java/org/akropia/gui/MusicPlayer.java) otevře `AudioInputStream` a ověří formát: přijme jen signed 16bitové PCM little endian (počet kanálů a frekvenci nechá na lince). Pak získá a otevře odpovídající `SourceDataLine` a spustí ji. Data čte po blocích **16 384 bajtů** (asi 0,1 s stereo skladby), každý blok vynásobí aktuální hlasitostí a zapíše do výstupu. Nenačítá předem celou skladbu do paměťové cache.
5. Po konci souboru zavolá `drain()` a v každém případě `close()` na lince i vstupním streamu. Další skladba ani opakování nenásledují. Změna obrazovky, konec tahu nebo výsledek mise nejsou v této cestě zvukové spouštěče.

`MusicPlayer` a `MusicController` jsou sdílené instance vytvořené přes Guice. [AkropiaModule](../akropia-game/src/main/java/org/akropia/gui/AkropiaModule.java) registruje instance s anotací `@Listener` do společného Guava `EventBus`; na události reagují metody označené `@Subscribe`. `MusicController` takto poslouchá pouze `VolumeChangeEvent` a podle jeho kategorie předá hodnotu buď hudbě, nebo přehrávači zvuku vítězství.

### Hlasitost a vypnutí zvuku

Od [R-0227](requirements/done/R-0227.md) má hudba a efekty každá vlastní hlasitost.

| Nastavení | Současná implementace |
| --- | --- |
| Ovládací prvky | Dva posuvníky v nastavení, **Hudba / Music** (`settingVolume`) a **Efekty / Effects** (`settingEffectsVolume`), v nabídce i ve hře; [SettingVolumeView](../akropia-game/src/main/java/org/akropia/gui/screen/setting/SettingVolumeView.java). Každý má popisek, konce **Ticho / Silent** a **Nahlas / High** a vedle sebe aktuální hodnotu (`75 %`, při nule `Ticho` / `Silent`). Samostatný přepínač mute není; nula je ticho. |
| Rozsah a ovládání | Celá čísla 0 až 100. Šipky mění hodnotu o 10, Home a End nastaví konce; tažení a klik na lištu zůstávají. |
| Výchozí hodnoty | Nová instalace: obě 75. Nastavení uložené před R-0227 bez hlasitosti efektů: efekty převezmou uloženou hlasitost hudby, takže hráč, který zvolil ticho, zůstane v tichu. Hudba si ponechá uloženou hodnotu. Při první změně hudby se tato odvozená hlasitost efektů uloží ve stejném zápisu, takže se s hudbou nepohne; samotné čtení soubor nemění. |
| Změna za běhu | [SettingVolumePresenter](../akropia-game/src/main/java/org/akropia/gui/screen/setting/SettingVolumePresenter.java) propojuje posuvníky s `PreferencesProperties.musicVolumeProperty` a `effectsVolumeProperty`. |
| Uložení | [PreferencesProperties](../akropia-game/src/main/java/org/akropia/gui/preferences/PreferencesProperties.java) při každé změně okamžitě uloží jen danou kategorii přes `GamePreferences` a vyšle `VolumeChangeEvent` s její kategorií. Zpět, Escape, znovuotevření i restart aplikace ukážou uložené hodnoty. Jde o nastavení aplikace, ne o data uložené hry. |
| Soubor nastavení | Pole `volume` (hudba; název zůstal) a `effectsVolume` v `${user.home}/.akropia/.setting.json`; [SettingService](../akropia-game/src/main/java/org/akropia/gui/preferences/SettingService.java), [SettingDao](../akropia-game/src/main/java/org/akropia/gui/preferences/SettingDao.java) a [PersistingTool](../akropia-game/src/main/java/org/akropia/model/campaign/po/PersistingTool.java). |
| Neplatné hodnoty | `GamePreferences` odmítne zápis mimo 0–100. Ručně upravenou hodnotu mimo rozsah při čtení omezí na 0, resp. 100; chybějící hudba má výchozí 75. Nečitelný soubor (například text místo čísla) nahradí výchozím nastavením jako u ostatních voleb. |
| Zpracování události | `MusicController` předá hodnotu hudby do `MusicPlayer.setVolume()` a hodnotu efektů do `SuccessCuePlayer.setVolume()`, pokud je zvuk povolený. Kategorie se navzájem neovlivňují. |
| Vypnutí při spuštění | JVM vlastnost `soundEnabled=false`, například argument `-DsoundEnabled=false` předaný přímo JVM hry. Utiší hudbu i efekty bez ohledu na uložené hodnoty a nemění je. |
| Výchozí povolení | `GamePreferences.isSoundEnabled()` používá výchozí hodnotu **`true`**. |

Ukládání probíhá přes JSON, ačkoli starší komentář v [GamePreferences](../akropia-game/src/main/java/org/akropia/gui/preferences/GamePreferences.java) ještě odkazuje na `java.util.prefs`. Při přidání dalších nastavení navázat na skutečnou cestu přes `Setting`, `SettingService` a `SettingDao`.

Hudba i zvuk vítězství násobí vzorky signed 16bitového PCM hodnotou `hlasitost / 100` (sdílená metoda `JavaSoundSuccessCuePlayer.scale`). **Je to přímé procento amplitudy**, 50 je asi −6 dB a 100 beze změny; nic se nezesiluje ani neořezává. Ovladač `MASTER_GAIN` linky se nepoužívá, takže zařízení bez něj hlasitost i ticho respektuje. Při nule hudba dál zapisuje nulové vzorky, skladba tedy běží potichu a po zvýšení pokračuje ve stejném místě. Změna platí od dalšího zapsaného bloku; k tomu se přidá vyrovnávací paměť linky, jejíž délku určuje Java Sound a zařízení.

`soundEnabled` je systémová vlastnost JVM, ne pole v uživatelském JSON. Při spuštění přes nástroj vytvářející další JVM je nutné ověřit předání vlastnosti právě procesu hry; samotný parametr Maven příkazu to nemusí zajistit.

### Ukončení a omezení důležitá pro rozšíření

[QuitGameListener](../akropia-game/src/main/java/org/akropia/gui/event/QuitGameListener.java) při `QuitGameEvent` zavolá `MusicController.stop()` a poté `System.exit(0)`; od R-0227 hudbu zastaví i `AkropiaApplication.stop()`. `MusicPlayer.stop()` nastaví příznak `run=false` a otevřenou linku zastaví a vyprázdní, takže blokovaný zápis hned skončí; nečeká na konec vlákna. Přehrávací vlákno pak bez `drain()` zavře linku i stream.

Před použitím pro další efekty je potřeba počítat s těmito vlastnostmi současného kódu:

- Přehrávač má jedinou sdílenou `sourceLine` a příznak `run`. Nemá řízení souběžných hlasů, frontu, priority ani ochranu proti opakovanému `start()`. Spustit pro každý efekt další `MusicController.start()` by znovu spouštělo tutéž skladbu a sdílelo stav.
- Po `stop()` se `run` nevrací na `true`. Restart přehrávače v téže instanci tedy nelze považovat za hotovou funkci.
- Od R-0227 lze `setVolume()` volat kdykoli, i před otevřením linky, po jejím zavření nebo bez zařízení; hodnota se jen uloží pro další blok. Hodnotu mimo 0–100 odmítne.
- Chybějící resource, soubor v jiném formátu, nedostupné zařízení (`LineUnavailableException` i `IllegalArgumentException` z Java Sound) nebo chyba čtení či zápisu jen zapíší varování a hudba zůstane tichá; stream i linka se zavřou v blocích `finally` / try-with-resources. Nastavení ani hra o chybě nevědí a fungují dál.

Nepodporované formáty se odmítají, nikoli převádějí; podpora jiných formátů vyžaduje samostatné ověření.

## Kde hra nyní oznamuje události

Hra už má vizuální odezvu a modelové události, na které může další zvuková práce navázat. **Kromě vítězného konce mise žádná z následujících herních situací nepřehrává vlastní efekt.**

| Situace | Současná vizuální odezva a místo v kódu | Co rozlišit při budoucím ozvučení |
| --- | --- | --- |
| Zpráva scénáře | [MissionEventListener](../akropia-game/src/main/java/org/akropia/gui/event/model/MissionEventListener.java) otevírá lokalizovaný `DialogMessage` pro `EventShowMessages` a `DialogWithMan` pro `EventShowDialogWithMan`. | Typ dialogu sám neříká, zda jde o úspěch, varování nebo jinou zprávu. |
| Konec mise | Tentýž listener při `EventFinishMission` zobrazí zprávy a přepne na obrazovku `CAMPAIGN`. Vítězný konec doprovodí [zvuk vítězství](#mission-success-01-zvuk-vitezstvi-v-misi). | Proherní či neutrální konec zvuk nemá; viz níže. |
| Splněný dílčí cíl | Například [Default_1_mission](../akropia-game/src/main/java/org/akropia/model/campaign/Default_1_mission.java) mění stav cíle a posílá zprávu. [GoalsPanel](../akropia-game/src/main/java/org/akropia/gui/screen/goals/GoalsPanel.java) při otevření ukazuje různé pečetě podle splnění. | [MissionGoal.setFinished](../akropia-game/src/main/java/org/akropia/model/campaign/MissionGoal.java) pouze změní boolean; neexistuje zde univerzální událost dokončení cíle. Překreslení pečeti není nové splnění. |
| Začátek tahu a jeho přehled | [ShowTurnEvensOnTurnStartedEvent](../akropia-game/src/main/java/org/akropia/gui/screen/turnreport/ShowTurnEvensOnTurnStartedEvent.java) otevře report pouze pro čerstvý začátek tahu člověka s neprázdným seznamem zpráv. [TeService](../akropia-game/src/main/java/org/akropia/gui/screen/turnreport/TeService.java) zpracuje novou jednotku, příjezd lodě, hladomor, ztrátu či zničení kolonie a vyhozené zboží. | Oddělit zvuk začátku tahu od významu jednotlivých zpráv. Více zpráv nemá současně přehrát několik stejně důležitých efektů. |
| Boj | [UnitAttackedEventListener](../akropia-game/src/main/java/org/akropia/gui/screen/game/gamepanel/UnitAttackedEventListener.java) spustí animaci zkřížených mečů. [UnitAttackedEvent](../akropia-game/src/main/java/org/akropia/model/event/UnitAttackedEvent.java) nese útočníka, obránce a zničenou jednotku. | Výběr cíle, otevření bojového poradce, skutečný útok a výsledek jsou různé okamžiky; výhru vztáhnout ke správnému hráči. |
| Pohyb a dobytí kolonie | [UnitMovedListener](../akropia-game/src/main/java/org/akropia/gui/screen/game/gamepanel/UnitMovedListener.java) animuje krok pohybu. [GamePanelPresenter](../akropia-game/src/main/java/org/akropia/gui/screen/game/gamepanel/GamePanelPresenter.java) při `ColonyWasCapturedEvent` otevírá dialog dobytí kolonie. | Každý krok nemusí mít zvuk; zvlášť řešit významnou událost dobytí a její příjemce. |
| Narození jednotky | [SelectedUnitManager](../akropia-game/src/main/java/org/akropia/gui/screen/game/gamepanel/SelectedUnitManager.java) může po `NewUnitWasBornEvent` vybrat novou jednotku na vybraném políčku. Zpráva se objevuje také v přehledu tahu. | Určit jeden okamžik oznámení, aby narození a pozdější zobrazení reportu nehrály dvojmo. Výcvik, koupě a narození nejsou automaticky stejná událost. |
| Nedostatek peněz nebo nemožný pohyb | [BuyUnitPanel](../akropia-game/src/main/java/org/akropia/gui/screen/europe/BuyUnitPanel.java) a [MarketBuyPanel](../akropia-game/src/main/java/org/akropia/gui/screen/market/MarketBuyPanel.java) zobrazují `DialogNotEnoughGold`; `GamePanelPresenter` pro některé nepovolené pohyby zobrazí `DialogUnitCantMoveHere`. | Chyba vstupu není herní prohra. Úspěšný nákup sám nevysílá univerzální událost „success“ a ne každá neplatná akce otevírá dialog. |

**`EventFinishMission` nese od R-0228 výslovný výsledek.** Každý producent jej vytváří buď jako `EventFinishMission.victory(...)`, nebo `EventFinishMission.ended(...)`; GUI čte `isVictory()` a lokalizované zprávy nezkoumá. Vítězstvím jsou jen splnění všech cílů `Default_0_mission`, `Default_1_mission` a `Default_2_mission` a vyhrané povstání (`REBELLION_WON_PROCESSOR` v [GameOverProcessors](../akropia-game/src/main/java/org/akropia/model/campaign/GameOverProcessors.java)). Vypršení času, ztráta kolonií, prohrané povstání, kalendářní konec volné hry i odchod do důchodu jsou `ended`. Samostatný proherní motiv zůstává mimo rozsah.

[ModelListenerImpl](../akropia-game/src/main/java/org/akropia/gui/event/model/ModelListenerImpl.java) převádí modelové události na GUI `EventBus`, ale některé filtruje: například kroky pohybu podle viditelnosti a změnu zlata podle lidského hráče. Událost prodeje zboží v Evropě nepředává, boj naopak v této vrstvě nemá stejný filtr viditelnosti jako pohyb. Při napojení zvuku tedy ověřit celý tok od vzniku události po její zpracování; existence třídy události sama nestačí.

## Technický návrh pro budoucí efekty

Následující struktura je výchozí návrh pro efekty. R-0228 z ní zavedl jen adresáře `src/audio/` a `src/main/resources/sounds/` a přehrávač jediného efektu; obecná služba pro více efektů neexistuje.

### Soubory a formáty

- Stávající hudbu ponechat v `akropia-game/src/main/resources/music/`.
- Krátké efekty ukládat do `akropia-game/src/main/resources/sounds/`, s jednoduchými názvy podle účelu, například `mission-success-01.wav`. To je příklad budoucího souboru, nikoli dostupný resource.
- Pro první efekty používat jako výchozí export **WAV, nekomprimované PCM, 16 bitů, 44 100 Hz**. Navazuje na parametry existující skladby; přehrání nových souborů se přesto musí ověřit. Jednoduchý neprostorový efekt může být mono, hudba stereo. Jiné formáty či parametry nejprve ověřit v cílovém přehrávači.
- Má-li zvuk editovatelný zdroj nebo vzniká úpravou nahrávky, evidovat jej odděleně od runtime exportu; navržené místo je `akropia-game/src/audio/`. Ukládat postup exportu a původ, aby změna nebyla závislá na jednom hotovém WAV.
- Exporty mají mít čistý začátek a konec bez lupnutí, zbytečného ticha a slyšitelného zkreslení. Vnímanou hlasitost sladit s ostatními efekty a hudbou. Číselný cíl hlasitosti ani pevný počet variant zatím nejsou stanovené; smyčku připravovat jen tam, kde je požadována.

### Zapojení do kódu

Budoucí cesta má být **potvrzená událost → rozhodnutí, zda ji hráč má slyšet → identifikátor efektu → audio služba → resource**. Modelová pravidla nemají otevírat WAV ani znát jeho umístění. GUI vrstva může využít existující `EventBus`, pokud příslušná událost obsahuje správný výsledek a vzniká ve vhodný okamžik.

Pro efekty zavést samostatnou službu nebo vědomě rozšířit audio vrstvu o souběh s hudbou. Nezaměňovat dlouhý stream skladby s přehráním krátkého potvrzení. Načítání a přehrávání nesmí blokovat JavaFX vlákno; krátké opakované efekty mohou využít cache, dlouhá hudba streaming. Konkrétní API a implementace zůstávají rozhodnutím první funkční změny.

První implementace musí bezpečně obsloužit vlastní prostředky, respektovat současné vypnutí zvuku a zvládnout chybu bez dopadu na herní akci. Určit, co se stane při souběhu zvuků. Oddělené ovládání hudby a efektů doplnit při rozšíření nastavení; není podmínkou každého jednotlivého efektu. Pravidla pro prioritu, odstup nebo potlačení opakování ukládat k jednotlivým efektům a skutečně je zapojit do kódu; samotný zápis v dokumentaci nic nepřehrává ani neomezuje.

## První efekt schválený 1. listopadu 2026

1. Uživatel pro [R-0228](requirements/done/R-0228.md) vybral krátké potvrzení vítězství v misi a pouze podporu přehrávání nezbytnou pro tento efekt. [R-0226](requirements/canceled/R-0226.md) zůstává zrušen; jeho obecný rozsah se tím neobnovuje.
2. Efekt doplní první zobrazení potvrzeného úspěšného dokončení lidské mise (splněné cíle nebo potvrzené vítězství nezávislosti). Prohra, časový limit, odchod do důchodu, běžné kalendářní ukončení, znovuotevření zprávy a načtení dokončené hry jej nespustí. Stávající text a výsledek nesou úplnou informaci i bez zvuku.
3. Jedna dokončená partie má nejvýše jeden zvuk; nevzniká fronta ani překryv. Nově rozehraná partie může mít vlastní vítězství. Do R-0227 platí současná hlasitost a nulová hodnota znamená ticho; `soundEnabled=false` má vždy přednost. Hudba běží nezávisle. Chyba zařízení nebo souboru nesmí ovlivnit hru ani zdržet UI.
4. Implementace je popsána v [katalogu](#mission-success-01-zvuk-vitezstvi-v-misi), včetně zdroje, exportu, práv a stavu ověření. Uživatel schválil poslech náhledu 1. listopadu 2026; mix na všech platformách není ověřen. [R-0227](requirements/done/R-0227.md) následně přidá samostatné ovládání kategorií.

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
| Identifikátor a stav | `mission-success-01`; **zapojeno v kódu** ([R-0228](requirements/done/R-0228.md), 1. listopadu 2026). Export, automatické ověření a schválení poslechu náhledu jsou dokončeny; podrobnosti níže. |
| Význam | Úspěch: hráč dokončil misi nebo vyhrál povstání. |
| Spouštěč | `EventFinishMission` s `isVictory()`; [MissionEventListener](../akropia-game/src/main/java/org/akropia/gui/event/model/MissionEventListener.java) předá výsledek a identitu partie (`GameModel.getPlaythroughId()`) do [MissionVictoryCue](../akropia-game/src/main/java/org/akropia/gui/event/model/MissionVictoryCue.java). Konec mise vzniká jen z mise lidského hráče; roboti ani skryté dění jej nevyvolají. |
| Okamžik | Na JavaFX vlákně těsně před zobrazením první zprávy o vítězství. |
| Vizuální doprovod | Stávající zpráva o vítězství (např. `Missions.gameOver_rebellionVictory`, `default_m0_gameOver`, `default_m1_done`, `default_m2_done1`), okno výsledku a přechod na obrazovku kampaně. Bez zvuku se nic nemění. |
| Soubory | Runtime `sounds/mission-success-01.wav` v `akropia-game/src/main/resources/`; konstanta `JavaSoundSuccessCuePlayer.RESOURCE`. Editovatelný zdroj [mission-success-01.py](../akropia-game/src/audio/mission-success-01.py). Autor: projekt Akropia, listopad 2026; původní dílo vypočtené jen z čísel ve zdroji, bez převzaté nahrávky či melodie. Šíří se stejně jako ostatní původní zdroje projektu; cizí kredit není potřeba. |
| Export | Z kořene repozitáře `python3 -I akropia-game/src/audio/mission-success-01.py`. Bez náhody; stejný Python na stejné platformě zapíše stejný soubor. |
| Parametry | Zadané ve zdroji: WAV, nekomprimované PCM, 44 100 Hz, 16 bitů, mono, 1,4 s, jedna varianta, bez smyčky. Vzestupný rozložený akord C dur (C5 E5 G5 C6) se zvonivým tónem, náběh 8 ms, doznění 0,25 s do přesného ticha, špička −6 dBFS. Export ověřen: 61 740 rámců, 1,400 s, 44 100 Hz, 16bitové mono PCM, špička 16 384; opakovaný export je bitově totožný. |
| Hlasitost a priorita | Kategorie efekt, vlastní linka vedle hudby; hudba běží nezávisle. Od R-0227 platí jen uložená [hlasitost efektů](#hlasitost-a-vypnuti-zvuku) 0–100, hlasitost hudby se nečte: přehrávač lineárně násobí vzorky exportu `hlasitost / 100` (50 je asi −6 dB, 100 beze změny), takže nezávisí na `MASTER_GAIN` zařízení a nikdy nezesiluje ani neořezává. Změna během přehrávání platí od dalšího bloku 4 096 bajtů (asi 46 ms); nula pak zapisuje ticho. Ovladač gain linky nepoužívá; stejné násobení používá od R-0227 i hudba. Během přehrávání se další požadavek zahodí; fronta ani překryv nevznikají. |
| Opakování | Nejvýše jednou za partii a běh aplikace. Opakované oznámení, zprávy, Continue, načtení či vítězství znovu dosažené ze starší pozice téže partie jej nespustí; nová partie má vlastní. |
| Vypnutí a chyba | `soundEnabled=false` a hlasitost efektů 0 zvuk potlačí; nulová hlasitost hudby jej neovlivní. Chybějící či nečitelný soubor, soubor v jiném formátu než 44 100 Hz, 16 bitů, mono, signed PCM little endian (odmítne se před otevřením linky), nedostupné zařízení, selhání zápisu nebo vlákna jen zvuk vynechají a zapíší varování; stream i linka se vždy zavřou. Ukončení hry (`QuitGameListener`) i aplikace (`AkropiaApplication.stop`) přehrávání zastaví. |
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

Kontrola z **11. října 2026** zahrnuje prohledání audio resources a jejich volání, průchod implementací přehrávání a nastavení a přečtení WAV přes `file` a Python `wave`. [SettingDaoTest](../akropia-game/src/test/java/org/akropia/gui/preferences/SettingDaoTest.java) ověřuje ukládání celého nastavení včetně hodnoty hlasitosti; [test upozornění na hladomor](../akropia-test/src/test/java/org/akropia/test/TC_10_F10_famine_will_plague_colony_test.java) kontroluje vizuální report. Tyto testy nedokládají slyšitelné přehrání. Přímé testy životního cyklu `MusicPlayer` ani spouštění efektů ve zdrojích nejsou. Při této dokumentační změně nebyla hra spouštěna ani proveden poslech či test distribučního balíčku.

Dokumentace prošla téhož dne sestavením `target/docs-venv/bin/python -m mkdocs build --strict`, kontrolou odkazů a vizuální kontrolou místního náhledu.

**R-0227, 1. listopadu 2026 (implementace):** [MusicPlayerTest](../akropia-game/src/test/java/org/akropia/gui/MusicPlayerTest.java) a rozšířený [JavaSoundSuccessCuePlayerTest](../akropia-game/src/test/java/org/akropia/gui/JavaSoundSuccessCuePlayerTest.java) kontrolují skutečně zapsané vzorky přes testovací linku: násobení hlasitostí bez `MASTER_GAIN`, změnu během přehrávání, nulu jako nulové vzorky, oddělení kategorií, `soundEnabled=false`, nedostupné zařízení, nepodporovaný formát a zavření prostředků. [GamePreferencesVolumeTest](../akropia-game/src/test/java/org/akropia/gui/preferences/GamePreferencesVolumeTest.java) pokrývá výchozí hodnoty, převzetí hudby u starého nastavení, nezávislé uložení, restart a neplatné hodnoty; `TC_84_F75_framed_settings_test.TC_09` ovládání v nabídce i ve hře. Výsledky běhů zapisuje [funkční evidence](functional-tests.md); poslech mixu hudby a efektu ani test na skutečném zařízení v rámci R-0227 neproběhl.

Pro budoucí rozšíření ještě zůstává rozhodnout konkrétní zvukový charakter a obsazení, výběr efektů pro první herní situace, případné nové skladby a jejich opakování. Technicky zbývá zavést obecnou obsluhu dalších efektů a ověřit poslech i chování zařízení na podporovaných platformách; oddělená nastavení hudby a efektů doplnila R-0227. Žádná z těchto položek neznamená, že musí vzniknout předem celá rozsáhlá zvuková sada; doplňovat je spolu s konkrétními funkcemi.


## R-0405: ztráta rybáře — implementace a lokální přejímka 2026-11-03

1. Model vytvoří právě jednu uloženou událost `TurnEventFishermanLost`, když
   vítězná loď nemá místo pro zajatce nebo po ztrátě zásobujícího města není
   volná přípustná pevnina. Zpráva obsahuje důvod, rybáře, jeho pole a město.
2. Přehled událostí zobrazí text v EN/CS i po načtení. Nový zvuk ani zvukový
   soubor se nepřidává; výsledek je plně srozumitelný bez zvuku.
3. Skutečnou přejímku sleduje [společná evidence](functional-tests.md#r-0402r-0403r-0405-local-acceptance-2026-11-03).
