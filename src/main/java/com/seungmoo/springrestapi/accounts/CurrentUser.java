package com.seungmoo.springrestapi.accounts;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME) // Runtime 하는 때까지도 필요
// (expression = "account") --> getAccount()를 뜻하기 때문에  비인증 (principal이 그냥 "anonymousUser" 인 것)에 대한 대비가 필요!!
// "#this == 'anonymousUser' ? null : account" 현재 객체가 "anonymousUser" 문자열이면 그냥 Null을 리턴하고, 아니면 getAccount()하라.
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : account") // account라는 getter를 사용한다는 것이다!!
public @interface CurrentUser {
}
