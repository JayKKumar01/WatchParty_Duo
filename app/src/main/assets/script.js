const PREFIX = "JayKKumar01-WatchParty-Duo-";
const RANDOM_ID = Math.floor(100000 + Math.random() * 900000);

//utility functions

function getTodayDate() {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, "0");
    const day = String(today.getDate()).padStart(2, "0");
    return `${year}${month}${day}`;
}

function byteArrayToString(byteArray) {
    const decoder = new TextDecoder('utf-8');
    const utf8Text = decoder.decode(new Uint8Array(byteArray));
    return utf8Text;
}

// Generate peer connection ID using prefix and random ID
const peerBranch = `${PREFIX}${getTodayDate()}-`;
const peerId = `${peerBranch}${RANDOM_ID}`;
const peer = new Peer(peerId);

let remoteId;

let conn;

// Handle peer-related events like connection and disconnection
function handlePeer() {

    peer.on('open', () => {
        Android.onPeer(RANDOM_ID);
    });

    peer.on('connection', (incomingConn) => {
        setupConnection(incomingConn);
    });
    peer.on('disconnected', () => Android.onDisconnected(remoteId));
    peer.on('close', () => Android.onClose(remoteId));
}

// Setup the connection to the other peer
function setupConnection(connection) {
    conn = connection;
    remoteId = conn.peer.split('-').pop();
    conn.on('open', () => {
        Android.onConnected(remoteId);
    });

    // Handle data transfer
    conn.on('data', handleData);
    // conn.on('close', () => appendLog("Data connection closed."));
    // conn.on('error', (err) => appendLog("Data connection error: " + err));
}

function handleData(data) {
    if (data.type === 'message') {
        // Android.showMessage(data.id, data.name, data.message, data.millis);
    }
    else if (data.type === 'audioFile') {
        Android.readAudioFile(data.id, data.bytes, data.read, data.millis, data.loudness);
    }
}

function connect(peerId) {
    const targetPeerId = byteArrayToString(peerId);
    if (targetPeerId !== '') {
        let connection = peer.connect(peerBranch + targetPeerId, { reliable: true });
        setupConnection(connection);
    }
}


function sendAudioFile(bytes, read, millis, loudness){
    var data = {
        type: 'audioFile',
        id: RANDOM_ID,
        bytes: bytes,
        read: read,
        millis: millis,
        loudness: loudness
    };

    if (conn && conn.open) {
        conn.send(data);
    }
}



const playerTypes = ["Exo Player", "YouTube Player"];
let playerType = playerTypes[0]; // Default value is "Exo Player"

function setPlayerType(index) {
    if (index === 0 || index === 1) {
        playerType = playerTypes[index];
    } else {
        console.error("Invalid index. Please provide 0 for 'Exo Player' or 1 for 'YouTube Player'.");
    }
}