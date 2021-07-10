package com.drn.domaincrawler;

import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class DomainCrawlerService {

    private KafkaTemplate<String, Domain> kafkaTemplate;
    private final String KAFKA_TOPIC = "web-domains";
    private final String URI_NAME_BEGIN = "https://api.domainsdb.info/v1/domains/search?domain=";
    private final String URI_NAME_END = "&zone=com";

    public DomainCrawlerService(KafkaTemplate<String, Domain> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void crawl(String name) {

        Mono<DomainList> domainListMono = WebClient.create()
                .get()
                .uri(URI_NAME_BEGIN+name+URI_NAME_END)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(DomainList.class);

        domainListMono.subscribe(domainList -> {
           domainList.domains
                .forEach(domain -> {
                    kafkaTemplate.send(KAFKA_TOPIC, domain);
                    System.out.println("Domain message" + domain.getDomain());
                });
        });

    }
}
