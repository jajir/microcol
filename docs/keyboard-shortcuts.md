# Klávesové zkratky

Autoritativní inventura nastavitelných klávesových akcí, jejich kontextů,
výchozích kombinací a pevných kláves. Zavádí ji
[R-0433](requirements/done/R-0433.md); stav ověření vede požadavek a funkční
evidence. Obecná pravidla ovládání vlastní [UX](ux.md), vstupy hlavní mapy
[inventura mapy](map-controls.md).

## 1. Stav

1. **Implementováno ve zdroji 8. listopadu 2026** (Claude, izolovaný worktree
   nad `f29bf15e`). Lokálně proběhly cílené JUnit testy; Docker/Xvfb scénáře
   a smoke výběr provádí hlavní agent. Do jejich výsledku jde o neověřenou
   hráčskou cestu.
2. Pevné ovládání formulářů, dialogů a myši se nemění; viz kapitolu 5.

## 2. Model a uložení

1. Tři úplné profily
   `akropia-game/src/main/resources/shortcuts-windows.properties`,
   `shortcuts-linux.properties` a `shortcuts-macos.properties` uvádějí každou
   akci právě jednou. Hodnota je seznam kombinací oddělený čárkou, prázdná
   hodnota akci ponechá nepřiřazenou. Kombinace je `Ctrl`, `Alt`, `Shift`,
   `Meta` (na macOS Command) a jedna klávesa podle názvů JavaFX `KeyCode`,
   například `Ctrl+Shift+O`, `Space`, `F5`. Změna profilu nevyžaduje změnu Javy.
2. Běžící systém vybere profil (`ShortcutPlatform`, macOS / Windows / jinak
   Linux). Testy platformu předávají přímo, nezávisle na stroji.
3. Do `.setting.json` (pole `keyboardShortcuts`) se pro každou platformu ukládají
   jen hráčovy odchylky od jejího profilu; ostatní platformy a předvolby zůstávají.
   Prázdná uložená hodnota znamená výslovně nepřiřazenou akci. Neuložené akce
   sledují pozdější změny profilu.
4. Načtení nastavení nic nepřepisuje. Neznámé akce, ne-textové hodnoty a
   nečitelné kombinace se zapíšou do logu a ignorují; akce si ponechá výchozí
   hodnotu. Odstraní je až další výslovné uložení editoru.
5. Kolize po změně profilu: kombinaci, kterou si hráč výslovně uložil, ponechá
   jeho akce a z překrývající se výchozí akce zmizí. Mezi dvěma uloženými
   odchylkami ji drží dřívější akce v pořadí výčtu `ShortcutAction`. Výsledná
   konfigurace tak nikdy nespustí dvě akce jednou klávesou.
6. Kombinace platí jen s přesně uvedenými modifikátory. Např. `Shift+R` už
   neotevře Statistiky, protože `Shift+R` je Silnice.
7. Držené akce (posun mapy, vývojářské načtení v menu) sledují každou
   stisknutou klávesu zvlášť. Uvolnění jedné klávesy ukončí jen ji, bez ohledu
   na modifikátory v té chvíli; jiná držená kombinace stejného směru dál posouvá.
   Uložení nových zkratek zapomene všechny držené klávesy.
8. Akce spouštěné uvolněním (Režim vývojáře, Vynechat tah jednotky) proběhnou
   jednou za přijatý stisk: stisk musí odpovídat přesně, akce proběhne při
   uvolnění téže klávesy, i když modifikátor hráč pustil dřív. Opakovaný stisk
   držené klávesy nic nepřidá; uvolnění bez přijatého stisku nic nespustí.
   Změna fokusu v okně, které stisk přijalo, zapnutí plánovače tras nebo
   uložení zkratek přijatý stisk zruší.
9. Zaměřené textové pole vlastní psaní a úpravy: písmena, číslice, mezerník,
   šipky, Backspace i kombinace s Shift, Ctrl nebo Command. Zkratky v něm
   reagují jen na funkční klávesy (F1–F24), takže F3 v poli dál přepíná režim
   vývojáře, ale přemapované písmeno v poli zůstane jako text.

