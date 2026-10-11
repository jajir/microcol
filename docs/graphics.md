# Grafické podklady a jejich použití v MicroCol

MicroCol používá SVG jako editovatelné výtvarné podklady. Za běhu načítá rastrové soubory z `microcol-game/src/main/resources/images`, z některých vyřezává malé obrázky a další skládá do pozadí obrazovek. Změna SVG se ve hře projeví až po exportu a zařazení odpovídajícího rastrového souboru mezi resources.

Tento popis zachycuje všech **24 SVG** v `microcol-game/src/graphics`, všech **47 rastrových souborů** v `resources/images` a kód, který je používá. Vysvětluje současné technické vazby a platné obecné zadání vzhledu. Detailní pravidla stylu a historického zasazení zatím nejsou předmětem práce.

Stav k **11. říjnu 2026**, větev `develop`, revize `1854fa5446f54cbf99d4f6892d7f32aa4e3e5a00`. Rozměry níže jsou skutečné rozměry uložených obrázků v pixelech, pokud není výslovně uvedena velikost UI komponenty nebo souřadnice SVG.

## Současné zadání vzhledu

Platná dohoda z **11. října 2026**: konkrétní výtvarné detaily nyní není potřeba předem specifikovat. Pro novou i upravovanou grafiku stačí tato pravidla:

- Přibližně navazovat na současný vzhled hry, aby nové prvky působily jako její přirozená součást.
- Držet vysoký kontrast a dobrou čitelnost objektů vůči pozadí i v malých zobrazovaných velikostech.
- Usilovat o co nejhezčí a líbivý výsledek; konkrétní výtvarné řešení volí asistent podle vlastního estetického úsudku v rámci těchto pravidel.

Přesná paleta, způsob obrysů, stínování, proporce a další podrobnosti teď nepotřebují samostatné schvalování. Chybějící detailní stylový manuál není důvod zastavovat funkční změnu ani si vyžadovat další zadání vzhledu. Technické požadavky na rozměry, výřezy, průhlednost a zapojení obrázků popsané níže platí dál.

## Jak obrázek projde od kresby do hry

**SVG → export v Inkscape nebo `images.groovy` → rastrový soubor v `resources/images` → načtení ve hře → zobrazení.**

Po načtení přes `ImageCache` a `ImageProvider` se použije buď celý obrázek, nebo výřez atlasu podle souřadnic. Výřez může dostat alias a projít otočením či složením; výsledný obrázek se vykreslí na canvas nebo vloží do `ImageView`. Některé rastry načítá přímo JavaFX CSS nebo spouštěcí obrazovka JVM.

`colors.properties` ovlivňuje jak přebarvení při exportu SVG, tak barevné výplně pozadí kreslené kódem za běhu.

**Atlas** je jeden větší obrázek obsahující více malých obrázků v pravidelné mřížce. Hra má tři: `background.png`, `buildings.png` a `buttons.png`. Kód nepoznává obsah kresby. Prostě vyřízne předem určený obdélník. Přesunout postavu do jiného políčka atlasu tedy znamená změnit i její mapování v kódu.

**Alias** je jméno obrázku v paměti. Například `good-corn.png` není samostatný soubor v repozitáři, ale pojmenovaný výřez z `background.png`. Ani přípona `.png` v názvu konstanty nezaručuje, že takový soubor existuje.

**Vrstva a ID v SVG** pomáhají při editaci a exportu. Hra z nich jednotlivé objekty nevybírá. Výjimkou v přípravě podkladů je přebarvování vybraných definic podle jejich ID; to probíhá v exportním skriptu, nikoli při vykreslování SVG ve hře.

### Načítání a paměťová cache

[ImageCache](../microcol-game/src/main/java/org/microcol/gui/image/ImageCache.java) při `getImage(name)` nejprve hledá již připravený obrázek v paměti. Pokud ho nemá, otevře resource `images/<name>` a vytvoří JavaFX `Image`. Opakované volání vrací stejnou instanci. Registrace aliasu také sdílí tutéž instanci; registrace stejného jména podruhé je chyba. Chybějící resource ohlásí [StreamReader](../microcol-game/src/main/java/org/microcol/gui/util/StreamReader.java).

[ImageLoaderImpl](../microcol-game/src/main/java/org/microcol/gui/image/ImageLoaderImpl.java) připravuje grafiku v tomto pořadí: hlavní atlas → zelené pobřeží → ledové pobřeží → okraje neodhalené mapy → zboží → budovy → tlačítka → několik samostatných pozadí → jednotky. Pořadí je významné: například jednotky a zboží přejmenovávají výřezy vytvořené prvním loaderem.

[ImageProvider](../microcol-game/src/main/java/org/microcol/gui/image/ImageProvider.java) nabízí `getImage(name)`, `getTerrainImage(terrain)`, `getUnitImage(unit/type)` a `getGoodsTypeImage(goods/type)`. `getConstructionImage(type)` vrací `Optional<Image>`, protože pro některé typy staveb obrázek registrovaný není. Statická metoda `getRawImage` načítá resource přímo, bez této instance cache.

Samostatné obrázky mohou být načteny až při prvním použití. To, že nejsou uvedeny ve startup loaderu, proto neznamená, že je hra nepoužívá. Další cestou je JavaFX CSS; ta nepotřebuje aliasy v `ImageCache`. Uživatelské rozhraní sestavují Java třídy, v prohlédnutém repozitáři nejsou FXML soubory.

Cache nemá automatické sledování změn souborů. Přepsání PNG na disku nezmění již načtenou instanci `Image`; pro běžné ověření nového exportu je potřeba aktualizovat resources používaného buildu a hru znovu spustit.

## Katalog všech SVG zdrojů

Odkazy v této části otevírají editovatelné zdroje. **Skriptovaná vazba** znamená, že převod výslovně obsahuje `images.groovy`. **Exportní metadata** jsou uložené atributy `inkscape:export-filename` na dokumentu nebo jeho objektech. Dokládají zamýšlený výstup, ale samy nezaručují, že aktuální PNG lze přesně znovu vyrobit jedním exportem současného stavu SVG.

### Atlasy

| SVG | Obsah a vrstvy | Výstup a způsob použití |
| --- | --- | --- |
| [background.svg](../microcol-game/src/graphics/background.svg) | Terén, pobřeží, stromy, pole, město, jednotky a zboží. Viditelná vrstva `main`, skrytá pomocná `description`. | Skriptovaná vazba na `background.png` **648 × 432**. Loader vytvoří 96 výřezů **45 × 45** a další odvozené varianty. Podrobná mapa je níže. Metadata obsahují i staré překlepy `backgroud.png` a `backgroud2.png`; aktuální runtime jméno je `background.png`. |
| [buildings.svg](../microcol-game/src/graphics/buildings.svg) | Kresby staveb v mřížce, jedna vrstva `Layer 1`. Barevné krajní čtverce jsou součástí podkladu, nikoli registrované typy staveb. | Metadata míří na `buildings.png` **560 × 490**. `images.groovy` pro něj nemá funkci. Runtime řeže 56 buněk **70 × 70**, z nichž 30 má alias podle typu stavby. DPI na kořeni SVG a na jednotlivých objektech se liší; rozhodující je cílový rozměr a mřížka PNG. |
| [buttons.svg](../microcol-game/src/graphics/buttons.svg) | Ikony ve vrstvách `background` a `main`. | Metadata míří na `buttons.png` **200 × 200**, skriptovaný export chybí. Runtime řeže 16 buněk **50 × 50**. Obrázky jsou symboly tlačítek; rozložení, události a okolní vzhled tlačítek řeší UI a CSS. |

### Velká pozadí

