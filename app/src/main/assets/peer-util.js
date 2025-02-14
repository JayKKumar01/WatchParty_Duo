let peerId = getRandomId();
let remoteId = null;

let peer = null;
let isPeerOpen = false;
let isAllConnectionsOpen = false;

let mainConnection = null;
let lastSeenConn = null;
let signalConn = null;

let count = 0;
let isReceiver = null;

// ✅ Initialize Peer
function initPeer() {
    peer = new Peer(`${peerBranch}${peerId}`);
    handlePeerEvents(peer);

    // Close peer if it does not open within 9 seconds
    setTimeout(() => {
        if (!isPeerOpen) {
            peer.destroy();
            Android.onUpdate("⚠️ Peer failed to open, closing.");
        }
    }, 9000);
}

// ✅ Handle Peer Events
function handlePeerEvents(peer) {
    peer.on('open', () => {
        isPeerOpen = true;
        peerId = peer.id.replace(peerBranch, "");
        Android.onPeerOpen(peerId);
        Android.onUpdate(`✅ Peer opened with ID: ${peerId}`);
    });

    peer.on('connection', (incomingConn) => {
        Android.onUpdate("🔄 Incoming connection detected.");
        isReceiver = true;
        Android.onUpdate("📥 Marked as receiver.");
        confirmConnection(incomingConn);
    });

    peer.on('disconnected', () => {
        Android.onUpdate("⚠️ Peer disconnected.");
        Android.onPeerDisconnected(peerId);
    });

    peer.on('error', (err) => {
        Android.onUpdate(`❌ Peer error: ${err.message}`);
    });
}

// ✅ Confirm & Assign Connection
function confirmConnection(incomingConn) {
    incomingConn.on('open', () => {
        const connType = incomingConn.metadata?.type || "main";
        Android.onUpdate(`📌 Connection type detected: ${connType}`);

        if (connType === "main") {
            mainConnection = incomingConn;
            Android.onUpdate("🔗 Assigned as main connection.");
        } else if (connType === "lastSeen") {
            lastSeenConn = incomingConn;
            Android.onUpdate("👀 Assigned as lastSeen connection.");
        } else if (connType === "signal") {
            signalConn = incomingConn;
            Android.onUpdate("📡 Assigned as signal connection.");
        }

        // ✅ Ensure all connections are active before setup
        if (mainConnection?.open && lastSeenConn?.open && signalConn?.open) {
            isAllConnectionsOpen = true;
            Android.onUpdate("🔄 All connections active. Setting up...");
            setupConnection(mainConnection);
            LastSeenHandler.setupLastSeenConnection(lastSeenConn);
            SignalHandler.setupSignalConnection(signalConn);
            Android.onUpdate("✅ Connections setup complete.");
        } else {
            Android.onUpdate("⚠️ Waiting for all connections to be active.");
        }
    });

    incomingConn.on('close', () => Android.onUpdate("❌ Connection closed."));
    incomingConn.on('error', (err) => Android.onUpdate(`⚠️ Connection error: ${err.message}`));
}

// ✅ Setup Main Connection
function setupConnection(connection) {
    Android.onUpdate("🔧 Initializing connection setup...");
    mainConnection = connection;

    if (isReceiver) {
        Android.onUpdate("📥 Device in receiver mode.");
        if (count === 0) {
            Android.onUpdate("📡 Receiving metadata...");
            Android.onMetaData(mainConnection.metadata.metadata);
        } else {
            Android.onUpdate("🔄 Metadata reception skipped due to restart.");
        }
    }

    remoteId = mainConnection.peer.replace(peerBranch, "");
    Android.onUpdate(`🔗 Connected with: ${remoteId}`);

    Android.onConnectionOpen(peerId, remoteId, count++);

    mainConnection.on('data', handleData);
    mainConnection.on('error', (err) => Android.onUpdate(`❌ Connection error: ${err.message}`));

    Android.onUpdate("✅ Connection setup complete.");
}

// ✅ Handle Data
function handleData(data) {
    if (data.type === "rawData") {
        handleRawData(data);
    } else {
        Android.onUpdate(`📥 Received unknown data type: ${JSON.stringify(data)}`);
    }
}


// ✅ Connect to Remote Peer
function connectRemotePeer(otherPeerId, metadataJson, isReconnect = false) {
    const targetPeerId = isReconnect ? otherPeerId: byteArrayToString(otherPeerId);
    if (!targetPeerId) return Android.onUpdate("⚠️ Invalid target peer ID.");

    try {
        LastSeenHandler.initLastSeenConnection(peer, targetPeerId);
        SignalHandler.initSignalConnection(peer, targetPeerId);

        Android.onUpdate(`🔄 Connecting to remote peer: ${targetPeerId}`);

        const connection = peer.connect(peerBranch + targetPeerId, {
            reliable: true,
            metadata: { type: "main", metadata: JSON.stringify(metadataJson) }
        });

        confirmConnection(connection);

        if(isReconnect){
            return;
        }
        // ✅ Ensure connection opens within 4 seconds
        setTimeout(() => {
            if (!connection.open) {
                Android.onUpdate("⚠️ Connection did not open. Closing.");
                connection.close();
            }
        }, 4000);
    } catch (error) {
        Android.onUpdate(`❌ Failed to parse metadata JSON: ${error}`);
    }
}

// ✅ Send Data
function sendData(data) {
    if (mainConnection?.open) {
        mainConnection.send(data);
    }
}


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
