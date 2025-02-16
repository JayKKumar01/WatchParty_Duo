const ReconnectHandler = (() => {
    let restartCount = 0;
    const retryIntervalMs = 2000; // Wait 2 sec before retrying
    const maxRetryAttempts = 1000;  // Retry for 1 minute max
    let hasRestarted = false; // ✅ Flag to track first peer open event

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

        restartCount++; // 🔄 Increment restart count

        // ✅ Generate a new peer ID to avoid "peer ID already taken" error
        let newPeerId = `${peerId}-${restartCount}`;
        Android.onUpdate(`🔄 Assigning new Peer ID: ${newPeerId}`);

        // ✅ Update remoteId similarly (assuming the remote peer follows same logic)
        let newRemoteId = `${remoteId}-${restartCount}`;
        Android.onUpdate(`🔄 Assigning new Remote ID: ${newRemoteId}`);

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
        hasRestarted = false; // ✅ Reset flag before retry loop starts

        let retryCount = 0;
        let loopCount = 0;

        let newPeer = null;
        const retryInterval = setInterval(() => {
            Android.onUpdate("Entered loop: "+ ++loopCount);
            if (!newPeer) {

                Android.onUpdate(`🔄 Retrying Peer Initialization (${retryCount + 1}/${maxRetryAttempts})...`);

                if (hasRestarted) {
                    hasRestarted = false; // ✅ Prevent multiple calls
                    Android.onPeerError();
                }
                try {
                    newPeer = new Peer(`${peerBranch}${newPeerId}`);

                    newPeer.on('disconnected', () => {
                        try {
                            Android.onUpdate("🛑 Destroying old peer instance...");
                            newPeer.destroy();
                            newPeer = null;
                            Android.onUpdate("✅ Old peer instance destroyed.");
                        } catch (error) {
                            Android.onUpdate(`❌ Error destroying peer: ${error.message}`);
                        }
                    });
                    // handlePeerEvents(peer);
                } catch (error) {
                    Android.onUpdate(`❌ Error initializing peer: ${error.message}`);
                }

                retryCount++;
                if (retryCount >= maxRetryAttempts) {
                    Android.onUpdate("❌ Peer initialization failed after max attempts.");
                    clearInterval(retryInterval);
                    Android.onPeerRetryLimitReached();
                }
            }

            if ((newPeer && newPeer.open) && !isAllConnectionsOpen) {
                Android.onUpdate("✅ Peer successfully opened, waiting for connections...");

                // ✅ Call Android.onRestartPeer() only once when peer opens for the first time
                if (!hasRestarted) {
                    hasRestarted = true; // ✅ Prevent multiple calls
                    Android.onRestartPeer();
                }

                if (!isReceiver && newRemoteId) {
                    Android.onUpdate(`🔄 Reconnecting to remote peer: ${newRemoteId}`);
                    connectRemotePeer(newRemoteId, { message: "Reconnecting..." }, true);
                } else {
                    Android.onUpdate("📥 In receiver mode. Waiting for incoming connections...");
                }

                retryCount++;
                if (retryCount >= maxRetryAttempts) {
                    Android.onUpdate("❌ Peer initialization failed after max attempts.");
                    clearInterval(retryInterval);
                    Android.onPeerRetryLimitReached();
                }
            }

            if (newPeer && newPeer.open && isAllConnectionsOpen) {
                peer = newPeer;
                handlePeerEvents(peer);
                Android.onUpdate("✅ All connections successfully restored.");
                clearInterval(retryInterval);
                Android.onRestartConnection();
            }
        }, retryIntervalMs);  // Retry every 2 seconds        
    }

    return { restartPeer };
})();
