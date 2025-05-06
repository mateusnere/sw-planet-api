package io.mateusnere.swplanetapi.common;

import io.mateusnere.swplanetapi.domain.Planet;

public class PlanetConstants {

    public static final Planet PLANET = new Planet("Tatooine-1", "Dry", "Desert");
    public static final Planet INVALID_PLANET = new Planet("", "", "");
    public static final Planet TATOOINE = new Planet(1L, "Tatooine", "Arid", "Desert");
    public static final Planet ALDERAAN = new Planet(2L, "Alderaan", "temperate", "grasslands, mountains");
    public static final Planet YAVIN_IV = new Planet(3L, "Yavin IV", "temperate, tropical", "jungle, rainforest");

}
