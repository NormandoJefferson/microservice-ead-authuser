package com.ead.authuser.configs;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Classe de configuração responsável por fornecer uma instância de {@link RestTemplate}
 * com suporte a balanceamento de carga e timeout.
 *
 * <p>
 * A anotação {@link LoadBalanced} permite que o {@link RestTemplate} resolva nomes de serviço
 * registrados em um serviço de descoberta (como o Eureka), possibilitando chamadas HTTP
 * usando o nome lógico dos serviços.
 * </p>
 *
 * <p>
 * A instância criada também é configurada com timeouts de conexão e leitura, com o objetivo de evitar
 * chamadas bloqueantes prolongadas em casos de lentidão ou indisponibilidade dos serviços externos.
 * </p>
 *
 * <p>
 * O bean configurado pode ser injetado em outras partes da aplicação para facilitar integrações HTTP
 * de forma resiliente e escalável.
 * </p>
 */
@Configuration
public class RestTemplateConfig {

    static final int TIMEOUT = 5000;

    /**
     * Cria e registra um bean {@link RestTemplate} com suporte a balanceamento de carga e timeout.
     *
     * <p>
     * O {@code RestTemplate} é configurado com um tempo máximo de 5 segundos tanto para
     * a conexão quanto para a leitura da resposta. Isso ajuda a evitar travamentos caso o
     * serviço externo esteja lento ou indisponível.
     * </p>
     *
     * @param builder o {@link RestTemplateBuilder} injetado pelo Spring, usado para construir e configurar o {@code RestTemplate}
     * @return uma instância de {@link RestTemplate} com balanceamento de carga e timeout aplicados
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(TIMEOUT))
                .setReadTimeout(Duration.ofMillis(TIMEOUT))
                .build();
    }

}