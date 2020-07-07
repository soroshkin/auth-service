package com.icl.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.icl.auth.security.Role;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@ToString
@NamedQuery(name = User.GET_BY_LOGIN, query = "SELECT u FROM User u WHERE login=:login")
public class User implements Serializable {
    public static final String GET_BY_LOGIN = "GET_USER_BY_LOGIN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotBlank
    @Length(min = 1)
    @Column(unique = true)
    private String login;

    @NotNull
    @NotBlank
    @Length(min = 3)
    @Pattern(regexp = "\\w+\\W+")
    @ToString.Exclude
    @JsonIgnore
    private String password;

    @NotNull
    @Past
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    @NotNull
    @Enumerated(value = EnumType.STRING)
    private Role role;

    public User(@NotNull @NotBlank @Length(min = 1) String login,
                @NotNull @NotBlank @Length(min = 1) String password,
                @NotNull @Past LocalDate dateOfBirth,
                @NotNull Role role) {
        this.login = login;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        this.role = role;
    }

    public boolean isNew() {
        return id == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(login, user.login) &&
                Objects.equals(password, user.password) &&
                Objects.equals(dateOfBirth, user.dateOfBirth) &&
                role == user.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, login, password, dateOfBirth, role);
    }
}
