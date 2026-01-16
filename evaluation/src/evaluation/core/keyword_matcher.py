from torch.nn.functional import cosine_similarity

from evaluation.core import embedding_model
from evaluation.core.model import word_extractor


def find_similar_keywords(
    user_answer: str,
    keywords: list[str],
    threshold: float = 0.65
) -> dict:
    included_1st, remaining = match_keywords_by_raw_text(
        user_answer,
        keywords
    )

    included_2nd, missing = match_keywords_by_embedding(
        user_answer,
        remaining,
        threshold
    ) if remaining else ([], [])

    included_keywords = included_1st + included_2nd

    return {
        "keywords": keywords,
        "included_keywords": included_keywords,
        "missing_keywords": missing
    }


def match_keywords_by_raw_text(
    user_answer: str,
    keywords: list[str]
) -> tuple[list[str], list[str]]:
    included = []
    remaining = []

    for keyword in keywords:
        if keyword in user_answer:
            included.append(keyword)
        else:
            remaining.append(keyword)

    return included, remaining

def match_keywords_by_embedding(
    user_answer: str,
    keywords: list[str],
    threshold: float
) -> tuple[list[str], list[str]]:
    included = []
    missing = []

    user_words = word_extractor.extract_meaningful_words(user_answer)
    user_embeddings = [embedding_model.encode(word) for word in user_words]

    processed_keywords = word_extractor.extract_meaningful_words_from_keywords(keywords)
    keyword_embeddings = [
        embedding_model.encode(keyword)
        for keyword in processed_keywords
    ]

    for idx, keyword_embedding in enumerate(keyword_embeddings):
        found = False
        for user_embedding in user_embeddings:
            similarity = cosine_similarity(
                keyword_embedding,
                user_embedding,
                dim=0
            ).item()
            if similarity >= threshold:
                found = True
                break

        original_keyword = keywords[idx]
        if found:
            included.append(original_keyword)
        else:
            missing.append(original_keyword)

    return included, missing
