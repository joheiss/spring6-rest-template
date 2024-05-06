package com.jovisco.spring6resttemplate.client;

import java.net.URI;
import java.util.UUID;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.jovisco.spring6resttemplate.model.BeerDTO;
import com.jovisco.spring6resttemplate.model.BeerDTOPageImpl;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BeerClientImpl implements BeerClient{

    private final RestTemplateBuilder restTemplateBuilder;

    // private static final String BASE_URL = "http://localhost:8080/api/v1/";
    public static final String BEER_PATH = "/api/v1/beers";
    public static final String BEER_ID_PATH = "/api/v1/beers/{id}";

    @SuppressWarnings("unchecked")
    @Override
    public Page<BeerDTO> listBeers(String name) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath(BEER_PATH);

        if (name != null) {
            uriComponentsBuilder.queryParam("name", name);
        }
        
        @SuppressWarnings("rawtypes")
        ResponseEntity<BeerDTOPageImpl> pageResponse = restTemplate.getForEntity(
            uriComponentsBuilder.toUriString(), BeerDTOPageImpl.class);



        return pageResponse.getBody();
    }

    public BeerDTO getBeerById(UUID id) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        return restTemplate.getForObject(BEER_ID_PATH, BeerDTO.class, id);
    }

    @SuppressWarnings("null")
    @Override
    public BeerDTO createBeer(BeerDTO beer) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        URI uri = restTemplate.postForLocation(BEER_PATH, beer);
        return restTemplate.getForObject(uri.getPath(), BeerDTO.class);
    }

    @Override
    public BeerDTO updateBeer(BeerDTO beer) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.put(BEER_ID_PATH, beer, beer.getId());
        // fetch the updated entity ... and return it
        return getBeerById(beer.getId());
    }

    @Override
    public void deleteBeer(UUID id) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.delete(BEER_ID_PATH, id);
    }

}
