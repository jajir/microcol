# Grafické zdroje

Podrobný [popis grafiky a jejího použití v MicroCol](../../../docs/graphics.md) obsahuje katalog všech SVG, vazby na rastrové výstupy, souřadnice jednotlivých obrázků v atlasech, exportní postupy a odkazy na kód, který obrázky zobrazuje.

SVG zde slouží jako editovatelné podklady. Hra načítá hotové rastrové soubory z `src/main/resources/images`; běžný Maven build SVG automaticky neexportuje. Při úpravách se řiďte [současným zadáním vzhledu](../../../docs/graphics.md#soucasne-zadani-vzhledu).

Platí [závazná pravidla pro skutečné vektorové zdroje](../../../docs/graphics.md#zdrojovy-format-herni-grafiky). Současných 41 budov má samostatné zdroje `colony-buildings/TYPE.svg`; starší `buildings.svg` nepokrývá jejich současnou kresbu. [Postup exportu budov](../../../docs/graphics.md#vektorove-zdroje-budov-30-rijna-2026) zahrnuje kontrolu shody PNG. Soubory v `colony-buildings/reference/` jsou pouze historické rastrové předlohy.

Texty v SVG používají přibalený Playpen Sans Light. [Vložení fontu a export bez systémové instalace](../../../docs/graphics.md#pribaleny-font-v-svg-a-exportu) popisuje obnovu subsetů i export úvodního obrázku.
