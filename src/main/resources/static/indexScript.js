//Connection elements
const infoForUser = document.getElementById("infoForUser");
const userNameTextField = document.getElementById("userNameTextField");
const createRoomButton = document.getElementById("createRoomButton");
const connectionStatus = document.getElementById("connectionStatus");
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
const fileStatusTextInfo = document.getElementById("fileStatusTextInfo");

//RoomInfo elements
const viewersTableBody = document.getElementById("viewersTableBody");

//Script Variables
const PLAY_PAUSE = "playPause";
const MOVE = "move";
var userId = "mysteryUser";
var mouseOverControls = false;
var roomId = null;
var stompClient = null;
var isConnected = false;
var subscribedToRoom = null;
var subscribedToRoomInfo = null;
var userFile = null;

//Event Listeners
//Event after DOM is Loaded:
document.addEventListener('DOMContentLoaded', askForUserId);

//Connection Event Listeners
createRoomButton.addEventListener("click", createNewRoom);
connectButton.addEventListener("click", connectToSomeoneElseRoom);

//Video Player Event Listeners
playPauseButton.addEventListener("click", switchPlayPause);
fullScreenButton.addEventListener("click", fullScreen);
videoPlayer.addEventListener("click", switchPlayPause);
videoPlayer.addEventListener("timeupdate", updateVideoState);
videoPlayer.addEventListener("mousemove", showControlsInFullScreenMode);
videoPlayer.addEventListener("loadstart", userFileReady);
videoPlayer.addEventListener("error", videoPlayerError);
videoPlayer.addEventListener("load", freeVideoMemory);
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
    var videoDuration = validateUserFile();
    if(videoDuration <= 0) return;
    if(subscribedToRoom != null) unsubscribeFromRoom();
    roomCreatorName = getUserName();
    roomCreatorId = userId;
    //TODO: if the userId is invalid, ask again for a new userId.
    if (!validateUserName(roomCreatorName)) return;
    if(!validateUserId(roomCreatorId)) return;
    createRoom(roomCreatorId, roomCreatorName, videoDuration);
}

function connectToSomeoneElseRoom(){
    var roomCode = roomCodeTextField.value;
    if(!validateRoomCode(roomCode)) return;
    if(subscribedToRoom != null) unsubscribeFromRoom();
    let userName = getUserName();
    if (!validateUserName(userName)) return;
    var videoDuration = validateUserFile();
    if(videoDuration <= 0) return;
    roomId = roomCode;
    subscribeToRoomChannels();
}

function switchPlayPause() {
    if (videoPlayer.readyState !== 4) return;
    playPauseAction();
    videoCurrentTime = videoPlayer.currentTime;
    if(videoCurrentTime === null) videoCurrentTime = 0.0;
    //Maybe move the message generation somewhere else?
    message = {senderId: userId, action: PLAY_PAUSE, videoTimeStamp : videoCurrentTime};
    sendVideoPlayerChanges(message);
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
    if(subscribedToRoom != null) unsubscribeFromRoom();
    fileLoaderMessage("File Loaded Successfully");
    setVideoTitle(userFile.name);
}

function videoPlayerError(errorEvent){
    //TODO: do something with the error code?
    //console.log("Error event:");
    //console.log(errorEvent.target.error);
    if(subscribedToRoom != null) unsubscribeFromRoom();
    userFile = null;
    fileLoaderMessage("Unable To Load File");
    setVideoTitle("Title");
}

function progressBarMoveToTime(e) {
    if (videoPlayer.readyState !== 4) return;
    var percent = e.offsetX / this.offsetWidth;
    videoPlayerNewCurrentTime = percent * videoPlayer.duration;

    message = {senderId: userId, action: MOVE, videoTimeStamp : videoPlayerNewCurrentTime};
    sendVideoPlayerChanges(message);
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

function getUserName(){
    var roomCreatorName = userNameTextField.value;
    roomCreatorName = roomCreatorName.trim();
    return roomCreatorName;
}

function freeVideoMemory(){
    //free memory
    URL.revokeObjectURL(videoPlayer.src);
}

/////////////////////////////////////
/// SERVER COMMUNICATIONS METHODS ///
/////////////////////////////////////

function askForUserId(){
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
            connectToServer();
        } else {
            //TODO: show the problem?
            // There was a problem with the request.
            // For example, the response may have a 404 (Not Found)
            // or 500 (Internal Server Error) response code.
        }
    }
}

function connectToServer(){
    var socket = new SockJS("/privateUserEndpoint");
    stompClient = Stomp.over(socket);
    var headers = createStompConnectHeaders();
    //method used: client.connect(headers, connectCallback, errorCallback)
    stompClient.connect(headers, subscribeToUserPrivateChannel, unableToConnectToServer);
}

