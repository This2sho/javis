/* =========================
   Problem Detail Page
========================= */

/* =========================
   VIEW MODE
========================= */
function renderViewMode(problem) {
    const root = document.getElementById("root");
    root.innerHTML = `
        <div class="problem-box">
            <h2>${problem.content}</h2>
            <p><strong>난이도:</strong> ${problem.difficulty}</p>
            <p><strong>카테고리:</strong> ${problem.category}</p>
            <p><strong>예상 답변</strong></p>
            <pre>${problem.referenceAnswer}</pre>
            <p><strong>키워드:</strong> ${problem.keywords}</p>

            <button onclick="enterEditMode()">수정</button>
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
    saveBtn.innerText = "수정 저장";
    saveBtn.onclick = submitUpdate;

    root.appendChild(saveBtn);
}

function fillForm(box, problem) {
    box.querySelector('[data-field="content"]').value = problem.content;
    box.querySelector('[data-field="expectedAnswer"]').value = problem.referenceAnswer;
    box.querySelector('[data-field="category"]').value = problem.category;
    box.querySelector('[data-field="difficulty"]').value = problem.difficulty;

    box.querySelector(`#selected-category-${box.dataset.id}`).innerText = problem.category;

    // difficulty 버튼 선택
    box.querySelectorAll(".difficulty-btn").forEach(btn => {
        if (btn.innerText === problem.difficulty) {
            btn.classList.add("selected");
        }
    });

    // keywords
    const list = box.querySelector(".keyword-list");
    problem.keywords.split(",").forEach(k => {
        const tag = document.createElement("span");
        tag.className = "keyword-tag";
        tag.innerHTML = `${k.trim()} <button type="button" onclick="this.parentElement.remove()">×</button>`;
        list.appendChild(tag);
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
            if (!res.ok) throw new Error("수정 실패");
            window.location.reload();
        })
        .catch(() => alert("문제 수정 실패"));
}
