package com.ead.authuser.controllers;

import com.ead.authuser.dtos.UserDto;
import com.ead.authuser.enums.UserStatus;
import com.ead.authuser.enums.UserType;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/auth")
public class AuthenticationController{

    @Autowired
    UserService userService;

    /**
     * Registra um novo usuário no sistema.
     * <p>
     * Recebe os dados de um usuário (DTO) através de uma solicitação POST, valida as informações
     * fornecidas (como verificar se o nome de usuário e o email já estão em uso) e, em seguida, cria uma nova
     * instância de {@link UserModel} para salvar no banco de dados. Após a criação, o usuário é registrado com o
     * status {@link UserStatus#ACTIVE} e o tipo {@link UserType#STUDENT}. O evento de criação do usuário é
     * gerenciado pelo {@link UserService}, que salva um novo usuário no banco e publica o evento no rabbitMQ.
     * <p>
     * Caso o nome de usuário ou o email já estejam cadastrados, é retornado um erro com o status HTTP 409
     * (CONFLICT).
     *
     * @param userDto o objeto {@link UserDto} contendo as informações do usuário a ser registrado
     * @return uma resposta HTTP contendo o status da operação e o objeto {@link UserModel} do usuário salvo,
     *         com status HTTP 201 (CREATED) em caso de sucesso, ou status HTTP 409 (CONFLICT) em caso de
     *         nome de usuário ou email já em uso.
     */
    @PostMapping("/signup")
    public ResponseEntity<Object> registerUser(
            @RequestBody @Validated(UserDto.UserView.RegistrationPost.class)
            @JsonView(UserDto.UserView.RegistrationPost.class) UserDto userDto) {
        log.debug("POST registerUser userDto received {}", userDto.toString());
        if (userService.existsByUsername(userDto.getUsername())) {
            log.warn("Username {} is already taken.", userDto.getUsername());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Username is already taken.");
        }
        if (userService.existsByEmail(userDto.getEmail())) {
            log.warn("Email {} is already taken.", userDto.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Email is already taken.");
        }
        var userModel = new UserModel();
        BeanUtils.copyProperties(userDto, userModel);
        userModel.setUserStatus(UserStatus.ACTIVE);
        userModel.setUserType(UserType.STUDENT);
        userModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userService.saveUser(userModel);
        log.debug("POST registerUser userId saved {}", userModel.getUserId());
        log.info("User saved successfully userId {}", userModel.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(userModel);
    }

}