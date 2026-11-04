# Hlavní mapa: akce, vstupy a srovnání ovládání

Aktualizace 2026-11-04: vybrané změny této inventury implementuje
[R-0414](requirements/done/R-0414.md). Jednoduchý klik vybírá, dvojklik otevírá
vlastní město; zaměstnání se před ručním pohybem ruší výslovně v kontextové
nabídce. Rybář při zrušení zaplatí návrat do města. Pro tyto vstupy má přednost
[aktuální UX pravidlo](ux.md#map-selection-and-work) před historickým popisem níže.


Mapa potřebuje zpřístupnit město i každou jednotku na stejném políčku bez toho,
aby pouhý výběr vydal rozkaz. Dnešní Akropia má více vstupů, ale jejich význam
závisí na stavu jednotky a režimu mapy. Nejvýraznější je kolize otevření města
s ovládáním jednotky na jeho poli.

1. **Rozsah a datum:** průzkum 4. listopadu 2026, zdrojová revize
   [`953992ffa0fc`](https://github.com/jajir/microcol/commit/953992ffa0fcd50e07a09f267fe73cf099bc983d).
   „Současné“ níže znamená nalezené ve zdrojích, nikoli nově odehraný UI test.
   Hráčský nález nepřístupného města pochází od uživatele. Konkrétní save a stav
   jeho jednotky nebyly při tomto průzkumu reprodukovány.
2. **Výsledek:** inventura a podklad pro společný návrh; implementace se nemění.
   [R-0410](requirements/R-0410.md) zachycuje požadovaný výsledek jako `new`.
   Společné zásady nadále vlastní [UX pravidla](ux.md), pravidla akcí
   [katalog pravidel](gap-analysis.md) a [katalog UC](use-cases.md).
3. **Hranice:** rozlišujeme příkaz přímo v mapě, příkaz v její nabídce/panelu
   a přechod do obrazovky města či přístavu. To, že akce existuje v modelu
   nebo v kolonii, ještě neznamená, že má přímý vstup na hlavní mapě.

## Současné vstupy

| Vstup | Co dnes dělá | Podstatná podmínka nebo kolize |
| --- | --- | --- |
| Levé stisknutí políčka | Vybere pole; u vlastní jednotky s pohybem zkouší režim pohybu, jinak otevření vlastní kolonie | Výběr a zahájení rozkazu nejsou oddělené. Rozhoduje celé pole, ne samostatný zásah štítku města |
| Puštění levého tlačítka | V režimu pohybu dokončí volbu cíle; na výchozím poli zruší režim a zkusí otevřít kolonii | Proto nelze tvrdit, že každá jednotka vždy blokuje město; záleží, zda režim skutečně vznikl |
| Levé tažení jednotky na jiné pole | Náhled a dokončení pohybové akce v cíli | Podle cíle může jít i o útok, nalodění, vylodění nebo návštěvu; nejde o obecné přetažení libovolného objektu |
| Dvojklik v hlavní mapě | Nemá samostatnou větev obsluhy | Není zavedeným příkazem „otevřít město bez ohledu na jednotky“; dvě kliknutí nelze vydávat za spolehlivou alternativu |
| Pravý klik / Ctrl + levý klik | Nabídka vlastní jednotky na poli; případně podnabídka výběru dalších jednotek | Ruší nepotvrzený režim pohybu. Na poli bez vlastní jednotky neotevírá obecnou nabídku města/terénu. U vybraného rybáře přechází do jeho kolonie |
| Pravé tažení / tažení při kontextovém gestu | Posun mapy | Po rozpoznaném tažení se nabídka neotevře |
| Klávesa kontextové nabídky / Shift+F10 | Příkazy vybrané vlastní jednotky, když je zaměřené mapové plátno | Nejde o nabídku všech objektů pod kurzorem; fokus je důležitý |
| Klik na vlastní jednotku v pravém panelu | Vybere právě tuto jednotku pro další příkazy | Samotný výběr nemění rozkaz ani body; dostupný i pro vyčerpanou jednotku. Jiná vybraná jednotka ukončí nepotvrzený náhled |
| Tab | Čekat / vybrat další způsobilou jednotku | Zachová body. Vynechává vyčerpané jednotky, venkovní práci, rybáře, hlídku, opevnění a trvalé cesty/trasy; není to seznam všech jednotek |
| Mezerník | Vynechat tah vybrané jednotky | Spotřebuje její zbývající pohyb a přejde dál. Není totéž co Tab |
| M / příkaz Pohyb | Zapne pohyb vybrané jednotky; následuje cíl na mapě | Stávající dlouhodobý rozkaz může pohyb blokovat; UI nemá jeho rušení skrývat ve výběru |
| Enter | V režimu pohybu potvrdí pole pod kurzorem | Není univerzální „otevřít město“ ani samostatná plně klávesová navigace po cílových polích |
| G | Otevře výběr pojmenovaného cíle přesunu | Vlastní dostupná kolonie; u lodě také domovský přístav. Odlišné od příkazu vybrat libovolné pole pro dlouhodobý přesun |
| B / P / F | Založit kolonii / orat / vykácet les | Příkazy pro způsobilou jednotku. F zde neznamená opevnit; samostatnou zkratku pro silnici jsem v mapovém presenteru nenašel |
| I / nabídka Kolonie | Seznam vlastních kolonií → otevření vybrané kolonie | Již existující alternativní přístup i k městu s jednotkou |
| Escape | Zruší plánování tras; jinak nepotvrzený pohyb; v běžném režimu otevře herní menu | Zavření otevřeného menu/dialogu patří danému ovládacímu prvku; nesmí propadnout do dalšího příkazu |
| Hover | Popis terénu/objektů a v režimu pohybu náhled cíle | Informace se řídí známým/viditelným stavem, nemají odhalovat skryté objekty |
| Kolečko / trackpad, WASD | Posun hlavní mapy | Nejsou to směrové rozkazy jednotce; kolečko zde není zoom |
| C / Centrovat; Alt + levý klik | Centrovat výběr; při výběru pole požadovat plynulé posunutí | Alt+klik je stále součást původní výběrové/pohybové cesty |
| Klik/tažení minimapy; šipky při jejím fokusu | Posun pohledu | Nevydávají jednotce rozkaz |

Tab a mezerník mají výjimku při fokusu navigačních ovládacích prvků a v plánovači
tras; tam zůstává jejich navigační význam. Shift+Tab není implementovaný příkaz
„předchozí jednotka“. V prohlédnuté obsluze mapy není hromadný výběr obdélníkem,
Ctrl/Shift výběr více jednotek ani obecná fronta waypointů.

## Operace s jednotkou

Nabídka níže je kontextová nabídka na mapě; ne všechny její položky jsou pro
každý stav viditelné nebo povolené. Přesné podmínky a náklady patří modelu.

| Operace | Současný vstup z hlavní mapy | Dostupnost a návaznost |
| --- | --- | --- |
| Prohlédnout jednotku a její stav/náklad | Výběr pole, pravý panel, hover | Cizí jednotky lze pozorovat podle viditelnosti, ne jim vydávat příkazy |
| Vybrat konkrétní vlastní jednotku ve skupině | Řádek pravého panelu nebo podnabídka jednotek po pravém kliku | Důležité i pro jednotky vynechané Tabem |
| Přesun v aktuálním tahu | Levé tažení; M nebo Pohyb v nabídce → klik na cíl; v režimu pohybu také Enter | Náhled dostupných polí; neplatný přesun se odmítne |
| Trvalý přesun na mapové pole | Nabídka přesunu → výběr cíle na mapě | Postup během dalších vlastních tahů, informace o aktivní/pozastavené cestě |
| Přesun do pojmenované kolonie/přístavu | G nebo příslušná položka nabídky → výběrový dialog | U lodí nabídka respektuje dostupné přístavy a domovský přístav |
| Zrušit dlouhodobý přesun | Položka zrušení v nabídce | Výběr jednotky sám cestu nemaže; zrušení nevrací spotřebované body |
| Čekat a vrátit se k jednotce později | Tab nebo Čekat v nabídce | Body zachovány, vybere další jednotku |
| Vynechat zbytek tahu jednotky | Mezerník nebo položka nabídky | Zbývající pohyb se vyčerpá |
| Hlídka / aktivovat hlídku | Nabídka | Hlídka má také návaznost na automatické nalodění způsobilých cestujících |
| Opevnit / ukončit opevnění | Nabídka | Vlastní stav a omezení; F je vyhrazeno kácení |
| Založit kolonii | B nebo nabídka | Ověření podmínek, dialog názvu; při nulových bodech má B vlastní vysvětlení odmítnutí |
| Orat / zrušit orání | P nebo nabídka; zrušení v nabídce | Dlouhodobá práce, podmínky terénu/nástrojů, zobrazený postup |
| Vykácet les / zrušit kácení | F nebo nabídka; zrušení v nabídce | Samostatná práce s modelovými pravidly a následky |
| Postavit silnici / zrušit stavbu | Nabídka | Samostatná práce; R otevírá statistiky, ne silnici |
| Pracovat pro konkrétní kolonii na poli | Kontextové položky práce s názvem kolonie a produktem | Více přijímajících měst musí být rozlišeno; model určuje platná místa a výnos |
| Ukončit práci / vrátit se do přijímající kolonie | Nabídka venkovního pracovníka | Návrat a přerušení práce jsou rozdílné akce s podmínkami |
| Spravovat rybáře | Klik/kontext rybáře → jeho kolonie a vybraný pracovník | Zvláštní cesta; ovládání přes kolonii je již dohodnuté v R-0405 |
| Nalodit jednotku | Pohybový cíl na vhodného dopravce | Prohlédnutá větev bere prvního vhodného dopravce a prázdný slot; volba konkrétní lodi ve skupině zde není explicitním dialogem |
| Vylodit pasažéry na sousední břeh | Pohybový cíl lodi na vhodnou pevninu | Jeden způsobilý pasažér vystoupí přímo; více způsobilých otevře výběr checkboxy, zpočátku prázdný |
| Vstoupit do vlastního přístavu / odplout | Pohyb lodi, pojmenovaný cíl; dále přístavní obrazovka | Nakládání a vykládání patří především detailu kolonie/domovského přístavu |
| Odhodit zboží | Nabídka způsobilé lodi → dialog | Konkrétní náklad a potvrzení, nejde o zrušení výběru |
| Rozpustit jednotku | Nabídka → potvrzovací dialog | Nevratná akce, odmítnutí např. naloženého dopravce; zachovat vysvětlení následků |
| Zaútočit / dobýt nepřátelskou kolonii | Pohybový cíl na protivníka, podle pravidel a kontextu | Cíl pohybu může změnit význam na boj. Návštěvní dialog sídla má vlastní větev útoku |
| Prozkoumat území / navštívit nález | Pohyb do příslušného pole | Průzkum a nález jsou následkem pohybu; univerzální tlačítko automatického průzkumu v mapové nabídce nenalezeno |
| Navštívit domorodé sídlo | Pohybový cíl na známé sousední sídlo → nabídka návštěvy | Podle role/stavu náčelník, učení, obchod, tribut, misie včetně soupeřící misie, podněcování a útok |
| Obchodovat v cizí kolonii | Vhodný kontakt přes pohybový cíl → obchodní dialog | Diplomacie, náklad a další modelové podmínky; není to otevření plné správy cizího města |
| Plánovat/editovat obchodní trasu | Přepínač plánovače, výběr trasy v mapě, pravý panel, Nová trasa | Samostatný režim: mapový klik vybírá trasu, neposouvá jednotku ani neotevírá město. Přiřazení dopravce a nákladové operace navazují na správu tras/kolonie |
| Změnit vybavení/roli, práci v budově, náklad, výuku | Otevřít kolonii nebo domovský přístav → jeho ovládání | Nezaměňovat existenci těchto akcí za přímé mapové příkazy |

## Operace s městem

| Operace | Současný vstup | Přímo na hlavní mapě? |
| --- | --- | --- |
| Zjistit název, vlastníka a známý stav | Hover/výběr pole a informace pravého panelu | Ano, podle viditelnosti; historické pozorování není živý stav |
| Otevřít vlastní město | Klik na pole s výše popsanými kolizemi; Kolonie / I → konkrétní město | Ano, jako přechod do správy města |
| Vybrat jednotku stojící ve městě | Pravý panel nebo kontextová podnabídka jednotek | Ano; nezaměňovat s pracovníkem uvnitř budovy či vnitřní posádkou |
| Zadat práci na okolním poli pro konkrétní město | Kontext venkovního pracovníka | Ano; správa rybáře přechází do jeho kolonie |
| Přejmenovat město | Otevřít město → jeho název → dialog; zaměřený název také Enter/mezerník | Ne jako samostatný příkaz hlavní mapy |
| Přidělovat práci uvnitř města, měnit výrobu a stavební frontu | Otevřít město → jeho pracovní místa/budovy a příslušné dialogy | V detailu kolonie; mapová pole nejsou univerzální náhradou těchto ovládacích prvků |
| Spravovat náklad, vybavení, sklad a přístav | Otevřít město → dok, sklad, jednotky a jejich nabídky | V detailu kolonie; přímé nalodění/vylodění na mapě je jiná cesta |
| Spravovat vzdělávání, exporty a poklady | Otevřít město → příslušné tlačítko/dialog | V detailu kolonie |
| Spravovat obchodní trasy | Mapový plánovač nebo správa tras z kolonie | Dvě vstupní cesty; mapový plánovač má vlastní režim |
| Obchodovat/bojovat s cizím městem, navštívit domorodé sídlo | Vybraná způsobilá jednotka → cílové pole → odpovídající interakce | Ano; není to oprávnění spravovat cizí město |
| Vrátit se ze správy města | Zavřít/Zpět nebo Escape v obrazovce kolonie | Návrat do mapy; oddělit od rušení mapového příkazu |

Samostatné mapové příkazy pro přejmenování, stavební frontu či opuštění města
nejsou v prohlédnuté obsluze hlavní mapy. Jejich případné přidání je nové
návrhové rozhodnutí, nikoli už hotová alternativa kliknutí.

## Druhy jednotek a stavy, které musí návrh pokrýt

Ve zdrojích je **35 typů**. Typ, aktuální role/vybavení, umístění a rozkaz jsou
různé rozměry; například expert rybář nemusí právě rybařit a voják není vždy
samostatný základní typ. Matice návrhu proto musí pracovat se schopnostmi a stavem.

| Rodina | Typy ve zdrojích | Specifika ovládání |
| --- | --- | --- |
| Běžní kolonisté (3) | COLONIST, INDENTURED_SERVANT, PETTY_CRIMINAL | Pohyb, práce, založení, vybavení, přeprava podle pravidel |
| Odborníci (22) | EXPERT_ORE_MINER, EXPERT_FARMER, EXPERT_FISHERMAN, EXPERT_CLAY_DIGGER, EXPERT_SILVER_MINER, EXPERT_LUMBERJACK; MASTER_OLIVE_GROWER, MASTER_SHEPHERD, MASTER_GRAPE_GROWER, MASTER_CARPENTER, MASTER_OIL_MAKER, MASTER_WEAVER, MASTER_WINEMAKER, MASTER_POTTER, MASTER_WEAPONSMITH, MASTER_BLACKSMITH; FIREBRAND_PREACHER, ELDER_STATESMAN, SEASONED_SCOUT, HARDY_PIONEER, VETERAN_SOLDIER, JESUIT_MISSIONARY | Stejný základ výběru a pohybu; specializované akce odvozené od role, výstroje, práce a cíle |
| Pozemní zvláštní jednotky (3) | WAGON, TREASURE_TRAIN, ARTILLERY | Náklad/trasy, transport pokladu, boj; nepřidělovat automaticky kolonistické příkazy |
| Lodě (6) | FRIGATE, GALLEON, CARAVEL, MERCHANTMAN, PRIVATEER, MAN_OF_WAR | Přístavy, náklad/pasažéři, plavba, případně boj podle schopností |
| Domorodá jednotka (1) | BRAVE | V běžné lidské koloniální hře pozorování nebo interakce jako s cizí jednotkou; vlastnictví rozhoduje o ovladatelnosti |

1. U každé použitelné rodiny zahrnout: samostatnou jednotku, více vlastních
   jednotek, vlastní město pod nimi, dostupné i vyčerpané body, trvalou cestu,
   hlídku/opevnění/práci a neplatný cíl.
2. Zvlášť ověřit venkovního pracovníka, rybáře s vlastní obrannou lodí na poli,
   pasažéra v dopravci, jednotku v budově/vnitřní posádce a loď na plavbě/v opravě.
   Ne všechny tyto objekty jsou přímo volitelné na světové mapě; potřebují
   srozumitelnou cestu přes svůj kontejner, kolonii nebo přístav.
3. Zahrnout cizí a naposledy pozorované objekty, tah AI, probíhající animaci,
   otevřenou nabídku/dialog, fokus klávesnice a aktivní plánovač tras.

## Proč může jednotka zablokovat otevření města

1. `GamePanelMouseListener.onMousePressed` nejprve vybere pole. Potom
   `tryToSwitchToMoveMode` zjišťuje vlastní jednotky s kladným pohybem.
   Pokud nějakou najde, vrátí `true` po odeslání události pohybu.
   Přímé otevření města se pak přeskočí.
2. `GamePanelPresenter.swithToMoveMode` ale může režim odmítnout: například
   při aktivním go-to, hlídce nebo opevnění. Volající přesto považuje
   kliknutí za obsloužené. Při následném puštění tlačítka už není aktivní
   režim, ze kterého by se otevření města vyvolalo. To je konkrétní zdrojově
   doložená cesta ke kolizi, nikoli prokázání stavu uživatelovy partie.
3. U běžné pohyblivé jednotky režim vznikne. Puštění na stejném poli vede
   přes `switchToNormalMode` k `tryToOpenColonyDetail`, tedy město může otevřít.
   Při nulovém pohybu všech vlastních jednotek může být dostupná přímá větev.
   Význam kliknutí se tím mění podle rozkazu a bodů.
4. Vlastní město je i dnes dostupné přes **Kolonie / I**. To je alternativa,
   ne odstranění nekonzistence přímého mapového přístupu. Nabídka jednotky
   neobsahuje univerzální položku otevření města na stejném poli a městský
   název není v této obsluze samostatný klikací cíl.
5. Oprava musí prověřit stisk, tažení, puštění, případný dvojklik a přepnutí
   režimu společně. Pouhé přidání druhého kliknutí bez pravidla precedence
   může nejdříve vydat rozkaz a teprve potom otevřít město.

## Co používají srovnatelné hry

Jde o doložené desktopové vzory z manuálů, ne o osobní test aktuálních sestavení.
Rozsah srovnání je záměrně konkrétní: překryv města a jednotky, více cest ke stejné
akci a plánování opakované práce. Neověřené zkratky jiných her nepřebíráme.

| Hra a podklad | Doložené ovládání | Co z něj vyplývá pro návrh Akropie |
| --- | --- | --- |
| **FreeCol**, User Guide v0.11.6, §3.3.4–3.3.6 | Klik vybere jednotku; kolonii otevře klik, při aktivní jednotce na stejném poli dvojklik. Pravá nabídka zpřístupní jednotky na poli a přesun vybrané jednotky na cíl. Jednotkové příkazy mají tlačítka, menu Orders i zkratky. Minimapou lze klikat i táhnout | Nejbližší srovnání: existuje explicitní řešení přesně naší kolize. Dvojklik ale potřebuje viditelnou alternativu pro hráče, který o něm neví |
| **Civilization VI**, oficiální PC manuál, s. 69 a 94 | Vybraná jednotka dostane cíl pravým klikem; alternativa je Move Mode v panelu a následný levý klik. Klik na městský banner otevírá městskou obrazovku; cizí banner vede do diplomacie | Oddělený cíl pro město a více vstupů do stejného přesunu. Převzetí pravého kliku pro okamžitý pohyb by v Akropii kolidovalo s již existující kontextovou nabídkou |
| **Old World**, oficiální manuál v1.65, s. 110–111 | U pracovníka lze pomocí Shift + pravého kliku a zadávání vylepšení sestavit řetězec waypointů a prací | Pokročilé plánování lze přidat jako rozšíření běžné akce. Není nutné jím zatížit základní výběr; jde o možný pozdější rozsah, nikoli požadavek nynější implementace |

Zdroje:

1. [FreeCol User Guide, Main Map a Unit Buttons](https://freecol.sourceforge.io/docs/FreeCol.html), vydání manuálu 30. prosince 2019, přečteno 4. listopadu 2026.
2. [Civilization VI, oficiální manuál](https://downloads.2kgames.com/civilization/vi/manuals/eu/CIV_VI_25TH_ONLINE_MANUAL_ENG.pdf), kapitoly Movement a The City Banner / The City Screen, přečtené dostupné indexované pasáže 4. listopadu 2026. Úplné PDF přes webový nástroj překročilo velikostní limit.
3. [Old World, oficiální manuál v1.65](https://shared.steamstatic.com/store_item_assets/steam/apps/597180/manuals/bdd2ff370f17bfe9ecb09483be0b8b22f5e62efe/Old_World-Official_User_Manual.pdf), pasáž semi-automated workers, přečteno 4. listopadu 2026.

## Okrajové scénáře v jiných hrách — doplnění 4. listopadu 2026

Uživatel připomněl probuzení opevněné jednotky dvojklikem ve FreeCol. Následující
průzkum rozlišuje **výběr pro zobrazení**, **aktivaci/probuzení**, **zrušení rozkazu**
a **ztrátu obranného účinku**. Nejsou to automaticky stejné operace. Jde o čtení
primárních zdrojů, nikoli o nově odehrané testy cizích her.

### FreeCol: vzpomínka na dvojklik a rozdíly verzí

1. **Vzpomínku se nepodařilo potvrdit jako obecné pravidlo mapy.** Ve verzi
   0.11.6 obsluha dvojkliku otevírá kolonii; nemá zvláštní větev „opevněná
   jednotka → aktivní“. To nevylučuje jinou verzi, fork nebo jiný panel.
   [Zdroj v0.11.6, CanvasMouseListener](https://github.com/FreeCol/freecol/blob/2410226fe6240387dc1ac39e36fd196243d369e0/src/net/sf/freecol/client/gui/CanvasMouseListener.java).
2. **Dnešní prohlédnutý vývojový kód se liší od starého manuálu.** Na revizi
   `85f3eb9a20fa` má v běžné klikací větvi sídlo přednost před jednotkou.
   Mimo sídlo opakované kliknutí na vlastní skupinu přepíná jednotky.
   Výběr nastavuje jednotku v UI, sám nenastavuje herní stav `ACTIVE`.
   [SwingGUI.clickAt/changeView](https://github.com/FreeCol/freecol/blob/85f3eb9a20fa2cfb070601e268382fc2acf0ad70/src/net/sf/freecol/client/gui/SwingGUI.java)
   a [MapViewerState.setActiveUnit](https://github.com/FreeCol/freecol/blob/85f3eb9a20fa2cfb070601e268382fc2acf0ad70/src/net/sf/freecol/client/gui/mapviewer/MapViewerState.java).
3. Kontextová nabídka obsahuje explicitní aktivaci, zrušení cesty, kolonii
   a seznam jednotek. **Aktivovat všechny** volá zrušení rozkazů pro jednotky
   na poli; nejde jen o jejich prohlédnutí. Přerušení rozpracovaného vylepšení
   má potvrzení. [TilePopup](https://github.com/FreeCol/freecol/blob/85f3eb9a20fa2cfb070601e268382fc2acf0ad70/src/net/sf/freecol/client/gui/TilePopup.java)
   a [InGameController.clearOrders](https://github.com/FreeCol/freecol/blob/85f3eb9a20fa2cfb070601e268382fc2acf0ad70/src/net/sf/freecol/client/control/InGameController.java).
4. Pro Akropii z toho vyplývá potřeba jednoznačného označení aktivace a jejího
   rozsahu. Manuál v předchozí tabulce zůstává historickým dokladem, nikoli
   popisem všech vydání FreeCol. Probuzení dvojklikem ponecháváme jako možný
   návrh; nenahrazuje přijaté pravidlo bezpečného výběru v pravém panelu.

### Doložené vzory dalších her

| Hra / situace | Doložený postup | Poučení pro Akropii, zatím návrh |
| --- | --- | --- |
| **Civilization III: opevněná jednotka sama, ve skupině, ve městě** | Samostatná se aktivuje klikem. U skupiny pravý klik otevře seznam a hráč vybere jednotku. Posádka města je dostupná přes pravou nabídku města nebo jeho obrazovku | Přístup ke každé jednotce nesmí záviset na tom, která je nakreslená navrchu |
| **Civilization III: čekání a přerušená cesta** | W/Tab odloží jednotku na konec fronty; mezerník ji podrží tento tah. Útok nebo překážka na go-to cestě jednotku znovu aktivuje | Odlišit dočasné odložení, konec činnosti v tahu a návrat jednotky, která potřebuje rozhodnutí |
| **Civilization IV: Colonization: jednotka mimo automatickou frontu** | Automatický výběr přeskakuje opevněné, spící, přepravované a jednotky provádějící dřívější rozkazy. Klikem lze vybrat i jednotku s předchozím rozkazem | Automatická fronta nemůže být jediným přístupem k jednotkám |
| **Civilization IV: Colonization: skupinové rozkazy a hlídka** | Panel má samostatné přesuny všech jednotek na poli a všech stejného typu. Sentry reaguje na nepřítele v sousedství; sleep čeká na aktivaci | Hromadný rozsah označit příkazem; z dvojkliku na jednoho vojáka nesmí nenápadně vzniknout rozkaz celé posádce |
| **Freeciv21: hlídky a výběr ve skupině** | Manuál zkratek uvádí Ctrl+prostřední tlačítko pro probuzení hlídek, Ctrl+Shift+D pro všechny hlídky na poli a Ctrl+Space pro výběr jednotky ze skupiny pod kurzorem | Tyto akce mají smysl, ale uvedené kombinace nejsou vhodný univerzální základ pro tři platformy |
| **Freeciv21: nákup bez otevření města** | Tlačítko Back na myši je uvedeno pro okamžité urychlení nákupu výroby | Vedlejší tlačítko s okamžitým utracením prostředků bychom neměli kopírovat jako výchozí ovládání |
| **Battle for Wesnoth: přerušený pohyb a trvalé držení pozice** | Projektová wiki rozlišuje T pro pokračování přerušeného pohybu, Space pro konec tahu jednotky a Shift+Space pro trvalé držení pozice; N/Shift+N pro další/předchozí jednotku | Užitečné jsou odlišné příkazy pro pokračování, čekání a trvalý stav, ne jeden neurčitý „aktivní mód“ |
| **Battle for Wesnoth: oprava omylu a zkratky** | Wiki uvádí vrácení pouze deterministických pohybů a možnost většinu zkratek přenastavit/vypnout; pro Mac dokumentuje odlišnosti | Nenabízet obecné Undo tam, kde už pohyb odkryl nové informace nebo spustil náhodný výsledek; zkratky mají mít nastavení |

Primární podklady, přečtené 4. listopadu 2026:

1. [Civilization III, dodaný manuál](https://cdn.akamai.steamstatic.com/steam/apps/3910/manuals/manual.pdf), části Fortified Units, GoTo Orders, Hold, Sentry a Wait Orders. Historický desktopový vzor, ne tvrzení o jiné generaci Civilization.
2. [Civilization IV: Colonization, dodaný manuál](https://cdn.akamai.steamstatic.com/steam/apps/16810/manuals/manual_english.pdf), části Selecting Units a Unit Orders. Nezaměňovat s FreeCol ani původní Colonization.
3. [Freeciv21, Game Shortcut Options](https://longturn.readthedocs.io/en/stable/Manuals/Game/shortcut-options.html). Jde o klient Freeciv21, nikoli závazný popis všech klientů Freeciv.
4. [Battle for Wesnoth, projektová wiki HotKeysSystem](https://wiki.wesnoth.org/HotKeysSystem). Stránka nefixuje verzi sestavení; její seznam je doklad návrhových vzorů, ne příslib shodných výchozích kláves v každé aktuální instalaci.

## Jak složité zkratky používat na macOS, Linuxu a Windows

**Doporučení pro Akropii:** základní hra musí být dosažitelná viditelnými prvky
a běžným klikáním. Časté mapové akce mají jednu klávesu; rozšíření nejvýše jeden
modifikátor. Složitější kombinace ponechat jen jako volitelnou zkratku zkušeného
hráče. Nejde o nově přijaté mapování ani o již implementované nastavení zkratek.

| Úroveň | Doporučené použití | Příklad / hranice |
| --- | --- | --- |
| Běžný vstup | Klik, pravý klik, viditelná nabídka; samostatná klávesa při fokusu mapy | Zachovat M/B/P/F/G/I podle dosavadních významů. Neaktivovat herní příkazy při psaní názvu nebo v dialogu |
| Jedna rozšiřující klávesa | Shift + klávesa nebo zřetelně vysvětlené Shift + klik | Např. předchozí místo dalšího, nebo přidání do výběru. Konkrétní přiřazení se teprve vybírá |
| Standardní aplikační zkratka | Command na macOS, Ctrl na Windows/Linuxu | Uložit / otevřít přes ⌘S/⌘O a Ctrl+S/Ctrl+O. Není to pokyn mechanicky zaměnit každý Ctrl v herních gestech za Command |
| Dva modifikátory | Jen výjimečně, s viditelnou alternativou a možností změny | Nepoužívat jako základní cestu k městu, jednotce nebo probuzení. Standardní aplikační konvence mohou být výjimkou |
| Tři modifikátory, složitá tlačítka myši, načasovaná kombinace | Ne jako výchozí herní ovládání | Žádné povinné Ctrl+Alt+Shift+klik, trojklik nebo prostřední tlačítko. Dvojklik a tažení mohou být pohodlné alternativy, nikdy jediná cesta |

### Konkrétní konflikty platforem

| Prostředí | Co respektovat | Důsledek pro návrh |
| --- | --- | --- |
| macOS: myš a trackpad | Control+klik znamená kontextovou nabídku; sekundární klik lze dělat trackpadem | Nepoužít Ctrl+levý klik pro jinou zásadní akci, např. okamžitý útok či přidání do skupiny. Nabídnout i klávesovou/viditelnou cestu k příkazům |
| macOS: systém a aplikace | ⌘Tab přepíná aplikace, ⌘Space vyvolává systémovou funkci; Control+Space může přepínat vstupní zdroj. ⌘Q/⌘W/⌘H/⌘M mají běžné aplikační významy | Nezabírat tyto kombinace herními příkazy. Zejména nepřebírat Ctrl+Space z jiné hry jako univerzální výběr skupiny |
| MacBook a další notebooky | F1–F12 mohou vyžadovat Fn nebo změnu nastavení; numerický blok a samostatná klávesa Menu nemusí být přítomné | F-klávesy, Shift+F10 i numerický pohyb jen jako alternativy; důležitá akce má další dostupný vstup |
| Linux / GNOME a KDE | GNOME používá Super+tažení pro okno; KDE umožňuje Meta nebo Alt jako modifikátor okenních akcí | Alt/Meta+tažení není spolehlivá herní zkratka napříč desktopy. Nevyžadovat změnu správce oken |
| Windows | Alt+Tab, Alt+F4, Alt+Space a kombinace Win mají systémový význam; Shift+F10 je konvence kontextové nabídky | Zachovat systémové ovládání a kontextovou nabídku. Windows klávesu nevolit jako výchozí herní modifikátor |
| Mezinárodní klávesnice | Ctrl+Alt může ve Windows znamenat AltGr a tvorbu znaku; rozložení mění dostupnost interpunkce i čísel | Vyhnout se Ctrl+Alt+znak a povinné US interpunkci. Ověřit CZ QWERTZ i US QWERTY; zachovat psaní Unicode a kompozici textu |

Podklady k systémovým faktům:

1. [Apple: sekundární klik](https://support.apple.com/en-gb/guide/mac-help/mh35853/mac), dostupná indexovaná pasáž; úplná stránka při průzkumu vracela HTTP 429. [Apple: klávesové zkratky](https://support.apple.com/en-us/102650) dokládá Command, vstupní zdroje a chování F-kláves.
2. [GNOME: přesun oken](https://help.gnome.org/gnome-help/shell-windows-states.html) a [KDE: Window Behavior](https://docs.kde.org/trunk_kf6/en/kwin/kcontrol/windowbehaviour/index.html), nastavitelný modifikátor Meta/Alt. Linux není jediné pevné mapování kláves.
3. [Microsoft: Windows keyboard shortcuts](https://support.microsoft.com/en-us/accessibility/windows/keyboard-shortcuts-in-windows) a [Microsoft: proč nepoužívat Ctrl+Alt jako aplikační zkratku](https://devblogs.microsoft.com/oldnewthing/20040329-00/?p=40003), vysvětlení AltGr.

### Navržený kontrakt aktivace a ověření

1. **Klik pouze vybere.** U opevněné jednotky zůstane vidět stav a samostatný
   příkaz „Ukončit opevnění“, u hlídky „Aktivovat“, u cesty „Zrušit přesun“.
   Název musí odpovídat konkrétnímu následku. Hromadné zrušení rozkazů není
   totéž co probuzení jedné jednotky.
2. **Dvojklik na probuzení zatím nedoporučuji jako výchozí pravidlo celého
   políčka.** Koliduje s městem a skupinou. Pokud jej vybereme jako doplněk,
   musí cílit na jednoznačný symbol/řádek jednotky, mít stejnou viditelnou akci
   v menu a nikdy současně otevřít město, pohnout nebo probudit všechny.
3. Aktivace nesmí doplnit pohybové body. Zda a kdy končí obranný bonus, je
   otázka herního pravidla, ne vedlejší efekt volby tlačítka. Průzkum jiných
   her sám nemění současný modelový kontrakt Akropie.
4. Zobrazovat skutečnou zkratku v nabídce, umožnit její změnu/vypnutí a obnovu
   výchozího nastavení. Kontrolovat kolize ve stejném kontextu a přesnou sadu
   modifikátorů: například ⌘M nebo Ctrl+M nesmí nechtěně projít jako prosté M.
   Konflikty zachycené správcem oken nelze vyřešit jen interní kontrolou hry.
5. Testovací matice: jedna jednotka / skupina / město; volná / vyčerpaná /
   hlídka / opevnění / cesta / práce; myš / trackpad / klávesnice; fokus
   mapy / panelu / nabídky / textového pole; CZ/US rozložení. Ověřit první i
   druhé kliknutí, toleranci tažení, držení klávesy, ztrátu fokusu a uvolnění
   modifikátoru během gesta. Jedno gesto vykoná nejvýše jeden příkaz.
6. Při budoucí implementaci zachovat běžné model/JUnit a Docker/Linux UI
   ověření podle [funkčního kontraktu](functional-tests.md). Logiku volby
   platformy a modifikátorů pokrýt modelovými/JUnit testy; nativní macOS
   zůstává pouze výslovně vyžádanou manuální kontrolou. Doporučení pro tři
   platformy není prohlášením, že bylo nyní na třech systémech otestováno.

## Doporučení k dalšímu návrhu, zatím neschválené

1. **Nejprve definovat akci a objekt, potom gesta.** Jeden příkaz má jedinou
   sadu podmínek, nákladů, náhledu a následků. Menu, klávesa a tažení musí
   používat stejnou operaci. Dva vstupy nesmějí znamenat dva odlišné výpočty.
2. **Výběr bez vedlejších účinků.** Klik na jednotku nebo její řádek vybírá.
   Nezruší práci, hlídku či trasu. Výběr jiné jednotky odstraní nepotvrzený
   náhled, ne již zadaný trvalý rozkaz. Tento princip již používá pravý panel.
3. **Město musí mít vlastní cestu nezávislou na posádce.** Kandidátem je klikací
   název/štítek města a explicitní „Otevřít město“ v kontextu políčka. Dvojklik
   může být další zkratka, nikoli jediná záchrana. Kolonie / I zůstává užitečný
   seznam pro vzdálená města. Je potřeba rozhodnout přesnou oblast zásahu a
   prioritu při výběru cíle pohybu.
4. **Kontext má ukázat dostupné objekty a činnosti.** Pole může obsahovat město,
   několik jednotek, pracovníka i dopravce. Hráč musí vědět, koho vybral,
   a umět přepnout bez nechtěného rozkazu. Již zavedené pravé menu je vhodný
   základ; pravý klik současně nesmí bez varování znamenat okamžitý útok.
5. **Pohyb dvěma rovnocennými cestami.** Zachovat kandidáty tažení a
   „Pohyb → cíl“ pro hráče, který nechce táhnout. Před potvrzením ukázat
   konkrétní výsledek: přesun, útok, nalodění na pojmenovanou loď,
   vylodění vybraných pasažérů nebo návštěva. Zrušení nespotřebuje prostředky.
6. **Tab není univerzální prohlížeč.** Zachovat rozdíl Čekat a Vynechat tah.
   K jednotkám mimo frontu musí vést panel/seznam; případný příkaz „další
   jednotka na tomto poli“ nebo „předchozí“ nesmí tiše změnit význam Tabu.
7. **Rybář a pracovníci zachovají přijatá pravidla.** Nový návrh zpřehlední
   jejich objevení a přístup, ale bez další dohody nepřepíše správu rybáře přes
   kolonii, přijímající město ani pohybové podmínky.
8. **Kvalitu měřit úkoly.** U všech variant „město + jednotky“ musí jít město
   otevřít a konkrétní jednotku vybrat bez spotřeby bodů. Porovnat úspěšnost
   prvního pokusu, nechtěné rozkazy, počet akcí a čas nalezení příkazu u myši
   a klávesnice. Označení „nejlepší ovládání“ musí vycházet z pozorování hráčů,
   nikoli jen z množství gest.

## Podklady a navazující ověření

1. [Obsluha myši](https://github.com/jajir/microcol/blob/953992ffa0fcd50e07a09f267fe73cf099bc983d/akropia-game/src/main/java/org/akropia/gui/screen/game/gamepanel/GamePanelMouseListener.java),
   [význam cíle a otevření kolonie](https://github.com/jajir/microcol/blob/953992ffa0fcd50e07a09f267fe73cf099bc983d/akropia-game/src/main/java/org/akropia/gui/screen/game/gamepanel/GamePanelPresenter.java),
   [klávesy mapy](https://github.com/jajir/microcol/blob/953992ffa0fcd50e07a09f267fe73cf099bc983d/akropia-game/src/main/java/org/akropia/gui/screen/game/ScreenGamePresenter.java)
   a [fokus Tab/Space](https://github.com/jajir/microcol/blob/953992ffa0fcd50e07a09f267fe73cf099bc983d/akropia-game/src/main/java/org/akropia/gui/screen/game/ScreenGame.java).
2. [Příkazy a nabídka jednotky](https://github.com/jajir/microcol/blob/953992ffa0fcd50e07a09f267fe73cf099bc983d/akropia-game/src/main/java/org/akropia/gui/screen/game/components/ButtonsGamePanel.java),
   [jejich dostupnost](https://github.com/jajir/microcol/blob/953992ffa0fcd50e07a09f267fe73cf099bc983d/akropia-game/src/main/java/org/akropia/gui/screen/game/components/ButtonsGamePanelController.java),
   [seznam měst](https://github.com/jajir/microcol/blob/953992ffa0fcd50e07a09f267fe73cf099bc983d/akropia-game/src/main/java/org/akropia/gui/screen/game/components/ColoniesMenu.java),
   [výběr v pravém panelu](https://github.com/jajir/microcol/blob/953992ffa0fcd50e07a09f267fe73cf099bc983d/akropia-game/src/main/java/org/akropia/gui/UnitsPanel.java).
3. [Typy jednotek](https://github.com/jajir/microcol/blob/953992ffa0fcd50e07a09f267fe73cf099bc983d/akropia-game/src/main/java/org/akropia/model/UnitType.java),
   [fronta způsobilých jednotek](https://github.com/jajir/microcol/blob/953992ffa0fcd50e07a09f267fe73cf099bc983d/akropia-game/src/main/java/org/akropia/model/UnitStorage.java).
   Pro implementaci dodržet [odpovědnosti balíčků](architecture.md).
4. Existující scénáře k opětovnému využití: `TC_11_F11_skip_to_next_unit_test`
   (včetně `TC_05_unit_context_keeps_global_controls_and_keyboard_orders`),
   `TC_103_F94_persistent_go_to_test`, `TC_45_F36_sentry_boarding_test`,
   `TC_61_F52_fortification_test`, `TC_201_F233_world_workers_test`,
   `TC_202_F234_local_fishing_test`, `TC_116_F107_trade_route_creation_test`
   a `TC_196_F228_world_minimap_test`. To je mapa návazností, nikoli tvrzení
   o novém průchodu testů nebo úplné testovací pokrytí tohoto návrhu.
5. Před implementací doplnit konkrétní scénáře kolonie s běžnou jednotkou,
   go-to, hlídkou a opevněním, vyčerpané jednotky ve skupině a dvojice rybář/loď.
   Ověřit myš i klávesnici, zrušení, nechtěné vydání rozkazu při dvojkliku,
   více dopravců a výběr při změně režimu. Výsledky patří do
   [funkční evidence](functional-tests.md), dokumentační přejímka do
   [publikační evidence](publishing.md).
