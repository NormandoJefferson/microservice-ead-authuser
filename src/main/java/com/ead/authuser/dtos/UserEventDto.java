package com.ead.authuser.dtos;

import lombok.Data;

import java.util.UUID;

/**
 * Dto com informações que serão enviadas para o rabbitMQ.
 * Possui todos os atributos de UserModel menos o password e informações de data.
 * Utilizaremos strings nos tipos enum.
 * */
@Data
public class UserEventDto {

    private UUID userId;
    private String username;
    private String email;
    private String fullName;
    private String userStatus;
    private String userType;
    private String phoneNumber;
    private String cpf;
    private String imageUrl;
    private String actionType;

}