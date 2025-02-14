let peerId = getRandomId();
let remoteId = null;
let targetPeerId = null;

let peer = null;
let isPeerOpen = false;


let mainConnection = null;
let lastSeenConn = null;
let signalConn = null;


let isClosingForRestart = false;



let count = 0;

let isReceiver = null;

// Initialize Peer
function initPeer() {
    const id = `${peerBranch}${peerId}`;
    peer = new Peer(id);
    handlePeer(peer);

    // Close peer if it does not open within 9 seconds
    setTimeout(() => {
        if (!isPeerOpen) {
            peer.destroy();
            Android.onUpdate("⚠️ Peer failed to open, closing peer.");
        }
    }, 9000);
}

// Handle Peer Events
function handlePeer(peer) {
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
        Android.onUpdate("✅ Connection confirmation initiated.");
    });
    

    peer.on('disconnected', () => {
        Android.onUpdate("⚠️ Peer disconnected.");
        Android.onPeerDisconnected(peerId);
    });

    peer.on('error', (err) => {
        Android.onUpdate(`❌ Peer error: ${err}`);
    });
}


function confirmConnection(incomingConn) {

    incomingConn.on('open', () => {
        Android.onUpdate("✅ Connection opened successfully.");

        const connType = incomingConn.metadata?.type || "main";
        Android.onUpdate(`📌 Connection type detected: ${connType}`);

        if (connType === "main") {
            mainConnection = incomingConn;
            Android.onUpdate("🔗 Assigned as main connection.");
        } else if (connType === "lastSeen") {
            lastSeenConn = incomingConn;
            Android.onUpdate("👀 Assigned as lastSeen connection.");
        }

        if ((mainConnection && mainConnection.open) && (lastSeenConn && lastSeenConn.open)) {
            Android.onUpdate("🔄 Both connections are active. Setting up...");
            setupConnection(mainConnection);
            LastSeenHandler.setupLastSeenConnection(lastSeenConn);
            Android.onUpdate("✅ Connections setup complete.");
        } else {
            Android.onUpdate("⚠️ Waiting for both connections to be active.");
        }
    });

    incomingConn.on('close', () => {
        Android.onUpdate("❌ Connection closed.");
    });

    incomingConn.on('error', (err) => {
        Android.onUpdate(`⚠️ Connection error: ${err.message}`);
    });
}







// Setup Connection
function setupConnection(connection) {
    Android.onUpdate("🔧 Initializing connection setup...");

    mainConnection = connection;
    Android.onUpdate("✅ Main connection assigned.");

    if (isReceiver) {
        Android.onUpdate("📥 Device is in receiver mode.");
        
        if (!isClosingForRestart) {
            Android.onUpdate("📡 Receiving metadata...");
            Android.onMetaData(mainConnection.metadata.metadata);
        } else {
            Android.onUpdate("🔄 Metadata reception skipped due to restart.");
        }
    }

    remoteId = mainConnection.peer.replace(peerBranch, "");
    Android.onUpdate(`🔗 Connection established with: ${remoteId}`);

    Android.onConnectionOpen(peerId, remoteId, count++);

    mainConnection.on('data', (data) => {
        handleData(data);
    });

    mainConnection.on('close', () => {
        if (!isClosingForRestart) {
            Android.onUpdate("⚠️ Connection closed.");
            Android.onConnectionClosed();
        } else {
            Android.onUpdate("🔄 Connection closed but restart in progress.");
        }
    });

    mainConnection.on('error', (err) => {
        Android.onUpdate(`❌ Connection error: ${err.message}`);
    });

    Android.onUpdate("✅ Connection setup complete.");
}


// Connect to Remote Peer
function connectRemotePeer(otherPeerId, metadataJson) {
    targetPeerId = byteArrayToString(otherPeerId);
    if (targetPeerId !== '') {
        try {
            LastSeenHandler.initLastSeenConnection(peer, targetPeerId);

            const metadata = JSON.stringify(metadataJson);

            Android.onUpdate(`🔄 Connecting to remote peer: ${targetPeerId}`);

            const connection = peer.connect(peerBranch + targetPeerId, {
                reliable: true,
                metadata: { type: 'main', metadata: metadata }
            });

            confirmConnection(connection);

            // Check if the connection is still closed after 4 seconds
            setTimeout(() => {
                if (!connection.open) {
                    Android.onUpdate("⚠️ Connection did not open. Closing.");
                    connection.close();
                }
            }, 4000);
        } catch (error) {
            Android.onUpdate(`❌ Failed to parse metadata JSON: ${error}`);
        }
    } else {
        Android.onUpdate("⚠️ Invalid target peer ID.");
    }
}

// Send Data
function sendData(data) {
    if (mainConnection && mainConnection.open) {
        mainConnection.send(data);
    } else {
        Android.onUpdate("⚠️ Connection is not open. Unable to send data.");
    }
}

// Handle Incoming Data
function handleData(data) {
    if (data.type === "rawData") {
        handleRawData(data);
    } else {
        Android.onUpdate(`📥 Received unknown data type: ${JSON.stringify(data)}`);
    }
}

// Close Connection
function closeMainConnection() {
    if (mainConnection && mainConnection.open) {
        isClosingForRestart = true;
        targetPeerId = remoteId; // Store for reconnection
        Android.onUpdate(`❌ Closing main connection with ${targetPeerId}`);
        mainConnection.close();
        mainConnection = null;
    } else {
        Android.onUpdate("⚠️ No active connection to close.");
    }
}