| SVG | Obsah a vrstvy | Výstup a způsob použití |
| --- | --- | --- |
| [sunset2.svg](../microcol-game/src/graphics/sunset2.svg) | Scéna moře, slunce a pobřeží; vrstvy `fill` a `contours`. | Zdroj funkce `sunset()`: `sunset.png` **781 × 651**, z něj `sunset-top-line.png` **65 × 90** a `sunset-bottom-line.png` **48 × 75**. Menu skládá středovou scénu a opakované kraje. |
| [sunset.svg](../microcol-game/src/graphics/sunset.svg) | Jednodušší varianta scény, jedna vrstva. Staré exportní metadata ukazují na Windows cestu k `sunset.png`. | Současná funkce `sunset()` tento soubor **nebere**; používá `sunset2.svg`. Jde o alternativní podklad bez aktuální skriptované vazby. Stejné jméno PNG v metadatech neznamená, že se používají obě SVG. |
| [europe1.svg](../microcol-game/src/graphics/europe1.svg) | Evropský přístav; `background`, skrytá `house1`, viditelná `house2`. | Zdroj funkce `europe()`: `europe.png` **780 × 624** a kraje `europe-left.png` **83 × 624**, `europe-right.png` **22 × 624**. Lodě, lidé a zboží jsou nad scénou samostatné prvky UI. |
| [europe.svg](../microcol-game/src/graphics/europe.svg) | Jiná kompozice přístavu; vrstvy `background` a `port`. | Také má metadata pro `europe.png`, ale současný skript vybírá `europe1.svg`. Tento zdroj proto nelze automaticky považovat za aktuální předlohu runtime pozadí. |
| [market.svg](../microcol-game/src/graphics/market.svg) | Tržištní tabule v krajině; vrstvy `background`, `ptak`, `lines`. | Funkce `market()` vytvoří `market.png` **700 × 598**, `market-top.png` **10 × 598** a `market-bottom.png` **65 × 598**. Poslední dva názvy jsou zavádějící: ve hře jde o levý a pravý kraj. |
| [colony-background.svg](../microcol-game/src/graphics/colony-background.svg) | Krajina s molem za obrazovkou kolonie, jedna vrstva. | Funkce `colony()` vytvoří `colony.png` **780 × 758** a tři kraje: `colony-top1.png` **65 × 203**, `colony-top2.png` **85 × 190**, `colony-bottom.png` **71 × 62**. Výrobní budovy, rámce, pracovníci a pole se doplňují zvlášť. Některá stará metadata na objektech zmiňují i `sunset.png`; zdrojovou vazbu zde určuje skript. |
| [wood.svg](../microcol-game/src/graphics/wood.svg) | Dřevěná textura; skryté pracovní vrstvy `work`, `parts`, viditelná `main`. | Metadata objektů míří na `background-wood2.png` **600 × 922**. Statistiky a přehled tahu jej opakují v obou osách. Kořenové metadata `/Users/jan/bitmap.png` jsou stará pracovní cesta, nikoli runtime resource. |

### Papír, rámečky a dialogy

| SVG | Obsah a vrstvy | Výstup a způsob použití |
| --- | --- | --- |
| [books.svg](../microcol-game/src/graphics/books.svg) | Otevřená kniha ve vrstvě `main`. | Metadata pro `book.png` **895 × 585**. Soubor je v resources, současný odkaz z Java/CSS/build kódu nebyl nalezen. Statistiky používají `book2.png`. |
| [books2.svg](../microcol-game/src/graphics/books2.svg) | Více samostatných návrhů: skryté `element`, `papers`, `wood`; viditelná `page`. | Metadata spojují `papers` s `book2.png` **900 × 888**, `page` s `page.png` **600 × 727** a `wood` s `background-wood.png` **200 × 506**. Exportuje se správná vrstva či výběr, nikoli všechny návrhy dohromady. Aktivní jsou kniha statistik a list přehledu tahu; pro tuto první dřevěnou variantu nebyl nalezen runtime odkaz. |
| [colony-frame.svg](../microcol-game/src/graphics/colony-frame.svg) | Více rámů. Skryté vrstvy `pok`, `colony-building-queue`, `colony-fields`, `colony-frame`, `colony-warehouse`; viditelná `colony-building-queue copy`. Název viditelné kopie sám neurčuje exportní soubor. | Metadata pro `colony-fields.png` **145 × 145**, `colony-building-queue.png` **264 × 135** a `colony-warehouse.png` **846 × 134**. DPI uložené v těchto PNG odpovídá po zaokrouhlení metadatům tohoto SVG. Pro `colony-frame.png` **100 × 134** obsahuje odpovídající rámovou vrstvu, ale výslovný exportní atribut pro tento název chybí. Rámy jsou CSS pozadí samostatných panelů. |
| [colony-frames.svg](../microcol-game/src/graphics/colony-frames.svg) | Další sada rámů; všechny tři vrstvy `structures`, `fields`, `building-queue` jsou v uloženém stavu skryté. | Metadata zmiňují stejné `colony-fields.png` a `colony-building-queue.png`, avšak při jiných DPI než současné PNG; dále `colony-structures.png` a na kořeni `colony-dock.png`, které v `resources/images` nejsou. Je to konkurenční/alternativní podklad; současné malé rámy nelze bez ověření regenerovat z tohoto souboru. |
| [dialog.svg](../microcol-game/src/graphics/dialog.svg) | Rámeček společně s postavou muže, jedna vrstva. | Metadata pro `dialog-petr.png` **501 × 378**. Hra používá celou kompozici jako CSS pozadí dialogu. Text, tlačítka a obsah dialogu nejsou součástí obrázku. |

### Drobné obrázky a pracovní předlohy

| SVG | Obsah a vrstvy | Výstup a způsob použití |
| --- | --- | --- |
| [crates.svg](../microcol-game/src/graphics/crates.svg) | Zavřená a otevřená bedna nad sebou, vrstva `main`. | Funkce `crates()` vytvoří `crates.png` **200 × 400** a dva řezy **200 × 200**: `crate-closed.png`, `crate-open.png`. Runtime načítá jednotlivé řezy a zmenšuje je do nákladového slotu **45 × 45**. |
| [cursors.svg](../microcol-game/src/graphics/cursors.svg) | Šipka, značka chůze a kotva nad sebou, vrstva `main`. | Funkce `cursors()` vytvoří `cursors.png` **30 × 90**, kurzor **30 × 30** a čtyři značky **25 × 25**. Bojové značky tímto skriptem nevznikají. |
| [bullet.svg](../microcol-game/src/graphics/bullet.svg) | Malý nepravidelný kroužek, vrstva `main`. | Funkce `bullet()` vytvoří `bullet.png` **15 × 15**. Slouží jako odrážka přehledu tahu, nikoli jako střela v bojové animaci. Je to jediná nyní aktivně zavolaná exportní funkce skriptu. |
| [clouds.svg](../microcol-game/src/graphics/clouds.svg) | Viditelné `example-background` a `cloud-1`, skrytá `cloud-2`. Ukázkové pozadí je vložený raster přímo uvnitř SVG. | Skupina `g929` má metadata pro `cloud-1.png` **799 × 479**, `g1215` pro `cloud-2.png` **1000 × 795**. Exportovat se má mrak bez referenčního pozadí. Mapa posouvá celý `cloud-1.png`; pro `cloud-2.png` nebyl nalezen runtime odkaz. |
| [kings-seal.svg](../microcol-game/src/graphics/kings-seal.svg) | Skrytá vrstva `main` a viditelná `shadow`. | Dva samostatné exporty `kings-seal.png` a `kings-seal-shadow.png`, oba **40 × 41**. V přehledu cílů se vybírá plná pečeť pro splněný cíl a světlá varianta pro nesplněný. Hra je nevrství přes sebe jako objekt a jeho stín. |
| [person.svg](../microcol-game/src/graphics/person.svg) | Tři kopie drobné postavy a větší otevřený rám, jedna vrstva s opacitou `0.94`. | Nemá skriptovaný výstup ani exportní metadata. Patnáct ze sedmnácti atributů cest `d` je doslova shodných s cestami v `background.svg`; dvě zbývající rámové cesty odpovídají `colony-frame.svg`. Dokládá sdílené kresby, ne automatické propojení. Herní kolonista se načítá z atlasu, nikoli z tohoto souboru. |
| [wagon.svg](../microcol-game/src/graphics/wagon.svg) | Viditelná vektorová vrstva `draw`, skrytá vrstva `image` s vloženou rastrovou předlohou. | Všech 12 cest `d` je doslova obsaženo také v `background.svg`. Samostatný export ve skriptu není. Loader registruje `wagon` z buňky `(1,6)` atlasu, ale aktuální Java kód nemá nalezeného spotřebitele tohoto aliasu. Změna zdejšího vozu sama atlas neaktualizuje. |
| [logo.svg](../microcol-game/src/graphics/logo.svg) | Kostel, pozadí a nápis MicroCol. Skrytá `background`, viditelné `background-2` a `logo`. | Metadata pro `splash-screen.png` **200 × 200**. Build jej vkládá do manifestu jako spouštěcí obrázek JVM a používá jej také argument `-splash`. Text zůstává textem, takže při novém exportu záleží i na dostupném písmu. |
| [icon.svg](../microcol-game/src/graphics/icon.svg) | Černý text MicroCol v Inkscape `flowRoot`, nikoli ilustrace kostela. Rozměr dokumentu **1024 × 1024**. | Metadata ukazují na starou cestu `microcol-game/src/icon.png`, která v současném stromu není. Současná distribuční ikona s kostelem má odlišný obsah; tento SVG nelze označit za její doložený zdroj. Obecný SVG renderer nemusí podporovat `flowRoot` a může zobrazit prázdný náhled. |

