jQuery('#logoutForm').on('submit', function(e) {
    e.preventDefault();

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/logout",
        data: jQuery(this).serialize(),
        success: function(response){
            if(response.status === 'success'){
                window.location.href = 'movie-list.html';
            }
            else{ //failed login
                jQuery('#errorMsg').text('Incorrect email or password').show();
            }
        },
        error: function(){
            jQuery('#errorMsg').html('<p>Incorrect email or password</p>').show();
        }
    });
});

