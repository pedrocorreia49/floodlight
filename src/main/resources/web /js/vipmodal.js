function validateIPaddress(ipaddress){
    if (/^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/.test(ipaddress)){
        return (true)
    }
    return (false)
}

function validatePort(port){
    let aux = parseInt(port)
    if(port <= 65535) return true
    return false
}

$("#btnAddVIP").on("click", () => {

    console.log("click");
    var protocol = "";

    if ($("#tcp").is(":checked")) protocol = "tcp";
    else if ($("#udp").is(":checked")) protocol = "udp";
    else protocol = "icmp";

    var id = $("#vip-id").val();
    var name = $("#vip-name").val();
    var address = $("#vip-ip").val();
    var port = $("#vip-port").val();
    let data = {
        id,
        name,
        protocol,
        address,
        port,
    };

    
    if(validateIPaddress(address)){
        if(validatePort(port)){
            $.ajax({
                url: "http://localhost:8080/quantum/v1.0/vips/",
                type: "post",
                dataType: "json",
                data: JSON.stringify(data),
                success: (response) => {
                    loadVIPS();
                    id = $("#vip-id").val("");
                    protocol = "";
                    name = $("#vip-name").val("");
                    address = $("#vip-ip").val("");
                    port = $("#vip-port").val("");
                    $("#tcp").prop("checked", false);
                    $("#udp").prop("checked", false);
                    $("#icmp").prop("checked", false);
                    $("#addVIPModal").modal("toggle");
                },
                error: (error) => {
                    console.log(error.status);
                    alert("")
                },
            });
        }else{
            $("#porterror").append("<div class='alert alert-danger' role='alert'>Invalid port!</div>")
            setTimeout(function(){$("#porterror").html("")}, 6000);
        }

    }else{
        $("#iperror").append("<div class='alert alert-danger' role='alert'>Invalid IP address!</div>")
        setTimeout(function(){$("#iperror").html("")}, 6000);

    }


});

$("#vip-modal button").on("click", function(e) {
    console.log("CLICK");
    var $target = $(e.target);
    console.log($target);
    //$(this).closest('.modal').on('hidden.bs.modal', function (e) {
    //    alert('The buttons id that closed the modal is: #' + $target[0].id);
    //});
    if ($target[0].id == "start-button") {
        $("#vip-modal").on("hide.bs.modal", function(e) {
            //setIpAddress();
            if (!checkConnection($("#ipaddress").val(), $("#restport").val())) {
                e.preventDefault();
                e.stopImmediatePropagation();
                return false;
            } else setIpAddress();
        });
    }
});

function checkConnection(ip, port) {
    var returnMessage = $.ajax({
        url: "http://" + ip + ":" + port + "/wm/core/health/json",
        async: false,
        success: function(data) {
            console.log("Connection made!");
            return data;
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.log("Connection cannot be made! :(");
        },
    });

    if (returnMessage.statusText == "OK") return true;
    else return false;
}

function setIpAddress() {
    var ip = $("#ipaddress").val();
    var port = $("#restport").val();

    $.cookie("cip", ip, { expires: 7 });
    $.cookie("cport", port, { expires: 7 });

    var readed = $.cookie("cip");

    if (readed != null && readed != "") {
        console.log("correct!");
        ipaddress = ip;
        restport = port;
    } else {
        console.log("Can not set the ip address!");
    }
}