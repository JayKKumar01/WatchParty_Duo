let peerId = getRandomId();
let remoteId = null;

let peer = null;
let conn = null;

let isPeerOpen = false;
let isConnectionOpen = false;

//initPeer

function initPeer() {
    const id = `${peerBranch}${peerId}`;
    peer = new Peer(id);
    handlePeer(peer);
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
        Android.onConnectionOpen(peerId, remoteId);
        onConnectionOpen();
    });

    conn.on('data', (data) => {
        handleData(data);
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

function sendData(data) {
    if (conn && conn.open) {
        conn.send(data);
    } else {
        console.warn("Connection is not open. Unable to send feed.");
    }
}