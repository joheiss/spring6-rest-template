package com.jovisco.spring6resttemplate.client;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.jovisco.spring6resttemplate.model.BeerDTO;

public interface BeerClient {

    Page<BeerDTO> listBeers(String name);
    BeerDTO getBeerById(UUID id);
    BeerDTO createBeer(BeerDTO beer);
    BeerDTO updateBeer(BeerDTO beer);
    void deleteBeer(UUID id);

}
