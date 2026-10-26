// ==========================================
// LOAD SELECTED TRAIN
// ==========================================

document.addEventListener("DOMContentLoaded", function () {

    const selectedTrain =
        JSON.parse(
            localStorage.getItem("selectedTrain")
        );


    // ==========================================
    // CHECK SELECTED TRAIN
    // ==========================================

    if (!selectedTrain) {

        alert(
            "No train selected. Please search for a train first."
        );

        window.location.href = "index.html";

        return;
    }


    // ==========================================
    // DISPLAY TRAIN INFORMATION
    // ==========================================

    document.getElementById("trainName")
        .textContent =
        `${selectedTrain.number} - ${selectedTrain.name}`;


    document.getElementById("route")
        .textContent =
        `${selectedTrain.from} → ${selectedTrain.to}`;


    const journeyDate =
        localStorage.getItem("journeyDate");


    document.getElementById("travelDate")
        .textContent =
        journeyDate ||
        selectedTrain.date ||
        "--";


    document.getElementById("departure")
        .textContent =
        selectedTrain.departure ||
        "--";


    // ==========================================
    // DISPLAY FARE
    // ==========================================

    const fare =
        selectedTrain.price ||
        1500;


    document.getElementById("fare")
        .textContent =
        fare;


    // ==========================================
    // BOOKING FORM
    // ==========================================

    const bookingForm =
        document.getElementById("bookingForm");


    bookingForm.addEventListener(
        "submit",
        function (event) {

            event.preventDefault();


            // ======================================
            // GET PASSENGER DETAILS
            // ======================================

            const passengerName =
                document.getElementById(
                    "passengerName"
                ).value.trim();


            const age =
                document.getElementById(
                    "age"
                ).value;


            const gender =
                document.getElementById(
                    "gender"
                ).value;


            const mobile =
                document.getElementById(
                    "mobile"
                ).value;


            const email =
                document.getElementById(
                    "email"
                ).value;


            const travelClass =
                document.getElementById(
                    "travelClass"
                ).value;


            const paymentMode =
                document.getElementById(
                    "paymentMode"
                ).value;


            // ======================================
            // CREATE BOOKING OBJECT
            // ======================================

            const booking = {

                passenger_name:
                    passengerName,

                age:
                    parseInt(age),

                gender:
                    gender,

                mobile:
                    mobile,

                email:
                    email,

                train_name:
                    selectedTrain.name,

                source:
                    selectedTrain.from,

                destination:
                    selectedTrain.to,

                travel_class:
                    travelClass,

                payment_mode:
                    paymentMode,

                fare:
                    parseFloat(fare),

                status:
                    "CONFIRMED"
            };


            console.log(
                "Booking being sent:",
                booking
            );


            // ======================================
            // SEND TO JAVA WEBSERVER
            // ======================================

            fetch(
                "/api/book",
                {

                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(booking)

                }
            )


            // ======================================
            // JAVA RESPONSE
            // ======================================

            .then(function (response) {

                if (!response.ok) {

                    throw new Error(
                        "Server error: HTTP " +
                        response.status
                    );
                }

                return response.json();

            })


            .then(function (result) {

                console.log(
                    "Server response:",
                    result
                );


                // ==================================
                // BOOKING SUCCESSFUL
                // ==================================

                if (result.success) {

                    // Save PNR
                    localStorage.setItem(
                        "lastPNR",
                        result.pnr
                    );


                    // Save booking for ticket page
                    booking.pnr =
                        result.pnr;


                    localStorage.setItem(
                        "booking",
                        JSON.stringify(booking)
                    );


                    // Go to ticket page
                    window.location.href =
                        "ticket.html";

                }


                // ==================================
                // BOOKING FAILED
                // ==================================

                else {

                    document.getElementById(
                        "bookingMessage"
                    ).textContent =
                        result.message ||
                        "Booking failed.";

                }

            })


            // ======================================
            // CONNECTION ERROR
            // ======================================

            .catch(function (error) {

                console.error(
                    "Booking error:",
                    error
                );


                document.getElementById(
                    "bookingMessage"
                ).textContent =
                    "Unable to connect to Java server.";

            });

        }
    );

});