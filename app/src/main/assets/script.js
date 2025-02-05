function onConnectionOpen(){
}

function receiveFromAndroid(batch) {
    sendData(JSON.stringify(batch));
}

function handleData(data) {
    Android.onBatchReceived(data);
}
