package io.github.tuannh982.spring.boot.arch.router.rest.response;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GeneralResponse<T> {
    private ResponseStatus status;
    private T data;
}
