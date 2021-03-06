/**
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
  */
package edu.uchicago.cs.encsel.dataset.persist

import edu.uchicago.cs.encsel.dataset.column.Column
import edu.uchicago.cs.encsel.model.DataType

import scala.collection.mutable.ArrayBuffer

class DummyPersistence extends Persistence {
  var storage = new ArrayBuffer[Column]()

  def save(datalist: Traversable[Column]) = {
    storage ++= datalist
  }

  def load(): Iterator[Column] = {
    storage.clone().toIterator
  }

  def clean() = {
    storage.clear()
  }

  def lookup(dataType: DataType): Iterator[Column] = {
    load.filter(_.dataType == dataType)
  }
}