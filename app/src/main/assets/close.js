function closeConnectionAndDestroyPeer() {
    if (mainConnection) {
        mainConnection.close();
        console.log("Connection closed.");
    }
    
    if (peer) {
        peer.destroy();
        console.log("Peer destroyed.");
    }

    isPeerOpen = false;
    
    remoteId = null;
    mainConnection = null;
    peer = null;
}