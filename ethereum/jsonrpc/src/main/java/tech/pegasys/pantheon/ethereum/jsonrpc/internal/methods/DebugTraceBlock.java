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

import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.processor.BlockTracer;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.results.DebugTraceBlockResult;
import tech.pegasys.pantheon.ethereum.mainnet.MainnetBlockHashFunction;
import tech.pegasys.pantheon.ethereum.rlp.RLP;
import tech.pegasys.pantheon.util.bytes.BytesValue;

public class DebugTraceBlock implements JsonRpcMethod {

  private final JsonRpcParameter parameters;
  private final BlockTracer blockTracer;

  public DebugTraceBlock(final JsonRpcParameter parameters, final BlockTracer blockTracer) {
    this.parameters = parameters;
    this.blockTracer = blockTracer;
  }

  @Override
  public String getName() {
    return "debug_traceBlock";
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest request) {
    final String input = parameters.required(request.getParams(), 0, String.class);
    final Block block =
        Block.readFrom(
            RLP.input(BytesValue.fromHexString(input)), MainnetBlockHashFunction::createHash);
    DebugTraceBlockResult result =
        blockTracer.trace(block).map(DebugTraceBlockResult::new).orElse(null);
    return new JsonRpcSuccessResponse(request.getId(), result);
  }
}
