# UX a pravidla ovládání MicroCol

MicroCol má zachovat strategickou hloubku a současně hráči pomáhat pochopit jeho možnosti, ovládání a důsledky rozhodnutí. Při nové funkci proto řešíme také to, kde ji hráč najde, jak ji použije a jak pozná výsledek.

Tato stránka je společným místem pro pravidla UX, popis současného ovládání a zjištění z jeho ověřování. Navazuje na [grafické zadání](graphics.md#soucasne-zadani-vzhledu) a [pravidla zvukové odezvy](audio.md). Platná pracovní dohoda z **11. října 2026**. Technická pozorování níže vycházejí z kódu na větvi `develop`, revize `77f75eb03c74f07ad0253595d14fb0b31ac6e78a`; nejsou výsledkem pozorování hráčů.

## Společná pravidla pro další změny

| Pravidlo | Jak se projeví ve hře |
| --- | --- |
| Hráč pozná aktuální stav. | Je zřejmé, která jednotka či kolonie je vybraná, kdo je na tahu, kolik zbývá pohybu a zda se právě zadává nebo provádí příkaz. Výběr a provedení akce jsou rozlišitelné. |
| Důsledky jsou srozumitelné před akcí. | U nákupu ukázat cenu a podmínky, u změny práce dostupnou informaci o očekávané produkci. U nejistých výsledků vyjádřit nejistotu; neprozrazovat skryté herní informace. |
| Nedostupná akce má dohledatelný důvod. | V relevantním kontextu vysvětlit například chybějící nástroje, peníze či pohyb. Pouhé zmizení nebo zešednutí ovládacího prvku nemusí hráči vysvětlit, co má změnit. |
| Stejné činnosti mají stejné ovládání. | Sjednotit význam kliknutí, přetažení, kontextového menu, potvrzení a zrušení. Zachovávat názvy, ikony a umístění běžných akcí napříč obrazovkami. |
| Klíčové možnosti lze objevit. | Důležitá činnost má mít viditelnou cestu nebo nápovědu v místě použití. Klávesové zkratky a gesta pomáhají zkušeným hráčům, ale jejich znalost nemá být jedinou cestou k základním akcím. |
| Rozpracovanou akci lze opustit. | Před provedením příkazu nabídnout jasné zrušení bez vedlejších změn. U závažných nevratných akcí vysvětlit důsledky a použít přiměřené potvrzení; běžné opakované kroky nemají vyžadovat zbytečné dialogy. Vrácení již provedené herní akce zavádět jen v souladu s pravidly hry. |
| Odezva odpovídá výsledku a důležitosti. | Potvrdit skutečně dokončenou akci, chybu vysvětlit včetně dalšího možného kroku. Hrozící hladomor odlišit od běžného příjezdu lodi. Přerušující dialog používat tam, kde je pozornost nebo rozhodnutí hráče potřebné. |
| Informace je čitelná a dostupná více způsoby. | Důležitý význam nesmí záviset pouze na zvuku nebo barvě. Držet kontrast, čitelné texty a viditelné klávesové zaměření. U měněné obrazovky prověřit relevantní velikosti okna, ovládání klávesnicí a délky lokalizovaných textů. |
| Nápověda zůstává dostupná. | Úvodní výuka může naučit základ; zapomenutý postup má jít později dohledat bez opakování celé výuky. Texty používají názvy známé hráči, nikoli interní názvy tříd a událostí. |

Pravidla vycházejí také z [principů použitelnosti NN/g](https://www.nngroup.com/articles/ten-usability-heuristics/) a herních [doporučení k vizuální a zvukové odezvě](https://learn.microsoft.com/en-us/xbox/accessibility/xbox-accessibility-guidelines/103). Konkrétní způsob jejich použití se řídí kontextem MicroCol a zjištěními z hraní.

### Kdy akci skrýt a kdy vysvětlit její nedostupnost

Akci, která se k vybranému objektu vůbec nevztahuje, lze skrýt. Pokud ji hráč v daném kontextu může očekávat a chybí pouze splnitelná podmínka, upřednostnit dohledatelnou akci s důvodem nedostupnosti. Vysvětlení musí být dostupné také u neaktivního ovládacího prvku; samotná existence tooltipu v kódu to nezaručuje.

Toto je pravidlo pro návrh dalších změn, nikoli tvrzení, že tak nyní fungují všechny panely. Při úpravách zohlednit také stabilitu polohy tlačítek a množství zobrazených možností.

## Krátké zadání UX pro novou funkci

Před jednotlivými změnami vyjasnit potřebnou strukturu [vybrané oblasti](requirements/areas.md):
A01 vlastní menu a navigaci aplikace, A02 nastavení partie a A03 obrazovky volné
hry se společným jazykem UI. Struktura a ovládání se připravují už před prvním
řezem; pozdější hlavní ladění grafiky a UX neznamená odklad těchto základů.
Současná cesta obrazovkami je podklad k posouzení, nikoli automaticky finální návrh.

Před implementací funkce ovlivňující hráče doplnit do jejího zadání následující body. U malé změny stačí několik vět; podrobný návrh každé obrazovky ani samostatná studie nejsou automaticky potřeba.

| Otázka | Co má zadání obsahovat |
| --- | --- |
| Co chce hráč udělat | Cíl vyjádřený herním jazykem, například získat novou jednotku nebo zvýšit výrobu potravin. |
| Kde možnost najde | Obrazovka, vybraný objekt, viditelná akce a případná kontextová nápověda. |
| Co potřebuje vědět předem | Cena, podmínky, dostupné kapacity a očekávané důsledky. |
| Jak akci provede a zruší | Základní cesta ovládání, případná zkratka, potvrzení a chování při zrušení. |
| Jak pozná výsledek | Změna stavu, text, ikona nebo animace; zvuk pouze tam, kde pomáhá. |
| Co se stane při nesplněných podmínkách | Konkrétní vysvětlení a možný další krok; zachování rozpracovaných údajů, kde to dává smysl. |
| Jak navazuje na další práci | Co zůstane vybrané, kam se hráč vrátí a jak snadno činnost zopakuje. |
| Jak funkci ověříme | Pozorovatelný scénář, očekávaný výsledek a potřebný druh kontroly. |

Například nová jednotka potřebuje srozumitelný popis účelu, místo pořízení, cenu a podmínky před nákupem, vysvětlení nedostupnosti a jasný výsledek po získání. Vlastní dialog, nová ikona ani vlastní zvuk nejsou automatickou podmínkou: vhodné existující prvky lze použít znovu.

## Současné ovládání a místa pro další práci

### Pojmenování a přejmenování kolonie — Z02

Uživatel **12. října 2026** zvolil modální dialog po kliknutí na jméno,
nikoli inline editor. Založení používá stejnou podobu s jiným potvrzovacím
tlačítkem. Chybné jméno má červené pole i vysvětlující text; samotná barva
nestačí. Zrušit/Escape nic nezmění, Enter potvrzuje platný návrh. Jméno je
dostupné také klávesnicí a po přejmenování se do něj vrací fokus.
Podrobná pravidla patří do [Z02](implementation-plan.md#z02-zalozeni-pojmenovane-kolonie).
Jde o přijatý návrh; automatické testy nejsou uživatelskou studií.

Následující přehled zachycuje vybrané vazby ve zdrojovém kódu. Nejde o úplný audit ovládání ani hodnocení skutečné úspěšnosti hráčů.

| Oblast | Současný stav doložený kódem | Co potřebujeme ověřit při používání |
| --- | --- | --- |
| Výběr a pohyb na mapě | [GamePanelMouseListener](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/GamePanelMouseListener.java) při primárním stisku na poli s pohyblivou vlastní jednotkou z běžného režimu zkouší přejít do režimu pohybu; zpracovává také uvolnění myši, posouvání mapy a modifikátory Ctrl/Alt. | Zda hráč pozná rozdíl mezi výběrem, zadáním příkazu a posouváním mapy a dokáže rozpracovaný příkaz zrušit. |
| Dostupnost akcí | [ButtonsGamePanelController](../microcol-game/src/main/java/org/microcol/gui/screen/game/components/ButtonsGamePanelController.java) podle stavu přidává či odebírá akce pro založení kolonie a orbu. [ButtonsGamePanel](../microcol-game/src/main/java/org/microcol/gui/screen/game/components/ButtonsGamePanel.java) je umísťuje do panelu. | Zda hráč pochopí důvod chybějící akce a zda změny polohy tlačítek nekomplikují naučené ovládání. |
| Popisky ikon | [ButtonImage](../microcol-game/src/main/java/org/microcol/gui/buttonpanel/ButtonImage.java) při najetí myši posílá lokalizovaný popis do stavového řádku a při opuštění jej odstraňuje. | Zda si hráč popisku všimne, rozpozná ikonu a dostane obdobnou pomoc při ovládání klávesnicí. |
| Práce a vybavení v kolonii | [PanelOutsideColonyUnit](../microcol-game/src/main/java/org/microcol/gui/screen/colony/PanelOutsideColonyUnit.java) používá přetažení a kontextové menu vybavení. [PanelColonyFields](../microcol-game/src/main/java/org/microcol/gui/screen/colony/PanelColonyFields.java) nabízí změnu produkce přes kontextové menu obsazeného pole. | Zda hráč najde tyto možnosti bez náhodného zkoušení, pozná platný cíl přesunu a potřebné suroviny. |
| Přehled tahu | [ShowTurnEvensOnTurnStartedEvent](../microcol-game/src/main/java/org/microcol/gui/screen/turnreport/ShowTurnEvensOnTurnStartedEvent.java) otevírá report pro čerstvý tah člověka s neprázdným seznamem. [TurnEventPanel](../microcol-game/src/main/java/org/microcol/gui/screen/turnreport/TurnEventPanel.java) zobrazuje zprávu jako text s odrážkou. | Zda hráč pozná naléhavost zprávy, najde dotčenou kolonii a ví, co může udělat dál. |
| Výuka a následná nápověda | [Default_0_mission](../microcol-game/src/main/java/org/microcol/model/campaign/Default_0_mission.java) obsahuje kontextové instrukce k počátečním úkolům. Hlavní obsah [ColonizopediaDialog](../microcol-game/src/main/java/org/microcol/gui/screen/colonizopedia/ColonizopediaDialog.java) je zatím prázdný. | Zda hráč zvládne první kolonii a později dokáže znovu dohledat zapomenutý postup. Existence třídy dialogu neprokazuje dostupnou a úplnou nápovědu. |

### Evropské vztahy — R-0026

1. Rozpracované [R-0026](requirements/done/R-0026.md) přidává přehled **Statistiky →
   Evropské vztahy**. Zobrazuje všechny dvojice kolonizačních hráčů a mír/válku,
   bez skrytých vojenských či hospodářských údajů. Upravený název doplňuje národ,
   aby dvě stejná zobrazovaná jména nezaměnila soupeře.
2. Během vlastního tahu lze u mírového soupeře otevřít potvrzení vyhlášení války.
   Text pojmenuje oba hráče a důsledek pro útoky a zabrání kolonií. Zrušit/Escape
   nic nemění; válka vznikne až po výslovném potvrzení. Po změně hry, vztahu nebo
   zobrazeného jména se stará volba odmítne a přehled obnoví.
3. Výsledek a odmítnutí jsou textové a nezávislé na zvuku. Samostatné vyjednání
   míru ještě nemá hráčskou cestu. Implementace a automatické scénáře jsou
   rozpracované; aktuální důkazy vlastní [testová evidence](functional-tests.md).
   Tento popis není výsledkem uživatelské studie.

## První scénář k ověření

Souvislý průchod navazuje na [cíl běžné nové hry](implementation-plan.md#cil-bezne-nove-hry):
hráč začne z hlavního menu bez nutnosti dokončené kampaně. Níže uvedený úkol
je zadání pro ověření použitelnosti, nikoli povinná mise odemykající volnou hru.

Výchozím návrhem je **založení první fungující kolonie**. Pokrývá souvislou činnost od mapy po ekonomický výsledek:

1. Vybrat jednotku a zjistit, co s ní lze provést.
2. Přesunout ji na vhodné místo a založit kolonii.
3. Přidělit pracovníka k práci a zjistit, co bude vyrábět.
4. Ukončit tah a rozpoznat, co se změnilo.
5. Vysvětlit vzniklou produkci a najít případný problém nebo další potřebný krok.

Zadání pro hráče popisuje cíl, například „Založ kolonii a zajisti v ní výrobu potravin“. Konkrétní kliknutí jsou součástí interního funkčního scénáře, nikoli návodem při ověřování, zda hráč sám najde potřebné ovládání. Výchozí uložená hra, pravidla a podmínky musí tento úkol skutečně umožňovat.

Sledujeme dokončení úkolu bez zásahu pozorovatele, místa zaváhání, mylná očekávání, potřebu nápovědy a schopnost hráče poznat výsledek. Počet kliknutí či rychlost slouží jako doplňující údaje; promyšlené strategické rozhodování samo o sobě není problém UX.

Scénář je **navržený, dosud zde není zaznamenáno ověření s hráči**. Další vhodné cesty jsou naložení a vyložení lodě, obchod v Evropě, reakce na hladomor a získání nové jednotky. Rozšiřovat je podle skutečných změn a nalezených problémů.

## Jak pravidla udržovat v implementaci

Opakované chování zavádět do společných komponent, aby je nové obrazovky přebíraly. Existujícími místy jsou například [ButtonImage](../microcol-game/src/main/java/org/microcol/gui/buttonpanel/ButtonImage.java), [AbstractButtonsPanel](../microcol-game/src/main/java/org/microcol/gui/buttonpanel/AbstractButtonsPanel.java) a základ dialogů [AbstractYesNoDialog](../microcol-game/src/main/java/org/microcol/gui/util/AbstractYesNoDialog.java). Hráčské texty zapojovat přes existující [I18n](../microcol-game/src/main/java/org/microcol/i18n/I18n.java) a jazykové resources.

Při změně sdílené komponenty projít její dotčená použití. Změna jednoho tlačítka nemá nechtěně změnit chování ostatních obrazovek. Novou abstrakci zavádět tehdy, když řeší skutečně společné chování; samotné přijetí těchto pravidel nevyžaduje přepis celého UI.

Před dokončením změny ovlivňující ovládání:

- Projít základní hráčskou cestu a relevantní nedostupný, chybový či zrušený stav.
- Zkontrolovat srozumitelnost textů, stav výběru, návrat z dialogu a návaznost na další krok.
- Ověřit dotčené ovládání myší a klávesnicí, čitelnost a lokalizaci v přiměřeném rozsahu.
- Sladit vizuální a případnou zvukovou odezvu s jejich vlastními pravidly.
- Zapsat důležité nové zjištění nebo změnu společného pravidla sem; konkrétní podrobnosti funkce ponechat u jejího zadání a kódu.

Automatické testy se řídí [pravidly funkčních scénářů](functional-tests.md): každá změna chování má odpovídající pokrytí a úpravy vzhledu zachovávají herní očekávání. Úspěšný skriptovaný průchod není důkaz, že nový hráč ovládání objeví nebo pochopí. Rozsah ověřování volit podle změny; drobná textová oprava nepotřebuje nové uživatelské testování.

## Ověřování s hráči a záznam zjištění

Pro první malé kolo lze zvolit několik lidí s různou zkušeností se strategiemi. Pozorovatel zadá cíl a sleduje postup, očekávání a záseky; pokud pomůže, pomoc zaznamená. Hráč může nahlas popisovat, co očekává. Po úkolu ověřit, zda rozumí výsledku, a zjistit, co mu připadalo nejasné.

Nejdříve řešit překážky dokončení, nechtěné závažné akce a opakované nedorozumění. Následují zbytečné kroky a další nepohodlí. Upravenou cestu znovu ověřit. Malá skupina pomůže najít konkrétní potíže, ale sama nedokazuje použitelnost pro všechny hráče ani zábavnost celé kampaně. Delší hraní je potřeba pro tempo, opakování činností a pozdní fáze hry.

Rozpočet a rozsah plánovaných kol zůstávají v [odhadu pracnosti](implementation-estimate.md#real-player-test-plan-and-completion-evidence); tato pravidla automaticky nezahajují nábor ani další kolo. Průchod kódem a aplikací může připravit implementátor, skutečné porozumění a dlouhodobý dojem potřebují také zkušenost hráčů.

### Druhy důkazů

| Označení | Co skutečně dokládá |
| --- | --- |
| Návrh nebo hypotéza | Očekávaný problém či zlepšení, které je potřeba ověřit. |
| Doloženo kódem | Konkrétní implementovaná vazba; nemusí být ověřená v běžící aplikaci. |
| Ověřeno v aplikaci | Popsaný scénář proběhl v konkrétní verzi a prostředí. Uvést datum, postup a výsledek. |
| Ověřeno s hráči | Záznam konkrétních pozorování, počtu a zkušenosti účastníků, podmínek a výsledku. Nezobecňovat mimo rozsah ověření. |

Tyto značky popisují důkaz, nikoli automaticky úspěch. Zvlášť uvádět stav řešení: otevřeno, plánováno, upraveno nebo uzavřeno. Za potvrzený problém nepovažovat pouhý předpoklad odvozený z neobvyklého kódu.

### Počáteční seznam k ověření

| ID | Otázka | Dosavadní důkaz a stav |
| --- | --- | --- |
| UX-001 | Rozpozná hráč výběr jednotky, režim pohybu a zrušení příkazu? | Hypotéza z mapového ovládání popsaného výše; otevřeno. |
| UX-002 | Pochopí, proč chybí akce založení kolonie nebo orby? | Skrývání akcí doloženo kódem; dopad na hráče je hypotéza, otevřeno. |
| UX-003 | Najde přidělení práce, změnu produkce a podmínky vybavení? | Přetažení a kontextové nabídky doloženy kódem; nalezení těchto možností hráčem je k ověření, otevřeno. |
| UX-004 | Dokáže podle zprávy o hladomoru najít kolonii a navázat řešením? | Textový report doložen kódem; porozumění a návaznost jsou k ověření, otevřeno. |
| UX-005 | Dokáže po úvodní výuce znovu najít zapomenutý postup? | Výuka a prázdný obsah encyklopedie doloženy kódem; hráčská cesta je k ověření, otevřeno. |

Při práci na položce doplnit scénář a verzi hry, datum, očekávané a skutečné chování, druh důkazu, dopad a četnost, navrženou úpravu, stav řešení a výsledek následného ověření. Ke skutečnému pozorování patří i podmínky, například velikost okna, způsob ovládání a použitá nápověda. Podle potřeby připojit odkaz na úkol, změnu kódu nebo snímek.

## Údržba pravidel

Pravidla lze průběžně upřesňovat podle ověřených zkušeností. Při změně uvést důvod a datum, upravit příslušné společné komponenty v rozsahu dané práce a ponechat zde jedinou aktuální verzi pravidla. Rozhodnutí o herních mechanikách patří také do jejich vlastní dokumentace; výtvarné detaily do [grafiky](graphics.md), zvukové soubory a jejich spouštěče do [audia](audio.md).

Při založení stránky **11. října 2026** byly zkontrolovány uvedené zdrojové vazby. Nebyl proveden nový průchod běžící hrou ani test s hráči; první scénář a seznam hypotéz zůstávají připravené k budoucímu ověření.

Dokumentace byla téhož dne ověřena přísným sestavením `target/docs-venv/bin/python -m mkdocs build --strict`, kontrolou odkazů a vizuálním náhledem.

## Volná hra a vybavení — 13. října 2026

1. Nová volná hra otevírá nastavení se dvěma skutečnými mapami a samostatným
   zobrazovaným jménem hráče. Výběr národa výslovně uvádí, že jde o náhled bez
   změny skutečného vlastníka. Back/Escape zachovává existující uložení.
2. Nabídka Kolonie (O) uvádí pouze vlastní kolonie. Myš a šipky/Enter otevírají
   stejnou obrazovku; Escape vrací hráče zpět. Samotné prohlížení nemění tah ani
   zlato. Dílčí TC21 a TC24 tyto cesty ověřily.
3. Kontextové menu kolonisty nabízí vybavení i při nedostatku zboží. Dialog
   ukazuje spotřebu, vracené zboží a textový důvod nedostupnosti. Přístup zůstává
   pravým tlačítkem nebo Shift+F10; nápověda a obrys označují klávesové zaměření.
   Při změně množství vyžaduje aktualizovaná volba nové potvrzení.
4. Sdílený dialog se centruje až po zobrazení nativního okna. Opakované otevření
   nesmí ponechat jiné souřadnice v JavaFX než na obrazovce; stejná tlačítka musí
   fungovat i při druhém a dalším potvrzení. Cílené TC05/TC06/TC25 po opravě prošly.
5. Toto jsou implementační rozhodnutí a automaticky ověřované cesty,
   nikoli výsledky uživatelské studie. Přijetí a kontrola snímků obrazovek jsou doložené v [záznamu implementace](functional-tests.md#free-play-implementation-2026-10-13).

## Stavění — 14. října 2026

1. Cílem hráče je najít dostupný projekt, porozumět ceně a podmínkám,
   výslovně jej zařadit a zjistit důvod čekání. Viditelné tlačítko v dílně
   otevírá stejnou nabídku myší i klávesnicí; výběr ještě frontu nemění.
2. Zrušení musí fungovat také s fokusem uvnitř seznamu. V TC22 se ukázalo,
   že samotná obsluha Escape na konci průchodu události nestačí, protože
   klávesu spotřebuje seznam. Opravený dialog a přímá kontrola jeho zavření
   prošly cílenými scénáři. Potvrzené přesuny/odebrání z fronty jsou samostatné
   provedené akce; „Zavřít / zrušit výběr“ zahazuje pouze nepotvrzený výběr.
3. České a anglické texty, náklady, důvod nedostupnosti a čekání na nástroje
   byly zkontrolovány na skutečných snímcích. Výsledky TC32 včetně výroby a
   Continue jsou v [evidenci](functional-tests.md#construction-ui-implementation-2026-10-14).
   Nejde o pozorování hráčů ani úplnou přejímku všech pravidel staveb.

## Výchozí výprava soupeře — 14. října 2026

1. R-0027 přidává do běžné volby mapy možnost „Soupeřova výprava“
   / „Rival expedition“. Hráč ji volí stejným ovládáním jako dosavadní mapy;
   původní světy s francouzskými městy zůstávají dostupné. Nový svět začíná
   soupeřovou lodí s kolonistou. Strategie nyní v referenčním světě založí
   první kolonii ve třetím tahu přes běžné pohybové a zakládací operace.
2. TC34 ověřil skutečný start a Continue i zachování této volby po návratu přes
   nastavení jazyka. Anglické a české snímky byly zkontrolované. Tyto výsledky
   potvrzují nastavení počátečního světa; TC34/06 navíc ověřuje Continue během
   plavby, viditelnou kolonii ve třetím tahu a její další tři tahy bez druhé
   kolonie. Úplná lokální regrese [R-0027](requirements/done/R-0027.md) prošla všemi 135 hráčskými/podpůrnými případy; stav publikace vlastní analýza požadavku.

3. TC34/07 ověřuje také dvě další kola bez dosažitelného břehu: ovládání
   dalšího tahu zůstává použitelné a nevzniká kolonie ani změna lidské flotily.
   Jde o automatické ověření cesty, nikoli pozorování skutečných hráčů.

### Obnova po nedokončené přípravě tahu (R-0052)

Během přípravy dalšího národního tahu sdílí mapa, herní klávesy a tlačítko
dalšího tahu stejnou podmínku připravenosti modelu. Při selhání přípravy
hráč může odejít do menu; pokus o uložení nepřepíše předchozí soubor a
dialog vysvětlí návrat přes Pokračovat nebo načtení. Upozornění je textové
a nevyžaduje zvuk. Platná uložená pozice se načte bez opakování přípravy.
Automatizované ověření patří k [R-0052](requirements/done/R-0052.md); nejde o
výsledek uživatelského výzkumu.


## Čekat a vynechat tah — R-0053

1. Hráč může ponechat jednotku na později v tomtéž tahu nebo jí zbývající
   pohyb vyčerpat. Toolbar nabízí **Čekat (Tab)** a **Vynechat tah (Mezerník)**;
   vysvětlivky rozlišují zachování a spotřebu pohybu. Anglické popisky jsou
   Wait (Tab) a Skip turn (Space). Oba příkazy používají živou dostupnost
   modelu, včetně probíhající přípravy a práce; GUI blokuje také animaci.
2. Akce je okamžitá. Před volbou lze ponechat současný výběr nebo stiskem
   Escape zavřít náhled trasy. Volba jiného pořadí náhled zruší a neprovede
   pohyb. Další vybraná jednotka a její body poskytují výsledek bez zvuku;
   odmítnutí vysvětlí status bar. Prázdný výběr neukončuje celý tah.
3. Ruční prohlížení vyčerpané jednotky je možné; opětovné vynechání není.
   Pokračovat nevrací pohyb. Obnovení patří až následujícímu tahu vlastníka.
   Pravidlo je v [jednotkových příkazech](gap-analysis.md#unit-turn-orders),
   stav přijetí v [R-0053](requirements/done/R-0053.md). Pozdější ladění vzhledu
   nebo zkratek není dosud výsledkem hráčského pozorování.

## Náhled plavby a zrušení — R-0054

1. Náhled rozlišuje plavbu do vlastního pobřežního přístavu od samostatného
   vylodění pasažéra na pevninu. Vnitrozemská kolonie není cíl plavby;
   podmínky a náklady vlastní [pravidlo přístavu](gap-analysis.md#coastal-port-movement).
2. Escape ruší nepotvrzený náhled i po návratu přes Pokračovat, když zůstane
   klávesové zaměření na menu kolonií. Bez aktivního náhledu Escape nadále
   zavírá menu. Přijetí a regresní ověření opravy spotřebované klávesy eviduje
   [R-0054](requirements/done/R-0054.md); automatický průchod není uživatelská studie.


## Volba práce kolonisty — R-0056

1. Cílem hráče je porovnat dostupná zaměstnání před přesunem pracovníka.
   Kontextová nabídka pracovníka na poli, v budově i uprostřed kolonie nabízí
   „Vybrat práci…“. Uprostřed kolonie zůstává také Shift+F10 / klávesa kontextové
   nabídky a původní volby vybavení. Stávající přetažení a nabídka plodin zůstávají.
2. Dialog odděluje ponechání současného pole od výslovného přesunu na zobrazené
   nejlepší pole. Vnitřní práce ukazuje možnou produkci, potřebné vstupy,
   současnou zásobu a textový nedostatek; nejde o slib růstu skladu. Nedostupná
   práce má důvod a zakázané potvrzení. Pravidla vlastní
   [náhled zaměstnání](gap-analysis.md#colony-job-choices-and-yield-previews).
3. Šipky vybírají, Enter potvrzuje výběr v seznamu, Escape nebo Zrušit zavírá
   bez změny. Stejně jako u stavební fronty musí dialog zachytit klávesu dříve,
   než ji spotřebuje seznam. První TC40 běhy odhalily spotřebované Escape i Enter;
   opravená cesta včetně hraničních případů prošla 15 hráčskými scénáři 16. října 2026.
4. Změna pracovníka nebo místa před potvrzením zobrazí odmítnutí a obnoví nabídku;
   jiná práce se sama nepřidělí. Odmítnuté přetažení do budovy otevře stejný dialog
   s vysvětlením a obnovenými možnostmi; hráč znovu potvrdí nebo výběr zruší.
   Zpráva tak přežije překreslení budovy i návrat ukazatele po přetažení.
   Úspěch překreslí kolonii. Anglické a české
   snímky byly zkontrolované: popisky, výnosy i upozornění se vejdou do dialogu.
   Snímek odmítnutého přetažení také potvrzuje čitelnou zprávu a dostupné Zrušit.
   Jde o automatizované ověření, nikoli pozorování skutečných hráčů. Úplná přejímka
   a publikace jsou doložené v [evidenci R-0056](functional-tests.md#r-0056-job-choice-implementation-2026-10-16).


### Volba obtížnosti — R-0064

Nastavení nové volné hry nabízí pojmenovaný výběr pěti obtížností vedle mapy,
ve stejném stylu jako ostatní volby. Výchozí volba je Dobyvatel (Conquistador).
Start použije vybranou identitu; Zpět a Escape zahodí pouze rozpracované spuštění
bez změny rozehrané nebo uložené partie. Název obtížnosti je viditelný také
v herním panelu a po Pokračovat. Změna jazyka nemění vybranou úroveň.
Jde o implementaci profilu; jednotlivé číselné dopady zůstávají u svých
požadavků. [R-0064](requirements/done/R-0064.md) rozlišuje ověření od návrhu.
Tento popis není záznam uživatelské studie.

### Referenční výprava — R-0066

Volba mapy obsahuje samostatné „Výpravy čtyř národů“ vedle tří autorských
scénářů. Start sestaví čerstvou partii na známém terénu se čtyřmi výpravami;
obtížnost určuje jejich počáteční zlato. R-0063 zpřístupňuje volbu všech čtyř národů
u referenčního profilu. U autorských scénářů je národ zamčený na Nizozemsko
a vysvětlený poznámkou; přepnutí zpět na scénář volbu národa resetuje. Zpět zachová rozehranou i uloženou hru,
Pokračovat obnoví identity a vybavení výprav. Generování terénu má vlastní požadavek. Automatizované ověření je v
[evidenci R-0066](functional-tests.md#r-0066-reference-expeditions-2026-10-16);
nejde o uživatelskou studii.

### Odmítnutí poškozené uložené hry — R-0067

Načíst i Pokračovat při nečitelném nebo poškozeném souboru zobrazí existující
dialog s vysvětlením, že se hra nepodařila načíst a předchozí hra ani soubor
nebyly změněny. Potvrzení zprávy vrací hráče k předchozí obrazovce; může vybrat
jiný soubor. Zrušení výběru je bez účinku. Dialog nenabízí automatickou opravu,
která by změnila hráčova data. Aktuální ověření uvádí
[evidence R-0067](functional-tests.md#r-0067-load-recovery-2026-10-16).

### Obrácení plavby — R-0072

1. V evropském přehledu plujících lodí je vedle zbývající doby pojmenované
   tlačítko **Obrátit směr**. Akce patří konkrétní lodi a je dostupná během
   připraveného vlastního tahu. Je ovladatelná klávesnicí.
2. Potvrzení ukáže nový cíl a počet zbývajících tahů podle modelu. Zrušit/Escape
   nezmění směr, čas ani náklad, ani po dříve potvrzeném obrácení. Po potvrzení
   se loď zobrazí v odpovídajícím směru nebo v přístavu; obsazené pobřeží
   ponechá srozumitelný stav čekání. Zastaralá nabídka zobrazí odmítnutí a
   přehled se obnoví.
3. [R-0072](requirements/done/R-0072.md) a [testová evidence](functional-tests.md#r-0072-voyage-reversal-2026-10-16)
   rozlišují implementaci, automatické ověření a dosud neprovedené uživatelské
   pozorování. Popis není výsledkem uživatelské studie.

### R-0073 — Hlídka a automatické nalodění

1. Rozpracováno 2026-10-16: vybraná jednotka na mapě nabízí rozkaz Hlídka,
   po jeho zadání Aktivovat. Na evropském molu jsou stejné rozkazy v nabídce
   jednotky vedle vybavení; Escape nabídku zavře beze změny. Zbývající pohyb
   se samotným přepnutím rozkazu nemění.
2. Značka S označuje hlídku. Odplouvající loď nabere vhodné čekající jednotky
   podle ID do volných míst; ostatní zůstanou viditelné na břehu či molu se
   zachovaným rozkazem. Oprávnění i cenu nastoupení určuje model. Neplatný
   rozkaz zobrazí existující zpětnou vazbu a ovládání se obnoví.
3. [R-0073](requirements/done/R-0073.md) a [testová evidence](functional-tests.md#r-0073-sentry-and-automatic-boarding-2026-10-16)
   rozlišují návrh, implementaci a skutečné ověření. Nejde o pozorování hráčů.


### Nabídka evropských odborníků — 16. října 2026

R-0074 rozšiřuje nabídku nákupu na 19 odborností. Dvousloupcový seznam
se posouvá uvnitř dialogu; zavření zůstává mimo posuvnou plochu. Cena a
nedostupnost používají modelový nákupní kontrakt. Tab/mezerník umožní
najmout i poslední položku, Escape zavře nabídku bez nákupu.
TC30/11–12 ověřily všechny názvy a ceny v EN/CZ, viditelnost po posunutí,
klávesový nákup a Continue. Snímky obou jazyků byly zkontrolovány;
nejde o uživatelskou studii ani přejímku všech neprodukčních schopností.
Podrobnosti uvádí [evidence R-0074](functional-tests.md#r-0074-specialist-roster-2026-10-16).


### Nábor tří kandidátů — 16. října 2026

Evropská obrazovka má samostatné textové tlačítko Nábor/Recruit vedle nákupu.
Dialog ukazuje tři uložené kandidáty, jejich jména a cenu placené cesty.
Nedostatek peněz znepřístupní potvrzení a zobrazí důvod; Escape nebo OK zavře
nabídku bez změny kandidátů. Tab/mezerník dovoluje vybrat libovolného kandidáta.
Úspěch vrátí hráče na molo s novým člověkem a aktualizovaným zlatem;
zastaralá cena vyžaduje nové otevření nabídky.
Snímky EN/CZ v TC46/F37 byly zkontrolovány: všechny tři názvy a ceny jsou
čitelné bez posouvání. Jde o automatizované ověření, ne uživatelskou studii.
Výsledky a závislosti vede [evidence R-0075](functional-tests.md#r-0075-persistent-recruitment-2026-10-16).

### Národní postup přistěhovalectví (R-0076)

Evropa ukazuje národní počet křížů a další práh. Po dosažení prahu vysvětlí,
že přistěhovalec dorazí na začátku příštího tahu; stejnou zprávu ukáže pokus
zaplatit cestu před tímto příchodem. Výroba v koloniích probíhá až po fázi
příchodů. Příchod oznamuje běžný přehled tahu a člověk je dostupný na molu.
Otevření Evropy ani Pokračovat nevyrábí kříže a nevytváří další přistěhovalce.
Aktuální stav přejímky vede [R-0076](requirements/R-0076.md).

### Cena opakovaného nákupu (R-0077)

Nabídka v Evropě ukazuje aktuální celkovou cenu před potvrzením. Další nákup
stejného typu lodě zvýší jeho další cenu; cena jiného typu se nemění. Zrušení
nabídky nic neúčtuje. Pokud se nabídka změní před potvrzením, dialog nákup
odmítne a vyzve k opětovnému otevření. Neúčtuje automaticky vyšší cenu.
Nedostatek peněz ponechá nabídku otevřenou s nedostupným tlačítkem a vysvětlením.
Historie cen přetrvá přes Pokračovat; stav přejímky vede
[R-0077](requirements/R-0077.md).

### Chov koní v kolonii (R-0078)

Hráč sleduje koně a jídlo ve stávajícím skladu kolonie. Náhled přírůstku koní
zahrnuje dostupné jídlo i volnou kapacitu; stáje postaví běžnou stavební
frontou. Jejich účinek se projeví v následujícím výrobním tahu. Jídlo se
nejprve použije pro obyvatele a narození kolonistů, teprve zbytek pro koně.
Plný sklad ani chybějící jídlo nezpůsobí záporné zásoby nebo zbytečné odečty.
Zavření kolonie či Pokračovat samo nic nechová. Stav ověření vede
[R-0078](requirements/done/R-0078.md).

### Výrobní upgrade a nedostatek vstupů (R-0079)

Hráč volí upgrade ve stávající stavební frontě. Po dokončení zůstává
pracovník na svém místě a skladový náhled ukáže novou spotřebu i produkci.
Při nedostatku se náhled omezuje dostupnými surovinami celé budovy;
železárna může ve stejném tahu dodat nástroje zbrojnici. Otevření kolonie
ani Pokračovat výrobu nespouští. Konkrétní hráčské kroky a jejich ověření
vede [evidence R-0079](functional-tests.md#r-0079-industrial-production-2026-10-16).

### Chybějící dok nebo loděnice (R-0080)

Tažení kolonisty na vodní pole bez doku zůstává odmítnuté a stavový řádek
vysvětlí potřebnou stavbu. Po dokončení doku lze stejným gestem přidělit
rybolov; vyšší zařízení tuto možnost zachovají. Stavební nabídka ponechává
lodní projekty viditelné s vysvětlením chybějící loděnice nebo pobřeží.
Zrušení nabídky nemění stav a potvrzení znovu kontroluje aktuální podmínky.
Přejímku hráčských cest a jejich výsledky vede [R-0080](requirements/done/R-0080.md).

### Tisk a příspěvek státníka (R-0081)

Hráč vybírá tiskárnu a noviny ve stávající stavební nabídce; zrušení výběru
nemění frontu. Radnice ukazuje společný výstup včetně základního zvonu
a tiskového bonusu. Výběr práce označuje jednotlivý příspěvek státníka
jako hodnotu před bonusy kolonie, aby jej hráč nezaměňoval za celý výstup.
Dokončená stavba zvyšuje další produkci bez přesunu pracovníků.
Automatické hráčské ověření zaznamenává [R-0081](requirements/done/R-0081.md);
nejde o pozorování skutečných hráčů.

### Odchod třetího obyvatele z opevnění (R-0082)

Při tažení chráněného obyvatele ven rozhraní nepovolí přesun a ve stavovém
řádku vysvětlí minimum tří obyvatel i možnost změnit práci uvnitř kolonie.
Kontrola se opakuje při dokončení akce; model zůstává autoritou. Čtvrtý
obyvatel může odejít běžným tažením a potom nastoupit na loď. Hlad může
snížit populaci pod minimum, aniž by odstranil opevnění. Automatické
hráčské ověření vede [R-0082](requirements/R-0082.md).

### Společná zásoba kladiv (R-0109)

Stavební nabídka ukazuje jednu společnou zásobu práce nad frontou.
U jednotlivých projektů zobrazuje jejich ceny, aby nevypadaly jako
nezávisle rozpracované stavby se stejnými kladivy. Změna či odstranění projektu
zásobu ponechá. Dokončení spotřebuje pouze
cenu prvního projektu a přebytek je dostupný pro další.

### Potvrzení nákupu dokončení (R-0083)

Ve stavební nabídce hráč otevře „Koupit dokončení…“ pro první projekt
fronty. Samostatné okno jmenuje projekt, použitá a dokoupená kladiva
a nástroje, celkovou cenu včetně daně a zbývající pokladnici. Platba
vyžaduje „Zaplatit a dokončit“; zrušení a Escape nic neutratí.
Nedostatek zlata zakáže potvrzení. Změněná nabídka vysvětlí odmítnutí
a vyžádá novou cenu. Po zaplacení se fronta a kolonie překreslí.

### Kongres — R-0104

1. Z mapy otevřít Kongres, prohlédnout národní zvony, cíl, kandidáty podle
   kategorií a členy. Před první kolonií okno vysvětlí podmínku.
2. Označení kandidáta ještě nic nemění; volbu provede samostatné tlačítko.
   Zavření či Escape bez potvrzení ponechá nabídku i svět beze změny.
3. Po potvrzení zobrazit zvolený cíl; po příchodu členství a zprávu v přehledu
   tahu. Jonesova odměna rozlišuje doručení a čekání na bezpečný přístav.
   Staré potvrzení po změně modelu nebo nabídky odmítnout s pokynem znovu otevřít.
4. Ověření vede [R-0104](requirements/R-0104.md); popis není samostatným
   tvrzením o provedené hráčské studii.

### Stavby vyžadující otce — R-0084

1. Nabídka staveb při splněné populaci a předchůdci jmenuje chybějícího
   otce: Smitha pro továrny, Stuyvesanta pro celnici. Hráč jej získá
   obvyklým výběrem a výrobou zvonů v Kongresu.
2. Členství zpřístupní pouze jinak dovolený projekt. Zařazení vyžaduje
   samostatné potvrzení; zavření nabídky nic nezařadí.
3. Rozpracovaná stavba bez potřebného členství zobrazí stejný důvod
   pozastavení. Dokoupení není dostupné a zásoby zůstanou zachované.
   Hotové stavby při změně vlastníka nezmizí. Ověření vede
   [evidence R-0084](functional-tests.md#r-0084-founder-construction-2026-10-16).

### Vývoz celnicí — R-0085

1. Z kolonie otevřít „Vývoz celnicí…“. Bez hotové celnice je tlačítko
   nedostupné s vysvětlením podmínky.
2. Zaškrtnout obchodovatelné druhy a výslovně uložit výběr. Okno vysvětluje
   rezervu 100, pořadí po výrobě a stavbě, evropskou cenu po dani a za
   revoluce poloviční příjem. Zrušení a Escape návrh zahodí.
3. Samotné uložení zboží neprodává. Příští běžný tah uvede v přehledu
   kolonii, prodané množství, druh a skutečný příjem. Pokračovat zachová
   nastavení a neopakuje dokončený prodej. Zastaralé potvrzení se odmítne
   s pokynem okno znovu otevřít. Stav ověření vede
   [evidence R-0085](functional-tests.md#r-0085-custom-house-export-2026-10-16).

### Vyjednání míru — R-0087

1. Hráč najde vyjednání v přehledu evropských vztahů u válečného soupeře.
   Bez viditelného sousedního kontaktu kolonisty je akce nedostupná; tooltip
   vysvětluje potřebný kontakt. Základní přehled vztahů zůstává viditelný.
2. Nabídka ukáže soupeře, kontaktní jednotku, cenu a výsledek platby.
   Nedostatek zlata zakáže potvrzení. Potvrdit, zrušit návrh a rozhodnout
   později jsou oddělené akce; Escape návrh ruší, odložení jej zachová.
3. Continue obnoví původní návrh. Změněné podmínky model odmítne před platbou
   a dialog vysvětlí možnost zrušit návrh a vyžádat nový. Po přijetí přehled
   ukáže mír; další porušení smlouvy používá stávající varování.
4. Implementace 2026-10-16; hráčské ověření a snímek se evidují v
   [R-0087](functional-tests.md#r-0087-peace-negotiation-2026-10-16).
   Nejde o výsledek pozorování skutečných hráčů.

### Šířka horní lišty — ověření 2026-10-16

1. Po přidání dalších akcí se horní řada v úzkém okně stlačila a TC11
   zachytil nedostatečnou šířku popisků. Lišta nyní zalamuje celé ovládací
   prvky při zachování jejich preferované šířky a rezervy pravého panelu.
2. Rozměry a dostupnost akcí ověřuje TC11; dopad společné lišty na ostatní
   obrazovky vyžaduje celou funkční sadu. Aktuální výsledky jsou v
   [evidenci R-0087](functional-tests.md#r-0087-peace-negotiation-2026-10-16).

### Požadavek tributu — R-0088

1. Hráč otevře přehled evropských vztahů a zvolí tribut u sousedního kontaktu
   vojáka, dragouna nebo průzkumníka. Nedostupná akce vysvětlí potřebný kontakt
   nebo počet zbývajících tahů do dalšího požadavku.
2. Dialog před potvrzením ukáže soupeře, kontakt, očekávanou platbu, pokladnu
   a předpokládané odmítnutí či prázdnou pokladnu. Výslovně upozorní, že
   potvrzený požadavek zahájí deset tahů čekání i při odmítnutí.
3. Potvrzení, zrušení a odložení jsou oddělené. Continue zachová nabídku;
   změněné podmínky se odmítnou bez převodu. Výsledek rozlišuje platbu,
   odmítnutí a chybějící zlato a potvrzuje, že vztahy zůstaly stejné.
4. Implementace je připravena v izolovaném worktree; skutečnou přejímku vede
   [evidence R-0088](functional-tests.md#r-0088-european-tribute-2026-10-16).


### Franklinova nabídka míru při aktualizaci tahu

1. Nové hlášení uvádí soupeře, cenu a místo, kde lze návrh prohlédnout: evropské vztahy. Samotné hlášení mír neuzavře.
2. Odložený návrh zůstane dostupný po Continue; další tah nevytváří duplicitní nabídku ani nové hlášení k témuž návrhu. Potvrzení znovu ověří původní podmínky.
3. Implementace je připravena v izolovaném pracovním stromu; hráčskou cestu ověřuje nový pátý scénář TC59. Jeho spuštění zatím čeká na dokončení běžící úplné UI sady.

### R-0106 — Opevňování a účinné opevnění

1. Hráč vybere vojáka či dragouna na mapě a použije tlačítko Opevnit.
   Vysvětlení uvádí spotřebu zbývajícího pohybu a účinnost až příští vlastní tah.
2. Popisek se změní na Zrušit opevňování; po aktualizaci na Zrušit opevnění.
   Výběr jiné jednotky, návrat ani Continue rozkaz nezruší a neurychlí.
3. Výslovné zrušení dovolí další dostupné akce, ale neobnoví již utracený pohyb.
   Pokus táhnout opevněnou jednotku vysvětlí nutnost nejprve zrušit rozkaz.
4. F52/TC61 jsou připravené hráčské scénáře; jejich spuštění a vizuální kontrola
   zatím neproběhly. Nejde o výsledek uživatelské studie.


### Viditelnost mapy pod zalomenou lištou

1. Úplný běh 2026-10-16 odhalil, že dvě řady tlačítek zakryly celé severní
   políčko mapy v TC34/06. Zkrácení popisků ani posun kliknutí není opravou.
2. Herní obsah proto rezervuje skutečnou výšku celé lišty a mapa začíná až
   pod ní. Kontrola geometrie doplňuje čitelnost popisků; severní okraj a
   návrat z Continue musí projít skutečnou hráčskou cestou. Ověření opravy čeká.

### Náhled obyčejného pozemního boje — R-0093/R-0094

1. Dialog při přetažení ozbrojené jednotky na protivníka ukáže
   skutečně vybraného obránce, základ a nenulové bonusy obou stran, výsledné
   síly a odpovídající šanci útočníka. Zaokrouhlení je jen pro zobrazení.
2. Potvrzení předem vysvětlí, že poražený ztratí koně nebo muškety, oba
   účastníci spotřebují pohyb a přeživší zůstanou na původních místech.
   Změněné vybavení během dialogu původní nabídku zneplatní bez boje.
3. Zrušení, Escape a zavření okna svět ani náhodný stav nemění. Po boji
   stavový řádek oznámí ztracené vybavení. Continue výsledek neopakuje.
4. Lokální TC35 průchody a kontrola anglických/českých snímků prošly;
   nejde o uživatelskou studii ani dokončení celého bojového UC.
   [Evidence](functional-tests.md#r-0093r-0094-ordinary-land-combat-2026-10-16)
   rozlišuje opravenou chybu GUI vlákna, lokální výsledky a publikaci.


### Dělostřelectvo R-0095 (2026-10-17)

Hráč najde dělo v běžné nabídce evropských jednotek a kolonijních projektů.
Nedostupná stavba vysvětlí chybějící zbrojnici; nákup ukáže aktuální cenu.
Bojový náhled uvádí venkovní postih a následky prohry ještě před potvrzením.
Poškozené dělo má zlomenou hlaveň i textové označení; zpětná vazba se zobrazí
po animaci boje. Zrušení ponechá svět beze změny, Continue poškození neopraví.
Jde o implementovaný návrh s probíhající přejímkou v
[evidenci R-0095](functional-tests.md#r-0095-artillery-lifecycle-2026-10-17),
nikoli o poznatek z uživatelské studie.

### Přehled oprav lodí — R-0098

Hráč najde tlačítko **Opravy lodí** v Evropě i pro opravy v koloniích.
Přehled ukazuje vlastní lodě, jejich cíl, přepravu/opravu a počet tahů
vlastníka do zprovoznění. Vysvětluje bezplatnost i nedostupnost pohybu,
boje a nákladu; nezobrazuje ovládání, které by opravu obešlo. Zavřít nebo
Escape vrátí hráče do Evropy bez změny průběhu. Prázdný seznam má výslovnou
zprávu. Příjezd, dokončení, změna cíle a potopení se ztrátami se oznamují
v běžném hlášení tahu. Nejde o nové zvukové události.

Čtyři scénáře TC63 i vizuální kontrola EN/CZ přehledu prošly 17. října
2026. Přesné výsledky a samostatnou publikaci vede [evidence](functional-tests.md#r-0098-ship-repair-2026-10-17).
Pozorování hráčů zatím neproběhlo.

### Námořní bitva — R-0097

Hráč táhne svou bojeschopnou loď na cizí loď. Sdílené potvrzení ukáže
síly, šanci a riziko potopení, opravy a ztráty nákladu; zrušení nic nespotřebuje.
Po animaci samostatné hlášení pojmenuje poraženou loď, kořist vítěze,
ztracené nebo zachované pasažéry a zboží a případný cíl opravy.
Poškozenou vlastní loď hráč najde v existujícím přehledu oprav v Evropě.
Pokračování uložené hry boj ani kořist neopakuje. Jde o implementovaný návrh;
výsledky hráčských scénářů vede [evidence testů](functional-tests.md).

### Korzár a diplomatické potvrzení — R-0099

Při útoku korzára na cizí loď i bojové lodi proti korzárovi hráč předem vidí,
že akce nevyhlašuje válku a zachová dosavadní vztahy. Vypnutí běžného bojového
poradce tuto diplomatickou informaci nepřeskočí. Zrušení nic nespotřebuje;
při změně vztahů během potvrzení hra vyžádá novou volbu útoku. Výsledek,
kořist a opravy sdílejí cestu R-0097. Nejde o ochranu korzára před napadením.
Přejímka hráčských scénářů je vedená v [R-0099](requirements/R-0099.md).

### Povýšení vítězného vojáka — R-0111

Po boji hráč uvidí vedle ztráty vybavení poraženého také skutečný krok povýšení
vítěze. Informace o jednotce trvale ukazují veteránský stav odděleně od civilní
odbornosti. Následující bojové potvrzení již používá vyšší základní sílu.
Zrušení útoku nepovyšuje; Continue zachová výsledek bez dalšího losování.
Stav hráčské přejímky vede [R-0111](requirements/R-0111.md).

### Kostel, katedrála a kazatelé — R-0110

Hráč zvolí náboženské rozšíření v běžné stavební nabídce, která ověří populaci
a materiál; zrušení nemění frontu. Po dokončení vidí vyšší výrobu u kostela.
Přes nabídku práce přidělí běžného nebo odborného kazatele; odebrání pracovníka
ponechá základ budovy. Penn upraví společný součet a zobrazený přírůstek
odpovídá příštímu skutečnému tahu i národnímu imigračnímu fondu. Continue
nevytvoří další kříže. Návrh ověřuje TC67; výsledky jsou v
[evidenci testů](functional-tests.md#r-0110-religious-production-2026-10-17).

### Školní výuka a odborná praxe — R-0107

Tlačítko Vzdělávání v kolonii otevírá přehled učitelů, žáků, dovedností
a uloženého pokroku. Nabídku určuje model podle školy a dostupného místa.
Před potvrzením hráč vidí minimální stupeň školy, délku a příští sociální
nebo odborný krok. Zavření návrhu nic nepřidělí. Potvrzení přesune učitele
do školy, žákovi ponechá práci. Zrušení vybraného kurzu smaže pokrok,
učitel zůstane dostupný ve škole pro jiného žáka. Při výměně hráč nejprve
přesune původního učitele na jinou práci; přehled ukáže pozastavení.
Stejná dovednost s novým učitelem pokračuje v zachovaném pokroku.
Změněné účastníky model při potvrzení odmítne bez částečného přiřazení.
Dokončení výuky či odborné praxe se objeví ve hlášení tahu; jeho opětovné
zobrazení ani Continue nepřidává pokrok. Návrh hráčské cesty a výsledky
TC68 jsou odděleny v [evidenci](functional-tests.md#r-0107-education-work-2026-10-17).


### Čekající výběr přistěhovalce (R-0075/R-0076)

Brewsterova získaná imigrace nabízí tři konkrétní lidi s tlačítkem „Vybrat
zdarma“. Vysvětlení ukazuje kříže a další hranici, odlišuje výběr od placeného
náboru a říká, že zavření pouze odkládá rozhodnutí. Přes Evropu lze nabídku
znovu otevřít. Během čekání nelze obchodovat ani pokračovat dalším tahem;
uložení a návrat do nabídky zůstávají dostupné. Po poslední volbě hra dokončí
tah a ohlásí skutečné příchody. Jde o implementované zamýšlené chování,
nikoli uživatelskou studii; přejímku vede
[testová evidence](functional-tests.md#r-0075r-0076-brewster-integration-2026-10-17).

### Současně přijíždějící a odjíždějící lodě

Při ověřování Magellana (2026-10-17) snímek Evropy ukázal překrytí nadpisu
odjezdu tlačítkem obrácení přijíždějící lodi. Oblast odjezdu je posunutá níž,
aby zůstal čitelný směr i dostupný cíl přetažení. TC71 kontroluje oddělení
oblastí i skutečný odjezd; jde o technické ověření cesty, nikoli uživatelskou
studii. Výsledky vede [evidence](functional-tests.md#r-0104-magellan-integration-2026-10-17).
