# Zdroje budov

Každý `TYPE.svg` je současným editovatelným zdrojem odpovídajícího obrázku v `src/main/resources/images/colony-buildings/TYPE.png`. Názvy odpovídají `ConstructionType.name()`, včetně `BASIC_WAREHOUSE`.

[Vybraná varianta 8 — Lehký nadhled](../../../../docs/graphics.md#budovy-lehky-nadhled-produkce-30-rijna-2026) je od R-0361 překreslená jednoduchými vektorovými tvary. SVG mají pojmenované skupiny pro stavbu a profesní vybavení, jednolité výplně a mírně nepravidelný obrys. Pozadí je průhledné; žádné bitmapy, filtry, gradienty ani fonty se nevkládají. Upravovat přímo SVG a potom spustit [opakovatelný export](../../../../docs/graphics.md#vektorove-zdroje-budov-30-rijna-2026).

`tracing.json` je historický záznam jednorázového převodu předchozí podrobné kresby v R-0352. Nepopisuje současnou variantu 8 a nepoužívá se při jejím exportu. Původní SVG z této konverze zůstávají v historii Gitu.

`reference/` obsahuje původní tři rastrové atlasy, prompty (`generation.json`) a souřadnice výřezů (`sprites.json`) z 30. října 2026. Jde o zachovaný původ a vizuální předlohy, nikoli o současné produkční zdroje. Běžný export z nich nic nečte.
