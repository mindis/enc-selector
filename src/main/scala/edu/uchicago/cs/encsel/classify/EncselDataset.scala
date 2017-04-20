/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License,
 *
 * Contributors:
 *     Hao Jiang - initial API and implementation
 *
 */

package edu.uchicago.cs.encsel.classify

import java.util.Collections

import edu.uchicago.cs.encsel.dataset.persist.Persistence
import edu.uchicago.cs.encsel.model.DataType
import edu.uchicago.cs.ndnn.{Dataset, DefaultDataset}
import org.nd4j.linalg.api.ndarray.INDArray

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object EncselDataset {
  val featureMap = Map(
    (DataType.STRING -> Array(("Distinct", "ratio"),
      ("Entropy", "line_mean"),
      ("Entropy", "line_max"),
      ("Entropy", "line_min"),
      ("Entropy", "line_var"),
      ("Entropy", "total"),
      ("Length", "variance"),
      ("Sparsity", "valid_ratio")))
    , (DataType.INTEGER -> Array(("Distinct", "ratio"),
      ("Entropy", "line_mean"),
      ("Entropy", "line_max"),
      ("Entropy", "line_min"),
      ("Entropy", "line_var"),
      ("Entropy", "total"),
      ("Length", "variance"),
      ("Sparsity", "valid_ratio")))
  )
}

/**
  * Encoding Selector Dataset. Load Column Data from Database with the given data type
  */
class EncselDataset(val dataType: DataType) extends DefaultDataset {

  override def load(): (Array[Array[Double]], Array[Double]) = {

    val typeMap = new mutable.HashMap[String, Int]

    val features = new ArrayBuffer[Array[Double]]
    val labels = new ArrayBuffer[Double]

    val pairs = Persistence.get.load().map(
      column => {
        val mapping = EncselDataset.featureMap.getOrElse(dataType, Array.empty[(String, String)])
        val feature = mapping.map(m => {
          column.findFeature(m._1, m._2) match {
            case None => 0
            case Some(e) => e.value
          }
        })
        val minName = column.findFeatures("EncFileSize").minBy(_.value).name
        val label = typeMap.getOrElseUpdate(minName, typeMap.size)

        features += feature
        labels += label
      }
    )

    (features.toArray, labels.toArray)
  }

  def split(ratio: Seq[Double]): Seq[Dataset[INDArray]] = {

    val length = this.datas.length
    val permutation = (0 until length).toBuffer
    Collections.shuffle(permutation)

    val slice = ratio.map(d => Math.floor(d * length).toInt).toBuffer
    val addup = length - slice.sum

    slice(slice.length - 1) += addup

    var pointer = 0

    slice.map(cnt => {
      val result = fetchSub(permutation, pointer, pointer + cnt)
      pointer += cnt
      result
    })
  }

  protected def fetchSub(permutation: Seq[Int], from: Int, to: Int): Dataset[INDArray] = {
    val data = new ArrayBuffer[Array[Double]]
    val label = new ArrayBuffer[Double]
    (from until to).foreach(idx => {
      data += this.datas(permutation(idx))
      label += this.expects(permutation(idx))
    })
    new EncselSplitDataset(data.toArray, label.toArray)
  }
}

class EncselSplitDataset(val features: Array[Array[Double]],
                         val expect: Array[Double])
  extends DefaultDataset {

  override def load(): (Array[Array[Double]], Array[Double]) = (features, expect)
}
