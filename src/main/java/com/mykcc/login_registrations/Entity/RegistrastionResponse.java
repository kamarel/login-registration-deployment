package com.mykcc.login_registrations.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrastionResponse {

    private boolean response;
    private String message;
}
