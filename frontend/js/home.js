document.addEventListener("DOMContentLoaded", function () {

    const trains = [
        {
            number: "12951",
            name: "Mumbai Rajdhani",
            from: "Mumbai",
            to: "Delhi",
            departure: "17:00",
            arrival: "08:35",
            seats: 42,
            price: 2450
        },
        {
            number: "12953",
            name: "August Kranti Rajdhani",
            from: "Mumbai",
            to: "Delhi",
            departure: "17:40",
            arrival: "10:00",
            seats: 35,
            price: 2350
        },
        {
            number: "12903",
            name: "Golden Temple Mail",
            from: "Mumbai",
            to: "Delhi",
            departure: "18:45",
            arrival: "09:15",
            seats: 28,
            price: 1850
        },
        {
            number: "12925",
            name: "Paschim Express",
            from: "Mumbai",
            to: "Delhi",
            departure: "11:35",
            arrival: "09:00",
            seats: 51,
            price: 1650
        }
    ];

    const searchForm = document.getElementById("searchForm");
    const trainList = document.getElementById("trainList");
    const results = document.getElementById("results");
    const newSearch = document.getElementById("newSearch");
    const swap = document.getElementById("swap");

    searchForm.addEventListener("submit", function (event) {

        event.preventDefault();

        const from = document
            .getElementById("from")
            .value
            .trim();

        const to = document
            .getElementById("to")
            .value
            .trim();

        const date = document
            .getElementById("date")
            .value;

        if (!from || !to || !date) {
            alert("Please enter From, To and Journey Date.");
            return;
        }

        const matchingTrains = trains.filter(function (train) {

            return (
                train.from.toLowerCase() === from.toLowerCase() &&
                train.to.toLowerCase() === to.toLowerCase()
            );

        });

        trainList.innerHTML = "";

        if (matchingTrains.length === 0) {

            trainList.innerHTML = `
                <div class="no-trains">
                    <h3>No trains found</h3>
                    <p>
                        No trains are available for
                        ${from} → ${to}.
                    </p>
                </div>
            `;

        } else {

            matchingTrains.forEach(function (train) {

                const card = document.createElement("div");

                card.className = "train-card";

                card.innerHTML = `
                    <div>
                        <h3>${train.number} - ${train.name}</h3>

                        <p>
                            ${train.from} → ${train.to}
                        </p>

                        <p>
                            ${train.departure}
                            →
                            ${train.arrival}
                        </p>

                        <p>
                            Seats Available:
                            <strong>${train.seats}</strong>
                        </p>

                        <p>
                            Fare:
                            <strong>₹${train.price}</strong>
                        </p>
                    </div>

                    <button class="book-btn">
                        Book Now
                    </button>
                `;

                const bookButton =
                    card.querySelector(".book-btn");

                bookButton.addEventListener("click", function () {

                    localStorage.setItem(
                        "selectedTrain",
                        JSON.stringify(train)
                    );

                    localStorage.removeItem("booking");

                    window.location.href = "booking.html";

                });

                trainList.appendChild(card);

            });

        }

        results.classList.remove("hidden");

        results.scrollIntoView({
            behavior: "smooth"
        });

    });


    newSearch.addEventListener("click", function () {

        results.classList.add("hidden");

        document.getElementById("search").scrollIntoView({
            behavior: "smooth"
        });

    });


    swap.addEventListener("click", function () {

        const fromInput = document.getElementById("from");
        const toInput = document.getElementById("to");

        const temp = fromInput.value;

        fromInput.value = toInput.value;
        toInput.value = temp;

    });

});