Pracovní SVG mají různě velké stránky, často A4, a kresby mohou sahat mimo stránku. Rozměr stránky není rozměr herního obrázku. Staré absolutní cesty, výběry objektů, skryté vrstvy a exportní DPI nejsou automatický exportní manifest. Zejména soubory s více návrhy vyžadují určení správné vrstvy a exportované oblasti.

U `colony-frame.svg` jsou výslovná metadata `colony-warehouse.png` právě ve viditelné vrstvě **`g1251`, nazvané `colony-building-queue copy`**, nikoli ve skryté vrstvě pojmenované `colony-warehouse`. Při obnově tohoto exportu je tedy potřeba řídit se konkrétními objekty a jejich obsahem, ne pouze názvem vrstvy.

## Export SVG do rastrových souborů

### Co dnes provádí exportní skript

[images.groovy](../microcol-game/src/main/groovy/images.groovy) je samostatný přípravný nástroj. Maven build `microcol-game` jej nespouští; balí již připravené soubory z `src/main/resources`. Samotná úprava SVG tedy při běžném buildu PNG nepřegeneruje.

Skript má natvrdo uložené cesty k Inkscape a ke starému checkoutu `/Users/jan/Documents/repo/github/microcol/`. SVG kopíruje do `microcol-game/target/`, v kopii případně zamění barvy, zavolá Inkscape a některé PNG následně rozřeže přes Java `ImageIO`. Originální SVG přebarvováním nepřepisuje.

V horní části jsou volání `sunset`, `colony`, `europe`, `market`, `background` a `cursors` zakomentovaná. **Spouští se pouze `bullet(conf, colors)`.** Funkce `crates()` existuje, ale v seznamu volání vůbec není. Spustit současný soubor proto neznamená vyexportovat veškerou grafiku.

| Funkce | Vstupní SVG | `exportArea` předané Inkscape | Výstupní PNG | Požadovaný rozměr |
| --- | --- | --- | --- | --- |
| `background` | `background.svg` | `0:0:720:480` | `background.png` | 648 × 432 |
| `sunset` | `sunset2.svg` | `-446:-113:1422:1450` | `sunset.png` | 781 × 651 |
| `colony` | `colony-background.svg` | `-635:-292:1327:1611` | `colony.png` | 780 × 758 |
| `europe` | `europe1.svg` | `-885:-879:1180:772` | `europe.png` | 780 × 624 |
| `market` | `market.svg` | `-257:-114:1674:1536` | `market.png` | 700 × 598 |
| `crates` | `crates.svg` | `0:0:125:250` | `crates.png` | 200 × 400 |
| `cursors` | `cursors.svg` | `0:0:100:300` | `cursors.png` | 30 × 90 |
| `bullet` | `bullet.svg` | `0:0:50:50` | `bullet.png` | 15 × 15 |

`exportArea` je čtveřice hran oblasti předávaná exportéru, nikoli `(x,y,šířka,výška)` runtime výřezu. Tabulka zachycuje přesné argumenty současného skriptu. Jejich interpretaci a kompatibilitu je při obnovení exportního nástroje nutné ověřit s konkrétní verzí Inkscape; nelze je mechanicky zaměnit za čísla z `viewBox`.

Příkaz používá směs přepínačů `--without-gui`, `--export-type=png`, `--export-file`, `--export-area`, `--export-width`, `--export-height`. Skript nekontroluje návratový kód exportéru a řetězec příkazu nemá robustní ošetření cest s mezerami. Tento popis proto není potvrzením, že původní skript lze na dnešním prostředí spustit beze změn. Během tohoto rozboru spuštěn nebyl a žádné herní obrázky nebyly regenerovány.

### Přesné následné výřezy PNG

Pomocná funkce má signaturu `cut(source, target, width, height, positionX, positionY)`. Následující tabulka používá pro snadnější čtení pořadí **x, y, šířka, výška**. Souřadnice jsou v pixelech zdrojového PNG, od levého horního rohu. Vytvoří se nový průhledný rastrový obrázek dané velikosti; nejde o výběr SVG elementu ani o škálování.

| Zdroj PNG | Výstup | x | y | Šířka | Výška |
| --- | --- | ---: | ---: | ---: | ---: |
| `sunset.png` | `sunset-top-line.png` | 0 | 65 | 65 | 90 |
| `sunset.png` | `sunset-bottom-line.png` | 0 | 555 | 48 | 75 |
| `colony.png` | `colony-top1.png` | 20 | 74 | 65 | 203 |
| `colony.png` | `colony-top2.png` | 685 | 82 | 85 | 190 |
| `colony.png` | `colony-bottom.png` | 2 | 645 | 71 | 62 |
| `europe.png` | `europe-left.png` | 1 | 0 | 83 | 624 |
| `europe.png` | `europe-right.png` | 757 | 0 | 22 | 624 |
| `market.png` | `market-top.png` | 1 | 0 | 10 | 598 |
| `market.png` | `market-bottom.png` | 634 | 0 | 65 | 598 |
| `crates.png` | `crate-closed.png` | 0 | 0 | 200 | 200 |
| `crates.png` | `crate-open.png` | 0 | 200 | 200 | 200 |
| `cursors.png` | `cursor-goto.png` | 0 | 0 | 30 | 30 |
| `cursors.png` | `icon-steps-25x25.png` | 2 | 32 | 25 | 25 |
| `cursors.png` | `icon-steps-turn-25x25.png` | 2 | 32 | 25 | 25 |
| `cursors.png` | `icon-steps-anchor-25x25.png` | 2 | 62 | 25 | 25 |
| `cursors.png` | `icon-steps-anchor-turn-25x25.png` | 2 | 62 | 25 | 25 |

Při porovnání dekódovaných RGBA pixelů uložených souborů odpovídá **13 ze 16** výřezů přesně této tabulce. `colony-top2.png` se liší ve **2 pixelech**, `europe-left.png` v **5** a `europe-right.png` v **1**; alfa se neliší. Jsou to drobné odchylky, nikoli rozdílné kompozice. Důvod z aktuálního kódu nelze určit. Tabulka popisuje recept skriptu, ne tvrzení o přesné totožnosti všech uložených výstupů.

Varianty chůze a kotvy s `-turn` jsou podle skriptu stejný výřez jako základní varianta; také uložené dvojice jsou pixelově shodné. Samotná přítomnost dvou jmen tedy dnes nezajišťuje vizuální rozlišení aktuálního a následujícího tahu.

## Barvy a přebarvování

[colors.properties](../microcol-game/src/main/resources/colors.properties) má dvojí účel: exportní skript podle něj přebarvuje část SVG a [ColorScheme](../microcol-game/src/main/java/org/microcol/gui/ColorScheme.java) jej čte za běhu pro barevné výplně okolo pozadí.

| ID | Aktuální hodnota | Význam v souboru |
| --- | --- | --- |
| `sun-1` | `#d0d627` | Slunce |
| `grass-1` | `#489b4b` | Tráva |
| `green-2` | `#3a7238` | Stromy, kopce a další zelené prvky |
| `sky-1` | `#f3f3f5` | Obloha |
| `sea-1` | `#0084c9` | Moře |
| `dirt-1` | `#ad7141` | Hlína |

`colorReplacer` prochází přímé potomky `<defs>`, hledá shodu jejich `id` s klíčem barvy a v jediném podřízeném `<stop>` nahrazuje položku `stop-color` uvnitř `style`. Ostatní položky stylu zachovává. Při jiném počtu stylů stop vypíše upozornění a danou definici nepřebarví. Nejde o obecnou náhradu všech podobných barev v dokumentu a ani o obecné zpracování vícebodových přechodů.

