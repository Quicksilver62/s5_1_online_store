package ru.yandex.practicum.showcase_service.facades;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.showcase_service.dto.ItemDto;
import ru.yandex.practicum.showcase_service.model.CartModel;
import ru.yandex.practicum.showcase_service.services.CartService;
import ru.yandex.practicum.showcase_service.services.PaymentService;

@Service
@RequiredArgsConstructor
public class CartFacade {

    private final CartService cartService;
    private final PaymentService paymentService;

    public Mono<CartModel> getCartModel() {
        return cartService.getCartItems()
                .collectList()
                .flatMap(items -> {
                    double total = items.stream()
                            .mapToDouble(ItemDto::getPrice)
                            .sum();
                    return paymentService.isPurchaseAvailable(total)
                            .map(isAvailable -> new CartModel(items, total, isAvailable));
                });
    }

    public Mono<CartModel> handleItemActionAndGetModel(String action, Integer itemId) {
        return cartService.handleItemAction(action, itemId)
                .then(getCartModel());
    }
}
