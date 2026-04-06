package org.example.expert.domain.todo.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

public class TodoCustomRepositoryImpl implements TodoCustomRepository{

    private final JPAQueryFactory queryFactory;

    public TodoCustomRepositoryImpl(EntityManager em){
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Optional<TodoResponse> findTodoSummary(Long todoId){
        TodoResponse result = queryFactory
                .select(Projections.constructor(
                        TodoResponse.class,
                        todo.id,
                        todo.title,
                        todo.contents,
                        todo.weather,
                        Projections.constructor(
                                UserResponse.class,
                                user.id, user.email, user.nickname, user.userRole
                        ),
                        todo.createdAt,
                        todo.modifiedAt
                ))
                .from(todo)
                .leftJoin(todo.user, user) //유저 정보 조인
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<TodoSearchResponse> searchTodo(String title,
                                               String managerNickname,
                                               LocalDateTime startDate,
                                               LocalDateTime endDate,
                                               Pageable pageable
    ){
        List<TodoSearchResponse> result = queryFactory
                .select(Projections.constructor(
                        TodoSearchResponse.class,
                        todo.title,
                        manager.id.countDistinct(), //담당자 수(중복 제거)
                        comment.id.countDistinct() //댓글 수 (중복 제거)
                ))
                .from(todo)
                .leftJoin(todo.managers, manager) //담당자 테이블 조인
                .leftJoin(manager.user, user)
                .leftJoin(todo.comments, comment) //댓글 테이블 조인
                .where(
                        titleContains(title),//제목 부분 일치
                        managerNicknameContains(managerNickname),//닉네임 부분 일치
                        creationDateBetween(startDate, endDate)//생성일 범위 일치
                )
                .groupBy(todo.id)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        //전체 개수
        long total = queryFactory
                .select(todo.count())
                .from(todo)
                .where(
                        titleContains(title),
                        managerNicknameContains(managerNickname),
                        creationDateBetween(startDate, endDate)
                )
                .fetchOne();

        return new PageImpl<>(result, pageable, total);
    }

    private BooleanExpression titleContains(String title){
        return title != null ? todo.title.contains(title) : null;
    }

    private BooleanExpression managerNicknameContains(String managerNickname){
        return managerNickname != null ? user.nickname.contains(managerNickname) : null;
    }

    private BooleanExpression creationDateBetween(LocalDateTime startDate, LocalDateTime endDate){
        if(startDate == null && endDate == null) return null;
        if(startDate == null) return todo.createdAt.loe(endDate); //endDate 이전까지
        if(endDate == null) return todo.createdAt.goe(startDate); //startDate 이후부터
        return todo.createdAt.between(startDate, endDate);
    }
}
