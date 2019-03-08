/*
 * Copyright 2018 ConsenSys AG.
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
package tech.pegasys.pantheon.ethereum.jsonrpc.internal.processor;

import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.debug.TraceOptions;
import tech.pegasys.pantheon.ethereum.mainnet.TransactionProcessor;
import tech.pegasys.pantheon.ethereum.vm.BlockHashLookup;
import tech.pegasys.pantheon.ethereum.vm.DebugOperationTracer;

import java.util.Optional;

/** Used to produce debug traces of blocks */
public class BlockTracer {

  private final BlockReplay blockReplay;

  private final BlockReplay.Action<TransactionTrace> replayAction =
      (transaction, header, blockchain, mutableWorldState, transactionProcessor) -> {
        DebugOperationTracer tracer = new DebugOperationTracer(TraceOptions.DEFAULT);
        final TransactionProcessor.Result result =
            transactionProcessor.processTransaction(
                blockchain,
                mutableWorldState.updater(),
                header,
                transaction,
                header.getCoinbase(),
                new DebugOperationTracer(TraceOptions.DEFAULT),
                new BlockHashLookup(header, blockchain));
        return new TransactionTrace(transaction, result, tracer.getTraceFrames());
      };

  public BlockTracer(final BlockReplay blockReplay) {
    this.blockReplay = blockReplay;
  }

  public Optional<BlockTrace> trace(final Hash blockHash) {
    return Optional.of(blockReplay.block(blockHash, this.replayAction));
  }

  public Optional<BlockTrace> trace(final Block block) {
    return Optional.of(blockReplay.block(block, this.replayAction));
  }
}
