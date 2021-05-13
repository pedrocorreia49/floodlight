var ipaddress = $.cookie("cip");
var port = $.cookie("cport");
var id = "";
if (ipaddress == null || ipaddress == "") window.location.href = "login.html";

$(document).ready(function() {
    $("#pagetitle").append(
        " <h3  class='page-header'>Pool #" + getId() + " Members</h3>"
    );
    loadMembers();
});

function getId() {
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
    console.log(reverseArray);
    return joinArray;
}

$("#deleteAllMembers").on("click", () => {
    membersTable.rows().every(function(rowIdx, tableLoop, rowLoop) {
        var data = this.data();
        deleteMember(data.id);
    });
});

function loadMembers() {
    console.log("Loading members...");

    membersTable = $("#tableMembers").DataTable({
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
            url: "http://localhost:8080/quantum/v1.0/pools/" + id + "/members/",
            type: "GET",
            dataSrc: "",
            async: true,
        },
        columnDefs: [{
            render: function(data, type, row) {
                return (
                    "<a class='btn btn-md btn-danger id='btnDeleteMember' onclick='deleteMember(" +
                    row.id +
                    ")''>Delete</<a>"
                );
            },
            targets: 9,
        }, ],
        columns: [
            { data: "id" },
            { data: "address" },
            { data: "port" },
            { data: "poolId" },
            { data: "vipId" },
            { data: "status" },
            { data: "weight" },
            { data: "flows" },
            { data: "bytesIn" }
        ],
    });
}

function deleteMember(memberid) {
    console.log(memberid)
    $.ajax({
        type: "DELETE",
        dataType: "json",
        url: "http://" +
            ipaddress +
            ":" +
            restport +
            "/quantum/v1.0/members/" +
            memberid +
            "/",
        success: function(data) {
            loadMembers();
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