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

import edu.uchicago.cs.ndnn._

/**
  * Created by harper on 4/21/17.
  */
object EncSelNNGraph {
  val hiddenDim = 300
}

class EncSelNNGraph(numFeature: Int, numClass: Int)
  extends Graph(Xavier, new Adam(0.001,0.9,0.999), new SoftMaxLogLoss) {

  val x = input("x")

  {
    val w = param("w", Array(numFeature, EncSelNNGraph.hiddenDim))
    val b = param("b", Array(EncSelNNGraph.hiddenDim))(Zero)
    val map = param("map", Array(EncSelNNGraph.hiddenDim, numClass))
    val mapb = param("mapb", Array(numClass))(Zero)

    val wx = new DotMul(x, w)
    val wxab = new Add(wx, b)
    val sigmoid = new Tanh(wxab)
    val mapped = new DotMul(sigmoid, map)
    val offset = new Add(mapped, mapb)
    val softmax = new SoftMax(offset)

    output(softmax)
  }
}
