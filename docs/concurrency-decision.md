# Concurrency Decision

Two partial PostgreSQL GiST exclusion constraints combine resource equality with
`tstzrange(start_time,end_time,'[)') &&` for Confirmed appointments. One protects technicians and the
other service bays. These constraints protect every writer and every application instance.

Application availability queries are advisory. The non-transactional coordinator invokes a sole
`REQUIRES_NEW` attempt and may try at most three distinct candidate pairs after complete rollbacks.
Known exclusion violations become HTTP 409 without leaking constraint names. Unknown database failures
remain operational errors. Pessimistic locks require coarse resource locking; serializable isolation
requires broad retries. Exclusion constraints express the scheduling invariant directly.
