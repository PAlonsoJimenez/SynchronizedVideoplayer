//Connection elements
const userNameTextField = document.getElementById("userNameTextField");
const createRoomButton = document.getElementById("createRoomButton");
const roomConnectedName = document.getElementById("roomConnectedName");
const roomCodeTextField = document.getElementById("roomCodeTextField");
const connectButton = document.getElementById("connectButton");

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
var userId = "mysteryUser";
var mouseOverControls = false;
var roomId = null;
var stompClient = null;
var userFile = null;

//Event Listeners
//Event after DOM is Loaded:
document.addEventListener('DOMContentLoaded', askForUserId);

//Connection Event Listeners
createRoomButton.addEventListener("click", createNewRoom);

//Video Player Event Listeners
playPauseButton.addEventListener("click", switchPlayPause);
fullScreenButton.addEventListener("click", fullScreen);
videoPlayer.addEventListener("click", switchPlayPause);
videoPlayer.addEventListener("timeupdate", updateVideoState);
videoPlayer.addEventListener("mousemove", showControlsInFullScreenMode);
videoPlayer.addEventListener("loadstart", userFileReady);
videoPlayer.addEventListener("error", videoPlayerError);
progressBar.addEventListener("click", progressBarMoveToTime);
controlsContainer.addEventListener("mouseover", mouseOverControlsContainer);
controlsContainer.addEventListener("mouseout", mouseOutControlsContainer);

//File Selector Event Listeners
customButton.addEventListener("click", customButtonClick);
hiddenInputFileButton.addEventListener("change", selectFile);

////////////////////////////////
/// USER INTERACTION METHODS ///
////////////////////////////////

function createNewRoom(){
    if(stompClient != null) disconnectFromRoom();
    roomCreatorName = userNameTextField.value;
    roomCreatorName = roomCreatorName.trim();
    roomCreatorId = userId;
    //TODO: Maybe some user message to let the user know there is some problem?
    //TODO: if the userId is invalid, ask again for a new userId.
    if (!validateUserName(roomCreatorName)) return;
    if(!validateUserId(roomCreatorId)) return;
    createRoom(roomCreatorId, roomCreatorName);
}

function switchPlayPause() {
    if (videoPlayer.readyState !== 4) return;
    playPauseAction();
    videoCurrentTime = videoPlayer.currentTime;
    if(videoCurrentTime === null) videoCurrentTime = 0.0;
    //Maybe move the message generation somewhere else?
    message = {senderId: userId, action: PLAY_PAUSE, videoTimeStamp : videoCurrentTime};
    sendAction(message);
}

function fullScreen() {
    //Only checking if is any element in fullScreen mode, not necessarily the element we want
    //https://developer.mozilla.org/en-US/docs/Web/API/DocumentOrShadowRoot/fullscreenElement
    if (document.fullscreenElement) {
        document.exitFullscreen();
        setControlsVisible(true);
    } else {
        videoContainer.requestFullscreen();
        setControlsVisible(false);
    }
}

function updateVideoState() {
    updateProgressBar();
    if (videoPlayer.ended) {
        playPauseButton.style.backgroundImage = "url(" + playIcon.src + ")";
        //When reloading the video I get the error:
        //TypeError: HTMLProgressElement.value setter: Value being assigned is not a finite floating-point value.
        //while updating the progressBar:
        videoPlayer.load();
    }
}

function showControlsInFullScreenMode() {
    //Only checking if is any element in fullScreen mode, not necessarily the element we want
    //https://developer.mozilla.org/en-US/docs/Web/API/DocumentOrShadowRoot/fullscreenElement
    if (document.fullscreenElement) {
        showControlsShortTime();
    } else {
        setControlsVisible(true);
    }
}

function userFileReady(){
    //TODO: If connected, disconnect
    //TODO: Move this to view Method. (Not all the function, only after check if connected and disconnected)
    titleText.innerHTML = userFile.name;
    customText.innerHTML = "File Loaded Successfully";
}

function videoPlayerError(){
    //TODO: ask for the error variable in the method.
    //TODO: If connected, disconnect
    userFile = null;
    //TODO: Move the next part to viewMethods
    titleText.innerHTML = "Title";
    customText.innerHTML = "Unable To Load File";
}

