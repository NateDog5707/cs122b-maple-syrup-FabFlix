/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleBrowseResult(resultData) {
    // console.log("handleBrowseResult: populating table from resultData");

    // Find the empty table body by id "search_table_body"
    let browseBodyElement = jQuery("#movie_list_table_body");
    browseBodyElement.empty();

    // Iterate through resultData

    for (let i = 0; i < resultData.length; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";

        rowHTML += "<td>" + "<button class='addToCartButton' " +
            "movieId='"+ resultData[i]['movie_id']+"'" +
            "movieName='"+ resultData[i]['movie_title']+"'" +
            "moviePrice='"+ resultData[i]['movie_price']+"'" +
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


        rowHTML += "<td>"+ resultData[i]['movie_rating']+"</td>"
        rowHTML += "<td>"+ resultData[i]['movie_price'] +"</td>"
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        browseBodyElement.append(rowHTML);
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

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

function sortMovieList(col1, order1, col2, order2)
{
    const params = new URLSearchParams(window.location.search)
    params.set('col1', col1);
    params.set('order1', order1);
    params.set('col2', col2);
    params.set('order2', order2);

    window.location.href = `movie-list.html?${params.toString()}`;
}

jQuery("#sort1").click(() => sortMovieList('title', 'asc', 'rating', 'asc'));
jQuery("#sort2").click(() => sortMovieList('title', 'asc', 'rating', 'desc'));
jQuery("#sort3").click(() => sortMovieList('title', 'desc', 'rating', 'asc'));
jQuery("#sort4").click(() => sortMovieList('title', 'desc', 'rating', 'desc'));
jQuery("#sort5").click(() => sortMovieList('rating', 'asc', 'title', 'asc'));
jQuery("#sort6").click(() => sortMovieList('rating', 'asc', 'title', 'desc'));
jQuery("#sort7").click(() => sortMovieList('rating', 'desc', 'title', 'asc'));
jQuery("#sort8").click(() => sortMovieList('rating', 'desc', 'title', 'desc'));


function loadOptions()
{
    // Makes the HTTP GET request and registers on success callback function handleSearchResult
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/browse",
        success: (genres) => {
            let genreSection = jQuery("#genre_browse");
            for (let i = 0; i < genres.length; i++) {
                let genreName = genres[i]["genre_name"];
                let link = `<a href="movie-list.html?type=genre&keyword=${genreName}&limit=10&offset=0">${genreName} </a>`;
                genreSection.append(link);
            }
        }
    });

    let letters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ*".split('');
    let letterSection = jQuery("#title_browse");
    for (let i = 0; i < letters.length; i++)
    {
        let link = `<a href="movie-list.html?type=title&keyword=${letters[i]}&limit=10&offset=0">${letters[i]} </a>`;
        letterSection.append(link);
    }
}

loadOptions();