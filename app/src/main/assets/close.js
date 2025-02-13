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