function unableToConnectToServer(){
    //TODO: This method
    console.log("Unable to connect to server. EXPLOSION!!!");
}

function subscribeToUserPrivateChannel(){
    isConnected = true;
    var headers = createStompSubscribeToUserPrivateChannel();
    stompClient.subscribe("/user/queue/reply", function (messageOutput) {
        receivePrivateUserMessage(tryingToParseToJsonObject(messageOutput.body));
    }, headers);
}

function tryingToParseToJsonObject(messageOutputBody){
    try{
        var parsed = JSON.parse(messageOutputBody);
        return parsed;
    } catch (error){
        //Probably is only byte[] data from template.convertAndSendToUser from MessageInterceptor. IT MAY BE A REAL PROBLEM...
        return messageOutputBody;
    }
}

function receivePrivateUserMessage(privateUserMessage){
    if (typeof privateUserMessage === 'string' || privateUserMessage instanceof String){
        processUserError(privateUserMessage);
    }else{
        processUserMessage(privateUserMessage);
    }
}

function processUserError(userError){
    roomId = null;
    subscribedToRoom = null;
    subscribedToRoomInfo = null;
    switch(userError){
        case "INCORRECT_VIDEO_FILE":
            showConnectionStatus("Not Connected", "red");
            showMessage("Unable to connect to room. The file selected is not the file everyone else is watching. Please change the file.", "red");
            break;
        default:
            break;
    }
}

function processUserMessage(userMessage){
    if(userMessage.action == "FULL_ROOM_INFO"){
        userMessage.users.forEach(addUserToPeopleWatchingTable);
    }
}

function createRoom(roomCreatorId, roomCreatorName, videoDuration){
    //Not letting the user create a new room if they aren't previously connected to the server
    if(!isConnected) return;
    httpRequest = new XMLHttpRequest();
    httpRequest.onreadystatechange = roomCreated;
    url = "/createRoom";
    url = addQueryParameters(url, "userId", roomCreatorId);
    url = addQueryParameters(url, "userName", roomCreatorName);
    url = addQueryParameters(url, "videoDuration", videoDuration);
    httpRequest.open('POST', url, true);
    httpRequest.send();
}

function roomCreated() {
    if (httpRequest.readyState === XMLHttpRequest.DONE) {
        if (httpRequest.status === 200) {
            parseRoomInfo(httpRequest.responseText);
        } else {
            //TODO: show the problem?
            // There was a problem with the request.
            // For example, the response may have a 404 (Not Found)
            // or 500 (Internal Server Error) response code.
        }
    }
}

function parseRoomInfo(responseJson){
    let room = JSON.parse(responseJson);
    roomId = room.roomId;
    subscribeToRoomChannels();
}

function subscribeToRoomChannels() {
    subscribeToRoomInfoChanges();
    subscribeToVideoPlayerChanges();
    // The Stomp library doesn't give a confirmation message of any kind for a subscription. This problem
    // is attempted to be solved with private user messages.
    showConnectionStatus("Connected to  Room: " + roomId, "green");
    showMessage("Connected", "green");
}

function subscribeToRoomInfoChanges() {
    //subscribe(destination, callback, headers = {})
    var headers = createStompSubscribeToRoomInfoHeaders();
    subscribedToRoomInfo = stompClient.subscribe("/roomInfoController/change/" + roomId, function (messageOutput) {
        receiveRoomInfoChanges(JSON.parse(messageOutput.body));
    }, headers);
}

function receiveRoomInfoChanges(roomInfo) {
    if(roomInfo.action == "JOINING"){
        roomInfo.users.forEach(addUserToPeopleWatchingTable);
    }
    if(roomInfo.action == "LEAVING"){
        roomInfo.users.forEach(removeUserFromPeopleWatchingTable);
    }
}

function subscribeToVideoPlayerChanges() {
    //subscribe(destination, callback, headers = {})
    var headers = createStompSubscribeToVideoPLayerChangeHeaders();
    subscribedToRoom = stompClient.subscribe("/videoController/change/" + roomId, function (messageOutput) {
        receiveVideoPlayerChanges(JSON.parse(messageOutput.body));
    }, headers);
}

