function validateIPaddress(ipaddress) {
    if (/^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/.test(ipaddress)) {
        return (true)
    }
    return (false)
}

$("#btnAddMonitor").on("click", () => {
    console.log("click");
    var type = "";

    if ($("#tcptype").prop("checked")) {
        type = "tcp";
    } else if ($("#udptype").prop("checked")) {
        type = "udp";
    } else type = "icmp";

    var id = $("#monitor-id").val();
    var name = $("#monitor-name").val();
    var address = $("#monitor-ip").val();
    var pool_id = $("#pool").val();

    let data = {
        id,
        name,
        pool_id,
        address,
        type,
    };
    console.log(data);

    if (validateIPaddress(address)) {
        console.log("aaabbbb")
        $.ajax({
            url: "http://localhost:8080/quantum/v1.0/pools/" + pool_id + "/health_monitors/",
            type: "post",
            dataType: "json",
            data: JSON.stringify(data),
            success: (response) => {
                loadMonitors();
                id = $("#monitor-id").val("");
                protocol = "";
                name = $("#monitor-name").val("");
                address = $("#monitor-ip").val("");
                pool_id = $("#pool").val("")
                $("#tcptype").prop("checked", false);
                $("#udptype").prop("checked", false);
                $("#icmptype").prop("checked", false);
                $("#addMonitorModal").modal("toggle");
            },
        });
    } else {
        console.log("falhou")
        $("#monitoriperror").append("<div class='alert alert-danger' role='alert'>Invalid IP address!</div>")
        setTimeout(function() { $("#monitoriperror").html("") }, 6000);
    }

});
$("#tcp").on("click", () => {
    console.log("tcp");
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