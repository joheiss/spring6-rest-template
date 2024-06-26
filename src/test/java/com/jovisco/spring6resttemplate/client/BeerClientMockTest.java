package com.jovisco.spring6resttemplate.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withAccepted;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jovisco.spring6resttemplate.config.OAuth2ClientInterceptor;
import com.jovisco.spring6resttemplate.config.RestTemplateBuilderConfig;
import com.jovisco.spring6resttemplate.model.BeerDTO;
import com.jovisco.spring6resttemplate.model.BeerDTOPageImpl;
import com.jovisco.spring6resttemplate.model.BeerStyle;

@RestClientTest(BeerClientImpl.class)
@Import(RestTemplateBuilderConfig.class)
public class BeerClientMockTest {

    static final String BASE_URL = "http://localhost:8080";

    @Autowired
    RestTemplateBuilder restTemplateBuilderConfigured;

    @Autowired
    ObjectMapper objectMapper;
    
    @Mock
    RestTemplateBuilder mockRestTemplateBuilder = new RestTemplateBuilder(new MockServerRestTemplateCustomizer());

    BeerClient beerClient;
    MockRestServiceServer mockRestServiceServer;

    @MockBean
    OAuth2AuthorizedClientManager manager;

    @TestConfiguration
    public static class TestConfig {

        @Bean
        ClientRegistrationRepository clientRegistrationRepository() {
            return new InMemoryClientRegistrationRepository(ClientRegistration
                    .withRegistrationId("springauth")
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .clientId("test")
                    .tokenUri("test")
                    .build());
        }

        @Bean
        OAuth2AuthorizedClientService auth2AuthorizedClientService(ClientRegistrationRepository clientRegistrationRepository){
            return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
        }

        @Bean
        OAuth2ClientInterceptor oAuthClientInterceptor(OAuth2AuthorizedClientManager manager, ClientRegistrationRepository clientRegistrationRepository){
            return new OAuth2ClientInterceptor(manager, clientRegistrationRepository);
        }
    }

    @Autowired
    ClientRegistrationRepository clientRegistrationRepository;

    @BeforeEach
    void setUp() {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("springauth");

        OAuth2AccessToken token = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "test", 
            Instant.MIN, 
            Instant.MAX
        );

        when(manager.authorize(any())).thenReturn(new OAuth2AuthorizedClient(clientRegistration, "test", token));

        RestTemplate restTemplate = restTemplateBuilderConfigured.build();
        mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build();
        when(mockRestTemplateBuilder.build()).thenReturn(restTemplate);
        beerClient = new BeerClientImpl(mockRestTemplateBuilder);
    }

    @Test
    void testCreateBeer() throws JsonProcessingException {
        var beer = getBeerDto();

        URI uri = UriComponentsBuilder.fromPath(BeerClientImpl.BEER_ID_PATH).build(beer.getId());
        
        mockRestServiceServer
            .expect(method(HttpMethod.POST))
            .andExpect(requestTo(BASE_URL + BeerClientImpl.BEER_PATH))
            .andExpect(header("Authorization", "Bearer test"))
            .andExpect(header("Authorization", Matchers.containsString("Bearer ")))
            .andExpect(header("Authorization", Matchers.startsWith("Bearer ")))
            .andRespond(withAccepted().location(uri));

        mockGetOperation(beer);

        BeerDTO result = beerClient.createBeer(beer);
        assertThat(result.getId()).isEqualTo(beer.getId());
    }

    @Test
    void testDeleteBeer() {
        var beer = getBeerDto();

        mockRestServiceServer
            .expect(method(HttpMethod.DELETE))
            .andExpect(requestToUriTemplate(BASE_URL + BeerClientImpl.BEER_ID_PATH, beer.getId()))
            .andExpect(header("Authorization", "Bearer test"))
            .andRespond(withNoContent());

        beerClient.deleteBeer(beer.getId());

        // check that the mock was called ...
        mockRestServiceServer.verify();
    }

     @Test
    void testDeleteNotFound() {
        var beer = getBeerDto();

        mockRestServiceServer
            .expect(method(HttpMethod.DELETE))
            .andExpect(requestToUriTemplate(BASE_URL + BeerClientImpl.BEER_ID_PATH, beer.getId()))
            .andExpect(header("Authorization", "Bearer test"))
            .andRespond(withResourceNotFound());

        assertThatExceptionOfType(HttpClientErrorException.class).isThrownBy(
            () -> beerClient.deleteBeer(beer.getId())
        );

        mockRestServiceServer.verify();
    }

    @Test
    void testGetBeerById() throws JsonProcessingException {
        var beer = getBeerDto();

        mockGetOperation(beer);

        BeerDTO result = beerClient.getBeerById(beer.getId());
        assertThat(result.getId()).isEqualTo(beer.getId());
    }

    @Test
    void testListBeers() throws JsonProcessingException {
        var payload = objectMapper.writeValueAsString(getPage(getBeerDto()));

        mockRestServiceServer
            .expect(method(HttpMethod.GET))
            .andExpect(requestTo(BASE_URL + BeerClientImpl.BEER_PATH))
            .andExpect(header("Authorization", "Bearer test"))
            .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        Page<BeerDTO> result = beerClient.listBeers(null);
        assertThat(result.getContent().size()).isGreaterThan(0);
    }

    @Test
    void testListBeersWithName() throws JsonProcessingException { 

        var response = objectMapper.writeValueAsString(getPage(getBeerDto()));

        URI uri = UriComponentsBuilder
            .fromHttpUrl(BASE_URL + BeerClientImpl.BEER_PATH)
            .queryParam("name", "ALE")
            .build()
            .toUri();

        mockRestServiceServer
            .expect(method(HttpMethod.GET))
            .andExpect(requestTo(uri))
            .andExpect(header("Authorization", "Bearer test"))
            .andExpect(queryParam("name", "ALE"))
            .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        Page<BeerDTO> result = beerClient.listBeers("ALE");
        assertThat(result.getContent().size()).isEqualTo(1);
    }

    @Test
    void testUpdateBeer() throws JsonProcessingException {
        var beer = getBeerDto();

        mockRestServiceServer
            .expect(method(HttpMethod.PUT))
            .andExpect(requestToUriTemplate(BASE_URL + BeerClientImpl.BEER_ID_PATH, beer.getId()))
            .andExpect(header("Authorization", "Bearer test"))
            .andRespond(withNoContent());

        mockGetOperation(beer);

        BeerDTO updated= beerClient.updateBeer(beer);
        assertThat(updated.getId()).isEqualTo(beer.getId());
    }

    private BeerDTOPageImpl<BeerDTO> getPage(BeerDTO beerDTO) {
        return new BeerDTOPageImpl<>(Arrays.asList(beerDTO), 1, 25, 1);
    }
    
    private BeerDTO getBeerDto() {
        return BeerDTO.builder()
            .id(UUID.randomUUID())
            .style(BeerStyle.PORTER)
            .name("Porter Ricks")
            .price(new BigDecimal("7.65"))
            .quantityOnHand(111)
            .upc("47111001")
            .build();
    }

    private void mockGetOperation(BeerDTO beer) throws JsonProcessingException {
        var response = objectMapper.writeValueAsString(beer);

        mockRestServiceServer
            .expect(method(HttpMethod.GET))
            .andExpect(requestToUriTemplate(BASE_URL + BeerClientImpl.BEER_ID_PATH, beer.getId()))
            .andExpect(header("Authorization", "Bearer test"))
            .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
    }
}