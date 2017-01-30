package edu.uchicago.cs.encsel.colread

import org.junit.Test
import org.junit.Assert._
import java.net.URI
import java.io.File
import edu.uchicago.cs.encsel.model.DataType

class SchemaTest {

  @Test
  def testFromParquetFile(): Unit = {
    var schema = Schema.fromParquetFile(new File("resource/test.schema").toURI())
    assertEquals(22, schema.columns.length)
    assertTrue(schema.hasHeader)

    assertEquals(DataType.STRING, schema.columns(3)._1) 
    assertEquals ("Block", schema.columns(3)._2)
  }

}