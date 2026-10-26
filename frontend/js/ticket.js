document.addEventListener("DOMContentLoaded", function () {

    // Get booking saved by booking.js
    const booking =
        JSON.parse(
            localStorage.getItem("booking")
        );

    // Get selected train
    const selectedTrain =
        JSON.parse(
            localStorage.getItem("selectedTrain")
        );


    // ==========================================
    // CHECK BOOKING
    // ==========================================

    if (!booking) {

        alert("No booking found.");

        window.location.href =
            "index.html";

        return;
    }


    // ==========================================
    // PNR
    // ==========================================

    const pnrElement =
        document.getElementById("pnr");

    if (pnrElement) {

        pnrElement.textContent =
            booking.pnr ||
            localStorage.getItem("lastPNR") ||
            "--";
    }


    // ==========================================
    // PASSENGER NAME
    // ==========================================

    const passengerElement =
        document.getElementById("passenger");

    if (passengerElement) {

        passengerElement.textContent =
            booking.passenger_name ||
            booking.passenger ||
            booking.name ||
            "--";
    }


    // ==========================================
    // TRAIN
    // ==========================================

    const trainElement =
        document.getElementById("train");

    if (trainElement) {

        if (selectedTrain) {

            trainElement.textContent =
                `${selectedTrain.number} - ${selectedTrain.name}`;

        } else {

            trainElement.textContent =
                booking.train_name ||
                "--";
        }
    }


    // ==========================================
    // FROM
    // ==========================================

    const fromElement =
        document.getElementById("from");

    if (fromElement) {

        fromElement.textContent =
            booking.source ||
            booking.from ||
            "--";
    }


    // ==========================================
    // TO
    // ==========================================

    const toElement =
        document.getElementById("to");

    if (toElement) {

        toElement.textContent =
            booking.destination ||
            booking.to ||
            "--";
    }


    // ==========================================
    // CLASS
    // ==========================================

    const classElement =
        document.getElementById("class");

    if (classElement) {

        classElement.textContent =
            booking.travel_class ||
            booking.travelClass ||
            booking.class ||
            "--";
    }


    // ==========================================
    // FARE
    // ==========================================

    const fareElement =
        document.getElementById("fare");

    if (fareElement) {

        const fare =
            booking.fare;

        fareElement.textContent =
            fare !== undefined
                ? `₹${fare}`
                : "₹--";
    }

});