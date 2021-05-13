$("#btnAddPool").on("click", () => {
    console.log("click");
    var protocol = "";
    var lb_method = "";

    if ($("#rr").is(":checked")) lb_method = "rr";
    else if ($("#wrr").is(":checked")) lb_method = "wrr";
    else if ($("#statistics").is(":checked")) lb_method = "statistics";
    else lb_method = "slp";

    if ($("#tcptype").prop("checked")) {
        protocol = "tcp";
    } else if ($("#udptype").prop("checked")) {
        protocol = "udp";
    } else protocol = "icmp";

    var id = $("#pool-id").val();
    var vip_id = $("#vip").val();
    var name = $("#pool-name").val();
    var timeout = $("#pool-timeout").val();

    let data = {
        id,
        name,
        lb_method,
        protocol,
        vip_id,
        timeout: parseInt(timeout),
    };
    console.log(vip_id);
    console.log(JSON.stringify(data));

    $.ajax({
        url: "http://localhost:8080/quantum/v1.0/pools/",
        type: "post",
        dataType: "json",
        data: JSON.stringify(data),
        success: (response) => {
            loadPools();
            id = $("#pool-id").val("");
            protocol = "";
            lb_method = " ";
            name = $("#pool-name").val("");
            timeout = $("#pool-timeout").val("");
            $("#tcptype").prop("checked", false);
            $("#udptype").prop("checked", false);
            $("#icmptype").prop("checked", false);
            $("#rr").prop("checked", false);
            $("#wrr").prop("checked", false);
            $("#statistics").prop("checked", false);
            $("#slp").prop("checked", false);
            $("#closeModal").click();
        },
        error: (error) => {
            console.log(error);
        },
    });
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