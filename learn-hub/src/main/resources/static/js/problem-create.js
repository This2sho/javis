/* =========================
   Problem Create Page
========================= */

const problemCreateMessages = window.problemCreateMessages || {};

function problemCreateMessage(key, fallback) {
    return problemCreateMessages[key] || fallback;
}

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
            if (!res.ok) throw new Error(problemCreateMessage("failed", "Failed to create the problem."));
            const location = res.headers.get("Location");
            if (!location) throw new Error(problemCreateMessage("missingLocation", "Location header is missing."));
            window.location.href = location;
        })
        .catch(err => {
            console.error(err);
            alert(problemCreateMessage("error", "An error occurred while creating the problem."));
        });
}
