package com.taxi.tripservice.repository;

import com.taxi.tripservice.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, Long> {

}