/* =========================
   Problem Form (공용)
========================= */

let problemIdSeq = 0;
const problemFormMessages = window.problemFormMessages || {};
const difficultyLabels = window.difficultyLabels || {};

function problemFormMessage(key, fallback) {
    return problemFormMessages[key] || fallback;
}

function difficultyLabel(difficulty) {
    return difficultyLabels[difficulty] || difficulty;
}

/* =========================
   Problem Node 생성
========================= */
function createProblemNode(parentCategoryPath = "", isFollowUp = false) {
    const problemId = problemIdSeq++;

    const box = document.createElement("div");
    box.className = "problem-box";
    box.dataset.id = problemId;

    const deleteBtn = isFollowUp
        ? `<button class="delete-followup" onclick="deleteFollowUpProblem(this)">🗑️ ${problemFormMessage("delete", "Delete")}</button>`
        : '';

    box.innerHTML = `
        <div class="problem-header">
            ${deleteBtn}
        </div>
        <div class="field">
            <label>${problemFormMessage("content", "Question")}</label>
            <textarea data-field="content"></textarea>
        </div>

        <div class="field">
            <label>${problemFormMessage("referenceAnswer", "Reference Answer")}</label>
            <textarea data-field="expectedAnswer"></textarea>
        </div>

        <div class="field">
            <label>${problemFormMessage("difficulty", "Difficulty")}</label>
            <div class="difficulty-selector" id="difficulty-selector-${problemId}"></div>
            <input type="hidden" data-field="difficulty">
        </div>

        <div class="field">
            <label>${problemFormMessage("category", "Select Category")}</label>
            <div class="category-selector" id="category-selector-${problemId}"></div>
            <div class="selected-category" id="selected-category-${problemId}">
                ${parentCategoryPath || problemFormMessage("categoryUnselected", "Not selected")}
            </div>
            <input type="hidden" data-field="category" value="${parentCategoryPath}">
        </div>

        <button class="add-followup" onclick="addFollowUpProblem(${problemId})">
            ➕ ${problemFormMessage("addFollowUp", "Add Follow-up Question")}
        </button>

        <div class="children" id="children-${problemId}"></div>
    `;

    renderDifficultySelector(DIFFICULTIES, box.querySelector(`#difficulty-selector-${problemId}`), problemId);
    renderCategorySelector(CATEGORY_TREE, box.querySelector(`#category-selector-${problemId}`), problemId);

    return box;
}

/* =========================
   난이도 선택
========================= */
function renderDifficultySelector(difficulties, container, id) {
    if (!container) return;

    difficulties.forEach(difficulty => {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "difficulty-btn";
        btn.dataset.value = difficulty;
        btn.innerText = difficultyLabel(difficulty);

        btn.onclick = () => {
            const box = document.querySelector(`[data-id="${id}"]`);
            box.querySelector('[data-field="difficulty"]').value = difficulty;

            container.querySelectorAll(".difficulty-btn")
                .forEach(b => b.classList.remove("selected"));

            btn.classList.add("selected");
        };

        container.appendChild(btn);
    });
}

/* =========================
   카테고리 단계형 선택
========================= */
function renderCategorySelector(nodes, container, id, parentPath = "", depth = 0) {
    Array.from(container.children)
        .filter(row => Number(row.dataset.depth) >= depth)
        .forEach(row => row.remove());

    const row = document.createElement("div");
    row.className = "category-row";
    row.dataset.depth = depth;

    nodes.forEach(node => {
        const item = document.createElement("div");
        item.className = "category-item";
        item.innerText = node.name;

        const currentPath = parentPath
            ? parentPath + ":" + node.name
            : node.name;

        item.onclick = () => {
            selectCategory(id, currentPath, item);

            if (node.children && node.children.length > 0) {
                renderCategorySelector(
                    node.children,
                    container,
                    id,
                    currentPath,
                    depth + 1
                );
            }
        };

        row.appendChild(item);
    });

    container.appendChild(row);
}

function selectCategory(id, path, clickedItem) {
    const box = document.querySelector(`[data-id="${id}"]`);
    box.querySelector('[data-field="category"]').value = path;
    box.querySelector(`#selected-category-${id}`).innerText = path;

    box.querySelectorAll(".category-item")
        .forEach(el => el.classList.remove("selected"));

    clickedItem.classList.add("selected");
}

/* =========================
   꼬리 문제
========================= */
function addFollowUpProblem(parentId) {
    const parentBox = document.querySelector(`[data-id="${parentId}"]`);
    const parentCategory = parentBox.querySelector('[data-field="category"]').value;

    const child = createProblemNode(parentCategory, true);
    document.getElementById(`children-${parentId}`).appendChild(child);
}

function deleteFollowUpProblem(button) {
    const box = button.closest(".problem-box");
    const problemId = box.dataset.problemId;

    if (problemId && typeof trackDeletedProblem === 'function') {
        trackDeletedProblem(Number(problemId));
    }

    box.remove();
}

/* =========================
   수집
========================= */
function collectProblemNode(box) {
    const id = box.dataset.problemId
        ? Number(box.dataset.problemId)
        : null;

    const problem = box.querySelector('[data-field="content"]').value;
    const referenceAnswer = box.querySelector('[data-field="expectedAnswer"]').value;
    const category = box.querySelector('[data-field="category"]').value;
    const difficulty = box.querySelector('[data-field="difficulty"]').value;

    const followUpProblems = [];
    box.querySelectorAll(':scope > .children > .problem-box')
        .forEach(child => followUpProblems.push(collectProblemNode(child)));

    return {
        problemId: id,
        problem,
        referenceAnswer,
        difficulty,
        category,
        followUpProblems
    };
}
