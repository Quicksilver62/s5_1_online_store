package ru.yandex.practicum.showcase_service.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.client.ApiClient;
import ru.yandex.practicum.client.api.DefaultApi;

@Configuration
public class ApiClientConfig {

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    @Bean
    public ApiClient apiClient() {
        return new ApiClient().setBasePath(paymentServiceUrl);
    }

    @Bean
    public DefaultApi paymentServiceApi(ApiClient apiClient) {
        return new DefaultApi(apiClient);
    }
}
