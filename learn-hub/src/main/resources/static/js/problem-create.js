/* =========================
   Problem Create Page
========================= */

document.getElementById("root").appendChild(
    createProblemNode("")
);

function submitProblem() {
    const rootBox = document.querySelector('#root .problem-box');
    const payload = collectProblemNode(rootBox);

    fetch("/api/problems", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(payload)
    })
        .then(res => {
            if (!res.ok) throw new Error("문제 생성 실패");
            const location = res.headers.get("Location");
            if (!location) throw new Error("Location 헤더 없음");
            window.location.href = location;
        })
        .catch(err => {
            console.error(err);
            alert("문제 생성 중 오류가 발생했습니다.");
        });
}
