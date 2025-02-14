const ReconnectHandler = (() => {
    function resetAllConnections() {
        Android.onUpdate("⚠️ Resetting peer and all connections...");

        // ✅ Close all active connections
        if (mainConnection?.open) {
            Android.onUpdate("🔗 Closing main connection...");
            mainConnection.close();
        } else {
            Android.onUpdate("⚠️ No active main connection to close.");
        }
        mainConnection = null;

        if (lastSeenConn?.open) {
            Android.onUpdate("👀 Closing lastSeen connection...");
            lastSeenConn.close();
        } else {
            Android.onUpdate("⚠️ No active lastSeen connection to close.");
        }
        lastSeenConn = null;

        if (signalConn?.open) {
            Android.onUpdate("📡 Closing signal connection...");
            signalConn.close();
        } else {
            Android.onUpdate("⚠️ No active signal connection to close.");
        }
        signalConn = null;

        Android.onUpdate("✅ All connections closed.");
    }

    function restartPeer() {
        resetAllConnections();
        Android.onUpdate("🔄 Restarting peer with the same ID...");

        // ✅ Destroy existing peer safely
        if (peer) {
            try {
                Android.onUpdate("🛑 Destroying old peer instance...");
                peer.destroy();
                Android.onUpdate("✅ Old peer instance destroyed.");
            } catch (error) {
                Android.onUpdate(`❌ Error destroying peer: ${error.message}`);
            }
        }

        peer = null;
        isPeerOpen = false;
        isAllConnectionsOpen = false;

        let retryCount = 0;
        const retryInterval = setInterval(() => {
            if (!isPeerOpen) {
                Android.onUpdate(`🔄 Initializing peer (Attempt ${retryCount + 1})...`);
                try {
                    peer = new Peer(`${peerBranch}${peerId}`);
                    handlePeerEvents(peer);
                } catch (error) {
                    Android.onUpdate(`❌ Error initializing peer: ${error.message}`);
                }

                retryCount++;
                if (retryCount >= 1000) {
                    Android.onUpdate("❌ Peer initialization failed after 1000 attempts.");
                    clearInterval(retryInterval);
                }
            } 

            if (isPeerOpen && !isAllConnectionsOpen) {
                Android.onUpdate("✅ Peer successfully opened, waiting for connections...");

                // ✅ Keep retrying connection to remote peer
                if (!isReceiver && remoteId) {
                    Android.onUpdate(`🔄 Reconnecting to remote peer: ${remoteId}`);
                    connectRemotePeer(remoteId, { message: "Reconnecting..." }, true);
                } else {
                    Android.onUpdate("📥 In receiver mode. Waiting for incoming connections...");
                }
            }

            // ✅ Ensure all connections are fully established before clearing the interval
            if (isPeerOpen && isAllConnectionsOpen) {
                Android.onUpdate("✅ All connections successfully restored.");
                clearInterval(retryInterval);
            }
        }, 1000);
    }

    return { restartPeer };
})();
