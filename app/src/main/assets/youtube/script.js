let player;

function onYouTubeIframeAPIReady() {
    Android.onReady(); // Notify Android that the API is ready
}

// Function to destroy existing player (if any) and create a new one
function loadVideo(videoId) {
    if (player) {
        player.destroy(); // Destroy existing player
        player = null;
    }

    player = new YT.Player('player', {
        height: document.body.clientWidth,
        width: document.body.clientHeight,
        videoId: videoId,
        playerVars: {
            'autoplay': 1,
            'controls': 1,
            'modestbranding': 1,
            'rel': 0,
            'iv_load_policy': 3,
            'fs': 0
        },
        events: {
            'onStateChange': onPlayerStateChange,
            'onReady': onPlayerReady
        }
    });

}

// Called when player is ready
function onPlayerReady(event) {
    Android.onPlayerReady();
    
    // Explicitly try playing video
    setTimeout(() => {
        event.target.playVideo();
    }, 500); // Small delay to ensure player is ready
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
