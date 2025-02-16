const ReconnectHandler = (() => {
    const retryIntervalMs = 2000; // Wait 2 sec before retrying
    const maxRetryAttempts = 1000;  // Retry for 1 minute max
    let hasRestarted = false; // ✅ Flag to track first peer open event

    function restartPeer() {
        Android.onUpdate("🔄 Restarting peer with the same ID...");
    
        // 🔴 Step 1: Fully destroy old peer instance if it exists
        if (peer) {
            try {
                Android.onUpdate("🛑 Destroying old peer instance...");
                peer.destroy(); // ✅ Properly destroy old peer
                peer = null;    // ✅ Set to null to ensure cleanup
                Android.onUpdate("✅ Old peer instance destroyed.");
            } catch (error) {
                Android.onUpdate(`❌ Error destroying peer: ${error.message}`);
            }
        }
    
        isPeerOpen = false;
        isAllConnectionsOpen = false;
        hasRestarted = false;
    
        let retryCount = 0;
        let loopCount = 0;
    
        const retryInterval = setInterval(() => {
            Android.onUpdate(`Entered loop: ${++loopCount}`);
    
            // 🔴 Step 2: Ensure peer is null before creating a new one
            if (!peer) {
                if (retryCount >= maxRetryAttempts) {
                    Android.onUpdate("❌ Peer initialization failed after max attempts.");
                    clearInterval(retryInterval);
                    Android.onPeerRetryLimitReached();
                    return;
                }
    
                Android.onUpdate(`🔄 Retrying Peer Initialization (${retryCount + 1}/${maxRetryAttempts})...`);
    
                if (hasRestarted) {
                    hasRestarted = false; // ✅ Prevent multiple calls
                    Android.onPeerError();
                }
    
                try {
                    peer = new Peer(`${peerBranch}${peerId}`);
    
                    peer.on('disconnected', () => {
                        if(!peer){
                            return;
                        }
                        Android.onUpdate("⚠️ Peer disconnected. Restarting...");
                        peer.destroy();
                        peer = null;
                    });
    
                    peer.on('error', (error) => {
                        if(!peer){
                            return;
                        }
                        Android.onUpdate(`❌ Peer error: ${error.message}`);
                        peer.destroy();
                        peer = null;
                    });
    
                    // ✅ Attach events only to the new peer instance
                    handlePeerEvents(peer);
                } catch (error) {
                    Android.onUpdate(`❌ Error initializing peer: ${error.message}`);
                }
    
                retryCount++;
            }
    
            // 🔴 Step 3: If peer is open and waiting for connections
            if (peer && peer.open && !isAllConnectionsOpen) {
                Android.onUpdate("✅ Peer successfully opened, waiting for connections...");
    
                if (!hasRestarted) {
                    hasRestarted = true;
                    Android.onRestartPeer();
                }
    
                if (!isReceiver && remoteId) {
                    Android.onUpdate(`🔄 Reconnecting to remote peer: ${remoteId}`);
                    connectRemotePeer(remoteId, { message: "Reconnecting..." }, true);
                } else {
                    Android.onUpdate("📥 In receiver mode. Waiting for incoming connections...");
                }
            }
    
            // 🔴 Step 4: If all connections are restored, stop the loop
            if (peer && peer.open && isAllConnectionsOpen) {
                Android.onUpdate("✅ All connections successfully restored.");
                clearInterval(retryInterval);
                Android.onRestartConnection();
            }
        }, retryIntervalMs); // Retry every 2 seconds
    }
    
        
    


    return { restartPeer };
})();
