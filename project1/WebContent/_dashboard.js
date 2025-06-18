let recaptcha = false;
jQuery("#dashboardLoginForm").on("submit", function(event) {
    event.preventDefault(); // prevent default form submission
    if (recaptcha) {
        // Get reCAPTCHA token
        const gRecaptchaResponse = grecaptcha.getResponse();

        if (!gRecaptchaResponse) {
            jQuery('#errorMsg').text('Please complete the reCAPTCHA').show();
            return;
        }
    }
    let formData = null;
    if (recaptcha) {
        // serialize form data and add the recaptcha response
        formData = jQuery(this).serialize() + "&g-recaptcha-response=" + gRecaptchaResponse;
    }
    else {
        formData = jQuery(this).serialize()
    }

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/_dashboard",
        data: formData,
        success: function(response){
            // console.log("Response: ", response);
            if(response.status === 'success'){
                window.location.href = '_dashboard/menu.html';
            }
            else{ // failed login
                jQuery('#errorMsg').text(response.message || "Incorrect email or password").show();
                grecaptcha.reset();
            }
        },
        error: function(){
            jQuery('#errorMsg').html('<p>Something went wrong, please try again</p>').show();
        }
    });
});
