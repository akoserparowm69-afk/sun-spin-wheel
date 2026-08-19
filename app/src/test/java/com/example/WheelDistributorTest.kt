package com.example

import com.example.data.WheelItem
import com.example.util.WheelDistributor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WheelDistributorTest {

    @Test
    fun testGenerateSlicesDistribution() {
        val items = listOf(
            WheelItem(id = "1", name = "ထီး", colorHex = "#EC4899", quantity = 2),
            WheelItem(id = "2", name = "ဦးထုပ်", colorHex = "#3B82F6", quantity = 1),
            WheelItem(id = "3", name = "တီရှပ်", colorHex = "#10B981", quantity = 3)
        )

        val slices = WheelDistributor.distributeSlices(items)
        assertEquals(6, slices.size)

        val item1Count = slices.count { it.itemId == "1" }
        val item2Count = slices.count { it.itemId == "2" }
        val item3Count = slices.count { it.itemId == "3" }

        assertEquals(2, item1Count)
        assertEquals(1, item2Count)
        assertEquals(3, item3Count)
    }

    @Test
    fun testInterleavingAdjacentIdenticalItems() {
        val items = listOf(
            WheelItem(id = "1", name = "Item A", colorHex = "#EC4899", quantity = 2),
            WheelItem(id = "2", name = "Item B", colorHex = "#3B82F6", quantity = 2)
        )

        val slices = WheelDistributor.distributeSlices(items)
        assertEquals(4, slices.size)

        // Verify items are interleaved (A, B, A, B)
        for (i in slices.indices) {
            val next = (i + 1) % slices.size
            assertTrue(slices[i].itemId != slices[next].itemId)
        }
    }
}

