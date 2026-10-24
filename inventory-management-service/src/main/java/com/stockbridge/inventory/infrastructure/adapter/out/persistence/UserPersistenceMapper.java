package com.stockbridge.inventory.infrastructure.adapter.out.persistence;

import com.stockbridge.inventory.domain.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {
    UserEntity toEntity(User user);

    default User toDomain(UserEntity e) {
        return User.restore(e.getId(), e.getFullName(), e.getEmail(),
                e.getPasswordHash(), e.getRole(), e.isActive());
    }
}
