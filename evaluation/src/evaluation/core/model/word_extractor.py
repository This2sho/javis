import re

from kiwipiepy import Kiwi

kiwi = Kiwi()

complexity_pattern = re.compile(r"O\([a-zA-Z0-9\s\*\/\+\-]+\)", re.IGNORECASE)

def extract_meaningful_words(sentence: str) -> list[str]:
    words = []
    complexities = complexity_pattern.findall(sentence)
    words.extend(complexities)
    sentence = complexity_pattern.sub('', sentence)

    result = kiwi.tokenize(sentence)
    for token in result:
        if token.tag.startswith("N") or token.tag.startswith("V") or token.tag in ["XR", "SL"]:
            words.append(token.lemma)
    return words

def extract_meaningful_words_from_keywords(keywords: list[str]) -> list[str]:
    meaningful_words = []
    for keyword in keywords:
        matches = complexity_pattern.findall(keyword)
        if matches:
            meaningful_words.extend(matches)
            continue

        tokens = kiwi.tokenize(keyword)
        for token in tokens:
            if token.tag.startswith("N") or token.tag.startswith("V") or token.tag in ["XR", "SL"]:
                meaningful_words.append(token.lemma)
    return meaningful_words