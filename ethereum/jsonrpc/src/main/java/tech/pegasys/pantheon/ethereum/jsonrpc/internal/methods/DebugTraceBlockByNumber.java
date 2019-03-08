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
package tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods;

import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.processor.BlockTracer;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.results.DebugTraceBlockResult;

import java.util.Optional;

public class DebugTraceBlockByNumber implements JsonRpcMethod {

  private final JsonRpcParameter parameters;
  private final BlockTracer blockTracer;
  private final BlockchainQueries blockchain;

  public DebugTraceBlockByNumber(
      final JsonRpcParameter parameters,
      final BlockTracer blockTracer,
      final BlockchainQueries blockchain) {
    this.parameters = parameters;
    this.blockTracer = blockTracer;
    this.blockchain = blockchain;
  }

  @Override
  public String getName() {
    return "debug_traceBlockByNumber";
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest request) {
    final Long blockNumber = parameters.required(request.getParams(), 0, Long.class);
    Optional<Hash> blockHash = this.blockchain.getBlockHashByNumber(blockNumber);
    DebugTraceBlockResult result =
        blockHash
            .map(hash -> blockTracer.trace(hash).map(DebugTraceBlockResult::new).orElse(null))
            .orElse(null);
    return new JsonRpcSuccessResponse(request.getId(), result);
  }
}
