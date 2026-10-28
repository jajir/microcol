# Průzkumné hraní Codexem

1. [R-0334](requirements/done/R-0334.md) zavádí samostatný projekt
   [`microcol-ux-test`](../microcol-ux-test/pom.xml). Codex CLI běží v Dockeru
   vedle skutečné JavaFX hry na Xvfb. Z obrazovky získává PNG a přes MCP ovládá
   myš a klávesnici. Model používá běžné cloudové připojení Codexu.
2. Výsledkem je záznam pokusu a podložené UX hypotézy. Nejde o studii lidí,
   automatický důkaz dohratelnosti, JUnit scénář ani náhradu
   [lokální přejímky](publishing.md#local-acceptance-policy).
3. Dohodnutý profil z 28. října 2026: zkušený hráč počítačových her a tahových
   strategií, který konkrétní pravidla MicroCol poznává z obrazovky a hráčské
   nápovědy. Vlastní odhady musí označit; zkušenost s jinou strategií není
   dokladem pravidla MicroCol. Cíl upřesněný uživatelem je 40 dokončených tahů v jedné nové hře, s průběžnými screenshoty a hodnocením. Podrobné zadání vlastní
   [`player.md`](../microcol-ux-test/player.md).

## Oddělené prostředí

1. Přebíráme prostředí ze stávajícího UI image: Java, Xvfb, Openbox a kompozitor.
   Nový image má vlastní entrypoint, Codex CLI, oficiální Python MCP SDK,
   `scrot`, `xdotool` a `xclip`. Hra vzniká v oddělené build fázi jako stávající
   spustitelný JAR s jeho závislostmi na Javě 17; hráčská fáze neobsahuje herní zdroje ani vývojářské návody.
2. Chrome není potřeba. Screenshot obsahuje pixely skutečné virtuální obrazovky
   v původním rozlišení 1600 × 1200. Vstupy používají stejné souřadnice.
   Každá akce vrací nový snímek; potvrzení doručení vstupu neprokazuje splnění cíle.
3. Runtime má vlastní uživatelský profil. Uložené hry, nastavení a diagnostické logy se při
   normálním ukončení kopírují do výsledků. Běh neotevírá ani nemění hráčovy
   existující uložené hry. Tato první verze začíná vždy novou relací; automatické
   navazování dlouhých kampaní není implementované.
4. Přístupové údaje Codexu se připojují pouze při běhu a nejsou součástí image
   ani exportovaných výsledků. Shell, web a pluginy jsou pro hráče vypnuté;
   Codex používá sandbox pouze pro čtení a ovládání poskytuje devět MCP nástrojů. Obrazovka zůstává
   nedůvěryhodným obsahem, který nemůže změnit zadání.

## Spuštění

1. Potřebujeme dostupný Docker, hostitelský Python, přihlášený Codex CLI
   s dostupným `auth.json` a přesné ID původního Codex chatu. Běžný výběr modelu
   a úrovně reasoning se zachovává; povinné parametry zabraňují skryté změně.
   Přihlášení z macOS Keychain se automaticky neexportuje.
2. Projekt se sestavuje výslovně. Jeho přenosné tooling testy spustí
   `mvn -f microcol-ux-test/pom.xml test`, případně profil `ux-playtesting`.
   Tyto testy nevolají model, Docker ani běžnou UI regresi.
3. Nejprve ověřit skutečné prostředí. Příkazy se zadávají v kořeni repozitáře;
   `TASK_UUID` nahradit přesným ID tohoto chatu:

   ```sh
   python3 scripts/codex/run.py --thread TASK_UUID --timeout 1800 ux -- check
   ```

4. Runner vrátí adresář completion evidence a PID. Po callbacku prohlédnout
   výsledek a nový adresář `microcol-ux-test/runs/`. Úspěšný `check` ověřuje
   skutečné MCP obrázky, odmítnutí neplatného vstupu, fokus, Unicode, klávesu,
   tažení, kolečko, evidenci a start zabalené hry. Kontrolní okno je samostatná
   technická pomůcka; jeho úspěch není hráčský UX výsledek.
5. Spustit hráče se zvoleným modelem a reasoning. `IMAGE_ID` je identita
   úspěšně ověřeného image z `manifest.json`; při změně zdrojů sestavit nový:

   ```sh
   python3 scripts/codex/run.py --thread TASK_UUID --timeout 7800 ux -- \
     play --image IMAGE_ID --model SELECTED_MODEL --reasoning SELECTED_REASONING \
     --seconds 7200 --actions 1000
   ```

6. Limit času platí pro aktivní Codex průchod. Vnější watchdog zahrnuje přípravu
   a úklid. Akční limit se vztahuje na vstupy včetně čekání; další pozorování a
   poznámky zůstávají dostupné do konce časového limitu. Výchozí limity jsou 7 200 sekund a 1 000 akcí. Po vyčerpání času je
   relace neúplná. Nezvyšovat limit ani neopakovat běh jako náhradu za diagnózu.
7. [Completion runner](ai/completion-callbacks.md) používá společný lokální
   Docker zámek kvůli zatížení počítače. UX si zachovává vlastní spouštění,
   data i vyhodnocení; nespouští se automaticky v modelové, smoke ani plné regresi.
   Zrušení přes SIGTERM supervisoru uklidí pouze kontejnery vlastněné tímto během.

## Evidence a vyhodnocení

1. `manifest.json` identifikuje revizi, image, nastavení a při sestavení hashe
   vstupních zdrojů. `session.json` odlišuje dokončení, chybu a vypršení limitu.
   Ukončení procesu s nulovým kódem samo neznamená úspěch hráčského cíle. `documented_turns` počítá souvislé kontrolní body `Turn 01/40` až `Turn 40/40` s pozorováním, zdrojem a existujícím snímkem. Méně než 40 znamená neúplný běh (kód 2). Pravdivost počtu tahů stále vyžaduje kontrolu herní evidence.
2. `actions.jsonl` uchovává provedené akce, stav doručení/chyby, snímky před a po
   akci a poznámky s cílem, očekáváním, pozorováním, zdrojem informace a nálezem.
   `screenshots/` obsahuje odkazované PNG. Záznam se zapisuje průběžně na disk.
3. `report.md` je závěrečná česká zpráva Codexu. `codex-events.jsonl`, `codex.log`
   a logy hry/prostředí slouží následné technické diagnostice. Hráč je při
   průchodu nečte. `profile/` obsahuje exportovaný profil a uložené pozice.
4. Výsledky žijí mimo Maven `target`, jsou ignorované Gitem a nejsou automaticky
   publikované. `mvn clean` je nesmaže. Silové ukončení nebo výpadek hostitele
   nemusí stihnout závěrečný export profilu; již uložené snímky a události zůstávají.
5. Vývojář následně rozliší chybu hry, nejasné vysvětlení, nevhodnou strategii
   a selhání ovládacího nástroje. Nález vyžaduje konkrétní snímek a kroky.
   Potvrzené opravy mohou dostat vlastní regresní test, ale průzkumný protokol
   zůstává oddělený od běžné funkční evidence.

## Stav ověření — 28. října 2026

1. První příprava `20261028T102640Z-85c0910c3094` selhala na obsazeném UID 1000 v základním image. Oprava nechává image přidělit UID a při běhu používá hostitelská UID/GID a vlastní dočasný domovský adresář.
2. Druhý pokus `20261028T103157Z-e67bf49815da` sestavil image a ověřil přenos skutečných PNG přes MCP. Akce scroll vypršela na `xdotool mousemove --sync` při nezměněné pozici ukazatele; tato volba byla odstraněna. Kontrola uložené fixture navíc odhalila chybějící aktivaci jejího nezdobeného okna pro klávesnici. Setup okno aktivuje, vlastní kliknutí a Unicode vstup se nadále ověřují skutečným obsahem pole. Oba neúspěšné pokusy zůstávají neúspěšné; nejsou dokladem fungujícího textového vstupu.
3. Jednotkový test exportu původně selhal kvůli testovací náhradě `copyfile`, která nepřijímala parametr `follow_symlinks`. Náhrada byla omezena na přístupové údaje; skutečný export se dál ověřuje. Následně prošlo 11 přenosných UX testů a 24 testů completion runneru bez chyb či přeskočení.
4. Modelová fáze `20261028T103703Z-925d18c10f7d` prošla za 136,353 s. Úplných 349 XML reportů v `target/verification/run-x0e7rhv7` obsahuje 4 782 testů, nula selhání, chyb a přeskočení. Herní zdroje se tímto úkolem nemění.
5. Běh `20261028T103934Z-353488d531ce` znovu selhal na textovém vstupu. Pomocné Tk okno obcházelo správu Openboxu a nezískalo klávesové zaměření. Normální spravované okno se souřadnicemi odvozenými z jeho skutečné polohy opravilo příčinu; cílená diagnostika potvrdila přesný Unicode text, Enter, přetažení i kolečko.
6. Následující diagnostika odhalila samostatný problém modulárního runtime revize `e7857466b3ef10ebc14bde127ffed3ae1b55d661`: inicializace končí `CreationException`, konkrétně při načítání `images/status-gold.png` přes `ImageProvider.getRawImage`, přestože prostředek v jlink image existuje. Evidence je v `target/ux-focus-diagnostic/` a `target/ux-focus-diagnostic.log`. UX image proto používá již existující spustitelný JAR; tento úkol neopravuje modulární distribuci ani nepotvrzuje funkčnost instalátorů.
7. Běh `20261028T105137Z-ea36846cf4e5` potvrdil vstupy, ale start JARu narazil na výchozí `noexec` nového dočasného domovského adresáře. JavaFX do něj rozbaluje nativní knihovny. Připojení profilu nyní výslovně dovoluje jejich načtení (`exec`); přístupové údaje zůstávají jen v dočasném profilu.
8. Cílené ověření `20261028T105425Z-b541318e1a0f` prošlo devíti živými kontrolami za 13,557 s. Následující sestavení a kontrola `20261028T105501Z-b64f079af0f6` následně prošly za 116,461 s. Výsledky `microcol-ux-test/runs/20261028T105501Z-2c75cbbb` identifikují image `sha256:c94b785306ff32aa52e9b4069c0a7e5dc34fca0175ea95aa9776eb5874bbf68b`; tento tehdejší stav byl později rozšířen o konfiguraci Codexu a samostatnou JavaFX kontrolu popsanou níže.
9. První hráčský pilot použil již ověřený image `sha256:79f31a05530d4109a99b3860f5d623c0b1b0c0d0401b84bd6b140d644605cb9e`. Porovnání manifestů potvrdilo totožnost všech hráčských a herních souborů; rozdílem je pouze hostitelský `run.py` (oprava připojení profilu), který se uvnitř image nespouští. Model/reasoning zůstává podle hostitelského nastavení `gpt-6-astra` / `xhigh`, rozpočet 600 sekund a 60 vstupních akcí.
10. Pevný smoke/support výběr z `scripts/ui-tests/smoke.json` prošel v běhu `20261028T105553Z-acd674ff476e`: 12 úplných XML reportů, 21 testů, nula selhání, chyb i přeskočení. `target/verification/run-f35k9vha` eviduje 209,994 s; celý callback běh včetně úklidu 210,191 s, pod limitem 420 s. Čekání na společný zámek se podle politiky počítá odděleně. Hashová kontrola potvrdila nezměněné herní a testovací vstupy modelové i smoke fáze.
11. První pilot `20261028T105627Z-5f1367b19e1c` / `microcol-ux-test/runs/20261028T110028Z-27f3ce2d` skončil procesně úspěšně, ale **neprošel přejímkou hraní**: CLI odmítlo `screenshot` i `record_note` kvůli schvalování MCP a neprovedlo žádnou herní akci. Původní report i stav procesu zůstávají zachované. Konfigurace nyní výslovně povoluje devět nástrojů vlastního serveru (`enabled_tools`, `default_tools_approval_mode = "approve"`) podle [dokumentace Codex MCP](https://learn.chatgpt.com/docs/extend/mcp). Nula skutečných pozorování nyní znamená chybu relace i při nulovém návratovém kódu CLI.
12. Doplněný test nejprve odhalil chybné odsazení svého nového řádku; oprava je ověřená celým modulem: 12 testů, nula selhání, chyb a přeskočení (`target/ux-acceptance/maven.log`, zachované selhání `tooling-failed-indent.log`). Druhý krátký pilot `20261028T110456Z-9fe75da34b18` následně provedl skutečné vstupy přes Codex: při kontrole už měl 14 doručených akcí, tři pozorování a dvě poznámky. Byl vědomě ukončen při upřesnění cíle na 40 tahů; není přejímkou požadovaného průchodu.

13. Pro upřesněný cíl je zadání upravené na 40 skutečně dokončených tahů se souvislými poznámkami a snímky. Nový test ověřuje chybějící tah, chybějící snímek i všech 40 bodů: celkem 13 testů prošlo. Běh `20261028T111157Z-73f6e9bd8751` používá nové sestavení, stejný model/reasoning a rozpočet 7 200 sekund / 1 000 akcí; jeho přejímka zatím čeká na skutečné výsledky.
14. Během 40tahového průchodu se podařilo samostatnou JavaFX aplikací na jiném Xvfb displeji reprodukovat chybu nástroje pro přetažení: přímý přesun ze zdroje na cíl vyvolal `DRAG_DETECTED`, ale nikoli puštění na cíl. S mezilehlým pohybem stejná aplikace přijala data (`DROPPED`). Oprava `desktop.py` přidává mezikrok a živá kontrola nyní obsahuje samostatnou JavaFX fixture; původní Tk kontrola toto rozlišení nepokrývala. Důkazy jsou v `target/ux-acceptance/drag-probe.log` a `drag-calibration/`; opravená fixture prošla. Její Java soubor byl zkontrolován sdíleným formatterem (shodné tokeny, idempotence). Rozehraný 40tahový pokus dál používá svůj původní neměnný image: jeho neúspěšné nativní přetahování nelze vydávat za potvrzenou chybu hry. Diagnostika neměnila jeho plochu, ovladač ani hráčské instrukce. Úplná kontrola nového image ještě čeká na uvolnění lokálního slotu.
15. Při integraci byl pracovní strom posunut na aktuální `main` `d0ad1feed73e8745143766701d63890418bc50cd`. Číslo R-0332 mezitím obsadil samostatný dokončený Congress úkol, proto tato dosud necommitnutá analýza dostala volné R-0334. Čekající ověření `20261028T111857Z-d70d4c763ceb` a `20261028T114909Z-9a0e6b6acbe2` byla ukončena před spuštěním a budou nahrazena kontrolami sjednocených zdrojů. Původní 40tahový image zůstává beze změn a dokládá průchod revizí `e7857466b3ef10ebc14bde127ffed3ae1b55d661`; pozdější změnu obrazovky Congress tím neověřujeme.
16. Průchod `20261028T111157Z-73f6e9bd8751` skončil úspěšně: relace 7 167,951 s, celkový běh 7 294,346 s. Souvislých 40 checkpointů doplňuje počáteční nultý bod; celkem 46 poznámek, 581 doručených akcí a 1 214 snímků. Ruční pozice `MicroCol Turn 40 of 40` obsahuje `model.calendar.numberOfPlayedTurns = 40`, rok 1530; mezisave z 15. tahu sdílí `playthroughId = 858a4f54-93a7-4c48-a05e-8a86d3d77294`. Zpráva, kontrola a `audit.json` jsou v `microcol-ux-test/runs/20261028T111157Z-2db96ad5/`. Původní výpověď hráče zůstává oddělená od kontroly vývojáře.
17. Sjednocená modelová fáze `20261028T130033Z-2e0042747f3c` / `target/verification/run-4p2h7v2e` prošla: 349 úplných XML, 4 782 testů, nula selhání, chyb a přeskočení; celý běh 139,129 s. Následný UX check `20261028T130033Z-2e78df0e2b0c` selhal na kalibraci nové JavaFX fixture: snímek ukázal, že Openbox změnil její rozměry a polohu až po výpisu souřadnic. Fixture nyní chrání minimální velikost a měří souřadnice po zmapování okna; ovladač ani požadavek skutečného předání drag-and-drop dat se neoslabují. Selhání a snímky zůstávají v `runs/20261028T131551Z-c2ccf083`.

## Závěr prvního 40tahového průchodu

1. Výsledky jsou trvale uložené mimo pracovní strom v `/Users/jan/Library/Logs/Microcol/ux/20261028T111157Z-2db96ad5/`; původní adresář v `microcol-ux-test/runs/` na ně odkazuje. `review.html` nabízí galerii po tazích, původní zprávu i kontrolu vývojáře; `audit.json` dokládá počet tahů, totožnost hry a kontrolní součty. Výsledky se nepublikují do Gitu.
2. Hráč založil New Amsterdam a Delft, měnil výrobu, stavěl, obchodoval, vybavoval jednotky a zkoumal mapu. Není to pouze čtyřicet stisknutí dalšího tahu.
3. Konkrétní UX podněty: u vozu dialog říká jen obecnou nedostupnost místo chybějící podmínky; automatický nákup 223 mušket zanechal tři zlaté a hráč nerozuměl předem jeho rozsahu; opakované zmenšování oken vyžadovalo maximalizaci. Příslušné snímky a rozlišení pozorování od hypotéz jsou v `review.md`. Nákup není prokázanou chybou ekonomických pravidel.
4. Náhled konkrétní produkce pomohl plánovat hospodářství. Alternativní příkazy pohybu a obchodní trasy umožnily pokračovat přes potíže s přetažením. Prokázaná chyba původního ovladače drag-and-drop se nepočítá jako chyba hry; oprava je součástí nástroje a má vlastní živé ověření.
5. Jde o jednu AI relaci na označené revizi. Neprokazuje dohratelnost celé kampaně ani zkušenost lidských hráčů. Congress byl na main mezitím změněn samostatným úkolem; jeho novou podobu tento starší průchod nehodnotí.

## Konečná lokální přejímka

1. Finální zdroje navazují na `main` `413d0126e9f7c6487650092640ad1ca9faa6e8ed`, včetně samostatně dokončené nové nápovědy. Přenosné testy modulu: 13; completion runner: 24; obojí bez selhání a přeskočení. JavaFX fixture prošla sdíleným formatterem, shodou tokenů a kontrolou idempotence.
2. Model/JUnit: `20261028T132606Z-211e19ae0499`, `target/verification/run-isdd3osf`; 349 úplných XML, 4 782 testů, nula selhání/chyb/přeskočení; 140,899 s včetně úklidu.
3. Cílené prostředí: `20261028T133024Z-bdd0ed235d2a`, `microcol-ux-test/runs/20261028T133024Z-5d294f65`; všech 10 živých X11/MCP/JavaFX kontrol prošlo za 119,464 s včetně sestavení a úklidu. Image `sha256:1659b786eec99082f57e12dffe33e4afa24fb3833b4545f9b7ab068f7b7b6d95`. Kontrola 1 758 vstupních hashů neodhalila rozdíl proti přijímaným zdrojům. JavaFX kontrola ověřuje skutečné předání dat cíli, nejen pohyb se stisknutým tlačítkem.
4. Pevný smoke/support výběr `scripts/ui-tests/smoke.json`: `20261028T133224Z-04f6943148e1`, `target/verification/run-ywb04jqt`; 12 úplných XML, 21 testů, nula selhání/chyb/přeskočení. Verifier eviduje 214,498 s; celý běh 214,655 s včetně úklidu, pod limitem 420 s. Interní název této fáze v souhrnu je `precommit`; příkaz je `verify.sh --smoke`. Modelové a smoke vstupy byly porovnány s přijímanými zdroji.
5. Dokumentační konzistence a strict MkDocs jsou součástí závěrečné přejímky. Hraní výše dokládá svou původní revizi; finální technické kontroly nástroje dokládají novější sjednocené zdroje. Tato lokální přejímka neověřuje vzdálené CI ani nově publikovanou distribuci.
