package net.consensys.pantheon.ethereum.eth.sync;

import static org.apache.logging.log4j.LogManager.getLogger;

import net.consensys.pantheon.ethereum.chain.Blockchain;
import net.consensys.pantheon.ethereum.core.BlockHeader;
import net.consensys.pantheon.ethereum.core.Hash;
import net.consensys.pantheon.ethereum.eth.manager.EthContext;
import net.consensys.pantheon.ethereum.eth.manager.EthPeer;
import net.consensys.pantheon.ethereum.eth.manager.EthPeers.ConnectCallback;
import net.consensys.pantheon.ethereum.eth.sync.tasks.GetHeadersFromPeerByHashTask;
import net.consensys.pantheon.ethereum.mainnet.ProtocolSchedule;
import net.consensys.pantheon.ethereum.p2p.wire.messages.DisconnectMessage.DisconnectReason;

import org.apache.logging.log4j.Logger;

public class ChainHeadTracker implements ConnectCallback {

  private static final Logger LOG = getLogger();

  private final EthContext ethContext;
  private final ProtocolSchedule<?> protocolSchedule;
  private final TrailingPeerLimiter trailingPeerLimiter;

  public ChainHeadTracker(
      final EthContext ethContext,
      final ProtocolSchedule<?> protocolSchedule,
      final TrailingPeerLimiter trailingPeerLimiter) {
    this.ethContext = ethContext;
    this.protocolSchedule = protocolSchedule;
    this.trailingPeerLimiter = trailingPeerLimiter;
  }

  public static void trackChainHeadForPeers(
      final EthContext ethContext,
      final ProtocolSchedule<?> protocolSchedule,
      final Blockchain blockchain,
      final SynchronizerConfiguration syncConfiguration) {
    final TrailingPeerLimiter trailingPeerLimiter =
        new TrailingPeerLimiter(
            ethContext.getEthPeers(),
            blockchain,
            syncConfiguration.trailingPeerBlocksBehindThreshold(),
            syncConfiguration.maxTrailingPeers());
    final ChainHeadTracker tracker =
        new ChainHeadTracker(ethContext, protocolSchedule, trailingPeerLimiter);
    ethContext.getEthPeers().subscribeConnect(tracker);
    blockchain.observeBlockAdded(trailingPeerLimiter);
  }

  @Override
  public void onPeerConnected(final EthPeer peer) {
    LOG.debug("Requesting chain head info for {}", peer);
    GetHeadersFromPeerByHashTask.forSingleHash(
            protocolSchedule, ethContext, Hash.wrap(peer.chainState().getBestBlock().getHash()))
        .assignPeer(peer)
        .run()
        .whenComplete(
            (peerResult, error) -> {
              if (peerResult != null && !peerResult.getResult().isEmpty()) {
                final BlockHeader chainHeadHeader = peerResult.getResult().get(0);
                peer.chainState().update(chainHeadHeader);
                trailingPeerLimiter.enforceTrailingPeerLimit();
              } else {
                LOG.debug("Failed to retrieve chain head information for " + peer, error);
                peer.disconnect(DisconnectReason.USELESS_PEER);
              }
            });
  }
}