| SVG | Nalezené definice odpovídající současným klíčům |
| --- | --- |
| `background.svg` | `green-2`, `grass-1`, `sea-1` |
| `colony-background.svg` | `dirt-1`, `sky-1`, `sea-1`, `grass-1` |
| `europe.svg` | `grass-1`, `dirt-1`, `sea-1`, `sky-1` |
| `europe1.svg` | `sun-1`, `grass-1`, `dirt-1`, `sea-1`, `sky-1` |
| `market.svg` | `grass-1`, `sea-1`, `sky-1` |
| `sunset2.svg` | `sun-1`, `grass-1`, `sea-1`, `sky-1` |

Ostatních 18 SVG tyto konkrétní definice nemá. Proto mohou původní SVG v editoru vypadat barevně jinak než hotová PNG. Po načtení upraveného `colors.properties` použije hra nové barvy pro výplně kreslené kódem, ale **pixely existujících PNG se tím nepřebarví**. Pro shodu se scénou je nutný i nový export příslušných PNG a krajů. `ColorScheme` načítá soubor jednou při svém vytvoření; změny souboru za chodu automaticky nesleduje.

Soubor [colors-backup.properties](../microcol-game/src/main/resources/colors-backup.properties) uchovává starší čtyřbarevnou variantu. Současný skript a `ColorScheme` vybírají `colors.properties`. Další barvy jsou zároveň přímo v SVG, Java kódu a CSS, takže těchto šest hodnot ještě není úplná paleta celé hry.

## Jak se řežou tři atlasy

[AbstractTiledImageLoader](../microcol-game/src/main/java/org/microcol/gui/image/AbstractTiledImageLoader.java) nejprve zkontroluje přesný rozměr souboru a potom přes `PixelReader` a `WritableImage` vytvoří všechny čtvercové výřezy. Nesprávný celkový rozměr vyvolá chybu při načítání.

Souřadnice `(x,y)` v dalších tabulkách znamenají **sloupec a řádek od nuly**, nikoli pixel. Sloupec roste doprava a řádek dolů. To je jiný prostor souřadnic než herní `Location`, která mapové pozice čísluje od jedničky.

| Atlas | Sloupce × řádky | Jeden výřez | Krok počátků | Povinný rozměr PNG | Klíč výřezu |
| --- | --- | --- | --- | --- | --- |
| `background.png` | 12 × 8 | 45 × 45 | 54 px | 648 × 432 | `type_x_y` |
| `buildings.png` | 8 × 7 | 70 × 70 | 70 px | 560 × 490 | `building_x_y` |
| `buttons.png` | 4 × 4 | 50 × 50 | 50 px | 200 × 200 | `button_x_y` |

Obecný výřez je `(x × krok, y × krok, velikost, velikost)`. Například galeona v `type_4_5` vzniká z pixelů začínajících na **(216,270)**, v obdélníku **45 × 45**.

U hlavního atlasu po 45 pixelech obrázku následuje **9 pixelů mezery vpravo a dole**. Není to devítipixelový vnitřní okraj ze všech stran. Mezera se počítá i za posledním sloupcem a řádkem. Odděluje kresby při antialiasingu exportu. [TileImageParams](../microcol-game/src/main/java/org/microcol/gui/image/TileImageParams.java) z ní odvozuje krok a rozměry.

Všechny buňky dostanou souřadnicový klíč, i prázdné nebo pracovní. Dostupnost v cache sama nedokládá, že je hra někde zobrazuje.

## Hlavní atlas background

Náhled ukazuje **skutečné runtime výřezy**, nikoli celou SVG stránku. Každá buňka je pro čitelnost zvětšená 2×; šachovnice znázorňuje průhlednost a není součástí herního obrázku.

![Výřezy hlavního atlasu se souřadnicemi](graphics-assets/background-grid.png)

### Terén, les, pole a město

Mapování je v [ImageLoaderTerrain](../microcol-game/src/main/java/org/microcol/gui/image/ImageLoaderTerrain.java).

| Buňka | Alias v paměti | Obsah |
| --- | --- | --- |
| `(0,0)` | `tile-high-sea.png` | Vysoké moře |
| `(1,0)` | `tile-grassland.png` | Travnatá základní plocha |
| `(3,0)`, `(4,0)` | `tile-tundra-1.png`, `tile-tundra-2.png` | Dvě varianty tundry |
| `(5,0)` | `tile-ocean-1.png` | Běžný oceán |
| `(6,0)` | `tile-ocean-2.png` | Oceán s vlnkou |
| `(7,0)` | `tile-ocean-3.png` | Oceán s velrybou |
| `(8,0)`, `(9,0)` | `tile-prairie-1.png`, `tile-prairie-2.png` | Dvě varianty prérie |
| `(10,3)` | `tile-hidden.png` | Celé neodhalené políčko |
| `(0,4)`, `(1,4)` | `tile-arctic-1.png`, `tile-arctic-2.png` | Dvě varianty arktidy |
| `(3,4)`, `(4,4)` | `tree-1.png`, `tree-2.png` | Dva lesní překryvy |
| `(5,4)`, `(6,4)` | `tile-mountain-1.png`, `tile-mountain-2.png` | Dvě varianty hor |
| `(0,5)` | `tile-town.png` | Kolonie na světové mapě |
| `(1,5)` | `field` | Překryv obdělávaného pole |
| `(6,5)`, `(7,5)` | `tile-hill-1.png`, `tile-hill-2.png` | Dvě varianty kopců |

[TerrainImageProvider](../microcol-game/src/main/java/org/microcol/gui/image/TerrainImageProvider.java) vybírá jednu výchozí variantu podle `TerrainType`. Hlavní mapa navíc používá [ImageRandomProvider](../microcol-game/src/main/java/org/microcol/gui/image/ImageRandomProvider.java) a [WorldMapRandoms](../microcol-game/src/main/java/org/microcol/gui/image/WorldMapRandoms.java). Variace závislé na seedu mapy jsou pro dané políčko stabilní: hory, kopce, prérie a arktida mají dvě varianty podle parity náhodného čísla, druhá tundra se vybírá při dělitelnosti sedmi, druhý les při dělitelnosti třemi a velryba při dělitelnosti 63.

Vlnka je časované přepnutí na jiný statický výřez, nikoli animace několika snímků. Počáteční výběr má šanci 1/51 a trvání 0–6 sekund; pozdější výběr se zkouší po tickách a trvá 10–21 sekund. Používá jiný generátor náhodných čísel než stabilní vzhled terénu. [TileAnimation](../microcol-game/src/main/java/org/microcol/gui/image/TileAnimation.java) převádí sekundy na tick podle `AnimationScheduler.FPS`, nyní 30.

### Pobřeží a okraje neodhalené mapy

Řádky **y=1, y=2, y=3**, vždy sloupce **x=0 až 9**, jsou zdrojové díly přechodů. Zelené pobřeží má prefix `grass-`, ledové `ice-` a neodhalený okraj `hidden-`.

Pomocná vrstva `description` v `background.svg` obsahuje čísla sloupců 0–11, řádků 0–7 a popisky `Grassland` a `Iceland` pro pobřežní řádky 1 a 2. Popisek pobřežního řádku `Grassland` není označením základní travnaté dlaždice `(1,0)`. Při exportu musí pomocná vrstva zůstat skrytá.

| Sloupec x | Základní jméno bez prefixu | Úloha |
| ---: | --- | --- |
| 0 | `well` | Volné políčko obklopené okolní hmotou |
| 1 | `u-shapeNorth-03` | U přechod, první zakončení |
| 2 | `u-shapeNorth-02` | U přechod, druhé zakončení |
| 3 | `u-shapeNorth-12` | U přechod, třetí zakončení |
| 4 | `l-shapeSouthEast-39` | Rohový L přechod |
| 5 | `l-shapeSouthEast-29` | Další zakončení L |
| 6 | `l-shapeSouthEast-2a` | Další zakončení L |
| 7 | `i-shapeNorth-03` | Jednostranný I přechod |
| 8 | `i-shapeNorth-04` | Další zakončení I |
| 9 | `i-shapeNorth-b4` | Další zakončení I |

[AbstractCoastImageLoader](../microcol-game/src/main/java/org/microcol/gui/image/AbstractCoastImageLoader.java) z deseti zdrojů každé rodiny vytvoří **81 runtime obrázků**: 1 well, 16 U, 16 L, 16 I a 32 II. Další orientace vznikají otočením po 90°, zrcadlením a transpozicí. II je složení dvou protilehlých I přechodů. Celkem tak vznikají tři rodiny po 81 obrázcích bez dalších SVG souborů.

