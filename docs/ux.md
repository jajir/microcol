# UX a pravidla ovládání MicroCol

## Přehled budov v nápovědě — 30. října 2026

1. [R-0351](requirements/done/R-0351.md) pomáhá vybrat další stavbu: z mapy otevřít nápovědu tlačítkem nebo H, zvolit **Budovy** a porovnat obrázky, přínosy a náklady všech úrovní. Řady vylepšení zůstávají pohromadě; názvy lze hledat bez diakritiky.
2. Cena patří celé cílové úrovni, nejde o rozdíl cen sousedních budov ani o cenu urychlení za zlato. Vedle ceny jsou předpoklady a základní výrobní hodnoty; skutečný výnos závisí na pracovnících, zásobách a bonusech. U velkých stájí se výslovně uvádí, že nyní nezvyšují chov nad účinek běžných stájí.
3. Přehled nemá stavební akci. Zpět do hry i Escape vracejí hráče na mapu bez změny partie. Ilustrace mají přístupné názvy, dlouhý obsah používá stávající posuvník a sdílenou typografii. Automatická přejímka patří do [funkční evidence](functional-tests.md); nejde o pozorování nezávislého hráče.

## Správa města v samostatných sekcích — 30. října 2026

1. [R-0349](requirements/done/R-0349.md) zachovává hráčovu pracovní smyčku: zkontrolovat obyvatele a zásoby, přiřadit práci, vybrat výstavbu a odbavit dopravce. Šestnáct rodin budov drží stejné pozice při změně úrovně; pasivní budovy nemají pracovní sloty.
2. Kliknutí nebo šipky na mapě vybírají pouze políčko. Detail napravo ukazuje jeho aktuálního pracovníka a modelový výnos. Explicitní výběr plodiny mění produkci, tlačítko práce otevírá existující nabídku s potvrzením/zrušením. Enter či mezerník na mapě otevírá práci vybraného pracovníka. Střed města ukazuje automatické výnosy bez možnosti přidělit pracovníka nebo změnit plodinu.
3. Přetahování, kontextové nabídky, fronta, náklad a zprávy při odmítnutí zůstávají zachované. Okolní lidé a dopravci mají vlastní vodorovný posun; celá pracovní plocha je při malém okně posuvná. Stavová nápověda nahrazuje tooltipy. Čistá bilance zásob už zahrnuje spotřebu.
4. Toto jsou implementované ovládací záměry, nikoli závěr uživatelského výzkumu. Skutečné lokální ověření se uvádí ve [funkční evidenci](functional-tests.md).
## Minimapa světa — 2026-10-30

