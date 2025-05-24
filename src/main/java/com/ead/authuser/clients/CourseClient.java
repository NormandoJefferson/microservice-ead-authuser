package com.ead.authuser.clients;

import com.ead.authuser.dtos.CourseDto;
import com.ead.authuser.dtos.ResponsePageDto;
import com.ead.authuser.services.UtilsService;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Componente responsável por consumir o serviço de cursos via API externa.
 *
 * <p>
 * Essa classe realiza requisições HTTP para buscar cursos associados a um usuário específico,
 * utilizando o {@link RestTemplate}. O endereço da API é configurado via propriedade
 * externa <code>ead.api.url.course</code>.
 * </p>
 *
 * <p>
 * O {@link #getAllCoursesByUser(UUID, Pageable)} utiliza a biblioteca Resilience4j
 * com a anotação {@link Retry} para aplicar tentativas automáticas em caso de falha.
 * </p>
 *
 * <p>
 * A classe é anotada com {@link Component}, permitindo que seja gerenciada pelo Spring, e
 * com {@link Log4j2} para registro de logs.
 * </p>
 */
@Log4j2
@Component
public class CourseClient {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    UtilsService utilsService;

    @Value("${ead.api.url.course}")
    String REQUEST_URL_COURSE;

    /**
     * Busca todos os cursos associados a um determinado usuário.
     *
     * <p>
     * Realiza uma chamada GET à API externa de cursos, montando a URL com base
     * no identificador do usuário e nos parâmetros de paginação fornecidos. A resposta
     * é convertida em uma {@link Page} de {@link CourseDto}.
     * </p>
     *
     * <p>
     * Em caso de falha na comunicação com o serviço externo, será aplicada a política de retry
     * definida com o nome <code>retryInstance</code>. Se todas as tentativas falharem,
     * o fallback {@link #retryfallback(UUID, Pageable, Throwable)} será acionado,
     * retornando uma página vazia como resposta padrão.
     * </p>
     *
     * @param userId identificador único do usuário
     * @param pageable objeto contendo os parâmetros de paginação
     * @return uma página de cursos ({@link Page<CourseDto>}) associados ao usuário
     */
    @Retry(name = "retryInstance", fallbackMethod = "retryfallback")
    public Page<CourseDto> getAllCoursesByUser(UUID userId, Pageable pageable) {
        List<CourseDto> searchResult = null;
        String url = REQUEST_URL_COURSE + utilsService.creatUrlGetAllCoursesByUser(userId, pageable);
        log.info("Request URL: {} ", url);
        try {
            ParameterizedTypeReference<ResponsePageDto<CourseDto>> responseType =
                    new ParameterizedTypeReference<ResponsePageDto<CourseDto>>() {};
            ResponseEntity<ResponsePageDto<CourseDto>> result =
                    restTemplate.exchange(url, HttpMethod.GET, null, responseType);
            searchResult = result.getBody().getContent();
            log.debug("Response Number of Elements: {} ", searchResult.size());
        } catch (HttpStatusCodeException e) {
            log.error("Error request /courses {} ", e);
        }
        log.info("Ending request /courses userId {} ", userId);
        return new PageImpl<>(searchResult);
    }

    /**
     * Função de fallback utilizado quando a tentativa de buscar cursos para um usuário falha,
     * mesmo após as tentativas definidas pela política de retry.
     *
     * <p>
     * Será invocado automaticamente pelo mecanismo de resiliência
     * (como o Resilience4j) quando todas as tentativas da operação principal falharem.
     * Ele registra o erro ocorrido e retorna uma página vazia de cursos como resposta padrão.
     * </p>
     *
     * <p><strong>Observação:</strong> Retornamos uma pagina vazia para manter a consistência com
     * o retorno do {@code getAllCoursesByUser}, que também retorna um {@link Page} de {@link CourseDto}.</p>
     *
     * @param userId o identificador único do usuário para o qual os cursos seriam buscados
     * @param pageable os parâmetros de paginação da requisição original
     * @param t a exceção que causou a falha na execução da função principal
     * @return uma {@link Page} vazia de {@link CourseDto}, indicando falha na obtenção dos dados
     */
    public Page<CourseDto> retryfallback(UUID userId, Pageable pageable, Throwable t) {
        log.error("Inside retry retryfallback, cause: {} ", t.toString());
        List<CourseDto> searchResult = new ArrayList<>();
        return new PageImpl<>(searchResult);
    }

}