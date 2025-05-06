package io.mateusnere.swplanetapi.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Example;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static io.mateusnere.swplanetapi.common.PlanetConstants.PLANET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
public class PlanetRepositoryTest {

    @Autowired
    private PlanetRepository planetRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @AfterEach
    public void clearId() {
        PLANET.setId(null);
    }

    @Test
    public void createPlanet_withValidData_ReturnsPlanet() {
        Planet planet = planetRepository.save(PLANET);
        Planet sut = testEntityManager.find(Planet.class, planet.getId());
        assertThat(sut).isNotNull();
        assertThat(sut.getName()).isEqualTo(PLANET.getName());
        assertThat(sut.getClimate()).isEqualTo(PLANET.getClimate());
        assertThat(sut.getTerrain()).isEqualTo(PLANET.getTerrain());
    }

    private static Stream<Arguments> providesInvalidPlanets() {
        return Stream.of(
                Arguments.of(new Planet(null, "climate", "terrain")),
                Arguments.of(new Planet("name", null, "terrain")),
                Arguments.of(new Planet("name", "climate", null)),
                Arguments.of(new Planet(null, null, "terrain")),
                Arguments.of(new Planet(null, "climate", null)),
                Arguments.of(new Planet("name", null, null)),
                Arguments.of(new Planet(null, null, null)),
                Arguments.of(new Planet("", "climate", "terrain")),
                Arguments.of(new Planet("name", "", "terrain")),
                Arguments.of(new Planet("name", "climate", "")),
                Arguments.of(new Planet("", "", "terrain")),
                Arguments.of(new Planet("", "climate", "")),
                Arguments.of(new Planet("name", "", "")),
                Arguments.of(new Planet("", "", ""))
        );
    }

    @ParameterizedTest
    @MethodSource("providesInvalidPlanets")
    public void createPlanet_withInvalidData_ThrowsException(Planet planet) {
        assertThatThrownBy(() -> planetRepository.save(planet)).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void createPlanet_withExistingName_ThrowsException() {
        Planet planet = testEntityManager.persistFlushFind(PLANET);
        // O detach serve para o hibernate parar de gerenciar o objeto e assim eu poder retirar o id para tentar inserir de novo e gerar a exceção esperada
        testEntityManager.detach(planet);
        planet.setId(null);

        assertThatThrownBy(() -> planetRepository.save(planet)).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void getPlanet_ByExistingId_ReturnsPlanet() {
        Planet planet = testEntityManager.persistFlushFind(PLANET);

        Optional<Planet> sut = planetRepository.findById(planet.getId());
        assertThat(sut).isNotEmpty();
        assertThat(sut.get()).isEqualTo(planet);
    }

    @Test
    public void getPlanet_ByUnexistingId_ReturnsEmpty() {
        Optional<Planet> sut = planetRepository.findById(1L);
        assertThat(sut).isEmpty();
    }

    @Test
    public void getPlanet_ByExistingName_ReturnsPlanet() {
        Planet planet = testEntityManager.persistFlushFind(PLANET);

        Optional<Planet> sut = planetRepository.findByName(planet.getName());
        assertThat(sut).isNotEmpty();
        assertThat(sut.get()).isEqualTo(planet);
    }

    @Test
    public void getPlanet_ByUnexistingName_ReturnsEmpty() {
        Optional<Planet> sut = planetRepository.findByName("Marte");
        assertThat(sut).isEmpty();
    }

    @Sql(scripts = "/import_planets.sql")
    @Test
    public void getPlanets_ReturnsListOfPlanets() {

        Example<Planet> query = QueryBuilder.makeQuery(new Planet(null, null));
        Example<Planet> queryAlderaan = QueryBuilder.makeQuery(new Planet("temperate", null));
        Example<Planet> queryTatooine = QueryBuilder.makeQuery(new Planet(null, "desert"));
        List<Planet> sut = (List<Planet>) planetRepository.findAll(query);
        List<Planet> sutAlderaan = (List<Planet>) planetRepository.findAll(queryAlderaan);
        List<Planet> sutTatooine = (List<Planet>) planetRepository.findAll(queryTatooine);

        assertThat(sut).isNotEmpty();
        assertThat(sut).hasSize(3);
        assertThat(sutAlderaan).isNotEmpty();
        assertThat(sutAlderaan).hasSize(1);
        assertThat(sutAlderaan.getFirst().getName()).isEqualTo("Alderaan");
        assertThat(sutTatooine).isNotEmpty();
        assertThat(sutTatooine).hasSize(1);
        assertThat(sutTatooine.getFirst().getName()).isEqualTo("Tatooine");
    }

    @Test
    public void getPlanets_ByUnexistingFilter_ReturnsEmptyList() {
        Example<Planet> query = QueryBuilder.makeQuery(new Planet("climate", "terrain"));
        List<Planet> sut = (List<Planet>) planetRepository.findAll(query);

        assertThat(sut).isEmpty();
    }

    @Sql(scripts = "/import_planets.sql")
    @Test
    public void deletePlanet_ByExistingId_RemovePlanetFromDatabase() {
        Example<Planet> query = QueryBuilder.makeQuery(new Planet("arid", "desert"));
        Example<Planet> queryRetunAll = QueryBuilder.makeQuery(new Planet());

        planetRepository.deleteById(1L);
        List<Planet> sut = (List<Planet>) planetRepository.findAll(query);
        List<Planet> sutAll = (List<Planet>) planetRepository.findAll(queryRetunAll);

        assertThat(sut).isEmpty();
        assertThat(sutAll).hasSize(2);
    }
}
