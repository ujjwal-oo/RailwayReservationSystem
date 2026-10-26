
document.addEventListener("DOMContentLoaded", function () {

    const $ = (id) => document.getElementById(id);

    const params = new URLSearchParams(window.location.search);
    let pnr = params.get("pnr") || localStorage.getItem("lastPNR");

    if (!pnr) {
        alert("No booking found.");
        window.location.href = "index.html";
        return;
    }

    const BERTH_NAMES = {
        LOWER: "Lower", MIDDLE: "Middle", UPPER: "Upper",
        SIDE_LOWER: "Side Lower", SIDE_UPPER: "Side Upper", SEAT: "Seat"
    };

    const STATUS_VIEW = {
        CONFIRMED: {
            icon: "✓", tag: "BOOKING CONFIRMED", title: "Your ticket is confirmed!",
            subtitle: "Your railway reservation has been successfully saved to the system.",
            badge: "✓ CONFIRMED", css: "status-confirmed", color: "#1b8a4b", bg: "#e5f6ec"
        },
        WAITLISTED: {
            icon: "⏳", tag: "WAITLISTED", title: "You are on the waiting list",
            subtitle: "No berth was free. The booking is saved and will be confirmed if a berth opens up.",
            badge: "⏳ WAITLISTED", css: "status-waitlisted", color: "#a15c00", bg: "#fff1d9"
        },
        CANCELLED: {
            icon: "✕", tag: "CANCELLED", title: "This ticket is cancelled",
            subtitle: "You can undo the cancellation below.",
            badge: "✕ CANCELLED", css: "status-cancelled", color: "#c02424", bg: "#fde8e8"
        }
    };

    function message(text, ok) {
        const el = $("pageMessage");
        el.textContent = text || "";
        el.className = "page-message " + (text ? (ok ? "ok" : "err") : "");
    }

    function post(url, body) {
        return fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body)
        }).then((r) => r.json());
    }
    function render(t) {

        const view = STATUS_VIEW[t.status] || STATUS_VIEW.CONFIRMED;

        $("statusIcon").textContent = view.icon;
        $("statusIcon").style.color = view.color;
        $("statusIcon").style.background = view.bg;
        $("statusTag").textContent = view.tag;
        $("statusTitle").textContent = view.title;
        $("statusSubtitle").textContent = view.subtitle;
        $("statusBadge").textContent = view.badge;
        $("statusBadge").className = "status-badge " + view.css;

        $("pnr").textContent = t.pnr;
        $("passenger").textContent = t.passenger_name;
        $("ageGender").textContent = `${t.age} / ${t.gender || "--"}`;
        $("train").textContent = t.train_name;
        $("from").textContent = t.source;
        $("to").textContent = t.destination;
        $("journeyDate").textContent = t.journey_date || "--";
        $("class").textContent = t.travel_class;
        $("fare").textContent = t.fare;

        const cats = {
            SENIOR_CITIZEN: "Senior citizen (40% off)",
            CHILD: "Child fare (50% off)",
            TATKAL: "Tatkal (+30%)",
            REGULAR: ""
        };
        $("fareCategory").textContent = cats[t.fare_category] || "";
        if (t.coach && t.berth_no > 0) {
            const type = BERTH_NAMES[t.berth_type] || t.berth_type;
            $("berth").textContent =
                t.berth_type === "SEAT"
                    ? `${t.coach} / Seat ${t.berth_no}`
                    : `${t.coach} / ${t.berth_no} (${type})`;
        } else {
            $("berth").textContent = t.status === "WAITLISTED" ? "Waitlisted" : "Not allotted";
        }

        const note = $("berthNote");
        if (t.berth_note) {
            note.textContent = t.berth_note;
            note.classList.remove("hidden");
            note.classList.toggle("good", /lower berth allotted/i.test(t.berth_note));
        } else {
            note.classList.add("hidden");
        }
        const cancelled = t.status === "CANCELLED";
        $("cancelBtn").classList.toggle("hidden", cancelled);
        $("undoBtn").classList.toggle("hidden", !(cancelled && t.can_undo));
        $("rebookSection").classList.toggle("hidden", cancelled);
        const list = $("historyList");
        list.innerHTML = "";
        (t.history || []).forEach(function (h) {
            const li = document.createElement("li");
            const where = h.coach ? ` · ${h.coach}-${h.berth_no} ${BERTH_NAMES[h.berth_type] || ""}` : "";
            li.textContent = `${h.saved_at} — ${h.label.replace(/_/g, " ").toLowerCase()}: was ${h.status}${where}`;
            list.appendChild(li);
        });
        $("historySection").classList.toggle("hidden", !(t.history && t.history.length));
    }

    function load() {
        return fetch("/api/ticket?pnr=" + encodeURIComponent(pnr))
            .then((r) => r.json())
            .then(function (t) {
                if (!t.success) {
                    message(t.message || "Ticket not found", false);
                    return;
                }
                render(t);
            })
            .catch(function () {
                message("Unable to connect to Java server.", false);
            });
    }
    $("cancelBtn").addEventListener("click", function () {
        if (!confirm("Cancel this ticket? You can undo it afterwards.")) return;
        post("/api/cancel", { pnr: pnr }).then(function (r) {
            message(r.success ? "Ticket cancelled." : r.message, r.success);
            load();
        });
    });

    $("undoBtn").addEventListener("click", function () {
        post("/api/undo", { pnr: pnr }).then(function (r) {
            message(r.success ? "Cancellation undone." : r.message, r.success);
            load();
        });
    });
    const dateInput = $("rebookDate");
    const today = new Date().toISOString().slice(0, 10);
    dateInput.min = today;

    function rebook(body) {
        if (!dateInput.value) {
            message("Please choose a date first.", false);
            return;
        }
        body.pnr = pnr;
        body.journey_date = dateInput.value;

        post("/api/rebook", body).then(function (r) {
            if (r.success) {
                localStorage.setItem("lastPNR", r.pnr);
                window.location.href = "ticket.html?pnr=" + r.pnr;
            } else {
                message(r.message || "Could not create booking.", false);
            }
        }).catch(function () {
            message("Unable to connect to Java server.", false);
        });
    }

    $("returnBtn").addEventListener("click", function () {
        rebook({ mode: "RETURN" });
    });

    $("companionBtn").addEventListener("click", function () {
        const name = $("compName").value.trim();
        const age = parseInt($("compAge").value);
        if (!name || !age) {
            message("Enter the passenger's name and age.", false);
            return;
        }
        rebook({
            mode: "COMPANION",
            passenger_name: name,
            age: age,
            gender: $("compGender").value
        });
    });

    load().then(function () {
        const original = $("journeyDate").textContent;
        if (/^\d{4}-\d{2}-\d{2}$/.test(original)) dateInput.value = original;
    });
});
