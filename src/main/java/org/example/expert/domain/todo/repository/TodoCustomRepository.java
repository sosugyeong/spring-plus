package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.response.TodoResponse;

import java.util.Optional;

public interface TodoCustomRepository {

    Optional<TodoResponse> findTodoSummary(Long todoId);
}
