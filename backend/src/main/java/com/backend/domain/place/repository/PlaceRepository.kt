package com.backend.domain.place.repository

import com.backend.domain.place.entity.Place
import org.springframework.data.jpa.repository.JpaRepository

interface PlaceRepository : JpaRepository<Place, Long> {

    fun existsByPlaceNameAndAddress(placeName: String, address: String): Boolean

    fun findByCategoryId(categoryId: Int): List<Place>

    fun getPlaceById(id: Long): Place

    fun findByCategory_Name(name: String): List<Place>

    fun findTop100ByLatitudeIsNullOrderByIdAsc(): List<Place>
}