package ru.yandex.practicum.s5_1_online_store.helpers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

@Getter
@AllArgsConstructor
public class PageableWrapper {

    private final Pageable pageable;
    private final boolean hasNext;

    public int getPageSize() {
        return pageable.getPageSize();
    }

    public int getPageNumber() {
        return pageable.getPageNumber();
    }

    public boolean hasPrevious() {
        return pageable.getPageNumber() > 0;
    }

    public boolean hasNext() {
        return pageable.getPageNumber() < getPageSize();
    }
}
