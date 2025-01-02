console.log("this is Script file")

const toggleSidebar = () => {
    if ($('.sidebar').is(":visible")) {

        $('.sidebar').css("display", "none");
        $(".content").css("margin-left", "0%");

    } else {

        $('.sidebar').css("display", "block");
        $(".content").css("margin-left", "20%");

    }

}


const search = () => {
    // console.log("Searching...");
    let query = $("#search-input").val();


    if (query == "") {
        $(".search-result").hide();

    } else {

        console.log(query);

        //sending request to server

        let url = `http://localhost:8282/search/${query}`; /* search/${query} it is url in Search Controller  */

        fetch(url)
            .then((response) => {
                return response.json();
            })

        .then((data) => {
            console.log(data);

            let text = `<div class='list-group'>`;

            data.forEach((contact) => {
                text += `<a href='/user/contact/${contact.cId}' class='list-group-item list-group-item-action'> ${contact.name} </a>  `
            });

            text += '</div>';

            $(".search-result").html(text);
            $(".search-result").show();


        });
    }
};


//first request to server to create order

const paymentStart = () => {
    console.log("Payement Started");
    var amount = $("#paymentField").val();
    console.log(amount);

    if (amount == "" || amount == null) {

        alert("Amount is required ");
        return;
    }
    
    $.ajax( 
		{
            url: "/user/create_order",
            data: JSON.stringify({ amount: amount, info: 'order_request' }),
            contentType: 'application/json',
            type: 'POST',
            dataType: 'json',
            success: function(response) {
                console.log(response);
            },
            error: function(error) {
                console.log(error)
                alert("something went wrong")
            }

        } )

};