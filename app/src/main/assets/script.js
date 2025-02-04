const PREFIX = "JayKKumar01-WatchParty-Duo-Multiple-PeerIds-";

// Utility functions
function getTodayDate() {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, "0");
    const day = String(today.getDate()).padStart(2, "0");
    return `${year}${month}${day}`;
}

function byteArrayToString(byteArray) {
    const decoder = new TextDecoder('utf-8');
    return decoder.decode(new Uint8Array(byteArray));
}

function getRandomId() {
    return Math.floor(100000 + Math.random() * 900000);
}

function getFileTransferId() {
    return String(Math.floor(10_000_000_000_000 + Math.random() * 9_000_000_000_000_000)); // 15-digit ID
}
// Generate peer connection ID using prefix and random ID
const peerBranch = `${PREFIX}${getTodayDate()}-`;


let peerId = getRandomId();
let peer = null;

let remoteId = null;
let conn = null;

let isPeerOpen = false;
let isConnectionOpen = false;


//initPeer

function initPeer(receivedPeerId) {
    const id = receivedPeerId ? `${peerBranch}${byteArrayToString(receivedPeerId)}` : `${peerBranch}${peerId}`;
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

    peer.on('close', () => {
        Android.onPeerClose(peerId);
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
        Android.onConnectionOpen(peerId,remoteId);
    });

    conn.on('data', (data) => {
        handleData(data);
    });

    conn.on('close', () => {
        Android.onConnectionClose(remoteId);
    });

    conn.on('error', (err) => {
        Android.onConnectionError(err);
        console.error("Connection error:", err);
    });
}

function connect(otherPeerId) {
    const targetPeerId = byteArrayToString(otherPeerId);
    if (targetPeerId !== '') {
        const connection = peer.connect(peerBranch + targetPeerId, { reliable: true });
        setupConnection(connection);
    }
}

function handleData(data) {
    if (data.type === 'audioFeed') {
        Android.readAudioFile(data.id, data.bytes, data.read, data.millis, data.loudness);
    } else if (data.type === 'imageFeed') {
        Android.readImageFeed(data.id, data.imageFeedBytes, data.millis);
    }
}

function sendImageFeed(imageFeedBytes, millis){
    const data = {
        type: 'imageFeed',
        id: peerId,
        imageFeedBytes: imageFeedBytes,
        millis: millis
    };

    if (conn && conn.open) {
        conn.send(data);
    } else {
        console.warn("Connection is not open. Unable to send feed.");
    }
}