package com.stockbridge.inventory.infrastructure.adapter.out.persistence;

import com.stockbridge.inventory.application.port.out.UserRepositoryPort;
import com.stockbridge.inventory.domain.exception.EmailAlreadyRegisteredException;
import com.stockbridge.inventory.domain.model.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repository;
    private final UserPersistenceMapper mapper;

    public UserPersistenceAdapter(SpringDataUserRepository repository, UserPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        try {
            return mapper.toDomain(repository.save(mapper.toEntity(user)));
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyRegisteredException(user.getEmail());
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }
}