Koncovky jako `03` nebo `2a` označují body napojení hran, nikoli čísla animačních snímků. Dvanáct konektorů `0..9,a,b` a jejich uspořádání popisují [AbstractCoastMapGenerator](../microcol-game/src/main/java/org/microcol/gui/image/AbstractCoastMapGenerator.java) a [Connector](../microcol-game/src/main/java/org/microcol/gui/image/Connector.java).

Konkrétní tvar určí osm okolních políček: přímí sousedé hlavní tvar a diagonály zakončení. Zelené pobřeží odděluje moře od pevniny kromě tundry/arktidy; ledové od tundry/arktidy. Kreslí se jako průhledný překryv na mořském políčku. Neodhalený okraj obdobně odděluje viditelnou a neviditelnou oblast. [MapManager](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/MapManager.java) dává při souběhu přednost zelenému pobřeží před ledovým; automaticky obě neskládá na jedno místo.

Technicky důležité je chování [ImageWrapper](../microcol-game/src/main/java/org/microcol/gui/image/ImageWrapper.java): při skládání vybírá v každém pixelu barvu s **větší neprůhledností**, při rovnosti první obrázek. Není to běžné prolnutí alfa kanálů. Operace jsou pevně velikosti 45 × 45. Průhlednost, napojení hran a otočitelnost kresby jsou tedy funkční vlastnosti. Spolu s pobřežím se otočí i případný nakreslený stín nebo směrová textura.

### Jednotky

Mapování je v [ImageLoaderUnit](../microcol-game/src/main/java/org/microcol/gui/image/ImageLoaderUnit.java), výběr varianty v [UnitImageProvider](../microcol-game/src/main/java/org/microcol/gui/image/UnitImageProvider.java).

| Buňka | Alias | Výběr |
| --- | --- | --- |
| `(4,5)` | `galeon_east` | Galeona směrem vpravo |
| Zrcadlo `(4,5)` | `galeon_west` | Galeona směrem vlevo |
| `(5,5)` | `tile-ship-frigate.png` | Fregata |
| Zrcadlo `(5,5)` | `frigate_left` | Vytvořená varianta, současný resolver ji nevybírá |
| `(2,4)` | `free-colonist` | Základní kolonista |
| `(0,6)` | `free-colonist-mounted` | Kolonista na koni |
| `(2,6)` | `free-colonist-tools` | Pěší kolonista s nástroji |
| `(6,6)` | `free-colonist-muskets` | Pěší kolonista se zbraněmi |
| `(5,6)` | `free-colonist-mounted-muskets` | Ozbrojený kolonista na koni |
| `(1,6)` | `wagon` | Registrovaný vůz, spotřebitel nenalezen |

U kolonisty má nejprve význam kůň, potom zbraně a u pěšího nástroje. Orientace nemění jeho sprite. Galeona má zvláštní větev pro východ a západ; fregata vždy dostává základní variantu. Jednotka mimo mapovou lokaci používá při běžném dotazu výchozí orientaci east.

Tytéž obrázky se používají na mapě, v přístavu, v docích, v nákladových slotech a seznamech. [PaintService](../microcol-game/src/main/java/org/microcol/gui/util/PaintService.java) kreslí jednotku i mapovou kolonii s posunem **(2,4)** od levého horního rohu mapového políčka, bez zmenšení. Obrázek proto může přesáhnout hranici svého políčka. Vlajku, jméno kolonie a znak akce jednotky kreslí kód zvlášť.

[AnimationWalk](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/AnimationWalk.java) pohybuje stejným obrázkem po trase. V současném atlasu není sekvence fází chůze.

### Zboží a produkce

Všech 19 aliasů zavádí [ImageLoaderGoods](../microcol-game/src/main/java/org/microcol/gui/image/ImageLoaderGoods.java); [GoodsImageProvider](../microcol-game/src/main/java/org/microcol/gui/image/GoodsImageProvider.java) je přiřazuje k `GoodsType`.

| Buňka | Alias | Obsah |
| --- | --- | --- |
| `(9,5)` | `good-bell.png` | Zvony |
| `(10,5)` | `good-cross.png` | Kříže |
| `(11,5)` | `good-goods.png` | Obchodní zboží |
| `(7,6)` | `good-horse.png` | Koně |
| `(8,6)` | `good-cigars.png` | Doutníky |
| `(9,6)` | `good-rum.png` | Rum |
| `(10,6)` | `good-tobacco.png` | Tabák |
| `(11,6)` | `good-ore.png` | Ruda |
| `(0,7)` | `good-hammer.png` | Kladiva, stavební produkce |
| `(1,7)` | `good-tools.png` | Nástroje |
| `(2,7)` | `good-lumber.png` | Dřevo |
| `(3,7)` | `good-coat.png` | Kabáty |
| `(4,7)` | `good-fur.png` | Kožešiny |
| `(5,7)` | `good-cotton.png` | Bavlna |
| `(6,7)` | `good-musket.png` | Muškety |
| `(7,7)` | `good-silk.png` | Textil, v kódu pojmenovaný silk |
| `(8,7)` | `good-silver.png` | Stříbro |
| `(9,7)` | `good-corn.png` | Jídlo, ikona kukuřice |
| `(10,7)` | `good-sugar.png` | Cukr |

Množství zboží nemění obrázek; číslo a další informace doplňuje UI. Zdrojový výřez má 45 × 45, ale [PanelColonyFields](../microcol-game/src/main/java/org/microcol/gui/screen/colony/PanelColonyFields.java) produkci polí kreslí **25 × 25**, [AbstractPanelConstructionWithSlots](../microcol-game/src/main/java/org/microcol/gui/screen/colony/AbstractPanelConstructionWithSlots.java) produkci stavby **30 × 30** a [UnitPanel](../microcol-game/src/main/java/org/microcol/gui/UnitPanel.java) malý náklad **15 × 15**. Při úpravách je nutné posuzovat i tyto skutečné zobrazované velikosti.

### Zbývající buňky a skládání mapy

Pro následující buňky nebyl v loaderech nalezen pojmenovaný alias ani použití jako zdroj pobřeží: `(2,0)`, `(10,0)`, `(11,0)`, `(10,1)`, `(11,1)`, `(10,2)`, `(11,2)`, `(11,3)`, `(7,4)` až `(11,4)`, `(2,5)`, `(3,5)`, `(8,5)`, `(3,6)`, `(4,6)`, `(11,7)`. Nadále existují jako `type_x_y`; některé obsahují kresbu či barevnou značku. Nejsou tím automaticky označeny za volné místo pro nové assety.

Pořadí uvnitř viditelného mapového políčka je: **základní terén → pobřeží → les → pole → barevné zvýraznění → okraj neodhalené oblasti**. Neviditelné políčko dostává celé `tile-hidden.png`. Nad mapou se dále vykreslují mřížka/výběr, kolonie, jednotky, plán cesty a animace; mrak je až nad nimi. Konkrétní pořadí určují [PaintService](../microcol-game/src/main/java/org/microcol/gui/util/PaintService.java), [GamePaintService](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/GamePaintService.java) a [GamePanelPainter](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/GamePanelPainter.java).

Les a pole tedy mají být samostatné překryvy. Pokud by nový les obsahoval neprůhlednou celou plochu podkladového terénu, zakryl by předchozí vrstvy. To je současný technický požadavek, nikoli rozhodnutí o budoucím stylu.

[EditorPaintingService](../microcol-game/src/main/java/org/microcol/gui/screen/editor/EditorPaintingService.java) používá jednodušší náhled: pevnou výchozí variantu terénu, vždy `tree-1.png` a překryv pole. Nevybírá náhodné varianty hlavní mapy ani generované pobřeží. Mapovou kolonii kreslí přímo do levého horního rohu políčka, bez herního posunu `(2,4)`. Náhled v editoru proto není úplnou kontrolou výsledného vzhledu ve hře.

## Atlas budov

![Výřezy atlasu budov se souřadnicemi](graphics-assets/buildings-grid.png)

[ImageLoaderBuilding](../microcol-game/src/main/java/org/microcol/gui/image/ImageLoaderBuilding.java) převádí následující buňky na aliasy `building_<ConstructionType>`. Všechny výřezy mají 70 × 70.

