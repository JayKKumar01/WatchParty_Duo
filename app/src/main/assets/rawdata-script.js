// script for handling raw data

function receiveFromAndroid(batch) {
    const data = {
        type: "rawData",
        batch: JSON.stringify(batch)
    };
    sendData(data);
}

function handleRawData(data){
    Android.onBatchReceived(data.batch);
}
