package com.soten.deliverycheck.repository

import com.soten.deliverycheck.data.entity.TrackingInformation
import com.soten.deliverycheck.data.entity.TrackingItem
import kotlinx.coroutines.flow.Flow

interface TrackingItemRepository {

    val trackingItems: Flow<List<TrackingItem>>

    suspend fun getTrackingInformation(companyCode: String, invoice: String): TrackingInformation?

    suspend fun getTrackingItemInformation(): List<Pair<TrackingItem, TrackingInformation>>

    suspend fun saveTrackingItem(trackingItem: TrackingItem)

    suspend fun deleteTrackingItem(trackingItem: TrackingItem)

}