| Řádek y | Sloupec x a typ stavby |
| ---: | --- |
| 0 | 1 `CHAPEL`; 2 `CHURCH`; 3 `CATHEDRAL`; 4 `TOWN_HALL`; 5 `STABLES`; 6 `LARGE_STABLES` |
| 1 | 1 `TOBACCONISTS_HOUSE`; 2 `TOBACCONISTS_SHOP`; 3 `CIGAR_FACTORY`; 5 `CARPENTERS_STAND`; 6 `CARPENTERS_SHOP`; 7 `LUMBER_MILL` |
| 2 | 1 `FUR_TRADING_POST`; 2 `FUR_TRADERS_HOUSE`; 3 `FUR_FACTORY`; 5 `SCHOOLHOUSE`; 6 `COLLEGE`; 7 `UNIVERSITY` |
| 3 | 1 `BLACKSMITHS_HOUSE`; 2 `BLACKSMITHS_SHOP`; 3 `IRON_WORKS`; 5 `RUM_DISTILLERS_HOUSE`; 6 `RUM_DISTILLERY`; 7 `RUM_FACTORY` |
| 4 | 1 `WAREHOUSE_BASIC`; 2 `WAREHOUSE`; 3 `WAREHOUSE_EXPANSION` |
| 5 | 1 `WEAVERS_HOUSE`; 2 `WEAVERS_SHOP`; 3 `TEXTILE_MILL` |
| 6 | Bez pojmenovaných aliasů |

Ostatní buňky zůstávají přístupné pouze svým souřadnicovým klíčem. Z 42 konstant [ConstructionType](../microcol-game/src/main/java/org/microcol/model/ConstructionType.java) má zde přiřazení 30. Chybí `FORTRESS`, `FORT`, `STOCKADE`, `ARSENAL`, `MAGAZINE`, `ARMORY`, `SHIPYARD`, `DRYDOCK`, `DOCK`, `NEWSPAPER`, `PRINTING_PRESS`, `CUSTOM_HOUSE`. Neznamená to samo o sobě 12 chyb na aktuálních obrazovkách; znamená to, že tento loader pro tyto typy obrázek neposkytuje.

[AbstractPanelConstruction](../microcol-game/src/main/java/org/microcol/gui/screen/colony/AbstractPanelConstruction.java) dostane typ stavby, požádá o obrázek a vykreslí jej v původní velikosti. Tento konkrétní spotřebitel při chybějícím obrázku vyvolá chybu. [PanelConstructionBasic](../microcol-game/src/main/java/org/microcol/gui/screen/colony/PanelConstructionBasic.java) umisťuje stavbu na `(15,0)` do samostatného rámu, [PanelConstructionSimple](../microcol-game/src/main/java/org/microcol/gui/screen/colony/PanelConstructionSimple.java) na `(0,0)` bez něj.

Karta stavby není jediná bitmapa: rám přidává CSS, samotnou stavbu canvas, pracovníky a produkci další kreslení/UI. Navíc [ConstructionDraggablePanel](../microcol-game/src/main/java/org/microcol/gui/screen/colony/buildingqueue/ConstructionDraggablePanel.java) a [QueueListCell](../microcol-game/src/main/java/org/microcol/gui/screen/colony/buildingqueue/QueueListCell.java) v současné stavební frontě používají i pro stavební položky obrázek kolonisty. Nelze tedy předpokládat, že každý seznam staveb už využívá tento atlas.

## Atlas tlačítek

![Výřezy atlasu tlačítek se souřadnicemi](graphics-assets/buttons-grid.png)

Následuje skutečné přiřazení z [ImageLoaderButtons](../microcol-game/src/main/java/org/microcol/gui/image/ImageLoaderButtons.java), včetně existujícího nesouladu u kácení.

| Buňka | Alias | Význam |
| --- | --- | --- |
| `(0,0)` | `button_center` | Vycentrovat mapu |
| `(1,0)` | `button_help` | Nápověda |
| `(2,0)` | `button_statistics` | Statistiky |
| `(3,0)` | `button_exit` | Odchod, zpět či zavření podle obrazovky |
| `(0,1)` | `button_goals` | Cíle |
| `(1,1)` | `button_turn_report` | Přehled tahu |
| `(2,1)` | `button_next_turn` | Další tah |
| `(3,1)` | `button_europe` | Evropa |
| `(0,2)` | `button_move` | Pohyb |
| `(1,2)` | Bez pojmenovaného aliasu | V atlasu je kresba pařezu |
| `(2,2)` | `button_build_colony` | Založení kolonie |
| `(3,2)` | `button_cut_tree` a `button_build_road` | **Oba aliasy míří na stejnou buňku s cestou** |
| `(0,3)` | `button_plow_field` | Orba; současné UI stejnou ikonu používá také pro nezávislost |
| `(1,3)` | `button_buy` | Nákup |
| `(2,3)`, `(3,3)` | Bez pojmenovaných aliasů | Prázdná/pracovní buňka |

[ButtonImage](../microcol-game/src/main/java/org/microcol/gui/buttonpanel/ButtonImage.java) použije obrázek jako vycentrované neopakované pozadí tlačítka. [ButtonsPanel.css](../microcol-game/src/main/resources/gui/ButtonsPanel.css) stanovuje minimální velikost tlačítka 50 × 50. Samostatné výřezy pro hover a stisk se v této třídě nevybírají; chování a případné další zvýraznění řeší UI/CSS.

[ButtonsGamePanel](../microcol-game/src/main/java/org/microcol/gui/screen/game/components/ButtonsGamePanel.java) tlačítko nápovědy vytvoří, ale jeho přidání do panelu je zakomentované. Pro `BUTTON_CUT_TREE` a `BUTTON_BUILD_ROAD` nebyl nalezen spotřebitel mimo jejich definici a registraci. Sdílený chybný výřez proto dokumentujeme jako stav mapování, nikoli jako ověřenou viditelnou chybu na obrazovce. Tlačítka v ostatních screenech často sdílejí `button_exit`; nákup v Evropě používá `button_buy`.

## Jak se sestavují samostatné obrázky na obrazovkách

### Velké scény a opakované kraje

Pozadí menu, Evropy, tržiště a kolonie mají společný princip: barevné plochy přes celý canvas, opakované krajové obrázky a nakonec centrální PNG v původní velikosti. Kód jej umístí na `(velikost canvasu − velikost obrázku) / 2`. Změna velikosti okna tedy obrázek automaticky nenatahuje ani nezmenšuje; menší okno může kresbu oříznout.

[ImageStripePainter](../microcol-game/src/main/java/org/microcol/gui/background/ImageStripePainter.java) opakuje celé krajové PNG vodorovně. Volné místo uprostřed má šířku centrálního obrázku minus 10 px, takže střed překryje kraje přibližně o 5 px na každé straně. Následující posuny Y jsou měřené **od svislého středu canvasu**, nikoli od horního okraje hlavního PNG.

| Obrazovka a kód | Střed | Opakované kraje a posun Y | Barevné výplně |
| --- | --- | --- | --- |
| [GameMenuBackground](../microcol-game/src/main/java/org/microcol/gui/screen/menu/GameMenuBackground.java) | `sunset.png` | `sunset-top-line.png` obě strany, −260; `sunset-bottom-line.png` obě strany, +230 | Obloha, moře, tráva |
| [EuropeBackground](../microcol-game/src/main/java/org/microcol/gui/screen/europe/EuropeBackground.java) | `europe.png` | `europe-left.png` vlevo a `europe-right.png` vpravo, obě −312 | Obloha, moře, hlína |
| [MarketBackground](../microcol-game/src/main/java/org/microcol/gui/screen/market/MarketBackground.java) | `market.png` | `market-top.png` vlevo, `market-bottom.png` vpravo, obě −299 | Obloha, moře, tráva |
| [ColonyBackground](../microcol-game/src/main/java/org/microcol/gui/screen/colony/ColonyBackground.java) | `colony.png` | `colony-top1.png` vlevo −305; `colony-top2.png` vpravo −298; `colony-bottom.png` obě strany +265 | Obloha, tráva, tráva |

Návaznost horizontu, mola, země a krajových pásů závisí současně na ilustraci, výřezech i těchto číslech. Evropské lodě a postavy umisťuje zvlášť [Europe.css](../microcol-game/src/main/resources/gui/Europe.css). Posun mola v obrázku bez úpravy odpovídajícího UI proto může způsobit, že lodě nebo lidé budou stát mimo zamýšlené místo.

### Papír, dřevo a skutečné grafy

