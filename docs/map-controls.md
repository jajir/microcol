# Hlavní mapa: akce, vstupy a srovnání ovládání

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
