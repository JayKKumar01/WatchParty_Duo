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
            if (data.type === "message") {
                handleMessage(data);
            } else if (data.type === "destroyPeer") {
                console.warn("🛑 Destroy signal received. Closing connection...");
                Android.onConnectionClosed();
                closePeerAndConnections(); // ✅ Destroy peer on signal
            } else if (data.type === "playback"){
                Android.onPlaybackUpdate(data.data);
            }
        });

        signalConn.on('close', () => {
            console.warn("⚠️ Signal connection closed.");
            stop();
        });

        signalConn.on('error', (err) => {
            console.error("❌ Signal connection error:", err);
        });
    }

    // Send a message
    function sendMessage(content) {
        if (signalConn && signalConn.open) {
            signalConn.send({ type: "message", content });
            console.log("📤 Message sent:", content);
        } else {
            console.warn("⚠️ Signal connection is not open. Cannot send message.");
        }
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

    function receivePlaybackUpdate(data){
        const playbackData = {
            type: "playback",
            data: JSON.stringify(data)
        };
        if (signalConn && signalConn.open) {
            
            signalConn.send(playbackData);
            console.log("📤 Playback update sent:", payload);
        } else {
            console.warn("⚠️ Signal connection is not open. Cannot send playback update.");
        }
    }

    // Handle incoming messages
    function handleMessage(data) {
        console.log("📥 Received message:", data.content);
        Android.onMessageReceived(data.content);
    }

    // Stop signal connection
    function stop() {
        if (signalConn) {
            signalConn.close();
            signalConn = null;
        }
    }

    function isConnectionOpen() {
        return signalConn && signalConn.open;
    }

    return { initSignalConnection, setupSignalConnection, isConnectionOpen, sendMessage, sendDestroySignal, stop, receivePlaybackUpdate };
})();

// **🔹 Expose to Android**
window.receivePlaybackUpdate = SignalHandler.receivePlaybackUpdate;