function receiveVideoPlayerChanges(message) {
    //TODO: The sender of the message is receiving it too. I would like to NOT receive my own message.
    if(message.senderId === userId) return;

    switch (message.action) {
        //TODO: check if the new time is in range of the video playing.
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

function sendVideoPlayerChanges(message) {
    message = JSON.stringify(message);
    if (stompClient !== null && roomId !== null) {
        socketUrl = "/app/room/" + roomId;
        stompClient.send(socketUrl, {}, message);
    }
}

function unsubscribeFromRoom(){
    // Same as subscriptions, the user will not receive any answer for they unsubscription attempt. This problem
    // is attempted to be solved with private user messages.
    if(subscribedToRoom != null){
        var headers = createUnsubscribeHeaders("videoControllerChannel");
        subscribedToRoom.unsubscribe(headers);
    }

    if(subscribedToRoomInfo != null){
        var headers = createUnsubscribeHeaders("roomInfoChannel");
        subscribedToRoomInfo.unsubscribe(headers);
    }

    subscribedToRoom = null;
    subscribedToRoomInfo = null;
    roomId = null;
    showConnectionStatus("Not Connected", "red");
    showMessage("Disconnected", "red");
    emptyViewersTable();
}

function disconnect(){
    if(stompClient != null) {
        stompClient.disconnect();
    }
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

function createStompConnectHeaders(){
    var stompConnectHeaders = {
        userId: userId
    };
    return stompConnectHeaders;
}

function createStompSubscribeToUserPrivateChannel(){
    var stompHeaders = {
        userId: userId,
    };
    return stompHeaders;
}

function createStompSubscribeToVideoPLayerChangeHeaders(){
    var stompHeaders = {
        userId: userId,
        videoDuration: videoPlayer.duration
    };
    return stompHeaders;
}

function createStompSubscribeToRoomInfoHeaders(){
    var stompHeaders = {
        userId: userId,
        userName: getUserName(),
        videoDuration: videoPlayer.duration
    };
    return stompHeaders;
}

function createUnsubscribeHeaders(unsubscribeChannel){
    unsubscribedUserName = getUserName();
    if (unsubscribedUserName.length < 1) unsubscribedUserName = "XxXlittle_weeperxXx";
    var channelToUnsubscribe = "";
    switch(unsubscribeChannel){
        case "videoControllerChannel":
            channelToUnsubscribe = "videoControllerChannel";
            break;
        case "roomInfoChannel":
            channelToUnsubscribe = "roomInfoChannel";
            break;
        default:
            channelToUnsubscribe = "videoControllerChannel";
    }
    var stompHeaders = {
        userId: userId,
        userName: unsubscribedUserName,
        roomId: roomId,
        channelToUnsubscribe: channelToUnsubscribe
    }
    return stompHeaders;
}

//////////////////////////
/// VALIDATION METHODS ///
//////////////////////////

function validateUserName(userName) {
    if (userName.length < 1) {
        showMessage("Username empty", "orange");
        return false;
    }
    return true;
}

function validateUserId(userIdToValidate){
    if(userIdToValidate == null || userIdToValidate == "mysteryUser"){
        showMessage("User Id invalid, please refresh page");
    }else{
        return true;
    }
}

function validateRoomCode(roomCode){
    if(roomCode == "" || roomCode == null){
        showMessage("Empty room code, unable to connect", "orange");
        return false;
    }
    if(roomCode == roomId){
        showMessage("Already connected to room: " + roomCode, "orange");
        return false;
    }

    return true;
}

function validateUserFile(){
    if(userFile == null){
        showMessage("No file selected yet", "orange");
        return -1;
    }

    return videoPlayer.duration;
}

////////////////////
/// VIEW METHODS ///
////////////////////

function showMessage(message, color){
    infoForUser.innerHTML = message;
    switch(color){
        case "green":
            infoForUser.style.color = "green";
            break;
        case "orange":
            infoForUser.style.color = "orange";
            break;
        default:
            infoForUser.style.color = "red";
    }
}

function showConnectionStatus(message, color){
    connectionStatus.innerHTML = message;
    if(color == "green"){
        connectionStatus.style.color = "green";
    }else{
        connectionStatus.style.color = "red";
    }
}

function fileLoaderMessage(newStatus){
    fileStatusTextInfo.innerHTML = newStatus;
}

function setVideoTitle(newTitle){
    titleText.innerHTML = newTitle;
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

function addUserToPeopleWatchingTable (newUser){
    //User object: {"userId":"4a208408-c797-46a7-99ec-cc8f9f10cfcc","userName":"Anonymous"}
    var trNode = document.createElement("tr");
    trNode.id = newUser.userId;

    var thNode = document.createElement("th");
    thNode.className = "roomViewer text";
    thNode.innerHTML = newUser.userName;

    trNode.appendChild(thNode);
    viewersTableBody.appendChild(trNode);
}

function removeUserFromPeopleWatchingTable(userToRemove){
    //User object: {"userId":"4a208408-c797-46a7-99ec-cc8f9f10cfcc","userName":"Anonymous"}
    var userToRemove = document.getElementById(userToRemove.userId);
    userToRemove.remove();
}

function emptyViewersTable(){
    //Maybe this left some object in memory that I'm not aware of?
    viewersTableBody.innerHTML = "";
}