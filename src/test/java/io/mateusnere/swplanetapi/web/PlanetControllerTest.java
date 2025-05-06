package io.mateusnere.swplanetapi.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mateusnere.swplanetapi.domain.Planet;
import io.mateusnere.swplanetapi.domain.PlanetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static io.mateusnere.swplanetapi.common.PlanetConstants.INVALID_PLANET;
import static io.mateusnere.swplanetapi.common.PlanetConstants.PLANET;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class PlanetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlanetService planetService;

    @Test
    public void createPlanet_withValidData_ReturnsCreated() throws Exception {
       when(planetService.create(PLANET)).thenReturn(PLANET);

        mockMvc.perform(
                post("/planets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(PLANET))
        ).andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value(PLANET.getName()));
    }

    @Test
    public void createPlanet_withInvalidData_ReturnsBadRequest() throws Exception {
        mockMvc.perform(
                post("/planets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(INVALID_PLANET))
        ).andExpect(status().isUnprocessableEntity());

        mockMvc.perform(
                post("/planets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Planet()))
        ).andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void createPlanet_withExistingPlanetData_ReturnsConflict() throws Exception {
        when(planetService.create(any())).thenThrow(DataIntegrityViolationException.class);

        mockMvc.perform(
                post("/planets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(PLANET))
        ).andExpect(status().isConflict());
    }

    @Test
    public void getPlanet_ByExistingId_ReturnsPlanet() throws Exception {
        when(planetService.getById(1L)).thenReturn(Optional.of(PLANET));

        mockMvc.perform(
                get("/planets/1")
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$").value(PLANET));
    }

    @Test
    public void getPlanet_ByUnexistingId_ReturnsNotFound() throws Exception {
        when(planetService.getById(any())).thenReturn(Optional.empty());

        mockMvc.perform(
                get("/planets/1")
        ).andExpect(status().isNotFound());
    }

    @Test
    public void getPlanet_ByExistingName_ReturnsPlanet() throws Exception {
        when(planetService.getByName(PLANET.getName())).thenReturn(Optional.of(PLANET));

        mockMvc.perform(
                get("/planets/name/Tatooine-1")
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$").value(PLANET));
    }

    @Test
    public void getPlanet_ByUnexistingName_ReturnsNotFound() throws Exception {
        when(planetService.getByName(any())).thenReturn(Optional.empty());

        mockMvc.perform(
                get("/planets/name/Marte")
        ).andExpect(status().isNotFound());
    }

    @Test
    public void getPlanets_ByNoFilter_ReturnsListOfPlanets() throws Exception {
        when(planetService.list(null, null)).thenReturn(List.of(PLANET));

        mockMvc.perform(
                get("/planets")
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$.[0]").value(PLANET));
    }

    @Test
    public void getPlanets_ByExistingClimateFilter_ReturnsListOfPlanets() throws Exception {
        when(planetService.list(PLANET.getClimate(), null)).thenReturn(List.of(PLANET));

        mockMvc.perform(
                get("/planets?climate=" + PLANET.getClimate())
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$.[0]").value(PLANET));
    }

    @Test
    public void getPlanets_ByExistingTerrainFilter_ReturnsListOfPlanets() throws Exception {
        when(planetService.list(null, PLANET.getTerrain())).thenReturn(List.of(PLANET));

        mockMvc.perform(
                get("/planets?terrain=" + PLANET.getTerrain())
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$.[0]").value(PLANET));
    }

    @Test
    public void getPlanets_ByExistingClimateAndTerrainFilter_ReturnsListOfPlanets() throws Exception {
        when(planetService.list(PLANET.getClimate(), PLANET.getTerrain())).thenReturn(List.of(PLANET));

        mockMvc.perform(
                get("/planets?climate=" + PLANET.getClimate() + "&terrain=" + PLANET.getTerrain())
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$.[0]").value(PLANET));
    }

    @Test
    public void getPlanets_ByUnexistingFilter_ReturnsEmptyList() throws Exception {
        when(planetService.list("unexisting", null)).thenReturn(List.of());
        when(planetService.list(null, "unexisting")).thenReturn(List.of());
        when(planetService.list("unexisting", "unexisting")).thenReturn(List.of());

        mockMvc.perform(
                get("/planets?climate=unexisting")
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(
                get("/planets?terrain=unexisting")
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(
                get("/planets?climate=unexisting&terrain=unexisting")
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void deletePlanet_ByExistingId_ReturnsNoContent() throws Exception {
        mockMvc.perform(
                delete("/planets/1")
        ).andExpect(status().isNoContent());
    }

    @Test
    public void deletePlanet_ByUnexistingId_ReturnsNotFound() throws Exception {
        doThrow(EmptyResultDataAccessException.class).when(planetService).remove(1L);

        mockMvc.perform(
                delete("/planets/1")
        ).andExpect(status().isNotFound());
    }
}
