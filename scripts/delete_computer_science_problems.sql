START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS cs_root_problem_ids;
CREATE TEMPORARY TABLE cs_root_problem_ids (
    id BIGINT PRIMARY KEY
)
SELECT p.id
FROM problem p
JOIN category c ON c.id = p.category_id
WHERE p.parent_problem_id = -1
  AND (c.path = 'computer_science' OR c.path LIKE 'computer_science:%');

DROP TEMPORARY TABLE IF EXISTS cs_problem_ids;
CREATE TEMPORARY TABLE cs_problem_ids (
    id BIGINT PRIMARY KEY
)
WITH RECURSIVE problem_tree AS (
    SELECT id
    FROM cs_root_problem_ids
    UNION ALL
    SELECT child.id
    FROM problem child
    JOIN problem_tree parent ON child.parent_problem_id = parent.id
)
SELECT DISTINCT id
FROM problem_tree;

SELECT COUNT(*) AS root_problem_count FROM cs_root_problem_ids;
SELECT COUNT(*) AS total_problem_count FROM cs_problem_ids;

DELETE FROM problem_scoring_info
WHERE problem_id IN (SELECT id FROM cs_problem_ids);

DELETE FROM review
WHERE root_problem_id IN (SELECT id FROM cs_root_problem_ids);

DELETE FROM problem
WHERE id IN (SELECT id FROM cs_problem_ids);

COMMIT;
