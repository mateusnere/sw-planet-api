package io.mateusnere.swplanetapi.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.mateusnere.swplanetapi.common.PlanetConstants.INVALID_PLANET;
import static io.mateusnere.swplanetapi.common.PlanetConstants.PLANET;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

//@SpringBootTest(classes = PlanetService.class) -> esse import não será usado pois quando o utilizamos, é carregado muita coisa do spring que não precisamos para realizar os testes
// Com isso, também vamos remover as anotações @Autowired (pois ela é do spring) e o @MockitoBean também.
@ExtendWith(MockitoExtension.class)
public class PlanetServiceTest {

//    @Autowired
    @InjectMocks
    private PlanetService planetService;

//    @MockitoBean
    @Mock
    private PlanetRepository planetRepository;

    @Test
    public void createPlanet_WithValidData_ReturnsPlanet() {

        // Esse teste pode ser chamado AAA - Arrange Act Assert

        // Arrange
        when(planetRepository.save(PLANET)).thenReturn(PLANET);

        //Act
        // sut = System Under Test -> alvo do teste no momento
        Planet sut = planetService.create(PLANET);

        // Assert
        assertThat(sut).isEqualTo(PLANET);
    }

    @Test
    public void createPlanet_withInvalidData_ThrowsException() {
        when(planetRepository.save(INVALID_PLANET)).thenThrow(RuntimeException.class);
        assertThatThrownBy(() -> planetService.create(INVALID_PLANET)).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void getPlanet_ByExistingId_ReturnsPlanet() {
        when(planetRepository.findById(anyLong())).thenReturn(Optional.of(PLANET));
        Optional<Planet> optionalPlanet = planetService.getById(1L);
        assertThat(optionalPlanet).isEqualTo(Optional.of(PLANET));
    }

    @Test
    public void getPlanet_ByUnexistingId_ReturnsEmpty() {
        when(planetRepository.findById(anyLong())).thenReturn(Optional.empty());
        Optional<Planet> optionalPlanet = planetService.getById(1L);
        assertThat(optionalPlanet).isEmpty();
    }

    @Test
    public void getPlanet_ByExistingName_ReturnsPlanet() {
        when(planetRepository.findByName(PLANET.getName())).thenReturn(Optional.of(PLANET));
        Optional<Planet> optionalPlanet = planetService.getByName(PLANET.getName());
        assertThat(optionalPlanet).isEqualTo(Optional.of(PLANET));
    }

    @Test
    public void getPlanet_ByUnexistingName_ReturnsEmpty() {
        var unexistingNamePlanet = "unexisting name";
        when(planetRepository.findByName(unexistingNamePlanet)).thenReturn(Optional.empty());
        Optional<Planet> optionalPlanet = planetService.getByName(unexistingNamePlanet);
        assertThat(optionalPlanet).isEmpty();
    }

    @Test
    public void listPlanets_ReturnsAllPlanets() {
        List<Planet> planets = new ArrayList<>() {
            {
                add(PLANET);
            }
        };
        Example<Planet> example = QueryBuilder.makeQuery(new Planet(PLANET.getClimate(), PLANET.getTerrain()));
        when(planetRepository.findAll(example)).thenReturn(planets);
        List<Planet> sut = planetService.list(PLANET.getClimate(), PLANET.getTerrain());
        assertThat(sut).isNotEmpty();
        assertThat(sut).hasSize(1);
        assertThat(sut.getFirst()).isEqualTo(PLANET);
    }

    @Test
    public void listPlanets_ReturnsNoPlanet() {
        when(planetRepository.findAll(any())).thenReturn(Collections.emptyList());
        List<Planet> sut = planetService.list(PLANET.getClimate(), PLANET.getTerrain());
        assertThat(sut).isEmpty();
    }

//    Os dois exemplos abaixo são importantes para ver como se faz testes de métodos void
    @Test
    public void removePlanet_withExistingId_doesNotThrowAnyException() {
        when(planetRepository.findById(1L)).thenReturn(Optional.of(PLANET));
        assertThatCode(() -> planetService.remove(1L)).doesNotThrowAnyException();
        verify(planetRepository, times(1)).deleteById(1L);
    }

    @Test
    public void removePlanet_ByUnexistingId_throwsException() {
        assertThatThrownBy(() -> planetService.remove(99L)).isInstanceOf(EmptyResultDataAccessException.class);
    }
}
