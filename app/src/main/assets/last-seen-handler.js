const LastSeenHandler = (() => {
    let lastSeen = Date.now();
    let lastConnectionStatus = false;
    let lastSeenConn = null;
    let isLastSeenConnected = false;
    let connectionCheckInterval = null;

    // Initialize LastSeen connection
    function initLastSeenConnection(peer, targetPeerId) {
        if (!peer || !targetPeerId) return;

        console.log("🔗 Establishing LastSeen connection...");
        lastSeenConn = peer.connect(peerBranch + targetPeerId, {
            reliable: false,
            metadata: { type: "lastSeen" } // ✅ Pass type in metadata
        });

        setupLastSeenConnection(lastSeenConn);
    }

    // Setup last-seen-specific connection
    function setupLastSeenConnection(connection) {
        lastSeenConn = connection;

        lastSeenConn.on('open', () => {
            Android.onUpdate("Last Seen opened: "+Date.now());
            isLastSeenConnected = true;
            console.log("✅ LastSeen connection established.");
            start();
        });

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

            if (lastConnectionStatus !== isAlive) {
                console.log("📡 Connection status changed:", isAlive);
                Android.onConnectionAlive(isAlive);
                lastConnectionStatus = isAlive;
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
        lastConnectionStatus = false;
    }

    function close(){
        lastSeenConn.close();
    }

    function isConnectionOpen(){
        return lastSeenConn && lastSeenConn.open;
    }

    // Send lastSeen updates separately
    function sendLastSeen() {
        if (lastSeenConn && lastSeenConn.open) {
            lastSeenConn.send({ type: "lastSeen" });
        }
    }

    return { initLastSeenConnection, setupLastSeenConnection , isConnectionOpen, close};
})();
