package com.ead.authuser.services.impl;

import com.ead.authuser.clients.CourseClient;
import com.ead.authuser.dtos.UserEventDto;
import com.ead.authuser.enums.ActionType;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.publishers.UserEventPublisher;
import com.ead.authuser.repositories.UserRepository;
import com.ead.authuser.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    CourseClient courseClient;

    @Autowired
    UserEventPublisher userEventPublisher;

    @Override
    public List<UserModel> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Page<UserModel> findAll(Specification<UserModel> spec, Pageable pageable) {
        return userRepository.findAll(spec, pageable);
    }

    @Override
    public Optional<UserModel> findById(UUID userId) {
        return userRepository.findById(userId);
    }

    @Override
    public void delete(UserModel userModel) {
        userRepository.delete(userModel);
    }

    @Override
    public UserModel save(UserModel userModel) {
        return userRepository.save(userModel);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Salva um novo usuário no banco de dados e publica um evento no RabbitMQ.
     * <p>
     * Realiza a persistência do usuário fornecido no banco de dados e, em seguida, publica um evento
     * relacionado à criação do usuário no RabbitMQ. O evento é enviado como um {@link UserEventDto},
     * com a ação associada sendo {@link ActionType#CREATE}.
     * <p>
     * A operação é executada dentro de uma transação, garantindo que, caso ocorra algum erro durante o processo de
     * salvamento ou publicação do evento, a transação seja revertida.
     *
     * @param userModel o objeto {@link UserModel} contendo os dados do usuário a ser salvo
     * @return o objeto {@link UserModel} persistido, incluindo os dados gerados pelo banco (por exemplo, ID)
     */
    @Transactional
    @Override
    public UserModel saveUser(UserModel userModel) {
        userModel = save(userModel);
        userEventPublisher.publishUserEvent(userModel.convertToUserEventDto(), ActionType.CREATE);
        return userModel;
    }

    /**
     * Remove um usuário do banco de dados e publica um evento no RabbitMQ.
     * <p>
     * Realiza a exclusão do usuário fornecido do banco de dados e, em seguida, publica um evento
     * relacionado à exclusão do usuário no RabbitMQ. O evento é enviado como um {@link UserEventDto},
     * com a ação associada sendo {@link ActionType#DELETE}.
     * <p>
     * A operação é executada dentro de uma transação, garantindo que, caso ocorra algum erro durante o processo de
     * exclusão ou publicação do evento, a transação seja revertida.
     *
     * @param userModel o objeto {@link UserModel} representando o usuário a ser removido
     */
    @Transactional
    @Override
    public void deleteUser(UserModel userModel) {
        delete(userModel);
        userEventPublisher.publishUserEvent(userModel.convertToUserEventDto(), ActionType.DELETE);
    }

    /**
     * Atualiza os dados de um usuário no banco de dados e publica um evento no RabbitMQ.
     * <p>
     * Realiza a atualização dos dados do usuário fornecido no banco de dados e, em seguida, publica um evento
     * relacionado à atualização do usuário no RabbitMQ. O evento é enviado como um {@link UserEventDto},
     * com a ação associada sendo {@link ActionType#UPDATE}.
     * <p>
     * A operação é executada dentro de uma transação, garantindo que, caso ocorra algum erro durante o processo de
     * atualização ou publicação do evento, a transação seja revertida.
     *
     * @param userModel o objeto {@link UserModel} contendo os dados atualizados do usuário
     * @return o objeto {@link UserModel} atualizado e persistido no banco de dados
     */
    @Transactional
    @Override
    public UserModel updateUser(UserModel userModel) {
        userModel = save(userModel);
        userEventPublisher.publishUserEvent(userModel.convertToUserEventDto(), ActionType.UPDATE);
        return userModel;
    }

    /**
     * Atualiza a senha de um usuário no banco de dados.
     * <p>
     * Realiza a persistência da nova senha do usuário fornecido no banco de dados.
     * <p>
     * Diferentemente de outras operações, esta atualização não publica um evento no RabbitMQ.
     *
     * @param userModel o objeto {@link UserModel} contendo os dados atualizados de senha do usuário
     * @return o objeto {@link UserModel} com a senha atualizada e persistida no banco de dados
     */
    @Override
    public UserModel updatePassword(UserModel userModel) {
        return save(userModel);
    }

}