let recaptcha = false;
jQuery('#loginForm').on('submit', function(e) {
    e.preventDefault();

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
        formData = jQuery(this).serialize();
    }

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/login",
        data: formData,
        success: function(response){
            if(response.status === 'success'){
                console.log("Success login")
                window.location.href = 'frontpage.html';
            }
            else{ //failed login OR failed captcha
                jQuery('#errorMsg').text(response.message || "Incorrect email or password").show();
                if (recaptcha) {
                    grecaptcha.reset();
                }
            }
        },
        error: function(){
            jQuery('#errorMsg').html('<p>Something went wrong, please try again</p>').show();
        }
    });
});

