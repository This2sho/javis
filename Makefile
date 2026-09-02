.PHONY: bench-async-answer-only-current

bench-async-answer-only-current:
	REBUILD_IMAGE="$(REBUILD_IMAGE)" \
	DOCKER_CPUS="$(DOCKER_CPUS)" \
	EVAL_CORE_POOL_SIZE="$(EVAL_CORE_POOL_SIZE)" \
	EVAL_MAX_POOL_SIZE="$(EVAL_MAX_POOL_SIZE)" \
	EVAL_QUEUE_CAPACITY="$(EVAL_QUEUE_CAPACITY)" \
	RESULT_BASENAME="$(RESULT_BASENAME)" \
	zsh ./scripts/run_async_answer_only_1m_benchmark.sh
