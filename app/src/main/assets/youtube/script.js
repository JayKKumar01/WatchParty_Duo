let player;
let lastPosition = 0;
let lastEvent = null; // Track the last event state

function onYouTubeIframeAPIReady() {
    Android.onIFrameAPIReady(); // Notify Android that the API is ready
}

// Function to create a temporary player just to get the title
function fetchVideoTitle(videoId) {
    let tempPlayer = new YT.Player('player', {
        videoId: videoId,
        events: {
            'onReady': function (event) {
                let videoData = event.target.getVideoData();
                let videoTitle = videoData.title;

                // Convert title to JSON and send to Android
                let jsonString = JSON.stringify(videoTitle);
                console.log("Sending JSON: " + jsonString);
                Android.onPlayerCreated(jsonString);

                tempPlayer.destroy(); // Cleanup temporary player
            }
        }
    });
}

// Function to destroy existing player (if any) and create a new one
function loadVideo(videoId, autoplay, startTime) {
    if (player) {
        stopVideo(); // Ensure the previous player is properly stopped before creating a new one
    }

    player = new YT.Player('player', {
        height: document.body.clientHeight,
        width: document.body.clientWidth,
        videoId: videoId,
        playerVars: {
            'enablejsapi': 1,
            'autoplay': autoplay,
            'controls': 1,
            'modestbranding': 1,
            'rel': 0,
            'iv_load_policy': 3,
            'fs': 0,
            'start': startTime // Start at the exact position where it was last stopped
        },
        events: {
            'onStateChange': onPlayerStateChange,
            'onReady': onPlayerReady
        }
    });
}

// Called when the player is ready
function onPlayerReady(event) {
    Android.onPlayerReady();
}

let stateChangeTimeout = null; // Store timeout reference

function onPlayerStateChange(event) {
    lastEvent = event.data; // Update last event state
    lastPosition = player.getCurrentTime(); // Keep track of last known time

    // Clear any existing timeout before setting a new one
    if (stateChangeTimeout) {
        clearTimeout(stateChangeTimeout);
    }

    // Run this block after 300 ms to verify the event is still the same
    stateChangeTimeout = setTimeout(() => {
        if (event.data !== lastEvent) {
            return; // Exit if the event state has changed
        }

        if (event.data === YT.PlayerState.PLAYING) {
            Android.onPlay(lastPosition);
        } else if (event.data === YT.PlayerState.PAUSED) {
            Android.onPause(lastPosition);
        }
    }, 300);
}


function requestPlayback() {
    if (player) {
        let currentTime = player.getCurrentTime();
        let isPlaying = (player.getPlayerState() === YT.PlayerState.PLAYING);
        Android.onRequestPlayback(isPlaying, currentTime);
    }
}

function updatePlayback(isPlaying, currentTime) {
    if (player) {
        player.seekTo(currentTime, true); // Seek to the provided time

        if (isPlaying) {
            player.playVideo(); // Resume playback if it was playing
        } else {
            player.pauseVideo(); // Pause playback if it was paused
        }
    }
}



// Stop and destroy the player explicitly, ensuring Android gets the exact last known time
function stopVideo() {
    if (player) {
        lastPosition = player.getCurrentTime();

        // Notify Android before destroying the player
        Android.onDestroy(lastPosition);

        player.destroy();
        player = null;
    }
}
