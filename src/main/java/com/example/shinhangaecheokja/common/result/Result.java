package com.example.shinhangaecheokja.common.result;

import java.util.function.Function;

public sealed interface Result<S, F> {

  record Success<S, F>(S value) implements Result<S, F> {}

  record Failure<S, F>(F error) implements Result<S, F> {}

  static <S, F> Result<S, F> success(S value) {
    return new Success<>(value);
  }

  static <S, F> Result<S, F> failure(F error) {
    return new Failure<>(error);
  }

  default <T> Result<T, F> map(Function<S, T> f) {
    if (this instanceof Success<S, F> success) {
      return Result.success(f.apply(success.value()));
    }
    // sealed interface has only Success/Failure, so this cast is safe
    return Result.failure(((Failure<S, F>) this).error());
  }

  default <T> Result<T, F> flatMap(Function<S, Result<T, F>> f) {
    if (this instanceof Success<S, F> success) {
      return f.apply(success.value());
    }
    // sealed interface has only Success/Failure, so this cast is safe
    return Result.failure(((Failure<S, F>) this).error());
  }

  default boolean isSuccess() {
    return this instanceof Success<?, ?>;
  }
}
