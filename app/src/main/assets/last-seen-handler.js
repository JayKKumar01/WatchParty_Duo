const LastSeenHandler = (() => {
    let lastSeen = Date.now();
    let lastSeenConn = null;
    let connectionCheckInterval = null;
    let lastIsAliveStatus = null;

    // Initialize LastSeen connection
    function initLastSeenConnection(peer, targetPeerId) {
        if (!peer || !targetPeerId) return;

        console.log("🔗 Establishing LastSeen connection...");
        lastSeenConn = peer.connect(peerBranch + targetPeerId, {
            reliable: true,
            metadata: { type: "lastSeen" } // ✅ Pass type in metadata
        });

        confirmConnection(lastSeenConn);
    }

    // Setup last-seen-specific connection
    function setupLastSeenConnection(connection) {
        lastSeenConn = connection;
        lastIsAliveStatus = null;
        
        Android.onUpdate("Last Seen opened: " + Date.now());
        console.log("✅ LastSeen connection established.");
        start();

        lastSeenConn.on('data', (data) => {
            if (data.type === "lastSeen") {
                update();
            }
        });

        lastSeenConn.on('close', () => {
            console.warn("⚠️ LastSeen connection closed.");
            stop();
        });

        lastSeenConn.on('error', (err) => {
            console.error("LastSeen connection error:", err);
        });
    }

    // Start monitoring last-seen status
    function start() {
        if (connectionCheckInterval) return;

        lastSeen = Date.now();

        console.log("🔄 Starting LastSeenHandler...");
        connectionCheckInterval = setInterval(() => {
            sendLastSeen(); // ✅ Send via separate channel

            const timeSinceLastSeen = Date.now() - lastSeen;
            const isAlive = timeSinceLastSeen <= 1500; // ✅ Faster detection

            // ✅ Only notify Android if status changed
            if (lastIsAliveStatus !== isAlive) {
                Android.onConnectionAlive(isAlive);
                lastIsAliveStatus = isAlive;
            }

            // ✅ If connection is lost, restart peer
            if (!isAlive) {
                Android.onUpdate("⛔ Main connection lost, restarting peer...");
                restartPeer();
                stop();
            }


        }, 1000); // ✅ Check every second
    }

    // Update last-seen timestamp
    function update() {
        lastSeen = Date.now();
    }

    // Stop last-seen monitoring
    function stop() {
        if (connectionCheckInterval) {
            clearInterval(connectionCheckInterval);
            connectionCheckInterval = null;
        }
        close();
    }

    function close() {
        lastSeenConn.close();
        lastSeenConn = null;
    }

    function isConnectionOpen() {
        return lastSeenConn && lastSeenConn.open;
    }

    // Send lastSeen updates separately
    function sendLastSeen() {
        if (lastSeenConn && lastSeenConn.open) {
            lastSeenConn.send({ type: "lastSeen" });
        }
    }

    return { initLastSeenConnection, setupLastSeenConnection, isConnectionOpen, close };
})();
