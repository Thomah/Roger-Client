function refresh() {
  overload_xhr("GET",
    "/api/sounds",
    (xhr) => {
      var json = JSON.parse(xhr.responseText);
      doc_refresh(json);
    },
    () => { },
    () => { },
    "",
    document.getElementById("refresh"));
}

function save() {
  overload_xhr(
    "PUT",
    `/api/files`,
    () => { },
    (xhr) => {
      xhr.setRequestHeader("Content-type", "application/json; charset=utf-8");
    },
    () => { },
    JSON.stringify(doc_getValues()),
    document.getElementById("save")
  );
}

function snapshot() {
  overload_xhr(
    "POST",
    `/api/karotz/snapshot`,
    () => { },
    (xhr) => {
      xhr.setRequestHeader("Content-type", "application/json; charset=utf-8");
    },
    () => { },
    document.getElementById("snapshot")
  );
}

function upload() {
  var form = new FormData();
  var file = document.querySelector("#uploadFile").files[0];
  if (file === undefined) {
    alert("No file provided");
  }
  form.append("file", file);
  form.append("matches", "");
  overload_xhr(
    'POST',
    '/api/files/upload',
    () => {
      document.getElementById("uploadFile").value = "";
      refresh();
    },
    () => { },
    () => { },
    form,
    document.getElementById("upload")
  );
}

var play = function play() {
  var textButton = this.firstChild.parentElement;
  var row = textButton.parentElement.parentElement.parentElement;
  var url = row.getElementsByClassName("url")[0].href;
  var table = row.parentElement;
  overload_xhr(
    "POST",
    `/api/karotz/sound?url=${url}`,
    () => {
      refresh();
    },
    () => { },
    () => { }
  );
};

var remove = function remove() {
  var textButton = this.firstChild.parentElement;
  var row = textButton.parentElement.parentElement.parentElement;
  var table = row.parentElement;
  overload_xhr(
    "DELETE",
    `/api/files/${row.id}`,
    () => {
      refresh();
    },
    () => { },
    () => { }
  );
};

function doc_refresh(json) {
  var table = document
    .getElementById("files")
    .getElementsByTagName("tbody")[0];
  table.style.display = "none";

  // Delete previous entries
  var rowCount = table.childNodes.length;
  for (var x = rowCount - 1; x >= 0; x--) {
    table.removeChild(table.childNodes[x]);
  }

  // Append new entries
  var rowId, row, newEntry, cell, button, cellSpan, link;
  for (rowId in json) {
    row = json[rowId];
    newEntry = document.createElement("tr");
    newEntry.id = row.id;

    // Original Name
    cell = document.createElement("td");
    cell.classList.add("originalName");
    cell.textContent = row.originalName;
    newEntry.appendChild(cell);

    // Actions
    cell = document.createElement("td");
    cellSpan = document.createElement("span");
    cellSpan.classList.add("action-bar");

    // -- Download Button
    button = document.createElement("button");
    button.classList.add("btn");
    button.classList.add("btn-secondary");
    link = document.createElement("a")
    link.classList.add("url");
    link.href = row.url
    link.textContent = "Download"
    button.appendChild(link);
    cellSpan.appendChild(button);

    // -- Play Button
    button = document.createElement("button");
    button.classList.add("btn");
    button.classList.add("btn-secondary");
    button.appendChild(document.createTextNode("Play"));
    button.onclick = play;
    cellSpan.appendChild(button);

    // -- Delete Button
    button = document.createElement("button");
    button.classList.add("btn");
    button.classList.add("btn-secondary");
    button.appendChild(document.createTextNode("Delete"));
    button.onclick = remove;
    cellSpan.appendChild(button);

    cell.appendChild(cellSpan);
    newEntry.appendChild(cell);

    table.appendChild(newEntry);
  }

  table.style.display = "table-row-group";
}

function doc_getValues() {
  var table = document
    .getElementById("files")
    .getElementsByTagName("tbody")[0];

  var row, values = [];
  for (var x = 0; x < table.childNodes.length; x++) {
    row = table.childNodes[x];
    values.push({
      id: row.id,
      name: row.getElementsByClassName("name")[0].textContent,
      originalName: row.getElementsByClassName("originalName")[0].textContent,
      directory: row.getElementsByClassName("directory")[0].textContent,
      format: row.getElementsByClassName("format")[0].textContent,
      matches: row.getElementsByClassName("matches")[0].value,
      url: row.getElementsByClassName("url")[0].href
    });
  }

  return values;
}