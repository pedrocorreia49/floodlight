$("#btnAddMember").on("click", () => {
    console.log(id)
    var id = $("#member-id").val();
    var address = $("#member-ip").val();
    var port = $("#member-port").val();
    let pool_id = getId()
    var weight = $("#member-weight").val();

    let data = {
        id,
        address,
        port,
        pool_id,
        weight
    };
    console.log(JSON.stringify(data));

    $.ajax({
        url: "http://localhost:8080/quantum/v1.0/members/",
        type: "post",
        dataType: "json",
        data: JSON.stringify(data),
        success: (response) => {
            loadMembers();
            id = $("#member-id").val("");
            address = $("#member-id").val("");
            port = $("#member-port").val("");
            pool_id = ""
            $("#closeModal").click();
        },
        error: (error) => {
            console.log(error);
        },
    });
});

function getId() {
    let id = ""
    let url = window.location.href;
    for (let i = url.length - 1; i >= 0; i--) {
        if (url[i] !== "=") {
            id = id + url[i];
        } else {
            break;
        }
    }
    return reverseString(id);
}

function reverseString(str) {
    var splitString = str.split("");
    var reverseArray = splitString.reverse();
    var joinArray = reverseArray.join("");
    return joinArray;
}

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