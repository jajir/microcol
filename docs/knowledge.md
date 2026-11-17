# Poznání, agora a historické předlohy

## Herní pravidla

Dohoda a implementace z 17. listopadu 2026 nahrazují hráčské pojmy Kongres,
otcové zakladatelé a radnice systémem poznání a agory. Parametry 25 účinků
zůstávají zachované. Jednorázová odměna není opakovatelná; trvalé bonusy
platí po zbytek partie. Historické ilustrace jsou tematické předlohy, nikoli
tvrzení, že daná památka skutečně způsobovala herní efekt.

1. Agora je základní budova kolonie produkující sovy. Vyrobené sovy se sčítají
   do národního účtu od založení první kolonie. Místní podpora nezávislosti
   a její dosavadní vztah k sovám se nemění.
2. Obrazovku **Poznání** otevře hráč z lišty, menu nebo klávesou **K**.
   Nabídka má nejvýše jednu položku z každé z pěti kategorií. Je neměnná
   až do získání poznání; prázdná vyčerpaná kategorie už nic nenabízí.
3. Kliknutí na kartu pouze vybere nabídku. Potvrzení **Získat poznání**
   se zpřístupní, když jde o aktuální nabídku ve vlastním tahu a hráč má
   dost sov. Zavření nebo Escape nic nekupuje. Potvrzení ihned odečte cenu,
   zapíše poznání a uplatní účinek; na další tah se nečeká. Oznámení je
   dostupné v přehledu událostí právě probíhajícího tahu.
4. Cena je `ceil(100 × (počet získaných poznání + 1) × koeficient obtížnosti)`.
   Koeficienty od nejnižší obtížnosti jsou 0,75 / 0,90 / 1 / 1,15 / 1,30.
   Přebytek zůstává na účtu. Žádné poznání nevyžaduje jiné předchozí poznání.
   Další nákup potřebuje novou platnou nabídku a její vyšší cenu.
5. Výběr z neplatné nabídky nebo bez potřebných sov nezmění stav hry.
   Uložení a načtení zachová poznání, účet a případné čekající doručení lodi.
   Jednorázové odměny se načtením neopakují. Cíl volné hry je získat všech
   25 poznání; bodování dál používá dosavadní počet získaných bonusů.
6. Odemčené stavby stále vyžadují své běžné stavební předpoklady a suroviny.
   Mistrovské loďařství dodá jednu fregatu do domovského přístavu; po vyhlášení
   nezávislosti do bezpečné pobřežní kolonie, případně s odkladem doručení.
   Městské opevnění vyžaduje alespoň tři obyvatele a způsobilou kolonii.
7. Tlačítko nápovědy na obrazovce Poznání otevře přímo nové téma. Každý řádek
   má vlevo ilustraci, vpravo účinek, podmínky a pod nimi historický kontext.
   Hledání zahrnuje názvy poznání i památek. Odkazy otevře výchozí prohlížeč;
   nápověda nemění partii. Návrat vede zpět do hry.

## Katalog

Wikipedia někdy popisuje širší téma, protože jednotlivý nález nemá vlastní
článek. Druhý odkaz proto vede na muzeum, archeologickou publikaci nebo katalog
konkrétního předmětu. Kresby jsou autorské zjednodušené 2D interpretace;
nejde o přesné archeologické rekonstrukce ani reprodukce všech nápisů.

