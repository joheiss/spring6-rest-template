package com.jovisco.spring6resttemplate.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.HttpClientErrorException;

import com.jovisco.spring6resttemplate.model.BeerDTO;
import com.jovisco.spring6resttemplate.model.BeerStyle;

@SpringBootTest
public class BeerClientImplTest {
    @Autowired
    BeerClientImpl beerClient;

    @Test
    void testListBeersWithName() {
        String name = "Golden";
        var result = beerClient.listBeers(name);

        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    void testListBeersWithOutName() {
        var result = beerClient.listBeers(null);

        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    void testGetBeerById() {
        var testBeer = beerClient.listBeers(null).getContent().getFirst();
        var found = beerClient.getBeerById(testBeer.getId());
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(testBeer.getId());
    }

    @Test
    void testCreateBeeer() {
        var beer = BeerDTO.builder()
            .name("Striezi Dunkel")
            .style(BeerStyle.IPA)
            .price(new BigDecimal("9.87"))
            .quantityOnHand(23)
            .upc("4711001")
            .build();
        
            var created = beerClient.createBeer(beer);
        
            assertThat(created).isNotNull();
            assertThat(created.getName()).isEqualTo(beer.getName());
    }

    @Test
    void testUpdateBeer() {

        // first create new beer
        var beer = BeerDTO.builder()
            .name("Striezi Dunkel")
            .style(BeerStyle.IPA)
            .price(new BigDecimal("9.87"))
            .quantityOnHand(23)
            .upc("4711001")
            .build();
        
        var created = beerClient.createBeer(beer);

        // ... and then update it
        final String updatedName = created.getName() + "*UPDATED*";
        created.setName(updatedName);
        var updated = beerClient.updateBeer(created);

        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getName()).isEqualTo(updatedName);

    }

    @Test
    void testDeleteBeer() {

        // first get a beer to be deleted
        var testBeer = beerClient.listBeers(null).getContent().getLast();

        var id = testBeer.getId();
        beerClient.deleteBeer(id);

        assertThatExceptionOfType(HttpClientErrorException.class).isThrownBy(
            () -> beerClient.getBeerById(id)
        );
    }

}
