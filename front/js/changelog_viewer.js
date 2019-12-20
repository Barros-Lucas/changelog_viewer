$( "#directory" ).change(function() {
    $("#label").text("Selected files");
});
    
//Create random Int
function getRandomInt(max) 
{
    return Math.floor(Math.random() * Math.floor(max));
}

//Create log viewer from an URL. Use an API
function fromUrl ()
{
    initialize();
    //Create request
    var requete = $("#url").val();
    var url = "http://localhost:8081/fromUrl?url="+requete;
    //Table wich contains color. It's used to random color
    var color = ["#f04f32", "#88b950", "#1fb3b2"];
    str="";
    //GET request
    $.get(url, function(data, status){
        $("#load").hide();
        //Unique values
        var tabAuthor = new Set();
        for (elt in data)
        {
            //Memorize values
            var author = data[elt]["author"];
            var date = data[elt]["date"];
            var message = data[elt]["message"];
            //Adding in str
            str += log_viewer (color[getRandomInt(3)], message, author, new Date(date))
            //Adding in table
            tabAuthor.add(author);
        }
        //Displaying
        display_viewer(str);
        //Author's list
        str="";
        for (elt of tabAuthor)
        {
            str += "&#8627; "+elt+"<br>";
        }
        //Displaying informations
        display_informations(str, data);
    }) 
}

//Display log viewer
function display_viewer (str)
{
    jQuery($("#log")).append("<div class='jumbotron'><h2>Log viewer<h2><hr>\
        <div class='row'><div class='col-md-1 line' ></div>\
        <div class='col-md-11'>"+str+"</div></div></div>");
}

//Display one commit
function log_viewer (color, message, author, date)
{
    return '\
    <div class="card text-white mb-3" style="max-width: auto; background-color : '+color+' !important">\
        <div class="card-header"><small>'+message+'</small></div>\
            <div class="card-body"><h5 class="card-title">By '+author+'</h5>\
            <p class="card-text"><small>On '+date+'</small></p>\
        </div>\
    </div>';
}

//Display infirmations
function display_informations (str, data)
{
    str = '<div class="card text-white mb-3" style="max-width: auto; background-color : #1fb3b2 !important">\
            <div class="card-header"><small>Contributors</small></div>\
                <div class="card-body"><h5 class="card-title">'+str+'</h5>\
            </div></div>\
            <div class="card text-white mb-3" style="max-width: auto; background-color : #88b950 !important">\
            <div class="card-header"><small>Commits</small></div>\
                <div class="card-body"><h5 class="card-title">'+data.length+'</h5>\
            </div>';

    jQuery($("#precision")).append("<div class='jumbotron'><h2>Informations<h2><hr>\
    <div class='col-md-11'>"+str+"</div></div>");
}

//Create a zip from an existing folder
function getZip ()
{
    var zip = new JSZip();
    var iptEls = document.querySelectorAll('#directory');
    [].forEach.call(iptEls, function(iptEl) {
        console.log(iptEl.files);
        $.each(iptEl.files,function(i,file){
            zip.file(file.webkitRelativePath, file, {
                createFolders: true // default value
            });
        })
        zip.folder(".git");
    })
    return zip;
}

//Initialize displaying
function initialize ()
{
    $("#load").show();
    $("#log").html("");
    $("#precision").html("");
}

//Create log viewer from a directory
function directory ()
{
    initialize();
    zip = getZip();
    zip.generateAsync({type:"blob"})
    .then(function(content) {
        //Create new File
        var fileObj = new File([content], ".zip");
        //Initialize a form data
        var fd = new FormData();
        fd.append('file', fileObj);
        //POST request
        $.ajax({
            type: "POST",
            enctype: 'multipart/form-data',
            url: 'http://localhost:8081/upload',
            data: fd,
            processData: false, //prevent jQuery from automatically transforming the data into a query string
            contentType: false,
            cache: false,
            timeout: 600000,
            success: function (data) {
                $("#load").hide();
                $("#btnSubmit").prop("disabled", false);
                var str = "";
                //Table wich contains color. It's used to random color
                var color = ["#f04f32", "#88b950", "#1fb3b2"];
                //Unique value
                var tabAuthor = new Set();
                for (elt in data)
                {
                    //Memorize values
                    var id = data[elt]["id"];
                    var author = data[elt]["author"]["name"];
                    var date = data[elt]["author"]["when"];
                    var message = data[elt]["message"];
                    //Adding in str
                    str += log_viewer (color[getRandomInt(3)], message, author, new Date(date))
                    //Adding in table
                    tabAuthor.add(author);
                }
                //Displaying
                display_viewer(str);
                //Author's list
                str="";
                for (elt of tabAuthor)
                {
                    str += "&#8627; "+elt+"<br>";
                }
                //Display informations
                display_informations(str, data);

            },
            error: function (e) {
                //Display error
                console.log("ERROR : ", e);
                $("#btnSubmit").prop("disabled", false);
            }
        });
        
    });
}    