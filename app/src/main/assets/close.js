function closeConnectionAndDestroyPeer() {
    // ✅ Send destroy signal before closing
    if (SignalHandler.isConnectionOpen()) {
        Android.onUpdate("📡 Sending destroy signal before closing peer...");
        SignalHandler.sendDestroySignal();
    }
}

function cleanupPeer(){
    // ✅ Destroy the peer safely
    if (peer) {
        peer.destroy();
        console.log("🛑 Peer destroyed.");
    }
    Android.onUpdate("✅ Peer cleanup completed.");
}