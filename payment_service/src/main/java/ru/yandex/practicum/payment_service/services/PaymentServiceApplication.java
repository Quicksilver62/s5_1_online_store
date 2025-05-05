package ru.yandex.practicum.payment_service.services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.server.OpenApiGeneratorApplication;

@SpringBootApplication
@Import(OpenApiGeneratorApplication.class)
public class PaymentServiceApplication {

    public static void main(String[] args) { SpringApplication.run(PaymentServiceApplication.class, args);}

}
