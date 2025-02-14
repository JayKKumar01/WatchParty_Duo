function closeConnectionAndDestroyPeer() {
    // ✅ Send destroy signal before closing
    if (SignalHandler.isConnectionOpen()) {
        Android.onUpdate("📡 Sending destroy signal before closing peer...");
        SignalHandler.sendDestroySignal();
    }

    // ✅ Close all active connections
    closePeerAndConnections();

    // ✅ Destroy the peer safely
    if (peer) {
        peer.destroy();
        console.log("🛑 Peer destroyed.");
    }

    // ✅ Reset variables
    isPeerOpen = false;
    remoteId = null;
    mainConnection = null;
    peer = null;

    Android.onUpdate("✅ Peer cleanup completed.");
}

// ✅ Close All Connections
function closePeerAndConnections() {
    Android.onUpdate("⚠️ Closing all active connections...");

    if (mainConnection?.open) {
        Android.onUpdate("🔗 Closing main connection...");
        mainConnection.close();
        mainConnection = null;
    }

    if (lastSeenConn?.open) {
        Android.onUpdate("👀 Closing lastSeen connection...");
        lastSeenConn.close();
        lastSeenConn = null;
    }

    if (signalConn?.open) {
        Android.onUpdate("📡 Closing signal connection...");
        signalConn.close();
        signalConn = null;
    }

    Android.onUpdate("✅ All connections closed.");

    // ✅ Destroy the peer
    if (peer) {
        Android.onUpdate("🛑 Destroying peer...");
        peer.destroy();
        peer = null;
        isPeerOpen = false;
    }
}
