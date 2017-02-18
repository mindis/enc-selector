package edu.uchicago.cs.encsel.dataset.parser.excel

import edu.uchicago.cs.encsel.dataset.parser.csv.CommonsCSVParser

import java.io.File
import edu.uchicago.cs.encsel.dataset.schema.Schema
import org.junit.Test
import org.junit.Assert._

class XLSXParserTest {
  @Test
  def testParse(): Unit = {
    var records = new XLSXParser().parse(new File("src/test/resource/test_xlsx_parser.xlsx").toURI(),
      Schema.fromParquetFile(new File("src/test/resource/test_xlsx_parser.schema").toURI())).toArray
    assertEquals(6, records.length)
    assertEquals("1.77", records(0)(1))
  }

  @Test
  def testGuessHeader: Unit = {
    var parser = new XLSXParser()
    var records = parser.parse(new File("src/test/resource/test_xlsx_parser.xlsx").toURI(),
      null).toArray
    var guessedHeader = parser.guessHeaderName
    assertArrayEquals(Array[Object]("X", "Y", "Z", "W"), guessedHeader.toArray[Object])
    assertEquals(6, records.length)
    assertEquals(4, records(0).length())
    assertEquals("""This is a long story""", records(0)(2))
    assertEquals("XSSFCell@3[3,3.44,32w24ger,MM]", records(2).toString())
    assertArrayEquals(Array[Object]("3", "3.44", "32w24ger", "MM"), records(2).iterator().toArray[Object])
  }
}