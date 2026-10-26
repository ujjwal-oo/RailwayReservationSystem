
document.addEventListener("DOMContentLoaded", function () {

    const selectedTrain = JSON.parse(localStorage.getItem("selectedTrain"));

    if (!selectedTrain) {
        alert("No train selected. Please search for a train first.");
        window.location.href = "index.html";
        return;
    }

    const journeyDate =
        localStorage.getItem("journeyDate") ||
        new Date().toISOString().slice(0, 10);

    const baseFare = parseFloat(selectedTrain.price || 1500);

    const $ = (id) => document.getElementById(id);
    $("trainName").textContent = `${selectedTrain.number} - ${selectedTrain.name}`;
    $("route").textContent = `${selectedTrain.from} → ${selectedTrain.to}`;
    $("travelDate").textContent = journeyDate;
    $("departure").textContent = selectedTrain.departure || "--";
    $("fare").textContent = baseFare;

    const ageInput = $("age");
    const classSelect = $("travelClass");
    const prefSelect = $("berthPreference");
    let timer = null;

    function refreshInfo() {
        clearTimeout(timer);
        timer = setTimeout(loadInfo, 250);
    }

    async function loadInfo() {

        const age = parseInt(ageInput.value);
        const cls = classSelect.value;
        if (cls) {
            try {
                const q = new URLSearchParams({
                    train: selectedTrain.name,
                    date: journeyDate,
                    class: cls
                });
                const a = await (await fetch("/api/availability?" + q)).json();
                showAvailability(a, cls);
            } catch (e) {  }
        } else {
            $("berthInfo").classList.add("hidden");
        }
        if (!age || age < 1) { showFare(null); return; }

        try {
            const q = new URLSearchParams({
                age: age,
                class: cls || "",
                fare: baseFare,
                preference: prefSelect.value
            });
            const quote = await (await fetch("/api/quote?" + q)).json();
            showFare(quote);
            showPolicy(quote, cls);
        } catch (e) {  }
    }

    function showAvailability(a, cls) {
        const box = $("berthInfo");
        box.classList.remove("hidden");

        if (!a.success || a.capacity === 0) {
            $("availabilityText").textContent = "";
            return;
        }

        if (a.available === 0) {
            $("availabilityText").textContent =
                `${cls} is full on ${journeyDate}. You will be placed on the waiting list.`;
        } else if (cls.toUpperCase() === "GENERAL") {
            $("availabilityText").textContent =
                `${a.available} of ${a.capacity} seats available on ${journeyDate}.`;
        } else {
            $("availabilityText").textContent =
                `${a.available} of ${a.capacity} berths available on ${journeyDate} ` +
                `(${a.lower_available} lower).`;
        }
    }

    function showPolicy(quote, cls) {
        const isGeneral = (cls || "").toUpperCase() === "GENERAL";
        const senior = quote.berth_policy === "LOWER_PRIORITY";
        prefSelect.disabled = isGeneral || senior;
        if (senior) prefSelect.value = "NONE";

        const text = $("berthPolicyText");
        text.textContent = isGeneral ? "" : quote.berth_message;
        text.className = senior ? "senior-note" : "";
        if (!cls) text.textContent = "";
    }

    function showFare(quote) {
        const fareEl = $("fare");
        const oldFare = $("oldFare");
        const note = $("fareNote");

        if (!quote || !quote.success) {
            fareEl.textContent = baseFare;
            oldFare.classList.add("hidden");
            note.textContent = "Inclusive of all charges";
            return;
        }

        fareEl.textContent = quote.final_fare;

        if (quote.final_fare !== baseFare) {
            oldFare.textContent = "₹" + baseFare;
            oldFare.classList.remove("hidden");
        } else {
            oldFare.classList.add("hidden");
        }

        const labels = {
            SENIOR_CITIZEN: "Senior citizen concession (40% off)",
            CHILD: "Child fare (50% off)",
            TATKAL: "Tatkal premium (+30%)",
            REGULAR: "Inclusive of all charges"
        };
        note.textContent = labels[quote.fare_category] || "Inclusive of all charges";
    }

    ageInput.addEventListener("input", refreshInfo);
    classSelect.addEventListener("change", refreshInfo);
    prefSelect.addEventListener("change", refreshInfo);
    $("bookingForm").addEventListener("submit", function (event) {

        event.preventDefault();

        const message = $("bookingMessage");
        message.textContent = "";

        const booking = {
            passenger_name: $("passengerName").value.trim(),
            age: parseInt(ageInput.value),
            gender: $("gender").value,
            mobile: $("mobile").value,
            email: $("email").value,
            train_name: selectedTrain.name,
            source: selectedTrain.from,
            destination: selectedTrain.to,
            travel_class: classSelect.value,
            payment_mode: $("paymentMode").value,
            journey_date: journeyDate,
            berth_preference: prefSelect.disabled ? "NONE" : prefSelect.value,
            fare: baseFare            // base fare; the server applies the fare strategy
        };

        fetch("/api/book", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(booking)
        })
            .then(function (response) {
                return response.json().catch(function () {
                    throw new Error("Server error: HTTP " + response.status);
                });
            })
            .then(function (result) {

                if (result.success) {
                    localStorage.setItem("lastPNR", result.pnr);
                    window.location.href = "ticket.html?pnr=" + result.pnr;
                } else {
                    message.textContent = result.message || "Booking failed.";
                }
            })
            .catch(function (error) {
                console.error("Booking error:", error);
                message.textContent = "Unable to connect to Java server.";
            });
    });
});
