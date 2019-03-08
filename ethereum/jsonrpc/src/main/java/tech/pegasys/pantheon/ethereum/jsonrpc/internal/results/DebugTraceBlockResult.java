/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.ethereum.jsonrpc.internal.results;

import tech.pegasys.pantheon.ethereum.debug.TraceFrame;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.processor.BlockTrace;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.processor.TransactionTrace;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"gas", "structLogs"})
public class DebugTraceBlockResult {

  private final List<StructLog> structLogs;
  private final long gas;

  public DebugTraceBlockResult(final BlockTrace blockTrace) {
    this.structLogs = getAggregatedTraceFrames(blockTrace);
    this.gas = getTotalGas(blockTrace);
  }

  private long getTotalGas(final BlockTrace blockTrace) {
    return blockTrace.getTransactions().stream()
        .map(TransactionTrace::getGas)
        .mapToLong(Long::valueOf)
        .sum();
  }

  private List<StructLog> getAggregatedTraceFrames(final BlockTrace blockTrace) {
    return blockTrace.getTransactions().stream()
        .map(TransactionTrace::getTraceFrames)
        .flatMap(List::stream)
        .map(DebugTraceBlockResult::createStructLog)
        .collect(Collectors.toList());
  }

  //  todo: redundant code
  private static StructLog createStructLog(final TraceFrame frame) {
    return frame.getExceptionalHaltReasons().isEmpty()
        ? new StructLog(frame)
        : new StructLogWithError(frame);
  }

  @JsonGetter(value = "gas")
  public long getGas() {
    return gas;
  }

  @JsonGetter(value = "structLogs")
  public List<StructLog> getStructLogs() {
    return structLogs;
  }
}
