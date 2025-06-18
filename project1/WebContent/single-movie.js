/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    let singleMovieElement = jQuery("#single_movie");

    // append two html <p> created to the h3 body, which will refresh the page

    singleMovieElement.append(
        "<div style='display: flex; justify-content: space-between'>" +
            "<p>Movie Title: " + resultData[0]["smovie_title"] + "</p>" +
            "<button class ='addToCartButton' " +
            "movieId='" + resultData[0]['smovie_id'] + "' " +
            "movieName='" + resultData[0]['smovie_title'] + "' " +
            "moviePrice='" + resultData[0]['smovie_price'] + "'" +
            ">Add to Cart</button>"+
        "</div>" +
        "<p>Year: " + resultData[0]["smovie_year"] + "</p>" +
        "<p>Director: " + resultData[0]["smovie_director"] + "</p>" +
        "<p>Rating: " + resultData[0]["smovie_rating"] + "</p>");

    let genreTableBodyElement = jQuery("#genre_table_body");
    // Concatenate the html tags with resultData jsonObject to create table rows
    let genreRowHTML = "";
    genreRowHTML += "<tr>";
    let genreLinks = "";
    let genresList = resultData[0]['smovie_genres'].split(", ");

    for (let j = 0; j < genresList.length; j++) {
        let genreName = genresList[j]
        genreLinks += `<a href="movie-list.html?type=genre&keyword=${genreName}">${genreName}</a>`;
        genreLinks += '<br>'
    }
    genreRowHTML += "<td>" + genreLinks + "</td>";

    genreRowHTML += "</tr>";

    genreTableBodyElement.append(genreRowHTML);

    let starRowHTML = "";
    starRowHTML += "<tr>";
    // Find the empty table body by id "single_movie_table_body"
    let singleMovieBodyElement = jQuery("#single_movie_table_body");

    let starLinks = "";
    let starList = resultData[0]['smovie_stars'].split(", ");
    for (let j = 0; j < starList.length; j++) {
        let [starId, starName] = starList[j].split(":");
        starLinks += `<a href="single-star.html?id=${starId}">${starName}</a>`;
        starLinks += '<br>'
    }
    starRowHTML += "<td>" + starLinks + "</td>";

    starRowHTML += "</tr>";

    starRowHTML += "</tr>";

    // Append the row created to the table body, which will refresh the page
    singleMovieBodyElement.append(starRowHTML);
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
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId,
    success: (resultData) => handleResult(resultData)
});