/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleSearchResult(resultData) {
    console.log("handleSearchResult: populating table from resultData");
    console.log(resultData)
    // Find the empty table body by id "search_table_body"
    let searchBodyElement = jQuery("#movie_list_table_body");
    searchBodyElement.empty();

    // Iterate through resultData

    for (let i = 0; i < resultData.length; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";

        rowHTML += "<td>" + "<button class='addToCartButton' " +
            "movieId='" + resultData[i]['movie_id'] + "'" +
            "movieName='" + resultData[i]['movie_title'] + "'" +
            "moviePrice='" + resultData[i]['movie_price'] + "'" +
            ">Add to Cart</button></td>"

        rowHTML +=
            "<td>" + '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]['movie_title'] +
            "</a>" +
            "</td>";
        rowHTML += "<td>" + resultData[i]['movie_year'] + "</td>"
        rowHTML += "<td>" + resultData[i]['movie_director'] + "</td>"

        let genreLinks = "";
        let genresList = resultData[i]['movie_genres'].split(", ");
        for (let j = 0; j < genresList.length; j++) {
            let genreName = genresList[j]
            genreLinks += `<a href="movie-list.html?type=genre&keyword=${genreName}&limit=10&offset=0">${genreName}</a>`;
            if (j !== genresList.length - 1) {
                genreLinks += ", ";
            }
        }
        rowHTML += "<td>" + genreLinks + "</td>";

        let starLinks = "";
        let starList = resultData[i]['movie_stars'].split(", ");
        for (let j = 0; j < starList.length; j++) {
            let [starId, starName] = starList[j].split(":");
            starLinks += `<a href="single-star.html?id=${starId}">${starName}</a>`;
            if (j !== starList.length - 1) {
                starLinks += ", ";
            }
        }
        rowHTML += "<td>" + starLinks + "</td>";


        rowHTML += "<td>" + resultData[i]['movie_rating'] + "</td>"
        rowHTML += "<td>" + resultData[i]['movie_price'] + "</td>"

        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        searchBodyElement.append(rowHTML);
    }
    let limit = parseInt(params.get('limit'));
    let offset = parseInt(params.get('offset'));

    // Disable or enable Next
    if (resultData.length < limit) {
        jQuery("#next-page").prop('disabled', true);
    } else {
        jQuery("#next-page").prop('disabled', false);
    }

    // Disable or enable Previous
    if (offset === 0) {
        jQuery("#prev-page").prop('disabled', true);
    } else {
        jQuery("#prev-page").prop('disabled', false);
    }
    //add to cart api call
    document.querySelectorAll(".addToCartButton").forEach(button => {
    button.addEventListener("click", function() {
        let movieId = this.getAttribute("movieId");
        let movieName = this.getAttribute("movieName");
        let moviePrice = this.getAttribute("moviePrice");

        jQuery.ajax({
            method: "post",
            url: "api/cart",
            data: {
                action: "add",
                movieId: movieId,
                movieName: movieName,
                moviePrice: moviePrice},
            success: (response) => {
                console.log("Added to cart!");
            }
        });
    });
});
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleSearchResult
jQuery("#search").submit(function (event){
    event.preventDefault();

    let title = jQuery("#title").val().trim();
    let year = jQuery("#year").val().trim();
    let director = jQuery("#director").val().trim();
    let star_name = jQuery("#star_name").val().trim();

    if (title === "" && year === "" && director === "" && star_name === "") {
        alert("Please enter at least one search field!");
        return;
    }

    // check for valid numerical 4 digit year
    if (year !== "" && !/^\d{4}$/.test(year)) {
        alert("Please enter a valid 4-digit year!");
        return;
    }

    let params = jQuery(this).serialize()
    window.location.href = `movie-list.html?${params}`;
});

//on movie-list load, i want to reinput the entry boxes
jQuery.ajax({
    dataType: "json",
    method: "post",
    url: "api/maintain-search",
    success: (resultData) => maintainSearch(resultData)
});
