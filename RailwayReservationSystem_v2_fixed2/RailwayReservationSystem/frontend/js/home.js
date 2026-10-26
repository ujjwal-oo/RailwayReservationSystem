document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("searchForm");
    const list = document.getElementById("trainList");
    const results = document.getElementById("results");
    const swap = document.getElementById("swap");
    const newSearch = document.getElementById("newSearch");
    const dateInput = document.getElementById("date");

    dateInput.min = new Date().toISOString().slice(0, 10);

    const safe = (value) => String(value ?? "").replace(/[&<>"']/g, ch => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    })[ch]);

    function showMessage(title, message, isError = false) {
        list.innerHTML = `<div class="no-trains ${isError ? "error-state" : ""}">
            <h3>${safe(title)}</h3><p>${safe(message)}</p></div>`;
    }

    function time(value) {
        if (!value) return "—";
        const match = String(value).match(/T(\d{2}:\d{2})/);
        return match ? match[1] : String(value).slice(0, 5);
    }

    function trainFromApi(item, from, to) {
        const train = item.train || item;
        const origin = item.from || {};
        const destination = item.to || {};
        const live = item.live || {};
        const number = String(train.number || train.trainNumber || "");
        const name = train.name || train.trainName || "Train";
        const departure = origin.departure || origin.scheduledDeparture || "";
        const arrival = destination.arrival || destination.scheduledArrival || "";
        const delay = Number(live.delayMinutes || 0);
        const status = live.type || live.status || "";
        return {
            number, name,
            from: origin.name || from,
            to: destination.name || to,
            departure: time(departure),
            arrival: time(arrival),
            seats: null,
            price: 1500,
            delayMinutes: delay,
            liveStatus: status,
            liveUpdated: live.lastUpdatedAt || "",
            type: train.type || train.category || "Express",
            distance: item.distance || null
        };
    }

    function renderTrains(items, from, to, date) {
        list.innerHTML = "";
        if (!items.length) {
            showMessage("No trains found", `No trains were returned for ${from} → ${to} on ${date}. Try station names or station codes.`);
            return;
        }

        items.forEach(item => {
            const train = trainFromApi(item, from, to);
            const delayText = train.liveStatus
                ? (train.delayMinutes > 0 ? `Delayed by ${train.delayMinutes} min` : train.delayMinutes < 0 ? `Early by ${Math.abs(train.delayMinutes)} min` : "Live status: on time")
                : "Live status unavailable";
            const card = document.createElement("article");
            card.className = "train-card";
            card.innerHTML = `
                <div class="train-info">
                    <div class="train-title"><h3>${safe(train.name)}</h3><span class="train-number">${safe(train.number)}</span></div>
                    <p class="route">${safe(train.from)} <span>→</span> ${safe(train.to)}</p>
                    <div class="train-times"><div><small>DEPARTURE</small><strong>${safe(train.departure)}</strong></div>
                    <div><small>ARRIVAL</small><strong>${safe(train.arrival)}</strong></div></div>
                    <p class="live-status">${safe(delayText)}</p>
                    <p class="train-type">${safe(train.type)}${train.distance ? ` · ${safe(train.distance)} km` : ""}</p>
                </div>
                <div class="train-action"><p>Fare shown during booking</p><button class="book-btn" type="button">Book Now</button></div>`;

            card.querySelector(".book-btn").addEventListener("click", () => {
                localStorage.setItem("selectedTrain", JSON.stringify(train));
                localStorage.setItem("journeyDate", date);
                localStorage.removeItem("booking");
                window.location.href = "booking.html";
            });
            list.appendChild(card);
        });
    }

    form.addEventListener("submit", async event => {
        event.preventDefault();
        const from = document.getElementById("from").value.trim();
        const to = document.getElementById("to").value.trim();
        const date = dateInput.value;
        if (!from || !to || !date) {
            showMessage("Missing details", "Enter source, destination and journey date.", true);
            results.classList.remove("hidden");
            return;
        }
        if (from.toLowerCase() === to.toLowerCase()) {
            showMessage("Choose different stations", "Source and destination cannot be the same.", true);
            results.classList.remove("hidden");
            return;
        }

        results.classList.remove("hidden");
        showMessage("Searching live trains…", "Connecting to the railway data service.");
        results.scrollIntoView({ behavior: "smooth", block: "start" });

        try {
            const url = `/api/trains?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&date=${encodeURIComponent(date)}`;
            const response = await fetch(url, { headers: { "Accept": "application/json" } });
            const data = await response.json();
            if (!response.ok || !data.success) {
                showMessage("Live train search unavailable", data.message || "The live train service is temporarily unavailable. Please try again later.", true);
                return;
            }
            renderTrains(Array.isArray(data.trains) ? data.trains : [], from, to, date);
        } catch (error) {
            showMessage("Connection problem", "Could not reach the train service. Check your internet connection and try again.", true);
        }
    });

    newSearch.addEventListener("click", () => {
        results.classList.add("hidden");
        document.getElementById("search").scrollIntoView({ behavior: "smooth" });
    });

    swap.addEventListener("click", () => {
        const from = document.getElementById("from");
        const to = document.getElementById("to");
        [from.value, to.value] = [to.value, from.value];
    });
});
