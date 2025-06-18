jQuery('#addMovieForm').on('submit', function (e){
    e.preventDefault();
    let birth_year = jQuery("#star_birth_year").val().trim();
    // check for valid numerical 4 digit year
    if (birth_year !== "" && !/^\d{4}$/.test(birth_year)) {
        jQuery('#yearError').show();
        jQuery('#successMsg').hide();
        jQuery('#errorMsg').hide();
        return;
    }

    let movie_year = jQuery("#movie_year").val().trim();
    // check for valid numerical 4 digit year
    if (movie_year !== "" && !/^\d{4}$/.test(movie_year)) {
        jQuery('#yearError').show();
        jQuery('#successMsg').hide();
        jQuery('#errorMsg').hide();
        return;
    }

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "/cs122b-maple-project/api/_dashboard/add-movie",
        data: jQuery(this).serialize(),
        success: function (response) {
            const star_id = response.star_id;
            const genre_id = response.genre_id;
            const movie_id = response.movie_id;
            const unique = response.unique
            if(response.status === 'success'){
                if (unique === 0)
                {
                    jQuery('#errorMsg').text("ERROR: DUPLICATE MOVIE").show()
                    jQuery('#successMsg').hide()
                    jQuery('#yearError').hide();
                }
                else
                {
                    jQuery('#successMsg').text("Successfully Added!\nStar ID: " + star_id
                        + " Genre ID: " + genre_id + " Movie ID: " + movie_id).show();
                    jQuery('#errorMsg').hide();
                    jQuery('#yearError').hide();

                }
                jQuery("#movie_title").val("");
                jQuery("#movie_director").val("");
                jQuery("#movie_year").val("");
                jQuery("#genre_name").val("");
                jQuery("#star_name").val("");
                jQuery("#star_birth_year").val("");
            }
            else{ // failed to add
                jQuery('#errorMsg').text(response.message).show();
                jQuery('#successMsg').hide();
                jQuery('#yearError').hide();
            }
        }
    });
});
