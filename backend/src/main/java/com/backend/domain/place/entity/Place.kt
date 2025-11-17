package com.backend.domain.place.entity

import com.backend.domain.category.entity.Category
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Place(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var placeName: String,

    var address: String? = null,

    @Column(length = 50)
    var gu: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: Category,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column
    var latitude: Double? = null,   // y

    @Column
    var longitude: Double? = null,  // x

    @Column(length = 100)
    var kakaoPlaceId: String? = null,

    @Column(nullable = false)
    var ratingSum: Long = 0L,

    @Column(nullable = false)
    var ratingCount: Int = 0,

    @Column(nullable = false)
    var ratingAvg: Double = 0.0,

    @Version
    var version: Long? = null,

    var createdDate: LocalDateTime? = null,
    var updatedDate: LocalDateTime? = null,
) {

    protected constructor() : this(
        placeName = "",
        address = null,
        gu = null,
        category = Category(),
        description = null,
        // 새 필드는 기본값 그대로 사용 (null / null / null)
    )

    fun update(
        placeName: String,
        address: String,
        gu: String,
        description: String?
    ) {
        this.placeName = placeName
        this.address = address
        this.gu = gu
        this.description = description
    }

    fun updateCoordinate(lat: Double, lng: Double, kakaoPlaceId: String? = null) {
        this.latitude = lat
        this.longitude = lng
        if (kakaoPlaceId != null) {
            this.kakaoPlaceId = kakaoPlaceId
        }
    }

}