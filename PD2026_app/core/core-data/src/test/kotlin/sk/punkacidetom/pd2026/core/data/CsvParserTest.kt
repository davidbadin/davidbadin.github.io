package sk.punkacidetom.pd2026.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import sk.punkacidetom.pd2026.core.data.mapper.BandMapper
import sk.punkacidetom.pd2026.core.data.remote.CsvParser

class CsvParserTest {

    @Test
    fun `parse simple row`() {
        val csv = "A,B,C\n1,hello,world"
        val rows = CsvParser.parse(csv)
        assertEquals(2, rows.size)
        assertEquals(listOf("A", "B", "C"), rows[0])
        assertEquals(listOf("1", "hello", "world"), rows[1])
    }

    @Test
    fun `parse quoted field with comma`() {
        val csv = "A,B\n\"hello, world\",foo"
        val rows = CsvParser.parse(csv)
        assertEquals("hello, world", rows[1][0])
        assertEquals("foo", rows[1][1])
    }

    @Test
    fun `parse doubled-quote escape`() {
        val csv = "A\n\"say \"\"hi\"\"\""
        val rows = CsvParser.parse(csv)
        assertEquals("say \"hi\"", rows[1][0])
    }

    @Test
    fun `blank ID row is skipped by BandMapper`() {
        val csv = buildString {
            appendLine("ID,START_DATE,START_TIME,END_DATE,END_TIME,BAND,DESCRIPTION,STAGE,SPOTIFY_URL,GENRE,SORTING_PRIORITY,DESCRIPTION_EN,IMAGE_NAME")
            appendLine(",28.5.2026,20:00:00,28.5.2026,21:00:00,ShouldBeSkipped,,A,,,,,")
            appendLine("1,28.5.2026,20:00:00,28.5.2026,21:00:00,TestBand,,A,,,,,")
        }
        val rows = CsvParser.parse(csv)
        val bands = BandMapper.mapRows(rows)
        assertEquals(1, bands.size)
        assertEquals("TestBand", bands[0].name)
    }

    @Test
    fun `duplicate ID keeps first occurrence`() {
        val csv = buildString {
            appendLine("ID,START_DATE,START_TIME,END_DATE,END_TIME,BAND,DESCRIPTION,STAGE,SPOTIFY_URL,GENRE,SORTING_PRIORITY,DESCRIPTION_EN,IMAGE_NAME")
            appendLine("1,28.5.2026,20:00:00,28.5.2026,21:00:00,First,,A,,,,,")
            appendLine("1,28.5.2026,22:00:00,28.5.2026,23:00:00,Second,,A,,,,,")
        }
        val rows = CsvParser.parse(csv)
        val bands = BandMapper.mapRows(rows)
        assertEquals(1, bands.size)
        assertEquals("First", bands[0].name)
    }

    @Test
    fun `parse date without leading zeros`() {
        val csv = buildString {
            appendLine("ID,START_DATE,START_TIME,END_DATE,END_TIME,BAND,DESCRIPTION,STAGE,SPOTIFY_URL,GENRE,SORTING_PRIORITY,DESCRIPTION_EN,IMAGE_NAME")
            appendLine("1,5.6.2026,9:30:00,5.6.2026,10:30:00,TestBand,,A,,,,,")
        }
        val rows = CsvParser.parse(csv)
        val bands = BandMapper.mapRows(rows)
        assertEquals(1, bands.size)
        val band = bands[0]
        assertEquals(5, band.startDate.dayOfMonth)
        assertEquals(6, band.startDate.monthValue)
        assertEquals(2026, band.startDate.year)
        assertEquals(9, band.startTime.hour)
        assertEquals(30, band.startTime.minute)
    }

    @Test
    fun `missing DESCRIPTION_EN falls back gracefully`() {
        val csv = buildString {
            appendLine("ID,START_DATE,START_TIME,END_DATE,END_TIME,BAND,DESCRIPTION,STAGE,SPOTIFY_URL,GENRE,SORTING_PRIORITY")
            appendLine("1,28.5.2026,20:00:00,28.5.2026,21:00:00,TestBand,Slovak text,A,,,")
        }
        val rows = CsvParser.parse(csv)
        val bands = BandMapper.mapRows(rows)
        assertEquals(1, bands.size)
        assertEquals("", bands[0].descriptionEn) // column missing → empty string
        assertEquals("Slovak text", bands[0].description("sk"))
        assertEquals("Slovak text", bands[0].description("en")) // fallback to SK
    }
}
