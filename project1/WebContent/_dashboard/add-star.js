jQuery('#addStarForm').on('submit', function (e){
    e.preventDefault();
    let birth_year = jQuery("#birth_year").val().trim();
    // check for valid numerical 4 digit year
    if (birth_year !== "" && !/^\d{4}$/.test(birth_year)) {
        jQuery('#yearError').show();
        jQuery('#successMsg').hide();
        jQuery('#errorMsg').hide();
        return;
    }
    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "/cs122b-maple-project/api/_dashboard/add-star",
        data: jQuery(this).serialize(),
        success: function (response) {
            const starId = response.star_id;
            if(response.status === 'success'){
                jQuery('#successMsg').text("Successfully Added!\nNew Star ID: " + starId).show();
                jQuery('#errorMsg').hide();
                jQuery('#yearError').hide();

                jQuery("#star_name").val("");
                jQuery("#birth_year").val("");
            }
            else{ // failed to add
                jQuery('#errorMsg').text(response.message).show();
                jQuery('#successMsg').hide();
                jQuery('#yearError').hide();
            }
        }
    });
});
