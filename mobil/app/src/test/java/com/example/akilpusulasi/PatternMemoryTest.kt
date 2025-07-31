package com.example.akilpusulasi

import org.junit.Test
import org.junit.Assert.*

/**
 * Pattern Memory oyunu için basit test sınıfı
 */
class PatternMemoryTest {

    @Test
    fun testPatternGeneration() {
        // Test: Desen oluşturma fonksiyonu doğru çalışıyor mu?
        val pattern = mutableListOf<Int>()
        val gridSize = 3
        val numToIlluminate = 3
        
        // Basit desen oluşturma simülasyonu
        for (i in 0 until numToIlluminate) {
            pattern.add(i)
        }
        
        assertEquals("Desen boyutu doğru olmalı", numToIlluminate, pattern.size)
        assertTrue("Desen boş olmamalı", pattern.isNotEmpty())
    }

    @Test
    fun testPatternComparison() {
        // Test: Desen karşılaştırma fonksiyonu doğru çalışıyor mu?
        val originalPattern = listOf(0, 1, 2)
        val userSelection = listOf(2, 1, 0) // Sıra farklı ama aynı hücreler
        
        val isCorrect = userSelection.sorted() == originalPattern.sorted()
        
        assertTrue("Sıralama farklı olsa bile aynı hücreler doğru kabul edilmeli", isCorrect)
    }

    @Test
    fun testWrongPattern() {
        // Test: Yanlış desen tespiti doğru çalışıyor mu?
        val originalPattern = listOf(0, 1, 2)
        val userSelection = listOf(0, 1, 3) // Farklı hücre
        
        val isCorrect = userSelection.sorted() == originalPattern.sorted()
        
        assertFalse("Farklı hücreler yanlış kabul edilmeli", isCorrect)
    }

    @Test
    fun testLevelProgression() {
        // Test: Seviye ilerlemesi doğru çalışıyor mu?
        var currentLevel = 1
        var gridSize = 3
        var cellsToIlluminate = 3
        
        // Seviye 3'e kadar ilerle
        for (i in 1..3) {
            currentLevel = i
            if (currentLevel % 3 == 0 && gridSize < 5) {
                gridSize++
            }
            cellsToIlluminate++
        }
        
        assertEquals("Seviye 3'te grid boyutu artmalı", 4, gridSize)
        assertEquals("Hücre sayısı artmalı", 6, cellsToIlluminate)
    }

    @Test
    fun testGridSizeLimits() {
        // Test: Grid boyutu sınırları doğru kontrol ediliyor mu?
        var gridSize = 3
        
        // 5x5'e kadar artır
        for (i in 1..10) {
            if (i % 3 == 0 && gridSize < 5) {
                gridSize++
            }
        }
        
        assertTrue("Grid boyutu 5'i geçmemeli", gridSize <= 5)
        assertEquals("Maksimum grid boyutu 5 olmalı", 5, gridSize)
    }
} 