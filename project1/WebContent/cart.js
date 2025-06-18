
function displayCartContents(cartData){
    let totalCartCost = 0.0
    let cartTableBodyElement = jQuery("#cart-table-body");

    for(let i = 0; i < cartData.length; i++){
        let rowHTML = "";
        rowHTML += "<tr>";
        //three buttons
        let itemId = cartData[i]['item_id'];
        rowHTML += "<td><button class='incrementButton' " +
            "itemId='"+ itemId +"'>+1</button>"
        rowHTML += "<button class='decrementButton' " +
            "itemId='"+ itemId +"'>-1</button>"
        rowHTML += "<button class='deleteItemButton' " +
            "itemId='"+ itemId +"'>X</button></td>"



        rowHTML += "<td>" + cartData[i]['item_name'] + "</td>"
        rowHTML += "<td>" + cartData[i]['item_price'] + "</td>"
        rowHTML += "<td>" + cartData[i]['item_quantity'] + "</td>"
        totalPrice = cartData[i]['item_price'] * cartData[i]['item_quantity'];
        rowHTML += "<td>" + totalPrice + "</td>"
        totalCartCost += totalPrice;
        rowHTML += "</tr>";
        cartTableBodyElement.append(rowHTML);

    }
    let cartTotalCostElement = $("#cartTotalCost");
    cartTotalCostElement.html("Cart Total: " + totalCartCost);

    document.querySelectorAll(".incrementButton").forEach(button => {
        button.addEventListener("click", function() {
            let itemId = this.getAttribute("itemId");

            jQuery.ajax({
                method: "post",
                url: "api/cart",
                data: {
                    action: "increment",
                    itemId: itemId},
                success: (response) => {
                    // console.log("Incrementing itemId " + itemId );
                }
            });
        });
    });
    document.querySelectorAll(".decrementButton").forEach(button => {
        button.addEventListener("click", function() {
            let itemId = this.getAttribute("itemId");

            jQuery.ajax({
                method: "post",
                url: "api/cart",
                data: {
                    action: "decrement",
                    itemId: itemId},
                success: (response) => {
                    // console.log("Decrementing itemId " + itemId );
                }
            });
        });
    });
    document.querySelectorAll(".deleteItemButton").forEach(button => {
        button.addEventListener("click", function() {
            let itemId = this.getAttribute("itemId");

            jQuery.ajax({
                method: "post",
                url: "api/cart",
                data: {
                    action: "delete",
                    itemId: itemId},
                success: (response) => {
                    // console.log("Deleting itemId " + itemId );
                }
            });
        });
    });

}

jQuery(document).ready(function() {
    jQuery.ajax({
        method: "post",
        url: "api/cart",
        dataType: "json",
        data: {action: "display"},
        success: (cartData) => displayCartContents(cartData)
    });
});

