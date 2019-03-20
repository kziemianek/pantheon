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

import tech.pegasys.pantheon.ethereum.debug.TraceOptions;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.processor.TransactionTraceParams;

import java.util.Optional;

public interface DebugTraceJsonRpcMethod {

  default TraceOptions getTraceOptions(
      final JsonRpcRequest request, final JsonRpcParameter parameters) {
    final Optional<TransactionTraceParams> transactionTraceParams =
        parameters.optional(
            request.getParams(),
            transactionTraceParamsParameterIndex(),
            TransactionTraceParams.class);
    return transactionTraceParams
        .map(TransactionTraceParams::traceOptions)
        .orElse(TraceOptions.DEFAULT);
  }

  default int transactionTraceParamsParameterIndex() {
    return 1;
  }
}
