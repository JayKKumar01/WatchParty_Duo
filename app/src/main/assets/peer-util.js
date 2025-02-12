let peerId = getRandomId();
let remoteId = null;

let peer = null;
let conn = null;

let isPeerOpen = false;
let isConnectionOpen = false;

let count = 0;

//initPeer

function initPeer() {
    const id = `${peerBranch}${peerId}`;
    peer = new Peer(id);
    handlePeer(peer);
    // Close peer if it does not open within 5 seconds
    setTimeout(() => {
        if (!isPeerOpen) {
            peer.destroy();
            console.warn("Peer failed to open, closing peer.");
        }
    }, 9000);
}

// Handle peer-related events like connection and disconnection
function handlePeer(peer) {
    peer.on('open', () => {
        isPeerOpen = true;
        peerId = peer.id.replace(peerBranch, ""); // Remove the prefix
        Android.onPeerOpen(peerId);
    });

    peer.on('connection', (incomingConn) => {
        setupConnection(incomingConn);
    });

    peer.on('disconnected', () => {
        Android.onPeerDisconnected(peerId);
    });

    peer.on('error', (err) => {
        console.error("Peer error:", err);
    });
}

// Setup the connection to the other peer
function setupConnection(connection) {
    conn = connection;

    conn.on('open', () => {
        isConnectionOpen = true;
        remoteId = conn.peer.replace(peerBranch, ""); // Remove the prefix
        Android.onConnectionOpen(peerId, remoteId, count++);
        onConnectionOpen();
    });

    conn.on('data', (data) => {
        handleData(data);
    });

    conn.on('close', () => {
        Android.onConnectionClosed();
    });

    conn.on('error', (err) => {
        console.error("Connection error:", err);
    });
}

function connect(otherPeerId) {
    const targetPeerId = byteArrayToString(otherPeerId);
    if (targetPeerId !== '') {
        const connection = peer.connect(peerBranch + targetPeerId, { reliable: true });
        setupConnection(connection);
        // Check if the connection is still closed after 5 seconds
        setTimeout(() => {
            if (!connection.open) {
                connection.close();
            }
        }, 4000);
    }
}


function connectRemotePeer(otherPeerId, metadataJson) {
    const targetPeerId = byteArrayToString(otherPeerId);
    if (targetPeerId !== '') {
        try {
            const metadata = JSON.stringify(metadataJson);
            
            // Establish a connection with metadata
            const connection = peer.connect(peerBranch + targetPeerId, { 
                reliable: true, 
                metadata: metadata // Pass metadata while connecting 
            });

            setupConnection(connection);

            // Check if the connection is still closed after 4 seconds
            setTimeout(() => {
                if (!connection.open) {
                    connection.close();
                }
            }, 4000);
        } catch (error) {
            console.error("Failed to parse metadata JSON:", error);
        }
    }
}



function sendData(data) {
    if (conn && conn.open) {
        conn.send(data);
    } else {
        console.warn("Connection is not open. Unable to send feed.");
    }
}

function closeConnectionAndDestroyPeer() {
    if (conn) {
        conn.close();
        console.log("Connection closed.");
    }
    
    if (peer) {
        peer.destroy();
        console.log("Peer destroyed.");
    }

    isPeerOpen = false;
    isConnectionOpen = false;
    remoteId = null;
    conn = null;
    peer = null;
}
