CREATE TABLE IF NOT EXISTS template.templates
(
    ts DateTime,
    id UUID,
    text LowCardinality(String)
) ENGINE = ReplacingMergeTree()
ORDER BY (id)
PRIMARY KEY (id)
PARTITION BY toYear(ts)
