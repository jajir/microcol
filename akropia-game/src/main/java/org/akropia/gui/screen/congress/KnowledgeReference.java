package org.akropia.gui.screen.congress;

import org.akropia.model.Founder;
import org.akropia.i18n.I18n;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Historical illustrations and reading links; gameplay remains in the model.
 */
public enum KnowledgeReference {
    HERNAN_CORTES("treasury", "https://en.wikipedia.org/wiki/Athenian_Treasury",
            "https://delphi.culture.gr/the-treasury-of-the-athenians/"),
    FRANCIS_DRAKE("trireme", "https://en.wikipedia.org/wiki/Trireme",
            "https://www.theacropolismuseum.gr/en/relief-depicting-trireme-lenormant-relief"),
    JOHN_PAUL_JONES("kyrenia", "https://en.wikipedia.org/wiki/Kyrenia_ship",
            "https://nauticalarch.org/projects/kyrenia-shipwreck-excavation/"),
    PAUL_REVERE("helmet", "https://en.wikipedia.org/wiki/Corinthian_helmet",
            "https://www.metmuseum.org/art/collection/search/703054"),
    GEORGE_WASHINGTON("miltiades", "https://en.wikipedia.org/wiki/Miltiades",
            "https://2500years.culture.gov.gr/en/chapters/epigrafes/kranos-miltiadi"),
    SIMON_BOLIVAR("democracy",
            "https://en.wikipedia.org/wiki/Ancient_Agora_of_Athens",
            "https://atticinscriptions.com/inscription/AgoraXVI/73"),
    BENJAMIN_FRANKLIN("treaty", "https://en.wikipedia.org/wiki/Proxeny",
            "https://www.theacropolismuseum.gr/en/stele-decree-treaty-between-arcadia-achaia-elis-and-phleious"),
    THOMAS_JEFFERSON("water-clock", "https://en.wikipedia.org/wiki/Water_clock",
            "https://www.athens-agora.gr/project/index.php?lang_id=en&pid=5&view=evr"),
    THOMAS_PAINE("tribute", "https://en.wikipedia.org/wiki/Delian_League",
            "https://www.atticinscriptions.com/browse/byinscriptiontype/tribute-list/"),
    POCAHONTAS("citizenship",
            "https://en.wikipedia.org/wiki/Athenian_democracy",
            "https://www.atticinscriptions.com/inscription/IGII31/1218"),
    ADAM_SMITH("kiln",
            "https://fr.wikipedia.org/wiki/Plaquettes_de_Penteskouphia",
            "https://commons.wikimedia.org/wiki/File:Penteskouphia_tablet_-_pots_in_a_kiln_%28Berlin_Antikensammlung_F_893%29.jpg"),
    JAKOB_FUGGER("debt", "https://en.wikipedia.org/wiki/Seisachtheia",
            "https://www.ascsa.edu.gr/uploads/media/oa_ebooks/oa_hesperia_supplements/HS9.pdf"),
    PETER_MINUIT("boundary",
            "https://en.wikipedia.org/wiki/Ancient_Agora_of_Athens",
            "https://www.ascsa.edu.gr/uploads/media/oa_ebooks/oa_agora/Agora_XIX.pdf"),
    PETER_STUYVESANT("stamp", "https://en.wikipedia.org/wiki/Amphora",
            "https://www.metmuseum.org/art/collection/search/241973"),
    JAN_DE_WITT("kyrenia", "https://en.wikipedia.org/wiki/Kyrenia_ship",
            "https://nauticalarch.org/projects/kyrenia-shipwreck-excavation/"),
    JEAN_DE_BREBEUF("hymns", "https://en.wikipedia.org/wiki/Delphic_Hymns",
            "https://delphi.culture.gr/the-hymns-to-apollo-and-the-white-kylix/"),
    WILLIAM_BREWSTER("oath", "https://en.wikipedia.org/wiki/Cyrene,_Libya",
            "https://igcyr.unibo.it/igcyr011000"),
    BARTOLOME_DE_LAS_CASAS("freedom",
            "https://en.wikipedia.org/wiki/Slavery_in_ancient_Greece",
            "https://www.atticinscriptions.com/inscription/CIDV/534"),
    WILLIAM_PENN("torch", "https://en.wikipedia.org/wiki/Demeter",
            "https://www.ascsa.edu.gr/uploads/media/hesperia/148242.pdf"),
    JUAN_DE_SEPULVEDA("nike",
            "https://en.wikipedia.org/wiki/Winged_Victory_of_Samothrace",
            "https://www.louvre.fr/en/explore/the-palace/a-stairway-to-victory"),
    FRANCISCO_DE_CORONADO("wreath", "https://en.wikipedia.org/wiki/Wreath",
            "https://artmuseum.princeton.edu/art/collections/objects/33324"),
    HENRY_HUDSON("clay",
            "https://fr.wikipedia.org/wiki/Plaquettes_de_Penteskouphia",
            "https://search.smb.museum/object/obj-689220"),
    SIEUR_DE_LA_SALLE("walls", "https://en.wikipedia.org/wiki/Messene",
            "https://www.ancientmessene.gr/en/fortifications/"),
    FERDINAND_MAGELLAN("antikythera",
            "https://en.wikipedia.org/wiki/Antikythera_mechanism",
            "https://antikythera-mechanism.namuseum.gr/en/"),
    HERNANDO_DE_SOTO("omphalos", "https://en.wikipedia.org/wiki/Omphalos",
            "https://delphi.culture.gr/omfalos/");

    private final String artwork;
    private final String wikipedia;
    private final String source;

    KnowledgeReference(final String artwork, final String wikipedia,
            final String source) {
        this.artwork = artwork;
        this.wikipedia = wikipedia;
        this.source = source;
    }

    public static KnowledgeReference of(final Founder founder) {
        return valueOf(founder.name());
    }

    public String getSource() {
        return source;
    }

    public String getWikipedia() {
        return wikipedia;
    }

    public String getArtifact(final I18n i18n) {
        return i18n.get(CongressText.valueOf("artifact" + name()));
    }

    public String getHistory(final I18n i18n) {
        return i18n.get(CongressText.valueOf("history" + name()));
    }

    public ImageView createView(final double width) {
        final ImageView view = new ImageView(new Image(KnowledgeReference.class
                .getResource("/images/knowledge/" + artwork + ".png")
                .toExternalForm()));
        view.setFitWidth(width);
        view.setPreserveRatio(true);
        view.setId("knowledgeImage-" + name());
        return view;
    }
}
