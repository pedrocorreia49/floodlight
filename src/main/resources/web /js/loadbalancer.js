var ipaddress = $.cookie("cip");
if (ipaddress == null || ipaddress == "") window.location.href = "login.html";
var restport = $.cookie("cport");
if (restport == null || restport == "") window.location.href = "login.html";
let vipsTable;
let poolsTable;

$(document).ready(function() {
    console.log("ready");
    $("#login-modal-include").load("loginmodal.html");
    $("#vip-modal-include").load("vipmodal.html");
    $("#pool-modal-include").load("poolmodal.html");
    $("#monitor-modal-include").load("monitormodal.html");
    loadVIPS();
    loadPools();
    loadMonitors();
    startRefresh();
});

function startRefresh() {
    setTimeout(startRefresh, 15000);
    vipsTable.ajax.reload();
    poolsTable.ajax.reload();
    monitorsTable.ajax.reload();
}

$("#deleteAllVIPS").on("click", () => {
    vipsTable.rows().every(function(rowIdx, tableLoop, rowLoop) {
        var data = this.data();
        deleteVIP(data.id);
    });
});

$("#deleteAllPools").on("click", () => {
    poolsTable.rows().every(function(rowIdx, tableLoop, rowLoop) {
        var data = this.data();
        deletePool(data.id);
    });
});

$("#deleteAllMonitors").on("click", () => {
    monitorsTable.rows().every(function(rowIdx, tableLoop, rowLoop) {
        var data = this.data();
        deleteMonitor(data.id);
    });
});

function loadVIPS() {
    vipsTable = $("#tableVIPS").DataTable({
        responsive: true,
        searching: false,
        lengthChange: false,
        destroy: true,
        scrollX: true,
        paging: false,
        order: [
            [0, "asc"]
        ],
        ajax: {
            url: "http://" + ipaddress + ":" + restport + "/quantum/v1.0/vips/",
            type: "GET",
            dataSrc: "",
            async: true,
        },
        columnDefs: [{
            render: function(data, type, row) {
                return (
                    "<a class='btn btn-md btn-danger' onclick='deleteVIP(" +
                    row.id +
                    ")'>Delete</<a>"
                );
            },
            targets: 5,
        }, ],
        columns: [
            { data: "id" },
            { data: "name" },
            { data: "protocol" },
            { data: "address" },
            { data: "port" },
        ],
    });
}

function loadPools() {
    console.log("loading pooltable");
    poolsTable = $("#tablePools").DataTable({
        responsive: true,
        searching: false,
        lengthChange: false,
        destroy: true,
        scrollX: true,
        paging: false,
        order: [
            [0, "asc"]
        ],
        ajax: {
            url: "http://localhost:8080/quantum/v1.0/pools/",
            type: "GET",
            dataSrc: "",
            async: true,
        },
        columnDefs: [{
                render: function(data, type, row) {
                    return (
                        "<a class='btn btn-md btn-primary' href='members.html?id=" +
                        row.id +
                        "'>Members</<a>"
                    );
                },
                targets: 5,
            },
            {
                render: function(data, type, row) {
                    console.log(row.id);
                    return (
                        "<a class='btn btn-md btn-danger' onclick='deletePool(" +
                        row.id +
                        ")'>Delete</<a>"
                    );
                },
                targets: 6,
            },
        ],
        columns: [
            { data: "id" },
            { data: "name" },
            { data: "vipId" },
            { data: "lbMethod" },
            { data: "Timeout" },
        ],
    });
}

function loadMonitors() {
    monitorsTable = $("#tableMonitors").DataTable({
        responsive: true,
        searching: false,
        lengthChange: false,
        destroy: true,
        scrollX: true,
        paging: false,
        order: [
            [0, "asc"]
        ],
        ajax: {
            url: "http://localhost:8080/quantum/v1.0/health_monitors/",
            type: "GET",
            dataSrc: "",
            async: true,
        },
        columnDefs: [{
                render: function(data, type, row) {

                    return (
                        "<a id='monitorstate' class='btn btn-md btn-primary' onclick='toggleMonitorState(" +
                        row.id +
                        ")'>Enable</<a>"
                    );
                },
                targets: 5,
            },
            {
                render: function(data, type, row) {
                    return (
                        "<a class='btn btn-md btn-danger' onclick='deleteMonitor(" +
                        row.id +
                        ")'>Delete</<a>"
                    );
                },
                targets: 6,
            },
        ],
        columns: [
            { data: "id" },
            { data: "name" },
            { data: "address" },
            { data: "poolId" },
            { data: "type" },
        ],
    });
}

function deleteVIP(vipid) {
    $.ajax({
        type: "DELETE",
        dataType: "json",
        url: "http://" +
            ipaddress +
            ":" +
            restport +
            "/quantum/v1.0/vips/" +
            vipid +
            "/",
        success: function(data) {
            loadVIPS();
        },
        error: function(jqXHR, textStatus, errorThrown) {
            alert(
                "Error: " +
                " " +
                jqXHR.responseText +
                " \n Status: " +
                textStatus +
                " \n Error Thrown: " +
                errorThrown
            );
        },
    });
}

function deletePool(poolid) {
    $.ajax({
        type: "DELETE",
        dataType: "json",
        url: "http://" +
            ipaddress +
            ":" +
            restport +
            "/quantum/v1.0/pools/" +
            poolid +
            "/",
        success: function(data) {
            loadPools();
        },
        error: function(jqXHR, textStatus, errorThrown) {
            alert(
                "Error: " +
                " " +
                jqXHR.responseText +
                " \n Status: " +
                textStatus +
                " \n Error Thrown: " +
                errorThrown
            );
        },
    });
}

function deleteMonitor(monitorid) {
    $.ajax({
        type: "DELETE",
        dataType: "json",
        url: "http://" +
            ipaddress +
            ":" +
            restport +
            "/quantum/v1.0/health_monitors/" +
            monitorid +
            "/",
        success: function(data) {
            loadMonitors();
        },
        error: function(jqXHR, textStatus, errorThrown) {
            alert(
                "Error: " +
                " " +
                jqXHR.responseText +
                " \n Status: " +
                textStatus +
                " \n Error Thrown: " +
                errorThrown
            );
        },
    });
}

function toggleMonitorState(monitorid) {
    console.log(monitorid);
    let state = $("#monitorstate").html();

    if (state.indexOf("Enable") !== -1) {
        $("#monitorstate").html("Disable");
    } else {
        $("#monitorstate").html("Enable");
    }
}