function handleMetadata(resultData) {
    console.log("handleMetadata: populating metadata info from resultData");

    let tableBody = jQuery("#metadata_table_body");
    tableBody.empty(); // clear existing content

    // map of maps to keep track of each table + attributes
    // {table_name: {col_name: name, col_type: type}}
    let groupedData = {};
    for (let i = 0; i < resultData.length; i++) {
        const tableName = resultData[i]["table_name"];
        if (!groupedData[tableName]) {
            groupedData[tableName] = [];
        }
        groupedData[tableName].push({
            column_name: resultData[i]["column_name"],
            column_type: resultData[i]["column_type"]
        });
    }

    for (const [tableName, columns] of Object.entries(groupedData)) {
        // Add a row to indicate the table name
        let tableRow = `<tr><td colspan="3"><strong>Table: ${tableName}</strong></td></tr>`;
        tableBody.append(tableRow);

        // Add rows for each column in the table
        for (let col of columns) {
            let rowHTML = "<tr>";
            rowHTML += "<td></td>";
            rowHTML += `<td>${col.column_name}</td>`;
            rowHTML += `<td>${col.column_type}</td>`;
            rowHTML += "</tr>";
            tableBody.append(rowHTML);
        }
    }
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "/cs122b-maple-project/api/_dashboard/view-metadata",
    success: (resultData) => handleMetadata(resultData)
});