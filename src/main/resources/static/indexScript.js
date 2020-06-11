//Connection elements
const userIdTextField = document.getElementById("userIdTextField");
const createRoomButton = document.getElementById("createRoomButton");
const roomConnectedName = document.getElementById("roomConnectedName");
const roomCodeTextField = document.getElementById("roomCodeTextField");
const connectButton = document.getElementById("connectButton");
const connectionStatusSpan = document.getElementById("connectionStatusSpan");

//Title element
const titleText = document.getElementById("title");

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

//File Selector elements
const hiddenInputFileButton = document.getElementById("UserFile");
const customButton = document.getElementById("customButton");
const customText = document.getElementById("customText");

//Script Variables
const PLAY_PAUSE = "playPause";
const MOVE = "move";
var mouseOverControls = false;
var roomId = null;
var stompClient = null;
var userFile = null;

//Event Listeners
//Connection Event Listeners
createRoomButton.addEventListener("click", createRoom);

//Video Player Event Listeners
playPauseButton.addEventListener("click", switchPlayPause);
fullScreenButton.addEventListener("click", fullScreen);
videoPlayer.addEventListener("click", switchPlayPause);
videoPlayer.addEventListener("timeupdate", updateVideState);
videoPlayer.addEventListener("mousemove", fullScreenMode);
videoPlayer.addEventListener("loadstart", userFileReady);
videoPlayer.addEventListener("error", videoPlayerError);
progressBar.addEventListener("click", progressBarMoveToTime);
controlsContainer.addEventListener("mouseover", mouseOverControlsContainer);
controlsContainer.addEventListener("mouseout", mouseOutControlsContainer);

//File Selector Event Listeners
customButton.addEventListener("click", customButtonClick);
hiddenInputFileButton.addEventListener("change", selectFile);

function createRoom() {
    userID = userIdTextField.value;
    userID = userID.trim();
    //TODO: Remember to validate this in the server side too.
    if (!validateUserName(userID))
        return;

    httpRequest = new XMLHttpRequest();
    httpRequest.onreadystatechange = roomCreated;
    url = "/createRoom";
    url = addQueryParameters(url, "userId", userID);
    console.log(url);
    httpRequest.open('POST', url, true);
    httpRequest.send();
}

function roomCreated() {
    if (httpRequest.readyState === XMLHttpRequest.DONE) {
        if (httpRequest.status === 200) {
            connectToCreatedRoom(httpRequest.responseText);
        } else {
            //TODO: show the problem?
            // There was a problem with the request.
            // For example, the response may have a 404 (Not Found)
            // or 500 (Internal Server Error) response code.
        }
    }
}

function connectToCreatedRoom(responseJson) {
    let room = JSON.parse(responseJson);
    roomId = room.roomId;

    roomConnectedName.innerHTML = "Connected to  Room: " + roomId;
    roomConnectedName.style.color = "green";

    var socket = new SockJS("/room/" + roomId);
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        subscribe(frame);
    });
}

function subscribe(frame) {
    console.log("Connected");
    //console.log('Connected: ' + frame);
    stompClient.subscribe("/videoController/change/" + roomId, function (messageOutput) {
        receiveAction(JSON.parse(messageOutput.body));
    });
}

function validateUserName(userID) {
    if (userID.length < 1) {
        roomConnectedName.innerHTML = "User Name Empty";
        return false;
    }
    return true;
}

function receiveAction(message) {
    //TODO: the rest of the possible actions
    switch (message.action) {
        case PLAY_PAUSE:
            videoPlayer.currentTime = message.videoTimeStamp;
            playPauseAction();
            break;
        case MOVE:
            //TODO: Check videoTimeStamp type before doing moving...
            moveToTimeAction(message.videoTimeStamp);
        default:
            break;
    }
}

function sendAction(message) {
    message = JSON.stringify(message);
    if (stompClient !== null && roomId !== null) {
        socketUrl = "/app/room/" + roomId;
        stompClient.send(socketUrl, {}, message);
    }
}

function switchPlayPause() {
    if (videoPlayer.readyState !== 4)
        return;

    //TODO: The sender of the message is receiving it too. I would like to NOT receive my own message.
    //playPauseAction();

    //TODO: rest of the message body, if any (Remember the jackson tag "optional" or something like that, that made some json attributes optionals)
    //TODO: See if I can pause the video... And then get the currentTime.
    videoCurrentTime = videoPlayer.currentTime;
    if(videoCurrentTime === null) videoCurrentTime = 0.0;
    message = {action: PLAY_PAUSE, videoTimeStamp : videoCurrentTime};
    sendAction(message);
}

function playPauseAction() {
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

function userFileReady(){
    //TODO: If connected, disconnect
    titleText.innerHTML = userFile.name;
    customText.innerHTML = "File Loaded Successfully";
}

function videoPlayerError(){
    //TODO: ask for the error variable in the method.
    //TODO: If connected, disconnect
    userFile = null;
    titleText.innerHTML = "Title";
    customText.innerHTML = "Unable To Load File";
}

function updateVideState() {
    updateProgressBar();
    if (videoPlayer.ended) {
        playPauseButton.style.backgroundImage = "url(" + playIcon.src + ")";
        //When reloading the video I get the error:
        //TypeError: HTMLProgressElement.value setter: Value being assigned is not a finite floating-point value.
        //while updating the progressBar:
        videoPlayer.load();
    }
}

function updateProgressBar() {
    var percentageViewed = (videoPlayer.currentTime) / videoPlayer.duration;
    //TODO: When reloading the video I get the error:
    //TypeError: HTMLProgressElement.value setter: Value being assigned is not a finite floating-point value.
    //while updating the progressBar HERE:
    progressBar.value = percentageViewed;
}

function fullScreenMode() {
    //Only checking if is any element in fullScreen mode, not necessarily the element we want
    //https://developer.mozilla.org/en-US/docs/Web/API/DocumentOrShadowRoot/fullscreenElement
    if (document.fullscreenElement) {
        showControlsShortTime();
    } else {
        controlsContainer.style.visibility = "visible";
    }
}

function showControlsShortTime() {
    controlsContainer.style.visibility = "visible";
    setTimeout(hideControls, 3000);
}

function hideControls() {
    if (!mouseOverControls)
        controlsContainer.style.visibility = "hidden";
}

function progressBarMoveToTime(e) {
    if (videoPlayer.readyState !== 4)
        return;
    var percent = e.offsetX / this.offsetWidth;
    videoPlayerNewCurrentTime = percent * videoPlayer.duration;


    message = {action: MOVE, videoTimeStamp : videoPlayerNewCurrentTime};
    sendAction(message);
    //TODO: The sender of the message is receiving it too. I would like to NOT receive my own message.
    //This one has to be after sending the message... try to do it with threads.
    //moveToTimeAction()
}

function moveToTimeAction (newTime){
    //TODO: check the newTime variable type after reading the message...
    videoPlayer.currentTime = newTime;
    progressBar.value = (newTime/videoPlayer.duration) / 100;
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
        userFile = hiddenInputFileButton.files[0];
        console.log("Size: " + userFile.size);
        videoPlayer.src = URL.createObjectURL(userFile);
        //TODO: videoPlayer.onload = function () {URL.revokeObjectURL(videoPlayer.src); // free memory};
    }
}

//Utility functions
//Right now only one parameter per call...
function addQueryParameters(originalQuery, key, value) {
    if ((originalQuery.indexOf("?") !== -1)) {
        originalQuery = originalQuery + "&";
    } else {
        originalQuery = originalQuery + "?";
    }
    originalQuery = originalQuery + key + "=" + encodeURIComponent(value);
    return originalQuery;
}
