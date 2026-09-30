package com.bookingSystem.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class UserResponse
{
    private Integer id;
    private String userName;
    private String email;
}
