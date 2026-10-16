package org.microcol.model;

/** The five manual categories and their founding fathers. */
public enum Founder {
    HERNAN_CORTES(Category.MILITARY), FRANCIS_DRAKE(Category.MILITARY),
    JOHN_PAUL_JONES(Category.MILITARY), PAUL_REVERE(Category.MILITARY),
    GEORGE_WASHINGTON(Category.MILITARY), SIMON_BOLIVAR(Category.POLITICAL),
    BENJAMIN_FRANKLIN(Category.POLITICAL), THOMAS_JEFFERSON(Category.POLITICAL),
    THOMAS_PAINE(Category.POLITICAL), POCAHONTAS(Category.POLITICAL),
    ADAM_SMITH(Category.TRADE), JAKOB_FUGGER(Category.TRADE),
    PETER_MINUIT(Category.TRADE), PETER_STUYVESANT(Category.TRADE),
    JAN_DE_WITT(Category.TRADE), JEAN_DE_BREBEUF(Category.RELIGIOUS),
    WILLIAM_BREWSTER(Category.RELIGIOUS),
    BARTOLOME_DE_LAS_CASAS(Category.RELIGIOUS),
    WILLIAM_PENN(Category.RELIGIOUS), JUAN_DE_SEPULVEDA(Category.RELIGIOUS),
    FRANCISCO_DE_CORONADO(Category.EXPLORATION),
    HENRY_HUDSON(Category.EXPLORATION), SIEUR_DE_LA_SALLE(Category.EXPLORATION),
    FERDINAND_MAGELLAN(Category.EXPLORATION),
    HERNANDO_DE_SOTO(Category.EXPLORATION);

    public enum Category {
        MILITARY, POLITICAL, TRADE, RELIGIOUS, EXPLORATION
    }

    private final Category category;

    Founder(final Category category) {
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }
}
