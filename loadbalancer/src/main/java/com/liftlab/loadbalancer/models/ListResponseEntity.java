package com.liftlab.loadbalancer.models;

import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ListResponseEntity<E>  extends ResponseEntity<PageImpl<E>>{

    public ListResponseEntity(PageImpl<E> listOfObjects, HttpStatus responseStatus) {
        super(listOfObjects, responseStatus);
    }

}
