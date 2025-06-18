
function displayWelcomeInfo(welcomeInfo) {
    let welcomeLine = jQuery('#welcomeMessage');
    let welcomeString = "<h3>Welcome ";
    welcomeString += welcomeInfo.firstName + " " + welcomeInfo.lastName + " to FabFlix!</h3>";
    welcomeLine.html(welcomeString);
}

//when coming to the movie-list page (landing page after login), scan for session and say hi user
jQuery.ajax({
    dataType: "json",
    method: "POST",
    url: "api/welcome",
    success: (welcomeInfo) => displayWelcomeInfo(welcomeInfo)
});