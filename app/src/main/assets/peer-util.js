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
        peerId = peer.id.replace(peerBranch, "").split("-")[0]; 
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

    remoteId = mainConnection.peer.replace(peerBranch, "").split("-")[0]; 
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