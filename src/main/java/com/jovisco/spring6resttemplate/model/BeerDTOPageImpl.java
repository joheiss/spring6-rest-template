package com.jovisco.spring6resttemplate.model;

import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true, value = "pageable")
public class BeerDTOPageImpl<T extends BeerDTO> extends PageImpl<T> {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public BeerDTOPageImpl(
        @JsonProperty("content") List<T> content,
        @JsonProperty("number") int page,
        @JsonProperty("size") int size,
        @JsonProperty("totalElements") long total
        ) {
            super(content, PageRequest.of(page, size), total);
    }

    public BeerDTOPageImpl(List<T> content, Pageable pageable, long total) {
        super(content);
    }

    public BeerDTOPageImpl(List<T> content) {
        super(content);
    }

}
