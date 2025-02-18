let player;

function onYouTubeIframeAPIReady() {
    Android.onIFrameAPIReady(); // Notify Android that the API is ready
}

// Function to create a temporary player just to get the title
function fetchVideoTitle(videoId) {
    let tempPlayer = new YT.Player('player', {
        videoId: videoId,
        events: {
            'onReady': function (event) {
                var videoData = event.target.getVideoData();
                var videoTitle = videoData.title;

                // Convert to JSON string (just the title, not an object)
                var jsonString = JSON.stringify(videoTitle);

                console.log("Sending JSON: " + jsonString);
                Android.onPlayerCreated(jsonString);

                tempPlayer.destroy(); // Cleanup temporary player
            }
        }
    });
}

// Function to destroy existing player (if any) and create a new one
function loadVideo(videoId, autoplay) {
    if (player) {
        player.destroy(); // Destroy existing player
        player = null;
    }

    player = new YT.Player('player', {
        height: document.body.clientWidth,
        width: document.body.clientHeight,
        videoId: videoId,
        playerVars: {
            'autoplay': autoplay,
            'controls': 1,
            'modestbranding': 1,
            'rel': 0,
            'iv_load_policy': 3,
            'fs': 0
        },
        events: {
            'onStateChange': onPlayerStateChange,
            'onReady': Android.onPlayerReady()
        }
    });

}


// Handle player state changes and notify Android
function onPlayerStateChange(event) {
    const currentTimeMs = Math.round(player.getCurrentTime() * 1000);

    if (event.data === YT.PlayerState.PLAYING) {
        Android.onPlay(currentTimeMs);
    } else if (event.data === YT.PlayerState.PAUSED) {
        Android.onPause(currentTimeMs);
    } else if (event.data === YT.PlayerState.BUFFERING) {
        Android.onSeek(currentTimeMs);
    }
}

// Stop and destroy the player explicitly
function stopVideo() {
    if (player) {
        player.destroy();
        player = null;
    }
}
