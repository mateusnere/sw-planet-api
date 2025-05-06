package io.mateusnere.swplanetapi;

import io.mateusnere.swplanetapi.domain.Planet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Objects;

import static io.mateusnere.swplanetapi.common.PlanetConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("it")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"/import_planets.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"/remove_planets.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class PlanetIT {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    public void createPlanet_ReturnsCreated() {
        ResponseEntity<Planet> sut = restTemplate.postForEntity("/planets", PLANET, Planet.class);
        assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(sut.getBody()).getId()).isNotNull();
        assertThat(sut.getBody().getName()).isEqualTo(PLANET.getName());
        assertThat(sut.getBody().getTerrain()).isEqualTo(PLANET.getTerrain());
        assertThat(sut.getBody().getClimate()).isEqualTo(PLANET.getClimate());
    }

    @Test
    public void getPlanet_ReturnsPlanet() {
        ResponseEntity<Planet> sut = restTemplate.getForEntity("/planets/1", Planet.class);
        assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sut.getBody()).isEqualTo(TATOOINE);
    }

    @Test
    public void getPlanet_ByName_ReturnsPlanet() {
        ResponseEntity<Planet> sut = restTemplate.getForEntity("/planets/name/Alderaan", Planet.class);
        assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sut.getBody()).isEqualTo(ALDERAAN);
    }

    @Test
    public void getPlanets_WithNoFilter_ReturnsListOfPlanets() {
        ResponseEntity<List<Planet>> sut = restTemplate.exchange(
                "/planets",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Planet>>() {
                }
        );

        assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sut.getBody()).isNotEmpty();
        assertThat(sut.getBody()).hasSize(3);
        assertThat(sut.getBody().getFirst()).isEqualTo(TATOOINE);
        assertThat(sut.getBody().getLast()).isEqualTo(YAVIN_IV);
    }

    /*
    * Esse teste também poderia ser feito com a seguinte chamada:
    * restTemplate.getForEntity("/planets?climate=Arid", Planet[].class);
    * */
    @Test
    public void getPlanets_ByClimate_ReturnsListOfPlanets() {
        ResponseEntity<List<Planet>> sut = restTemplate.exchange(
                "/planets?climate=Arid",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Planet>>() {
                }
        );

        assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sut.getBody()).isNotEmpty();
        assertThat(sut.getBody().getFirst()).isEqualTo(TATOOINE);
    }

    @Test
    public void getPlanets_ByTerrain_ReturnsListOfPlanets() {
        ResponseEntity<List<Planet>> sut = restTemplate.exchange(
                "/planets?terrain=grasslands, mountains",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Planet>>() {
                }
        );

        assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sut.getBody()).isNotEmpty();
        assertThat(sut.getBody().getFirst()).isEqualTo(ALDERAAN);
    }

    @Test
    public void getPlanets_ByClimateAndTerrain_ReturnsListOfPlanets() {
        ResponseEntity<List<Planet>> sut = restTemplate.exchange(
                "/planets?climate=temperate, tropical&terrain=jungle, rainforest",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Planet>>() {
                }
        );

        assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sut.getBody()).isNotEmpty();
        assertThat(sut.getBody()).hasSize(1);
        assertThat(sut.getBody().getFirst()).isEqualTo(YAVIN_IV);
    }

    @Test
    public void deletePlanet_ReturnsNoContent(){
        ResponseEntity<Void> sut = restTemplate.exchange(
                "/planets/1",
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(sut.getBody()).isNull();
    }
}