1. [R-0350](requirements/done/R-0350.md) dává hráči trvalý přehled celého světa nahoře v pravém panelu. Známé oblasti používají terénní barvy, neprůhledné neznámo nic dalšího neprozrazuje a kontrastní rámeček odpovídá hlavnímu pohledu.
2. Kliknutí do mapy ihned přesune pouze kameru. U okraje se výřez omezí na svět; svět menší než hlavní pohled zůstává vystředěný. Kliknutí do papírového okraje nemění pohled. Nejde o rozkaz jednotce, neodkrývá území ani nevyžaduje potvrzení či zrušení.
3. Stálá česká nebo anglická nápověda popisuje kliknutí a šipky; stejný text je přístupným vysvětlením. Zaměřená minimapa má tečkovaný okraj. Šipky posunou kameru o jedno pole, Tab přejde na další ovládací prvek. Ostatní běžné způsoby posunu mapy i změna velikosti okna aktualizují rámeček.
4. Jde o implementovaný návrh; [automatické ověření](functional-tests.md#r-0350-world-minimap-2026-10-30) a vizuální kontrola nejsou uživatelská studie.


## Zarovnání evropského přístavu — 2026-10-29

[R-0348](requirements/done/R-0348.md) navazuje přistěhovalectví, příchozí lodě,
přístav a náklad na společný levý okraj lišt. Trh rovnoměrně rozmístí ikony
od levého k pravému okraji při zachování vystředěných cen. Vstup do Evropy,
výběr lodi, obchod, samostatné posouvání seznamů a návrat do hry se nemění.
Ověření zahrnuje široké i úzké okno a oba jazyky; automatické kontroly rozložení
nejsou pozorováním nezávislého hráče.

## Ceny evropského zboží — 2026-10-29

1. Hráč při prohlížení evropského trhu přiřadí dvojici prodej/nákup k ikoně přímo nad ní podle [společného středu](graphics.md#evropsky-pristav-namorni-skicar-28-rijna-2026). Jde o uživatelem vybranou prezentaci [R-0346](requirements/done/R-0346.md), bez nové akce nebo potvrzení; pravidla nákupu, prodeje a bojkotu se nemění.
2. Obsah zboží přebírá také [šířku a boční odsazení herních lišt](#sirka-obsahu-hernich-list-r-0342), aby při rozšiřování okna udržel stejné okraje.
3. Rozhodnutí není výsledkem hráčské studie. Skutečné ověření obou jazyků, velikostí okna a obchodních cest patří do [funkční evidence](functional-tests.md#r-0346-centered-goods-prices-2026-10-29).

## Žádné vyskakovací bubliny — 2026-10-29

1. Dohoda [R-0345](requirements/done/R-0345.md): nikde ve hře ani v úvodním menu nezobrazovat plovoucí nápovědu po najetí nebo zastavení myši. Toto pravidlo nahrazuje starší požadavky na tooltipy uvedené níže i v historických analýzách.
2. Popisky a přístupné názvy zůstávají. Dosavadní kontext ve spodní stavové liště, vysvětlení přímo v nabídce rozkazů a samostatná herní nápověda zůstávají běžnými cestami k informacím; klávesové ovládání ani herní pravidla se nemění.


## Viditelný návrat do hry — 2026-10-29

1. [R-0344](requirements/done/R-0344.md): pravé návratové tlačítko v horní liště kolonie, Evropy, cílů, přehledů, zpráv tahu, Kongresu, nápovědy a herního Nastavení má šipku a společný text „Back to game“ / „Zpět do hry“. Popisek zůstává viditelný i v kompaktní liště; ostatní akce dál používají dosavadní pravidla přizpůsobení šířce.
2. Kliknutí vrací do stejné partie. Nastavení otevřené z menu zachovává „Back“ / „Zpět“ a návrat do menu. Vnořené dialogy ani odchod z partie tento popisek nepřebírají.
3. Dohodnuté chování prošlo lokálními UI kontrolami v angličtině a češtině, včetně menšího okna; výsledky a jejich omezení vede [evidence R-0344](functional-tests.md#r-0344-visible-return-labels-2026-10-29).

## Šířka obsahu herních lišt — R-0342

1. Dohoda z 29. října 2026: horní a dolní lišta mají společnou vycentrovanou vnitřní oblast širokou nejvýše **1 440 logických pixelů**. Pozadí a kreslené vodorovné hrany lišt pokračují přes celé okno; mapa si ponechává celou dostupnou šířku.
2. Na menších oknech se obsah přizpůsobí dostupné šířce s nejméně 16 logickými pixely na každé straně. Od vnitřní šířky okna 1 472 se obsah již neroztahuje. Text ani tlačítka se nezvětšují a stávající přechod na ikony zůstává zachovaný.
3. Hráč může okno kdykoli znovu zmenšit. Stejné pravidlo platí na mapě, v kolonii, Evropě, obchodních obrazovkách, přehledech, Kongresu, nápovědě a herním nastavení. Úvodní nabídka a samostatné dialogy nejsou součástí této úpravy.
4. [R-0342](requirements/done/R-0342.md) vlastní implementaci této dohody; [evidence](functional-tests.md#r-0342-bar-content-width-2026-10-29) odděluje návrhové rozhodnutí od skutečně provedených kontrol.

## Společné posouvání — R-0338

1. Posuvník se v evropských seznamech lodí a lidí objeví až při přetečení; prázdná i krátká skupina ponechává nadpis a aktivní oblast pro příslušné přetažení. Dlouhé skupiny se stále posouvají nezávisle a změna velikosti okna nesmí skrýt jejich konec.
2. Hráč pozná posuvník podle stejné kreslené dráhy a pískového jezdce v nápovědě, Evropě, ostatních obrazovkách i dialozích. Kolečko, klávesnice a tažení zůstávají nativní. Zobrazení lišty nesmí posunout lidi mimo molo, změnit výběr nebo zasáhnout do nákladu.
3. [R-0338](requirements/done/R-0338.md) vlastní tuto přijatou úpravu, [grafika](graphics.md#spolecne-kreslene-posuvniky-29-rijna-2026) je autoritativní pro vzhled. Jde o požadavek uživatele a následnou implementační kontrolu, nikoli novou hráčskou studii.

## Evropský přístav — R-0335 a R-0336

1. Přijatý směr z 28. října 2026: [Námořní skicář](graphics.md#evropsky-pristav-namorni-skicar-28-rijna-2026). Hráč vybere loď v levé části mola a v jeho stěně prohlíží či plní její náklad; lidé čekají vpravo, trh s prodejní/nákupní cenou je vždy pod kresbou.
2. Každá skupina má nadpis, aktuální počet a vlastní vodorovné posouvání. Průhledný ruční rámeček rozlišuje vybranou loď; identifikátor zůstává v popisku, přístupném názvu a detailu. Pod lodí není samostatné číslo. Posouvání nemění výběr ani náklad; prázdná skupina zůstává dosažitelným cílem příslušného přetažení.
3. Předpoklady a odmítnutí určují dosavadní pravidla. Výběr sám nic nekupuje; množství, potvrzení, zrušení, daně, bojkot a uzavření Evropy používají existující cesty. Menší okno posouvá stránku, nikoli zmenšené jednotky. Klávesnice nadále ovládá výběr, nabídky i návrat.
4. Náklad vybrané lodi tvoří šest plochých okrových beden přímo na kameni: obsah a množství, prázdná dostupná místa a kříž pro nedostupná. Bez lodi nadpis vyzývá k výběru. Přetahování používá původní pravidla a cíle.
5. Příkazy plující lodi se otevírají pravým tlačítkem nebo klávesou nabídky / Shift+F10; tooltip tuto cestu vysvětluje. Otevření vybere správnou loď. Escape zavře nabídku beze změny, potvrzení či zrušení operace zůstává v původním dialogu. Velká tlačítka pod plavbami se nezobrazují.
6. [R-0335](requirements/done/R-0335.md) vlastní původní implementaci, [R-0336](requirements/done/R-0336.md) její následné vizuální upřesnění a nabídku příkazů. Zde je zaznamenaná přijatá specifikace; nejde o novou studii ani pozorování skutečného hráče.

7. [R-0343](requirements/done/R-0343.md) přebírá uživatelem vybranou teplou paletu 7. Přistěhovalecký údaj je přímo na nebi bez světlého pásu; aktuální kříže, práh a případná čekající volba zůstávají čitelné v obou jazycích. Jde o vizuální úpravu existující cesty, bez nové akce či potvrzování. Skutečné kontroly jsou oddělené v [evidenci](functional-tests.md#r-0343-warm-harbor-2026-10-29).

## Kongres v hlavním okně — 28. října 2026

1. [R-0332](requirements/done/R-0332.md) přesouvá zvolené Karty do celé herní plochy
   uvnitř stávajícího okna, stejně jako nápovědu. Otevření nemění velikost okna
   ani systémový režim celé obrazovky a nevytváří další okno.
2. Horní Zpět do hry a Escape obnoví mapu bez potvrzení označené karty. Nové
   otevření zobrazí aktuální přehled od začátku. Pevný stavový řádek zachovává
   pokladnu a datum; dlouhý obsah se posouvá mezi ním a horní lištou.
3. Níže popsané karty, potvrzení a odmítnutí zůstávají. Skutečnou přejímku
   zaznamenává [evidence](functional-tests.md#r-0332-congress-screen-2026-10-28),
   nikoli návrhový náhled nebo hráčská studie.
4. Od 29. října 2026 doplňuje [R-0339](requirements/done/R-0339.md) na mapě
   klávesu **K**. Český i anglický kontextový popis tlačítka uvádí „(K)“
   stejně jako Evropa „(E)“. Klávesa otevře tentýž Kongres během připraveného
   tahu člověka; návrat nepotvrdí výběr ani nezmění herní stav.
   [Přejímka zkratky](functional-tests.md#r-0339-congress-shortcut-2026-10-29)
   je oddělená od původního ověření vzhledu.

## Karty Kongresu — 28. října 2026

1. [R-0331](requirements/done/R-0331.md) používá vybraný návrh **Karty**. Hráč otevře
   Kongres z mapy a porovná přínosy nabízených zakladatelů. Označení karty
   pouze zobrazí detail; samostatné tlačítko vedle detailu potvrzuje cíl zvonů.
2. Karty se podle šířky zalamují. Detail a potvrzení lze posunout do záběru;
   tlačítko návratu zůstává v horní liště. Escape zavírá bez potvrzení výběru.
   Odmítnutí zastaralé nabídky nadále vysvětlí potřebu znovu otevřít Kongres.
3. Národní podpora, zvony a členové jsou nahoře. Královské síly mají součty
   a rozbalitelné podrobnosti; Jonesova odměna a intervence zachovávají
   dosavadní zprávy. Před první kolonií a po získání všech zakladatelů
   zůstává příslušné vysvětlení místo nabídky.
4. Jde o zvolený návrh a jeho implementaci, nikoli výsledek hráčské studie.
   [Přejímka](functional-tests.md#r-0331-congress-cards-2026-10-28) zaznamenává
   skutečné testy a vizuální kontrolu odděleně.

## Jednoduchý spodní řádek — 27. října 2026

1. [R-0327](requirements/done/R-0327.md) zachovává vlevo existující popis objektu
   pod myší nebo nápovědu zaměřeného ovládání. Dlouhý text se zalamuje;
   údaje o pokladně a datu zůstávají vpravo čitelné a nepřekrývají jej.
2. Kreslené mince nahrazují slovní předponu zlata. Částka používá nezalomitelné
   mezery mezi tisíci; přístupný název stále obsahuje lokalizované Zlato/Gold.
   Stávající nápověda po najetí na peníze a datum zůstává zachovaná.
3. Datum začíná `AD`, následuje rok a případné lokalizované roční období.
   Změny zlata, tahu a jazyka i Pokračovat používají skutečný modelový stav.
   Jde o informační panel bez nových akcí. Vzhled určuje
   [grafické pravidlo](graphics.md#spodni-radek-cista-linka-27-rijna-2026).
4. Přejímku uvádí [funkční evidence](functional-tests.md#r-0327-clean-status-line-2026-10-27);
   nejde o pozorování hráčů při uživatelském testování.


## Nápověda v hlavním okně — 2026-10-27

1. [R-0324](requirements/done/R-0324.md) sjednocuje Help s Cíli: tlačítko na mapě i H
   otevřou celou herní plochu uvnitř stávajícího okna. Jeho velikost ani systémový
   režim celé obrazovky se nemění; nevzniká další okno.
2. Po otevření je hledání prázdné a fokus má seznam témat. Seznam i článek využívají
   dostupnou výšku, článek se rozšiřuje s oknem. Dlouhý obsah má vlastní posouvání.
   České i anglické texty, stav bez výsledků a obrázkové tabulky zůstávají dostupné.
3. Horní OK a Escape vracejí hráče na mapu bez změny partie. Sdílený stavový panel
   zůstává viditelný. [Evidence](functional-tests.md#r-0324-full-window-help-2026-10-27)
   odděluje automatické a vizuální kontroly od případného pozorování hráčů.
4. Následná [Přímá úprava R-0325](requirements/done/R-0325.md) umisťuje název do horní lišty a hledání pouze nad rejstřík. Článek začíná vedle hledání; při nenalezeném výrazu lze hledání upravit či vymazat. Kreslené ovládání zachovává textový fokus, kolečko a tažení posuvníku. Jde o rozložení zvolené uživatelem, nikoli výsledek hráčské studie.

## Atlas přehledů ve Statistikách — 2026-10-27

1. Hráč z mapy otevře Statistiky, vybere jeden z dvanácti přehledů v levém
   rejstříku a čte jeho obsah vpravo. [R-0323](requirements/done/R-0323.md) navazuje
   na uživatelem vybraný Atlas. Existující akce reportů mají stejné důsledky
   a potvrzení; prohlížení a návrat nemění tah ani zlato.
2. Výběr zůstává viditelný i po opakovaném kliknutí. Rejstřík se posouvá
   samostatně a při ovládání klávesnicí odhalí fokusovanou položku. Escape
   i horní Zpět vedou na mapu; spodní stav zůstává dosažitelný.
3. Souhrn grafu výslovně označuje poslední historický záznam; není příslibem
   stejné hodnoty v aktuální pokladně. Prázdná historie má vysvětlení a jeden
   záznam se ukáže jako bod. Další otevření přehled obnoví ze skutečné hry.
4. [Evidence](functional-tests.md#r-0323-statistics-atlas-2026-10-27) rozlišuje
   automatické kontroly a vizuální přejímku. Snazší orientace je návrhový záměr,
   nikoli nové pozorování hráčů.


## Obrys modálních oken — 27. října 2026

1. [R-0322](requirements/done/R-0322.md) mění pouze vnější vizuální hranici modalu
   podle [zvolené varianty](graphics.md#rucne-kreslena-modalni-okna-27-rijna-2026).
   Hráč otevírá stejné dialogy ze stejných míst, čte stávající obsah a potvrzuje
   či ruší původními tlačítky a klávesami. Výsledek akce i důvody odmítnutí
   zůstávají u dosavadního vlastníka.
2. Obsah uvnitř obrysu zakrývá podklad; průhledné okolí není novou cestou
   k ovládání zablokovaného rodičovského okna. Stejně fungují vnořená
   potvrzení, Escape i návrat po zavření. Návrh vzhledu není pozorováním
   hráčů; skutečná přejímka je v [evidenci](functional-tests.md#r-0322-hand-drawn-modal-silhouettes-2026-10-27).

## Režim vývojáře — 2026-10-27

1. [R-0311](requirements/done/R-0311.md) přidává v Nastavení sekci Vývojář s volbou
   „Režim vývojáře (F3)“ a stručným vysvětlením. Přepínač je dostupný z hlavního
   menu i rozehrané hry. Nový profil začíná s vypnutou volbou; poslední stav se
   ukládá do nastavení aplikace, nikoli do partie.
2. Podle navazujícího [R-0312](requirements/done/R-0312.md) F3 mění stejnou volbu
   na každé obrazovce hry, včetně úvodního menu a herních dialogů. Přepne se
   při uvolnění klávesy, takže držení režim opakovaně nepřepíná. Funguje také
   při psaní do textového pole, aniž změní jeho obsah nebo zavře dialog.
   Kombinace s modifikátory a systémová okna pro výběr souborů si ponechávají
   vlastní ovládání. Na Macu může být nutná Fn+F3.
3. Stav režimu ukazuje zaškrtávátko v Nastavení. Stavový řádek neobsahuje
   značku DEV ani dočasnou zprávu o zapnutí/vypnutí. Běžný popis pole zůstává;
   zapnutý režim přidá `x=12, y=8 · …` i bez pohybu myši. Souřadnice odpovídají
   mapě: levý horní roh `[1, 1]`, x doprava a y dolů. Opuštění mapy odstraní
   souřadnice; neprozkoumané pole neprozradí terén ani skryté objekty.
4. Změna nepotřebuje potvrzení ani restart a neruší připravený rozkaz jednotky.
   [Přejímka R-0312](functional-tests.md#r-0312-global-developer-shortcut-2026-10-27)
   prošla dne 27. října 2026. Automatické testy a kontrola snímků nejsou novým pozorováním hráčů.

## Klidná hladina jezera — K3, 27. října 2026

1. Hráč při otevření nebo obnovení mapy rozpozná souvislé jezero; jeho dekorace
   nevytváří další malá jezírka. Vybraná [animace K3](graphics.md#jezero-se-tremi-kruhy-k3-27-rijna-2026)
   přidává pouze tři jemně se rozbíhající kruhy. Není potřeba nová akce,
   potvrzení ani cesta ke zrušení; ovládání mapy a rychlosti animace zůstává stejné.
2. Břeh a ryby mají přednost před pohybem hladiny. Voda nesmí vyzrazovat skrytou
   mapu ani měnit dostupnost plavby a rybolovu. Kruhy přirozeně doznějí před
   další volbou dekorace a nevydávají zvuk.
3. Volba K3 je rozhodnutí uživatele. Automatické kontroly a stav přijetí jsou
   v [evidenci R-0314](functional-tests.md#r-0314-k3-lake-ripples-2026-10-27);
   nejde o nové pozorování hráčů ani změnu pravidel.

## Čitelné hranice krajiny — R-0294

1. Hráč při otevření nové i rozehrané mapy uvidí souvislé barevné oblasti s mírně nepravidelným okrajem podle [grafického pravidla](graphics.md#nepravidelne-hranice-terenu-26-rijna-2026). Střed políčka, název pod kurzorem a stávající výběr zachovávají přesnou informaci o terénu; klikání a pohyb stále patří čtvercové herní mřížce.
2. Změna je automatická a nepřidává nastavení, potvrzení ani akci ke zrušení. Posun, průzkum, návrat do menu a Continue zachovávají současnou obsluhu. Nově objevená souš může doplnit společnou hranici; skrytý typ nesmí ovlivnit známé barevné plochy.
3. Přirozenější vzhled je výtvarný záměr. [Automatická a vizuální evidence](functional-tests.md#r-0294-terrain-boundaries-2026-10-26) se nepovažuje za pozorování skutečných hráčů.

## Vlna s dopadem a rozplynutím — 26. října 2026

1. [R-0293](requirements/done/R-0293.md) přebírá schválené „Široké rozlití“: převis padá dopředu, dopadne a rozpustí se do hladiny. Statická ukázka vždy obsahuje čitelný převis; název terénu, bonusy a směrová značka dalekého moře zůstávají dostupné.
2. Pohyb je automatický a používá běžné otevření mapy, posun a Continue. Nepřidává ovládání, potvrzení ani zvuk. Výběr návrhu uživatelem je podkladem vzhledu; technické ověření a dosud neprovedené pozorování při hraní rozlišuje [funkční evidence](functional-tests.md#r-0293-wide-wash-2026-10-26). Nahrazuje pohyb předchozí varianty 04 popsané níže.


## Rozpoznatelné vlny — 2026-10-26

1. Podle [R-0289](requirements/done/R-0289.md) hráč pozná moře podle tenké nízké
   přelévající se vlny; vhodná políčka dalekého moře mají značku vždy, také
   ve statickém náhledu. Název terénu a významové bonusy zůstávají dostupné.
   Změna je automatická, nevyžaduje nastavení ani potvrzení a nepřidává akci
   k rušení. Posun mapy, návrat a Continue používají dosavadní ovládání.
2. Lepší rozlišení vody od kopců je výtvarný záměr podle uživatelovy volby 04.
   [Automatické a vizuální ověření](functional-tests.md#r-0289-rolling-waves-2026-10-26)
   je oddělené od dosud neprovedeného pozorování hráčů.

Nastavení podle [R-0248](requirements/done/R-0248.md) zachovává okamžité účinky
voleb a návrat tlačítkem Zpět. Dřevěné rámečky rozlišují nezávislý výběr
fajfkou a jedinou volbu tečkou; pravítko ukazuje hodnotu polohou jezdce.
Klávesnice, fokus a čitelnost se ověřují na skutečné obrazovce, nikoli jen
na návrhu; výsledky jsou ve [funkční evidenci](functional-tests.md).

MicroCol má zachovat strategickou hloubku a současně hráči pomáhat pochopit jeho možnosti, ovládání a důsledky rozhodnutí. Při nové funkci proto řešíme také to, kde ji hráč najde, jak ji použije a jak pozná výsledek.

Tato stránka je společným místem pro pravidla UX, popis současného ovládání a zjištění z jeho ověřování. Navazuje na [grafické zadání](graphics.md#soucasne-zadani-vzhledu) a [pravidla zvukové odezvy](audio.md). Platná pracovní dohoda z **11. října 2026**. Technická pozorování níže vycházejí z kódu na větvi `develop`, revize `77f75eb03c74f07ad0253595d14fb0b31ac6e78a`; nejsou výsledkem pozorování hráčů.

Výtvarné provedení společných prvků od **18. října 2026** určuje
[minimalistický grafický manuál](graphics.md#minimalisticky-graficky-manual),
včetně stavů ovládání a tloušťky čar při výsledné velikosti UI.

Pro běžná tlačítka uživatel vybral [dřevěnou tabulku bez bočních zářezů](graphics.md#drevena-tlacitka-bez-bocnich-zarezu).
[R-0246](requirements/done/R-0246.md) zachovává existující umístění akcí,
potvrzení a zrušení i vlastní vzhled individuálně upravených tlačítek.
Nejde o uživatelskou studii ani přepracování modalit; skutečné ověření
myši, klávesnice a výsledků vede [funkční evidence](functional-tests.md#r-0246-drevena-tlacitka-2026-10-18).

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
| Výuka a následná nápověda | [Default_0_mission](../microcol-game/src/main/java/org/microcol/model/campaign/Default_0_mission.java) obsahuje kontextové instrukce k počátečním úkolům. Tlačítko na mapě a klávesa H otevírají [ColonizopediaDialog](../microcol-game/src/main/java/org/microcol/gui/screen/colonizopedia/ScreenColonizopedia.java) s tématy pravidel, hledáním a zprávou o prázdném výsledku ([R-0206](requirements/done/R-0206.md)); cílené automatizované ověření prošlo, společná regrese dávky ještě následuje. | Zda hráč zvládne první kolonii a později dokáže znovu dohledat zapomenutý postup. Existence třídy dialogu neprokazuje dostupnou a úplnou nápovědu. |

Při vstupu do hry mají zaměřené pole a skutečný výběr jednotky odpovídat:
[R-0310](requirements/done/R-0310.md) vybírá vlastní jednotku nezávisle na úvodním
tahu AI, aby hráč mohl po převzetí tahu rovnou použít `M` nebo tažení myší.
Výběr sám neprovádí pohyb ani neobchází omezení tahu. Stav ověření je vedený
ve [funkční evidenci](functional-tests.md#r-0310-initial-free-play-movement-repair-2026-10-27).

### Přehled práce — R-0198

1. Cíl hráče: zjistit, kolik kolonistů jednotlivých typů má národ a kde každý
   stojí, bez hledání na mapě. Přehled je v **Statistiky → Práce**; vybraný typ
   vypíše své osoby s číslem a místem.
2. Tlačítko **Zobrazit** otevře kolonii, dlaždici mapy nebo evropský přístav.
   Pokud místo nemá dostupný cíl (plavba, domorodé sídlo, ostatní místa),
   tlačítko je vypnuté a text místa to vysvětlí. Zavření přehledu klávesou
   Escape nic nezmění a nespotřebuje tah.
3. Osoba, která mezitím zanikla, byla zajata nebo už není v národě, nic
   neotevře; přehled se obnoví a zobrazí se srozumitelné hlášení.
4. Stav je zatím ověřen jen modelovými testy; cílený UI scénář TC166 je napsán,
   ale dosud nebyl spuštěn.

### Náboženský přehled — R-0199

1. Cíl hráče: zjistit, kolik křížů dává každá kolonie v příštím tahu, kolik
   jich národ už nasbíral a kolik chybí do dalšího přistěhovalce. Přehled je
   v **Statistiky → Náboženství**.
2. Aktuální výstup, nasbíraný postup a stav získaného přistěhovalce mají vlastní
   nadpisy. Kolonisté v kapli nebo kostele se projeví okamžitě, bez posunu tahu;
   nasbírané kříže se tím nemění.
3. Přistěhovalec, který dorazí na začátku příštího tahu, čeká na výběr v Evropě,
   nebo se u národa, který vyhlásil nezávislost, nepřipíše. Kříže v posledním
   případě zůstávají uložené a přehled to říká textem. Otevření, zavření ani
   opětovné otevření přehledu nic nestojí.
4. Záložka přibyla do postranní lišty statistik, která má nyní deset položek.
   Výška lišty, klávesová navigace a čitelnost na snímcích zatím nejsou ověřeny.
   Stav ověření a známé mezery vede [testová evidence](functional-tests.md#r-0199-religion-report-written-not-verified-2026-10-23).

### Přehled kolonií — R-0200

1. Cíl hráče: zjistit, kolik kolonistů má každá kolonie, co zůstane v zásobě,
   co se vyrobí a spotřebuje a co se při dalším tahu změní, bez otevírání každé
   kolonie. Přehled je v **Statistiky → Kolonie**.
2. U každé kolonie je název s počtem kolonistů, věta o růstu nebo hladu, tabulka
   zboží (zásoba, výroba, spotřeba, utraceno nebo prodáno, změna a příští stav)
   a věta o projektu. Význam nesou slova a čísla; barva nestačí. Tabulka začíná
   zásobou, změnou a příštím stavem; nadpisy se nelámou uvnitř slov a širší
   tabulka se posouvá vodorovně, zatímco věty se zalamují ve viditelné šířce.
   Tlačítko **Otevřít kolonii** stojí vedle názvu kolonie a otevře ji, pokud je
   ještě vlastní a platná. Jinak
   přehled obnoví a vysvětlí, že kolonie už není vaše.
3. Projekt, který při dalším tahu nemá dost kladiv nebo nástrojů, ukazuje stejný
   chybějící počet nástrojů jako hlášení tahu; přehled žádné nové upozornění
   nevytváří. Při nedostatku jídla přijde náhodný kolonista o život dřív, než se
   přepočte výroba a projekt. Přehled proto u takové kolonie ukazuje zásobu
   a upozornění, ale místo výroby, spotřeby, změny a příštího stavu jen `?`. Otevření, zavření ani Pokračovat nic nestojí a nic nespotřebuje.
4. Stav: model a scénář TC_168 jsou napsány, ještě nebyly spuštěny. Záznam
   ověření vede [evidence](functional-tests.md#r-0200-colony-advisor-written-not-verified-2026-10-23).

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
Aktuální stav přejímky vede [R-0076](requirements/done/R-0076.md).

### Cena opakovaného nákupu (R-0077)

Nabídka v Evropě ukazuje aktuální celkovou cenu před potvrzením. Další nákup
stejného typu lodě zvýší jeho další cenu; cena jiného typu se nemění. Zrušení
nabídky nic neúčtuje. Pokud se nabídka změní před potvrzením, dialog nákup
odmítne a vyzve k opětovnému otevření. Neúčtuje automaticky vyšší cenu.
Nedostatek peněz ponechá nabídku otevřenou s nedostupným tlačítkem a vysvětlením.
Historie cen přetrvá přes Pokračovat; stav přejímky vede
[R-0077](requirements/done/R-0077.md).

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
hráčské ověření vede [R-0082](requirements/done/R-0082.md).

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
4. Ověření vede [R-0104](requirements/done/R-0104.md); popis není samostatným
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

### Přehled tahu a Evropa — R-0204

1. Cílem hráče je vědět, co se během tahu stalo, a vybrat další krok. Rutinní
   přehled se otevírá na začátku tahu; v Nastavení jej lze vypnout. Hlad, ztráta
   nebo zánik kolonie se otevřou vždy.
2. U přistání lodi v Evropě nabízí přehled tlačítko „Otevřít Evropu“. Otevření
   a zavření přehledu nic nespotřebují; zastaralý přehled po změně hry vrátí na
   mapu, nikoli do cizí hry.
3. Nastavení nabízí dvě volby: rutinní přehled tahu (výchozí zapnuto) a automatické
   otevření Evropy po příjezdu lodi (výchozí vypnuto). Když se přehled otevře, je
   první; automatická Evropa čeká za ním a otevře se až po jeho zavření nebo Escape.
   Bez přehledu se Evropa otevře přímo. Kritické zprávy přehled otevřou i při
   vypnutém rutinním přehledu. Nové tlačítko Otevřít Evropu nic nespotřebuje.
   Oznámení o dokončeném projektu, o zahozeném zboží (kolonie, zboží a množství)
   a o narození jmenují kolonii a nabízejí Otevřít kolonii; zaniklou nebo cizí
   kolonii akce odmítne a nic nezmění. Anglické texty zahozeného zboží a narození
   byly nesprávné (kolonie na místě zboží, chybějící kolonie) a byly opraveny.
4. Stav je napsán a částečně doložen jen v proběhlém běhu bez druhého pokusu:
   [evidence R-0204](functional-tests.md#r-0204-turn-notices-attempt-3-written-not-verified-2026-10-23).
   Nejde o pozorování hráčů; čitelnost bez zvuku a barvy zatím není ověřena.

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
Přejímka hráčských scénářů je vedená v [R-0099](requirements/done/R-0099.md).

### Povýšení vítězného vojáka — R-0111

Po boji hráč uvidí vedle ztráty vybavení poraženého také skutečný krok povýšení
vítěze. Informace o jednotce trvale ukazují veteránský stav odděleně od civilní
odbornosti. Následující bojové potvrzení již používá vyšší základní sílu.
Zrušení útoku nepovyšuje; Continue zachová výsledek bez dalšího losování.
Stav hráčské přejímky vede [R-0111](requirements/done/R-0111.md).

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


### Hlášení čekající stavby — 17. října 2026

[R-0086](requirements/done/R-0086.md) přidává k hlášení chybějících nástrojů
akci Otevřít kolonii. Hráč zjistí projekt i chybějící množství v přehledu tahu;
myš nebo Tab/mezerník jej přenesou k zásobám a stavební frontě beze změny
herních zdrojů. Zavření přehledu nic nepotvrzuje ani nespotřebovává. Po ztrátě
kolonie se akce odstraní; odkaz ze staré hry neotevře kolonii v nové hře.
Český automatický průchod a kontrola screenshotu odhalily zkrácený popisek
a přesah tlačítka. Řádek s akcí nyní ponechává místo celému tlačítku a zalamuje
text hlášení. [Testová evidence](functional-tests.md#r-0086-construction-waiting-notice-2026-10-17)
rozlišuje ověření v aplikaci od dosud neprovedeného ověření s hráči.

### Stabilita mapy při změně výšky — 2026-10-22

Při zalomení stavového textu nebo svislé změně velikosti okna zůstává horní řádek mapy na místě, pokud to dovolují hranice mapy. Mapa menší než plátno zůstává vystředěná; vodorovné změny zachovávají dosavadní centrování. Tím se při změně výšky stavové zprávy neposune políčko pod ukazatelem. Stejný výpočet používá editor. Cílené ověření a jeho omezení uvádí [základ pokladu R-0175](functional-tests.md#r-0175-treasure-foundation-2026-10-22); úplná regrese ještě následuje.

## Odchod a ukončení bodované hry — R-0194 (2026-10-23)

1. Tlačítko **Odejít** (Exit) zůstává beze změny: uloží rozehranou hru a vrátí do
   menu. Nic nepřepočítává a nic nezapisuje do síně slávy.
2. **Ukončit s výsledkem** (Retire) se nabídne jen v běžící bodované volné hře na
   vlastním tahu. Mimo tento případ tlačítko není vidět. Při čekání na volbu jara
   1800, v misi a po konci hry se nenabízí.
3. Potvrzení ukáže přesný výsledek, který model právě spočítal, a složky základu,
   odečtení, zvony a obtížnost. Vysvětlí důsledek: živé ukončení zapíše nový
   výsledek do síně slávy; pokračování bez bodování zachová dosavadní zapsaný
   výsledek a nic nepřidá. **Ponechat hru** nebo Escape nic nezmění.
4. Po potvrzení hra skončí, zobrazí se výsledek se složkami a místem v síni slávy
   a poté hlavní obrazovka kampaně. Zastaralý náhled model odmítne a hráč dostane
   vysvětlení; nic se nepotvrdí tiše.
5. Stav ověření je v [evidenci R-0194](functional-tests.md#r-0194-retirement-implementation-2026-10-23-not-verified):
   napsáno, nespuštěno. Nejde o uživatelské ověření.


## Čitelné kontextové nabídky — R-0271

Název jednotky je nevybíratelný nadpis bez barevného fokusu. Jediná čára
pod ním odděluje identifikaci od příkazů; menší mezery nemění velikost písma
ani vysvětlení nedostupných akcí. Viz [R-0279](requirements/done/R-0279.md).

1. Nabídka pojmenuje každou akci a případnou skutečnou zkratku oddělí do
   rámečku vpravo. Zkratku nevkládáme do závorky v názvu ani ji nevymýšlíme
   pro akci, která ji nemá. [R-0276](requirements/done/R-0276.md) sjednocuje
   jediný ruční obrys fokusu v kontextové nabídce a seznamu kolonií; myš
   i klávesnice zvýrazní celou volbu stejnou teplou výplní a inkoustovým
   rámečkem. Nedostupné položky zachovají vysvětlení.
   Samotná existence zkratky na mapě neopravňuje
   ukazovat ji jako dostupnou v kolonii či Evropě.
2. Společné [grafické provedení](graphics.md#kontextove-nabidky-r-0271)
   zachovává rozpoznatelné zaměření a nedostupnost. Myš i klávesnice vedou
   ke stejné stávající akci; Escape a kliknutí mimo nabídku ji zavírají bez
   změny herního stavu. Přesun příkazů jednotky do nové nabídky vlastní
   samostatný požadavek R-0270.
3. Jde o implementační pravidlo [R-0271](requirements/done/R-0271.md), nikoli
   pozorování hráčů. Ověření uvádí [funkční evidence](functional-tests.md#r-0271-context-menus-2026-10-25).


### Příkazy jednotky přímo na mapě — R-0272

1. Upřesnění uživatele z 25. října 2026 v [R-0272](requirements/done/R-0272.md)
   nahrazuje ovládání přes kartu jednotky z R-0270. Horní lišta obsahuje
   přehledy, navigaci, ukončení tahu a celopartijní akce. Pravý panel je pouze
   informační: ukazuje jednotky, pohyb, náklad a stav cesty; neobsahuje tlačítko
   Akce ani výběr jednotky kliknutím na kartu.
2. Pravé kliknutí nebo Ctrl + levé kliknutí na vlastní jednotku v mapě
   otevře její příkazy u místa kliknutí. Pokud je na políčku více vlastních
   jednotek, jejich výběr je součástí této nabídky. Prázdné políčko ani cizí jednotka vlastní příkazy
   nenabízejí. Při zaměřené mapě vyvolá nabídku vybrané jednotky také Shift+F10
   nebo klávesa kontextové nabídky.
3. Na políčku kolonie levé kliknutí nadále otevře město; pravé kliknutí otevře
   příkazy jednotky bez vstupu do města, včetně lodi v přístavu. Pravé tažení
   mapu posouvá a nabídku neotevírá. Pravé kliknutí nepotvrdí rozpracovaný pohyb.
4. V nabídce slouží šipky k pohybu mezi příkazy, Enter nebo mezerník k aktivaci
   a Escape k zavření. Na mapě zůstávají původní zkratky: Tab vybere další
   jednotku, mezerník vyčerpá její pohyb, šipka Další tah nahoře ukončí tah hráče.
   Při fokusu v horní liště slouží Tab k přechodu mezi ovládacími prvky a mezerník
   k aktivaci zaměřeného tlačítka (doplnění R-0273).
   Nedostupné relevantní příkazy mají vysvětlení a původní potvrzovací dialogy.
5. Podporu Ctrl + levého kliknutí doplňuje [R-0275](requirements/done/R-0275.md).
   Rozpoznává se před běžnou akcí levého tlačítka, také při uvolnění Ctrl
   před tlačítkem myši. Alt + kliknutí zachovává plynulé zaměření políčka.
6. Skutečné kontroly zaznamenává [funkční evidence R-0272](functional-tests.md#r-0272-map-unit-context-2026-10-25).
   Jde o implementační pravidlo, nikoli pozorování hráčů; historické výsledky
   R-0270 zůstávají zachované.


## Horní lišta 07B — 2026-10-25

1. [R-0273](requirements/done/R-0273.md) používá ikonku a trvalý lokalizovaný název u každé globální akce. Najetí nebo klávesnicové zaměření zobrazí vysvětlení dole. Po O/Escape je fokus na výběru kolonií; Tab postupuje mezi prvky lišty, Shift+Tab se vrací a mezerník aktivuje zaměřené tlačítko bez vyčerpání pohybu jednotky. Fokus je navíc označen přerušovaným rámečkem; barva sama nenese stav akce. Playpen Sans Regular 14 se nemění.
2. Kolonie jsou přímá navigační nabídka s vlastním symbolem, rámečkem a oddělenou šipkou. Klávesa O ji otevře, kurzorové klávesy vyberou položku, Enter otevře kolonii; Escape nebo kliknutí mimo nabídku ji zruší. Prázdný seznam vysvětlí, že zatím není dostupná kolonie. Položky se při otevření znovu načtou a při výběru ověří vlastník i platnost.
3. Další tah, odchod do menu, ukončení se skóre a podmíněná nezávislost zachovávají dosavadní pravidla a potvrzení. Lišta se v užším okně zalomí nad mapou a nezkracuje názvy. Zimní modrá a ostatní zelená slouží atmosféře; neomezují objevitelnost akcí.
4. Automatizovaná přejímka a technická vizuální kontrola jsou v [evidenci](functional-tests.md#r-0273-toolbar-07b-2026-10-25). Nejde o uživatelskou studii.

## Čitelnost řídké krajiny — 25. října 2026

1. [R-0277](requirements/done/R-0277.md) umožňuje rozpoznat terén podle stálé barvy, s řidší kresbou podle [grafických pravidel](graphics.md#ridsi-znacky-terenu-a-ziva-voda-25-rijna-2026). Hráč změnu vidí při otevření mapy bez dalšího nastavení; posun a Continue zachovávají rozmístění na pevnině.
2. Les a ostatní herně významné atributy mají přednost před dekorací. Kopce a hory si ponechávají reliéf; pohybující se vlny nemění stav ani dostupnost políčka. Terén lze nadále pojmenovat ukázáním na políčko a porovnat v nápovědě, kde je kresba vždy přítomná.
3. Předpoklad klidnější a přehlednější mapy je návrhová hypotéza. Automatické vykreslovací kontroly a prohlídka snímků jsou evidované v [testech](functional-tests.md#r-0277-sparse-terrain-2026-10-25); nejsou studií hráčů ani ověřením všech poruch barvocitu.

## Šířka herního menu — 2026-10-25

Dohoda [R-0283](requirements/done/R-0283.md): hráč má při zmenšení okna dál vidět celý horní řádek akcí a co největší část mapy. Názvy se automaticky skryjí všechny současně před zalomením a po rozšíření se vrátí; není potřeba nic přepínat ani potvrzovat. Význam ikon dál vysvětluje stávající nápověda při najetí a zaměření klávesnicí, názvy pro asistivní technologie zůstávají. Výběr kolonie, zrušení menu a výsledky příkazů fungují stejně. Minimum šířky okna chrání dostupnost všech ikon. Jde o dohodnuté chování a místně ověřenou implementaci, nikoli pozorování hráčů; [ověření](functional-tests.md#r-0283-one-row-toolbar-2026-10-25) je samostatné.


## Přehled jednotek v nápovědě — 2026-10-25

[Units / Jednotky](requirements/done/R-0284.md) používá stejné téma a dvousloupcový
přehled jako terény. Hráč otevře nápovědu tlačítkem nebo H, vybere téma či
vyhledá název jednotky, porovná obrázek a vlastnosti a vrátí se přes OK nebo
Escape. Základní parametry typu jsou oddělené od účinků výstroje a bojové role;
nedostupný nákup není označen cenou nula. Dlouhý seznam se zalamuje a roluje.
Automatizované kontroly a vizuální přejímku eviduje
[funkční záznam](functional-tests.md#r-0284-unit-help-2026-10-25); nejde o studii hráčů.


## Jednotky v pohybu — návrh a ověření R-0285

1. Uživatel vybral 25. října 2026 sadu 09 — Lehký pohyb a požadoval čitelný tah. Hráč má na mapě poznat typ jednotky a aktuální výbavu; profesní rekvizita sama nepřidává žádnou herní schopnost. Obrázky sdílí mapa a existující panely i nápověda; pojmenování a modelové podmínky akcí zůstávají autoritativní.
2. Cesta zůstává stejná: hráč jednotku vybere na mapě nebo v panelu, použije stávající příkaz či gesto a uvidí výsledek v mapě a stavových údajích. Změna obrázku nepřidává potvrzení ani nový způsob rušení. Skutečné koně, muškety, nástroje a poškození mají při výběru obrázku přednost před profesí.
3. Zřetelný tmavý obrys, průhledný okraj a vlastní siluety jsou návrhová volba. [Funkční evidence](functional-tests.md#r-0285-walking-unit-artwork-2026-10-25) zaznamenává technické kontroly a skutečný snímek hry; nejde o uživatelskou studii ani měření úspěšnosti rozpoznávání profesí.


## Oddělení hlavních panelů — R-0286

1. Schválená tenká ruční linka pomáhá rozlišit mapu, nabídku akcí, informace o výběru a stav hry. Má stejný tah jako tlačítka a nepřidává ovládací prvek ani potvrzení. Hráč dál vybírá jednotku, používá stávající akce a čte jejich odezvu v panelech.
2. Návrh zachovává čitelnost textu, fokus, hover vysvětlení a obsluhu při změně velikosti okna i po Continue. Vizuální volba uživatele je doložená tímto zadáním; automatické kontroly a skutečné snímky vede [funkční evidence](functional-tests.md#r-0286-main-screen-separators-2026-10-25). Nejde o uživatelskou studii.


## Návaznost mapy na linky — R-0288

1. Podle upřesnění uživatele z 25. října 2026 nemá být mezi mapou a oddělovací linkou světlá mezera. Oprava zachovává stávající hráčskou cestu, fokus, stavové zprávy i ovládání; nevytváří novou akci ani potvrzení.
2. [Funkční evidence](functional-tests.md#r-0288-map-separator-seams-2026-10-25) ověřuje technické provedení v širokém anglickém a úzkém českém okně. Nejde o novou hráčskou studii.

### Síla kresby mapy — 26. října 2026

[R-0291](requirements/done/R-0291.md) přebírá druhý vybraný návrh: lesy, pobřeží, bonusy a kolonie mají být stejně snadno rozpoznatelné jako ikony horní lišty. Cílem je optické sjednocení kontur při zachování velikosti objektů, čitelné výplně a podružné mřížky. Nezavádí nový ovladač ani potvrzování; otevření hry, pohyb pohledu a Continue používají stávající cestu. Lepší čitelnost je návrhový předpoklad, nikoli výsledek hráčské studie; aktuální kontrola patří do funkční evidence.


## Úvodní menu — R-0297

1. Dohoda 26. října 2026: hráč hledá pokračování, obnovu, načtení, kampaň, novou hru, síň slávy, nastavení nebo ukončení v jednom sloupci vlevo. [R-0297](requirements/done/R-0297.md) sjednocuje tlačítka s herní lištou; dostupnost akcí a jejich následné obrazovky nemění.
2. Krátká ikona a úplný jednořádkový název sdělují akci. Když se název nevejde, zůstává ikona a tooltip se stejným názvem; přístupný název se neskrývá. Při zvětšení se popisek vrací. Zaměření klávesnicí a stavy tlačítka přebírají společný styl lišty.
3. Návrat z Nastavení vede do stejného menu i po změně jazyka. Continue bez dostupné poslední hry zůstává nepřístupné podle stávajících pravidel. Nejde o změnu výsledků hry ani o výsledek hráčské studie; ověřené scénáře a omezení uvádí [evidence](functional-tests.md#r-0297-opening-menu-2026-10-26).

## Ukládání a nabídka rozehrané hry

Vybrané chování z 26. října 2026 popisuje jediná autoritativní
[stránka ukládání a navigace](save-load-navigation.md). Nabídka hry využívá
stejné rozbalovací prvky jako výběr kolonií. Ruční soubory, rychlé pozice a body
obnovy mají jeden přehled a filtr; import/export jsou doplňkové akce.
Zrušení načítání, přepsání, smazání i odchodu zachová původní stav. Nastavení
vrací do obrazovky, ze které bylo otevřeno. Ochrana se v první implementaci
ptá při každé aktivní hře; sledování nezměněného stavu zůstává budoucím
zlepšením. Implementační kontrola ani automatický test nejsou pozorováním
skutečného hráče; výsledky patří do [funkční evidence](functional-tests.md).

## Jednotný rám herních obrazovek — 2026-10-27

1. [R-0307](requirements/done/R-0307.md) sjednocuje cíle, přehled tahu, statistiky
   a jejich záložky, kolonii, Evropu, obchod, nastavení během hry, Kongres
   a nápovědu. Hráč otevře obsah dosavadním tlačítkem nebo klávesou; horní
   lišta drží dostupné akce a návrat, spodní ukazuje aktuální zlato a datum.
2. Obsah se posouvá mezi lištami. Menší okno nezakrývá návrat ani stavový
   řádek; rozsáhlé plochy lze posouvat také vodorovně. Kolonie a Evropa
   zachovávají své pracovní plochy, reporty používají čitelnou papírovou kartu.
3. Zrušení, potvrzení, klávesové zkratky a pravidla akcí se nemění. Kongres
   a nápověda zůstávají modální; úvodní menu má svůj dosavadní vzhled.
   Jde o implementaci uživatelského zadání, nikoli o výsledek studie hráčů.
   Skutečnou přejímku vede [funkční evidence](functional-tests.md#r-0307-in-game-screen-frame-2026-10-27).

## Herní pohledy a výška lišt — návrh 2026-10-29

1. **Terminologie:** herní pohledy jsou celoplošné obrazovky rozehrané partie: mapa, kolonie, Evropa, nákup/prodej, Kongres, cíle, přehled tahu, statistiky, nápověda a nastavení během partie. Jejich společné lišty tvoří herní rám. Úvodní stránky, založení hry, editor a běžné dialogy jsou mimo tuto skupinu.
2. **Vybraný krok 29. října 2026:** podle dodaného snímku mapy mají všechny horní lišty pevnou výšku **72 logických pixelů JavaFX**, tedy shodné minimum, preferovanou i maximální výšku. Nepřidáváme vizuální prvky ani nové názvy stránek; zachováváme akce a obsah jednotlivých pohledů.
3. **Jeden vlastník stylu:** `GameScreenBars.css` určuje výšku, odsazení, pozadí, font a kreslený okraj lišt. `MicroCol.css`, `GamePanel.css` a `ButtonsPanel.css` jej importují; jednotlivé pohledy lišty dále nestylují. Navazující zadání téhož dne sjednocuje levé i pravé odsazení obou lišt na 16 logických pixelů v jediné společné části `GameScreenBars.css`; obě úplné deklarace jsou vedle sebe a jiné styly je nepřepisují. Vnořené kontejnery nepřidávají další okraj a údaj o roce má nulové vlastní pravé odsazení. Obsah stránky má nadále vlastní rozvržení.
4. **Spodní lišta je nezávislá:** zachovává minimum 72, odsazení 12 nahoře/dole a 16 po stranách a zalamování nápovědy v `StatusBarView`. Není uzamčená na výšku horní lišty a může růst s dlouhou nápovědou. Vlevo zůstává kontextová zpráva, vpravo zlato a rok.
5. **Nedostatek šířky:** horní akce mají jednu řadu; společný panel přebírá [existující chování mapy](graphics.md#jednoradkove-horni-menu-2026-10-25), tedy přechod na ikony při nedostatku místa a minimální šířku okna pro ovládací prvky. Přístupný název/nápověda zůstává. Dřívější dvě řady kolonie se slučují, aby ovládání zůstalo uvnitř pevné lišty. Návrat, Escape a dostupnost akcí se nemění.
6. **Původní širší návrh:** název stránky vlevo, akce vpravo a varianty Úsporný (72/48), Vzdušný (88/64) a Seskupený (80/56) jsou pouze dřívější návrhy. Uživatel nyní vybral menší krok podle snímku; varianty ani nové chování dlouhých nápověd se neimplementují.
7. [R-0337](requirements/done/R-0337.md) je dokončen po společné místní přejímce s R-0341. [Původní evidence](functional-tests.md#r-0337-shared-game-bars-2026-10-29) zachovává vynechání testů na tehdejší žádost uživatele; [následná přejímka](functional-tests.md#r-0341-shared-game-headings-2026-10-29) ověřila společné rozměry, navigaci a úzká i široká okna. Nejde o hráčskou studii ani ověření vzdálené publikace.

## Nadpisy herních pohledů — návrh 2026-10-29

1. Uživatel schválil [R-0341](requirements/done/R-0341.md): společný nadpis vlevo používá Playpen Sans 28, běžný řez; akce jsou vpravo. Pohled bez názvu má levou část prázdnou. Horní lišta má výšku 72 a obě lišty minimální boční odsazení 16. Jejich společně vycentrovaný obsah se zastaví na 1 440 logických pixelech podle R-0342. Vzhled tlačítek zůstává stejný.
2. Mapa má přesný nadpis **Mappa Mundi**. Při najetí nebo fokusu se dole objeví lokalizované vysvětlení latinského názvu „mapa světa“ a dobového používání latiny. Opuštění nadpisu obnoví předchozí kontext, pokud mezitím nepřišla novější zpráva; zlato a rok zůstávají vpravo. Informace je dostupná také bez myši a neprovádí herní akci.
3. Název kolonie si zachovává přejmenování. Při úzkém okně přejdou akce na dosavadní ikonové zobrazení a nadpis s nimi nesmí kolidovat. Společné záhlaví a jeho CSS jsou vlastníkem umístění a typografie, obsah jednotlivých pohledů je nepřepisuje.
4. [Původní náhled](publishing.md#r-0341-game-heading-proposal-2026-10-29) zachycuje návrhovou fázi, nikoli uživatelskou studii. Průběh přejímky implementace zachycuje [evidence funkčních testů](functional-tests.md#r-0341-shared-game-headings-2026-10-29).

## Jednotná akční tlačítka (2026-10-27)

1. Akce na všech obrazovkách používají společný
   [papírový vzhled](graphics.md#jednotna-papirova-tlacitka-2026-10-27) a ikonu
   odpovídající významu. Popisek zůstává hlavním vysvětlením akce; ikonová
   tlačítka mají přístupný název nebo nápovědu. Fokus, stisk, nedostupnost
   a trvalý výběr musí být rozeznatelné i při ovládání klávesnicí.
2. Sjednocení nesmí měnit cíl akce, předpoklady, potvrzení, zrušení ani zpětnou
   vazbu. Zejména u odchodu ze hry zůstávají odlišné akce uložit a pokračovat,
   odejít bez uložení a zrušit odchod. Plné popisky a ikony se nesmějí překrývat.
3. [R-0316](requirements/done/R-0316.md) a jeho
   [evidence](functional-tests.md#r-0316-shared-paper-buttons-and-action-icons-2026-10-27)
   zachycují kontrolu implementace; nejde o výsledky uživatelské studie.

## Názvy akcí v horní liště — 2026-10-29

1. [R-0347](requirements/done/R-0347.md): pokud se názvy vejdou, akční tlačítka mají vedle ikony celý text. Evropský nákup používá **Buy units / Koupit jednotky**, odlišený od **Recruit / Nábor**. Hráč tak pozná cíl před otevřením nabídky.
2. Při nedostatku místa zůstává stávající přepnutí horní lišty na ikony; návrat do hry si ponechá text. Rozšíření okna názvy obnoví. Nákup, zrušení a jejich výsledky se nemění. Jde o dohodnuté chování, skutečnou přejímku uvádí [funkční evidence](functional-tests.md#r-0347-europe-action-labels-2026-10-29).