| Poznání | Bonus | Památka a další čtení |
| --- | --- | --- |
| Válečná pokladnice | Dobyté místní osady vždy vydají poklad s dvojnásobnou hodnotou. | Athénská pokladnice v Delfách. Athénská pokladnice uchovávala votivní dary a kořist věnovanou Apollónovi. Dochované a znovu sestavené průčelí připomíná propojení vítězství, bohatství a svatyně. [Wikipedia](https://en.wikipedia.org/wiki/Athenian_Treasury); [odborný zdroj](https://delphi.culture.gr/the-treasury-of-the-athenians/). |
| Taktika triér | Korzáři získají +50 % bojové síly v útoku i obraně. | Lenormantův reliéf. Mramorový fragment z athénské Akropole zobrazuje veslaře triéry. Je svědectvím organizace řeckého válečného loďstva; herní bonus pro korzáry je volná tematická vazba. [Wikipedia](https://en.wikipedia.org/wiki/Trireme); [odborný zdroj](https://www.theacropolismuseum.gr/en/relief-depicting-trireme-lenormant-relief). |
| Mistrovské loďařství | Jednorázově získáte jednu fregatu zdarma. | Loď z Kyrénie. Dochovaný trup řecké obchodní lodi z pozdního 4. století př. n. l. odhaluje stavbu starověkých plavidel. Předloha je obchodní loď, zatímco herní odměna zůstává válečnou lodí. [Wikipedia](https://en.wikipedia.org/wiki/Kyrenia_ship); [odborný zdroj](https://nauticalarch.org/projects/kyrenia-shipwreck-excavation/). |
| Občanská hotovost | Nechráněná kolonie při útoku vyzbrojí pracovníka 50 uloženými zbraněmi. | Korintská přilba. Bronzová korintská přilba chránila hlavu, nos i tváře řeckého bojovníka. Profil ukazuje charakteristický chránič nosu a lícnice. [Wikipedia](https://en.wikipedia.org/wiki/Corinthian_helmet); [odborný zdroj](https://www.metmuseum.org/art/collection/search/703054). |
| Válečné zásluhy | Každé způsobilé vojenské vítězství povýší vítěznou jednotku o jeden stupeň. | Miltiadova přilba. Bronzová přilba zasvěcená v Olympii nese Miltiadovo jméno. Je dochovaným dokladem vojenské dedikace; herní povyšování jednotek je tematická interpretace. [Wikipedia](https://en.wikipedia.org/wiki/Miltiades); [odborný zdroj](https://2500years.culture.gov.gr/en/chapters/epigrafes/kranos-miltiadi). |
| Svobodná obec | Podpora nezávislosti vzroste v každé kolonii o 20 procentních bodů. | Stéla se zákonem proti tyranii. Athénská stéla z roku 337/336 př. n. l. nese zákon proti tyranii. Reliéf představuje Demokratii korunující Démos, zosobněný lid. [Wikipedia](https://en.wikipedia.org/wiki/Ancient_Agora_of_Athens); [odborný zdroj](https://atticinscriptions.com/inscription/AgoraXVI/73). |
| Diplomatický protokol | Války mateřského města již nevynucují koloniální vztahy. Mírová jednání stojí polovinu. | Řecká spojenecká stéla. Řecké obce zveřejňovaly dohody na kamenných stélách. Předlohou je spojenecký dekret v Muzeu Akropole; zjednodušený výjev podání rukou symbolizuje dohodu. [Wikipedia](https://en.wikipedia.org/wiki/Proxeny); [odborný zdroj](https://www.theacropolismuseum.gr/en/stele-decree-treaty-between-arcadia-achaia-elis-and-phleious). |
| Veřejná rozprava | Pracovníci agory produkují o 50% více sov. | Vodní hodiny z agory. Hliněné vodní hodiny měřily čas vyhrazený řečníkům u athénského soudu. Odtékající voda umožňovala odměřit délku řeči. [Wikipedia](https://en.wikipedia.org/wiki/Water_clock); [odborný zdroj](https://www.athens-agora.gr/project/index.php?lang_id=en&pid=5&view=evr). |
| Veřejné účty | Produkce sov se zvýší o procento odpovídající aktuální dani. | Athénské seznamy tributů. Kamenné seznamy zaznamenávaly podíl odvodů spojeneckých obcí věnovaný Athéně. Dokládají správu příjmů athénského námořního spolku. [Wikipedia](https://en.wikipedia.org/wiki/Delian_League); [odborný zdroj](https://www.atticinscriptions.com/browse/byinscriptiontype/tribute-list/). |
| Občanské soužití | Uklidní současný hněv a poplach místních obyvatel; nový místní poplach roste poloviční rychlostí. | Občanský dekret. Dochované athénské dekrety zaznamenávají udělení občanství a poct konkrétním osobám. Ve hře představují symbol přijetí a spolupráce, nikoli historickou všeobecnou rovnost. [Wikipedia](https://en.wikipedia.org/wiki/Athenian_democracy); [odborný zdroj](https://www.atticinscriptions.com/inscription/IGII31/1218). |
| Velké řemeslné dílny | Umožňuje stavbu továren. | Tabulka s hrnčířskou pecí. Votivní tabulky z Penteskúfie u Korintu zachycují také práci hrnčířů a vypalování nádob. Scéna s pecí je zjednodušená interpretace těchto dochovaných vyobrazení. [Wikipedia](https://fr.wikipedia.org/wiki/Plaquettes_de_Penteskouphia); [odborný zdroj](https://commons.wikimedia.org/wiki/File:Penteskouphia_tablet_-_pots_in_a_kiln_%28Berlin_Antikensammlung_F_893%29.jpg). |
| Dluhové vyrovnání | Jednorázově zruší současné bojkoty a daňové dluhy. Pozdější bojkoty zůstávají možné. | Zástavní kámen — horos. Athénské zástavní kameny označovaly majetek zatížený závazkem. Nejde o doklad konkrétního zrušení dluhu; herní odpuštění nedoplatků je tematická interpretace. [Wikipedia](https://en.wikipedia.org/wiki/Seisachtheia); [odborný zdroj](https://www.ascsa.edu.gr/uploads/media/oa_ebooks/oa_hesperia_supplements/HS9.pdf). |
| Pozemkové dohody | Místní půda již nevyžaduje platbu. Předchozí nákupy se nevracejí. | Hraniční kámen agory. Hraniční kameny vymezovaly prostor athénské agory. Motiv pevně stanovené hranice zde symbolizuje dohodu o využití půdy, nikoli historické právo bezplatného záboru. [Wikipedia](https://en.wikipedia.org/wiki/Ancient_Agora_of_Athens); [odborný zdroj](https://www.ascsa.edu.gr/uploads/media/oa_ebooks/oa_agora/Agora_XIX.pdf). |
| Obchodní pečetě | Umožňuje stavbu celnice. | Rhodský otisk na amfoře. Rhodské amfory nesly razítka s údaji o výrobě a úřednících. Zvolený kruhový otisk používá růži, znak Rhodu. [Wikipedia](https://en.wikipedia.org/wiki/Amphora); [odborný zdroj](https://www.metmuseum.org/art/collection/search/241973). |
| Zámořské obchodní styky | Umožňuje obchod s cizími koloniemi a podrobné přehledy cizích mocností. | Loď z Kyrénie. Náklad a trup lodi z Kyrénie dokládají středomořský obchod. Plavby spojovaly přístavy a přinášely zboží i zprávy; herní přehled mocností je stylizace. [Wikipedia](https://en.wikipedia.org/wiki/Kyrenia_ship); [odborný zdroj](https://nauticalarch.org/projects/kyrenia-shipwreck-excavation/). |
| Posvátné učení | Všichni misionáři pracují jako odborníci. | Delfské hymny Apollónovi. Dva hymny vytesané do kamene v Delfách uchovávají řecký text i hudební značky. Jsou mimořádným dokladem starověké náboženské hudby. [Wikipedia](https://en.wikipedia.org/wiki/Delphic_Hymns); [odborný zdroj](https://delphi.culture.gr/the-hymns-to-apollo-and-the-white-kylix/). |
| Přistěhovalecká charta | Výběr ze tří přistěhovalců. Zločinci a smluvní sluhové opustí nabídku přistěhování. | Přísaha zakladatelů Kyrény. Dochovaná kyrénská stéla zaznamenává občanská práva Théřanů a text přísahy spojovaný se založením města. Nápis vznikl později než samotná zakladatelská výprava. [Wikipedia](https://en.wikipedia.org/wiki/Cyrene,_Libya); [odborný zdroj](https://igcyr.unibo.it/igcyr011000). |
| Charta svobodných obyvatel | Současní místní konvertité se jednorázově stanou svobodnými kolonisty. | Delfský propouštěcí nápis. Nápisy v Delfách zaznamenávají propouštění zotročených lidí prostřednictvím formálního prodeje bohu Apollónovi. Historická svoboda a občanství nebyly totožné postavení. [Wikipedia](https://en.wikipedia.org/wiki/Slavery_in_ancient_Greece); [odborný zdroj](https://www.atticinscriptions.com/inscription/CIDV/534). |
| Chrámové obřady | Všechny kolonie produkují o 50 % více pochodní. | Votivní postava s pochodní. Korintská terakotová postava nese pochodeň a sele, atributy spojené s kultem Démétry a Persefony. Identifikace postavy jako bohyně nebo účastnice kultu není jistá. [Wikipedia](https://en.wikipedia.org/wiki/Demeter); [odborný zdroj](https://www.ascsa.edu.gr/uploads/media/hesperia/148242.pdf). |
| Prestiž vítězné svatyně | Zdvojnásobí šanci na obrácení místních obyvatel po odpovídajícím vítězství. | Niké ze Samothráky. Helénistická socha bohyně vítězství stojí na podstavci ve tvaru lodní přídě. Dochovala se bez hlavy a paží; bonus ke konverzi je herní symbolika. [Wikipedia](https://en.wikipedia.org/wiki/Winged_Victory_of_Samothrace); [odborný zdroj](https://www.louvre.fr/en/explore/the-palace/a-stairway-to-victory). |
| Soupis obcí | Jednorázově odhalí existující kolonie a jejich okolí. | Řecký zlatý věnec. Řecké zlaté věnce napodobují rostlinné větvičky a listy. Dochované exempláře připomínají pocty a společenské vazby; propojení s herním soupisem obcí je volná symbolická asociace. [Wikipedia](https://en.wikipedia.org/wiki/Wreath); [odborný zdroj](https://artmuseum.princeton.edu/art/collections/objects/33324). |
| Dobývání jílu | Zdvojnásobí produkci hlíny. | Korintská tabulka s těžbou hlíny. Tabulka F 871 z Penteskúfie patří k dochovaným řeckým vyobrazením těžby hrnčířské hlíny. Kresba převádí pracovní scénu do jednoduché herní podoby. [Wikipedia](https://fr.wikipedia.org/wiki/Plaquettes_de_Penteskouphia); [odborný zdroj](https://search.smb.museum/object/obj-689220). |
| Městské opevnění | Způsobilým koloniím s alespoň třemi obyvateli zajistí palisádu. | Hradby Messény. Messéna uchovává rozsáhlé kamenné opevnění s věžemi a branami. Pro herní palisádu slouží jako symbol organizované obrany, nikoli jako shodný stavební materiál. [Wikipedia](https://en.wikipedia.org/wiki/Messene); [odborný zdroj](https://www.ancientmessene.gr/en/fortifications/). |
| Hvězdné výpočty | Lodě získají +1 pohyb. Nové plavby do domovského přístavu ze západního okraje trvají polovinu tahů, zaokrouhlenou nahoru. | Mechanismus z Antikythéry. Dochované bronzové soukolí počítalo astronomické a kalendářní cykly. Nešlo o doložený lodní navigační přístroj; bonus k plavbě vyjadřuje obecný přínos astronomických znalostí. [Wikipedia](https://en.wikipedia.org/wiki/Antikythera_mechanism); [odborný zdroj](https://antikythera-mechanism.namuseum.gr/en/). |
| Obezřetný průzkum | Každá jednotka vidí alespoň dvě pole. Průzkum ztracených měst nemá škodlivé výsledky. | Delfský omfalos. Vyřezávaný kámen představuje mytický pupek světa a souvisí s Apollónovou věštírnou v Delfách. Ve hře symbolizuje předvídavost; ochrana průzkumníků je fikce. [Wikipedia](https://en.wikipedia.org/wiki/Omphalos); [odborný zdroj](https://delphi.culture.gr/omfalos/). |

## Vybrané motivy

- Antikythéra A, těžba jílu A, triéra A, zlatý věnec A, rhodská pečeť C.
- Pokladnice A, korintská přilba B, stéla demokracie C, postava s pochodní B,
  vodní hodiny B.
- Hrnčířská pec B, Miltiadova přilba A, seznam tributů B, hraniční kámen A,
  dluhový kámen B, přísaha osadníků A, občanský dekret A, delfské hymny B,
  propuštění na svobodu B, Niké A, loď z Kyrénie A, delfský omfalos A,
  hradby Messény A.

Písmena zachycují uživatelův výběr motivů z konceptů. Produkční vektory jsou
nové zjednodušené interpretace těchto motivů podle společného grafického
kontraktu; vizuální shoda s původními rastrovými listy není automaticky
prokázaná technickým exportem.

## Implementace a grafika

1. `Player.acquireKnowledge` vlastní validaci a okamžité přiznání účinku.
   UI a encyklopedie používají společné lokalizované názvy a popisy.
   `KnowledgeReference` obsahuje pouze vazby na ilustrace, historické popisy
   a odkazy; nevlastní herní pravidla.
2. Interní identifikátory `Founder`, `Congress`, `TOWN_HALL`, CSS ID a stabilní
   čísla UC/TC jsou zachované. Nejde o hráčské názvy. Dosavadní plánovaná volba
   používaná AI a připravenými scénáři je v modelu nadále zpracována; nové ruční
   nákupy používají okamžitou operaci. Tím se nemění parametry stávajících bonusů.
3. 24 skutečných SVG v `akropia-game/src/graphics/knowledge/` pokrývá 25 řádků;
   loď z Kyrénie ilustruje loďařství i zámořské styky. Každý zdroj má
   240 × 180 logických px, černé kontury 2 px a ploché jednobarevné výplně.
   Bez bitmap ve SVG, perspektivy, stínování a textur. Výběry A/B/C jsou
   převedené do tohoto jednotného stylu; nevybraná spojenecká stéla doplňuje
   diplomatický bonus. Zlatý věnec ilustruje společenské vazby a soupis obcí;
   Miltiadova přilba vojenské zásluhy. Vazby jsou symbolické.
4. Export: `python3 scripts/graphics/export-graphics.py knowledge --install`.
   Kontrola shody: stejný příkaz s `--check`. PNG 480 × 360 jsou odvozené
   exporty pro displeje s vyšší hustotou. Herní nápověda je zobrazuje v šířce
   240 logických px; karty přizpůsobují velikost dostupnému místu.
5. Ověření nákupu, bonusů a nápovědy je vedeno v
   [aktuální funkční evidenci](functional-tests.md#knowledge-2026-11-17).
   Starší výsledky zůstávají historickou evidencí původních pravidel.
