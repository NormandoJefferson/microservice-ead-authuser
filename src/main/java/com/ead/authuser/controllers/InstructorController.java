package com.ead.authuser.controllers;

import com.ead.authuser.dtos.InstructorDto;
import com.ead.authuser.enums.ActionType;
import com.ead.authuser.enums.UserType;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/instructors")
public class InstructorController {

    @Autowired
    UserService userService;

    /**
     * Inscreve um usuário existente como instrutor.
     * <p>
     * Este endpoint recebe um objeto {@link InstructorDto}, contendo o ID de um usuário previamente cadastrado,
     * e atualiza seu tipo para {@link UserType#INSTRUCTOR}. Também define a data da última atualização para o momento atual
     * e persiste as alterações no banco de dados.
     * <p>
     * A operação de atualização aciona a publicação de um evento no RabbitMQ com a ação
     * {@link ActionType#UPDATE}, por meio do  {@link UserService#updateUser(UserModel)}.
     * <p>
     * Caso o usuário informado não seja encontrado, retorna {@code 404 Not Found}.
     *
     * @param instructorDto o DTO contendo o ID do usuário a ser promovido para instrutor
     * @return {@code 200 OK} com o objeto {@link UserModel} atualizado, ou {@code 404 Not Found} caso o usuário não exista
     */
    @PostMapping("/subscription")
    public ResponseEntity<Object> saveSubscriptionInstructor(@RequestBody @Valid InstructorDto instructorDto) {
        Optional<UserModel> userModelOptional = userService.findById(instructorDto.getUserId());
        if (!userModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } else {
            var userModel = userModelOptional.get();
            userModel.setUserType(UserType.INSTRUCTOR);
            userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
            userService.updateUser(userModel);
            return ResponseEntity.status(HttpStatus.OK).body(userModel);
        }
    }

}