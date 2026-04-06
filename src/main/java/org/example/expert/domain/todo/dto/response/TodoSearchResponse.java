package org.example.expert.domain.todo.dto.response;

import lombok.Getter;

@Getter
public class TodoSearchResponse {
    private String title;
    private Long managerCount; //담당자 수
    private Long commentCount; //댓글 수

    public TodoSearchResponse(String title, Long managerCount, Long commentCount){
        this.title = title;
        this.managerCount = managerCount;
        this.commentCount = commentCount;
    }
}
