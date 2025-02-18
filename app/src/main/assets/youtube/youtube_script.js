let player;
let lastEvent = null; // Track the last event state
let logCount = 0; // Initialize log count

// YouTube IFrame API callback function
function onYouTubeIframeAPIReady() {
    player = new YT.Player('player', {
        height: '450',
        width: '800',
        videoId: 'W7h_BgLxAIc', // Replace with your video ID
        playerVars: {
            'autoplay': 1,
            'controls': 1,
            'modestbranding': 1,
            'rel': 0,
            'iv_load_policy': 3,
            'fs': 1
        },
        events: {
            'onReady': onPlayerReady,
            'onStateChange': onPlayerStateChange
        }
    });
}

// When the player is ready
function onPlayerReady(event) {
    console.log("Player is ready.");

    // Retrieve video title
    var videoData = event.target.getVideoData();
    var videoTitle = videoData.title;
    console.log("Video Title: " + videoTitle);

    // Optionally, display the title on the webpage
    var titleElement = document.getElementById('video-title');
    if (titleElement) {
        titleElement.textContent = videoTitle;
    }
}

// Handle player state changes
function onPlayerStateChange(event) {
    lastEvent = event.data; // Update last event state
    const currentTimeMs = Math.round(player.getCurrentTime() * 1000000); // Convert to milliseconds

    // Run this block after 1000 ms to verify the event is still the same
    setTimeout(() => {
        if (event.data !== lastEvent) {
            return; // Exit if the event state has changed
        }

        logCount++; // Increment the log count

        // Display log count and state
        if (event.data === YT.PlayerState.PLAYING) {
            console.log(`[Log ${logCount}] Video is playing at ${currentTimeMs} ms.`);
        } else if (event.data === YT.PlayerState.PAUSED) {
            console.log(`[Log ${logCount}] Video is paused at  ${currentTimeMs} ms.`);
        }
    }, 300);
}
