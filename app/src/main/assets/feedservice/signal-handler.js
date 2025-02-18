const SignalHandler = (() => {
    let signalConn = null;

    // Initialize Signal Connection
    function initSignalConnection(peer, targetPeerId) {
        if (!peer || !targetPeerId) return;

        console.log("📡 Establishing Signal connection...");
        signalConn = peer.connect(peerBranch + targetPeerId, {
            reliable: true,
            metadata: { type: "signal" } // ✅ Pass type in metadata
        });

        confirmConnection(signalConn);
    }

    // Setup signal-specific connection
    function setupSignalConnection(connection) {
        signalConn = connection;
        Android.onUpdate("📡 Signal connection opened at: " + Date.now());
        console.log("✅ Signal connection established.");

        signalConn.on('data', (data) => {
            if (data.type === "destroyPeer") {
                close();
                console.warn("🛑 Destroy signal received. Closing connection...");
                Android.onConnectionClosed();
                cleanupPeer();
            } else if (data.type === "playback") {
                Android.onPlaybackUpdate(data.data);
            }
        });
    }

    function close() {
        signalConn.close();
        signalConn = null;
    }

    // Send a destroy signal
    function sendDestroySignal() {
        if (signalConn && signalConn.open) {
            signalConn.send({ type: "destroyPeer", message: "Close your peer." });
            console.log("📡 Sent destroy peer signal.");
        } else {
            console.warn("⚠️ Signal connection is not open. Cannot send destroy signal.");
        }
    }

    function receivePlaybackUpdate(data) {
        const playbackData = {
            type: "playback",
            data: JSON.stringify(data)
        };
        if (signalConn && signalConn.open) {
            signalConn.send(playbackData);
            console.log("📤 Playback update sent:", playbackData);
        } else {
            console.warn("⚠️ Signal connection is not open. Cannot send playback update.");
        }
    }

    function isConnectionOpen() {
        return signalConn && signalConn.open;
    }

    return { initSignalConnection, setupSignalConnection, isConnectionOpen, sendDestroySignal, receivePlaybackUpdate };
})();

// **🔹 Expose to Android**
window.receivePlaybackUpdate = SignalHandler.receivePlaybackUpdate;