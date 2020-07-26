package com.icl.auth.model;

import lombok.*;
import org.springframework.data.annotation.Id;

/**
 * Class represents blocked tokens after user logged off.
 * Database contains table with same name, it stores blocked tokens.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@ToString
public class BlockedToken {
    @Id
    private String id;
}
