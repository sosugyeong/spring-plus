package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoCustomRepository {

    Optional<TodoResponse> findTodoSummary(Long todoId);

    Page<TodoSearchResponse> searchTodo(
            String title,
            String managerNickname,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );
}
