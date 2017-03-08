/**
 * *****************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Contributors:
 * Hao Jiang - initial API and implementation
 *
 * *****************************************************************************
 */
package edu.uchicago.cs.encsel.ndnn.rnn

import edu.uchicago.cs.encsel.ndnn.Dataset
import org.nd4j.linalg.api.ndarray.INDArray
import edu.uchicago.cs.encsel.ndnn.Dataset
import edu.uchicago.cs.encsel.ndnn.Batch

import scala.io.Source
import scala.collection.mutable.{ ArrayBuffer, Buffer, HashMap }
import org.nd4j.linalg.factory.Nd4j

import scala.collection.JavaConversions._
import java.io.InputStream

import edu.uchicago.cs.encsel.ndnn.Index
import org.nd4j.linalg.indexing.NDArrayIndex
import edu.uchicago.cs.encsel.ndnn.DatasetBase
import java.net.URI
import java.io.File

class LSTMDataset(file: URI, extdict: LSTMDataset) extends DatasetBase[Array[Array[Int]]] {

  def this(file: String)(implicit extdict: LSTMDataset = null) {
    this(new File(file).toURI, extdict)
  }

  def this(file: URI) {
    this(file, null)
  }

  private val dict = new HashMap[Char, Int]
  private val inverseDict = new ArrayBuffer[Char]

  private val PAD = '@'

  protected val strlines = Source.fromFile(file).getLines().map(line =>
    "{%s}".format(line.trim.toLowerCase)).toBuffer
  if (extdict == null) {
    dict += ((PAD, 0))
    inverseDict += PAD
    strlines.foreach(line =>
      line.toCharArray().foreach { c =>
        dict.getOrElseUpdate(c, { inverseDict += c; dict.size })
      })
  } else {
    dict ++= extdict.dict
    inverseDict ++= extdict.inverseDict
  }

  // Second pass, replace characters with index
  protected val lines = strlines.map(line =>
    line.toCharArray().map(dict.getOrElse(_, 0))).sortBy(-_.length)
  strlines.clear

  this.dataSize = lines.length
  initShuffle(false)
  // No shuffle to keep similar size batches together

  protected def construct(idices: Buffer[Int]): Batch[Array[Array[Int]]] = {
    val data = idices.map(lines(_))
    val maxlength = data.map(_.length).max
    val padded = data.map(_.padTo(maxlength, dict.getOrElse(PAD, -1)))
    val transposed = (0 until maxlength).map(i => padded.map(_(i)).toArray).toArray

    new Batch[Array[Array[Int]]](idices.length, transposed.dropRight(1), transposed.drop(1))
  }

  override def newEpoch() = {
    // No permute
  }

  def translate(input: String): Array[Int] =
    input.toCharArray().map(dict.getOrElse(_, -1))

  def translate(input: Double): Char = inverseDict(input.toInt)

  def numChars: Int = dict.size
}