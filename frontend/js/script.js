document.addEventListener("DOMContentLoaded", function () {

    // Get selected train
    const selectedTrain =
        JSON.parse(localStorage.getItem("selectedTrain"));

    // Get completed booking
    const booking =
        JSON.parse(localStorage.getItem("booking"));

    // If neither exists
    if (!selectedTrain && !booking) {
        alert("No booking found.");
        return;
    }

    // ------------------------------------------
    // TRAIN INFORMATION
    // ------------------------------------------

    const train = selectedTrain || {};

    // ------------------------------------------
    // BOOKING INFORMATION
    // ------------------------------------------

    const data = booking || {};

    // ------------------------------------------
    // PNR
    // ------------------------------------------

    const pnrElement =
        document.getElementById("pnr");

    if (pnrElement) {
        pnrElement.textContent =
            data.pnr || generatePNR();
    }

    // ------------------------------------------
    // PASSENGER
    // ------------------------------------------

    const passengerElement =
        document.getElementById("passenger");

    if (passengerElement) {
        passengerElement.textContent =
            data.passenger ||
            data.name ||
            data.passengerName ||
            "--";
    }

    // ------------------------------------------
    // TRAIN
    // ------------------------------------------

    const trainElement =
        document.getElementById("train");

    if (trainElement) {
        trainElement.textContent =
            train.number && train.name
                ? `${train.number} - ${train.name}`
                : train.number || "--";
    }

    // ------------------------------------------
    // FROM
    // ------------------------------------------

    const fromElement =
        document.getElementById("from");

    if (fromElement) {
        fromElement.textContent =
            train.from || data.from || "--";
    }

    // ------------------------------------------
    // TO
    // ------------------------------------------

    const toElement =
        document.getElementById("to");

    if (toElement) {
        toElement.textContent =
            train.to || data.to || "--";
    }

    // ------------------------------------------
    // CLASS
    // ------------------------------------------

    const classElement =
        document.getElementById("class");

    if (classElement) {
        classElement.textContent =
            data.class ||
            data.travelClass ||
            data.coach ||
            "--";
    }

    // ------------------------------------------
    // FARE
    // ------------------------------------------

    const fareElement =
        document.getElementById("fare");

    if (fareElement) {
        const fare =
            data.fare ||
            data.amount ||
            data.price;

        fareElement.textContent =
            fare ? `₹${fare}` : "₹--";
    }

});


// ==========================================
// GENERATE PNR
// ==========================================

function generatePNR() {

    const pnr =
        Math.floor(
            1000000000 +
            Math.random() * 9000000000
        );

    return pnr.toString();
}