package com.example.util

import com.example.data.WheelItem
import com.example.data.WheelSlice
import java.util.Collections

object WheelDistributor {

    /**
     * Generates wheel slices where each item appears according to its quantity.
     * Slices are interleaved so identical items are not placed adjacently when possible.
     */
    fun distributeSlices(items: List<WheelItem>): List<WheelSlice> {
        val validItems = items.filter { it.quantity > 0 && it.name.isNotBlank() }
        if (validItems.isEmpty()) return emptyList()

        val totalSlices = validItems.sumOf { it.quantity }
        if (totalSlices == 0) return emptyList()

        if (validItems.size == 1) {
            val single = validItems.first()
            return (0 until single.quantity).map { idx ->
                WheelSlice(
                    sliceIndex = idx,
                    itemId = single.id,
                    name = single.name,
                    colorHex = single.colorHex
                )
            }
        }

        // Sort items by quantity descending
        val sortedItems = validItems.sortedByDescending { it.quantity }
        val maxQty = sortedItems.first().quantity
        val bucketCount = maxQty.coerceAtLeast(1)

        val buckets = List(bucketCount) { mutableListOf<WheelItem>() }
        var currentBucket = 0

        for (item in sortedItems) {
            for (q in 0 until item.quantity) {
                buckets[currentBucket % bucketCount].add(item)
                currentBucket++
            }
        }

        // Flatten buckets into single list
        val flattened = mutableListOf<WheelItem>()
        for (bucket in buckets) {
            flattened.addAll(bucket)
        }

        // Resolve any adjacent duplicates if possible
        if (flattened.size > 2) {
            resolveAdjacency(flattened)
        }

        return flattened.mapIndexed { index, item ->
            WheelSlice(
                sliceIndex = index,
                itemId = item.id,
                name = item.name,
                colorHex = item.colorHex
            )
        }
    }

    private fun resolveAdjacency(list: MutableList<WheelItem>) {
        val n = list.size
        val maxAttempts = n * 2

        for (attempt in 0 until maxAttempts) {
            var collisionFound = false
            for (i in 0 until n) {
                val next = (i + 1) % n
                if (list[i].id == list[next].id) {
                    collisionFound = true
                    // Find a candidate to swap with
                    var swapped = false
                    for (j in 0 until n) {
                        if (j == i || j == next) continue
                        val prevJ = (j - 1 + n) % n
                        val nextJ = (j + 1) % n

                        val candidate = list[j]
                        val current = list[i]

                        // Check if placing candidate at 'i' and current at 'j' solves without creating new conflict
                        val canPlaceCandidateAtI = candidate.id != list[(i - 1 + n) % n].id && candidate.id != list[next].id
                        val canPlaceCurrentAtJ = current.id != list[prevJ].id && current.id != list[nextJ].id

                        if (canPlaceCandidateAtI && canPlaceCurrentAtJ) {
                            Collections.swap(list, i, j)
                            swapped = true
                            break
                        }
                    }
                    if (!swapped) {
                        // Fallback: pick any index not equal to current item
                        for (j in 0 until n) {
                            if (list[j].id != list[i].id) {
                                Collections.swap(list, next, j)
                                break
                            }
                        }
                    }
                }
            }
            if (!collisionFound) break
        }
    }
}