function progressBarMoveToTime(e) {
    if (videoPlayer.readyState !== 4) return;
    var percent = e.offsetX / this.offsetWidth;
    videoPlayerNewCurrentTime = percent * videoPlayer.duration;

    message = {senderId: userId, action: MOVE, videoTimeStamp : videoPlayerNewCurrentTime};
    sendAction(message);
    //Todo: This one has to be after sending the message... try to do it with threads.
    moveToTimeAction(videoPlayerNewCurrentTime);
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

/// Utility Methods ///

function showControlsShortTime() {
    setControlsVisible(true);
    setTimeout(hideControls, 3000);
}

function hideControls() {
    if (!mouseOverControls)
        setControlsVisible(false);;
}

/////////////////////////////////////
/// SERVER COMMUNICATIONS METHODS ///
/////////////////////////////////////

function askForUserId(){
    console.log("Asking...");
    httpRequest = new XMLHttpRequest();
    httpRequest.onreadystatechange = setUserId;
    url = "/getUserId";
    httpRequest.open('GET', url, true);
    httpRequest.send();
}

function setUserId() {
    if (httpRequest.readyState === XMLHttpRequest.DONE) {
        if (httpRequest.status === 200) {
            let newUserId = JSON.parse(httpRequest.responseText);
            userId = newUserId.id;
            console.log(userId);
        } else {
            //TODO: show the problem?
            // There was a problem with the request.
            // For example, the response may have a 404 (Not Found)
            // or 500 (Internal Server Error) response code.
        }
    }
}

function createRoom(roomCreatorId, roomCreatorName){
    httpRequest = new XMLHttpRequest();
    httpRequest.onreadystatechange = roomCreated;
    url = "/createRoom";
    url = addQueryParameters(url, "userId", roomCreatorId);
    url = addQueryParameters(url, "userName", roomCreatorName);
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
    updateRoomInfo(room);

    var socket = new SockJS("/room/" + roomId);
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        subscribe(frame);
    });
}

function subscribe(frame) {
    console.log("Connected");
    stompClient.subscribe("/videoController/change/" + roomId, function (messageOutput) {
        receiveAction(JSON.parse(messageOutput.body));
    });
}

function receiveAction(message) {
    //TODO: The sender of the message is receiving it too. I would like to NOT receive my own message.
    if(message.senderId === userId) return;

    switch (message.action) {
        case PLAY_PAUSE:
            //TODO: Move this line of code to the viewMethods (Already in moveToTimeAction)
            //Maybe I can Overload the playPauseAction() method, with a 'currentTime' param,
            //if change from play to pause: pause then set currentTime. if change from pause to play: set currentTime then play.
            videoPlayer.currentTime = message.videoTimeStamp;
            playPauseAction();
            break;
        case MOVE:
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

function disconnectFromRoom(){
    disconnect();
    updateConnectionStatus();
}

function disconnect(){
    if(stompClient != null) {
        stompClient.disconnect();
    }
    stompClient = null;
}

/// Utility Methods ///
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

//////////////////////////
/// VALIDATION METHODS ///
//////////////////////////

function validateUserName(userName) {
    if (userName.length < 1) {
        //TODO: move this to view methods
        roomConnectedName.innerHTML = "User Name Empty";
        return false;
    }
    return true;
}

function validateUserId(userIdToValidate){
    if(userIdToValidate == null || userIdToValidate == "mysteryUser"){
        //TODO: message to the user here?
        return false;
    }else{
        return true;
    }
}

////////////////////
/// VIEW METHODS ///
////////////////////

function updateRoomInfo(room){
    //TODO: rest of the info
    roomConnectedName.innerHTML = "Connected to  Room: " + room.roomId;
    roomConnectedName.style.color = "green";
}

function updateConnectionStatus(){
    roomConnectedName.innerHTML = "Not Connected Yet";
    roomConnectedName.style.color = "red";
    console.log("Disconnected");
}

function playPauseAction() {
    if (videoPlayer.paused) {
        videoPlayer.play();
        playPauseButton.style.backgroundImage = "url(" + pauseIcon.src + ")";
    } else {
        videoPlayer.pause();
        playPauseButton.style.backgroundImage = "url(" + playIcon.src + ")";
    }
}

function setControlsVisible(visible){
    if(visible){
        controlsContainer.style.visibility = "visible";
    }else{
        controlsContainer.style.visibility = "hidden";
    }
}

function updateProgressBar() {
    var percentageViewed = (videoPlayer.currentTime) / videoPlayer.duration;
    //TODO: When reloading the video I get the error:
    //TypeError: HTMLProgressElement.value setter: Value being assigned is not a finite floating-point value.
    //while updating the progressBar HERE:
    progressBar.value = percentageViewed;
}

function moveToTimeAction (newTime){
    videoPlayer.currentTime = newTime;
}