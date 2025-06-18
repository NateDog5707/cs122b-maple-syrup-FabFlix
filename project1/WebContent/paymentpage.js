
function displayCartTotal(total){

    let cartTotalMsg = jQuery("#cartTotalMsg")
    cartTotalMsg.html("Cart Total: " + total['total'])

}


   jQuery.ajax({
       method: "post",
       url:"api/paymentpage",
       dataType: "json",
       data: {action: "getCartTotal"},
       success: (total) => displayCartTotal(total),
       error: function(){

        }
   }) ;


jQuery('#paymentInfoForm').on('submit', function(e) {
    e.preventDefault();
    console.log("Form submitted via JS");


    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/paymentpage",
        data: {action: "validateCard",
                id: jQuery("#cardNumber").val(),
                firstName: jQuery("#firstName").val(),
                lastName: jQuery("#lastName").val(),
                expiration: jQuery("#expirationDate").val()
        },
        success: function(response){
            console.log("Response: ", response);
            if(response.status === 'success'){
                window.location.href = 'confirmation.html';
                return;
            }
            else{ //failed login
                jQuery('#errorMsg').text('Incorrect Credit Card Information').show();
            }
        },
        error: function(){
            jQuery('#errorMsg').text('Incorrect Credit Card Information').show();
        }
    });
});
