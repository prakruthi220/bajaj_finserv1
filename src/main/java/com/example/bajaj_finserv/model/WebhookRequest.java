package com.example.bajaj_finserv.model;

import lombok.Data;

@Data
public class WebhookRequest {
    private String name;
    private String regNo;
    private String email;
}