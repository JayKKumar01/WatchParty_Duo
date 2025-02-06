const PREFIX = "JayKKumar01-WatchParty-Duo-Constant-Transfer-Rate-";
// Generate peer connection ID using prefix and random ID
const peerBranch = `${PREFIX}${getTodayDate()}-`;

const logTextarea = document.getElementById('logTextarea');

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

// Add function to update the textarea
function updateLog(log) {
    const currentTime = new Date().toLocaleTimeString();
    logTextarea.value += `[${currentTime}]: ${log}\n`;

    // Automatically scroll to the bottom
    logTextarea.scrollTop = logTextarea.scrollHeight;
}