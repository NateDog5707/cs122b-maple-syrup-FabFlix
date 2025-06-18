/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleSearchResult(resultData) {
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
                }
            });
        });
    });
}

function handleNormalSearch(query){
    const params = new URLSearchParams();
    params.set("title", query);
    params.set("limit", 10);
    params.set("offset", 0);
    window.location.href = `movie-list.html?${params}`;
}

function handleLookup(query, doneCallback) {
    console.log("Autocomplete Initiated")

    let cached_data = localStorage.getItem(query);

    if (cached_data !== null)
    {
        cached_data = JSON.parse(cached_data);
        console.log("Using Cached");
        handleLookupAjaxSuccess(cached_data, query, doneCallback);
    }
    else
    {
        // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
        // with the query data
        jQuery.ajax({
            "method": "GET",
            // generate the request url from the query.
            // escape the query string to avoid errors caused by special characters
            "url": "api/fts?title=" + query,
            "success": function(data) {
                // pass the data, query, and doneCallback function into the success handler
                console.log("Sending AJAX request to the server");
                handleLookupAjaxSuccess(data, query, doneCallback);
            },
            "error": function(errorData) {
                console.log("lookup ajax error")
                console.log(errorData)
            }
        })
    }
}

function handleLookupAjaxSuccess(data, query, doneCallback) {
    let cached_data = localStorage.getItem(query);

    if (cached_data === null)
    {
        // add to cache
        localStorage.setItem(query, JSON.stringify(data))

    }

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation

    // Transform each movie into { value, data }
    const suggestions = data.map(movie => ({
        value: movie.movie_title,
        data: movie.movie_id
    }));
    console.log("Suggestion List: ", suggestions);
    // Use transformed format
    doneCallback({ suggestions: suggestions });
}
function handleSelectSuggestion(suggestion) {
    window.location.href = `single-movie.html?id=${suggestion["data"]}`;
}
$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    minChars: 3,
    lookupLimit: 10,
    formatResult: function(suggestion, currentValue) {
        return `<div class="movie-suggestion">${suggestion.value}</div>`;
    }
});

// Makes the HTTP GET request and registers on success callback function handleSearchResult
jQuery("#search").submit(function (event){
    event.preventDefault();
    const query = $('#autocomplete').val();
    handleNormalSearch(query);
});