## 3. Kontexty

Akce sdílejí kombinaci jen tehdy, když jejich kontexty nikdy neplatí současně.

| Kontext | Kdy platí | Překrývá |
| --- | --- | --- |
| Všude | každé okno a obrazovka | vše |
| Uložené hry, nastavení a konec | hlavní okno mimo editor map; Načíst na každé takové obrazovce, Uložit na připravené mapě, Nastavení a Konec v hlavním menu a na připravené mapě | Všude, Hlavní menu, Mapa, Evropa, Kolonie |
| Hlavní menu | uvítací obrazovka | Všude, Uložené hry |
| Mapa | hlavní mapa | Všude, Uložené hry |
| Evropa | obrazovka evropského přístavu | Všude, Uložené hry |
| Kolonie | obrazovka kolonie | Všude, Uložené hry |
| Editor map | vývojářský editor | Všude |

Hlavní menu, mapa, Evropa a kolonie se nikdy nezobrazují současně, proto
smějí sdílet písmena: `C` je Pokračovat / Najít vybrané místo / Náklad trasy,
`R` Statistiky / Naverbovat / Obchodní trasy kolonie. Příkazy obrazovek
Evropy, kolonie a hlavního menu stisknou jejich stávající tlačítko jen tehdy,
když je na zaměřené scéně zobrazené a povolené (`ShortcutButtons`).

## 4. Inventura a výchozí hodnoty

Windows a Linux mají stejné hodnoty; macOS používá Command (`Meta`) tam, kde
ostatní `Ctrl`. Zkratky dříve rozptýlené v GUI zůstaly zachovány; nové akce
jsou označeny.

