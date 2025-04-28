package com.ead.authuser.controllers;

import com.ead.authuser.dtos.UserDto;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.ead.authuser.specification.SpecificationTemplate;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserModel>> getAllUsers(
            SpecificationTemplate.UserSpec spec,
            @PageableDefault(page = 0, size = 10, sort = "userId", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<UserModel> userModelPage = userService.findAll(spec, pageable);
        if (!userModelPage.isEmpty()) {
            for (UserModel userModel : userModelPage.toList()) {
                userModel.add(linkTo(methodOn(UserController.class).getOneUser(userModel.getUserId())).withSelfRel());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(userModelPage);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getOneUser(@PathVariable("userId") UUID userId) {
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (!userModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(userModelOptional.get());
        }
    }

    /**
     * Exclui um usuário com base no ID fornecido.
     * <p>
     * Este endpoint HTTP DELETE localiza um usuário pelo seu ID e, se encontrado, o remove do banco de dados.
     * A exclusão é realizada por meio do {@code userService.deleteUser()}, que além de remover o usuário,
     * também publica um evento de exclusão no RabbitMQ. O evento publicado permite que outros serviços sejam notificados
     * da remoção.
     * <p>
     * Se o usuário não for encontrado, retorna uma resposta com status 404 (Not Found). Caso a exclusão seja
     * bem-sucedida, retorna uma resposta com status 200 (OK).
     *
     * @param userId o identificador único do usuário a ser excluído
     * @return uma {@link ResponseEntity} contendo a mensagem de sucesso ou erro apropriada
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable("userId") UUID userId) {
        log.debug("DELETE deleteUser userId received {}", userId);
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (!userModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } else {
            userService.deleteUser(userModelOptional.get());
            log.debug("DELETE deleteUser userId saved {}", userId);
            log.info("User deleted successfully userId {}", userId);
            return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully");
        }
    }

    /**
     * Atualiza os dados de um usuário com base no ID fornecido.
     * <p>
     * Este endpoint HTTP PUT localiza um usuário pelo seu ID e, se encontrado, atualiza os dados fornecidos
     * no corpo da requisição.
     * <p>
     * A atualização é realizada por meio do {@code userService.updateUser()}, que além de persistir as alterações
     * no banco de dados, também publica um evento de atualização no RabbitMQ.
     * <p>
     * Se o usuário não for encontrado, retorna uma resposta com status 404 (Not Found). Caso a atualização seja
     * bem-sucedida, retorna uma resposta com status 200 (OK) contendo o usuário atualizado.
     *
     * @param userId o identificador único do usuário a ser atualizado
     * @param userDto o objeto {@link UserDto} contendo os novos dados do usuário
     * @return uma {@link ResponseEntity} com o usuário atualizado ou uma mensagem de erro apropriada
     */
    @PutMapping("/{userId}")
    public ResponseEntity<Object> updateUser(
            @PathVariable("userId") UUID userId,
            @RequestBody @Validated(UserDto.UserView.UserPut.class)
            @JsonView(UserDto.UserView.UserPut.class) UserDto userDto) {
        log.debug("PUT updateUser userDto received {}", userDto.toString());
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (!userModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } else {
            var userModel = userModelOptional.get();
            userModel.setFullName(userDto.getFullName());
            userModel.setPhoneNumber(userDto.getPhoneNumber());
            userModel.setCpf(userDto.getCpf());
            userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
            userService.updateUser(userModel);
            log.debug("PUT updateUser userId saved {}", userModel.getUserId());
            log.info("User updated successfully userId {}", userModel.getUserId());
            return ResponseEntity.status(HttpStatus.OK).body(userModel);
        }
    }

    /**
     * Atualiza a senha de um usuário com base no ID fornecido.
     * <p>
     * Este endpoint HTTP PUT localiza um usuário pelo seu ID e, se encontrado, verifica se a senha antiga fornecida
     * corresponde à senha atual armazenada. Se a verificação for bem-sucedida, a nova senha é persistida no banco
     * de dados.
     * <p>
     * A operação é realizada por meio do {@code userService.updatePassword()}, que apenas atualiza a senha
     * no banco de dados, **sem publicar um evento** no RabbitMQ.
     * <p>
     * Se o usuário não for encontrado, retorna uma resposta com status 404 (Not Found). Se a senha antiga não corresponder,
     * retorna uma resposta com status 409 (Conflict). Em caso de sucesso, retorna status 200 (OK) com uma mensagem de confirmação.
     *
     * @param userId o identificador único do usuário cuja senha será atualizada
     * @param userDto o objeto {@link UserDto} contendo a senha antiga e a nova senha
     * @return uma {@link ResponseEntity} com mensagem de sucesso ou erro apropriado
     */
    @PutMapping("/{userId}/password")
    public ResponseEntity<Object> updatePassword(
            @PathVariable("userId") UUID userId,
            @RequestBody @Validated(UserDto.UserView.PasswordPut.class)
            @JsonView(UserDto.UserView.PasswordPut.class) UserDto userDto) {
        log.debug("PUT updatePassword userDto received {} ", userDto.toString());
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (!userModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } if (!userModelOptional.get().getPassword().equals(userDto.getOldPassword())) {
            log.warn("Mismatched old password userId {}", userDto.getUserId());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Mismatched old password");
        } else {
            var userModel = userModelOptional.get();
            userModel.setPassword(userDto.getPassword());
            userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
            userService.updatePassword(userModel);
            log.debug("PUT updatePassword userId saved {} ", userModel.getUserId());
            log.info("Password updated successfully userId {} ", userModel.getUserId());
            return ResponseEntity.status(HttpStatus.OK).body("Password updated successfully");
        }
    }

    /**
     * Atualiza a imagem de perfil de um usuário com base no ID fornecido.
     * <p>
     * Este endpoint HTTP PUT localiza um usuário pelo seu ID e, se encontrado, atualiza a URL da imagem
     * de perfil com base nos dados enviados no corpo da requisição.
     * <p>
     * A atualização é realizada por meio do {@code userService.updateUser()}, que além de persistir
     * as alterações no banco de dados, também publica um evento de atualização no RabbitMQ.
     * <p>
     * Se o usuário não for encontrado, retorna uma resposta com status 404 (Not Found). Caso a atualização seja
     * bem-sucedida, retorna uma resposta com status 200 (OK) contendo o usuário atualizado.
     *
     * @param userId o identificador único do usuário cuja imagem será atualizada
     * @param userDto o objeto {@link UserDto} contendo a nova URL da imagem do usuário
     * @return uma {@link ResponseEntity} com o usuário atualizado ou uma mensagem de erro apropriada
     */
    @PutMapping("/{userId}/image")
    public ResponseEntity<Object> updateImage(
            @PathVariable("userId") UUID userId,
            @RequestBody @Validated(UserDto.UserView.ImagePut.class)
            @JsonView(UserDto.UserView.ImagePut.class) UserDto userDto) {
        log.debug("PUT updateImage userDto received {} ", userDto.toString());
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (!userModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } else {
            var userModel = userModelOptional.get();
            userModel.setImageUrl(userDto.getImageUrl());
            userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
            userService.updateUser(userModel);
            log.debug("PUT updateImage userId saved {} ", userModel.getUserId());
            log.info("Image updated successfully userId {} ", userModel.getUserId());
            return ResponseEntity.status(HttpStatus.OK).body(userModel);
        }
    }

}