[StatisticsBackground](../microcol-game/src/main/java/org/microcol/gui/screen/statistics/StatisticsBackground.java) opakuje `background-wood2.png` a doprostřed položí `book2.png`. [TurnReportBackground](../microcol-game/src/main/java/org/microcol/gui/screen/turnreport/TurnReportBackground.java) postupuje stejně s `page.png`. Oba prostřední obrázky zachovávají původní rozměr. [RepeatingBackgroundPainter](../microcol-game/src/main/java/org/microcol/gui/background/RepeatingBackgroundPainter.java) začíná dláždění dřeva od středu canvasu směrem ven.

Čáry grafů nejsou nakreslené v SVG knihy. [StatisticsPanel](../microcol-game/src/main/java/org/microcol/gui/screen/statistics/StatisticsPanel.java) vytváří JavaFX `NumberAxis` a `LineChart<Number,Number>` nad skutečnými herními daty. Budoucí změna vzhledu grafů proto patří také do tohoto kódu a [Statistics.css](../microcol-game/src/main/resources/gui/Statistics.css), nejen do ilustrace pozadí.

### Rámy a dialogy přes CSS

Šest obrázků se načítá přímo z CSS: `dialog-petr.png`, `bullet.png`, `colony-fields.png`, `colony-frame.png`, `colony-building-queue.png` a `colony-warehouse.png`. Jejich použití je bez opakování. Rozměr obrázku není vždy totožný s rozměrem panelu:

| Obrázek | Rozměr souboru | Současná komponenta a obsah |
| --- | --- | --- |
| `dialog-petr.png` | 501 × 378 | [dialogs.css](../microcol-game/src/main/resources/gui/dialogs.css): panel 505 × 380, pravý textový padding 200 kvůli postavě; [DialogWithMan](../microcol-game/src/main/java/org/microcol/gui/dialog/DialogWithMan.java) připojuje styl. |
| `bullet.png` | 15 × 15 | [TurnReport.css](../microcol-game/src/main/resources/gui/TurnReport.css): prvek min. 20 × 20, posun Y 5. |
| `colony-fields.png` | 145 × 145 | [Colony.css](../microcol-game/src/main/resources/gui/Colony.css): panel max. 150 × 150 s paddingem 4 vlevo/nahoře. Vnitřní canvas kreslí 3 × 3 políčka, tedy 135 × 135. |
| `colony-frame.png` | 100 × 134 | Panel běžné stavby má preferované i maximální rozměry 100 × 135; uvnitř stavba, pracovníci a produkce. |
| `colony-building-queue.png` | 264 × 135 | Panel tesaře má preferované i maximální rozměry 264 × 137; canvas má posun X −82, popis X +37; kliknutí otevírá stavební frontu. |
| `colony-warehouse.png` | 846 × 134 | Panel skladu má minimální a preferovanou šířku 847, pevnou výšku 137 a posun X 220 a Y 293; vnitřní canvas posun `(10,−20)`. |

Pro `Colony.css` existuje také [Colony.scss](../microcol-game/src/main/sass/Colony.scss). Maven profil `sass` definuje generování CSS; běžný build tento profil automaticky nepoužívá. Při budoucích změnách rámů je nutné držet zdroj SCSS a používané CSS ve shodě.

### Náklad, kurzory, značky trasy a animace

[PanelCrateView](../microcol-game/src/main/java/org/microcol/gui/dock/PanelCrateView.java) zobrazuje `crate-open.png` nebo `crate-closed.png` přes `ImageView` s `fitWidth=45`, `fitHeight=45` a zachováním poměru stran. Na otevřenou bednu se přidává obrázek jednotky či zboží. Vlastní PNG bedny má 200 × 200.

[CursorServiceImpl](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/CursorServiceImpl.java) z `cursor-goto.png` vytváří JavaFX `ImageCursor` s aktivním bodem **(1,1)**. [MoveModeSupport](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/MoveModeSupport.java) vybírá běžnou, kotevní nebo bojovou značku plánované trasy. [GamePaintService](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/GamePaintService.java) pro značky 25 × 25 používá posun **(10,10)** uvnitř políčka 45 × 45 a podle dostupných kroků vybírá i variantu pro další tah.

[AnimationClouds](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/AnimationClouds.java) posouvá celý `cloud-1.png`, nezmenšuje jej a po průchodu opakuje cyklus. [AnimationFight](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/AnimationFight.java) při boji kreslí statický `crossed-swords.png`; tentýž soubor používá [DialogFight](../microcol-game/src/main/java/org/microcol/gui/dialog/DialogFight.java). Jednotlivé fáze mraku a mečů se z atlasu nečtou.

Odlišný případ je `bird.png` v menu: přes svou příponu jde o **animovaný GIF89a, 480 × 270, 80 snímků**. Java načte celý resource jako `Image`; kód jej kreslí do **80 × 45** a zároveň posouvá horizontálně. `bird-1.png` je také 80snímkový GIF, ale současný odkaz na něj nebyl nalezen. Při budoucím zpracování těchto souborů se nelze řídit pouze příponou.

## Úplný inventář rastrových souborů

Všechny položky jsou v [resources/images](../microcol-game/src/main/resources/images). Zkratka „odkaz nenalezen“ znamená výsledek hledání v aktuálním Java/CSS/build kódu, nikoli doporučení soubor odstranit. Podrobnosti spotřebitelů jsou uvedeny výše. „Původ nedoložen“ znamená, že mezi 24 posuzovanými SVG ani ve skriptu není potvrzený export tohoto obrázku.

| Soubor | Rozměr | Zdroj nebo role |
| --- | --- | --- |
| `background-wood.png` | 200 × 506 | `books2.svg`, odkaz nenalezen |
| `background-wood2.png` | 600 × 922 | `wood.svg`, statistiky a přehled tahu |
| `background.png` | 648 × 432 | `background.svg`, hlavní atlas |
| `bird-1.png` | 480 × 270 | GIF, původ nedoložen, odkaz nenalezen |
| `bird.png` | 480 × 270 | GIF, původ nedoložen, animovaný pták menu |
| `book.png` | 895 × 585 | `books.svg`, odkaz nenalezen |
| `book2.png` | 900 × 888 | `books2.svg`, pozadí statistik |
| `buildings.png` | 560 × 490 | `buildings.svg`, atlas staveb |
| `bullet.png` | 15 × 15 | `bullet.svg`, CSS odrážka |
| `buttons.png` | 200 × 200 | `buttons.svg`, atlas tlačítek |
| `cloud-1.png` | 799 × 479 | `clouds.svg`, překryv mapy |
| `cloud-2.png` | 1000 × 795 | `clouds.svg`, odkaz nenalezen |
| `colony-bottom.png` | 71 × 62 | Výřez `colony.png`, spodní kraj |
| `colony-building-queue.png` | 264 × 135 | Metadata `colony-frame.svg` i `colony-frames.svg`; DPI podporuje první, CSS panel |
| `colony-fields.png` | 145 × 145 | Stejná dvojice zdrojů; DPI podporuje `colony-frame.svg`, CSS rám polí |
| `colony-frame.png` | 100 × 134 | Odpovídající vrstva `colony-frame.svg`, přesný exportní recept nedoložen |
| `colony-top1.png` | 65 × 203 | Výřez `colony.png`, levý kraj |
| `colony-top2.png` | 85 × 190 | Výřez `colony.png`, pravý kraj, drobná pixelová odchylka |
| `colony-warehouse.png` | 846 × 134 | `colony-frame.svg`, CSS sklad |
| `colony.png` | 780 × 758 | `colony-background.svg`, centrální krajina |
| `crate-closed.png` | 200 × 200 | Výřez `crates.png`, zavřený slot |
| `crate-open.png` | 200 × 200 | Výřez `crates.png`, otevřený slot |
| `crates.png` | 200 × 400 | `crates.svg`, exportní mezisoubor, runtime odkaz nenalezen |
| `crossed-swords.png` | 70 × 70 | Původ nedoložen, boj na mapě a dialog |
| `cursor-goto.png` | 30 × 30 | Výřez `cursors.png`, kurzor |
| `cursors.png` | 30 × 90 | `cursors.svg`, exportní mezisoubor, runtime odkaz nenalezen |
| `dialog-petr.png` | 501 × 378 | `dialog.svg`, CSS dialog |
| `europe-left.png` | 83 × 624 | Výřez `europe.png`, levý kraj, drobná pixelová odchylka |
| `europe-right.png` | 22 × 624 | Výřez `europe.png`, pravý kraj, drobná pixelová odchylka |
| `europe.png` | 780 × 624 | Skript vybírá `europe1.svg`, centrální přístav |
| `icon-steps-25x25.png` | 25 × 25 | Výřez `cursors.png`, chůze |
| `icon-steps-anchor-25x25.png` | 25 × 25 | Výřez `cursors.png`, kotva |
| `icon-steps-anchor-turn-25x25.png` | 25 × 25 | Stejný výřez kotvy, další tah |
| `icon-steps-fight-25x25.png` | 25 × 25 | Původ nedoložen, bojová značka trasy |
| `icon-steps-fight-turn-25x25.png` | 25 × 25 | Původ nedoložen, bojová značka dalšího tahu |
| `icon-steps-turn-25x25.png` | 25 × 25 | Stejný výřez chůze, další tah |
| `icon-steps.png` | 551 × 551 | Původ nedoložen, odkaz nenalezen |
| `kings-seal-shadow.png` | 40 × 41 | `kings-seal.svg`, nesplněný cíl |
| `kings-seal.png` | 40 × 41 | `kings-seal.svg`, splněný cíl |
| `market-bottom.png` | 65 × 598 | Výřez `market.png`, pravý kraj |
| `market-top.png` | 10 × 598 | Výřez `market.png`, levý kraj |
| `market.png` | 700 × 598 | `market.svg`, centrální tržiště |
| `page.png` | 600 × 727 | `books2.svg`, přehled tahu |
| `splash-screen.png` | 200 × 200 | Metadata `logo.svg`, spouštěcí obrázek JVM |
| `sunset-bottom-line.png` | 48 × 75 | Výřez `sunset.png`, spodní kraj menu |
| `sunset-top-line.png` | 65 × 90 | Výřez `sunset.png`, horní kraj menu |
| `sunset.png` | 781 × 651 | Skript vybírá `sunset2.svg`, centrální menu |