| Klíč | Akce | Kontext | Windows / Linux | macOS | Poznámka |
| --- | --- | --- | --- | --- | --- |
| `global.developerMode` | Režim vývojáře | Všude | F3 | F3 | přepíná při uvolnění |
| `session.load` | Načíst hru | Uložené hry | Ctrl+O | Meta+O | |
| `session.save` | Uložit hru | Uložené hry | Ctrl+S | Meta+S | jen na připravené mapě |
| `session.settings` | Nastavení | Uložené hry | Ctrl+Comma | Meta+Comma | **nová**; hlavní menu a připravená mapa |
| `session.quit` | Ukončit Akropii | Uložené hry | Ctrl+Q | Meta+Shift+Q | **nová**; ochrana neuložené hry jako v menu; systémové ⌘Q na macOS zůstává |
| `menu.continue` | Pokračovat ve hře | Hlavní menu | C | C | |
| `menu.secretOption` | Podržet pro vývojářské načtení | Hlavní menu | S | S | držená |
| `menu.freeGame` | Nová volná hra | Hlavní menu | N | N | **nová**; tlačítko menu |
| `menu.campaign` | Hrát kampaň | Hlavní menu | P | P | **nová**; tlačítko menu |
| `menu.hallOfFame` | Síň slávy | Hlavní menu | H | H | **nová**; tlačítko menu |
| `map.quickSave` | Rychle uložit | Mapa | F5 | F5 | |
| `map.quickLoad` | Rychle načíst | Mapa | F9 | F9 | |
| `map.endTurn` | Ukončit tah | Mapa | Ctrl+Enter | Meta+Enter | **nová**; stejné jako tlačítko Další tah |
| `map.mainMenu` | Hlavní menu | Mapa | Ctrl+Shift+M | Meta+Shift+M | **nová**; položka herního menu s ochranou neuložené hry |
| `map.restartMission` | Restartovat misi | Mapa | Ctrl+Shift+T | Meta+Shift+T | **nová**; jen v misi kampaně, s potvrzením |
| `map.declareIndependence` | Vyhlásit nezávislost | Mapa | Ctrl+Shift+I | Meta+Shift+I | **nová**; jen když je tlačítko nabídnuto, s potvrzením |
| `map.retire` | Odejít na odpočinek | Mapa | Ctrl+Shift+R | Meta+Shift+R | **nová**; jen když model odchod nabízí, s potvrzením |
| `map.nextUnit` | Čekat, další jednotka | Mapa | Tab | Tab | mimo plánovač tras |
| `map.skipUnit` | Vynechat tah jednotky | Mapa | Space | Space | při uvolnění |
| `map.move` | Hýbnout jednotkou | Mapa | M | M | |
| `map.confirmMove` | Potvrdit cíl pohybu | Mapa | Enter | Enter | jen v režimu pohybu |
| `map.goTo` | Plout do… | Mapa | G | G | |
| `map.goToTile` | Vybrat pole cíle přesunu | Mapa | Shift+G | Shift+G | **nová**; tlačítko Go to (výběr pole, zrušení rozkazu) |
| `map.buildColony` | Založit kolonii | Mapa | B | B | i v nabídce jednotky |
| `map.plow` | Zůrodnit pole | Mapa | P | P | u orající jednotky ruší práci (Zrušit práci) |
| `map.clearForest` | Vykácet les | Mapa | F | F | u kácející jednotky ruší práci |
| `map.buildRoad` | Postavit silnici | Mapa | Shift+R | Shift+R | **nová**; u stavějící jednotky ruší práci |
| `map.fortify` | Opevnit | Mapa | Shift+F | Shift+F | **nová** |
| `map.sentry` | Hlídkovat | Mapa | Shift+S | Shift+S | **nová**; u hlídky ji zruší (stejné tlačítko) |
| `map.disband` | Rozpustit jednotku | Mapa | Shift+D | Shift+D | **nová**; otevře stávající potvrzení |
| `map.jettison` | Vyhodit náklad | Mapa | Shift+J | Shift+J | **nová**; jen loď na moři, stávající volba zboží |
| `map.tradeRoutes` | Plánování obchodních tras | Mapa | Shift+T | Shift+T | **nová**; přepínač plánovače na mapě |
| `map.newTradeRoute` | Nová obchodní trasa | Mapa | Shift+N | Shift+N | **nová**; jen v plánovači tras |
| `map.center` | Najít vybrané místo | Mapa | C | C | |
| `map.colonies` | Kolonie | Mapa | I | I | |
| `map.europe` | Domovský přístav | Mapa | E | E | |
| `map.congress` | Kongres | Mapa | K | K | |
| `map.nativeSettlements` | Domorodá sídla | Mapa | N | N | |
| `map.statistics` | Statistiky | Mapa | R | R | |
| `map.turnReport` | Hlášení tahu | Mapa | T | T | |
| `map.goals` | Cíle | Mapa | O | O | |
| `map.help` | Colonizopedie | Mapa | H | H | |
| `map.scrollUp` / `Left` / `Down` / `Right` | Posun mapy | Mapa | W / A / S / D | W / A / S / D | držené |
| `europe.recruit` | Naverbovat v Evropě | Evropa | R | R | **nová**; tlačítko Naverbovat |
| `europe.buyUnit` | Koupit jednotku | Evropa | B | B | **nová** |
| `europe.repairs` | Opravy lodí | Evropa | P | P | **nová** |
| `europe.treasure` | Poklady | Evropa | T | T | **nová** |
| `europe.taxDemand` | Daňový požadavek koruny | Evropa | X | X | **nová**; jen při nevyřízeném požadavku |
| `europe.backTax` | Zaplatit dlužné daně | Evropa | Shift+X | Shift+X | **nová**; jen při bojkotu |
| `colony.construction` | Fronta staveb | Kolonie | Q | Q | **nová** |
| `colony.education` | Škola | Kolonie | E | E | **nová** |
| `colony.customHouse` | Vývoz celnice | Kolonie | X | X | **nová**; jen s celnicí |
| `colony.tradeRoute` | Obchodní trasy kolonie | Kolonie | R | R | **nová** |
| `colony.routeCargo` | Náklad obchodní trasy | Kolonie | C | C | **nová** |
| `colony.treasure` | Poklad kolonie | Kolonie | T | T | **nová**; jen vlastní kolonie |
| `editor.save` | Uložit upravenou mapu | Editor map | Ctrl+S | Meta+S, Ctrl+S | macOS zachovává dřívější Control |

