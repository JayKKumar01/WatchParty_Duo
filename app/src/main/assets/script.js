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

let peerId = getRandomId();
let peer = null;

let remoteId = null;
let conn = null;

let nextPeerId = null;
let nextRemoteId = null;
let isPeerOpen = false;
let isConnectionOpen = false;

let lastSeen; // now

//initPeer


function nextPeer() {
    peerId = nextPeerId;
    remoteId = nextRemoteId;
    isPeerOpen = false;
    isConnectionOpen = false;


    let attemptCount = 0;
    const checkInterval = setInterval(() => {
        if (!isPeerOpen) {
            initPeer();
            Android.onRetryPeer(++attemptCount);
        } else {
            clearInterval(checkInterval);
        }

    }, 2000);


    
}

function initPeer() {
    const id = `${peerBranch}${peerId}`;
    peer = new Peer(id);

    handlePeer(peer);
}

// Handle peer-related events like connection and disconnection
function handlePeer(peer) {
    peer.on('open', () => {
        isPeerOpen = true;
        Android.onPeerOpen(peerId);

        if (nextPeerId !== null) {
            Android.onPeerReopened(peerId);
            let attemptCount = 0;
            const checkInterval = setInterval(() => {
                if (!isConnectionOpen) {
                    connect(remoteId, false);
                    Android.onRetryConnection(++attemptCount);
                } else {
                    clearInterval(checkInterval);
                }

            }, 2000);
        }
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
        remoteId = conn.peer.split('-').pop();
        nextPeerId = getRandomId();
        conn.send({ type: 'nextRemoteId', nextRemoteId: nextPeerId });
        Android.onConnectionOpen(remoteId);
        lastSeen = Date.now();

        // Start interval only after the connection is open
        const checkInterval = setInterval(() => {

            if (Date.now() - lastSeen > 5000) {
                clearInterval(checkInterval);
                Android.onConnectionAlive(false);
                Android.onNextPeer(nextPeerId, nextRemoteId);
                nextPeer();


            } else {
                Android.onConnectionAlive(true);
            }

            if (conn && conn.open) {
                conn.send({ type: 'checkingConnection' });
            }

        }, 3000);

        // start a timer each 3 seconds it should send type checkingConnection on handle data it should update the last seen before sending if the last seen is more than 5 seconds then Android.onConnectionAlive(false) should trigger
    });

    conn.on('data', handleData);

    conn.on('close', () => {
        Android.onConnectionClose(remoteId);
    });

    conn.on('error', (err) => {
        Android.onConnectionError(err);
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
    } else if (data.type === 'checkingConnection') {
        lastSeen = Date.now();
    }
}

function connect(otherPeerId, isByteArray) {
    const targetPeerId = isByteArray ? byteArrayToString(otherPeerId) : otherPeerId;

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