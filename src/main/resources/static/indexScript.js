//File Selector elements
const hiddenInputFileButton = document.getElementById("UserFile");
const customButton = document.getElementById("customButton");
const customText = document.getElementById("customText");

//Video Player elements
const videoContainer = document.querySelector(".videoContainer");
const controlsContainer = document.querySelector(".controlsContainer");
const videoPlayer = document.getElementById("videoPlayer");
const progressBar = document.getElementById("progressBar");
const playPauseButton = document.getElementById("playPauseButton");
const fullScreenButton = document.getElementById("fullScreenButton");

//Button Icons (The ones that will change dynamically)
const playIcon = new Image();
playIcon.src = "playIcon.png";
const pauseIcon = new Image();
pauseIcon.src = "pauseIcon.png";

//Script Variables
var mouseOverControls = false;

//Event Listeners
//Vide Player Event Listeners
playPauseButton.addEventListener("click", switchPlayPause);
fullScreenButton.addEventListener("click", fullScreen);
videoPlayer.addEventListener("click", switchPlayPause);
videoPlayer.addEventListener("timeupdate", updateVideState);
videoPlayer.addEventListener("mousemove", fullScreenMode);
progressBar.addEventListener("click", progressBarMoveToTime);
controlsContainer.addEventListener("mouseover", mouseOverControlsContainer);
controlsContainer.addEventListener("mouseout", mouseOutControlsContainer);

//File Selector Event Listeners
customButton.addEventListener("click", customButtonClick);
hiddenInputFileButton.addEventListener("change", selectFile);

//TODO: Do something to be able to replay it when finished
function switchPlayPause() {
    if (videoPlayer.readyState !== 4)
        return;
    if (videoPlayer.paused) {
        playPauseButton.style.backgroundImage = "url(" + pauseIcon.src + ")";
        videoPlayer.play();
    } else {
        playPauseButton.style.backgroundImage = "url(" + playIcon.src + ")";
        videoPlayer.pause();
    }
}

function fullScreen() {
    //Only checking if is any element in fullScreen mode, not necessarily the element we want
    //https://developer.mozilla.org/en-US/docs/Web/API/DocumentOrShadowRoot/fullscreenElement
    if (document.fullscreenElement) {
        document.exitFullscreen();
        controlsContainer.style.visibility = "visible";
    } else {
        videoContainer.requestFullscreen();
        controlsContainer.style.visibility = "hidden";
    }
}

function updateVideState() {
    updateProgressBar();
    if (videoPlayer.ended){
        playPauseButton.style.backgroundImage = "url(" + playIcon.src + ")";
        videoPlayer.load();
    }
}

function updateProgressBar(){
    var percentageViewed = (videoPlayer.currentTime) / videoPlayer.duration;
    progressBar.value = percentageViewed;
}

function fullScreenMode() {
    //Only checking if is any element in fullScreen mode, not necessarily the element we want
    //https://developer.mozilla.org/en-US/docs/Web/API/DocumentOrShadowRoot/fullscreenElement
    if (document.fullscreenElement) {
        showControllsShortTime();
    } else {
        controlsContainer.style.visibility = "visible";
    }
}

function showControllsShortTime() {
    controlsContainer.style.visibility = "visible";
    setTimeout(hideControls, 3000);
}

function hideControls() {
    if (!mouseOverControls) controlsContainer.style.visibility = "hidden";
}

function progressBarMoveToTime(e) {
    if (videoPlayer.readyState !== 4)
        return;
    var percent = e.offsetX / this.offsetWidth;
    videoPlayer.currentTime = percent * videoPlayer.duration;
    progressBar.value = percent / 100;
}

function mouseOverControlsContainer() {
    mouseOverControls = true;
}

function mouseOutControlsContainer() {
    mouseOverControls = false;
}

function customButtonClick() {
    hiddenInputFileButton.click();
}

function selectFile() {
    if (hiddenInputFileButton.value) {
        console.log(hiddenInputFileButton.value);
        //customTxt.innerHTML = realFileBtn.value.match(/[\/\\]([\w\d\s\.\-\(\)]+)$/)[1];

        //It seems the "type" attribute is not being added
        videoPlayer.type = "video/mp4";
        videoPlayer.src = URL.createObjectURL(hiddenInputFileButton.files[0]);

        videoPlayer.onload = function () {
            URL.revokeObjectURL(videoPlayer.src); // free memory
        };

    } else {
        customText.innerHTML = "No file chosen, yet.";
    }
}