V editoru map (R-0435) dále pevně platí: Escape odchází přes ochranu neuložených změn; po kliknutí do mapy šipky posouvají vybrané pole a Enter či mezerník na ně použije zvolený nástroj. Tyto klávesy nejsou konfigurovatelné akce.

Rozhodnutí 8. listopadu 2026:

1. Nové akce dostaly volné kombinace (včetně `Shift+G` a `Shift+T` doplněných po revizi) s jedním běžným modifikátorem, aby
   nekolidovaly s dosavadními písmeny: `R` zůstává Statistikám, `F` kácení,
   `S` posunu mapy. Ukončení tahu používá modifikátor, aby jej nespustil
   náhodný Enter.
2. Dřívější rozdíly v modifikátorech se sjednotily na přesnou shodu
   (dříve např. `Shift+R` otevíralo Statistiky a editor na macOS přijímal jen
   Control).
3. Nové akce spouštějí stejný ovládací prvek jako myš; jeho dostupnost,
   viditelnost, vysvětlení i potvrzovací dialog se nemění. Nedostupný příkaz
   nic neudělá; zrušení potvrzení nic nezmění.
4. Příkazy s vážným nebo nevratným následkem (Ukončit, Hlavní menu, Restart
   mise, Nezávislost, Odpočinek, Rozpustit, Vyhodit náklad, Ukončit tah)
   mají výchozí kombinaci s modifikátorem a vždy jdou přes svůj stávající
   potvrzovací tok.
5. Zahájení a zrušení stejné práce je jedna akce: Zůrodnit, Vykácet a
   Silnice spustí práci, nebo ji u pracující jednotky zruší; Opevnit, Hlídka
   a Go to přepínají stejné tlačítko. Samostatné akce „Zrušit…“ nevznikly.
6. Zkratku v popisku nově ukazují položky herního menu Nastavení, Hlavní
   menu, Restartovat misi a Ukončit a tlačítka Go to, Opevnit a Hlídka.
   Kompaktní popisky tlačítek Evropy, kolonie a hlavního menu se nemění;
   podle R-0345 bez plovoucích bublin ukazuje jejich aktuální kombinaci
   stavová lišta při najetí nebo zaměření (Evropa, kolonie: „vysvětlení (X)“)
   a přístupná nápověda tlačítek hlavního menu, které stavovou lištu nemá.
   Fronta staveb kolonie se tlačítkem obnovuje při každém překreslení a
   kombinaci ukazuje jen editor. Uložení nových zkratek se projeví při
   dalším najetí či zaměření.
7. Interpunkční klávesy se zobrazují symbolem (`Ctrl+,`, `⌘,`); profily
   a uložené odchylky dál používají název klávesy (`Ctrl+Comma`).

### Vědomé výjimky

| Ovládání | Důvod |
| --- | --- |
| Escape, Tab, Shift+Tab, Enter, Space a šipky v prvcích, formulářích, dialozích a nabídkách | pevné ovládání zaměřeného prvku (kapitola 5) |
| Zavřít / Zpět na obrazovkách Evropy, kolonie, nastavení a dalších | Escape a tlačítko Zpět; samostatná akce by duplikovala pevné Escape |
| Otevření herního menu | Escape na mapě; jeho položky mají vlastní akce |
| Výběr konkrétní jednotky na poli, práce v konkrétní kolonii, změna trasy konkrétního nosiče, položky seznamů a tabulek | volba konkrétní položky, ne obecný příkaz |
| Nákup a prodej zboží, výstroj, přesuny přetažením, rychlý nákup | myš, přetažení a modifikátory myši (kapitola 5) |
| Přejmenování kolonie | aktivace zaměřeného názvu Enter/Space |
| Minimapa a kamera kliknutím | navigační fokus a myš |
| Volby uvnitř dialogů (potvrdit, zrušit, záložky) | ovládání dialogu |

## 5. Pevné klávesy mimo editor

1. Escape vždy zavírá nabídky, dialogy a obrazovky, ruší plánování tras a
   nepotvrzený pohyb a na mapě otevírá herní menu. Nelze jej přiřadit; v editoru
   ruší zachytávání kombinace.
