package com.ead.authuser.configs;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Classe de configuração responsável por criar e configurar uma instância de {@link RestTemplate}.
 * <p>
 * A configuração define um tempo de timeout de conexão e de leitura, e habilita o balanceamento de carga
 * entre instâncias de serviços registrados.
 * </p>
 *
 * <p>Esta classe é marcada com a anotação {@link Configuration}, o que indica ao Spring que ela contém
 * definições de beans que devem ser gerenciados pelo contêiner de IoC.</p>
 *
 */
@Configuration
public class RestTemplateConfig {

    static final int TIMEOUT = 5000;

    /**
     * Cria um bean {@link RestTemplate} com suporte a balanceamento de carga.
     * <p>
     * O {@code RestTemplate} criado usará os valores definidos para os timeouts de conexão e leitura.
     * A anotação {@link LoadBalanced} permite que o {@code RestTemplate} utilize nomes de serviços
     * ao fazer chamadas, com o balanceamento de carga sendo tratado automaticamente.
     * </p>
     *
     * @param builder o {@link RestTemplateBuilder} injetado pelo Spring, usado para configurar o {@code RestTemplate}
     * @return uma instância de {@link RestTemplate} configurada
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