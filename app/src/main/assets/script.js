function onConnectionOpen(){
}
// Modified to handle PeerJS data channel limitations
let buffer = "";
const chunkSize = 16384; // 16KB chunks (PeerJS limit)

function receiveFromAndroid(batch) {
    // Stringify and split into chunks for PeerJS
    const jsonStr = JSON.stringify(batch);
    sendData(jsonStr);
    
    // for (let i = 0; i < jsonStr.length; i += chunkSize) {
    //     const chunk = jsonStr.slice(i, i + chunkSize);
    //     sendData(chunk);
    // }
}

function handleData(data) {
    // Reassemble chunks
    buffer += data;
    
    try {
        // Try parsing whenever we receive data
        const completeData = JSON.parse(buffer);
        const data = JSON.stringify(completeData);
        Android.onBatchReceived(data);
        buffer = ""; // Reset buffer after successful parse
    } catch (e) {
        // Incomplete data, wait for more chunks
    }
}