2. Tab, Shift+Tab, Enter, Space a šipky zachovávají běžnou funkci zaměřeného
   prvku a formulářů (navigace, potvrzení, výběr, posun minimapy a polí města).
   Herní Tab, Space a Enter na mapě se neuplatní při zaměřené horní liště,
   minimapě nebo přepínači plánovače.
3. Kontextová nabídka (`ContextMenu`, Shift+F10) a mezerník uvnitř otevřené
   nabídky jednotky zůstávají ovládáním nabídky.
4. Modifikátory myši, včetně rychlého nákupu v Evropě, výběru Alt+klik a
   Ctrl+klik jako pravého tlačítka, se nenastavují.
5. Psaní do textového pole, otevřený dialog, nabídka nebo souborová operace
   dál blokují zkratky hlavního okna jako dosud.

## 6. Editor v Nastavení

1. Tlačítko **Klávesové zkratky…** (`settingKeyboardShortcuts`) leží v Nastavení
   za rychlostí posunu mapy, v menu i ve hře. Otevře modální dialog.
2. Dialog zobrazí platformu výchozích hodnot a posuvný seznam akcí po
   kontextech. Každá kombinace je tlačítko pro změnu, vedle něj Odebrat;
   Přidat zachytí další kombinaci. Akce bez kombinace ukáže „Nepřiřazeno“.
3. Zachytávání pohltí všechny klávesy včetně jejich uvolnění, takže stará
   akce ani F3 neproběhne. Samotný modifikátor čeká na klávesu, Escape
   zachytávání zruší a dialog nezavře. Fokus se vrací na změněnou kombinaci
   nebo na Přidat.
4. Konflikt překrývajících se kontextů pojmenuje kombinaci a obě akce a
   zakáže Uložit a zpět. Stejná kombinace u jedné akce se nepřidá podruhé.
5. Obnovit výchozí změní jen koncept. Zrušit i Escape zahodí celý koncept
   včetně obnovení. Uložit a zpět nejdřív uloží odchylky a teprve potom novou
   konfiguraci použije všude; selhání uložení ponechá dialog, koncept i dosavadní
   aktivní zkratky a vyzve ke kontrole zápisu do složky nastavení (technický
   důvod jde do logu).
6. Popisky se zkratkou (nástrojová lišta a její stavové vysvětlení, nabídka
   jednotky, herní menu, tlačítko Načíst hru, Režim vývojáře, titulek Plout do…)
   se skládají z textu a aktuální první kombinace `text (zkratka)` a po uložení
   se obnoví bez restartu. Nepřiřazená akce popisek bez závorky.
7. Ovládací prvky dialogu jsou stávající ručně kreslená tlačítka (`ButtonIcon`);
   nová grafika nevznikla.

## 7. Kód

1. Balíček [`gui.shortcut`](../akropia-game/src/main/java/org/akropia/gui/shortcut/)
   vlastní akce, kontexty, profily a jedinou účinnou konfiguraci
   (`ShortcutController`, rozhraní `KeyboardShortcuts`). Obsluhy kláves volají
   `matches`/`find`; vlastní podmínky dostupnosti si ponechávají.
2. Odchylky čte a zapisuje `GamePreferences`; model ani AI na GUI nezávisí.
3. `ReleaseGesture` sleduje přijatý stisk akcí spouštěných uvolněním a
   `ShortcutInput` určuje klávesy patřící textovému poli.
4. Testy: `ShortcutControllerTest`, `KeyBindingTest`, `ReleaseGestureTest`,
   `ShortcutInputTest`, `WasdControllerTest`, `SessionShortcutTest`,
   `SettingServicePersistingTest`; hráčské scénáře
   `TC_104_F95_disband_test` TC_05 (Shift+D potvrzení a zrušení,
   nedostupné Shift+J, přemapování), `TC_46_F37_recruitment_test` TC_07
   (R v Evropě a přemapování), `TC_84_F75_framed_settings_test` TC_10–TC_14 (úpravy, zrušení a restart,
   čeština a úplné rozložení se snímky, textové pole s přemapovaným
   písmenem, selhání uložení) a `TC_191_F224_session_menu_test` TC_10.
