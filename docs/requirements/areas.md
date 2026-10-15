# Oblasti požadavků a pořadí práce

**Pracovní návrh z 13. října 2026.** Nad katalogem pravidel, use cases a malými
analýzami zavádíme deset oblastí. Nejdřív vyjasníme podobu vybrané oblasti,
potom postupně upřesníme a dodáme její malé požadavky. Číslo R ani A neurčuje
prioritu. Aktuální zásobník tvoří [nová řada R](backlog.md); původních čtyřicet
požadavků má [mapu migrace](migration-2026-10-14.md).

Podkladem je [katalog 230 pravidel](../gap-analysis.md#rule-catalogue),
[119 use cases](../use-cases.md) a [32 pracovních balíků](../implementation-plan.md#seznam-pracovnich-baliku)
na revizi [`36bf0ffc`](https://github.com/jajir/microcol/commit/36bf0ffc524c8bab6ff3726f1aa8b555c5b869d9).
Tato stránka vlastní rozdělení a hranice oblastí. Přesné hodnoty, otázky Q,
akceptaci UC ani důkazy implementace sem nekopírujeme. Jde o uspořádání
existujících podkladů, nikoli nový audit manuálu nebo běžící hry.

## Co oblast znamená

1. Každé R má **jednu hlavní oblast** podle výsledku, který mění, a podle potřeby
   související oblasti. Závislost pojmenuje konkrétní potřebné rozhodnutí nebo
   schopnost; obecné „čeká na celou ekonomiku“ nestačí.
2. Oblast je téma práce, **nikoli Java vrstva**. Pravidla všech herních oblastí
   patří do modelu, rozhodování soupeře do AI a ovládání do GUI podle
   [mapy odpovědností](../architecture.md). Hráč, jednotka, kolonie, vlastnictví,
   kapacita a dovolené převody zůstávají modelovým stavem a operacemi i tehdy,
   když je řešíme v různých oblastech.
3. **Hra / Nastavení / Regrese** jsou nadále samostatné kategorie přejímky UC.
   Oblast A02 například obsahuje výběr obtížnosti, ale její skutečné účinky
   musí být převzaté v příslušných herních oblastech. Zařazení do oblasti
   nemění stav pravidla ani nezmenšuje rozsah manuálu.
4. Menu, společné ovládání, grafický jazyk a zvuk mají také projektové požadavky
   mimo původní pravidlový audit. Nevymýšlíme jim dodatečná manuálová ID.
   P01 (rozhodnutí), P31 (ověření) a P32 (vydání) doprovázejí každou oblast.

## Deset oblastí

### A01

**Menu a navigace aplikace.** Hlavní menu, menu v rozehrané hře, dostupné
cíle, přechody, návrat, zrušení a vstupy do nové nebo uložené partie.
Příprava vychází z existujících obrazovek a ověřených přechodů; chybějící
chybové stavy mají vlastní malé požadavky.
A02 vlastní nastavení a vytvořenou partii, A04 obnovu pozice a A10 význam
odchodu se skóre. Společné ovládací prvky a texty přebíráme z A03.
Dodané menu: [R-0001](done/R-0001.md)/[R-0012](done/R-0012.md); další obnova po
neúspěšném načtení patří do A04, [R-0067](R-0067.md).

### A02

**Nastavení a zahájení volné partie.** Volba mapy, jména, národa, obtížnosti,
výchozí výprava a referenční profil běžné hry. Patří sem i návrh složení partie:
kteří protivníci se účastní, co lze měnit a co je přednastavené. Samostatná
volba počtu, jmen nebo „inteligence“ soupeřů zatím není přijatým požadavkem;
je potřeba odlišit možnosti MicroCol od pravidel manuálu. A08 vlastní stav
národů a rozhodování AI, A04 kalendář. Výstupem přípravy je seznam voleb,
výchozích hodnot, validací a výsledek potvrzení či zrušení nastavení.

### A03

**Obrazovky volné hry a společný jazyk UI.** Má dvě propojené části: co hráč
vidí a odkud ovládá mapu, kolonii, Evropu, reporty a nápovědu; a společné
pojmy, ovládání, fokus, klávesnici, chyby, potvrzení, ikony a zpětnou vazbu.
Výstupem je přehled obrazovek s dostupnými informacemi a jejich návazností
na sdílená [UX pravidla](../ux.md), [grafiku](../graphics.md) a [zvuk](../audio.md).
Herní hodnoty a příkazy dodává příslušná oblast modelu; UI je nepřepočítává
a nezpřístupňuje skryté údaje. Celá běžná partie UC-115–118 propojuje všechny
oblasti, není pouze úkolem vykreslení obrazovek. Detailní výtvarné ladění může
počkat, základní struktura a srozumitelné ovládání patří už k přípravě funkce.

### A04

**Tahy, kalendář a pokračování uložené hry.** Kdo hraje, pořadí fází, okamžik
produkce, posun času, ukládání, načítání a autosave. Výstupem přípravy je
přehled stavů a přechodů včetně míst, odkud lze bezpečně pokračovat bez
opakování událostí. Doménové stavy ukládáme spolu s jejich funkcí; kompatibilitu
starších save souborů během vývoje nevyžadujeme podle [platného kontraktu](../architecture.md#identita-kolonii-a-textove-vstupy).
Kandidáti: [R-0051](R-0051.md) a [R-0052](R-0052.md).

### A05

**Svět, jednotky a rozkazy.** Terén, průzkum, viditelnost, identita a vlastnosti
jednotek, výstroj, pohyb, dlouhodobé rozkazy a práce pionýrů. Připravíme
schopnosti objektů, jejich platné stavy a podmínky akcí včetně ceny a odmítnutí.
Kapacita lodě je vlastnost jednotky; konkrétní převod nákladu řeší A06.
Výnos pole používá A07, obranu A09. Kandidáti:
[R-0053](R-0053.md) a [R-0054](R-0054.md).

### A06

**Doprava, Evropa a zahraniční obchod.** Náklad, cestující, přestupy, Atlantik,
obchodní trasy, evropský trh, imigrace, nábor a nákupy jednotek. Potřebujeme
popsat umístění lidí a zboží, kapacity, podmínky a účtování převodů, stavy
plavby a obchodní cyklus. Limity kolonie dodává A07, schopnosti lodí A05,
daně a bojkot A10. Anglický imigrační a nizozemský tržní efekt mají hlavní
oblast zde; výběr národa je nepřesouvá do UI nastavení.

### A07

**Kolonie a jejich hospodářství.** Založení a jméno, obyvatelé, práce, sdílená
pole, jídlo, růst, hlad, sklad, výroba, stavby a vzdělávání. Výstupem je
přehled stavů kolonie, pořadí výpočtů a nezávislé příklady vstupů a výstupů.
Zobrazení použije A03, aktualizace A04, venkovní data A05 a převody A06.
Aktuální kandidáti jsou v [A07 nového katalogu](backlog.md#a07), zejména
sdílená pole R-0055, spotřeba rudy R-0061 a základní kříž R-0062.
Dokončené Z01/Z02 zůstávají přijatými výchozími kontrakty, ne novými úkoly.

### A08

**Ostatní národy, diplomacie a AI.** Evropské i domorodé společnosti, jejich
identita a vztahy, osady, kontakt, nabídky a rozhodování počítače. Oddělíme
stav a dovolené akce v modelu od strategie, která je vybírá; AI používá stejné
operace jako člověk. Příprava určí role účastníků, stavy vztahů a malé
ověřitelné cíle soupeře (například obstarat zásoby a ukončit tah).
Volby složení partie jsou A02, hospodářské možnosti A06/A07 a boj A09.

### A09

**Boj a jeho následky.** Povolení útoku, síly a modifikátory, řízená náhoda,
pozemní i námořní výsledek, zajetí, poškození, oprava, kořist a obsazení.
Nejdřív popíšeme podmínky útoku, výpočet a jednotlivé následky včetně
odmítnutí bez změny stavu. Jednotky a terén dodává A05, kolonie A07 a vztahy
A08. Španělská výhoda patří sem; zvláštní podmínky revoluce navazují z A10.

### A10

**Politika, nezávislost a výsledek hry.** Daně, bojkot, zvony, podpora,
Kongres, všech 25 zakladatelů, revoluce, královská armáda, intervence,
výhra/prohra, skóre a síň slávy. Připravíme stavy politiky a konce hry,
podmínky přechodů a místo zapojení každého efektu. Nábor a životní cyklus
zakladatele patří sem; jeho efekt používá pravidla ovlivněné oblasti.
Ukončení aplikace, odchod se skóre a pokračování bez skórování se nesmějí
slít do jedné operace jen proto, že jsou dostupné z menu A01.

## Úplné přiřazení pravidel manuálu

Každé existující katalogové ID má v tabulce právě jednu hlavní oblast.
Rozsahy jsou včetně krajních hodnot. Jde o počet položek katalogu, nikoli
odhad práce nebo počet implementačních úkolů. A01 má projektový rozsah,
přestože nemá výhradně přiřazené pravidlo původního auditu.

| Oblast | Hlavní pravidla | Počet | Související balíky |
| --- | --- | ---: | --- |
| [A01](#a01) | Žádné výhradní ID; vstupy do funkcí ostatních oblastí. | 0 | P28; vstupy P02/P26/P25 |
| [A02](#a02) | SETUP-001–003, SETUP-008 | 4 | P02 |
| [A03](#a03) | SUPPORT-002–004 | 3 | P27–P30 |
| [A04](#a04) | TURN-001–002, SETUP-009, SUPPORT-001 | 4 | P03/P26 |
| [A05](#a05) | MAP-001–009, UNIT-001–004, MOVE-001–009, PION-001–006 | 28 | P04–P07 |
| [A06](#a06) | CARGO-001–016, ROUTE-001–004, IMM-001–004, IMM-007, TRADE-001–009, SETUP-004, SETUP-007 | 36 | P08/P09/P14–P16 |
| [A07](#a07) | COL-001–012, PROD-001–011, BUILD-001–019, EDU-001–006 | 48 | P10–P13 |
| [A08](#a08) | NAT-001–021, DIP-001–008, SETUP-005 | 30 | P18–P21 |
| [A09](#a09) | COMBAT-001–016, SETUP-006 | 17 | P17 |
| [A10](#a10) | GOV-001–022, FATHER-001–025, END-001–004, SCORE-001–009 | 60 | P22–P25 |
| **Celkem** | **Všechna současná katalogová ID, bez duplicit** | **230** | P01/P31/P32 průběžně |

Referenční [terény](../gap-analysis.md#terrain-reference-data-audit) patří
k A05, [budovy](../gap-analysis.md#building-data-matrix) a
[dovednosti](../gap-analysis.md#skills-matrix) k A07 ve vazbě na typy jednotek
A05, [bojové síly](../gap-analysis.md#unit-strength-reference-matrix) k A09.
Zařazení pravidla nezastupuje ověření všech buněk tabulek ani variant UC.
IMM-005/006 neexistují; mezery v ID nedoplňujeme.

### Přesahy, které nesmíme ztratit

1. SETUP-002/003: A02 připraví zvolený a uložený profil. Účinky obtížnosti
   a národa se přijímají také v dotčených herních oblastech; nastavení samo
   nesplňuje pravidla soupeřů, výroby nebo boje.
2. COL-010: skladový limit A07 a vyložení A06 mají společnou bezpečnou
   transakci. PROD-008/009: výroba křížů A07 ještě neznamená imigraci A06.
3. EDU-006: domácí vzdělávání A07 se propojuje s domorodou výukou A08.
   BUILD-010: možnost pracovat na vodě spojuje stavbu A07 s terénem A05.
4. DIP-007: pravidlo zpřístupnění informací vlastní A08, zobrazení A03.
   GOV a FATHER propojují A10 s trhem, výrobou, pohybem i bojem; nesmějí
   vytvořit druhé výpočty těchto pravidel.
5. UC-115–118 jsou souvislé cesty přes oblasti. UC-119, save/load, společná
   přístupnost a ověření dodávky se prověřují průběžně, ne až na konci plánu.

## Postup po oblastech

1. Vybereme jednu aktivní oblast a její nejbližší ucelený výsledek. Níže
   navržené pořadí je vodítko; změnu směru pojmenujeme a zdůvodníme závislostí.
2. Nejdřív připravíme její společný základ: účel, hlavní objekty nebo obrazovky,
   stavy, přechody, pravidla, hranice vůči okolí a nevyřešená rozhodnutí.
   Zaznamenáme existující chování odděleně od návrhu a přijatého řešení.
3. Základ upřesníme v jednom vybraném R se čtyřmi kapitolami, nebo použijeme
   již existující autoritativní kontrakt. Tato stránka je rozcestník; finální
   strukturu menu či produkční pravidla nezapisujeme současně na více míst.
4. Z něj vybereme malé R s jedním výsledkem a pojmenovanými závislostmi.
   Před implementací musejí být vyřešená společná rozhodnutí ovlivňující tento
   řez. Není třeba dopředu navrhnout každý detail celé hry. Přijatá rozhodnutí
   znovu neschvalujeme; jejich odkazy přeneseme do dalších R.
5. Upřesňujeme a dodáváme jednotlivá R podle [společného postupu](README.md#work-through-requirements-one-at-a-time).
   Potřebnou práci v jiné oblasti vymezíme jako závislost a potom se vrátíme
   k aktivní oblasti. Nový návrh mimo ni automaticky nemění prioritu.
6. Po uceleném výsledku prověříme jeho zapojení do běžné volné hry a zvolíme
   další výsledek. Oblast není hotová jen proto, že prošel jeden R nebo build;
   rozhoduje pokrytí jejího rozsahu a příslušných UC.

### Navržené pořadí

| Krok | Soustředění | Co potřebujeme před přechodem dál |
| --- | --- | --- |
| 1 | A01 menu; nezbytné hranice A02/A03 | Stávající menu potvrzeno; R-0001 odstraní podmínku dokončené kampaně. |
| 2 | A04 tahy a A05 svět/jednotky | Konkrétní kontrakty času, stavu, pohybu a obnovy; malé řezy R-0002–0004 podle závislostí. |
| 3 | A07 kolonie | Postupné R-0005–0010 po vyjasnění společného základu; souvislá první kolonie a hospodářský tah. |
| 4 | A06 doprava a Evropa | Oběh lidí, zásob a peněz. Je-li získání odborníka podmínkou R-0009, tento úzký předpoklad vyřešíme už před ním. |
| 5 | A08 soupeři a diplomacie, poté A09 boj | Nejdřív identity, vztahy a základní hospodářské chování soupeřů; bojová strategie naváže na přijaté bojové operace. |
| 6 | A10 politika a dokončení partie | Propojená ekonomika, vztahy a boj; postupně politika, revoluce a jednotlivé výsledky hry. |

A02 se rozšiřuje o další volby, když existují jejich herní účinky. A03 provází
každou oblast a vytváří společný základ už na začátku. Přesný rozsah první
dodávky menu je upřesněn v R-0012; mock nastavení vlastní R-0014. Toto pořadí nahrazuje
automatický postup R-0001, R-0002, …; [etapy E0–E7](../implementation-plan.md#etapy-podle-toho-co-hrac-dokaze)
nadále popisují ověřitelné výsledky, nikoli konkurenční frontu úkolů.

## Navazující návrhy pro free play

Dne 13. října 2026 vzniklo [dalších dvacet analýz R-0012–R-0031](README.md#twenty-free-play-follow-up-drafts).
Všechny mají stav Draft. Rozvíjejí první hospodářský průchod a vybrané základy
pozdějších oblastí; nejsou úplným seznamem práce do dokončení hry.

| Oblast | Nově připravené malé analýzy |
| --- | --- |
| A01 | [R-0012 struktura menu](done/R-0012.md), [R-0013 bezpečný odchod a nahrazení partie](done/R-0013.md) |
| A02 | [R-0014 referenční profil](done/R-0014.md), [R-0015 účastníci partie](done/R-0015.md) |
| A03 | [R-0016 struktura obrazovek](done/R-0016.md), [R-0017 dostupnost příkazů a chyby](done/R-0017.md) |
| A04 | [R-0018 bezpečné obnovení hry](done/R-0018.md) |
| A05 | [R-0019 průzkum a viditelnost](done/R-0019.md), [R-0020 vybavení na evropském molu](done/R-0020.md) |
| A06 | [R-0021 odjezd a návrat přes Atlantik](done/R-0021.md), [R-0022 přeprava kolonisty](done/R-0022.md), [R-0023 nákup odborného kováře](done/R-0023.md) |
| A07 | [R-0024 vyložení vozu](done/R-0024.md), [R-0025 podmínky staveb](done/R-0025.md) |
| A08 | [R-0026 evropské vztahy](done/R-0026.md), [R-0027 první kolonie soupeře](done/R-0027.md) |
| A09 | [R-0028 přípustnost útoku](done/R-0028.md), [R-0029 výpočet pozemní síly](canceled/R-0029.md) |
| A10 | [R-0030 daňový požadavek](canceled/R-0030.md), [R-0031 podpora nezávislosti](canceled/R-0031.md) |

A01 má od 13. října 2026 dohodnutý základ: ponechat stávající menu, odstranit
podmínku kampaně v R-0001 a ověřit skutečné ukládání v R-0018. R-0023 zůstává
předpokladem dosažitelnosti odborníka pro R-0009; R-0031 předchází politickému
účinku protestu v R-0030. Číslo R neurčuje prioritu.

## Nejbližší příprava: struktura A01

**Rozhodnutí uživatele z 13. října 2026.** [R-0012](done/R-0012.md) vlastní
zachování současného menu; jeho nový návrh již není blokátorem R-0001.

1. R-0001 zpřístupní existující „New free play game“ bez dokončení kampaně.
2. R-0014 naváže mock nastavením ve stylu menu: Start/Zpět a dvě funkční mapy;
   národ bude prozatím pouze mock volba.
3. R-0015 přidá Francii na pevná místa s městy a jednotkami.
4. R-0016 ověří existující obrazovky myší a dostupnými klávesami. Chybějící
   klávesová cesta není důvod k návrhu celého nového UI.
5. R-0018 ověří automatické uložení a Pokračovat; nezamění uživatelský
   předpoklad o uložení při odchodu do menu za doložené chování.
6. Aktuální zadání je upřesnění a ověření cest. Nové herní funkce zůstávají
   jednotlivými následnými implementacemi.

Zařazení oblastí i pořadí jsou návrhy pro další společnou práci. Nezakládají
implementační úkoly, nespouštějí automaticky deset oblastí a nemění dosavadní
herní chování. Číselné hodnoty a vzorce přebíráme z [přijatých pravidel](../gap-analysis.md#accepted-game-rules).
