$("#artists-select").change(function () {
	var str = "";
	$("#artists-select option:selected").each(function () {
		str+=$(this).text()+", ";
	    });
	$("#albums-div").text(str);
    }).change();
