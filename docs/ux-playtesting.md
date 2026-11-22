# Průzkumné hraní Codexem

Samostatný projekt [`akropia-ux-test`](../akropia-ux-test/pom.xml) spouští Codex
CLI vedle skutečné JavaFX hry v Dockeru s Xvfb/Openbox. Hráč vidí snímky a ovládá
myš a klávesnici přes MCP. Jde o podložená AI pozorování, nikoli lidskou studii,
důkaz dohratelnosti nebo náhradu [funkční přejímky](publishing.md#local-acceptance-policy).

## Profily hráčů — 20. listopadu 2026

Schválené rozšíření odděluje tři profily. Každý příkaz spustí právě jeden profil;
ostatní se automaticky nespouštějí. Hráči neznají staré nálezy ani zdrojové kódy.
Společné instrukce jsou v [`player.md`](../akropia-ux-test/player.md), odlišné cíle
v adresáři [`players`](../akropia-ux-test/players/strategist.md).

| `--player` | Výchozí herní okno | Zaměření |
| --- | --- | --- |
| `strategist` | 45 minut | Zkušený hráč strategií, nováček v Akropii. Free game, skutečný cíl hry, co největší smysluplný pokrok. |
| `ux-reviewer` | 45 minut | Cesty hráče, hledání informací, nejistota při hraní a samostatná vizuální kontrola UI; bez cílového počtu tahů. |
| `beginner` | 40 minut | Běžné počítačové dovednosti bez předpokládané znalosti žánru. Objevování cíle a prvních smysluplných akcí. |

Začátečník je omezený přístup AI, nikoli model zbavený znalostí nebo náhrada lidského
nováčka. UX průzkumník eviduje i nedostupné a nestihnuté oblasti; netvrdí úplné
pokrytí všech herních stavů. Připravené pozice pro pozdní obrazovky ani automatické
navazování rozehraných kampaní tato verze nezavádí.

## Oddělené prostředí

1. Prostředí přebírá Java, Xvfb, Openbox a kompozitor ze základního UI image.
   UX image přidává vlastní entrypoint, Codex CLI, Python MCP, scrot, xdotool a
   xclip. Hra se sestaví jako spustitelný JAR na Javě 17. Hráčská fáze neobsahuje
   herní zdroje ani vývojářské návody. Chrome není potřeba.
2. Snímky obsahují skutečnou plochu 1600 × 1200; souřadnice nejsou přepočítané.
   Doručení vstupu neprokazuje jeho zamýšlený účinek. Dostupných zůstává devět
   desktopových MCP nástrojů, bez shellu, webu a pluginů.
3. Každý běh má čistý soukromý profil. Neotevírá ani nepřepisuje osobní uložené
   hry uživatele. Při normálním ukončení exportuje herní profil do výsledků.
4. Přihlašovací údaje jsou připojené jen při běhu a nevstupují do image ani ZIPu.
   Vybraný prompt se připojí samostatně pouze pro čtení. Jeden kompatibilní image
   lze použít se všemi třemi profily bez nového sestavování kvůli výběru hráče.
5. Image nese protokol nástroje `2`. Starý image s pevnou kontrolou 40 tahů
   nový runner odmítne; je nutné jej jednou znovu sestavit a ověřit.

## UX/UI reviewer — 22. listopadu 2026

Profil `ux-reviewer` má nadále výchozí okno 45 minut, bez cílového počtu tahů.
Hraní slouží k dosažení konkrétních situací. Před hledáním zaznamená hráčův cíl,
potřebnou informaci a očekávané místo; poté skutečné kroky, návraty, přečtenou
nápovědu a to, co umožnilo pokračovat nebo zůstalo nejasné. Nenalezená informace
znamená „nenašel jsem ji na těchto místech“, nikoli důkaz, že ve hře neexistuje.

Každá dosažená podstatně odlišná obrazovka dostane samostatnou vizuální kontrolu:
hierarchie a oddělení sekcí, zarovnání, mezery, čitelnost, zalamování, barvy,
konzistence ovládání a viditelnost odezvy. Srovnání obrazovek odkazuje na oba
snímky. Vizuální očekávání hráč odvozuje z viditelné hry, nikoli z nepřístupných
projektových dokumentů. Pokrytí rozlišuje ovládání, vizuální kontrolu a pouhou
návštěvu; záznam fungujícího tlačítka sám neprokazuje kontrolu designu.

Report má čtyři skupiny: **překážky při hraní**, **hledání informací**,
**vizuální problémy UI** a **povedené interakce**. Každý nález zachovává ID,
obrazovku, cíl, očekávání, postup hledání, výsledek, dopad a screenshoty.
Doporučení je označeno zvlášť od pozorování; opakovaný problém má jedno ID
s odkazy mezi skupinami. Není předepsaný minimální počet nálezů.

Změna upravuje pouze text profilu a zachovává společný protokol poznámek,
časové rezervy, ukládání i profily `strategist` a `beginner`. Listopadové piloty
níže ověřovaly předchozí zadání; nedokládají kvalitu výstupu této úpravy.

## Spuštění

Potřebujeme Docker, hostitelský Python, přihlášený Codex CLI s dostupným
`auth.json` a přesné ID původního Codex chatu. Model a reasoning se zachovávají
podle volby uživatele; hráčské profily nevybírají jiný model.

Nejprve technická kontrola sestavení a skutečného ovládání:

```sh
python3 scripts/codex/run.py --thread TASK_UUID --timeout 1800 ux -- check
```

Po callbacku zkontrolovat `result.json`, `selfcheck.json`, `session.json` a
`manifest.json`. Použít neměnné `IMAGE_ID` z tohoto ověřeného sestavení. Následující
příkazy jsou samostatné alternativy, nikoli povinná série:

```sh
python3 scripts/codex/run.py --thread TASK_UUID --timeout 3300 ux --   play --image IMAGE_ID --player strategist --model SELECTED_MODEL --reasoning SELECTED_REASONING

python3 scripts/codex/run.py --thread TASK_UUID --timeout 3300 ux --   play --image IMAGE_ID --player ux-reviewer --model SELECTED_MODEL --reasoning SELECTED_REASONING

python3 scripts/codex/run.py --thread TASK_UUID --timeout 3000 ux --   play --image IMAGE_ID --player beginner --model SELECTED_MODEL --reasoning SELECTED_REASONING
```

`--player` je u `play` povinný. `--seconds` může změnit herní okno na 1–7200 sekund;
`--actions` nastaví nezávislou pojistku 1–1000 vstupů (výchozí 1000). Pro vlastní
okno přizpůsobit vnější watchdog: herní okno + 300 sekund na závěr + rezerva na
přípravu a úklid. Maximální okno lze spustit s timeoutem 7800 sekund.

Herní čas začíná před spuštěním hráče, po přípravě kontejneru a startu hry. Zahrnuje
odpovědi modelu, pozorování a poznámky, proto není srovnatelný s rychlostí lidského
hráče. Posledních 120 sekund nebo 25 akcí hráč rezervuje pro uložení. Následuje
samostatný limit 300 sekund na uložení, kontrolní načtení a report, bez dalších
herních tahů. Povolení vstupů v tomto závěru nelze automaticky omezit jen na ukládání;
dodržení ověřuje reviewer z evidence. Tvrdé vypršení watchdogu zůstává neúplným během.

Runner používá [completion callbacks](ai/completion-callbacks.md) a společný
lokální Docker zámek. Zaznamenat adresář a PID, nepollovat opakovaně a po callbacku
zkontrolovat doklady. UX běhy zůstávají oddělené od model/JUnit a smoke regrese.

## Evidence a hodnocení

1. `manifest.json` uvádí ID běhu, profil, model/reasoning, herní a reportovací
   rozpočet, revizi a hash image. `prompt.md` a jeho SHA-256 zachycují konkrétní
   společné a profilové zadání včetně prefixu uložených pozic. Při použití staršího
   kompatibilního image je autoritou herních zdrojů `image_revision`, ne HEAD runneru.
2. `record_note` zachovává cíl, očekávání, pozorování, zdroj a snímek. Kategorie
   `progress` s volitelným `turn` zaznamenává rozhodnutí a skutečné přechody od nuly;
   počet kol se neodvozuje z kalendáře. Žádný profil nemá povinné minimum 40 tahů.
3. Kategorie `finding` nese text, klasifikaci `bug`, `unclear`, `contradiction`,
   `hypothesis`, `tool_error` nebo `positive`, závažnost `high`, `medium`, `low`,
   `info` a automatické ID `UX-001` atd. ID je jednoznačné společně s ID běhu.
   `bug` je hráčovo hodnocení, ne potvrzení příčiny v kódu. Počet nálezů není kvóta.
4. `coverage` uvádí obrazovku/cestu a `tested`, `visited`, `unavailable` nebo
   `not_reached`. Historie se zachovává; samotné otevření obrazovky není otestování
   všech akcí. Váhání se dokládá hledáním, návraty, pokusy nebo výslovnou nejistotou,
   nikoli délkou odpovědi modelu. Protimluv musí mít oba konkrétní podklady.
5. Poslední poznámka `session` uvádí `time_window`, `victory`, `defeat`, `blocked`
   nebo `action_limit`. Validátor kontroluje evidenci, souvislost zaznamenaných kol,
   snímky, pokrytí a rozpočet u příslušného důvodu. Platný časový průzkum může mít
   nula dokončených kol. `completed` znamená dokončený protokol, nikoli vyhranou hru.
   `ending_verified: false` a `review_status: pending` vyžadují nezávislé posouzení.
6. Hráč ukládá ruční pozice přibližně po deseti kolech, před zásadním nevratným
   krokem a na konci. Report rozlišuje potvrzené uložení a skutečné kontrolní načtení.
   Předchozí pozice ani původní evidence se nepřepisují.
7. Každý běh automaticky vytvoří `review.html`, `review.json` a `ux-results.zip`
   s reporty, časovou osou, citovanými snímky a exportovanými pozicemi. ZIP rozbalit
   celý a otevřít `review.html`; soubory `.akropia` importovat ve hře. JSON obsahuje
   kontrolní součty kopií pozic. Archiv není nezávislá přejímka a kompatibilita s
   jinými revizemi není zaručena. Neobsahuje autentizaci ani interní konverzaci modelu.
8. `actions.jsonl`, všechny původní snímky, `report.md`, `codex-events.jsonl` a
   diagnostické logy zůstávají v ignorovaném `akropia-ux-test/runs/`, mimo Maven
   clean. Pád hostitele nebo silové ukončení může zabránit finálnímu exportu.
9. Srovnávat stejný profil, revizi/scénář, model, rozpočet a podle možnosti seed.
   Měřit milníky a úspěšné cesty vedle počtu kol. Staré nálezy se neimportují do
   nového reportu jako současné chyby; potvrzení opravy vyžaduje nové pozorování.

## Přenosné a živé kontroly

```sh
mvn -f akropia-ux-test/pom.xml test
```

Přenosné testy nevolají cloud ani Docker. Ověřují volbu profilů, rozpočty,
strukturovanou evidenci, odmítnutí neúplných dokladů, bezpečné exporty a zachování
pozic. Živý `check` navíc ověřuje skutečné X11/MCP vstupy, JavaFX drag-and-drop,
strukturované poznámky a start zabalené hry. Smysluplnost odlišných profilů se
ověřuje samostatnými hráčskými piloty; samotný technický check ji neprokazuje.

## Stav rozšíření — 20. listopadu 2026

1. Tři profily lze spouštět jednotlivě pomocí `--player`. Lokální přejímka
   vychází z pracovní verze nad revizí `f3fdb8d4fc4ddf7a09a0a2a197e3280f3a0989cb`;
   přesné hashe zdrojů a běhy zachovává `target/ux-profiles-20261120/acceptance.json`.
   Prošlo 20 testů nástrojů, 5 144 model/JUnit testů
   (`run-pqtzpa7z`), 11 živých X11/MCP kontrol
   (`20261120T102123Z-0eeadae3`) a 23 smoke testů (`run-z7_blp5o`).
   Smoke včetně dokončení trval 295,258 s při limitu 420 s; bez chyb a přeskočení.
2. Všechny tři úspěšné piloty použily stejný image
   `sha256:df39878f8069a71e815fee4398e23eefb159a3bb36411660f8b6a1adf94fbb5c`,
   model `gpt-6-astra`, reasoning `medium`, samostatný čistý profil a novou hru.
   Výsledky jsou lokálně v `akropia-ux-test/runs/`:

   | Profil | Běh | Tahy | Nálezy včetně pozitivních | Exportované pozice |
   | --- | --- | ---: | ---: | ---: |
   | stratég, 45 minut | `20261120T102854Z-ec1bbb4f` | 49 | 6 | 16 |
   | UX reviewer, 45 minut | `20261120T111441Z-8ea2f33c` | 17 | 9 | 14 |
   | začátečník, 40 minut | `20261120T120237Z-4d5df49f` | 23 | 8 | 15 |

3. Počty pozic zahrnují ruční uložení i autosave. ZIPy prošly kontrolou integrity
   a kopie pozic souhlasí s evidovanými SHA-256. Závěrečné screenshoty odpovídají
   popsanému pokroku: stratég dosáhl 100 % podpory nezávislosti, reviewer 3 %,
   začátečník 12 %. Všechny běhy skončily časovým oknem, bez vítězství či porážky.
   Profily poskytly rozdílné průchody: stratég rozvíjel ekonomiku a obranu,
   reviewer zaznamenal 18 položek pokrytí a začátečník hledal postup v nápovědě.
   Počet tahů není srovnáním lidských schopností; světy se lišily a latence modelu
   je součástí časového okna. Jednotlivé nálezy zůstávají hypotézami k samostatnému
   posouzení, nikoli potvrzenými chybami nebo důkazem úplného pokrytí.
4. Neúspěšné pokusy zůstávají zachované. Příprava prvního modelového běhu
   překročila 20 s před startem testů. První živý check odhalil chybné odsazení
   kontroly za uzavřeným MCP spojením; opravený check prošel. První pilot stratéga
   `20261120T092937Z-c48f7f5e` odehrál 45 tahů, ale selhal na nesprávně opsaném
   odkazu na screenshot. Nově se takový odkaz odmítne už při zápisu poznámky,
   aby jej hráč mohl opravit. Následné kontroly i tři piloty prošly;
   původní neúspěchy se nepřepisují na úspěch.

Následující říjnová evidence patří historické verzi s pevnými 40 tahy a nedokládá
aktuální rozšíření. Lokální přejímka neprokazuje publikaci nové distribuce hry.

## Stav ověření — 28. října 2026

1. První příprava `20261028T102640Z-85c0910c3094` selhala na obsazeném UID 1000 v základním image. Oprava nechává image přidělit UID a při běhu používá hostitelská UID/GID a vlastní dočasný domovský adresář.
2. Druhý pokus `20261028T103157Z-e67bf49815da` sestavil image a ověřil přenos skutečných PNG přes MCP. Akce scroll vypršela na `xdotool mousemove --sync` při nezměněné pozici ukazatele; tato volba byla odstraněna. Kontrola uložené fixture navíc odhalila chybějící aktivaci jejího nezdobeného okna pro klávesnici. Setup okno aktivuje, vlastní kliknutí a Unicode vstup se nadále ověřují skutečným obsahem pole. Oba neúspěšné pokusy zůstávají neúspěšné; nejsou dokladem fungujícího textového vstupu.
3. Jednotkový test exportu původně selhal kvůli testovací náhradě `copyfile`, která nepřijímala parametr `follow_symlinks`. Náhrada byla omezena na přístupové údaje; skutečný export se dál ověřuje. Následně prošlo 11 přenosných UX testů a 24 testů completion runneru bez chyb či přeskočení.
4. Modelová fáze `20261028T103703Z-925d18c10f7d` prošla za 136,353 s. Úplných 349 XML reportů v `target/verification/run-x0e7rhv7` obsahuje 4 782 testů, nula selhání, chyb a přeskočení. Herní zdroje se tímto úkolem nemění.
5. Běh `20261028T103934Z-353488d531ce` znovu selhal na textovém vstupu. Pomocné Tk okno obcházelo správu Openboxu a nezískalo klávesové zaměření. Normální spravované okno se souřadnicemi odvozenými z jeho skutečné polohy opravilo příčinu; cílená diagnostika potvrdila přesný Unicode text, Enter, přetažení i kolečko.
6. Následující diagnostika odhalila samostatný problém modulárního runtime revize `e7857466b3ef10ebc14bde127ffed3ae1b55d661`: inicializace končí `CreationException`, konkrétně při načítání `images/status-gold.png` přes `ImageProvider.getRawImage`, přestože prostředek v jlink image existuje. Evidence je v `target/ux-focus-diagnostic/` a `target/ux-focus-diagnostic.log`. UX image proto používá již existující spustitelný JAR; tento úkol neopravuje modulární distribuci ani nepotvrzuje funkčnost instalátorů.
7. Běh `20261028T105137Z-ea36846cf4e5` potvrdil vstupy, ale start JARu narazil na výchozí `noexec` nového dočasného domovského adresáře. JavaFX do něj rozbaluje nativní knihovny. Připojení profilu nyní výslovně dovoluje jejich načtení (`exec`); přístupové údaje zůstávají jen v dočasném profilu.
8. Cílené ověření `20261028T105425Z-b541318e1a0f` prošlo devíti živými kontrolami za 13,557 s. Následující sestavení a kontrola `20261028T105501Z-b64f079af0f6` následně prošly za 116,461 s. Výsledky `akropia-ux-test/runs/20261028T105501Z-2c75cbbb` identifikují image `sha256:c94b785306ff32aa52e9b4069c0a7e5dc34fca0175ea95aa9776eb5874bbf68b`; tento tehdejší stav byl později rozšířen o konfiguraci Codexu a samostatnou JavaFX kontrolu popsanou níže.
9. První hráčský pilot použil již ověřený image `sha256:79f31a05530d4109a99b3860f5d623c0b1b0c0d0401b84bd6b140d644605cb9e`. Porovnání manifestů potvrdilo totožnost všech hráčských a herních souborů; rozdílem je pouze hostitelský `run.py` (oprava připojení profilu), který se uvnitř image nespouští. Model/reasoning zůstává podle hostitelského nastavení `gpt-6-astra` / `xhigh`, rozpočet 600 sekund a 60 vstupních akcí.
10. Pevný smoke/support výběr z `scripts/ui-tests/smoke.json` prošel v běhu `20261028T105553Z-acd674ff476e`: 12 úplných XML reportů, 21 testů, nula selhání, chyb i přeskočení. `target/verification/run-f35k9vha` eviduje 209,994 s; celý callback běh včetně úklidu 210,191 s, pod limitem 420 s. Čekání na společný zámek se podle politiky počítá odděleně. Hashová kontrola potvrdila nezměněné herní a testovací vstupy modelové i smoke fáze.
11. První pilot `20261028T105627Z-5f1367b19e1c` / `akropia-ux-test/runs/20261028T110028Z-27f3ce2d` skončil procesně úspěšně, ale **neprošel přejímkou hraní**: CLI odmítlo `screenshot` i `record_note` kvůli schvalování MCP a neprovedlo žádnou herní akci. Původní report i stav procesu zůstávají zachované. Konfigurace nyní výslovně povoluje devět nástrojů vlastního serveru (`enabled_tools`, `default_tools_approval_mode = "approve"`) podle [dokumentace Codex MCP](https://learn.chatgpt.com/docs/extend/mcp). Nula skutečných pozorování nyní znamená chybu relace i při nulovém návratovém kódu CLI.
12. Doplněný test nejprve odhalil chybné odsazení svého nového řádku; oprava je ověřená celým modulem: 12 testů, nula selhání, chyb a přeskočení (`target/ux-acceptance/maven.log`, zachované selhání `tooling-failed-indent.log`). Druhý krátký pilot `20261028T110456Z-9fe75da34b18` následně provedl skutečné vstupy přes Codex: při kontrole už měl 14 doručených akcí, tři pozorování a dvě poznámky. Byl vědomě ukončen při upřesnění cíle na 40 tahů; není přejímkou požadovaného průchodu.

13. Pro upřesněný cíl je zadání upravené na 40 skutečně dokončených tahů se souvislými poznámkami a snímky. Nový test ověřuje chybějící tah, chybějící snímek i všech 40 bodů: celkem 13 testů prošlo. Běh `20261028T111157Z-73f6e9bd8751` používá nové sestavení, stejný model/reasoning a rozpočet 7 200 sekund / 1 000 akcí; jeho přejímka zatím čeká na skutečné výsledky.
14. Během 40tahového průchodu se podařilo samostatnou JavaFX aplikací na jiném Xvfb displeji reprodukovat chybu nástroje pro přetažení: přímý přesun ze zdroje na cíl vyvolal `DRAG_DETECTED`, ale nikoli puštění na cíl. S mezilehlým pohybem stejná aplikace přijala data (`DROPPED`). Oprava `desktop.py` přidává mezikrok a živá kontrola nyní obsahuje samostatnou JavaFX fixture; původní Tk kontrola toto rozlišení nepokrývala. Důkazy jsou v `target/ux-acceptance/drag-probe.log` a `drag-calibration/`; opravená fixture prošla. Její Java soubor byl zkontrolován sdíleným formatterem (shodné tokeny, idempotence). Rozehraný 40tahový pokus dál používá svůj původní neměnný image: jeho neúspěšné nativní přetahování nelze vydávat za potvrzenou chybu hry. Diagnostika neměnila jeho plochu, ovladač ani hráčské instrukce. Úplná kontrola nového image ještě čeká na uvolnění lokálního slotu.
15. Při integraci byl pracovní strom posunut na aktuální `main` `d0ad1feed73e8745143766701d63890418bc50cd`. Číslo R-0332 mezitím obsadil samostatný dokončený Congress úkol, proto tato dosud necommitnutá analýza dostala volné R-0334. Čekající ověření `20261028T111857Z-d70d4c763ceb` a `20261028T114909Z-9a0e6b6acbe2` byla ukončena před spuštěním a budou nahrazena kontrolami sjednocených zdrojů. Původní 40tahový image zůstává beze změn a dokládá průchod revizí `e7857466b3ef10ebc14bde127ffed3ae1b55d661`; pozdější změnu obrazovky Congress tím neověřujeme.
16. Průchod `20261028T111157Z-73f6e9bd8751` skončil úspěšně: relace 7 167,951 s, celkový běh 7 294,346 s. Souvislých 40 checkpointů doplňuje počáteční nultý bod; celkem 46 poznámek, 581 doručených akcí a 1 214 snímků. Ruční pozice `Akropia Turn 40 of 40` obsahuje `model.calendar.numberOfPlayedTurns = 40`, rok 1530; mezisave z 15. tahu sdílí `playthroughId = 858a4f54-93a7-4c48-a05e-8a86d3d77294`. Zpráva, kontrola a `audit.json` jsou v `akropia-ux-test/runs/20261028T111157Z-2db96ad5/`. Původní výpověď hráče zůstává oddělená od kontroly vývojáře.
17. Sjednocená modelová fáze `20261028T130033Z-2e0042747f3c` / `target/verification/run-4p2h7v2e` prošla: 349 úplných XML, 4 782 testů, nula selhání, chyb a přeskočení; celý běh 139,129 s. Následný UX check `20261028T130033Z-2e78df0e2b0c` selhal na kalibraci nové JavaFX fixture: snímek ukázal, že Openbox změnil její rozměry a polohu až po výpisu souřadnic. Fixture nyní chrání minimální velikost a měří souřadnice po zmapování okna; ovladač ani požadavek skutečného předání drag-and-drop dat se neoslabují. Selhání a snímky zůstávají v `runs/20261028T131551Z-c2ccf083`.

## Závěr prvního 40tahového průchodu

1. Výsledky jsou trvale uložené mimo pracovní strom v `$PRE_R0389_LOGS/ux/20261028T111157Z-2db96ad5/`; původní adresář v `akropia-ux-test/runs/` na ně odkazuje. `review.html` nabízí galerii po tazích, původní zprávu i kontrolu vývojáře; `audit.json` dokládá počet tahů, totožnost hry a kontrolní součty. Výsledky se nepublikují do Gitu.
2. Hráč založil New Amsterdam a Delft, měnil výrobu, stavěl, obchodoval, vybavoval jednotky a zkoumal mapu. Není to pouze čtyřicet stisknutí dalšího tahu.
3. Konkrétní UX podněty: u vozu dialog říká jen obecnou nedostupnost místo chybějící podmínky; automatický nákup 223 mušket zanechal tři zlaté a hráč nerozuměl předem jeho rozsahu; opakované zmenšování oken vyžadovalo maximalizaci. Příslušné snímky a rozlišení pozorování od hypotéz jsou v `review.md`. Nákup není prokázanou chybou ekonomických pravidel.
4. Náhled konkrétní produkce pomohl plánovat hospodářství. Alternativní příkazy pohybu a obchodní trasy umožnily pokračovat přes potíže s přetažením. Prokázaná chyba původního ovladače drag-and-drop se nepočítá jako chyba hry; oprava je součástí nástroje a má vlastní živé ověření.
5. Jde o jednu AI relaci na označené revizi. Neprokazuje dohratelnost celé kampaně ani zkušenost lidských hráčů. Congress byl na main mezitím změněn samostatným úkolem; jeho novou podobu tento starší průchod nehodnotí.

## Konečná lokální přejímka

1. Finální zdroje navazují na `main` `413d0126e9f7c6487650092640ad1ca9faa6e8ed`, včetně samostatně dokončené nové nápovědy. Přenosné testy modulu: 13; completion runner: 24; obojí bez selhání a přeskočení. JavaFX fixture prošla sdíleným formatterem, shodou tokenů a kontrolou idempotence.
2. Model/JUnit: `20261028T132606Z-211e19ae0499`, `target/verification/run-isdd3osf`; 349 úplných XML, 4 782 testů, nula selhání/chyb/přeskočení; 140,899 s včetně úklidu.
3. Cílené prostředí: `20261028T133024Z-bdd0ed235d2a`, `akropia-ux-test/runs/20261028T133024Z-5d294f65`; všech 10 živých X11/MCP/JavaFX kontrol prošlo za 119,464 s včetně sestavení a úklidu. Image `sha256:1659b786eec99082f57e12dffe33e4afa24fb3833b4545f9b7ab068f7b7b6d95`. Kontrola 1 758 vstupních hashů neodhalila rozdíl proti přijímaným zdrojům. JavaFX kontrola ověřuje skutečné předání dat cíli, nejen pohyb se stisknutým tlačítkem.
4. Pevný smoke/support výběr `scripts/ui-tests/smoke.json`: `20261028T133224Z-04f6943148e1`, `target/verification/run-ywb04jqt`; 12 úplných XML, 21 testů, nula selhání/chyb/přeskočení. Verifier eviduje 214,498 s; celý běh 214,655 s včetně úklidu, pod limitem 420 s. Interní název této fáze v souhrnu je `precommit`; příkaz je `verify.sh --smoke`. Modelové a smoke vstupy byly porovnány s přijímanými zdroji.
5. Dokumentační konzistence a strict MkDocs jsou součástí závěrečné přejímky. Hraní výše dokládá svou původní revizi; finální technické kontroly nástroje dokládají novější sjednocené zdroje. Tato lokální přejímka neověřuje vzdálené CI ani nově publikovanou distribuci.
