const PREFIX = "JayKKumar01-WatchParty-Duo-";

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
// Generate peer connection ID using prefix and random ID
const peerBranch = `${PREFIX}${getTodayDate()}-`;

let peerId = null;
let peer = null;

let remoteId = null;
let conn = null;

let nextPeerId = null;
let nextRemoteId = null;

//initPeer

function initPeer(){
    peerId = getRandomId();
    const id = `${peerBranch}${peerId}`;
    peer = new Peer(id);

    handlePeer(peer);
}

// Handle peer-related events like connection and disconnection
function handlePeer(peer) {
    peer.on('open', () => {
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
    remoteId = conn.peer.split('-').pop();

    conn.on('open', () => {
        nextPeerId = getRandomId();
        conn.send({ type: 'nextRemoteId', nextRemoteId: nextPeerId });
        Android.onConnectionOpen(remoteId);
    });

    conn.on('data', handleData);

    conn.on('close', () => {
        Android.onConnectionClose(remoteId);
    });

    conn.on('error', (err) => {
        console.error("Connection error:", err);
    });
}

function handleData(data) {
    if (data.type === 'nextRemoteId') {
        nextRemoteId = data.nextRemoteId;
    } else if (data.type === 'message') {
        // Android.showMessage(data.id, data.name, data.message, data.millis);
    } else if (data.type === 'audioFile') {
        Android.readAudioFile(data.id, data.bytes, data.read, data.millis, data.loudness);
    }
}

function connect(otherPeerId) {
    const targetPeerId = byteArrayToString(otherPeerId);
    if (targetPeerId !== '') {
        const connection = peer.connect(peerBranch + targetPeerId, { reliable: true });
        setupConnection(connection);
    }
}

function sendAudioFile(bytes, read, millis, loudness) {
    const data = {
        type: 'audioFile',
        id: peerId,
        bytes: bytes,
        read: read,
        millis: millis,
        loudness: loudness
    };

    if (conn && conn.open) {
        conn.send(data);
    } else {
        console.warn("Connection is not open. Unable to send audio file.");
    }
}