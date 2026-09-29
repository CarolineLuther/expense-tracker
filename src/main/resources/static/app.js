const CATEGORY_LABELS = {
    FOOD: "Mat",
    TRANSPORT: "Transport",
    ENTERTAINMENT: "Nöje",
    HOUSING: "Boende",
    OTHER: "Övrigt"
};

const form = document.getElementById("expense-form");
const expenseList = document.getElementById("expense-list");
const summaryList = document.getElementById("summary-list");
const totalSum = document.getElementById("total-sum");

async function loadExpenses() {
    const response = await fetch("/api/expenses");
    const expenses = await response.json();
    renderExpenses(expenses);
}

async function loadSummary() {
    const response = await fetch("/api/expenses/summary");
    const summary = await response.json();
    renderSummary(summary);
}

function renderExpenses(expenses) {
    expenseList.innerHTML = "";
    for (const expense of expenses) {
        const li = document.createElement("li");
        li.dataset.testid = "expense-item";
        li.innerHTML = `
            <span>${expense.description} — ${CATEGORY_LABELS[expense.category]} — ${expense.date}</span>
            <span>
                ${expense.amount} kr
                <button data-testid="delete-button" data-id="${expense.id}">Ta bort</button>
            </span>
        `;
        expenseList.appendChild(li);
    }
}

function renderSummary(summary) {
    summaryList.innerHTML = "";
    for (const [category, amount] of Object.entries(summary.perCategory)) {
        const li = document.createElement("li");
        li.textContent = `${CATEGORY_LABELS[category]}: ${amount} kr`;
        summaryList.appendChild(li);
    }
    totalSum.textContent = summary.total;
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const expense = {
        description: document.getElementById("description").value,
        amount: document.getElementById("amount").value,
        category: document.getElementById("category").value,
        date: document.getElementById("date").value
    };

    const response = await fetch("/api/expenses", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(expense)
    });

    if (response.ok) {
        form.reset();
        await refresh();
    } else {
        alert("Kunde inte lägga till utgiften. Kontrollera fälten.");
    }
});

expenseList.addEventListener("click", async (event) => {
    if (event.target.dataset.testid === "delete-button") {
        const id = event.target.dataset.id;
        await fetch(`/api/expenses/${id}`, { method: "DELETE" });
        await refresh();
    }
});

async function refresh() {
    await loadExpenses();
    await loadSummary();
}

refresh();