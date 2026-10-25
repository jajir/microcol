# Horní lišta — výtvarné návrhy

**Dne 25. října 2026 uživatel vybral k implementaci 07B — Přirozený rukopis.** Tři podvarianty níže dokumentují porovnání jemné nepravidelnosti ikon. Produkční podklady a stav ověření popisuje [grafický manuál](graphics.md#navrhy-horni-listy-r-0273). Požadavek a jeho další rozhodnutí vlastní [R-0273](requirements/done/R-0273.md). Společná pravidla vzhledu zůstávají v [grafickém manuálu](graphics.md#soucasne-zadani-vzhledu).

Každý list porovnává letní a zimní lištu, trvalé názvy akcí, otevřený výběr kolonie a příklad vysvětlení ve spodní stavové liště. Nezávislost je uvedena zvlášť jako podmíněně dostupná akce. Nejde o snímky běžící hry.

## Společný princip

1. Tmavý inkoust zůstává, druhá barva se mění podle režimu. Pracovní návrh palety: inkoust `#302D27`, léto `#71864B`, zima `#568B9B`; krémový podklad `#FFF9EC` je pozadí. Hodnoty jsou návrhem, ne ověřenou konfigurací hry.
2. Ikona a krátký název jsou stále vidět. Podrobnější význam patří při najetí myší i klávesnicovém zaměření do spodní stavové lišty.
3. Dropdown má rámeček kolem celého ovladače a oddělenou šipku. Seznam má jasný výběr se značkou, čitelné názvy a možnost zavření bez změny.
4. Generované PNG slouží k volbě směru. Při převodu vybrané sady do SVG je nutné sjednotit geometrii mezi sezonami, přesné dvě barevné role a typografii; drobné rastrové odchylky a zvětšené příklady nejsou výrobní specifikace.

## Podvarianty 07 — Čistý skicář

Výběr rodiny je uzavřený: pokračujeme se sedmičkou. Uživatel požaduje drobnou přirozenou nepravidelnost samotné kresby, aby ikony nepůsobily geometricky pravidelně. Domluvené písmo **Playpen Sans 14 Regular** se nemění. Nadále platí stejná tloušťka perové linky, plné výplně a dvě barevné role; ruční dojem vytváří tvar obrysu, nikoli šrafování, textura nebo střídání tloušťky čar.

| Podvarianta / PNG | Rozdíl |
| --- | --- |
| [07A / Jemná ruční linka](graphics-assets/top-menu-2026-10-25/07a-jemna-rucni-linka.png) | Nejmenší zásah: lehce zvlněné obrysy, stále téměř pravidelné tvary. |
| [07B / Přirozený rukopis](graphics-assets/top-menu-2026-10-25/07b-prirozeny-rukopis.png) | Vyvážená varianta: drobná asymetrie i živější linie, bez ztráty čistoty. |
| [07C / Volnější skica](graphics-assets/top-menu-2026-10-25/07c-volnejsi-skica.png) | Nejvýraznější rukopis: volnější proporce a křivky; stále čisté plné výplně. |

**Vybráno: 07B.** Uživatel potvrdil přirozený rukopis pro herní ikony. 07A mění původní vzhled nejméně; 07C zůstává výraznější mezí pro porovnání. Větší detaily jsou výtvarné ukázky, ne důkaz stejné geometrie a přesného fontu ve výsledném SVG.

[Zadání podvariant pro imagegen](graphics-assets/top-menu-2026-10-25/prompts-07.txt). Původní návrh 07 i ostatní rodiny zůstávají níže pro srovnání.

### 07A / Jemná ruční linka

![07A / Jemná ruční linka — podvarianta ruční kresby](graphics-assets/top-menu-2026-10-25/07a-jemna-rucni-linka.png)

### 07B / Přirozený rukopis

![07B / Přirozený rukopis — podvarianta ruční kresby](graphics-assets/top-menu-2026-10-25/07b-prirozeny-rukopis.png)

### 07C / Volnější skica

![07C / Volnější skica — podvarianta ruční kresby](graphics-assets/top-menu-2026-10-25/07c-volnejsi-skica.png)

## Původní porovnání deseti rodin

Nejblíže současnému vzhledu je **01**, nejstřídmější je **07**. Pro nízkou lištu stojí za porovnání **10**, která však potřebuje více šířky pro názvy. Toto doporučení je výtvarný úsudek, nikoli výsledek uživatelského testování.

| Varianta / samostatné PNG | Charakter |
| --- | --- |
| [01 / Dvojitá perová linka](graphics-assets/top-menu-2026-10-25/01-dvojita-perova-linka.png) | Nejbližší současné hře; lehce nepravidelný dvojitý rámeček. |
| [02 / Mapové symboly](graphics-assets/top-menu-2026-10-25/02-mapove-symboly.png) | Hranaté značky a seříznuté rohy ve stylu mapové legendy. |
| [03 / Měkké oblázky](graphics-assets/top-menu-2026-10-25/03-mekke-oblazky.png) | Oblé, měkčí tvary a výraznější barevné plochy. |
| [04 / Kartografické štítky](graphics-assets/top-menu-2026-10-25/04-kartograficke-stitky.png) | Štítky s ustřiženým rohem a koloniálními štíty budov. |
| [05 / Kulaté medailony](graphics-assets/top-menu-2026-10-25/05-kulate-medailony.png) | Kruhové medailony s jasnými siluetami. |
| [06 / Koloniální praporky](graphics-assets/top-menu-2026-10-25/06-kolonialni-praporky.png) | Papírové praporky s mělkým výřezem dole. |
| [07 / Čistý skicář](graphics-assets/top-menu-2026-10-25/07-cisty-skicar.png) | Nejúspornější kresba, jednoduchý rámeček a hodně volného místa. |
| [08 / Ploché pečeti](graphics-assets/top-menu-2026-10-25/08-ploche-peceti.png) | Výraznější vyplněné siluety v osmihranném rámečku. |
| [09 / Navigační lístky](graphics-assets/top-menu-2026-10-25/09-navigacni-listky.png) | Lístky se zaobleným horním výstupkem. |
| [10 / Kompaktní rukopis](graphics-assets/top-menu-2026-10-25/10-kompaktni-rukopis.png) | Nízká lišta: ikona vlevo, text vedle ní. |

Všechny listy vznikly vestavěným nástrojem **imagegen**. [Úplná zadání a následné úpravy](graphics-assets/top-menu-2026-10-25/prompts.txt) jsou uloženy spolu s podklady.

## Náhledy

### 01 / Dvojitá perová linka

Nejbližší současné hře; lehce nepravidelný dvojitý rámeček.

![01 / Dvojitá perová linka — letní a zimní návrh](graphics-assets/top-menu-2026-10-25/01-dvojita-perova-linka.png)

### 02 / Mapové symboly

Hranaté značky a seříznuté rohy ve stylu mapové legendy.

![02 / Mapové symboly — letní a zimní návrh](graphics-assets/top-menu-2026-10-25/02-mapove-symboly.png)

### 03 / Měkké oblázky

Oblé, měkčí tvary a výraznější barevné plochy.

![03 / Měkké oblázky — letní a zimní návrh](graphics-assets/top-menu-2026-10-25/03-mekke-oblazky.png)

### 04 / Kartografické štítky

Štítky s ustřiženým rohem a koloniálními štíty budov.

![04 / Kartografické štítky — letní a zimní návrh](graphics-assets/top-menu-2026-10-25/04-kartograficke-stitky.png)

### 05 / Kulaté medailony

Kruhové medailony s jasnými siluetami.

![05 / Kulaté medailony — letní a zimní návrh](graphics-assets/top-menu-2026-10-25/05-kulate-medailony.png)

### 06 / Koloniální praporky

Papírové praporky s mělkým výřezem dole.

![06 / Koloniální praporky — letní a zimní návrh](graphics-assets/top-menu-2026-10-25/06-kolonialni-praporky.png)

### 07 / Čistý skicář

Nejúspornější kresba, jednoduchý rámeček a hodně volného místa.

![07 / Čistý skicář — letní a zimní návrh](graphics-assets/top-menu-2026-10-25/07-cisty-skicar.png)

### 08 / Ploché pečeti

Výraznější vyplněné siluety v osmihranném rámečku.

![08 / Ploché pečeti — letní a zimní návrh](graphics-assets/top-menu-2026-10-25/08-ploche-peceti.png)

### 09 / Navigační lístky

Lístky se zaobleným horním výstupkem.

![09 / Navigační lístky — letní a zimní návrh](graphics-assets/top-menu-2026-10-25/09-navigacni-listky.png)

### 10 / Kompaktní rukopis

Nízká lišta: ikona vlevo, text vedle ní.

![10 / Kompaktní rukopis — letní a zimní návrh](graphics-assets/top-menu-2026-10-25/10-kompaktni-rukopis.png)


## 07B ve skutečné hře

Výtvarná podoba převedená do SVG, skutečných lokalizovaných textů a nativních ovládacích prvků. Následující snímky vznikly v Docker/Xvfb; stav lokální přejímky uvádí [funkční evidence](functional-tests.md#r-0273-toolbar-07b-2026-10-25).

![07B ve hře — zelená sada](graphics-assets/top-menu-2026-10-25/toolbar-07b-summer.png)

![07B ve hře — zimní sada](graphics-assets/top-menu-2026-10-25/toolbar-07b-winter.png)

![07B ve hře — české menší okno](graphics-assets/top-menu-2026-10-25/toolbar-07b-czech-narrow.png)

Editovatelné [ikony SVG](../microcol-game/src/graphics/game-toolbar.svg), [atlas PNG](../microcol-game/src/main/resources/images/game-toolbar.png), [rámeček SVG](../microcol-game/src/graphics/game-toolbar-frame.svg) a [rámeček PNG](../microcol-game/src/main/resources/images/game-toolbar-frame.png) obsahují produkční kresbu bez textů.


![07B — viditelný fokus klávesnice na Vycentrovat](graphics-assets/top-menu-2026-10-25/toolbar-07b-keyboard-focus.png)
