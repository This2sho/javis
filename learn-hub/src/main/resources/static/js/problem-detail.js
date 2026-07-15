/* =========================
   Problem Detail Page
========================= */

const problemDetailMessages = window.problemDetailMessages || {};
const problemDetailDifficultyLabels = window.difficultyLabels || {};

function problemDetailMessage(key, fallback) {
    return problemDetailMessages[key] || fallback;
}

function problemDetailDifficultyLabel(difficulty) {
    return problemDetailDifficultyLabels[difficulty] || difficulty;
}

/* =========================
   VIEW MODE
========================= */
function renderViewMode(problem) {
    const root = document.getElementById("root");
    root.innerHTML = `
        <div class="problem-box">
            <h2>${problem.content}</h2>
            <p><strong>${problemDetailMessage("difficulty", "Difficulty")}:</strong> ${problemDetailDifficultyLabel(problem.difficulty)}</p>
            <p><strong>${problemDetailMessage("category", "Category")}:</strong> ${problem.category}</p>
            <p><strong>${problemDetailMessage("referenceAnswer", "Reference Answer")}</strong></p>
            <pre>${problem.referenceAnswer}</pre>

            <button onclick="enterEditMode()">${problemDetailMessage("edit", "Edit")}</button>
        </div>
    `;
}

/* =========================
   EDIT MODE
========================= */
function enterEditMode() {
    const root = document.getElementById("root");
    root.innerHTML = "";

    const form = createProblemNode(currentProblem.category);
    root.appendChild(form);

    fillForm(form, currentProblem);

    const saveBtn = document.createElement("button");
    saveBtn.className = "submit-btn";
    saveBtn.innerText = problemDetailMessage("save", "Save Changes");
    saveBtn.onclick = submitUpdate;

    root.appendChild(saveBtn);
}

function fillForm(box, problem) {
    box.querySelector('[data-field="content"]').value = problem.content;
    box.querySelector('[data-field="expectedAnswer"]').value = problem.referenceAnswer;
    box.querySelector('[data-field="category"]').value = problem.category;
    box.querySelector('[data-field="difficulty"]').value = problem.difficulty;

    box.querySelector(`#selected-category-${box.dataset.id}`).innerText = problem.category;

    box.querySelectorAll(".difficulty-btn").forEach(btn => {
        if (btn.dataset.value === problem.difficulty) {
            btn.classList.add("selected");
        }
    });

}

/* =========================
   UPDATE
========================= */
function submitUpdate() {
    const box = document.querySelector('.problem-box');
    const payload = collectProblemNode(box);

    fetch(`/api/problems/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(payload)
    })
        .then(res => {
            if (!res.ok) throw new Error(problemDetailMessage("updateFailed", "Failed to update the problem."));
            window.location.reload();
        })
        .catch(() => alert(problemDetailMessage("updateFailed", "Failed to update the problem.")));
}
