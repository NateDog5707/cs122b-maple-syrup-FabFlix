/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMovieListResult(resultData) {
    // Find the empty table body by id "movie_list_table_body"
    let movieListBodyElement = jQuery("#movie_list_table_body");

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
        rowHTML += "<td>"+ resultData[i]['movie_year'] +"</td>"
        rowHTML += "<td>"+ resultData[i]['movie_director'] +"</td>"

        let genreLinks = "";
        let genresList = resultData[i]['movie_genres'].split(", ");
        for (let j = 0; j < genresList.length; j++) {
            let genreName = genresList[j]
            genreLinks += `<a href="movie-list.html?type=genre&keyword=${genreName}&limit=10&offset=0">${genreName}</a>`;
            if (j !== genresList.length - 1)
            {
                genreLinks += ", ";
            }
        }
        rowHTML += "<td>" + genreLinks + "</td>";

        let starLinks = "";
        let starList = resultData[i]['movie_stars'].split(", ");
        for (let j = 0; j < starList.length; j++) {
            let [starId, starName] = starList[j].split(":");
            starLinks += `<a href="single-star.html?id=${starId}">${starName}</a>`;
            if (j !== starList.length - 1)
            {
                starLinks += ", ";
            }
        }
        rowHTML += "<td>" + starLinks + "</td>";

        rowHTML += "<td>"+resultData[i]['movie_rating']+"</td>";
        rowHTML += "<td>"+ resultData[i]['movie_price'] +"</td>"
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieListBodyElement.append(rowHTML);
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
                    // console.log("Added to cart!");
                }
            });
        });
    });

}


function sortMovieList(col1, order1, col2, order2)
{
    const params = new URLSearchParams(window.location.search)
    params.set('col1', col1);
    params.set('order1', order1);
    params.set('col2', col2);
    params.set('order2', order2);

    window.location.href = `movie-list.html?${params.toString()}`;
}

//on page load, i need to fill in the entry boxes
function maintainSearch(resultData){

    let searchTitle = jQuery("#title")
    if (resultData["lastTitle"] != null){
        searchTitle.val(resultData["lastTitle"]);
    }
    let url = 'movie-list.html?';
    let mustUpdateUrl = false;
    if (resultData["lastLimit"] != null) {
        url += "limit=" + resultData['lastLimit'];
        mustUpdateUrl = true;
    }

    if (resultData["lastOffset"] != null) {
        if (resultData["lastLimit"] != null) url += "&"
        url += "offset=" + resultData['lastOffset'];
        mustUpdateUrl = true
    }
    if (mustUpdateUrl === true){
        window.location.href = url;
    }
}

// event listeners
jQuery("#sort1").click(() => sortMovieList('title', 'asc', 'rating', 'asc'));
jQuery("#sort2").click(() => sortMovieList('title', 'asc', 'rating', 'desc'));
jQuery("#sort3").click(() => sortMovieList('title', 'desc', 'rating', 'asc'));
jQuery("#sort4").click(() => sortMovieList('title', 'desc', 'rating', 'desc'));
jQuery("#sort5").click(() => sortMovieList('rating', 'asc', 'title', 'asc'));
jQuery("#sort6").click(() => sortMovieList('rating', 'asc', 'title', 'desc'));
jQuery("#sort7").click(() => sortMovieList('rating', 'desc', 'title', 'asc'));
jQuery("#sort8").click(() => sortMovieList('rating', 'desc', 'title', 'desc'));

// change dropdown
jQuery("#page-size-select").change(function () {
    let newLimit = jQuery(this).val();
    params.set('limit', newLimit);
    params.set('offset', 0);
    window.location.href = `movie-list.html?${params.toString()}`;
});

// click next
jQuery("#next-page").click(function () {
    offset = parseInt(params.get('offset') || 0);
    limit = parseInt(params.get('limit') || 10);
    params.set('offset', offset + limit);
    window.location.href = `movie-list.html?${params.toString()}`;
});

// clicking prev
jQuery("#prev-page").click(function () {
    offset = parseInt(params.get('offset') || 0);
    limit = parseInt(params.get('limit') || 10);
    params.set('offset', Math.max(0, offset - limit));
    window.location.href = `movie-list.html?${params.toString()}`;
});

// on load
// Makes the HTTP GET request and registers on success callback function handleMovieListResult
const params = new URLSearchParams(window.location.search)
let type = params.get('type');
let keyword = params.get('keyword');
let title = params.get('title');
let col1 = params.get('col1');
let limit = params.get('limit');
let offset = params.get('offset');

if (limit == null)
{
    limit = 10;
    offset = 0;
    params.set('limit', '10');
    params.set('offset', '0');
}

if (!params.get('col1')) params.set('col1', 'rating');
if (!params.get('order1')) params.set('order1', 'desc');
if (!params.get('col2')) params.set('col2', 'title');
if (!params.get('order2')) params.set('order2', 'asc');

// set dropdowwn
jQuery("#page-size-select").val(limit);

if (type || keyword || title ) {
    // there is a search or browse
    if (type && keyword)
    {
        jQuery.ajax({
            dataType: "json",
            method: "GET",
            url: `api/browse?${params.toString()}`,
            success: (resultData) => {
                handleBrowseResult(resultData)
            }
        });
    }
    else
    {
        // let searchParams = "title=" + title
        jQuery.ajax({
            dataType: "json",
            method: "GET",
            url: `api/fts?${params.toString()}`,
            success: (resultData) => {
                handleSearchResult(resultData)
            }
        });
    }
}
// movie list servlet only
else
{
    // if completely empty
    if (params.toString() === "")
    {
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            url: "api/movie-list",
            success: (resultData) => {
                handleMovieListResult(resultData)
            }
        });
    }
    else// if (col1)
    {
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            url: `api/movie-list?${params.toString()}`,
            success: (resultData) => {
                handleMovieListResult(resultData)
            }
        });
    }

}


//if page comes from a different page, i want to keep the last search data
if (performance.getEntriesByType("navigation")[0].type === "navigate") {
    jQuery.ajax({
        dataType: "json",
        method: "post",
        url: "api/maintain-search",
        success: (resultData) => {
            maintainSearch(resultData)
        }
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
        console.log("Using Cache");
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
                console.log("Lookup AJAX Error")
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

// function clearFields(){
//     console.log("clearFields called");
//     jQuery("#title").val("");
//     params.delete('title');
// }

// jQuery("#searchResetForm").on('submit', function (event) {
//     event.preventDefault(); // prevent reload
//
//     // Clear all fields
//     clearFields();
//
//     // Reset params
//     params.delete('title');
//
//     params.set('limit', 10);
//     params.set('offset', 0);
//
//     // Redirect to fresh movie list page
//     window.location.href = `movie-list.html?${params.toString()}`;
// });