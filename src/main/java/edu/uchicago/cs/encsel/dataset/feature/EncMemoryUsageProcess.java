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

package edu.uchicago.cs.encsel.dataset.feature;

import edu.uchicago.cs.encsel.dataset.parquet.ParquetWriterHelper;
import edu.uchicago.cs.encsel.model.*;
import edu.uchicago.cs.encsel.tool.mem.MemoryMonitor;
import edu.uchicago.cs.encsel.tool.mem.MemoryStat;

import java.net.URI;

public class EncMemoryUsageProcess {

    public static void main(String[] args) throws Exception {
        MemoryMonitor.INSTANCE().start();
        URI colFile = new URI(args[0]);
        DataType colDataType = DataType.valueOf(args[1]);
        String encoding = args[2];
        try {
            switch (colDataType) {
                case INTEGER: {
                    IntEncoding e = IntEncoding.valueOf(encoding);
                    ParquetWriterHelper.singleColumnInt(colFile, e);
                }
                case LONG: {
                    LongEncoding e = LongEncoding.valueOf(encoding);
                    ParquetWriterHelper.singleColumnLong(colFile, e);
                }
                case STRING: {
                    StringEncoding e = StringEncoding.valueOf(encoding);
                    ParquetWriterHelper.singleColumnString(colFile, e);
                }
                case DOUBLE: {
                    FloatEncoding e = FloatEncoding.valueOf(encoding);
                    ParquetWriterHelper.singleColumnDouble(colFile, e);
                }
                case FLOAT: {
                    FloatEncoding e = FloatEncoding.valueOf(encoding);
                    ParquetWriterHelper.singleColumnFloat(colFile, e);
                }
                default: {

                }
            }
        } finally {
            MemoryStat stat = MemoryMonitor.INSTANCE().stop();
            System.out.println(stat.max());
            System.exit(0);
        }
    }
}
