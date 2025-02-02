package com.hubbox.demo.mapper;

import org.mapstruct.MappingTarget;

public interface BaseMapper<C, U, E, R> {
    E toEntity(C c);

    R toResponse(E e);

    void updateEntityFromRequest(U u, @MappingTarget E e);
}