[Obrazový přehled všech 47 souborů](graphics-assets/raster-overview.jpg) ukazuje zmenšené náhledy. U GIFů zachycuje pouze první snímek. Pro přesné výřezy atlasů slouží samostatné mřížky výše; pro posouzení detailů původní soubory.

Distribuční ikony mimo tento adresář nejsou součástí uvedených 47 položek. `microcol-dist/src/icon.png` obsahuje kostel a odpovídá 1024px obrazu v tamním `icon.icns`, zatímco `microcol-module` uchovává i staré textové ikony. Současný [MainStageBuilder](../microcol-game/src/main/java/org/microcol/gui/MainStageBuilder.java) vlastní ikonu okna nenastavuje a konfigurace `src/icon.icns` v [microcol-dist/pom.xml](../microcol-dist/pom.xml) je v zakomentovaném appbundle pluginu. Vazbu současných distribučních výstupů na konkrétní SVG bude potřeba sjednotit zvlášť.

## Co zachovat při budoucích grafických úpravách

Technická kompatibilita současné hry vyžaduje zachovat souřadnice, rozměry a význam výstupů, nebo zároveň upravit příslušný kód. Praktický postup pro jednu změnu je:

1. Najít zdroj SVG, skutečný výstupní soubor a všechny jeho spotřebitele. U nejasných ručních exportů nejprve potvrdit vrstvu a oblast na porovnání se současným PNG.
2. U atlasu určit buňku a její runtime alias. Zachovat počátek, velikost, průhlednost a případnou mezeru; pomocné vrstvy nevkládat do exportu.
3. U přebarvovaných SVG zachovat ID příslušných definic a ověřit přebarvený výstup, nejen náhled originálního SVG.
4. Exportovat do dočasného místa v přesných cílových rozměrech. U velkých scén připravit zároveň i jejich krajové výřezy; u beden a kurzorů jejich samostatné soubory.
5. Porovnat výstup s mřížkou a prověřit všechny používané varianty. Pobřeží posoudit také otočené a složené, zboží i při 15/25/30 px, bedny při 45 px.
6. Prohlédnout skutečné obrazovky v různých velikostech okna: návaznost krajů, umístění živých prvků, čitelnost, alfa překryvy a rámy kolem dynamického obsahu.
7. Zařadit společně upravené SVG, výsledná PNG a případné změny mapování/CSS. Aktualizovat tento katalog, pokud se změnil technický význam, rozměr nebo použití.

Pokud později vzniknou podrobná pravidla pro historické období, proporce, paletu, obrysy, světlo, přechody a textury, mohou na tento popis navázat. Do té doby se práce řídí [současným zadáním vzhledu](#soucasne-zadani-vzhledu). Technická omezení jako otáčení pobřeží, průhledné lesy nebo zmenšování produkčních ikon platí při každé úpravě.

## Nejasnosti a nesoulady k dalšímu řešení

| Zjištěný stav | Význam pro další práci |
| --- | --- |
| Export není součást buildu a skript nyní volá jen `bullet()` | Hromadná reprodukovatelná regenerace zatím není doložená. |
| Ruční exporty nemají jednotný manifest výběrů/vrstev | Metadata jsou vodítko; u některých zdrojů je nutné potvrdit přesný recept. |
| `sunset`/`sunset2`, `europe`/`europe1`, dvě sady rámů | Je potřeba zachovat rozlišení aktuálně doloženého zdroje a alternativní kresby. |
| `button_cut_tree` a `button_build_road` sdílejí `(3,2)` | Mapování se liší od očekávání podle kresby pařezu v `(1,2)`; v tomto rozboru nebylo opraveno. |
| `frigate_left` a `wagon` jsou registrované bez odpovídajícího současného použití | Příprava výřezu není totéž co aktivní herní zobrazení. |
| Pro 12 typů staveb nejsou aliasy | Při rozšíření použití těchto typů je potřeba doplnit grafické pokrytí nebo ošetřit chybějící obrázek. |
| Značky chůze/kotvy pro různé tahy jsou totožné | Rozdílné názvy dnes nenesou rozdílný vzhled. |
| Dvě `.png` jsou ve skutečnosti GIFy | Automatické nástroje musí ověřovat obsah formátu; převod jediného snímku by zahodil animaci. |
| Některé soubory nemají nalezený runtime odkaz či doložené SVG | Je nutné dohledat záměr/původ před úklidem; tento popis je neoznačuje k odstranění. |
| Barvy existují v properties, SVG, CSS i Java | Jednotná budoucí paleta bude vyžadovat sladění všech těchto míst. |

Starší [vývojová dokumentace](../microcol-site/src/site/apt/development.apt) popisuje ruční export `background.svg` a skrývání vrstvy `description`. Obsahuje ale překlep `scr/graphics` a odkaz na již nepřítomnou třídu `BackgroundImageLoader`. Aktuálním zdrojem rozměrů jsou výše uvedené loadery a `TileImageParams`.

## Rozsah ověření

Katalog vznikl z XML struktury všech 24 SVG, exportních metadat, exportního skriptu, skutečných rozměrů a formátů všech 47 rastrů a trasování jejich použití v Java, CSS a Maven konfiguraci. Výřezy atlasů byly zkontrolovány proti rozměrům loaderů a vizuálně prohlédnuty. Všech 16 následných PNG řezů ze skriptu bylo porovnáno s uloženými soubory po dekódování do RGBA; odchylky jsou uvedené výše. U SVG proběhla i pomocná vizuální kontrola, která nenahrazuje export v původním Inkscape ani jeho podporu písem a `flowRoot`.

Hra nebyla během tohoto rozboru spuštěna a Maven testy nebyly opakovány; popis zobrazování vychází z kódu a uložených obrázků. Produkční kód ani herní grafika nebyly změněny. Statické kontroly nedokládají pixelově totožnou regeneraci všech PNG ze současných SVG ani správný vzhled všech živých obrazovek.

Existující [ImageCacheTest](../microcol-game/src/test/java/org/microcol/gui/ImageCacheTest.java) ověřuje načtení, opakované použití a chybějící resource. Startup loadery kontrolují rozměry atlasů. [ImageProviderTestUtil](../microcol-game/src/test/java/org/microcol/integration/ImageProviderTestUtil.java) je ruční `main` utilita pro export obrázků z cache do `target/img`; není automatický vizuální test a exportuje jen tehdy načtené položky. Ke klíčům připojuje `.png`, takže některé výstupy mají `.png.png`.
