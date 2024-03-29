package com.seungmoo.springrestapi.accounts;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(unique = true)
    private String email;

    private String password;

    @ElementCollection(fetch = FetchType.EAGER) // 여러 개의 Enum을 가질 수 있음을 명시하기 위해 ElementCollection 선언
    @Enumerated(value = EnumType.STRING)
    private Set<AccountRole> roles;
}
