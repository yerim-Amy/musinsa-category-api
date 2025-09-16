package com.musinsa.category.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class TokenRequest {

    @NotBlank(message = "관리자 ID는 필수입니다")
    private String adminId;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
}