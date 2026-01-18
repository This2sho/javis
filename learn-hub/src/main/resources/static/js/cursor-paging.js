window.fetchCursorPage = async function ({
                                             url,
                                             params = {},
                                             cursor = null,
                                             credentials = "include"
                                         }) {
    const query = new URLSearchParams(params);

    if (cursor && cursor.targetTime && cursor.targetId !== -1) {
        query.append("targetUpdatedAt", cursor.targetTime);
        query.append("targetId", cursor.targetId);
    }

    const response = await fetch(`${url}?${query.toString()}`, {
        credentials
    });

    if (!response.ok) {
        throw new Error("Cursor paging request failed");
    }

    return response.json(); // CursorPageResponse<T>
};
