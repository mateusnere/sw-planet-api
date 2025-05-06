package io.mateusnere.swplanetapi;

import io.mateusnere.swplanetapi.domain.Planet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

import static io.mateusnere.swplanetapi.common.PlanetConstants.PLANET;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("it")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"/import_planets.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"/remove_planets.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class PlanetITWebTestClient {

    @Autowired
    WebTestClient webTestClient;

    /*
    * Exemplo de teste utilizando webTestClient (uma abordagem não bloqueante e uma linguagem mais fluente)
    * */
    @Test
    public void createPlanet_ReturnsCreated() {
        Planet sut = webTestClient.post().uri("/planets").bodyValue(PLANET)
                .exchange().expectStatus().isCreated().expectBody(Planet.class)
                .returnResult().getResponseBody();

        assertThat(Objects.requireNonNull(sut).getId()).isNotNull();
        assertThat(sut.getName()).isEqualTo(PLANET.getName());
    }
}
