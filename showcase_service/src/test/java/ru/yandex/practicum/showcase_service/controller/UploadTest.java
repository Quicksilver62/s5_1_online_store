package ru.yandex.practicum.showcase_service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.showcase_service.config.TestSecurityConfig;
import ru.yandex.practicum.showcase_service.configuration.SecurityConfig;
import ru.yandex.practicum.showcase_service.controllers.UploadController;
import ru.yandex.practicum.showcase_service.facades.ItemFacade;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@Import({SecurityConfig.class, TestSecurityConfig.class})
@WebFluxTest(UploadController.class)
public class UploadTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ItemFacade itemFacade;

    private final String validNdJson = "{\"id\":1,\"title\":\"Test Item\",\"description\":\"Description\",\"imgPath\":\"/img.jpg\",\"price\":100.0,\"count\":10}\n";

    @Test
    @WithMockUser(roles = "ADMIN")
    void upload_ShouldReturnCreated_WhenAdminAndValidData() {
        when(itemFacade.saveAll(any())).thenReturn(Mono.empty());

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/upload")
                .contentType(MediaType.APPLICATION_NDJSON)
                .bodyValue(validNdJson)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    @WithMockUser(roles = "USER")
    void upload_ShouldReturnForbidden_WhenNotAdmin() {
        webTestClient.mutateWith(csrf())
                .post()
                .uri("/upload")
                .contentType(MediaType.APPLICATION_NDJSON)
                .bodyValue(validNdJson)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void upload_ShouldReturnUnauthorized_WhenAnonymous() {
        webTestClient.mutateWith(csrf())
                .post()
                .uri("/upload")
                .contentType(MediaType.APPLICATION_NDJSON)
                .bodyValue(validNdJson)
                .exchange()
                .expectStatus().isFound()
                .expectHeader().valueEquals("Location", "/login");
    }
}
