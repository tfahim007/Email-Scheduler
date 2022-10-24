package com.example.timer.payload;

import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Setter
public class EmailRequest {

    @NotNull
    private String email;

    @NotNull
    private String subject;

    @NotNull
    private String body;

    @NotNull
    private LocalDateTime dateTime;

    @NotNull
    private ZoneId timeZone;



}
