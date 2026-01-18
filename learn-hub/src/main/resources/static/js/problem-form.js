/* =========================
   Problem Form (공용)
========================= */

let problemIdSeq = 0;

/* =========================
   Problem Node 생성
========================= */
function createProblemNode(parentCategoryPath = "") {
    const problemId = problemIdSeq++;

    const box = document.createElement("div");
    box.className = "problem-box";
    box.dataset.id = problemId;

    box.innerHTML = `
        <div class="field">
            <label>문제</label>
            <textarea data-field="content"></textarea>
        </div>

        <div class="field">
            <label>예상 답변</label>
            <textarea data-field="expectedAnswer"></textarea>
        </div>

        <div class="field">
            <label>난이도</label>
            <div class="difficulty-selector" id="difficulty-selector-${problemId}"></div>
            <input type="hidden" data-field="difficulty">
        </div>

        <div class="field">
            <label>핵심 키워드</label>
            <div class="keyword-input">
                <input type="text" placeholder="키워드 입력">
                <button type="button" onclick="addKeyword(this)">+</button>
            </div>
            <div class="keyword-list"></div>
        </div>

        <div class="field">
            <label>카테고리 선택</label>
            <div class="category-selector" id="category-selector-${problemId}"></div>
            <div class="selected-category" id="selected-category-${problemId}">
                ${parentCategoryPath || "선택되지 않음"}
            </div>
            <input type="hidden" data-field="category" value="${parentCategoryPath}">
        </div>

        <button class="add-followup" onclick="addFollowUpProblem(${problemId})">
            ➕ 꼬리 문제 추가
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
        btn.innerText = difficulty;

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
   키워드
========================= */
function addKeyword(button) {
    const field = button.closest(".field");
    const input = field.querySelector("input");
    const list = field.querySelector(".keyword-list");
    const value = input.value.trim();
    if (!value) return;

    const tag = document.createElement("span");
    tag.className = "keyword-tag";
    tag.innerHTML = `
        ${value}
        <button type="button" onclick="this.parentElement.remove()">×</button>
    `;

    list.appendChild(tag);
    input.value = "";
}

/* =========================
   꼬리 문제
========================= */
function addFollowUpProblem(parentId) {
    const parentBox = document.querySelector(`[data-id="${parentId}"]`);
    const parentCategory = parentBox.querySelector('[data-field="category"]').value;

    const child = createProblemNode(parentCategory);
    document.getElementById(`children-${parentId}`).appendChild(child);
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

    const keywords = Array.from(
        box.querySelector(".keyword-list")?.querySelectorAll(".keyword-tag") || []
    ).map(tag => tag.firstChild.textContent.trim());

    const followUpProblems = [];
    box.querySelectorAll(':scope > .children > .problem-box')
        .forEach(child => followUpProblems.push(collectProblemNode(child)));

    return {
        id,
        problem,
        referenceAnswer,
        difficulty,
        keywords,
        category,
        followUpProblems
    };
}

