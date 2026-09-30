package com.bookingSystem.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class ResourceResponse
{
    private Integer id;
    private String resourceName;
    private Double price;
}
