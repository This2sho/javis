package com.javis.learn_hub.support.repository;

import com.javis.learn_hub.support.domain.BaseEntity;
import com.javis.learn_hub.support.domain.CreatedOnlyEntity;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class InMemoryRepository<T> {

    protected final Map<Long, T> store = new HashMap<>();
    protected Long counter = 0L;

    public T save(T entity) {
        counter++;
        setField(entity, "id", counter);
        applyAuditFields(entity);
        store.put(counter, entity);
        return entity;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(fieldName + " 설정 실패", e);
        }
    }

    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new IllegalStateException("필드 " + fieldName + " 를 찾을 수 없습니다.");
    }

    private void applyAuditFields(T entity) {
        LocalDateTime now = LocalDateTime.now();

        if (entity instanceof CreatedOnlyEntity created) {
            setField(created, "createdAt", now);
        }

        if (entity instanceof BaseEntity base) {
            setField(base, "updatedAt", now);
        }
    }

    public void saveAll(Iterable<T> entities) {
        for (T entity : entities) {
            save(entity);
        }
    }

    public Optional<T> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public void clear() {
        store.clear();
    }

    public Optional<T> findOne(Predicate<T> predicate) {
        return store.values().stream()
                .filter(predicate)
                .findFirst();
    }

    public List<T> findAll(Predicate<T> predicate) {
        return store.values().stream()
                .filter(predicate)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
